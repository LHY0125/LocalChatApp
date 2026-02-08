package client;

import com.formdev.flatlaf.FlatLightLaf;
import client.service.*;
import client.view.LoginPage;
import global.global;
import server.serveice.Wrapper;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Client {
    private static boolean isConnected = false; // 是否连接成功

    // 接收信息线程
    private ChatReceiver chatReceiver;
    // 发送信息线程
    private ChatSender chatSender;
    // 信息处理队列
    private BlockingQueue<Wrapper> messageQueue;

    // 用于分配动态端口的计数器
    private static int dynamicPortCounter = 10000;
    private static final int MAX_DYNAMIC_PORT = 20000;

    // 程序入口
    public static void main(String[] args) {
        // 设置 FlatLaf 主题，使界面更现代化，符合当前审美
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        Client client = new Client();
        client.startClient();

        System.out.println("客户端启动成功,本地端口：" +
                (dynamicPortCounter > 10000 ? dynamicPortCounter - 1 : "未分配"));

        SwingUtilities.invokeLater(() -> {
            LoginPage.get().setVisible(true);
        });
    }

    // 启动客户端，连接服务端,初始化相关内容
    public void startClient() {
        messageQueue = new ArrayBlockingQueue<>(40);
        // 连接服务器 创建 clientSocket
        try {
            int localPort = findAvailablePort(getNextDynamicPort());
            Socket socket;
            if (localPort > 0) {
                socket = new Socket(
                        global.LOCAL_HOST,
                        global.SERVER_PORT,
                        null,
                        localPort);
            } else {
                socket = new Socket(
                        global.LOCAL_HOST,
                        global.SERVER_PORT);
            }

            chatSender = new ChatSender(socket, messageQueue);
            chatReceiver = new ChatReceiver(socket, messageQueue);

            chatSender.start();
            Thread.sleep(200);
            chatReceiver.start();
            isConnected = true;
        } catch (IOException e) {
            isConnected = false;
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 获取动态分配的本地端口
    private synchronized int getNextDynamicPort() {
        int port = dynamicPortCounter;
        dynamicPortCounter++;

        // 如果超出范围，重置到起始端口
        if (dynamicPortCounter >= MAX_DYNAMIC_PORT) {
            dynamicPortCounter = 10000;
        }

        return port;
    }

    // 寻找可用端口
    private int findAvailablePort(int startPort) {
        int port = startPort;

        while (port < MAX_DYNAMIC_PORT) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                // 如果能成功创建ServerSocket，说明端口可用
                serverSocket.close();
                return port;
            } catch (IOException e) {
                // 端口被占用，尝试下一个
                port++;
            }
        }

        // 如果没找到可用端口，使用0让系统自动分配
        return 0;
    }

    public static boolean isConnected() {
        return isConnected;
    }
}