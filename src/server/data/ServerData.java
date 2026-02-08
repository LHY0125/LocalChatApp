package server.data;

import util.FileUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 服务器数据,单个服务器仅对应一个服务器数据集合。
// 允许存储，用于数据持久化
// 辅助进行数据核验
public class ServerData implements Serializable {
    private static final long serialVersionUID = 5016807647175865383L;

    private static volatile ServerData instance = null;

    // 获取唯一的serverData对象（线程安全的懒加载）
    public static ServerData getInstance() {
        if (instance == null) {
            synchronized (ServerData.class) {
                if (instance == null) {
                    instance = new ServerData();
                    instance.loadData();
                }
            }
        }
        return instance;
    }

    // 重置实例（用于测试或重新加载）
    public static void resetInstance() {
        synchronized (ServerData.class) {
            instance = null;
        }
    }

    private Map<String, UserData> serverUsers;
    private Map<String, GroupData> serverGroups;
    private transient boolean dataLoaded = false;

    // 处理服务器信息的主类
    public ServerData() {
        // 初始化空数据
        serverUsers = new ConcurrentHashMap<>();
        serverGroups = new ConcurrentHashMap<>();
    }

    // 显式加载数据的方法
    public void loadData() {
        if (!dataLoaded) {
            synchronized (this) {
                if (!dataLoaded) {
                    ServerData loadedData = FileUtil.loadServerData();

                    if (loadedData != null) {
                        // 如果加载到了数据，合并到当前实例
                        if (loadedData.getServerUsers() != null) {
                            this.serverUsers = loadedData.getServerUsers();
                        }
                        if (loadedData.getServerGroups() != null) {
                            this.serverGroups = loadedData.getServerGroups();
                        }

                        System.out.println("服务器数据加载成功，用户数: " +
                                (serverUsers != null ? serverUsers.size() : 0) +
                                ", 群组数: " + (serverGroups != null ? serverGroups.size() : 0));
                    } else {
                        System.out.println("未找到数据文件或加载失败，使用初始化空数据");
                    }

                    dataLoaded = true;
                }
            }
        }
    }

    // 验证数据是否已正确初始化
    private void validateData() {
        if (serverUsers == null) {
            serverUsers = new ConcurrentHashMap<>();
        }
        if (serverGroups == null) {
            serverGroups = new ConcurrentHashMap<>();
        }
    }

    // 保存服务器数据到本地
    public void saveServerData() {
        validateData(); // 确保数据有效
        FileUtil.saveServerData();
    }

    // 添加用户
    public void addUser(UserData userData) {
        serverUsers.put(userData.getUserId(), userData);
    }

    // 移除用户
    public void removeUser(String userId) {
        serverUsers.remove(userId);
        serverGroups.values().forEach(groupData -> {
            groupData.removeMember(userId);
            if (groupData.getMemberCount() == 0) {
                removeGroup(groupData.getGroupId());
            }
        });
    }

    // 修改用户名字
    public void updateUserName(String userId, String newName) {
        serverUsers.get(userId).setNikename(newName);
    }

    public void updateUserPwd(String userId, String newPwd) {
        serverUsers.get(userId).setPassword(newPwd);
    }

    // 添加群聊
    public void addGroup(GroupData groupData) {
        serverGroups.put(groupData.getGroupId(), groupData);
        // 更新关联用户信息
        groupData.getMembers().forEach(member -> {
            addGroupToUser(member.id, groupData.getGroupId());
        });
    }

    public void addGroupToUser(String userId, String groupId) {
        serverUsers.get(userId).addGroupId(groupId);
    }

    // 移除群聊
    public void removeGroup(String groupId) {
        GroupData groupData = serverGroups.remove(groupId);
        groupData.getMembers().forEach(member -> {
            serverUsers.get(member.id).removeGroup(groupData.getGroupId());
        });
    }

    // 更新群聊信息
    public void updateGroupName(String groupId, String newName) {
        serverGroups.get(groupId).setGroupName(newName);
    }

    public void updateGroupOwner(String groupId, String newOwnerId) {
        serverGroups.get(groupId).setGroupOwner(newOwnerId);
    }

    // 添加群聊成员
    public void addUserToGroup(String groupId, String userId) {
        serverGroups.get(groupId).addMember(userId);
    }

    // 移除群聊成员
    public void removeUserFromGroup(String groupId, String userId) {
        serverGroups.values().forEach(groupData -> {
            if (groupData.getGroupId().equals(groupId)) {
                groupData.removeMember(userId);
            }
        });
    }

    // 移除成员的群聊
    public void removeGroupFromUser(String userId, String groupId) {
        serverUsers.get(userId).removeGroup(groupId);
    }

    // 获取用户名字，不存在就回复id本身
    public String getUserName(String userId) {
        if (serverUsers.containsKey(userId))
            return serverUsers.get(userId).getNickname();
        else
            return userId;
    }

    // 获取用户数据
    public UserData getUserData(String userId) {
        return serverUsers.get(userId);
    }

    // 获取群聊名字
    public String getGroupName(String groupId) {
        return serverGroups.get(groupId).getGroupName();
    }

    // 判断群聊是否存在
    public boolean containsGroup(String groupId) {
        return serverGroups.containsKey(groupId);
    }

    // 获取用户群聊id组
    public TreeSet<String> getUserGroups(String userId) {
        if (userId == null) {
            return new TreeSet<>();
        } else {
            return serverUsers.get(userId).getGroupIds();
        }
    }

    // 获取群聊的成员组
    public TreeSet<GroupData.GroupMember> getGroupUsers(String groupId) {
        return serverGroups.get(groupId).getMembers();
    }

    // 获取群聊的成员id组
    public List<String> getGroupMembersId(String groupId) {
        List<String> members = new ArrayList<>();
        serverGroups.get(groupId).getMembers().forEach(groupMember -> {
            members.add(groupMember.id);
        });
        return members;
    }

    /**
     * 判断账号是否存在
     *
     * @param userId 账号
     * @return true:存在 false：不存在
     */
    public boolean IsAccountExist(String userId) {
        return serverUsers.containsKey(userId);
    }

    /**
     * 校验账号与密码是否匹配
     *
     * @param userId   待校验的用户账号
     * @param password 待校验的用户密码
     * @return 密码匹配返回true，不匹配返回false
     */
    public boolean AccountAndPasswordIsMatch(String userId, String password) {
        if (serverUsers.get(userId).getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    /**
     * 设置服务器用户数据
     *
     * @param serverUsers 服务器用户数据映射表
     */
    public void setServerUsers(Map<String, UserData> serverUsers) {
        this.serverUsers = serverUsers;
    }

    /**
     * 设置服务器群聊数据
     *
     * @param serverGroups 服务器群聊数据映射表
     */
    public void setServerGroups(Map<String, GroupData> serverGroups) {
        this.serverGroups = serverGroups;
    }

    /**
     * 获取服务器用户数据映射表
     *
     * @return 服务器用户数据映射表
     */
    public Map<String, UserData> getServerUsers() {
        return serverUsers;
    }

    /**
     * 获取服务器用户id到用户名的映射表
     *
     * @return 服务器用户id到用户名的映射表
     */
    public Map<String, String> getIdNameMap() {
        Map<String, String> idNameMap = new HashMap<>();
        serverUsers.values().forEach(user -> {
            idNameMap.put(user.getUserId(), user.getNickname());
        });
        return idNameMap;
    }

    /**
     * 获取服务器群聊数据映射表
     *
     * @return 服务器群聊数据映射表
     */
    public Map<String, GroupData> getServerGroups() {
        return serverGroups;
    }

    /**
     * 获取服务器群聊数据映射表
     *
     * @return 服务器群聊数据映射表
     */
    public GroupData getGroupById(String groupId) {
        return serverGroups.get(groupId);
    }
}