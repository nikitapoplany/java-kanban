package taskmanager.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import taskmanager.model.Task;
import taskmanager.service.TaskManagerTest;
import taskmanager.service.impl.InMemoryTaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Специфические тесты для InMemoryTaskManager, которые не покрываются в TaskManagerTest
 */
class InMemoryTaskManagerSpecificTest extends TaskManagerTest<InMemoryTaskManager> {
    
    /**
     * Создает экземпляр InMemoryTaskManager для тестирования
     * @return экземпляр InMemoryTaskManager
     */
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
    
    /**
     * Тест на проверку отсутствия дубликатов в истории
     * Этот тест специфичен для InMemoryTaskManager, так как использует внутреннюю реализацию
     */
    @Test
    @DisplayName("История не должна содержать дубликатов")
    void getHistory_WhenSameTaskViewedMultipleTimes_ShouldContainTaskOnlyOnce() {
        Task task = new Task("Test Task", "Test Description");
        taskManager.createTask(task);
        
        // Просматриваем задачу несколько раз
        for (int i = 0; i < 5; i++) {
            taskManager.getTaskById(task.getId());
        }
        
        List<Task> history = taskManager.getHistory();
        
        // Проверяем, что задача встречается в истории только один раз
        long count = history.stream()
                .filter(t -> t.getId() == task.getId())
                .count();
        
        assertEquals(1, count, "Задача должна встречаться в истории только один раз");
    }
}