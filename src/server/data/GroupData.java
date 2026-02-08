package server.data;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * 群聊信息类，用于保存聊天室的具体信息。
 */
public class GroupData implements Serializable {
    private static final long serialVersionUID = 4303981922076715842L;

    public class GroupMember implements Comparable<GroupMember>, Serializable {
        private static final long serialVersionUID = 585007162886079570L;

        public String id;
        public boolean isOut;

        public GroupMember(String id) {
            this.id = id;
            isOut = true;
        }

        @Override
        public int compareTo(GroupMember o) {
            return this.id.compareTo(o.id);
        }
    }

    // 群聊id
    private String groupId;
    private String groupName;
    private GroupMember groupOwner;
    private TreeSet<GroupMember> members;

    public GroupData(String groupId, String groupName, String groupOwner) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.groupOwner = new GroupMember(groupOwner);
        members = new TreeSet<>();
    }

    public GroupData(String groupId) {
        this.groupId = groupId;
        this.groupName = "TEMP_TEST";
    }

    // 添加组员
    public void addMember(String id) {
        members.add(new GroupMember(id));
    }

    // 移除组员
    public boolean removeMember(String id) {
        GroupMember temp = new GroupMember(id);
        if (members.contains(temp)) {
            members.remove(new GroupMember(id));
            return true;
        } else {
            // System.out.println("组员移除失败. groupId: " + groupId);
            return false;
        }
    }

    public int getMemberCount() {
        return members.size();
    }

    // 获取群聊id
    public String getGroupId() {
        return groupId;
    }

    // 获取群聊名称
    public String getGroupName() {
        return groupName;
    }

    // 获取群主
    public GroupMember getGroupOwner() {
        return groupOwner;
    }

    // 获取群成员
    public TreeSet<GroupMember> getMembers() {
        return members;
    }

    // 设置群主
    public void setGroupOwner(String id) {
        this.groupOwner = new GroupMember(id);
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "GroupData{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupOwner=" + groupOwner.id +
                ", members count=" + members.size() +
                '}';
    }
}