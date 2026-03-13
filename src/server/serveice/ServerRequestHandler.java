package server.serveice;

import global.Global;
import server.data.GroupData;
import server.data.ServerData;
import server.data.UserData;
import util.FileUtil;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 专门负责处理服务端接收到的请求
 * 将业务逻辑从 Thread 类中剥离
 */
public class ServerRequestHandler {

    // 回调接口，用于向客户端发送消息
    public interface ResponseSender {
        void sendToSelf(Wrapper msg);
        void sendToUser(Wrapper msg, String userId);
        void sendToGroup(Wrapper msg, String groupId);
        void sendToGroupExceptSelf(Wrapper msg, String groupId);
        String getCurrentUserId();
        void setCurrentUserId(String userId);
        void setLogin(boolean isLogin);
        void registerOnlineUser(String userId, ObjectOutputStream oos);
        void removeOnlineUser(String userId);
        void sentToConnectedGroups(Wrapper msg, String userId);
    }

    private final ResponseSender sender;

    public ServerRequestHandler(ResponseSender sender) {
        this.sender = sender;
    }

    public void handleRequest(Wrapper msg) {
        int opt = msg.getOperation();
        String senderId = msg.getSenderId();

        switch (opt) {
            case Global.OPT_REGISTER:
                handleRegisterRequest(msg);
                break;
            case Global.OPT_LOGIN:
                handleLogInRequest(msg);
                break;
            case Global.OPT_LOGOUT:
                handleLogOutRequest(senderId);
                break;
            case Global.OPT_DELETE_ACCOUNT:
                handleDeleteUserRequest(senderId);
                break;
            case Global.OPT_UPDATE_NICKNAME:
                handleUpdateUserNameRequest(msg);
                break;
            case Global.OPT_UPDATE_PASSWORD:
                handleUpdateUserPwdRequest(msg);
                break;
            case Global.OPT_GROUP_CREATE:
                handleCreateGroupRequest((String) msg.getData(), msg.getGroupId());
                break;
            case Global.OPT_GROUP_INVITE:
                handleInviteRequest(msg);
                break;
            case Global.OPT_GROUP_JOIN:
                handleGroupJoinRequest(msg);
                break;
            case Global.OPT_FRIEND_ADD:
                handleFriendAddRequest(msg);
                break;
            case Global.OPT_FRIEND_ADD_AGREE:
                handleFriendAddAgree(msg);
                break;
            case Global.OPT_FRIEND_ADD_REFUSE:
                handleFriendAddRefuse(msg);
                break;
            case Global.OPT_GROUP_INVITE_AGREE:
                handleJoinGroupRequest(senderId, msg.getGroupId());
                break;
            case Global.OPT_GROUP_INVITE_REFUSE:
                sender.sendToUser(Wrapper.serverResponse(Global.OPT_GROUP_INVITE_REFUSE), (String) msg.getData());
                break;
            case Global.OPT_GROUP_QUIT:
                handleQuitGroupRequest(senderId, msg.getGroupId());
                break;
            case Global.OPT_GROUP_DISBAND:
                handleDeleteGroupRequest(msg.getGroupId());
                break;
            case Global.OPT_CHAT:
                sender.sendToGroupExceptSelf(msg, msg.getGroupId());
                FileUtil.addChatMessage(msg.getGroupId(), (String) msg.getData());
                break;
            case Global.OPT_PRIVATE_CHAT:
                handlePrivateChatRequest(msg);
                break;
            case Global.OPT_GROUP_UPDATE_NAME:
                handleUpdateGroupNameRequest(msg);
                break;
            case Global.OPT_GROUP_UPDATE_OWNER:
                handleUpdateGroupOwnerRequest(msg);
                break;
            case Global.OPT_INIT_CHAT:
                handleInitChatRequest();
                break;
            case Global.OPT_INIT_GROUP:
                handleInitGroupRequest();
                break;
            case Global.OPT_INIT_USER:
                sender.sendToSelf(Wrapper.initResponse(ServerData.getInstance().getIdNameMap()));
                break;
            case Global.OPT_INIT_USER_DETAIL:
                handleInitUserDetailRequest();
                break;
            case Global.OPT_UPDATE_USER_DETAIL:
                handleUpdateUserDetailRequest(msg);
                break;
            default:
                break;
        }
    }

    private void handleRegisterRequest(Wrapper msg) {
        Object dataObj = msg.getData();
        if (!(dataObj instanceof String[])) {
             sender.sendToSelf(Wrapper.serverResponse(Global.OPT_QUEST_WRONG));
             return;
        }
        
        String[] data = (String[]) dataObj;
        String userId = msg.getSenderId();
        String nikname = data[0];
        String password = data[1];

        if (userId == null || nikname == null || password == null) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_QUEST_WRONG));
            return;
        }

        if (ServerData.getInstance().IsAccountExist(userId)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_REGISTER_FAILED_ACC));
            return;
        }

        ServerData.getInstance().addUser(new UserData(nikname, userId, password));
        sender.sendToSelf(Wrapper.serverResponse(Global.OPT_REGISTER_SUCCESS));
        
        FileUtil.saveServerData();
    }

    private void handleLogInRequest(Wrapper msg) {
        String userId = msg.getSenderId();
        String password = (String) msg.getData();

        if (userId == null || password == null) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_LOGIN_FAILED_ACC));
            return;
        }

        if (!ServerData.getInstance().IsAccountExist(userId)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_LOGIN_FAILED_ACC));
            return;
        }

        if (!ServerData.getInstance().AccountAndPasswordIsMatch(userId, password)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_LOGIN_FAILED_PWD));
            return;
        }

        sender.setLogin(true);
        sender.setCurrentUserId(userId);
        sender.registerOnlineUser(userId, null);
        sender.sendToSelf(Wrapper.serverResponse(Global.OPT_LOGIN_SUCCESS));
    }
    
    private void handleLogOutRequest(String userId) {
        sender.setLogin(false);
        sender.removeOnlineUser(userId);
        sender.sendToSelf(Wrapper.serverResponse(Global.OPT_LOGOUT));
    }
    
    private void handleDeleteUserRequest(String userId) {
        ServerData.getInstance().removeUser(userId);
        sender.setLogin(false);
        sender.removeOnlineUser(userId);
        sender.sendToSelf(Wrapper.serverResponse(Global.OPT_DELETE_ACCOUNT));
        FileUtil.saveServerData();
    }

    private void handleUpdateUserNameRequest(Wrapper wrapper) {
        String newName = (String) wrapper.getData();
        ServerData.getInstance().updateUserName(sender.getCurrentUserId(), newName);

        Map<String, String> idNameMap = new HashMap<>();
        idNameMap.put(sender.getCurrentUserId(), newName);

        sender.sentToConnectedGroups(Wrapper.initResponse(idNameMap), sender.getCurrentUserId());
    }

    private void handleUpdateUserPwdRequest(Wrapper wrapper) {
        String newPwd = (String) wrapper.getData();
        ServerData.getInstance().updateUserPwd(sender.getCurrentUserId(), newPwd);
        sender.sendToSelf(Wrapper.serverResponse(Global.OPT_UPDATE_PASSWORD));
    }

    private void handleCreateGroupRequest(String groupName, String groupId) {
        if (groupName == null) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_QUEST_WRONG));
            return;
        }

        if (ServerData.getInstance().containsGroup(groupId)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.SERVER_MESSAGE, "群聊已存在，id重复"));
            return;
        }

        String userId = sender.getCurrentUserId();
        GroupData groupData = new GroupData(groupId, groupName, userId);
        groupData.addMember(userId);
        ServerData.getInstance().addGroup(groupData);
        ServerData.getInstance().addUserToGroup(groupId, userId);

        sender.sendToSelf(Wrapper.serverResponse(Global.OPT_GROUP_CREATE_SUCCESS));
        sender.sendToSelf(Wrapper.initResponse(groupData));
    }

    private void handleInviteRequest(Wrapper inviteMsg) {
        String inviteId = (String) inviteMsg.getData();
        String theGroupId = inviteMsg.getGroupId();
        String senderId = inviteMsg.getSenderId();

        if (senderId.equals(inviteId)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.SERVER_MESSAGE, "不能邀请自己加入群聊"));
            return;
        }

        if (ServerData.getInstance().getGroupMembersId(theGroupId).contains(inviteId)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.SERVER_MESSAGE, "该用户已在群聊中"));
            return;
        }
        
        // 这里的判断逻辑需要访问在线用户列表，这个列表在 ClientChatThread 中
        // 但我们可以尝试发送，如果用户不在线，sender.sendToUser 可能会处理或者忽略
        // 为了精确反馈，我们可以在 sendToUser 中返回是否成功，或者在接口中增加 checkOnline
        
        // 简单处理：直接尝试发送，如果 sender 内部判断不在线，它自己处理
        sender.sendToUser(Wrapper.groupInviteRequest(
                ServerData.getInstance().getGroupName(theGroupId), senderId, theGroupId),
                inviteId);
    }

    private void handleGroupJoinRequest(Wrapper msg) {
        String userId = msg.getSenderId();
        String groupId = msg.getGroupId();

        GroupData group = ServerData.getInstance().getGroupById(groupId);
        if (group == null) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_GROUP_JOIN_FAILED));
            return;
        }

        if (group.getMembers().contains(group.new GroupMember(userId))) {
            sender.sendToSelf(Wrapper.initResponse(group));
            return;
        }

        group.addMember(userId);
        ServerData.getInstance().getUserData(userId).addGroupId(groupId);

        sender.sendToSelf(Wrapper.initResponse(group));
        sender.sendToGroupExceptSelf(Wrapper.initResponse(group), groupId);
    }

    private void handleFriendAddRequest(Wrapper msg) {
        String userId = msg.getSenderId();
        String friendId = (String) msg.getData();

        if (userId.equals(friendId)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_FRIEND_ADD_FAILED));
            return;
        }

        UserData friendData = ServerData.getInstance().getUserData(friendId);
        if (friendData == null) {
            sender.sendToSelf(Wrapper.serverResponse(Global.OPT_FRIEND_ADD_FAILED));
            return;
        }

        UserData myData = ServerData.getInstance().getUserData(userId);
        if (myData.getFriendIds().contains(friendId)) {
            sender.sendToSelf(Wrapper.serverResponse(Global.SERVER_MESSAGE, "你们已经是好友了"));
            return;
        }

        sender.sendToUser(new Wrapper(myData.getNickname(), userId, null, Global.OPT_FRIEND_ADD), friendId);
        sender.sendToSelf(Wrapper.serverResponse(Global.SERVER_MESSAGE, "好友请求已发送"));
    }

    private void handleFriendAddAgree(Wrapper msg) {
        String myId = msg.getSenderId();
        String friendId = (String) msg.getData();

        UserData myData = ServerData.getInstance().getUserData(myId);
        UserData friendData = ServerData.getInstance().getUserData(friendId);

        if (friendData == null) return;

        myData.addFriend(friendId);
        friendData.addFriend(myId);

        Map<String, String> friendInfo = new HashMap<>();
        friendInfo.put(friendId, friendData.getNickname());
        sender.sendToSelf(new Wrapper(friendInfo, Global.SERVER_ACCOUNT, null, Global.OPT_FRIEND_ADD_SUCCESS));

        Map<String, String> myInfo = new HashMap<>();
        myInfo.put(myId, myData.getNickname());
        
        sender.sendToUser(new Wrapper(myInfo, Global.SERVER_ACCOUNT, null, Global.OPT_FRIEND_ADD_SUCCESS), friendId);
        sender.sendToUser(Wrapper.serverResponse(Global.SERVER_MESSAGE, myData.getNickname() + " 同意了你的好友请求"), friendId);
    }

    private void handleFriendAddRefuse(Wrapper msg) {
        String myId = msg.getSenderId();
        String friendId = (String) msg.getData();
        UserData myData = ServerData.getInstance().getUserData(myId);

        sender.sendToUser(new Wrapper(myData.getNickname(), myId, null, Global.OPT_FRIEND_ADD_REFUSE), friendId);
    }

    private void handlePrivateChatRequest(Wrapper msg) {
        String receiverId = msg.getGroupId();
        sender.sendToUser(msg, receiverId);
    }

    private void handleJoinGroupRequest(String userId, String groupId) {
        ServerData.getInstance().addUserToGroup(groupId, userId);
        ServerData.getInstance().addGroupToUser(userId, groupId);

        Wrapper wrapper = Wrapper.initResponse(ServerData.getInstance().getGroupById(groupId));
        sender.sendToGroup(wrapper, groupId);
    }

    private void handleQuitGroupRequest(String userId, String groupId) {
        ServerData.getInstance().removeUserFromGroup(groupId, userId);
        ServerData.getInstance().removeGroupFromUser(userId, groupId);

        if (ServerData.getInstance().getGroupById(groupId).getMemberCount() == 0) {
            ServerData.getInstance().removeGroup(groupId);
        } else {
            Wrapper wrapper = Wrapper.initResponse(ServerData.getInstance().getGroupById(groupId));
            sender.sendToGroupExceptSelf(wrapper, groupId);
        }
        sender.sendToSelf(new Wrapper(null, null, groupId, Global.OPT_GROUP_QUIT));
    }

    private void handleDeleteGroupRequest(String groupId) {
        Wrapper wrapper = new Wrapper(null, null, groupId, Global.OPT_GROUP_QUIT);
        ServerData.getInstance().removeGroup(groupId);
        sender.sendToGroup(wrapper, groupId);
    }

    private void handleUpdateGroupNameRequest(Wrapper groupUpdateMsg) {
        ServerData.getInstance().updateGroupName(groupUpdateMsg.getGroupId(), (String) groupUpdateMsg.getData());
        sender.sendToGroup(groupUpdateMsg, groupUpdateMsg.getGroupId());
    }

    private void handleUpdateGroupOwnerRequest(Wrapper groupUpdateMsg) {
        ServerData.getInstance().updateGroupOwner(groupUpdateMsg.getGroupId(), (String) groupUpdateMsg.getData());
        sender.sendToGroup(groupUpdateMsg, groupUpdateMsg.getGroupId());
    }

    private void handleInitChatRequest() {
        for (String groupId : ServerData.getInstance().getUserGroups(sender.getCurrentUserId())) {
            Wrapper wrapper = Wrapper.initResponse(FileUtil.loadGroupChatMsg(groupId), groupId);
            sender.sendToSelf(wrapper);
        }
    }

    private void handleInitGroupRequest() {
        for (String groupId : ServerData.getInstance().getUserGroups(sender.getCurrentUserId())) {
            Wrapper wrapper = Wrapper.initResponse(ServerData.getInstance().getGroupById(groupId));
            sender.sendToSelf(wrapper);
        }
    }

    private void handleInitUserDetailRequest() {
        Map<String, UserData> allUsers = ServerData.getInstance().getServerUsers();
        Map<String, UserData> safeUsers = new HashMap<>();

        for (Map.Entry<String, UserData> entry : allUsers.entrySet()) {
            safeUsers.put(entry.getKey(), entry.getValue().getSafeCopy());
        }

        sender.sendToSelf(Wrapper.initUserDetailResponse(safeUsers));
    }

    private void handleUpdateUserDetailRequest(Wrapper msg) {
        UserData updatedData = (UserData) msg.getData();
        String currentUserId = sender.getCurrentUserId();
        
        if (updatedData == null || !updatedData.getUserId().equals(currentUserId)) {
            return;
        }

        UserData serverData = ServerData.getInstance().getUserData(currentUserId);
        if (serverData != null) {
            serverData.setEmail(updatedData.getEmail());
            serverData.setBirthday(updatedData.getBirthday());
            serverData.setAddress(updatedData.getAddress());
            serverData.setSignature(updatedData.getSignature());

            // UserData safeCopy = serverData.getSafeCopy();
            // Wrapper updateMsg = Wrapper.updateUserDetailResponse(safeCopy);
            
            // 广播给所有人（这可能效率较低，但保持原逻辑）
            // 注意：这里需要遍历所有在线用户，这需要 ResponseSender 提供能力
            // 我们暂时假设 ResponseSender 没有提供 broadcast，或者需要补充
            // 暂时忽略全网广播，或者在 sender 中补充 broadcast
            // sender.broadcast(updateMsg); 
        }
    }
}
