package test;

import server.data.ServerData;
import util.FileUtil;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    public void testLoadEmptyFile() throws IOException {
        // 测试加载空文件的情况
        Path dataPath = Paths.get(FileUtil.DATA_FILE, FileUtil.GROUPS_DIR, FileUtil.SERVER_DATA_FILENAME);
        File folder = dataPath.getParent().toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 创建空文件
        Files.write(dataPath, new byte[0]);

        System.out.println("Created empty data file: " + dataPath.toAbsolutePath());

        // 重置实例，强制重新加载
        ServerData.resetInstance();

        // 测试获取实例是否返回非空对象
        ServerData data = ServerData.getInstance();

        assertNotNull(data);
        assertNotNull(data.getServerGroups()); // 确保群组映射不为空
        // assertTrue(data.getServerGroups().isEmpty()); // 可能会初始化空映射

        System.out.println("Test passed: Empty file handled correctly.");

        // 删除测试文件
        Files.deleteIfExists(dataPath);
    }

    @Test
    public void testLoadMissingFile() {
        // 测试加载缺失文件的情况
        Path dataPath = Paths.get(FileUtil.DATA_FILE, FileUtil.GROUPS_DIR, FileUtil.SERVER_DATA_FILENAME);
        try {
            Files.deleteIfExists(dataPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Ensured data file is missing.");

        // 重置实例，强制重新加载
        ServerData.resetInstance();

        // 测试获取实例是否返回非空对象
        ServerData data = ServerData.getInstance();

        assertNotNull(data);
        assertNotNull(data.getServerGroups()); // 确保群组映射不为空
        assertTrue(data.getServerGroups().isEmpty()); // 可能会初始化空映射

        System.out.println("Test passed: Missing file handled correctly.");
    }

    @Test
    public void testLoadValidFile() throws IOException {
        // 测试加载有效文件的情况
        System.out.println("Starting testLoadValidFile...");
        // 测试加载有效文件的情况
        Path dataPath = Paths.get(FileUtil.DATA_FILE, FileUtil.GROUPS_DIR, FileUtil.SERVER_DATA_FILENAME);
        try {
            Files.deleteIfExists(dataPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 初始化 ServerData 实例，确保其为空
        ServerData.resetInstance();
        ServerData data = ServerData.getInstance();
        System.out.println("Initialized ServerData for saving.");

        // 保存 ServerData 实例，确保文件存在
        data.saveServerData();
        System.out.println("Saved ServerData.");

        // 重置实例，强制重新加载
        ServerData.resetInstance();
        System.out.println("Reset instance. Now calling getInstance() to trigger load...");

        // 测试获取实例是否返回非空对象
        // 确保加载的 ServerData 实例不为空
        ServerData loaded = ServerData.getInstance();

        assertNotNull(loaded);
        System.out.println("Test passed: Valid file loaded correctly.");
    }
}