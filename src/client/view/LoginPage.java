package client.view;

import client.Client;
import client.service.ChatSender;
import client.service.LocalData;
import server.serveice.Wrapper;
import global.Global;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import client.util.EventBus;

import java.io.IOException;

/**
 * 登录页面控制器 (JavaFX Version)
 * 替代原有的 Swing JFrame 实现，现在作为 FXML Controller
 */
public class LoginPage {

    private static Stage primaryStage;

    // FXML 组件
    @FXML
    private Label statusLabel;
    @FXML
    private TextField userIdField;
    @FXML
    private PasswordField passwordField;

    // 提供静态方法来启动登录界面 (兼容旧代码调用习惯)
    public static void start(Stage stage) {
        primaryStage = stage;
        try {
            FXMLLoader loader = new FXMLLoader(LoginPage.class.getResource("/client/view/login/Login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);

            // 获取控制器实例 (不再保存为静态单例)
            // LoginPage controller = loader.getController();

            primaryStage.setTitle("Local Chat Room");
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> {
                System.exit(0);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // 使用 EventBus 订阅事件，更可靠地更新 UI
        
        EventBus.getInstance().subscribe("LOGIN_SUCCESS", data -> {
            System.out.println("LoginPage received LOGIN_SUCCESS");
            openMainPage();
        });

        EventBus.getInstance().subscribe("LOGIN_FAILED", data -> {
            String msg = (String) data;
            if (statusLabel != null) statusLabel.setText(msg);
            showMsgDialog(msg);
        });

        EventBus.getInstance().subscribe("REGISTER_FAILED", data -> {
            String msg = (String) data;
            if (statusLabel != null) statusLabel.setText(msg);
            showMsgDialog(msg);
        });
        
        EventBus.getInstance().subscribe("ERROR", data -> {
            String msg = (String) data;
            if (statusLabel != null) statusLabel.setText("Error: " + msg);
        });

        // 保留旧的 LocalData 监听作为兼容，但要注意避免重复处理
        // LocalData.get().getSystemMessages().addListener(...)
    }

    // 获取实例 (移除单例模式)
    // public static LoginPage get() { return INSTANCE; }

    @FXML
    protected void onLoginButtonClick() {
        String userId = userIdField.getText();
        String password = passwordField.getText();

        if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
            showMsgDialog("Please enter User ID and Password.");
            return;
        }

        if (statusLabel != null)
            statusLabel.setText("Logging in...");
        System.out.println("Login attempt: " + userId);

        // 确保 Client 已启动连接
        if (!Client.isConnected()) {
            showMsgDialog("Connecting to server...");
            return;
        }

        if (Client.isConnected()) {
            LocalData.get().setId(userId);
            ChatSender.addMsg(new Wrapper(userId, null, password, Global.OPT_LOGIN));
        } else {
            showMsgDialog("Connection failed.");
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        String userId = userIdField.getText();
        String password = passwordField.getText();

        if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
            showMsgDialog("Enter ID and Password to Register");
            return;
        }

        if (statusLabel != null)
            statusLabel.setText("Registering...");
        System.out.println("Register attempt: " + userId);

        if (Client.isConnected()) {
            // 使用 Wrapper 的静态辅助方法构造正确的注册请求
            // registerRequest(senderId, password, username)
            // 这里我们将 userId 同时作为 senderId 和 username
            ChatSender.addMsg(Wrapper.registerRequest(userId, password, userId));
        } else {
            showMsgDialog("Connection failed.");
        }
    }

    // private void handleSystemMessage(String msg) {
    //     Platform.runLater(() -> {
    //         if ("LOGIN_SUCCESS".equals(msg)) {
    //             openMainPage();
    //         } else if (!msg.startsWith("INVITE_REQUEST") && !msg.startsWith("FRIEND_REQUEST")
    //                 && !msg.startsWith("LOGOUT_SUCCESS")) {
    //             // 显示错误信息或其他提示
    //             if (statusLabel != null)
    //                 statusLabel.setText(msg);
    //             // 也可以弹窗
    //             // showMsgDialog(msg);
    //         }
    //     });
    // }

    // 打开主界面
    public void openMainPage() {
        Platform.runLater(() -> {
            try {
                MainPage.start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 显示消息对话框
    public void showMsgDialog(String text) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(text);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.showAndWait();
        });
    }

    // 兼容旧接口，虽然可能不再需要
    public void setVisible(boolean visible) {
        if (primaryStage != null) {
            if (visible)
                primaryStage.show();
            else
                primaryStage.hide();
        }
    }
}
