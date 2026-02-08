package client.view.main;

import client.service.LocalData;
import client.view.util.CircleCharIcon2;
import client.view.util.DesignToken;
import server.data.UserData;

import javax.swing.*;
import java.awt.*;

/**
 * 好友个人信息视图
 * 显示好友的头像、名称和ID，以及操作按钮（发送消息、删除好友）
 * 好友头像：显示好友的圆形头像
 * 好友名称：显示好友的名称，字体为默认字体，大小为24号，加粗
 * 用户ID：显示好友的唯一标识符，字体为默认字体，大小为16号，颜色为灰色
 * 发送消息按钮：点击后可以发送消息给好友
 * 删除好友按钮：点击后可以删除好友关系
 */
public class FriendProfileView extends JPanel {
    private String userId;
    private String userName;

    public FriendProfileView(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        initUI();
    }

    /**
     * 初始化UI组件
     * 设置布局为网格Bag布局，背景颜色为白色
     * 好友头像：显示好友的圆形头像，大小为80x80
     * 好友名称：显示好友的名称，字体为默认字体，大小为24号，加粗
     * 用户ID：显示好友的唯一标识符，字体为默认字体，大小为16号，颜色为灰色
     * 发送消息按钮：点击后可以发送消息给好友，大小为120x40，背景颜色为蓝色，文字颜色为白色
     * 删除好友按钮：点击后可以删除好友关系，大小为120x40，背景颜色为红色，文字颜色为白色
     */
    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // 好友头像
        JLabel icon = new JLabel(new CircleCharIcon2(Color.ORANGE, Color.WHITE,
                userName.substring(0, 1).toUpperCase(), 80));
        add(icon, gbc);

        // 好友名称
        gbc.gridy++;
        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font(DesignToken.DEFAULT_FONT, Font.BOLD, 24));
        add(nameLabel, gbc);

        // 用户ID
        gbc.gridy++;
        JLabel idLabel = new JLabel("ID: " + userId);
        idLabel.setForeground(Color.GRAY);
        add(idLabel, gbc);

        // 获取并显示详细信息
        UserData friendData = LocalData.get().getUserDetail(userId);
        if (friendData != null) {
            addInfoLabel(friendData.getEmail(), gbc);
            addInfoLabel(friendData.getBirthday(), gbc);
            addInfoLabel(friendData.getAddress(), gbc);
            addInfoLabel(friendData.getSignature(), gbc);
        }

        // 发送消息按钮
        gbc.gridy++;
        gbc.insets = new Insets(30, 10, 10, 10);
        JButton sendBtn = new JButton("发送消息");
        sendBtn.setPreferredSize(new Dimension(120, 40));
        sendBtn.setBackground(new Color(0, 122, 255));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.addActionListener(e -> {
            // 更新消息列表
            SecondaryOptionView.get().updateMessageList(userId, userName, "", 0);

            // 更新当前聊天ID
            client.service.LocalData.get().setCurrentChatId(userId);

            // 切换到聊天房间
            client.view.MainPage.get().exchangeToChatRoom(userId);
        });
        add(sendBtn, gbc);

        // 好友操作按钮
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        JButton deleteBtn = new JButton("删除好友");
        deleteBtn.setPreferredSize(new Dimension(120, 40));
        deleteBtn.setBackground(new Color(220, 53, 69));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "删除好友功能开发中...");
        });
        add(deleteBtn, gbc);
    }

    /**
     * 添加详细信息标签
     * 检查文本是否为空，如果不为空则添加到面板中
     * 标签字体为默认字体，大小为14号，颜色为深灰色
     * 
     * @param text 要添加的详细信息文本
     * @param gbc 网格BagConstraints对象，用于布局
     */
    private void addInfoLabel(String text, GridBagConstraints gbc) {
        if (text != null && !text.isEmpty()) {
            gbc.gridy++;
            gbc.insets = new Insets(2, 10, 2, 10);
            JLabel label = new JLabel(text);
            label.setFont(new Font(DesignToken.DEFAULT_FONT, Font.PLAIN, 14));
            label.setForeground(Color.DARK_GRAY);
            add(label, gbc);
        }
    }
}