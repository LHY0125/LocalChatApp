package client;

import client.service.*;
import client.view.LoginPage;
import global.Global;
import server.serveice.Wrapper;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Client extends Application {
    private static boolean isConnected = false; // 是否连接成功

    // 接收信息线程
    private ChatReceiver chatReceiver;
    // 发送信息线程
    private ChatSender chatSender;
    // 信息处理队列
    private BlockingQueue<Wrapper> messageQueue;

    // 单例模式，方便其他地方访问网络服务
    private static Client instance;

    public static Client getInstance() {
        return instance;
    }

    // 程序入口
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        // 启动网络服务（异步）
        new Thread(this::startClient).start();

        System.out.println("客户端启动成功");

        // 显示登录界面
        LoginPage.start(primaryStage);
    }

    // 启动客户端，连接服务端,初始化相关内容
    public void startClient() {
        messageQueue = new ArrayBlockingQueue<>(40);
        // 连接服务器 创建 clientSocket
        try {
            // 使用无参构造，然后 connect，可以更好地控制超时和避免 Invalid Argument 问题
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(Global.LOCAL_HOST, Global.SERVER_PORT), 5000);

            chatSender = new ChatSender(socket, messageQueue);
            chatReceiver = new ChatReceiver(socket, messageQueue);

            chatSender.start();
            chatReceiver.start();
            isConnected = true;
        } catch (IOException e) {
            isConnected = false;
            System.err.println("连接服务器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        return isConnected;
    }
}