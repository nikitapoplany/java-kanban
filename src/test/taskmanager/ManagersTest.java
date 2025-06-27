package test.taskmanager;

import model.Task;
import taskmanager.HistoryManager;
import taskmanager.InMemoryHistoryManager;
import taskmanager.InMemoryTaskManager;
import taskmanager.Managers;
import taskmanager.TaskManager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldReturnTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "TaskManager should not be null");
        assertTrue(taskManager instanceof InMemoryTaskManager, "TaskManager should be an instance of InMemoryTaskManager");
    }

    @Test
    void shouldReturnHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "HistoryManager should not be null");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "HistoryManager should be an instance of InMemoryHistoryManager");
    }

    @Test
    void shouldReturnSameHistoryManagerInstance() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();
        
        Task task = new Task("Test Task", "Test Task Description");
        task.setId(1);
        
        historyManager.add(task);
        taskManager.getTaskById(1); // This should add the task to the history
        
        assertTrue(historyManager.getHistory().contains(task), "Task should be in history");
    }
}