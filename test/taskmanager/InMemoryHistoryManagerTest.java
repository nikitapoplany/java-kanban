package taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import taskmanager.model.Task;
import taskmanager.service.HistoryManager;
import taskmanager.service.impl.InMemoryHistoryManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для InMemoryHistoryManager
 */
class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;
    
    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
        task3 = new Task("Task 3", "Description 3");
        task3.setId(3);
    }

    @Test
    @DisplayName("Задача должна быть добавлена в историю при просмотре")
    void add_ValidTask_ShouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertFalse(history.isEmpty(), "История не должна быть пустой");
        assertTrue(history.contains(task1), "Задача должна быть добавлена в историю");
    }

    @Test
    @DisplayName("Null задача не должна быть добавлена в историю")
    void add_NullTask_ShouldNotAddToHistory() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertTrue(history.isEmpty(), "Null задача не должна быть добавлена в историю");
    }

    @Test
    @DisplayName("История не должна иметь ограничений по размеру")
    void add_ManyItems_ShouldNotLimitHistorySize() {
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Task " + i, "Task " + i + " Description");
            task.setId(i);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(15, history.size(), "История не должна иметь ограничений по размеру");
    }

    @Test
    @DisplayName("При повторном просмотре задачи в истории должен остаться только последний просмотр")
    void add_SameTaskMultipleTimes_ShouldKeepOnlyLastView() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Повторный просмотр task1
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertEquals(task2, history.get(0), "Первой задачей должна быть task2");
        assertEquals(task1, history.get(1), "Второй задачей должна быть task1 (последний просмотр)");
    }

    @Test
    @DisplayName("Метод remove должен удалять задачу из истории")
    void remove_ExistingTask_ShouldRemoveFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        
        historyManager.remove(task2.getId()); // Удаляем task2
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertTrue(history.contains(task1), "task1 должна остаться в истории");
        assertFalse(history.contains(task2), "task2 должна быть удалена из истории");
        assertTrue(history.contains(task3), "task3 должна остаться в истории");
    }

    @Test
    @DisplayName("Метод remove не должен ничего делать, если задачи нет в истории")
    void remove_NonExistingTask_ShouldDoNothing() {
        historyManager.add(task1);
        historyManager.add(task3);
        
        historyManager.remove(task2.getId()); // Удаляем несуществующую в истории task2
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertTrue(history.contains(task1), "task1 должна остаться в истории");
        assertTrue(history.contains(task3), "task3 должна остаться в истории");
    }

    @Test
    @DisplayName("Порядок задач в истории должен сохраняться после удаления")
    void remove_MiddleTask_ShouldPreserveOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        
        historyManager.remove(task2.getId()); // Удаляем task2 из середины
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertEquals(task1, history.get(0), "Первой задачей должна быть task1");
        assertEquals(task3, history.get(1), "Второй задачей должна быть task3");
    }

    @Test
    @DisplayName("Удаление первой задачи из истории")
    void remove_FirstTask_ShouldUpdateHead() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        
        historyManager.remove(task1.getId()); // Удаляем первую задачу
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertEquals(task2, history.get(0), "Первой задачей должна быть task2");
        assertEquals(task3, history.get(1), "Второй задачей должна быть task3");
    }

    @Test
    @DisplayName("Удаление последней задачи из истории")
    void remove_LastTask_ShouldUpdateTail() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        
        historyManager.remove(task3.getId()); // Удаляем последнюю задачу
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertEquals(task1, history.get(0), "Первой задачей должна быть task1");
        assertEquals(task2, history.get(1), "Второй задачей должна быть task2");
    }
}
