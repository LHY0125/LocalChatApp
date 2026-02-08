package client.service;

import client.view.LoginPage;
import client.view.MainPage;
import client.view.main.*;
import global.global;
import server.data.GroupData;
import server.data.UserData;
import server.serveice.Wrapper;
import util.MsgUtil;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * 客户端消息接收服务线程
 * <p>
 * 该类负责维护与服务端的长连接，持续监听并接收服务端推送的消息包（Wrapper）。
 * 作为客户端的“下行数据通道”，它将接收到的原始数据根据操作码（Operation Code）进行解析和分发，
 * 驱动本地数据更新（LocalData）及 UI 界面刷新（UIUpdate）。
 * <p>
 * 核心功能包括：
 * 1. 登录/注册响应处理
 * 2. 实时聊天消息接收与存储
 * 3. 群组创建、邀请及变更通知
 * 4. 异常捕获与日志记录
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
            throw new RuntimeException(e);
        }

        while (isRunning) {
            try {
                // 阻塞式读取服务端发送的 Wrapper 对象
                Wrapper message = (Wrapper) ois.readObject();

                System.out.println("收到消息：" + message.getOperation());

                handleMessage(message);
            }
            // 捕获 IO 异常（如连接中断）
            catch (IOException e) {
                e.printStackTrace();
                // 连接中断，退出循环
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
     * 1. 登录/注册响应处理
     * 2. 实时聊天消息接收与存储
     * 3. 群组创建、邀请及变更通知
     * 4. 异常捕获与日志记录
     * 5. 服务器关闭通知
     * 6. 好友添加响应处理
     * 7. 好友删除响应处理
     * 8. 群组解散响应处理
     * 9. 好友添加请求处理
     * 10. 好友删除请求处理
     * 11. 群组解散请求处理
     * 12. 好友添加响应处理
     * 13. 好友删除响应处理
     * 14. 群组解散响应处理
     * 
     * @param msg 服务端下发的消息包
     */
    @SuppressWarnings("unchecked")
    private void handleMessage(Wrapper msg) {
        int opt = msg.getOperation();
        switch (opt) {
            case global.OPT_QUEST_WRONG:
                // 通用异常响应：直接展示服务端返回的错误信息
                System.out.println("出现未知异常");
                break;

            case global.OPT_REGISTER_FAILED_ACC:
                LoginPage.get().showMsgDialog("账号已存在");
                break;

            case global.OPT_REGISTER_SUCCESS:
            case global.OPT_LOGIN_SUCCESS:
                System.out.println("登录/注册成功");
                LoginPage.get().openMainPage();
                ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), global.OPT_INIT_USER));
                // 新增：请求初始化所有用户详细信息
                ChatSender.addMsg(Wrapper.simpleRequest(LocalData.get().getId(), null, global.OPT_INIT_USER_DETAIL));
                ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), global.OPT_INIT_GROUP));
                ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), global.OPT_INIT_CHAT));
                break;

            case global.OPT_LOGIN_FAILED_ACC:
                LoginPage.get().showMsgDialog("账号不存在");
                break;

            case global.OPT_LOGIN_FAILED_PWD:
                LoginPage.get().showMsgDialog("密码错误");
                break;

            case global.OPT_LOGIN_FAILED_REPEATED:
                LoginPage.get().showMsgDialog("账户已登录,请勿重复登录");
                break;

            case global.OPT_ERROR_NOT_LOGIN:
                System.out.println("未知的登录问题？");
                break;

            case global.OPT_LOGOUT:
                MainPage.get().openLogInPage();
                break;

            case global.OPT_DELETE_ACCOUNT:
                MainPage.get().showMsgDialog("账号已被删除");
                MainPage.get().openLogInPage();
                break;

            case global.OPT_UPDATE_NICKNAME:
                LocalData.get().setUserName(msg.getSenderId(), (String) msg.getData());
                break;

            case global.OPT_UPDATE_PASSWORD:
                MainPage.get().showMsgDialog("密码已经更新");
                break;

            case global.OPT_USER_UPDATE_NAME_FAILED:
                MainPage.get().showMsgDialog("昵称修改失败");
                break;

            case global.OPT_USER_UPDATE_PASSWORD_FAILED:
                MainPage.get().showMsgDialog("密码修改失败");
                break;

            case global.OPT_GROUP_CREATE_SUCCESS:
                MainPage.get().showMsgDialog("群组创建成功");
                break;

            case global.OPT_GROUP_INVITE:
                // 群组变更（新建/被拉入）：更新本地群组列表并刷新左侧导航栏
                MainPage.get().showGroupInviteRequestDialog(
                        msg.getSenderId(),
                        LocalData.get().getUserName(msg.getSenderId()),
                        (String) msg.getData(),
                        msg.getGroupId());
                break;

            case global.OPT_GROUP_INVITE_REFUSE:
                MainPage.get().showMsgDialog("对方拒绝了你的邀请");
                break;

            case global.OPT_GROUP_INVITE_OFFLINE:
                MainPage.get().showMsgDialog("邀请的用户并不在线");
                break;

            case global.OPT_GROUP_QUIT:
                handleGroupQuit(msg);
                break;

            case global.OPT_GROUP_DISBAND:
                MainPage.get().showMsgDialog("群聊已被解散");
                break;

            case global.OPT_CHAT:
                handleChatRequest(msg);
                break;

            case global.OPT_PRIVATE_CHAT:
                handlePrivateChatRequest(msg);
                break;

            case global.OPT_GROUP_UPDATE_NAME:
                // 群信息变更：更新群名显示
                LocalData.get().setGroupName(msg.getGroupId(), (String) msg.getData());
                break;

            case global.OPT_GROUP_UPDATE_OWNER:
                LocalData.get().getGroupData(msg.getGroupId()).setGroupOwner((String) msg.getData());
                break;

            case global.OPT_USER_UPDATE_NAME_FAILED_WRONG_FORMAT:
                MainPage.get().showMsgDialog("用户名格式错误，请重新输入");
                break;

            case global.OPT_GROUP_JOIN_FAILED:
                MainPage.get().showMsgDialog("加入群聊失败：群组不存在");
                break;

            case global.OPT_FRIEND_ADD_SUCCESS:
                handleFriendAddSuccess(msg);
                break;

            case global.OPT_FRIEND_ADD_FAILED:
                MainPage.get().showMsgDialog("添加好友失败：用户不存在");
                break;

            case global.OPT_INIT_USER_DETAIL:
                // 初始化所有用户详细信息
                Map<String, UserData> userDetails = (Map<String, UserData>) msg.getData();
                LocalData.get().setUserDetails(userDetails);
                break;

            case global.OPT_UPDATE_USER_DETAIL:
                // 更新单个用户详细信息
                UserData updatedUser = (UserData) msg.getData();
                LocalData.get().updateUserDetails(updatedUser);
                // 刷新界面（如果当前正显示该用户的资料）
                // 由于目前没有全局事件总线，这里简单起见，可以刷新整个 MainView 或者依赖用户重新点击
                // 实际上，如果正在查看好友资料，可能需要实时刷新。
                // 简单实现：不主动刷新UI，下次进入界面时读取最新数据即可。
                // 如果在“设置-个人信息”界面，且更新的是自己，界面通常已经由用户操作更新了，或者可以刷新一下。
                break;

            case global.OPT_EXIT:
                // 服务器关闭通知
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MainPage.get(), "服务器已关闭，程序将退出。", "系统通知", JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                });
                break;

            case global.OPT_FRIEND_ADD:
                handleFriendAddRequest(msg);
                break;

            case global.OPT_FRIEND_ADD_REFUSE:
                MainPage.get().showMsgDialog("对方拒绝了你的好友请求");
                break;

            case global.OPT_INIT_CHAT:
                handleChatInit(msg);
                break;

            case global.OPT_INIT_USER:
                handleUserInit(msg);
                break;

            case global.OPT_INIT_GROUP:
                handleGroupInit(msg);
                break;

            case global.SERVER_MESSAGE:
                MainPage.get().showMsgDialog((String) msg.getData());
        }
    }

    /**
     * 处理收到的好友请求
     * 弹出对话框询问用户是否同意
     */
    private void handleFriendAddRequest(Wrapper msg) {
        String senderId = msg.getSenderId();
        String senderName = (String) msg.getData();

        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(
                    MainPage.get(),
                    "收到来自 " + senderName + " (" + senderId + ") 的好友请求，是否同意？",
                    "好友请求",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                ChatSender.addMsg(Wrapper.friendAddAgree(LocalData.get().getId(), senderId));
            } else {
                ChatSender.addMsg(Wrapper.friendAddRefuse(LocalData.get().getId(), senderId));
            }
        });
    }

    /**
     * 处理好友添加成功消息
     * 
     * @param msg 好友添加成功消息
     */
    @SuppressWarnings("unchecked")
    private void handleFriendAddSuccess(Wrapper msg) {
        Map<String, String> friendInfo = (Map<String, String>) msg.getData();
        if (friendInfo != null) {
            for (Map.Entry<String, String> entry : friendInfo.entrySet()) {
                LocalData.get().addFriend(entry.getKey(), entry.getValue());
            }
            // 刷新好友列表 UI
            SwingUtilities.invokeLater(() -> {
                SecondaryOptionView.get().exchangeToFriendList();
                MainPage.get().showMsgDialog("添加好友成功");
            });
        }
    }

    /**
     * 处理群聊退出消息
     * 
     * @param msg 群聊退出消息
     */
    private void handleGroupQuit(Wrapper msg) {
        LocalData.get().removeGroupChatMsg(msg.getGroupId());
        LocalData.get().setCurrentChatId(null);

        SwingUtilities.invokeLater(() -> {
            SecondaryOptionView.get().removeGroupListItem(msg.getGroupId());
            MainPage.get().exchangeToBlankContent();
            MainPage.get().revalidate();
            MainPage.get().repaint();
        });
    }

    /**
     * 处理群聊初始化消息
     * 初始化群聊消息记录
     * 
     * @param msg 群聊初始化消息
     */
    @SuppressWarnings("unchecked")
    private void handleChatInit(Wrapper msg) {
        List<String> chatHistory = (List<String>) msg.getData();

        // 初始化群聊消息记录
        LocalData.get().addChatMsg(msg.getGroupId(), chatHistory);

        // 如果当前正好停留在该群聊界面，刷新消息
        if (msg.getGroupId().equals(LocalData.get().getCurrentChatId())) {
            SwingUtilities.invokeLater(() -> {
                ChatInfoView.get().setChatInfo(msg.getGroupId());
            });
        }
    }

    /**
     * 处理用户初始化消息
     * 
     * @param msg 用户初始化消息
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
     * 
     * @param msg 群聊初始化消息
     */
    private void handleGroupInit(Wrapper msg) {
        GroupData newGroupData = (GroupData) msg.getData();

        // 打印群聊初始化信息
        System.out.println(
                "handleGroupInit: " + newGroupData.getGroupId() + ", members: " + newGroupData.getMembers().size());

        // 添加群聊数据到本地存储
        LocalData.get().addGroup(newGroupData.getGroupId(), newGroupData);

        // 更新群聊列表 UI
        SecondaryOptionView.get().updateGroupList(
                newGroupData.getGroupId(),
                newGroupData.getGroupName(),
                0);

        // 如果当前处于群聊模式，刷新群聊列表以显示新加入的群组
        SecondaryOptionView.get().refreshIfInGroupMode();

        // 更新当前群组信息（如果当前正打开该群）
        String currentChatId = LocalData.get().getCurrentChatId();
        // SecondaryOptionView.get().updateGroupList(newGroupData.getGroupId(),
        // newGroupData.getGroupName(), 0);

        // 检查是否需要更新当前显示的群组信息（如果当前正打开该群）
        if (newGroupData.getGroupId().equals(currentChatId)) {
            System.out.println("Updating GroupInfoView...");
            SwingUtilities.invokeLater(() -> {
                GroupInfoView.get().updateInfo();
            });
        }
    }

    /**
     * 处理群聊请求消息
     * 
     * @param msg 群聊请求消息
     */
    private void handleChatRequest(Wrapper msg) {
        String content = (String) msg.getData();
        String[] split = MsgUtil.splitMsg(content);

        LocalData.get().addChatMsg(msg.getGroupId(), content);
        // 如果就在当前这个面板，立即更新面板
        if (LocalData.get().getCurrentChatId().equals(msg.getGroupId())) {
            SwingUtilities.invokeLater(() -> {
                ChatInfoView.get().addOtherUserMessage(split[1], split[2]);
            });
        } else {
            SecondaryOptionView.get().updateGroupList(
                    msg.getGroupId(),
                    LocalData.get().getGroupName(msg.getGroupId()),
                    1);
        }
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

        // 如果就在当前这个面板，立即更新面板
        if (senderId.equals(LocalData.get().getCurrentChatId())) {
            String finalSenderName = senderName;
            SwingUtilities.invokeLater(() -> {
                ChatInfoView.get().addOtherUserMessage(finalSenderName, text);
            });
        } else {
            // 更新消息列表
            SecondaryOptionView.get().updateMessageList(
                    senderId,
                    senderName,
                    text,
                    1);
        }
    }
}