package client.view.main;

import client.service.ChatSender;
import client.service.LocalData;
import client.view.MainPage;
import client.view.util.DesignToken;
import server.data.GroupData;
import server.serveice.Wrapper;

import javax.swing.*;
import java.awt.*;

/**
 * 聊天信息组件，用于展示当前聊天室的信息
 */
public class GroupInfoView extends JScrollPane {
    private static volatile GroupInfoView instance;

    public static GroupInfoView get() {
        if (instance == null) {
            synchronized (GroupInfoView.class) {
                if (instance == null) {
                    instance = new GroupInfoView();
                }
            }
        }
        return instance;
    }

    private final JPanel mainPanel;
    private final JPanel groupInfoPanel;
    private final JPanel groupMemberPanel;

    public GroupInfoView() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // 垂直布局

        setPreferredSize(new Dimension(DesignToken.INFO_PANEL_WIDTH, DesignToken.WINDOW_ORI_HEIGHT));
        setBackground(Color.GRAY);

        // 设置滚动策略
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        groupInfoPanel = createGroupInfoPanel();
        groupMemberPanel = createGroupMemberPanel();

        mainPanel.add(groupInfoPanel);

        mainPanel.add(groupInfoPanel);
        mainPanel.add(createInviteButton());
        mainPanel.add(createExitButton());
        mainPanel.add(groupMemberPanel);

        mainPanel.add(groupMemberPanel);

        mainPanel.setMinimumSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, 0));
        mainPanel.setPreferredSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, DesignToken.WINDOW_ORI_HEIGHT));

        this.setViewportView(mainPanel);
    }

    /**
     * 创建聊天成员信息面板
     */
    public JPanel createGroupMemberPanel() {
        JPanel memberInfoPanel = new JPanel();
        memberInfoPanel.setPreferredSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, DesignToken.WINDOW_ORI_HEIGHT - 50));

        memberInfoPanel.setMinimumSize(new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, 0));

        memberInfoPanel.setLayout(new BoxLayout(memberInfoPanel, BoxLayout.Y_AXIS));

        // 添加成员标题
        JLabel memberTitle = new JLabel("群成员");
        memberTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        memberTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        memberTitle.setForeground(Color.WHITE);
        memberTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        memberInfoPanel.add(memberTitle);

        // 添加分隔线
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.DARK_GRAY);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator.setMaximumSize(new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH - 20, 1));

        memberInfoPanel.add(separator);

        return memberInfoPanel;
    }

    /**
     * 创建群信息组件
     */
    public JPanel createGroupInfoPanel() {
        JPanel groupInfoPanel = new JPanel();
        groupInfoPanel.setPreferredSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, DesignToken.WINDOW_ORI_HEIGHT - 50));
        groupInfoPanel.setMinimumSize(new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, 0));
        groupInfoPanel.setLayout(new BoxLayout(groupInfoPanel, BoxLayout.Y_AXIS));

        // 添加标题
        JLabel titleLabel = new JLabel("群聊信息");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        groupInfoPanel.add(titleLabel);

        return groupInfoPanel;
    }

    /**
     * 创建一个邀请成员的按钮
     */
    public JButton createInviteButton() {
        JButton inviteButton = new JButton("邀请");
        inviteButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 居中对齐
        inviteButton.setPreferredSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH - 20, 30));
        inviteButton.setMaximumSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH - 20, 30));
        inviteButton.setMargin(
                new Insets(5, 10, 5, 10));

        inviteButton.addActionListener(e -> MainPage.get().showGroupInviteDialog());
        return inviteButton;
    }

    /**
     * 创建一个退出群聊的按钮
     */
    public JButton createExitButton() {
        JButton exitButton = new JButton("退出");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 居中对齐
        exitButton.setPreferredSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH - 20, 30));
        exitButton.setMaximumSize(
                new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH - 20, 30));
        exitButton.setMargin(
                new Insets(5, 10, 5, 10));

        exitButton.addActionListener(e -> {
            ChatSender.addMsg(Wrapper.groupQuitRequest(
                    LocalData.get().getId(),
                    LocalData.get().getCurrentChatId()));
        });
        return exitButton;
    }

    /**
     * 更新 groupInfoPanel 和 groupMemberPanel 这两个组件
     */
    public void updateInfo() {
        groupInfoPanel.removeAll();
        groupMemberPanel.removeAll();

        String currentChatId = LocalData.get().getCurrentChatId();
        if (currentChatId == null || currentChatId.isEmpty()) {
            // 如果没有当前群聊，显示提示信息
            JLabel noGroupLabel = new JLabel("未选择群聊");
            noGroupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            noGroupLabel.setForeground(Color.GRAY);
            groupInfoPanel.add(noGroupLabel);

            groupInfoPanel.revalidate();
            groupInfoPanel.repaint();
            groupMemberPanel.revalidate();
            groupMemberPanel.repaint();
            return;
        }

        GroupData groupData = LocalData.get().getGroupData(currentChatId);
        if (groupData == null) {
            JLabel errorLabel = new JLabel("群聊数据不存在");
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            errorLabel.setForeground(Color.RED);
            groupInfoPanel.add(errorLabel);

            groupInfoPanel.revalidate();
            groupInfoPanel.repaint();
            groupMemberPanel.revalidate();
            groupMemberPanel.repaint();
            return;
        }

        // 更新群聊信息面板
        updateGroupInfoPanel(groupData);

        // 更新群成员面板
        updateGroupMemberPanel(groupData);

        groupInfoPanel.revalidate();
        groupInfoPanel.repaint();
        groupMemberPanel.revalidate();
        groupMemberPanel.repaint();
    }

    /**
     * 更新群聊信息面板内容
     */
    private void updateGroupInfoPanel(GroupData groupData) {
        // 添加标题
        JLabel titleLabel = new JLabel("群聊信息");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        groupInfoPanel.add(titleLabel);

        // 群聊名称
        JLabel nameLabel = new JLabel("群名: " + groupData.getGroupName());
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        groupInfoPanel.add(nameLabel);

        // 群聊ID
        JLabel idLabel = new JLabel("群ID: " + groupData.getGroupId());
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        idLabel.setForeground(Color.DARK_GRAY);
        idLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        groupInfoPanel.add(idLabel);

        // 成员数量
        JLabel memberCountLabel = new JLabel("成员: " + (groupData.getMembers() != null ? groupData.getMembers().size() : 0) + "人");
        memberCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        memberCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        memberCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        groupInfoPanel.add(memberCountLabel);

        // 添加分隔线
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator.setMaximumSize(new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH - 30, 1));
        groupInfoPanel.add(separator);
    }

    /**
     * 更新群成员面板内容
     */
    private void updateGroupMemberPanel(GroupData groupData) {
        // 添加成员标题
        JLabel memberTitle = new JLabel("群成员 (" + (groupData.getMembers() != null ? groupData.getMembers().size() : 0) + ")");
        memberTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        memberTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        memberTitle.setForeground(Color.WHITE);
        memberTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        groupMemberPanel.add(memberTitle);

        // 添加分隔线
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.DARK_GRAY);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator.setMaximumSize(new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH - 20, 1));
        groupMemberPanel.add(separator);

        if (groupData.getMembers() == null || groupData.getMembers().isEmpty()) {
            JLabel noMemberLabel = new JLabel("暂无成员");
            noMemberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            noMemberLabel.setForeground(Color.LIGHT_GRAY);
            noMemberLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            groupMemberPanel.add(noMemberLabel);
            return;
        }

        // 添加成员列表
        for (GroupData.GroupMember memberId : groupData.getMembers()) {
            JPanel memberItemPanel =
                    createMemberItemPanel(memberId.id);
            groupMemberPanel.add(memberItemPanel);
        }

        // 添加底部空白，确保内容居中
        groupMemberPanel.add(Box.createVerticalGlue());
    }

    /**
     * 创建单个成员信息面板
     */
    private JPanel createMemberItemPanel(String memberId) {
        JPanel memberItemPanel = new JPanel();
        memberItemPanel.setLayout(new BoxLayout(memberItemPanel, BoxLayout.X_AXIS));
        memberItemPanel.setBackground(new Color(40, 40, 40));
        memberItemPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        memberItemPanel.setMaximumSize(new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, 50));
        memberItemPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 成员头像（使用圆形标签模拟）
        JLabel avatarLabel = new JLabel();
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(getMemberColor(memberId));
        avatarLabel.setPreferredSize(new Dimension(30, 30));
        avatarLabel.setMinimumSize(new Dimension(30, 30));
        avatarLabel.setMaximumSize(new Dimension(30, 30));
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        // 设置圆形头像
        avatarLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        // 成员信息
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(40, 40, 40));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // 成员名称
        String memberName = LocalData.get().getUserName(memberId);
        if (memberName == null) {
            memberName = "用户" + memberId.substring(0, Math.min(6, memberId.length()));
        }

        JLabel nameLabel = new JLabel(memberName);
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 成员ID
        JLabel idLabel = new JLabel("ID: " + memberId.substring(0, Math.min(10, memberId.length())));
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        idLabel.setForeground(Color.LIGHT_GRAY);
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(idLabel);

        memberItemPanel.add(avatarLabel);
        memberItemPanel.add(infoPanel);
        memberItemPanel.add(Box.createHorizontalGlue());

        // 如果是当前用户，添加标识
        if (memberId.equals(LocalData.get().getId())) {
            JLabel meLabel = new JLabel("(我)");
            meLabel.setFont(new Font("微软雅黑", Font.ITALIC, 11));
            meLabel.setForeground(new Color(100, 150, 255));
            memberItemPanel.add(meLabel);
        }

        return memberItemPanel;
    }

    /**
     * 根据用户ID生成固定颜色（用于头像背景）
     */
    private Color getMemberColor(String memberId) {
        // 简单的哈希算法生成固定颜色
        int hash = memberId.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;

        // 确保颜色不太暗
        r = Math.max(r, 50);
        g = Math.max(g, 50);
        b = Math.max(b, 50);

        return new Color(r, g, b);
    }
}