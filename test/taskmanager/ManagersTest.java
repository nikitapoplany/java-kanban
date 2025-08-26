package taskmanager;

import taskmanager.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    private static final String TASK_NAME = "Test Task";
    private static final String TASK_DESCRIPTION = "Test Task Description";
    private static final int TASK_ID = 1;

    @Test
    @DisplayName("Managers.getDefault() должен возвращать экземпляр TaskManager")
    void getDefault_WhenCalled_ShouldReturnTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "TaskManager не должен быть null");
        assertTrue(taskManager instanceof InMemoryTaskManager, "TaskManager должен быть экземпляром InMemoryTaskManager");
    }

    @Test
    @DisplayName("Managers.getDefaultHistory() должен возвращать экземпляр HistoryManager")
    void getDefaultHistory_WhenCalled_ShouldReturnHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не должен быть null");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "HistoryManager должен быть экземпляром InMemoryHistoryManager");
    }

    @Test
    @DisplayName("TaskManager и HistoryManager должны использовать одну и ту же историю")
    void getDefault_AndGetDefaultHistory_ShouldReturnSameHistoryManagerInstance() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(TASK_NAME, TASK_DESCRIPTION);
        task.setId(TASK_ID);

        historyManager.add(task);
        taskManager.getTaskById(TASK_ID);
        assertTrue(historyManager.getHistory().contains(task), "Задача должна быть в истории");
    }
}
