package client.view;

import client.Client;
import client.service.ChatSender;
import client.service.LocalData;
import client.view.main.*;
import global.global;
import server.serveice.Wrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static client.view.util.DesignToken.*;

/**
 * 主界面。
 */
public class MainPage extends JFrame {
    private volatile static MainPage instance;

    // 获取主界面
    public static MainPage get() {
        if (instance == null) {
            synchronized (LoginPage.class) {
                if (instance == null) {
                    instance = new MainPage();
                }
            }
        }
        return instance;
    }

    // 可以左右的移动大小的分割界面
    private final JSplitPane splitPane;
    private final SideOptionView sideOptionView;
    private final SecondaryOptionView secondaryOptionView;
    private final ContentView contentView;

    /**
     * 构造函数
     * 初始化主界面组件，包括侧边栏、二级选项栏、详细内容区域等。
     * 设置主界面的标题、关闭操作、大小、位置等属性。
     * 同时添加窗口关闭监听器，在窗口关闭时发送登出请求。
     */
    private MainPage() {
        Dimension size = new Dimension(WINDOW_ORI_WIDTH, WINDOW_ORI_HEIGHT);

        setTitle("本地网络聊天室");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(size);
        setLocationRelativeTo(null);

        // 侧边栏
        sideOptionView = new SideOptionView();
        sideOptionView.setMinimumSize(new Dimension(SIDE_PANEL_WIDTH, WINDOW_ORI_HEIGHT));
        sideOptionView.setMaximumSize(new Dimension(SIDE_PANEL_WIDTH, Integer.MAX_VALUE));
        sideOptionView.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, WINDOW_ORI_HEIGHT));

        // 二级选项栏
        secondaryOptionView = SecondaryOptionView.get();
        // 详细内容
        contentView = new ContentView();

        splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                secondaryOptionView,
                contentView);

        splitPane.setDividerSize(2);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        splitPane.setMinimumSize(new Dimension(SECONDARY_PANEL_WIDTH_MIN + CONTENT_PANEL_WIDTH_MIN, size.height));
        splitPane.setPreferredSize(new Dimension(SECONDARY_PANEL_WIDTH_MIN + CONTENT_PANEL_WIDTH_MIN, size.height));

        addDividerConstraintListener();

        this.add(sideOptionView, BorderLayout.WEST);
        this.add(splitPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 如果没有连接上，则直接退出
                if (Client.isConnected()) {
                    ChatSender.addMsg(Wrapper.logoutRequest(LocalData.get().getId()));
                }
                // 否则，窗口关闭的时候发送登出信息
                super.windowClosing(e);
            }
        });

        // 发送初始化请求
        ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), global.OPT_INIT_USER));
        ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), global.OPT_INIT_GROUP));
        ChatSender.addMsg(Wrapper.initRequest(LocalData.get().getId(), global.OPT_INIT_CHAT));
    }

    /**
     * 添加分隔条约束监听器
     * 监听分隔条位置变化事件，确保分隔条不会超出最小和最大允许位置范围。
     * 当窗口大小改变或分隔条位置改变时，调用constrainDividerLocation方法重新限制分隔条位置。
     */
    private void addDividerConstraintListener() {
        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 窗口大小改变时重新计算限制
                constrainDividerLocation();
            }
        });

        splitPane.getLeftComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                constrainDividerLocation();
            }
        });
    }

    /**
     * 限制分隔条位置
     * 确保分隔条不会超出最小和最大允许位置范围。
     * 如果当前位置小于最小位置，将分隔条位置设置为最小位置。
     * 如果当前位置大于最大位置，将分隔条位置设置为最大位置。
     */
    private void constrainDividerLocation() {
        int totalWidth = splitPane.getWidth();
        int dividerSize = splitPane.getDividerSize();
        int currentLocation = splitPane.getDividerLocation();

        // 计算有效位置范围
        int minLocation = SECONDARY_PANEL_WIDTH_MIN;
        int maxLocation = totalWidth - dividerSize - SECONDARY_PANEL_WIDTH_MIN;

        // 限制分隔条位置
        if (currentLocation < minLocation) {
            splitPane.setDividerLocation(minLocation);
        } else if (currentLocation > maxLocation) {
            splitPane.setDividerLocation(maxLocation);
        }
    }

    /**
     * 切换到设置界面，当前还没做具体实现
     */
    public void exchangeToSettingPage() {

    }

    /**
     * 显示消息对话框
     * 创建一个对话框，显示text内容。
     * 对话框标题为"信息"，大小为300x200，居中显示在主窗口上。
     * 对话框内容为text，居中对齐，宽度为210px， padding为10px。
     * 对话框包含一个确定按钮，点击后关闭对话框。
     */
    public void showMsgDialog(String text) {
        JDialog inviteDialog = new JDialog(MainPage.get(), "信息", true);
        inviteDialog.setSize(300, 200);
        inviteDialog.setLocationRelativeTo(MainPage.get());

        // 设置对话框内容
        String htmlText = "<html><body style='width: 210px; padding: 10px;'>" + text + "</body></html>";
        JLabel label = new JLabel(htmlText, SwingConstants.CENTER);
        JButton closeBtn = new JButton("确定");

        closeBtn.addActionListener(e -> inviteDialog.dispose());

        JPanel panel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(label);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);
        inviteDialog.add(panel);

        // 显示对话框（会阻塞主窗口交互）
        inviteDialog.setVisible(true);
    }

    /**
     * 切换到登录界面
     * 隐藏当前主窗口，显示登录界面。
     */
    public void openLogInPage() {
        LoginPage.get().setVisible(true);
        this.dispose();
    }

    /**
     * 切换到空白内容界面
     * 清空当前内容区域，显示一个空白界面。
     */
    public void exchangeToBlankContent() {
        contentView.exchangeToBlank();
    }

    /**
     * 切换到聊天房间界面
     * 清空当前内容区域，显示聊天房间界面。
     * 聊天房间界面显示与groupId相关的聊天内容。
     */
    public void exchangeToChatRoom(String groupId) {
        contentView.exchangeToChatRoom(groupId);
    }

    /**
     * 切换到好友个人信息界面
     * 清空当前内容区域，显示好友个人信息界面。
     * 好友个人信息界面显示与userId相关的好友信息。
     */
    public void exchangeToFriendProfile(String userId, String userName) {
        contentView.exchangeToFriendProfile(userId, userName);
    }

    /**
     * 切换到设置界面
     * 清空当前内容区域，显示设置界面。
     * 设置界面根据type显示不同的设置选项。
     */
    public void exchangeToSettings(String type) {
        contentView.exchangeToSettings(type);
    }

    /**
     * 切换到群聊邀请请求界面
     * 清空当前内容区域，显示群聊邀请请求界面。
     * 群聊邀请请求界面显示与inviterId相关的群聊邀请请求，包括邀请者姓名、群聊名称等。
     */
    public void showGroupInviteRequestDialog(String inviterId, String inviterName, String groupName, String groupId) {
        JDialog inviteDialog = new JDialog(MainPage.get(), "群聊邀请", true);
        inviteDialog.setSize(300, 200);
        inviteDialog.setLocationRelativeTo(MainPage.get());

        // 设置对话框内容
        JLabel label = new JLabel(
                "用户" + inviterName + "邀请你加入：" + groupName,
                SwingConstants.CENTER);
        JButton confirmBtn = new JButton("接受");
        JButton closeBtn = new JButton("拒绝");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(label);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(confirmBtn);
        bottomPanel.add(closeBtn);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        inviteDialog.add(panel);

        confirmBtn.addActionListener(
                e -> {
                    ChatSender.addMsg(
                            new Wrapper(inviterId, LocalData.get().getId(), groupId, global.OPT_GROUP_INVITE_AGREE));
                    inviteDialog.dispose();
                });

        closeBtn.addActionListener(
                e -> {
                    ChatSender.addMsg(
                            new Wrapper(inviterId, LocalData.get().getId(), groupId, global.OPT_GROUP_INVITE_REFUSE));
                    inviteDialog.dispose();
                });

        // 显示对话框（会阻塞主窗口交互）
        inviteDialog.setVisible(true);
    }

    /**
     * 切换到邀请好友界面
     * 清空当前内容区域，显示邀请好友界面。
     * 邀请好友界面允许用户输入好友ID，邀请好友加入当前聊天房间。
     */
    public void showGroupInviteDialog() {
        JDialog inviteDialog = new JDialog(MainPage.get(), "邀请好友", true);
        inviteDialog.setSize(300, 200);
        inviteDialog.setLocationRelativeTo(MainPage.get());

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建ID面板
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.add(new JLabel("好友ID  :"));
        JTextField idField = new JTextField(15);
        idPanel.add(idField);
        mainPanel.add(idPanel);

        // 添加间隔
        mainPanel.add(Box.createVerticalStrut(20));

        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");

        confirmButton.addActionListener(e -> {
            String userId = idField.getText().trim();

            if (userId.isEmpty()) {
                JOptionPane.showMessageDialog(inviteDialog, "好友不能为空", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 这里添加创建群聊的逻辑
            ChatSender.addMsg(Wrapper.groupInviteRequest(
                    userId,
                    LocalData.get().getId(),
                    LocalData.get().getCurrentChatId()));
            inviteDialog.dispose();
        });

        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);

        inviteDialog.add(mainPanel);
        inviteDialog.getRootPane().setDefaultButton(confirmButton);
        inviteDialog.setVisible(true);
    }

    /**
     * 切换到加入群聊界面
     * 清空当前内容区域，显示加入群聊界面。
     * 加入群聊界面允许用户输入群聊ID，申请加入群聊。
     */
    public void showJoinGroupDialog() {
        JDialog dialog = new JDialog(MainPage.get(), "加入群聊", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(MainPage.get());

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建ID面板
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.add(new JLabel("群聊ID:"));
        JTextField idField = new JTextField(15);
        idPanel.add(idField);
        mainPanel.add(idPanel);

        // 添加间隔
        mainPanel.add(Box.createVerticalStrut(20));

        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");

        confirmButton.addActionListener(e -> {
            String groupId = idField.getText().trim();

            if (groupId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "群聊ID不能为空", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 发送加入群聊请求
            ChatSender.addMsg(new Wrapper(null, LocalData.get().getId(), groupId, global.OPT_GROUP_JOIN));
            dialog.dispose();
        });

        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);

        dialog.add(mainPanel);
        dialog.getRootPane().setDefaultButton(confirmButton);
        dialog.setVisible(true);
    }

    /**
     * 切换到添加好友界面
     * 清空当前内容区域，显示添加好友界面。
     * 添加好友界面允许用户输入好友ID，申请添加好友。
     */
    public void showAddFriendDialog() {
        JDialog dialog = new JDialog(MainPage.get(), "添加好友", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(MainPage.get());

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建ID面板
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.add(new JLabel("好友ID:"));
        JTextField idField = new JTextField(15);
        idPanel.add(idField);
        mainPanel.add(idPanel);

        // 添加间隔
        mainPanel.add(Box.createVerticalStrut(20));

        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");

        confirmButton.addActionListener(e -> {
            String friendId = idField.getText().trim();

            if (friendId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "好友ID不能为空", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (friendId.equals(LocalData.get().getId())) {
                JOptionPane.showMessageDialog(dialog, "不能添加自己为好友", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 发送添加好友请求
            ChatSender.addMsg(new Wrapper(friendId, LocalData.get().getId(), null, global.OPT_FRIEND_ADD));
            dialog.dispose();
        });

        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);

        dialog.add(mainPanel);
        dialog.getRootPane().setDefaultButton(confirmButton);
        dialog.setVisible(true);
    }

    /**
     * 切换到创建群聊界面
     * 清空当前内容区域，显示创建群聊界面。
     * 创建群聊界面允许用户输入群聊ID和名称，创建一个新的群聊房间。
     */
    public void showGroupCreateDialog() {
        JDialog inviteDialog = new JDialog(MainPage.get(), "创建群聊", true);
        inviteDialog.setSize(300, 200);
        inviteDialog.setLocationRelativeTo(MainPage.get());

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建ID面板
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.add(new JLabel("群聊ID  :"));
        JTextField idField = new JTextField(15);
        idPanel.add(idField);
        mainPanel.add(idPanel);

        // 创建名称面板
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("群聊名称:"));
        JTextField nameField = new JTextField(15);
        namePanel.add(nameField);
        mainPanel.add(namePanel);

        // 添加间隔
        mainPanel.add(Box.createVerticalStrut(20));

        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");

        confirmButton.addActionListener(e -> {
            String groupId = idField.getText().trim();
            String groupName = nameField.getText().trim();

            if (groupId.isEmpty() || groupName.isEmpty()) {
                JOptionPane.showMessageDialog(inviteDialog, "群聊ID和名称不能为空", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (groupId.length() < 6 || groupId.length() > 10 || !groupId.matches("[a-zA-Z0-9_]+")) {
                JOptionPane.showMessageDialog(inviteDialog, "群聊ID只能包含字母大小写和数字下划线，且长度不得小于6，大于10", "警告",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (groupName.contains(" ")) {
                JOptionPane.showMessageDialog(inviteDialog, "群聊名称不能包含空格", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 添加创建群聊的逻辑
            ChatSender.addMsg(Wrapper.createGroupRequest(LocalData.get().getId(), groupName, groupId));
            inviteDialog.dispose();
        });

        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel);

        inviteDialog.add(mainPanel);
        inviteDialog.getRootPane().setDefaultButton(confirmButton);
        inviteDialog.setVisible(true);
    }
}
