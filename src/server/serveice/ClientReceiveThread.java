package server.serveice;

import global.global;
import server.ServerMainThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class ClientReceiveThread implements Runnable {
    private BlockingQueue<Wrapper> messageQueue;
    private volatile boolean isRunning;

    // 接收线程专属资源：构造器传入，仅用于接收消息
    private final Socket clientSocket;
    ObjectInputStream ois;

    // 构造器：初始化套接字资源
    public ClientReceiveThread(
            Socket clientSocket,
            BlockingQueue<Wrapper> messageQueue) throws IOException {

        this.messageQueue = messageQueue;
        this.clientSocket = clientSocket;

        isRunning = true;
    }

    // 线程核心：循环接收服务端消息（历史/广播），打印展示
    @Override
    public void run() {
        try {
            ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 当主线程和自身都在跑的时候
        while (ServerMainThread.isRunning() && this.isRunning) {
            // 接收服务端消息
            // 接收到后将其添加到阻塞队列中
            try {
                Wrapper msg = (Wrapper) ois.readObject();
                // 如果收到的是关闭信息，则这个循环结束后关闭自身
                if (msg.getOperation() == global.OPT_LOGOUT) {
                    isRunning = false;
                    System.out.println("接受线程已结束");
                }
                // 添加信息到阻塞队列。
                messageQueue.put(msg);
            } catch (InterruptedException e) {
                System.out.println("消息队列被中断");
                e.printStackTrace();
            } catch (IOException e) {
                if (isConnectionClosed(e)) {
                    // 链接断开则结束链接
                    // isRunning =false;
                    System.out.println(clientSocket.getPort() + ": 连接已断开");
                    break;
                } else {
                    System.out.println("发送消息时发生IO异常: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isConnectionClosed(IOException e) {
        // 根据异常类型判断连接是否断开
        return e instanceof SocketException ||
                e.getMessage() != null && (e.getMessage().contains("Connection reset") ||
                        e.getMessage().contains("Broken pipe") ||
                        e.getMessage().contains("Connection refused") ||
                        e.getMessage().contains("Software caused connection abort"));
    }
}