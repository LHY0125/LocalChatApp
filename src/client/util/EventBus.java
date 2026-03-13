package client.util;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 简单的事件总线，用于组件间通信
 * 替代之前的 LocalData 系统消息机制，提供更直接的回调
 */
public class EventBus {
    private static final EventBus instance = new EventBus();
    
    // 事件类型 -> 监听器列表
    private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return instance;
    }

    /**
     * 注册监听器
     * @param eventType 事件类型 (如 "LOGIN_SUCCESS", "REGISTER_FAILED")
     * @param listener 回调函数
     */
    public void subscribe(String eventType, Consumer<Object> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * 发布事件 (在 JavaFX 线程中执行回调)
     * @param eventType 事件类型
     * @param data 事件数据 (可为 null)
     */
    public void publish(String eventType, Object data) {
        List<Consumer<Object>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            // 确保在 JavaFX 线程执行，更新 UI
            Platform.runLater(() -> {
                for (Consumer<Object> listener : eventListeners) {
                    try {
                        listener.accept(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
