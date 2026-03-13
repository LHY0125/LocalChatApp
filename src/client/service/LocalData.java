package client.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import server.data.GroupData;
import server.data.UserData;

import java.util.List;
import java.util.Map;

// 这个类用于存储本地的数据，便于之后的UI更新操作。
public class LocalData {
    private static final LocalData INSTANCE = new LocalData();

    public static LocalData get() {
        return INSTANCE;
    }

    private String id;

    // 当前的所在的群聊id，辅助UI更新
    private String currentChatId;

    // 使用 ObservableMap/ObservableList 替代普通集合，支持 UI 绑定
    private ObservableMap<String, String> userIdNameMap;
    private ObservableMap<String, GroupData> groupDataMap;
    private ObservableMap<String, ObservableList<String>> groupChatMap;

    // 好友列表
    private ObservableMap<String, String> friendMap;

    // 存储所有用户的详细信息
    private ObservableMap<String, UserData> userDetailsMap;

    // 系统消息/日志列表 (UI 可以绑定这个来显示弹窗)
    private ObservableList<String> systemMessages;

    private LocalData() {
        // 使用 FXCollections.synchronizedObservableMap 保证线程安全，或者在更新时注意线程
        // 这里为了简单，我们假设更新都在 Platform.runLater 中进行，或者使用普通 ObservableMap 但在更新时切换线程
        // 实际上 ChatReceiver 是在后台线程，所以更新这些集合时必须用 Platform.runLater

        userIdNameMap = FXCollections.observableHashMap();
        groupDataMap = FXCollections.observableHashMap();
        groupChatMap = FXCollections.observableHashMap();
        friendMap = FXCollections.observableHashMap();
        userDetailsMap = FXCollections.observableHashMap();
        systemMessages = FXCollections.observableArrayList();

        id = "";
        currentChatId = "";
    }

    public ObservableMap<String, String> getFriends() {
        return friendMap;
    }

    public void addFriend(String id, String name) {
        friendMap.put(id, name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrentChatName() {
        if (currentChatId == null || !groupDataMap.containsKey(currentChatId))
            return "";
        return groupDataMap.get(currentChatId).getGroupName();
    }

    public synchronized void setCurrentChatId(String id) {
        this.currentChatId = id;
    }

    public synchronized String getCurrentChatId() {
        return currentChatId;
    }

    public void addUserId_name(String id, String name) {
        userIdNameMap.put(id, name);
    }

    public String getGroupName(String groupId) {
        if (groupDataMap.containsKey(groupId)) {
            return groupDataMap.get(groupId).getGroupName();
        }
        return groupId;
    }

    public void addChatMsg(String groupId, List<String> messages) {
        if (!groupChatMap.containsKey(groupId)) {
            groupChatMap.put(groupId, FXCollections.observableArrayList());
        }
        groupChatMap.get(groupId).addAll(messages);
    }

    public void addChatMsg(String groupId, String message) {
        if (!groupChatMap.containsKey(groupId)) {
            groupChatMap.put(groupId, FXCollections.observableArrayList());
        }
        groupChatMap.get(groupId).add(message);
    }

    public ObservableList<String> getChatMsg(String groupId) {
        if (groupChatMap.containsKey(groupId)) {
            return groupChatMap.get(groupId);
        } else {
            return FXCollections.observableArrayList();
        }
    }

    public void setUserDetails(Map<String, UserData> userDetails) {
        userDetailsMap.clear();
        userDetailsMap.putAll(userDetails);
        // 同时更新 userIdNameMap，确保一致性
        userDetails.forEach((id, userData) -> {
            userIdNameMap.put(id, userData.getNickname());
        });
    }

    public void updateUserDetails(UserData userData) {
        userDetailsMap.put(userData.getUserId(), userData);
        userIdNameMap.put(userData.getUserId(), userData.getNickname());
    }

    public UserData getUserDetail(String userId) {
        return userDetailsMap.get(userId);
    }

    public void removeGroupChatMsg(String groupId) {
        if (groupChatMap.containsKey(groupId)) {
            groupChatMap.remove(groupId);
        }
    }

    public String getUserName(String userId) {
        if (userIdNameMap.containsKey(userId))
            return userIdNameMap.get(userId);
        else
            return userId;
    }

    public void setUserName(String suerId, String name) {
        userIdNameMap.put(suerId, name);
    }

    public synchronized void addGroup(String groupId, GroupData groupData) {
        groupDataMap.put(groupId, groupData);
    }

    public void setGroupName(String groupId, String name) {
        if (groupDataMap.containsKey(groupId)) {
            groupDataMap.get(groupId).setGroupName(name);
            // 触发更新? ObservableMap put 相同 key 可能不会触发 list 刷新，视情况而定
            // 最好是 GroupData 内部属性也是 Observable 的，或者这里替换对象
            // 简单起见，这里不做复杂操作
        }
    }

    public GroupData getGroupData(String groupId) {
        return groupDataMap.get(groupId);
    }

    public ObservableMap<String, GroupData> getGroupDataMap() {
        return groupDataMap;
    }

    // 添加系统消息，用于触发 UI 弹窗
    public void addSystemMessage(String message) {
        systemMessages.add(message);
    }

    public ObservableList<String> getSystemMessages() {
        return systemMessages;
    }
}