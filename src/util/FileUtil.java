package util;

import server.data.ServerData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/*
 *通用文件IO工具类
 *将聊天记录等信息写入本地文件
 */
public class FileUtil {
    // 数据文件夹
    public static final String DATA_FILE = "data";

    // 子文件夹 (保留用于兼容旧数据)
    public static final String GROUPS_DIR = "groups";
    public static final String FRIENDS_DIR = "friends";

    // 旧的服务器数据文件名 (保留用于兼容旧数据)
    // public static final String SERVER_DATA_FILENAME = "server_data.data";

    // 新的统一服务器数据文件名
    public static final String SERVER_FULL_DATA_FILENAME = "server_data_full.data";

    // 聊天历史消息存储文件夹名
    public static final String CHAT_DATA_DIRNAME = "chat_data";

    // 辅助方法：获取新的统一数据文件路径
    private static Path getServerFullDataPath() {
        return Paths.get(DATA_FILE, SERVER_FULL_DATA_FILENAME);
    }

    // 辅助方法：获取旧的 Group ServerData路径
    private static Path getOldGroupServerDataPath() {
        return Paths.get(DATA_FILE, GROUPS_DIR, "server_data.data");
    }

    // 辅助方法：获取旧的 Friend ServerData路径
    private static Path getOldFriendServerDataPath() {
        return Paths.get(DATA_FILE, FRIENDS_DIR, "server_data.data");
    }

    // 辅助方法：获取Chat Data路径
    private static Path getChatDataPath(String id, boolean isGroup) {
        String subDir = isGroup ? GROUPS_DIR : FRIENDS_DIR;
        return Paths.get(DATA_FILE, subDir, CHAT_DATA_DIRNAME, id + ".txt");
    }

    /**
     * 读取文件中的serverData信息，返回ServerData对象
     * 优先读取新的统一文件，如果不存在则尝试读取旧文件并合并
     */
    public static ServerData loadServerData() {
        // 1. 尝试加载新的统一数据文件
        ServerData fullData = loadDataFromFile(getServerFullDataPath());
        if (fullData != null) {
            System.out.println("成功加载统一服务器数据文件");
            return fullData;
        }

        System.out.println("统一数据文件不存在，尝试加载旧版本数据并合并...");

        // 2. 如果不存在，尝试加载旧数据并合并
        ServerData finalData = new ServerData();
        boolean loadedAny = false;

        // Load Group Data
        ServerData groupData = loadDataFromFile(getOldGroupServerDataPath());
        if (groupData != null) {
            if (groupData.getServerGroups() != null) {
                finalData.setServerGroups(groupData.getServerGroups());
            }
            loadedAny = true;
        }

        // Load Friend Data
        ServerData friendData = loadDataFromFile(getOldFriendServerDataPath());
        if (friendData != null) {
            if (friendData.getServerUsers() != null) {
                finalData.setServerUsers(friendData.getServerUsers());
            }
            loadedAny = true;
        }

        if (loadedAny) {
            System.out.println("旧数据加载并合并成功，将保存为新格式...");
            // 立即保存为新格式，以便下次直接加载
            // 注意：这里不能直接调用 saveServerData()，因为它依赖 ServerData.getInstance()，而此时可能正在初始化
            // getInstance
            // 所以我们直接调用内部保存逻辑
            saveDataToFile(getServerFullDataPath(), finalData);
            return finalData;
        }

        return null;
    }

    private static ServerData loadDataFromFile(Path path) {
        if (!Files.exists(path) || path.toFile().length() == 0)
            return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path.toFile())))) {
            return (ServerData) ois.readObject();
        } catch (Exception e) {
            System.err.println("加载数据失败 (" + path + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * 使用静态方法获取serverData对象，将serverData中的信息写入文件中
     * 直接序列化整个 ServerData 对象
     */
    public static void saveServerData() {
        ServerData data = ServerData.getInstance();
        saveDataToFile(getServerFullDataPath(), data);
        System.out.println("服务器数据保存成功 (统一格式)");
    }

    private static void saveDataToFile(Path path, ServerData data) {
        File file = path.toFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // 确保 ServerData 本身是可序列化的，并且所有字段（serverUsers, serverGroups 等）也是可序列化的
        synchronized (ServerData.class) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)))) {
                oos.writeObject(data);
            } catch (IOException e) {
                System.err.println("保存数据失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查数据文件是否存在 (任意一个存在即认为存在)
     */
    public static boolean isDataFileExists() {
        return Files.exists(getServerFullDataPath()) ||
                Files.exists(getOldGroupServerDataPath()) ||
                Files.exists(getOldFriendServerDataPath());
    }

    /**
     * 获取数据文件大小（字节）
     */
    public static long getDataFileSize() {
        long size = 0;
        File fullFile = getServerFullDataPath().toFile();
        if (fullFile.exists())
            size += fullFile.length();

        File gFile = getOldGroupServerDataPath().toFile();
        if (gFile.exists())
            size += gFile.length();

        File fFile = getOldFriendServerDataPath().toFile();
        if (fFile.exists())
            size += fFile.length();

        return size;
    }

    // ===================================聊天信息=======================

    /**
     * 将聊天信息追加写入文件
     *
     * @param groupId    群聊id 或 用户id
     * @param senderId   发送者id
     * @param senderName 发送者名字
     * @param content    发送内容
     */
    public static void addChatMessage(String groupId, String senderId, String senderName, String content) {
        String msg = MsgUtil.combineMsg(senderId, senderName, content);
        writeChatMsg(groupId, msg);
    }

    public static void addChatMessage(String groupId, String contentFromWrapper) {
        writeChatMsg(groupId, contentFromWrapper);
    }

    private static void writeChatMsg(String id, String content) {
        // 判断是群聊还是私聊
        // 通过 ServerGroups 来判断，如果在群组Map中，则是群聊，否则认为是私聊
        boolean isGroup = false;
        if (ServerData.getInstance().getServerGroups() != null) {
            isGroup = ServerData.getInstance().getServerGroups().containsKey(id);
        }

        Path path = getChatDataPath(id, isGroup);
        File file = path.toFile();

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            // 写入文本并换行
            bw.write(content);
            bw.newLine(); // 跨平台换行
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取某个群的所有聊天记录
     *
     * @return 历史消息列表，无内容/文件不存在返回空List，绝不返回null
     */
    public static List<String> loadGroupChatMsg(String groupId) {
        // 判断是群聊还是私聊
        boolean isGroup = false;
        if (ServerData.getInstance().getServerGroups() != null) {
            isGroup = ServerData.getInstance().getServerGroups().containsKey(groupId);
        }

        Path path = getChatDataPath(groupId, isGroup);

        // 尝试从新路径读取，如果不存在，且是群聊，尝试旧路径（为了简单，这里暂时不处理旧聊天记录的迁移，因为聊天记录是文本文件，相对独立）
        // 如果需要，可以在这里增加 fallback 逻辑

        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (NoSuchFileException e) {
            lines = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (lines == null) {
            lines = new ArrayList<>();
        }
        return lines;
    }
}
