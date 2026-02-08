package client.view.main;

import client.service.ChatSender;
import client.service.LocalData;
import client.view.util.CircleCharIcon2;
import client.view.util.DesignToken;
import server.serveice.Wrapper;
import util.MsgUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static client.view.util.DesignToken.*;

// 群聊信息组件，包含底部的打字框和滚动的信息聊天信息
public class ChatInfoView extends JPanel {

    private static volatile ChatInfoView instance;

    public static ChatInfoView get() {
        if (instance == null) {
            synchronized (ChatInfoView.class) {
                if (instance == null) {
                    instance = new ChatInfoView();
                }
            }
        }
        return instance;
    }

    // 界面组件
    private JPanel messagePanel; // 使用JPanel来承载消息，可以自定义布局
    private List<JPanel> bubbles;
    private JScrollPane messageScrollPane;
    private JTextArea inputArea;
    private JScrollPane inputScrollPane;
    private JButton sendButton;
    private JPanel inputPanel;
    private JPanel buttonPanel;

    // 样式相关
    private SimpleDateFormat timeFormat;
    private Color userColor = Color.decode(DesignToken.BUBBLE_COLOR_GREEN); // 用户消息气泡颜色
    private Color otherColor = Color.decode(DesignToken.BUBBLE_COLOR_WHITE); // 他人消息气泡颜色（白色）
    private Color systemColor = Color.decode(DesignToken.BACKGROUND_COLOR); // 系统消息背景色

    // 头像颜色数组，用于不同用户的头像显示
    private final Color[] avatarColors = {
            Color.decode(DesignToken.BUBBLE_COLOR_BLUE),
            Color.decode(DesignToken.BUBBLE_COLOR_GRAY),
            Color.decode(DesignToken.BUBBLE_COLOR_RED),
            Color.decode(DesignToken.BUBBLE_COLOR_YELLOW),
            Color.decode(DesignToken.BUBBLE_COLOR_WHITE),
    };

    /**
     * 构造函数
     * 初始化界面组件和布局
     */
    private ChatInfoView() {
        setLayout(new BorderLayout(0, 0));

        // 初始化时间格式
        timeFormat = new SimpleDateFormat("HH:mm");

        // 初始化消息显示区域
        initMessagePanel();

        // 创建输入区域
        initInputPanel();
    }

    /**
     * 初始化消息面板
     * 包含滚动条和消息气泡容器
     */
    private void initMessagePanel() {
        bubbles = new ArrayList<>();

        // 创建消息面板，使用垂直箱式布局
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(systemColor);
        messagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 添加一个弹性空间，让新消息从底部开始
        messagePanel.add(Box.createVerticalGlue());

        // 添加滚动条
        messageScrollPane = new JScrollPane(messagePanel);
        messageScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        messageScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        messageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 设置视口的背景色
        JViewport viewport = messageScrollPane.getViewport();
        viewport.setBackground(systemColor);

        // 添加到主面板
        this.add(messageScrollPane, BorderLayout.CENTER);
    }

    /**
     * 初始化输入面板
     * 包含输入文本框和发送按钮
     */
    private void initInputPanel() {
        // 创建输入面板
        inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(systemColor);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建输入文本框
        inputArea = new JTextArea(3, 20);
        inputArea.setFont(new Font(DEFAULT_FONT, Font.PLAIN, DesignToken.FONT_SIZE));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode(DesignToken.EDGE_COLOR), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // 设置提示文本
        inputArea.setToolTipText("输入消息，按Enter发送，Ctrl+Enter换行");

        // 添加滚动条到输入框
        inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(null);

        // 创建发送按钮
        sendButton = createSendButton();

        // 创建按钮面板
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        buttonPanel.setBackground(systemColor);
        buttonPanel.add(sendButton);
        setupListeners();

        // 添加组件到输入面板
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 添加到主面板
        this.add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * 更新主题
     * 当主题变化时调用，用于更新所有组件的颜色
     */
    public void updateTheme() {
        // 更新颜色变量
        userColor = Color.decode(DesignToken.BUBBLE_COLOR_GREEN);
        otherColor = Color.decode(DesignToken.BUBBLE_COLOR_WHITE);
        systemColor = Color.decode(DesignToken.BACKGROUND_COLOR);

        // 更新组件背景
        if (messagePanel != null)
            messagePanel.setBackground(systemColor);
        if (messageScrollPane != null && messageScrollPane.getViewport() != null) {
            messageScrollPane.getViewport().setBackground(systemColor);
        }
        if (inputPanel != null)
            inputPanel.setBackground(systemColor);
        if (buttonPanel != null)
            buttonPanel.setBackground(systemColor);
        if (sendButton != null)
            sendButton.setBackground(userColor);
        if (inputArea != null) {
            inputArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode(DesignToken.EDGE_COLOR), 1),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        }

        // 刷新当前聊天记录
        if (LocalData.get().getCurrentChatId() != null) {
            init(LocalData.get().getCurrentChatId());
        }
    }

    /**
     * 创建发送按钮
     * 按钮文本为“发送”，字体为加粗，大小为 DesignToken.FONT_SIZE
     * 背景颜色为 DesignToken.BUBBLE_COLOR_GREEN，前景颜色为黑色
     * 点击时背景颜色为 DesignToken.BUBBLE_COLOR_GREEN，松开时恢复为 DesignToken.BUBBLE_COLOR_GREEN
     * 鼠标悬停时背景颜色为 DesignToken.BUBBLE_COLOR_GREEN
     * 
     * @return 发送按钮
     */
    private JButton createSendButton() {
        JButton button = new JButton("发送");
        button.setFont(new Font(DEFAULT_FONT, Font.BOLD, DesignToken.FONT_SIZE));
        button.setBackground(userColor);
        button.setForeground(Color.BLACK); // 强制设置字体颜色为黑色，确保在绿色背景下清晰可见
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.decode(DesignToken.BUBBLE_COLOR_GREEN));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(userColor);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(userColor);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(userColor);
            }
        });

        return button;
    }

    /**
     * 消息发送事件
     * 点击发送按钮或按下Enter键发送消息
     */
    private void setupListeners() {
        // 发送按钮点击事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // 回车发送消息，Ctrl+Enter换行
        inputArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (e.isControlDown()) {
                        // Ctrl+Enter 换行
                        inputArea.append("\n");
                    } else {
                        // Enter 发送消息
                        e.consume(); // 防止默认的换行行为
                        sendMessage();
                    }
                }
            }
        });

        // 窗口显示时自动聚焦到输入框
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent e) {
                inputArea.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    /**
     * 发送信息
     * 先调用使用DataManager进行信息发送操作
     * 如果发送成功，则更新界面，添加信息到消息历史上
     * 如果发送未成功，则忽略这个操作（发射未成功表示程序出现了问题，在控制态输出问题）
     */
    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (!text.isEmpty()) {

            String id = LocalData.get().getId();
            String currentChatId = LocalData.get().getCurrentChatId();
            String message = MsgUtil.combineMsg(id, LocalData.get().getUserName(id), text);

            // 检查是群聊还是私聊
            if (LocalData.get().getGroupData(currentChatId) != null) {
                // 群聊
                ChatSender.addMsg(Wrapper.groupChat(message, id, currentChatId));
            } else {
                // 私聊：发送纯文本
                ChatSender.addMsg(Wrapper.privateChat(text, id, currentChatId));
            }

            // 暂时保存消息
            LocalData.get().addChatMsg(
                    LocalData.get().getCurrentChatId(),
                    message);

            // 添加用户消息
            addUserMessage(text);

            // 清空输入框
            inputArea.setText("");

            // 滚动到底部
            scrollToBottom();
        }

        // 聚焦回输入框
        inputArea.requestFocusInWindow();
    }

    /**
     * 添加用户消息
     * 用户消息右对齐
     * 
     * @param content 消息内容
     */
    public void addUserMessage(String content) {
        // 用户消息右对齐
        addMessageBubble(true, "我", content);
    }

    /**
     * 添加他人消息
     * 他人消息左对齐
     * 
     * @param senderName 发送者名称
     * @param content 消息内容
     */
    public void addOtherUserMessage(String senderName, String content) {
        // 他人消息左对齐
        addMessageBubble(false, senderName, content);
    }

    /**
     * 刷新当前聊天信息
     * 从本地数据中获取聊天记录，根据发送者ID判断是用户消息还是他人消息
     * 
     * @param chatId 聊天ID
     */
    public void setChatInfo(String chatId) {
        // 清空当前消息
        messagePanel.removeAll();
        messagePanel.add(Box.createVerticalGlue());
        bubbles.clear();

        // 从本地数据中获取聊天记录
        List<String> messages = LocalData.get().getChatMsg(chatId);
        if (messages != null) {
            String myId = LocalData.get().getId();
            for (String msg : messages) {
                String[] split = MsgUtil.splitMsg(msg);
                // split[0] = senderId, split[1] = senderName, split[2] = content
                if (split.length >= 3) {
                    if (split[0].equals(myId)) {
                        addUserMessage(split[2]);
                    } else {
                        addOtherUserMessage(split[1], split[2]);
                    }
                }
            }
        }

        // 刷新消息面板
        messagePanel.revalidate();
        messagePanel.repaint();
        scrollToBottom();
    }

    /**
     * 添加系统消息
     * 系统消息居中显示，字体为斜体，颜色为灰色
     * 
     * @param content 系统消息内容
     */
    public void addSystemMessage(String content) {
        SwingUtilities.invokeLater(() -> {
            // 系统消息
            JPanel systemPanel = new JPanel();
            systemPanel.setLayout(new BorderLayout());
            systemPanel.setBackground(new Color(236, 236, 236));
            systemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel systemLabel = new JLabel(content);
            systemLabel.setFont(new Font(DEFAULT_FONT, Font.ITALIC, DesignToken.FONT_SIZE_SMALL));
            systemLabel.setForeground(Color.GRAY);
            systemLabel.setHorizontalAlignment(SwingConstants.CENTER);

            systemPanel.add(systemLabel, BorderLayout.CENTER);

            // 添加到消息面板顶部
            messagePanel.add(systemPanel, 0);
            messagePanel.revalidate();
            messagePanel.repaint();
        });
    }

    /**
     * 添加消息气泡
     * 根据是否是用户消息，创建不同的消息气泡样式
     * 
     * @param isSelf     是否是用户消息
     * @param senderName 发送者名称
     * @param content    消息内容
     */
    public void addMessageBubble(boolean isSelf, String senderName, String content) {
        SwingUtilities.invokeLater(() -> {
            // 创建消息气泡面板
            JPanel messageBubblePanel = new JPanel();
            messageBubblePanel.setLayout(new BorderLayout(8, 0));
            messageBubblePanel.setBackground(systemColor);
            messageBubblePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            // 添加头像
            JLabel avatarPanel = new JLabel(new CircleCharIcon2(
                    avatarColors[Math.abs(senderName.hashCode()) % avatarColors.length],
                    Color.WHITE,
                    senderName.substring(0, 1).toUpperCase(),
                    40));

            // 创建消息内容面板
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout(0, 5));
            contentPanel.setOpaque(false);

            // 创建发送者标签和时间标签
            String time = timeFormat.format(new Date());
            JLabel infoLabel = new JLabel(senderName + "  " + time);
            infoLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, FONT_SIZE_SMALL));
            infoLabel.setForeground(Color.GRAY);

            // 创建消息气泡
            JTextArea messageLabel = new JTextArea(content);
            messageLabel.setEditable(false);
            messageLabel.setLineWrap(true);
            messageLabel.setWrapStyleWord(true);
            messageLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, FONT_SIZE));
            messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            // 设置气泡颜色和样式
            if (isSelf) {
                // 用户消息：右对齐，绿色气泡
                messageLabel.setBackground(userColor);
                messageLabel.setForeground(Color.WHITE);
                infoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                contentPanel.add(infoLabel, BorderLayout.NORTH);
                contentPanel.add(messageLabel, BorderLayout.CENTER);

                // 右对齐布局
                messageBubblePanel.add(contentPanel, BorderLayout.CENTER);
                messageBubblePanel.add(avatarPanel, BorderLayout.EAST);
            } else {
                // 他人消息：左对齐，白色气泡
                messageLabel.setBackground(otherColor);
                messageLabel.setForeground(Color.decode(DesignToken.COLOR_FONT_BLACK));
                messageLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode(EDGE_COLOR), 1),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
                infoLabel.setHorizontalAlignment(SwingConstants.LEFT);
                contentPanel.add(infoLabel, BorderLayout.NORTH);
                contentPanel.add(messageLabel, BorderLayout.CENTER);

                // 左对齐布局
                messageBubblePanel.add(avatarPanel, BorderLayout.WEST);
                messageBubblePanel.add(contentPanel, BorderLayout.CENTER);
            }

            // 设置消息气泡的最大宽度（防止过宽）
            int maxBubbleWidth = 350;
            messageLabel.setSize(new Dimension(maxBubbleWidth, Integer.MAX_VALUE));
            int preferredHeight = messageLabel.getPreferredSize().height;
            messageLabel.setPreferredSize(new Dimension(maxBubbleWidth, preferredHeight));

            // 将其添加到消息列表当中
            bubbles.add(messageBubblePanel);

            // 添加到消息面板顶部（新消息在顶部显示）
            messagePanel.add(messageBubblePanel);
            messagePanel.revalidate();
            messagePanel.repaint();
        });
    }

    /**
     * 移除所有消息气泡（bubbles）
     * 清空输入栏的内容
     */
    public void removeAllMessageBubble() {
        for (JPanel panel : bubbles) {
            messagePanel.remove(panel);
        }
        bubbles.clear();
        inputArea.setText("");
    }

    /**
     * 根据群聊id在DataManager中选择并加载群聊信息
     *
     * @param groupId 群聊id
     */
    public void init(String groupId) {
        this.removeAllMessageBubble();
        List<String> messages = LocalData.get().getChatMsg(groupId);
        for (String text : messages) {
            String[] msgs = MsgUtil.splitMsg(text);

            if (msgs[0].equals(LocalData.get().getId())) {
                addUserMessage(msgs[2]);
            } else {
                addMessageBubble(msgs[0].equals(LocalData.get().getId()), msgs[1], msgs[2]);
            }
        }

        messagePanel.revalidate();
        messagePanel.repaint();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
