package client.view;

import client.Client;
import client.service.ChatSender;
import client.service.LocalData;
import client.util.EventBus;
import global.Global;
import server.serveice.Wrapper;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * 主界面控制器 (JavaFX Version)
 * 适配类似 QQ 的三栏布局
 */
public class MainPage {

    private static Stage primaryStage;

    @FXML
    private ListView<String> chatListView;
    @FXML
    private ListView<String> messageListView;
    @FXML
    private TextField inputField;
    @FXML
    private Label currentChatLabel;

    public static void start(Stage stage) {
        primaryStage = stage;
        try {
            FXMLLoader loader = new FXMLLoader(MainPage.class.getResource("/client/view/main/MainPage.fxml"));
            Scene scene = new Scene(loader.load(), 950, 600);

            primaryStage.setTitle("Local Chat");
            primaryStage.setScene(scene);
            primaryStage.show();

            // 初始化数据请求
            requestInitData();

            primaryStage.setOnCloseRequest(e -> {
                if (Client.isConnected()) {
                    ChatSender.addMsg(Wrapper.logoutRequest(LocalData.get().getId()));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // 1. 初始化列表
        updateChatList();

        // 2. 监听数据变化 (保留 LocalData 作为数据源)
        LocalData.get().getGroupDataMap()
                .addListener((MapChangeListener.Change<? extends String, ? extends server.data.GroupData> change) -> {
                    Platform.runLater(this::updateChatList);
                });

        // 3. 监听列表选择
        chatListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 列表项格式: "Name (ID)"
                String groupId = newValue.substring(newValue.lastIndexOf("(") + 1, newValue.lastIndexOf(")"));
                LocalData.get().setCurrentChatId(groupId);
                currentChatLabel.setText(LocalData.get().getGroupName(groupId));
                updateMessageList(groupId);
            }
        });

        // 4. 注册 EventBus 监听器 (替代旧的系统消息监听)
        registerEventListeners();
    }

    private void registerEventListeners() {
        EventBus bus = EventBus.getInstance();

        bus.subscribe("NEW_CHAT_MESSAGE", data -> {
            Wrapper msg = (Wrapper) data;
            // 如果收到的消息属于当前打开的聊天，滚动到底部
            if (msg.getGroupId().equals(LocalData.get().getCurrentChatId())) {
                Platform.runLater(() -> messageListView.scrollTo(messageListView.getItems().size() - 1));
            }
            // 可以添加未读消息红点逻辑
        });

        bus.subscribe("GROUP_CREATED", data -> showMsgDialog("群组创建成功"));

        bus.subscribe("INVITE_REQUEST", data -> {
            Wrapper msg = (Wrapper) data;
            // INVITE_REQUEST|senderId|senderName|groupName|groupId
            // 但 ChatReceiver 里已经把 Wrapper 传过来了，或者是拼接好的字符串？
            // 检查 ChatReceiver: LocalData.addSystemMessage(String),
            // EventBus.publish(Wrapper)
            // 我们在 ChatReceiver 发布的是 Wrapper 对象

            // 为了方便，我们在 ChatReceiver 里 publish 的是 msg，
            // 但 msg.getData() 只是 groupName。我们需要 inviterName
            // 这里的 msg 结构比较特殊，复用了字段。

            // 简单处理：直接弹窗提示，不显示太详细信息，或者再去查询
            String groupName = (String) msg.getData();
            String inviterId = msg.getSenderId();
            String groupId = msg.getGroupId();

            showGroupInviteRequestDialog(inviterId, inviterId, groupName, groupId);
        });

        bus.subscribe("ERROR", data -> showMsgDialog("错误: " + data));

        bus.subscribe("LOGOUT_SUCCESS", data -> {
            Platform.runLater(() -> {
                if (primaryStage != null) {
                    LoginPage.start(primaryStage);
                }
            });
        });
    }

    private void updateChatList() {
        String currentSelection = chatListView.getSelectionModel().getSelectedItem();
        chatListView.getItems().clear();
        LocalData.get().getGroupDataMap().forEach((id, group) -> {
            chatListView.getItems().add(group.getGroupName() + " (" + id + ")");
        });

        // 尝试恢复选择
        if (currentSelection != null && chatListView.getItems().contains(currentSelection)) {
            chatListView.getSelectionModel().select(currentSelection);
        }
    }

    private void updateMessageList(String groupId) {
        messageListView.setItems(LocalData.get().getChatMsg(groupId));
        messageListView.scrollTo(messageListView.getItems().size() - 1);

        // 监听当前聊天记录的变化，自动滚动
        LocalData.get().getChatMsg(groupId).addListener((ListChangeListener<String>) c -> {
            Platform.runLater(() -> messageListView.scrollTo(messageListView.getItems().size() - 1));
        });
    }

    @FXML
    protected void onSendMessage() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) return;
        
        String currentChatId = LocalData.get().getCurrentChatId();
        if (currentChatId == null || currentChatId.isEmpty()) {
            showMsgDialog("请先选择一个聊天");
            return;
        }
        
        ChatSender.addMsg(new Wrapper(LocalData.get().getId(), currentChatId, text, Global.OPT_CHAT));
        inputField.clear();
    }
    
    @FXML
    protected void onAddButtonClick() {
        // 弹出对话框选择操作
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("添加");
        alert.setHeaderText("选择操作");
        alert.setContentText("请选择要执行的操作：");

        ButtonType btnCreateGroup = new ButtonType("创建群聊");
        ButtonType btnAddFriend = new ButtonType("添加好友");
        ButtonType btnCancel = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnCreateGroup, btnAddFriend, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnCreateGroup) {
                showCreateGroupDialog();
            } else if (result.get() == btnAddFriend) {
                showAddFriendDialog();
            }
        }
    }
    
    private void showCreateGroupDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("创建群聊");
        dialog.setHeaderText("请输入群聊名称");
        dialog.setContentText("群名称:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                // 生成一个随机ID作为 GroupID (实际应由服务端生成，但这里协议要求客户端传)
                String groupId = String.valueOf(System.currentTimeMillis() % 1000000); 
                ChatSender.addMsg(new Wrapper(LocalData.get().getId(), name, groupId, Global.OPT_GROUP_CREATE));
            }
        });
    }
    
    private void showAddFriendDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("添加好友");
        dialog.setHeaderText("请输入好友 ID");
        dialog.setContentText("User ID:");

        dialog.showAndWait().ifPresent(friendId -> {
            if (!friendId.trim().isEmpty()) {
                ChatSender.addMsg(new Wrapper(LocalData.get().getId(), friendId, null, Global.OPT_FRIEND_ADD));
            }
        });
    }

    private static void requestInitData() {
        if (Client.isConnected()) {
            String userId = LocalData.get().getId();
            ChatSender.addMsg(Wrapper.initRequest(userId, Global.OPT_INIT_USER));
            ChatSender.addMsg(Wrapper.initRequest(userId, Global.OPT_INIT_GROUP));
            ChatSender.addMsg(Wrapper.initRequest(userId, Global.OPT_INIT_CHAT));
        }
    }

    public void showMsgDialog(String text) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.showAndWait();
        });
    }

    public void showGroupInviteRequestDialog(String inviterId, String inviterName, String groupName, String groupId) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("群聊邀请");
            alert.setHeaderText("用户 " + inviterName + " 邀请你加入：" + groupName);
            alert.setContentText("是否接受？");

            ButtonType buttonTypeAccept = new ButtonType("接受");
            ButtonType buttonTypeReject = new ButtonType("拒绝");

            alert.getButtonTypes().setAll(buttonTypeAccept, buttonTypeReject);

            alert.showAndWait().ifPresent(type -> {
                if (type == buttonTypeAccept) {
                     ChatSender.addMsg(
                             new Wrapper(inviterId, LocalData.get().getId(), groupId, Global.OPT_GROUP_INVITE_AGREE));
                }
            });
        });
    }
}