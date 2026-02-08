package client.view.main;

import client.service.LocalData;
import client.view.util.DesignToken;
import client.view.util.LimitSizePanel;

import javax.swing.*;
import java.awt.*;

import static client.view.util.DesignToken.*;

/**
 * 内容界面组件
 * 用于显示聊天、群聊、好友个人信息等内容。
 * 点击不同选项可以切换到对应的功能界面。
 */
public class ContentView extends LimitSizePanel {
    private ChatInfoView chatInfoView;
    private GroupInfoView groupInfoView;

    public ContentView() {
        super(CONTENT_PANEL_WIDTH_MIN);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setLayout(new BorderLayout());

        chatInfoView = ChatInfoView.get();
        chatInfoView.setPreferredSize(new Dimension(GROUP_CHAT_PANEL_WIDTH, this.getHeight()));
        chatInfoView.setMinimumSize(new Dimension(GROUP_CHAT_PANEL_WIDTH, this.getHeight()));

        groupInfoView = GroupInfoView.get();
        groupInfoView.setPreferredSize(new Dimension(DesignToken.GROUP_INFO_PANEL_WIDTH, this.getHeight()));

        exchangeToBlank();
    }

    public void exchangeToBlank() {
        this.removeAll();
    }

    /**
     * 将内容组件更改为群聊组件
     * 依据groupId来从DataManager中获取群聊信息
     * 更新 chatInfoView， groupInfoView两个组件
     */
    public void exchangeToChatRoom(String groupId) {
        this.removeAll();
        chatInfoView.init(groupId);
        this.add(chatInfoView, BorderLayout.CENTER);

        if (LocalData.get().getGroupData(groupId) != null) {
            groupInfoView.updateInfo();
            this.add(groupInfoView, BorderLayout.EAST);
        }

        this.revalidate();
        this.repaint();
    }

    /**
     * 将内容组件更改为好友个人信息组件
     * 依据userId和userName来创建FriendProfileView组件
     * 更新 chatInfoView， groupInfoView两个组件
     */
    public void exchangeToFriendProfile(String userId, String userName) {
        this.removeAll();
        FriendProfileView profileView = new FriendProfileView(userId, userName);
        this.add(profileView, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    /**
     * 将内容组件更改为设置组件
     * 依据type来创建SettingsView组件
     * 更新 chatInfoView， groupInfoView两个组件
     */
    public void exchangeToSettings(String type) {
        this.removeAll();
        SettingsView settingsView = new SettingsView(type);
        this.add(settingsView, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}