package server.serveice;

import global.global;
import server.data.GroupData;
import server.data.UserData;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

// 打包类，用于将数据打包传输

/**
 * Wrapper类的构造方法这里设置为私有，需要时请使用/创建对应的静态构造方法来获取对应的信息载体。
 * 详情请查阅源代码。
 */
public class Wrapper implements Serializable {
    private static final long serialVersionUID = 7499350690768481854L;

    private Object data;

    private String senderId;
    private String groupId;
    private int operation;

    public Wrapper(Object data, String senderId, String groupId, int operation) {

        this.data = data;
        this.senderId = senderId;
        this.groupId = groupId;
        this.operation = operation;
    }

    public Object getData() {
        return data;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getOperation() {
        return operation;
    }

    // 一下静态方法都是用于便捷创建消息体的方法。字如其名，当你需要创造相应的信息载体的时候，请使用对应的方法。
    // 如果有其它需要，请在这里手动添加新的静态方法用于创建对应数据类别。
    // 需要注意的时，有有一部分的类型的信息体是客户端和服务器端都可以发送的，但是他们发送具有不同的含义。

    // 注册请求
    public static Wrapper registerRequest(String[] nickNameAndpwsd, String senderId) {
        return new Wrapper(nickNameAndpwsd, senderId, null, global.OPT_REGISTER);
    }

    // 注册请求
    public static Wrapper registerRequest(String senderId, String password, String username) {
        String[] temp = new String[] { username, password };
        return new Wrapper(temp, senderId, null, global.OPT_REGISTER);
    }

    // 登录请求
    public static Wrapper loginRequest(String senderId, String password) {
        return new Wrapper(password, senderId, null, global.OPT_LOGIN);
    }

    // 服务器回复，用于回复一些简单的关于操作流的信息
    public static Wrapper serverResponse(int opt) {
        return new Wrapper(null, global.SERVER_ACCOUNT, null, opt);
    }

    // 服务器回复，带文本信息
    public static Wrapper serverResponse(int opt, String msg) {
        return new Wrapper(msg, global.SERVER_ACCOUNT, null, opt);
    }

    // 构造一个只包含操作码的消息（用于 OPT_EXIT 等）
    public Wrapper(int operation) {
        this.data = null;
        this.senderId = global.SERVER_ACCOUNT;
        this.groupId = null;
        this.operation = operation;
    }

    // 登出请求
    public static Wrapper logoutRequest(String senderId) {
        return new Wrapper(null, senderId, null, global.OPT_LOGOUT);
    }

    // 初始化请求，请求服务器发送账户和当前的群聊数据
    public static Wrapper initRequest(String senderId, int opt) {
        return new Wrapper(null, senderId, null, opt);
    }

    // 初始化回复，将群聊信息回复给客户端
    public static Wrapper initResponse(GroupData groupData) {
        return new Wrapper(groupData, global.SERVER_ACCOUNT, null, global.OPT_INIT_GROUP);
    }

    // 初始化回复，将聊天记录回复给客户端
    public static Wrapper initResponse(List<String> chatRecords, String groupId) {
        return new Wrapper(chatRecords, global.SERVER_ACCOUNT, groupId, global.OPT_INIT_CHAT);
    }

    // 将用户id/名字回复给客户端。
    public static Wrapper initResponse(Map<String, String> idNameMap) {
        return new Wrapper(idNameMap, global.SERVER_ACCOUNT, null, global.OPT_INIT_USER);
    }

    // 更新用户昵称请求
    public static Wrapper updateUserNameRequest(String senderId, String nickName) {
        return new Wrapper(nickName, senderId, null, global.OPT_UPDATE_NICKNAME);
    }

    // 申请加入群聊
    public static Wrapper joinGroupRequest(String senderId, String groupId) {
        return new Wrapper(null, senderId, groupId, global.OPT_GROUP_JOIN);
    }

    // 申请添加好友
    public static Wrapper addFriendRequest(String senderId, String friendId) {
        // friendId 放在 data 中
        return new Wrapper(friendId, senderId, null, global.OPT_FRIEND_ADD);
    }

    // 更新用户密码请求
    public static Wrapper updateUserPwdRequest2(String senderId, String newPwd) {
        return new Wrapper(newPwd, senderId, null, global.OPT_UPDATE_PASSWORD);
    }

    // 创建群聊请求
    public static Wrapper createGroupRequest(String senderId, String groupName, String groupId) {
        return new Wrapper(groupName, senderId, groupId, global.OPT_GROUP_CREATE);
    }

    // 邀请加入群聊请求
    public static Wrapper groupInviteRequest(String invitedIdOrGroupName, String senderId, String groupId) {
        return new Wrapper(invitedIdOrGroupName, senderId, groupId, global.OPT_GROUP_INVITE);
    }

    // 退出群聊请求
    public static Wrapper groupQuitRequest(String senderId, String groupId) {
        return new Wrapper(null, senderId, groupId, global.OPT_GROUP_QUIT);
    }

    // 退出群聊响应
    public static Wrapper groupQuitResponse(String quitMemberId, String groupId) {
        return new Wrapper(quitMemberId, global.SERVER_ACCOUNT, groupId, global.OPT_GROUP_QUIT);
    }

    // 解散群聊请求
    public static Wrapper groupDisbandRequest(String senderId, String groupId) {
        return new Wrapper(null, senderId, groupId, global.OPT_GROUP_DISBAND);
    }

    // 更新群聊名字请求
    public static Wrapper groupUpdateNameRequest(String senderId, String groupId, String groupName) {
        return new Wrapper(groupName, senderId, groupId, global.OPT_GROUP_UPDATE_NAME);
    }

    // 更新群聊群主请求
    public static Wrapper groupUpdateOwnerRequest(String senderId, String groupId, String ownerId) {
        return new Wrapper(ownerId, senderId, groupId, global.OPT_GROUP_UPDATE_OWNER);
    }

    // 聊天信息
    public static Wrapper groupChat(String text, String senderId, String groupId) {
        return new Wrapper(text, senderId, groupId, global.OPT_CHAT);
    }

    // 私聊信息
    public static Wrapper privateChat(String text, String senderId, String receiverId) {
        // 私聊数据存储在 data/friends/chat_data 中
        // 为了复用字段，我们将 receiverId 放在 groupId 字段中作为目标ID
        return new Wrapper(text, senderId, receiverId, global.OPT_PRIVATE_CHAT);
    }

    // 同意添加好友
    public static Wrapper friendAddAgree(String senderId, String friendId) {
        return new Wrapper(friendId, senderId, null, global.OPT_FRIEND_ADD_AGREE);
    }

    // 拒绝添加好友
    public static Wrapper friendAddRefuse(String senderId, String friendId) {
        return new Wrapper(friendId, senderId, null, global.OPT_FRIEND_ADD_REFUSE);
    }

    // 创建简单指令回复
    public static Wrapper simpleRequest(String senderId, String groupId, int opt) {
        return new Wrapper(null, senderId, groupId, opt);
    }

    // 初始化用户详细信息回复（Map<String, UserData>）
    public static Wrapper initUserDetailResponse(Map<String, UserData> userDetails) {
        return new Wrapper(userDetails, global.SERVER_ACCOUNT, null, global.OPT_INIT_USER_DETAIL);
    }

    // 更新用户详细信息请求
    public static Wrapper updateUserDetailRequest(String senderId, UserData userData) {
        return new Wrapper(userData, senderId, null, global.OPT_UPDATE_USER_DETAIL);
    }

    // 更新用户详细信息响应（服务端广播）
    public static Wrapper updateUserDetailResponse(UserData userData) {
        return new Wrapper(userData, global.SERVER_ACCOUNT, null, global.OPT_UPDATE_USER_DETAIL);
    }
}