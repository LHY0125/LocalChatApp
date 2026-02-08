package client.service;

import server.data.GroupData;
import server.data.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// 这个类用于存储本地的数据，便于之后的UI更新操作。
public class LocalData {
    private static final LocalData INSTANCE = new LocalData();

    public static LocalData get() {
        return INSTANCE;
    }

    private String id;

    // 当前的所在的群聊id，辅助UI更新
    private String currentChatId;

    // 使用 ConcurrentHashMap 替代 HashMap
    private ConcurrentMap<String, String> userIdNameMap;
    private ConcurrentMap<String, GroupData> groupDataMap;
    private ConcurrentMap<String, List<String>> groupChatMap;

    // 好友列表
    private ConcurrentMap<String, String> friendMap;

    // 存储所有用户的详细信息
    private ConcurrentMap<String, UserData> userDetailsMap;

    private LocalData() {
        userIdNameMap = new ConcurrentHashMap<>();
        groupDataMap = new ConcurrentHashMap<>();
        groupChatMap = new ConcurrentHashMap<>();
        friendMap = new ConcurrentHashMap<>();
        userDetailsMap = new ConcurrentHashMap<>();

        id = "";
        currentChatId = "";
    }

    public Map<String, String> getFriends() {
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
        return groupDataMap.get(groupId).getGroupName();
    }

    public void addChatMsg(String groupId, List<String> messages) {
        // 覆盖模式：用于初始化加载历史消息，避免重复添加
        groupChatMap.put(groupId, new ArrayList<>(messages));
    }

    public void addChatMsg(String groupId, String message) {
        if (groupChatMap.containsKey(groupId)) {
            groupChatMap.get(groupId).add(message);
        } else {
            groupChatMap.put(groupId, new ArrayList<>());
            groupChatMap.get(groupId).add(message);
        }
    }

    public List<String> getChatMsg(String groupId) {
        if (groupChatMap.containsKey(groupId)) {
            return groupChatMap.get(groupId);
        } else {
            return new ArrayList<>();
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
        groupDataMap.put(groupId, groupData); // HashMap的put方法会自动替换已有的键值对
    }

    public void setGroupName(String groupId, String name) {
        groupDataMap.get(groupId).setGroupName(name);
    }

    public GroupData getGroupData(String groupId) {
        return groupDataMap.get(groupId);
    }

    public List<GroupData> getAllGroups() {
        return new ArrayList<>(groupDataMap.values());
    }
}