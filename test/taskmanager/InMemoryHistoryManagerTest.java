package taskmanager;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private static final String TASK_1_NAME = "Task 1";
    private static final String TASK_1_DESCRIPTION = "Task 1 Description";
    private static final String TASK_2_NAME = "Task 2";
    private static final String TASK_2_DESCRIPTION = "Task 2 Description";
    private static final String TASK_11_NAME = "Task 11";
    private static final String TASK_11_DESCRIPTION = "Task 11 Description";
    private static final String TASK_0_NAME = "Task 0";
    private static final String TASK_0_DESCRIPTION = "Task 0 Description";
    private static final int TASK_ID_1 = 1;
    private static final int TASK_ID_2 = 2;
    private static final int TASK_ID_11 = 11;
    private static final int TASK_ID_0 = 0;
    private static final int MAX_HISTORY_SIZE = 10;

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task(TASK_1_NAME, TASK_1_DESCRIPTION);
        task1.setId(TASK_ID_1);
        task2 = new Task(TASK_2_NAME, TASK_2_DESCRIPTION);
        task2.setId(TASK_ID_2);
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
    @DisplayName("История должна быть ограничена 10 элементами")
    void add_MoreThanMaxItems_ShouldLimitHistorySize() {
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Task " + i, "Task " + i + " Description");
            task.setId(i);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertTrue(history.size() <= MAX_HISTORY_SIZE, "История должна быть ограничена 10 элементами");
    }

    @Test
    @DisplayName("Самый старый элемент должен быть удален, когда история заполнена")
    void add_WhenHistoryIsFull_ShouldRemoveOldestItem() {
        for (int i = 0; i < MAX_HISTORY_SIZE; i++) {
            Task task = new Task("Task " + i, "Task " + i + " Description");
            task.setId(i);
            historyManager.add(task);
        }
        Task task11 = new Task(TASK_11_NAME, TASK_11_DESCRIPTION);
        task11.setId(TASK_ID_11);

        historyManager.add(task11);
        List<Task> history = historyManager.getHistory();

        Task task0 = new Task(TASK_0_NAME, TASK_0_DESCRIPTION);
        task0.setId(TASK_ID_0);

        assertNotNull(history, "История не должна быть null");
        assertFalse(history.contains(task0), "Самый старый элемент должен быть удален");
        assertTrue(history.contains(task11), "Новейшая задача должна быть в истории");
    }
}
