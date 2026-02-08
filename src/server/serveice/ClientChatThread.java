package server.serveice;

import global.global;
import server.ServerMainThread;
import server.data.*;
import util.FileUtil;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.*;

/**
 * 不可独立创建线程，这个会在内部自主创建线程。
 * 每个客户端连接对应一个线程，用于处理客户端的请求。
 * 线程启动后，会进入一个循环，等待服务端发送的消息。
 * 收到消息后，会根据消息的操作码，调用对应的处理方法。
 * 处理完成后，会将结果发送给客户端。
 * 线程会在以下情况下退出：
 * 1. 服务端关闭
 * 2. 客户端主动关闭
 * 3. 线程被中断
 */
public class ClientChatThread implements Runnable {
    // 群在线用户存储：全局静态、线程安全
    // key：用户id value：用户在线输出流
    private static final Map<String, ObjectOutputStream> USER_ONLINE_MAP = new ConcurrentHashMap<>();

    // 与客户端相连的套接字
    private final Socket clientSocket;
    // 是否登录
    private boolean isLogin = false;
    // 用户的账户
    private String userId;
    // 阻塞队列，用于进行线程的信息交流
    private BlockingQueue<Wrapper> messageQueue;

    private ObjectOutputStream oos;

    private volatile boolean isRunning = true;

    /**
     * 创建一个数据发送线程，并且附带创建一个信息接收线程。
     * 由于两个线程总是同步创建和销毁的，因此不进行单独创建。
     *
     * @param clientSocket 与客户端相连的套接字
     */
    public ClientChatThread(Socket clientSocket, BlockingQueue<Wrapper> threadQueue)
            throws IOException {
        this.clientSocket = clientSocket;
        this.isLogin = false;
        this.userId = null;

        messageQueue = threadQueue;
    }

    // 线程核心：聊天业务主流程
    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (ServerMainThread.isRunning() && this.isRunning) {
            try {
                Wrapper msg = messageQueue.take();
                handleReceiveMsg(msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 处理服务端接收的消息，按规则解析。在解析出结果后，调用下面的内容来处理数据体
     * 
     * @param msg 服务端接收的消息
     */
    private void handleReceiveMsg(Wrapper msg) {
        int opt = msg.getOperation();
        String senderId = msg.getSenderId();

        switch (opt) {
            // 登录请求
            case global.OPT_REGISTER:
                handleRegisterRequest(msg);
                break;
            case global.OPT_LOGIN:
                handleLogInRequest(msg);
                break;
            case global.OPT_LOGOUT:
                handleLogOutRequest(senderId);
                break;
            case global.OPT_DELETE_ACCOUNT:
                handleDeleteUserRequest(senderId);
                break;
            case global.OPT_UPDATE_NICKNAME:
                handleUpdateUserNameRequest(msg);
                break;
            case global.OPT_UPDATE_PASSWORD:
                handleUpdateUserPwdRequest(msg);
                break;
            case global.OPT_GROUP_CREATE:
                handleCreateGroupRequest((String) msg.getData(), msg.getGroupId());
                break;
            case global.OPT_GROUP_INVITE:
                handleInviteRequest(msg);
                break;
            case global.OPT_GROUP_JOIN:
                handleGroupJoinRequest(msg);
                break;
            case global.OPT_FRIEND_ADD:
                handleFriendAddRequest(msg);
                break;
            case global.OPT_FRIEND_ADD_AGREE:
                handleFriendAddAgree(msg);
                break;
            case global.OPT_FRIEND_ADD_REFUSE:
                handleFriendAddRefuse(msg);
                break;
            case global.OPT_GROUP_INVITE_AGREE:
                // 用户加入群聊
                handleJoinGroupRequest(senderId, msg.getGroupId());
                break;
            case global.OPT_GROUP_INVITE_REFUSE:
                // 拒绝加入群聊
                sendToUser(Wrapper.serverResponse(global.OPT_GROUP_INVITE_REFUSE), (String) msg.getData());
                break;
            case global.OPT_GROUP_QUIT:
                handleQuitGroupRequest(senderId, msg.getGroupId());
                break;
            case global.OPT_GROUP_DISBAND:
                handleDeleteGroupRequest(msg.getGroupId());
                break;
            case global.OPT_CHAT:
                sendToGroupExceptSelf(msg, msg.getGroupId());
                FileUtil.addChatMessage(msg.getGroupId(), (String) msg.getData());
                break;
            case global.OPT_PRIVATE_CHAT:
                handlePrivateChatRequest(msg);
                break;
            case global.OPT_GROUP_UPDATE_NAME:
                handleUpdateGroupNameRequest(msg);
                break;
            case global.OPT_GROUP_UPDATE_OWNER:
                handleUpdateGroupOwnerRequest(msg);
                break;
            case global.OPT_INIT_CHAT:
                handleInitChatRequest();
                break;
            case global.OPT_INIT_GROUP:
                handleInitGroupRequest();
                break;
            case global.OPT_INIT_USER:
                // 发送用户 ID-Name 映射
                sendToSelf(Wrapper.initResponse(ServerData.getInstance().getIdNameMap()));
                break;
            case global.OPT_INIT_USER_DETAIL:
                // 发送所有用户详细信息
                handleInitUserDetailRequest();
                break;
            case global.OPT_UPDATE_USER_DETAIL:
                // 处理更新用户详细信息
                handleUpdateUserDetailRequest(msg);
                break;
        }
    }

    /**
     * 处理注册请求
     * 1. 检查用户ID是否为空
     * 2. 检查用户名是否为空
     * 3. 检查密码是否为空
     * 4. 检查用户ID是否已存在
     * 5. 添加新用户到ServerData
     * 6. 发送注册成功消息给客户端
     * 
     * @param msg 注册请求消息
     */
    private void handleRegisterRequest(Wrapper msg) {
        String[] data = (String[]) msg.getData();

        String userId = msg.getSenderId();
        String nikname = data[0];
        String password = data[1];

        // 数据为空的情况
        if (userId == null || nikname == null || password == null) {
            sendToSelf(Wrapper.serverResponse(global.OPT_QUEST_WRONG));
            return;
        }
        // 账户存在的情况
        if (ServerData.getInstance().IsAccountExist(userId)) {
            sendToSelf(Wrapper.serverResponse(global.OPT_REGISTER_FAILED_ACC));
            return;
        }

        ServerData.getInstance().addUser(new UserData(nikname, userId, password));

        this.isLogin = true;
        this.userId = userId;

        USER_ONLINE_MAP.values().forEach(oos -> {
            Map<String, String> newRegister = new HashMap<>();
            newRegister.put(userId, nikname);
            try {
                oos.writeObject(Wrapper.initResponse(newRegister));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 添加注册用户的id oos映射,初始化id，群id映射
        USER_ONLINE_MAP.put(this.userId, oos);

        sendToSelf(Wrapper.serverResponse(global.OPT_REGISTER_SUCCESS));

    }

    /**
     * 处理登录请求
     * 1. 检查用户ID是否为空
     * 2. 检查密码是否为空
     * 3. 检查用户ID是否已存在
     * 4. 添加新用户到ServerData
     * 5. 发送登录成功消息给客户端
     * 
     * @param msg 登录请求消息
     */
    private void handleLogInRequest(Wrapper msg) {
        String userId = msg.getSenderId();
        String password = (String) msg.getData();

        // 检测Id是否存在
        if (!ServerData.getInstance().IsAccountExist(userId)) {
            sendToSelf(Wrapper.serverResponse(global.OPT_LOGIN_FAILED_ACC));
            return;
        }

        // 检测是否重复登录
        if (USER_ONLINE_MAP.containsKey(userId)) {
            sendToSelf(Wrapper.serverResponse(global.OPT_LOGIN_FAILED_REPEATED));
            return;
        }

        // 检测账户密码是否相对应
        else if (!ServerData.getInstance().AccountAndPasswordIsMatch(userId, password)) {
            sendToSelf(Wrapper.serverResponse(global.OPT_LOGIN_FAILED_PWD));
            return;
        }
        // 修改数据
        this.isLogin = true;
        this.userId = userId;
        // 更新用户在线信息
        USER_ONLINE_MAP.put(this.userId, oos);

        sendToSelf(Wrapper.serverResponse(global.OPT_LOGIN_SUCCESS));
        USER_ONLINE_MAP.put(this.userId, oos);
    }

    /**
     * 判断用户是否已登录
     * 
     * @return 如果用户已登录且在在线映射中，则返回true；否则返回false
     */
    private boolean YesLogin() {
        return isLogin && USER_ONLINE_MAP.containsKey(userId) && this.userId != null;
    }

    /**
     * 关闭当前用户链接，关闭这个线程(接受线程在接收关闭信息时已经结束了）
     */
    private void closeClient() {
        // 优先结束阻塞队列
        ServerMainThread.dropMsgQueue(this.clientSocket);

        if (!isRunning) {
            return;
        }

        isRunning = false;
        if (oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                System.err.println("关闭输出流异常: " + e.getMessage());
            }
        }
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭socket异常: " + e.getMessage());
            }
        }
    }

    /**
     * 处理登出请求
     * 1. 检查用户是否已登录
     * 2. 从在线用户映射中移除用户
     * 3. 设置用户状态为未登录
     * 4. 发送登出成功消息给客户端
     * 
     * @param userId 要登出的用户ID
     */
    private void handleLogOutRequest(String userId) {
        if (!YesLogin()) {
            // 出现错误，现在还未登录
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }

        USER_ONLINE_MAP.remove(this.userId);
        isLogin = false;
        this.userId = null;
        sendToSelf(Wrapper.serverResponse(global.OPT_LOGOUT));
        try {
            Thread.sleep(100); // 短暂延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        closeClient();
    }

    /**
     * 处理删除用户请求
     * 1. 检查用户是否已登录
     * 2. 从在线用户映射中移除用户
     * 3. 从服务器数据中删除用户
     * 4. 设置用户状态为未登录
     * 5. 发送删除成功消息给客户端
     * 
     * @param userId 要删除的用户ID
     */
    private void handleDeleteUserRequest(String userId) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }
        USER_ONLINE_MAP.remove(this.userId);
        ServerData.getInstance().removeUser(userId);
        isLogin = false;
        this.userId = null;
        sendToSelf(Wrapper.serverResponse(global.OPT_DELETE_ACCOUNT));
        try {
            Thread.sleep(100); // 短暂延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        closeClient();
    }

    /**
     * 处理初始化所有用户详细信息的请求
     * 1. 检查用户是否已登录
     * 2. 从服务器数据中获取所有用户的详细信息
     * 3. 发送安全的用户详细信息副本给客户端
     */
    private void handleInitUserDetailRequest() {
        if (!YesLogin())
            return;

        Map<String, UserData> allUsers = ServerData.getInstance().getServerUsers();
        Map<String, UserData> safeUsers = new HashMap<>();

        for (Map.Entry<String, UserData> entry : allUsers.entrySet()) {
            // 只发送安全的副本（无密码）
            safeUsers.put(entry.getKey(), entry.getValue().getSafeCopy());
        }

        sendToSelf(Wrapper.initUserDetailResponse(safeUsers));
    }

    /**
     * 处理更新用户详细信息的请求
     * 1. 检查用户是否已登录
     * 2. 检查更新数据是否包含有效用户ID
     * 3. 从服务器数据中获取原始用户数据
     * 4. 更新服务器上的用户详细信息
     * 5. 广播更新给所有在线用户
     * 
     * @param msg 包含更新用户详细信息的消息
     */
    private void handleUpdateUserDetailRequest(Wrapper msg) {
        if (!YesLogin())
            return;

        UserData updatedData = (UserData) msg.getData();
        if (updatedData == null || !updatedData.getUserId().equals(userId)) {
            return; // 安全校验：只能更新自己的信息
        }

        // 获取服务器上的原始数据
        UserData serverData = ServerData.getInstance().getUserData(userId);
        if (serverData != null) {
            // 更新字段
            serverData.setEmail(updatedData.getEmail());
            serverData.setBirthday(updatedData.getBirthday());
            serverData.setAddress(updatedData.getAddress());
            serverData.setSignature(updatedData.getSignature());
            // 注意：不更新密码和昵称（有专门的接口），也不更新关系链

            // 广播更新给所有在线用户
            UserData safeCopy = serverData.getSafeCopy();
            Wrapper updateMsg = Wrapper.updateUserDetailResponse(safeCopy);

            for (ObjectOutputStream oos : USER_ONLINE_MAP.values()) {
                try {
                    synchronized (oos) {
                        oos.writeObject(updateMsg);
                        oos.flush();
                        oos.reset();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理申请加入群聊请求
     * 1. 检查用户是否已登录
     * 2. 检查群聊是否存在
     * 3. 检查用户是否已加入该群聊
     * 4. 加入群聊
     * 5. 通知群内其他成员有新人加入
     * 
     * @param msg 包含群聊ID的消息
     */
    private void handleGroupJoinRequest(Wrapper msg) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }

        String userId = msg.getSenderId();
        String groupId = msg.getGroupId();

        GroupData group = ServerData.getInstance().getGroupById(groupId);
        if (group == null) {
            sendToSelf(Wrapper.serverResponse(global.OPT_GROUP_JOIN_FAILED));
            return;
        }

        // 检查是否已经是成员
        if (group.getMembers().contains(group.new GroupMember(userId))) {
            // 已经是成员，视为成功
            sendToSelf(Wrapper.initResponse(group));
            return;
        }

        // 添加成员
        group.addMember(userId);
        ServerData.getInstance().getUserData(userId).addGroupId(groupId);

        // 1. 通知自己加入成功 (发送群组信息)
        sendToSelf(Wrapper.initResponse(group));

        // 2. 通知群内其他成员有新人加入
        sendToGroupExceptSelf(Wrapper.initResponse(group), groupId);
    }

    /**
     * 处理添加好友请求
     * 1. 检查用户是否已登录
     * 2. 检查好友是否存在
     * 3. 检查是否已添加该好友
     * 4. 转发请求给目标用户（如果在线）
     * 
     * @param msg 包含好友ID的消息
     */
    private void handleFriendAddRequest(Wrapper msg) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }

        String userId = msg.getSenderId();
        String friendId = (String) msg.getData(); // 目标好友ID

        if (userId.equals(friendId)) {
            // 不能添加自己
            sendToSelf(Wrapper.serverResponse(global.OPT_FRIEND_ADD_FAILED));
            return;
        }

        UserData friendData = ServerData.getInstance().getUserData(friendId);
        if (friendData == null) {
            sendToSelf(Wrapper.serverResponse(global.OPT_FRIEND_ADD_FAILED));
            return;
        }

        UserData myData = ServerData.getInstance().getUserData(userId);

        // 检查是否已经是好友
        if (myData.getFriendIds().contains(friendId)) {
            sendToSelf(Wrapper.serverResponse(global.SERVER_MESSAGE, "你们已经是好友了"));
            return;
        }

        // 如果对方在线，转发请求
        if (USER_ONLINE_MAP.containsKey(friendId)) {
            try {
                // 发送给目标用户：Sender=userId, Data=NickName
                USER_ONLINE_MAP.get(friendId)
                        .writeObject(new Wrapper(myData.getNickname(), userId, null, global.OPT_FRIEND_ADD));

                sendToSelf(Wrapper.serverResponse(global.SERVER_MESSAGE, "好友请求已发送"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendToSelf(Wrapper.serverResponse(global.SERVER_MESSAGE, "用户不在线，无法发送请求"));
        }
    }

    private void handleFriendAddAgree(Wrapper msg) {
        String myId = msg.getSenderId();
        String friendId = (String) msg.getData(); // 发起者的ID

        UserData myData = ServerData.getInstance().getUserData(myId);
        UserData friendData = ServerData.getInstance().getUserData(friendId);

        if (friendData == null)
            return;

        // 双向添加
        myData.addFriend(friendId);
        friendData.addFriend(myId);

        // 通知自己成功
        Map<String, String> friendInfo = new HashMap<>();
        friendInfo.put(friendId, friendData.getNickname());
        sendToSelf(new Wrapper(friendInfo, global.SERVER_ACCOUNT, null, global.OPT_FRIEND_ADD_SUCCESS));

        // 通知对方成功
        if (USER_ONLINE_MAP.containsKey(friendId)) {
            Map<String, String> myInfo = new HashMap<>();
            myInfo.put(myId, myData.getNickname());
            try {
                USER_ONLINE_MAP.get(friendId).writeObject(
                        new Wrapper(myInfo, global.SERVER_ACCOUNT, null, global.OPT_FRIEND_ADD_SUCCESS));
                // 还可以发个系统消息提示
                USER_ONLINE_MAP.get(friendId).writeObject(
                        Wrapper.serverResponse(global.SERVER_MESSAGE, myData.getNickname() + " 同意了你的好友请求"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理好友添加拒绝请求
     * 1. 检查用户是否已登录
     * 2. 检查好友是否存在
     * 3. 通知拒绝者好友请求已被拒绝
     * 
     * @param msg 包含拒绝好友ID的消息
     */
    private void handleFriendAddRefuse(Wrapper msg) {
        String myId = msg.getSenderId();
        String friendId = (String) msg.getData(); // 发起者的ID
        UserData myData = ServerData.getInstance().getUserData(myId);

        if (USER_ONLINE_MAP.containsKey(friendId)) {
            try {
                USER_ONLINE_MAP.get(friendId).writeObject(
                        new Wrapper(myData.getNickname(), myId, null, global.OPT_FRIEND_ADD_REFUSE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理私聊请求
     * 1. 检查用户是否已登录
     * 2. 检查接收者是否存在
     * 3. 检查接收者是否在线
     * 4. 转发消息给接收者
     * 
     * @param msg 包含接收者ID和消息内容的消息
     */
    private void handlePrivateChatRequest(Wrapper msg) {
        String receiverId = msg.getGroupId(); // 接收者ID

        if (USER_ONLINE_MAP.containsKey(receiverId)) {
            try {
                // 转发消息给接收者
                USER_ONLINE_MAP.get(receiverId).writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 对方不在线
            sendToSelf(Wrapper.serverResponse(global.SERVER_MESSAGE, "对方不在线"));
        }
    }

    /**
     * 处理更新用户信息请求
     * 1. 检查用户是否已登录
     * 2. 更新用户昵称
     * 3. 通知所有连接的客户端更新用户信息
     * 
     * @param wrapper 包含新昵称的消息
     */
    private void handleUpdateUserNameRequest(Wrapper wrapper) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }
        String newName = (String) wrapper.getData();
        ServerData.getInstance().updateUserName(this.userId, newName);

        Map<String, String> idNameMap = new HashMap<>();
        idNameMap.put(this.userId, newName);

        // OPT_INIT_USER
        sentToConnectedGroups(Wrapper.initResponse(idNameMap), this.userId);
    }

    /**
     * 处理更新用户密码请求
     * 1. 检查用户是否已登录
     * 2. 更新用户密码
     * 3. 通知客户端密码更新成功
     * 
     * @param wrapper 包含新密码的消息
     */
    private void handleUpdateUserPwdRequest(Wrapper wrapper) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }
        String newPwd = (String) wrapper.getData();
        ServerData.getInstance().updateUserPwd(this.userId, newPwd);
        sendToSelf(Wrapper.serverResponse(global.OPT_UPDATE_PASSWORD));
    }

    /**
     * 处理创建群组请求
     * 1. 检查用户是否已登录
     * 2. 检查群组名称是否为空
     * 3. 检查群组ID是否重复
     * 4. 创建新的群组
     * 5. 通知客户端群组创建成功
     * 
     * @param groupName 群组名称
     * @param groupId   群组ID
     */
    private void handleCreateGroupRequest(String groupName, String groupId) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }

        // 如果信息为空
        if (groupName == null) {
            sendToSelf(Wrapper.serverResponse(global.OPT_QUEST_WRONG));
            return;
        }

        // 如果已经有对应的id的群聊
        if (ServerData.getInstance().containsGroup(groupId)) {
            sendToSelf(Wrapper.serverResponse(global.SERVER_MESSAGE, "群聊已存在，id重复"));
            return;
        }

        // 处理数据调用serverData的addGroup
        GroupData groupData = new GroupData(groupId, groupName, userId);
        groupData.addMember(userId);
        ServerData.getInstance().addGroup(groupData);
        // 调用serverData的addUser
        ServerData.getInstance().addUserToGroup(groupId, userId);

        sendToSelf(Wrapper.serverResponse(global.OPT_GROUP_CREATE_SUCCESS));
        sendToSelf(Wrapper.initResponse(groupData));
    }

    /**
     * 处理邀请加入群组请求
     * 1. 检查用户是否已登录
     * 2. 检查邀请人是否是自己
     * 3. 检查用户是否已在群聊中
     * 4. 检查用户是否在线
     * 5. 通知被邀请人加入群组
     * 
     * @param inviteMsg 包含被邀请人ID和群组ID的消息
     */
    private void handleInviteRequest(Wrapper inviteMsg) {
        String inviteId = (String) inviteMsg.getData();
        String theGroupId = inviteMsg.getGroupId();
        String senderId = inviteMsg.getSenderId();

        // 如果邀请人是自己
        if (senderId.equals(inviteId)) {
            sendToSelf(Wrapper.serverResponse(global.SERVER_MESSAGE, "不能邀请自己加入群聊"));
            return;
        }

        // 如果邀请人已经在群聊中
        if (ServerData.getInstance().getGroupMembersId(theGroupId).contains(inviteId)) {
            sendToSelf(Wrapper.serverResponse(global.SERVER_MESSAGE, "该用户已在群聊中"));
            return;
        }

        // 如果用户不在线
        if (!USER_ONLINE_MAP.containsKey(inviteId)) {
            sendToSelf(Wrapper.serverResponse(
                    global.SERVER_MESSAGE,
                    "用户(" + ServerData.getInstance().getUserName(inviteId) + ")不在线！"));
            return;
        }

        // 通知被邀请人加入群组
        sendToUser(Wrapper.groupInviteRequest(
                ServerData.getInstance().getGroupName(theGroupId), senderId, theGroupId),
                inviteId);
    }

    /**
     * 处理加入群组请求
     * 1. 检查用户是否已登录
     * 2. 检查用户是否已在群聊中
     * 3. 检查群组是否存在
     * 4. 加入群组
     * 5. 通知群组内所有人用户加入
     * 
     * @param userId  用户ID
     * @param groupId 群组ID
     */
    private void handleJoinGroupRequest(String userId, String groupId) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }

        ServerData.getInstance().addUserToGroup(groupId, userId);
        ServerData.getInstance().addGroupToUser(userId, groupId);

        Wrapper wrapper = Wrapper.initResponse(ServerData.getInstance().getGroupById(groupId));
        // 向组内所有人推送更新。(人员加入）
        sendToGroup(wrapper, groupId);
    }

    /**
     * 处理退出群组请求
     * 1. 检查用户是否已登录
     * 2. 检查用户是否已在群聊中
     * 3. 从群组中移除用户
     * 4. 从用户群组列表中移除群组
     * 5. 如果群组为空，则删除群组
     * 6. 通知群组内其他用户更新
     * 
     * @param userId  用户ID
     * @param groupId 群组ID
     */
    private void handleQuitGroupRequest(String userId, String groupId) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }

        ServerData.getInstance().removeUserFromGroup(groupId, userId);
        ServerData.getInstance().removeGroupFromUser(userId, groupId);

        if (ServerData.getInstance().getGroupById(groupId).getMemberCount() == 0) {
            // 没有人的群聊就删掉
            ServerData.getInstance().removeGroup(groupId);
        } else {
            // 不为空则需要推送更新
            Wrapper wrapper = Wrapper.initResponse(ServerData.getInstance().getGroupById(groupId));
            // 向组内其它所有人推送更新。
            sendToGroupExceptSelf(wrapper, groupId);
        }
        // 退出人也要进行更新
        sendToSelf(new Wrapper(null, null, groupId, global.OPT_GROUP_QUIT));
    }

    /**
     * 处理删除群组请求
     * 1. 检查用户是否已登录
     * 2. 检查群组是否存在
     * 3. 检查用户是否是群组所有者
     * 4. 删除群组
     * 5. 通知所有连接的客户端群组已删除
     * 
     * @param groupId 群组ID
     */
    private void handleDeleteGroupRequest(String groupId) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }

        Wrapper wrapper = new Wrapper(null, null, groupId, global.OPT_GROUP_QUIT);

        // 向所有人推送更新。
        ServerData.getInstance().removeGroup(groupId);
        sendToGroup(wrapper, groupId);
    }

    /**
     * 处理更新群组名称请求
     * 1. 检查用户是否已登录
     * 2. 更新群组名称
     * 3. 通知所有连接的客户端更新群组名称
     * 
     * @param groupUpdateMsg 包含群组ID和新名称的消息
     */
    private void handleUpdateGroupNameRequest(Wrapper groupUpdateMsg) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }
        ServerData.getInstance().updateGroupName(groupUpdateMsg.getGroupId(),
                (String) groupUpdateMsg.getData());
        sendToGroup(groupUpdateMsg, groupUpdateMsg.getGroupId());
    }

    /**
     * 处理更新群组管理员请求
     * 1. 检查用户是否已登录
     * 2. 检查管理员是否是自己
     * 3. 更新群组管理员
     * 4. 通知群组内所有成员更新管理员
     * 
     * @param groupUpdateMsg 包含新管理员ID和群组ID的消息
     */
    private void handleUpdateGroupOwnerRequest(Wrapper groupUpdateMsg) {
        if (!YesLogin()) {
            sendToSelf(Wrapper.serverResponse(global.OPT_ERROR_NOT_LOGIN));
            return;
        }
        ServerData.getInstance().updateGroupOwner(groupUpdateMsg.getGroupId(), (String) groupUpdateMsg.getData());
        sendToGroup(groupUpdateMsg, groupUpdateMsg.getGroupId());
    }

    /**
     * 处理初始化聊天消息请求
     * 1. 检查用户是否已登录
     * 2. 遍历用户所属的所有群组
     * 3. 加载群组聊天记录
     * 4. 向用户发送群组聊天记录初始化消息
     */
    private void handleInitChatRequest() {
        for (String groupId : ServerData.getInstance().getUserGroups(userId)) {
            Wrapper wrapper = Wrapper.initResponse(FileUtil.loadGroupChatMsg(groupId), groupId);
            sendToSelf(wrapper);
        }
    }

    /**
     * 处理初始化群组请求
     * 1. 检查用户是否已登录
     * 2. 遍历用户所属的所有群组
     * 3. 加载群组信息
     * 4. 向用户发送群组初始化消息
     */
    private void handleInitGroupRequest() {
        for (String groupId : ServerData.getInstance().getUserGroups(userId)) {
            Wrapper wrapper = Wrapper.initResponse(ServerData.getInstance().getGroupById(groupId));
            sendToSelf(wrapper);
        }
    }

    /**
     * 向指定用户广播消息
     * 1. 遍历用户ID列表
     * 2. 检查用户是否在线
     * 3. 向在线用户发送消息
     * 
     * @param userIds 接收消息的用户ID列表
     * @param wrapper 要发送的消息包装对象
     */
    public static void broadcastMsg(String[] userIds, Wrapper wrapper) {
        for (String userId : userIds) {
            ObjectOutputStream oos = USER_ONLINE_MAP.get(userId);
            if (oos == null)
                continue;
            try {
                synchronized (oos) {
                    oos.writeObject(wrapper);
                    oos.flush();
                    oos.reset();
                }
            } catch (IOException e) {
                Thread.currentThread().interrupt();
                System.err.println("send error");
            }
        }
    }

    /**
     * 向用户发送消息
     * 1. 检查用户是否在线
     * 2. 向在线用户发送消息
     * 
     * @param o 要发送的消息包装对象
     */
    private void sendToSelf(Wrapper o) {
        try {
            synchronized (oos) {
                oos.writeObject(o);
                oos.flush();
                oos.reset();
            }
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            System.out.println("send error");
        }
    }

    /**
     * 向指定用户发送消息
     * 1. 检查用户是否在线
     * 2. 向在线用户发送消息
     * 
     * @param wrapper 要发送的消息包装对象
     * @param userId  接收消息的用户ID
     */
    private void sendToUser(Wrapper wrapper, String userId) {
        try {
            ObjectOutputStream oos = USER_ONLINE_MAP.get(userId);
            if (oos != null) {
                synchronized (oos) {
                    oos.writeObject(wrapper);
                    oos.flush();
                    oos.reset();
                }
            }
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            System.err.println("send error: " + userId);
        }
    }

    /**
     * 向指定群聊发送消息
     * 1. 遍历群聊成员ID列表
     * 2. 检查成员是否在线
     * 3. 向在线成员发送消息
     * 
     * @param wrapper 要发送的消息包装对象
     * @param groupId 接收消息的群聊ID
     */
    private void sendToGroup(Wrapper wrapper, String groupId) {
        List<String> members = ServerData.getInstance().getGroupMembersId(groupId);
        for (String member : members) {
            sendToUser(wrapper, member);
        }
    }

    /**
     * 向指定群聊发送消息（排除发送者）
     * 1. 遍历群聊成员ID列表
     * 2. 检查成员是否在线
     * 3. 向在线成员发送消息（排除发送者）
     * 
     * @param wrapper 要发送的消息包装对象
     * @param groupId 接收消息的群聊ID
     */
    private void sendToGroupExceptSelf(Wrapper wrapper, String groupId) {
        List<String> members = ServerData.getInstance().getGroupMembersId(groupId);
        for (String member : members) {
            if (!member.equals(userId)) {
                sendToUser(wrapper, member);
            }
        }
    }

    /**
     * 向用户所属的所有群聊发送消息
     * 1. 遍历用户所属的所有群组
     * 2. 向每个群组发送消息
     * 
     * @param wrapper 要发送的消息包装对象
     * @param userId  接收消息的用户ID
     */
    private void sentToConnectedGroups(Wrapper wrapper, String userId) {
        TreeSet<String> groups = ServerData.getInstance().getUserGroups(userId);
        groups.forEach(groupId -> sendToGroup(wrapper, groupId));
    }
}