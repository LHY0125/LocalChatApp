package server.serveice;

import server.ServerMainThread;
import server.data.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.*;

/**
 * 不可独立创建线程，这个会在内部自主创建线程。
 * 每个客户端连接对应一个线程，用于处理客户端的请求。
 * 线程启动后，会进入一个循环，等待服务端发送的消息。
 * 收到消息后，调用 ServerRequestHandler 进行处理。
 */
public class ClientChatThread implements Runnable, ServerRequestHandler.ResponseSender {
    // 群在线用户存储：全局静态、线程安全
    // key：用户id value：用户在线输出流
    private static final Map<String, ObjectOutputStream> USER_ONLINE_MAP = new ConcurrentHashMap<>();

    // 与客户端相连的套接字
    private final Socket clientSocket;
    // 是否登录
    private boolean isLogin = false;
    // 用户的账户
    private String userId;
    // 阻塞队列，用于进行线程的信息交流
    private BlockingQueue<Wrapper> messageQueue;

    private ObjectOutputStream oos;

    private volatile boolean isRunning = true;

    // 业务逻辑处理器
    private final ServerRequestHandler requestHandler;

    /**
     * 创建一个数据发送线程，并且附带创建一个信息接收线程。
     * 由于两个线程总是同步创建和销毁的，因此不进行单独创建。
     *
     * @param clientSocket 与客户端相连的套接字
     */
    public ClientChatThread(Socket clientSocket, BlockingQueue<Wrapper> threadQueue)
            throws IOException {
        this.clientSocket = clientSocket;
        this.userId = null;
        this.isLogin = false;

        messageQueue = threadQueue;
        this.requestHandler = new ServerRequestHandler(this);
    }

    // 线程核心：聊天业务主流程
    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (ServerMainThread.isRunning() && this.isRunning) {
            try {
                Wrapper msg = messageQueue.take();
                // 委托给 RequestHandler 处理
                requestHandler.handleRequest(msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        closeClient();
    }

    /**
     * 关闭当前用户链接，关闭这个线程(接受线程在接收关闭信息时已经结束了）
     */
    private void closeClient() {
        // 优先结束阻塞队列
        ServerMainThread.dropMsgQueue(this.clientSocket);

        if (!isRunning) {
            return;
        }

        isRunning = false;
        if (oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                System.err.println("关闭输出流异常: " + e.getMessage());
            }
        }
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭socket异常: " + e.getMessage());
            }
        }
    }

    // --- ResponseSender 接口实现 ---

    @Override
    public void sendToSelf(Wrapper o) {
        try {
            synchronized (oos) {
                oos.writeObject(o);
                oos.flush();
                oos.reset();
            }
        } catch (IOException e) {
            // Thread.currentThread().interrupt(); // 这里不需要 interrupt，可能是暂时的网络波动
            System.out.println("send error");
        }
    }

    @Override
    public void sendToUser(Wrapper wrapper, String userId) {
        try {
            ObjectOutputStream userOos = USER_ONLINE_MAP.get(userId);
            if (userOos != null) {
                synchronized (userOos) {
                    userOos.writeObject(wrapper);
                    userOos.flush();
                    userOos.reset();
                }
            }
        } catch (IOException e) {
            System.err.println("send error: " + userId);
        }
    }

    @Override
    public void sendToGroup(Wrapper wrapper, String groupId) {
        List<String> members = ServerData.getInstance().getGroupMembersId(groupId);
        for (String member : members) {
            sendToUser(wrapper, member);
        }
    }

    @Override
    public void sendToGroupExceptSelf(Wrapper wrapper, String groupId) {
        List<String> members = ServerData.getInstance().getGroupMembersId(groupId);
        for (String member : members) {
            if (!member.equals(userId)) {
                sendToUser(wrapper, member);
            }
        }
    }

    @Override
    public String getCurrentUserId() {
        return this.userId;
    }

    @Override
    public void setCurrentUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    @Override
    public void registerOnlineUser(String userId, ObjectOutputStream ignored) {
        // 使用当前的 oos
        USER_ONLINE_MAP.put(userId, this.oos);
    }

    @Override
    public void removeOnlineUser(String userId) {
        USER_ONLINE_MAP.remove(userId);
        // 如果是自己登出，也触发清理
        if (userId.equals(this.userId)) {
            closeClient();
        }
    }

    @Override
    public void sentToConnectedGroups(Wrapper wrapper, String userId) {
        TreeSet<String> groups = ServerData.getInstance().getUserGroups(userId);
        groups.forEach(groupId -> sendToGroup(wrapper, groupId));
    }

    // 保留静态方法供其他地方使用（如果有的话）
    public static void broadcastMsg(String[] userIds, Wrapper wrapper) {
        for (String userId : userIds) {
            ObjectOutputStream oos = USER_ONLINE_MAP.get(userId);
            if (oos == null)
                continue;
            try {
                synchronized (oos) {
                    oos.writeObject(wrapper);
                    oos.flush();
                    oos.reset();
                }
            } catch (IOException e) {
                System.err.println("broadcast send error");
            }
        }
    }
}
