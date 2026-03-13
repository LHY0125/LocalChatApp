package client.service;

import client.util.EventBus;
import global.Global;
import server.data.GroupData;
import server.data.UserData;
import server.serveice.Wrapper;
import util.MsgUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import javafx.application.Platform;

/**
 * 客户端消息接收服务线程
 * <p>
 * 该类负责维护与服务端的长连接，持续监听并接收服务端推送的消息包（Wrapper）。
 * 作为客户端的“下行数据通道”，它将接收到的原始数据根据操作码（Operation Code）进行解析和分发，
 * 驱动本地数据更新（LocalData）。
 * <p>
 * 注意：ChatReceiver 不再直接操作 UI，而是只负责更新 LocalData 中的 Observable 数据。
 * UI 绑定了这些数据，会自动刷新。
 * 对于需要弹窗的事件，通过 LocalData.addSystemMessage() 通知 UI。
 */
public class ChatReceiver extends Thread {
    // 客户端本地Socket，用于与服务端通信
    private final Socket localSocket;
    ObjectInputStream ois;
    private static boolean isRunning;

    /**
     * 构造函数
     * <p>
     * 初始化接收线程，绑定到已连接的客户端Socket。
     * 创建对象输入流，用于从Socket读取服务端消息。
     *
     * @param localSocket  已连接的客户端Socket
     * @param messageQueue 消息处理队列
     * @throws IOException 如果获取输入流失败
     */
    public ChatReceiver(Socket localSocket, BlockingQueue<Wrapper> messageQueue) {
        this.localSocket = localSocket;
        isRunning = true;
    }

    /**
     * 线程主循环：持续阻塞读取服务端消息并分发处理
     */
    @Override
    public void run() {

        try {
            ois = new ObjectInputStream(localSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("ChatReceiver 获取输入流失败，线程退出: " + e.getMessage());
            isRunning = false;
            return;
        }

        while (isRunning) {
            try {
                // 阻塞式读取服务端发送的 Wrapper 对象
                Object obj = ois.readObject();
                if (obj instanceof Wrapper) {
                    Wrapper message = (Wrapper) obj;
                    System.out.println("收到消息：" + message.getOperation());
                    // 所有对 Observable 数据的修改，建议在 JavaFX 线程执行，或者依靠 Observable 自身的线程安全实现
                    // 为了保险，统一用 Platform.runLater 包裹数据更新操作
                    Platform.runLater(() -> handleMessage(message));
                }
            }
            // 捕获 IO 异常（如连接中断）
            catch (IOException e) {
                System.out.println("ChatReceiver 连接中断: " + e.getMessage());
                isRunning = false;
                LocalData.get().addSystemMessage("服务器连接中断");
                break;
            }
            // 捕获反序列化异常
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void stopRunning() {
        ChatReceiver.isRunning = false;
    }

    /**
     * 消息处理核心方法：根据操作码分发业务逻辑
     * 
     * @param msg 服务端下发的消息包
     */
    @SuppressWarnings("unchecked")
    private void handleMessage(Wrapper msg) {
        int opt = msg.getOperation();
        switch (opt) {
            case Global.OPT_QUEST_WRONG:
                System.out.println("出现未知异常");
                EventBus.getInstance().publish("ERROR", "未知异常");
                break;

            case Global.OPT_REGISTER_FAILED_ACC:
                LocalData.get().addSystemMessage("账号已存在");
                EventBus.getInstance().publish("REGISTER_FAILED", "账号已存在");
                break;

            case Global.OPT_REGISTER_SUCCESS:
            case Global.OPT_LOGIN_SUCCESS:
                System.out.println("登录/注册成功");
                // 登录成功后，LocalData 的 ID 已经在 LoginController 设置过了
                LocalData.get().addSystemMessage("LOGIN_SUCCESS");

                // 发布登录成功事件，通知 LoginPage 切换界面
                EventBus.getInstance().publish("LOGIN_SUCCESS", null);

                ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), Global.OPT_INIT_USER));
                ChatSender.addMsg(Wrapper.simpleRequest(LocalData.get().getId(), null, Global.OPT_INIT_USER_DETAIL));
                ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), Global.OPT_INIT_GROUP));
                ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), Global.OPT_INIT_CHAT));
                break;

            case Global.OPT_LOGIN_FAILED_ACC:
                LocalData.get().addSystemMessage("账号不存在");
                EventBus.getInstance().publish("LOGIN_FAILED", "账号不存在");
                break;

            case Global.OPT_LOGIN_FAILED_PWD:
                LocalData.get().addSystemMessage("密码错误");
                EventBus.getInstance().publish("LOGIN_FAILED", "密码错误");
                break;

            case Global.OPT_LOGIN_FAILED_REPEATED:
                LocalData.get().addSystemMessage("账户已登录,请勿重复登录");
                EventBus.getInstance().publish("LOGIN_FAILED", "账户已登录,请勿重复登录");
                break;

            case Global.OPT_ERROR_NOT_LOGIN:
                System.out.println("未知的登录问题？");
                EventBus.getInstance().publish("LOGIN_FAILED", "未知的登录问题");
                break;

            case Global.OPT_LOGOUT:
                LocalData.get().addSystemMessage("LOGOUT_SUCCESS");
                EventBus.getInstance().publish("LOGOUT_SUCCESS", null);
                break;

            case Global.OPT_DELETE_ACCOUNT:
                LocalData.get().addSystemMessage("账号已被删除");
                LocalData.get().addSystemMessage("LOGOUT_SUCCESS");
                EventBus.getInstance().publish("LOGOUT_SUCCESS", "账号已被删除");
                break;

            case Global.OPT_UPDATE_NICKNAME:
                LocalData.get().setUserName(msg.getSenderId(), (String) msg.getData());
                break;

            case Global.OPT_UPDATE_PASSWORD:
                LocalData.get().addSystemMessage("密码已经更新");
                break;

            case Global.OPT_USER_UPDATE_NAME_FAILED:
                LocalData.get().addSystemMessage("昵称修改失败");
                break;

            case Global.OPT_USER_UPDATE_PASSWORD_FAILED:
                LocalData.get().addSystemMessage("密码修改失败");
                break;

            case Global.OPT_GROUP_CREATE_SUCCESS:
                LocalData.get().addSystemMessage("群组创建成功");
                EventBus.getInstance().publish("GROUP_CREATED", "群组创建成功");
                break;

            case Global.OPT_GROUP_INVITE:
                // 群组变更（新建/被拉入）：更新本地群组列表并刷新左侧导航栏
                // 这是一个需要用户交互的请求，用特殊前缀标记
                LocalData.get().addSystemMessage("INVITE_REQUEST|" + msg.getSenderId() + "|" +
                        LocalData.get().getUserName(msg.getSenderId()) + "|" +
                        (String) msg.getData() + "|" + msg.getGroupId());
                EventBus.getInstance().publish("INVITE_REQUEST", msg);
                break;

            case Global.OPT_GROUP_INVITE_REFUSE:
                LocalData.get().addSystemMessage("对方拒绝了你的邀请");
                EventBus.getInstance().publish("INVITE_REFUSED", "对方拒绝了你的邀请");
                break;

            case Global.OPT_GROUP_INVITE_OFFLINE:
                LocalData.get().addSystemMessage("邀请的用户并不在线");
                EventBus.getInstance().publish("ERROR", "邀请的用户并不在线");
                break;

            case Global.OPT_GROUP_QUIT:
                handleGroupQuit(msg);
                EventBus.getInstance().publish("GROUP_QUIT", msg.getGroupId());
                break;

            case Global.OPT_GROUP_DISBAND:
                LocalData.get().addSystemMessage("群聊已被解散");
                EventBus.getInstance().publish("GROUP_DISBANDED", "群聊已被解散");
                break;

            case Global.OPT_CHAT:
                handleChatRequest(msg);
                EventBus.getInstance().publish("NEW_CHAT_MESSAGE", msg);
                break;

            case Global.OPT_PRIVATE_CHAT:
                handlePrivateChatRequest(msg);
                EventBus.getInstance().publish("NEW_PRIVATE_MESSAGE", msg);
                break;

            case Global.OPT_GROUP_UPDATE_NAME:
                // 群信息变更：更新群名显示
                LocalData.get().setGroupName(msg.getGroupId(), (String) msg.getData());
                break;

            case Global.OPT_GROUP_UPDATE_OWNER:
                LocalData.get().getGroupData(msg.getGroupId()).setGroupOwner((String) msg.getData());
                break;

            case Global.OPT_USER_UPDATE_NAME_FAILED_WRONG_FORMAT:
                LocalData.get().addSystemMessage("用户名格式错误，请重新输入");
                break;

            case Global.OPT_GROUP_JOIN_FAILED:
                LocalData.get().addSystemMessage("加入群聊失败：群组不存在");
                break;

            case Global.OPT_FRIEND_ADD_SUCCESS:
                handleFriendAddSuccess(msg);
                EventBus.getInstance().publish("FRIEND_ADDED", "添加好友成功");
                break;

            case Global.OPT_FRIEND_ADD_FAILED:
                LocalData.get().addSystemMessage("添加好友失败：用户不存在");
                EventBus.getInstance().publish("ERROR", "添加好友失败：用户不存在");
                break;

            case Global.OPT_INIT_USER_DETAIL:
                // 初始化所有用户详细信息
                Map<String, UserData> userDetails = (Map<String, UserData>) msg.getData();
                LocalData.get().setUserDetails(userDetails);
                break;

            case Global.OPT_UPDATE_USER_DETAIL:
                // 更新单个用户详细信息
                UserData updatedUser = (UserData) msg.getData();
                LocalData.get().updateUserDetails(updatedUser);
                break;

            case Global.OPT_EXIT:
                // 服务器关闭通知
                LocalData.get().addSystemMessage("服务器已关闭，程序将退出。");
                // System.exit(0); // 不要直接退出，给 UI 一点时间显示提示
                break;

            case Global.OPT_FRIEND_ADD:
                handleFriendAddRequest(msg);
                EventBus.getInstance().publish("FRIEND_REQUEST", msg);
                break;

            case Global.OPT_FRIEND_ADD_REFUSE:
                LocalData.get().addSystemMessage("对方拒绝了你的好友请求");
                EventBus.getInstance().publish("FRIEND_REFUSED", "对方拒绝了你的好友请求");
                break;

            case Global.OPT_INIT_CHAT:
                handleChatInit(msg);
                break;

            case Global.OPT_INIT_USER:
                handleUserInit(msg);
                break;

            case Global.OPT_INIT_GROUP:
                handleGroupInit(msg);
                break;

            case Global.SERVER_MESSAGE:
                LocalData.get().addSystemMessage((String) msg.getData());
        }
    }

    /**
     * 处理收到的好友请求
     */
    private void handleFriendAddRequest(Wrapper msg) {
        LocalData.get().addSystemMessage("FRIEND_REQUEST|" + msg.getSenderId());
    }

    /**
     * 处理好友添加成功消息
     */
    @SuppressWarnings("unchecked")
    private void handleFriendAddSuccess(Wrapper msg) {
        Map<String, String> friendInfo = (Map<String, String>) msg.getData();
        if (friendInfo != null) {
            for (Map.Entry<String, String> entry : friendInfo.entrySet()) {
                LocalData.get().addFriend(entry.getKey(), entry.getValue());
            }
            LocalData.get().addSystemMessage("添加好友成功");
        }
    }

    /**
     * 处理群聊退出消息
     */
    private void handleGroupQuit(Wrapper msg) {
        LocalData.get().removeGroupChatMsg(msg.getGroupId());
        LocalData.get().setCurrentChatId(null);
    }

    /**
     * 处理群聊初始化消息
     */
    @SuppressWarnings("unchecked")
    private void handleChatInit(Wrapper msg) {
        List<String> chatHistory = (List<String>) msg.getData();
        LocalData.get().addChatMsg(msg.getGroupId(), chatHistory);
    }

    /**
     * 处理用户初始化消息
     */
    @SuppressWarnings("unchecked")
    private void handleUserInit(Wrapper msg) {
        Map<String, String> groupMap = (Map<String, String>) msg.getData();
        for (Map.Entry<String, String> entry : groupMap.entrySet()) {
            LocalData.get().addUserId_name(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 处理群聊初始化消息
     */
    private void handleGroupInit(Wrapper msg) {
        GroupData newGroupData = (GroupData) msg.getData();
        // 添加群聊数据到本地存储，ObservableMap 会自动通知 UI 更新
        LocalData.get().addGroup(newGroupData.getGroupId(), newGroupData);
    }

    /**
     * 处理群聊请求消息
     */
    private void handleChatRequest(Wrapper msg) {
        String content = (String) msg.getData();
        // 更新 ObservableList，UI 会自动刷新
        LocalData.get().addChatMsg(msg.getGroupId(), content);
    }

    /**
     * 处理私聊请求消息
     */
    private void handlePrivateChatRequest(Wrapper msg) {
        String senderId = msg.getSenderId();
        String text = (String) msg.getData();

        // 获取发送者名称
        String senderName = LocalData.get().getFriends().get(senderId);
        if (senderName == null) {
            senderName = LocalData.get().getUserName(senderId);
            if (senderName == null)
                senderName = senderId;
        }

        // 组合成标准消息格式: id|name|text
        String combinedMsg = MsgUtil.combineMsg(senderId, senderName, text);

        // 存储消息，使用senderId作为chatId
        LocalData.get().addChatMsg(senderId, combinedMsg);
    }
}