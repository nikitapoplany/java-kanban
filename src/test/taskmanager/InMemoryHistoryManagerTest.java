package test.taskmanager;

import model.Task;
import taskmanager.HistoryManager;
import taskmanager.Managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task("Task 1", "Task 1 Description");
        task1.setId(1);
        task2 = new Task("Task 2", "Task 2 Description");
        task2.setId(2);
    }

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "History should not be null");
        assertFalse(history.isEmpty(), "History should not be empty");
        assertTrue(history.contains(task1), "Task should be added to history");
    }

    @Test
    void shouldNotAddNullTaskToHistory() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "History should not be null");
        assertTrue(history.isEmpty(), "Null task should not be added to history");
    }

    @Test
    void shouldLimitHistoryToTenItems() {
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Task " + i, "Task " + i + " Description");
            task.setId(i);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "History should not be null");
        assertTrue(history.size() <= 10, "History should be limited to 10 items");
    }

    @Test
    void shouldRemoveOldestItemWhenHistoryIsFull() {
        for (int i = 0; i < 10; i++) {
            Task task = new Task("Task " + i, "Task " + i + " Description");
            task.setId(i);
            historyManager.add(task);
        }
        Task task11 = new Task("Task 11", "Task 11 Description");
        task11.setId(11);
        historyManager.add(task11);
        
        List<Task> history = historyManager.getHistory();
        
        Task task0 = new Task("Task 0", "Task 0 Description");
        task0.setId(0);
        
        assertNotNull(history, "History should not be null");
        assertFalse(history.contains(task0), "Oldest task should be removed");
        assertTrue(history.contains(task11), "Newest task should be in history");
    }
}