package server.data;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * 用户信息类，用于保存用户的具体信息。
 */
public class UserData implements Serializable, Comparable<UserData> {
    // 序列化版本号，用于版本控制
    private static final long serialVersionUID = 2809761558436195616L;

    // 用户昵称
    private String nikename;
    // 用户ID
    private String id;
    // 用户密码
    private String password;
    // 所属群聊ID集合
    private TreeSet<String> groupIds;
    // 好友ID集合
    private TreeSet<String> friendIds;
    // 用户邮箱
    private String email;
    // 用户生日
    private String birthday;
    // 用户地址
    private String address;
    // 用户签名 
    private String signature;

    public UserData(String nikename, String id, String password) {
        this.nikename = nikename;
        this.id = id;
        this.password = password;
        this.groupIds = new TreeSet<>();
        this.friendIds = new TreeSet<>();
        // 初始化扩展信息为空字符串，避免 null
        this.email = "";
        this.birthday = "";
        this.address = "";
        this.signature = "";
    }

    // 获取安全的副本（不包含密码），用于网络传输
    public UserData getSafeCopy() {
        UserData copy = new UserData(this.nikename, this.id, null);
        copy.setGroupIds(new TreeSet<>(this.groupIds));
        copy.setFriendIds(new TreeSet<>(this.friendIds));
        copy.setEmail(this.email);
        copy.setBirthday(this.birthday);
        copy.setAddress(this.address);
        copy.setSignature(this.signature);
        return copy;
    }

    public String getNickname() {
        return nikename;
    }

    public void setNikename(String nikename) {
        this.nikename = nikename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TreeSet<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(TreeSet<String> groupIds) {
        this.groupIds = groupIds;
    }

    public TreeSet<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(TreeSet<String> friendIds) {
        this.friendIds = friendIds;
    }

    public void addFriend(String friendId) {
        this.friendIds.add(friendId);
    }

    public boolean addGroupId(String groupId) {
        return this.groupIds.add(groupId);
    }

    public boolean removeGroupId(Long groupId) {
        if (groupIds.contains(groupId.toString())) {
            groupIds.remove(groupId.toString());
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(UserData o) {
        return this.id.compareTo(o.id);
    }

    public void removeGroup(String groupId) {
        this.groupIds.remove(groupId);
    }

    // 扩展信息的 Getter 和 Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}