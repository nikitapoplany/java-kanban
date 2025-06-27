package test.taskmanager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import taskmanager.Managers;
import taskmanager.TaskManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        task = new Task("Test Task", "Test Task Description");
        epic = new Epic("Test Epic", "Test Epic Description");
        taskManager.createEpic(epic);
        subtask = new Subtask("Test Subtask", "Test Subtask Description", epic.getId());
    }

    @Test
    void shouldAddTaskWithGeneratedId() {
        taskManager.createTask(task);
        assertNotEquals(0, task.getId(), "Task ID should be generated");
        assertEquals(task, taskManager.getTaskById(task.getId()), "Task should be found by ID");
    }

    @Test
    void shouldAddEpicWithGeneratedId() {
        assertNotEquals(0, epic.getId(), "Epic ID should be generated");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Epic should be found by ID");
    }

    @Test
    void shouldAddSubtaskWithGeneratedId() {
        taskManager.createSubtask(subtask);
        assertNotEquals(0, subtask.getId(), "Subtask ID should be generated");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Subtask should be found by ID");
    }

    @Test
    void shouldUpdateTask() {
        taskManager.createTask(task);
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskById(task.getId()).getStatus(), "Task status should be updated");
    }

    @Test
    void shouldUpdateEpic() {
        epic.setName("Updated Epic");
        taskManager.updateEpic(epic);
        assertEquals("Updated Epic", taskManager.getEpicById(epic.getId()).getName(), "Epic name should be updated");
    }

    @Test
    void shouldUpdateSubtask() {
        taskManager.createSubtask(subtask);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtaskById(subtask.getId()).getStatus(), "Subtask status should be updated");
    }

    @Test
    void shouldDeleteTask() {
        taskManager.createTask(task);
        int taskId = task.getId();
        taskManager.deleteTaskById(taskId);
        assertNull(taskManager.getTaskById(taskId), "Task should not be found after deletion");
    }

    @Test
    void shouldDeleteEpic() {
        int epicId = epic.getId();
        taskManager.deleteEpicById(epicId);
        assertNull(taskManager.getEpicById(epicId), "Epic should not be found after deletion");
    }

    @Test
    void shouldDeleteSubtask() {
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();
        taskManager.deleteSubtaskById(subtaskId);
        assertNull(taskManager.getSubtaskById(subtaskId), "Subtask should not be found after deletion");
    }

    @Test
    void shouldAddTaskToHistory() {
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "History should not be null");
        assertFalse(history.isEmpty(), "History should not be empty");
        assertTrue(history.contains(task), "Task should be added to history");
    }

    @Test
    void shouldNotAddSameTaskToHistoryMoreThanTenTimes() {
        taskManager.createTask(task);
        for (int i = 0; i < 15; i++) {
            taskManager.getTaskById(task.getId());
        }
        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "History should not be null");
        assertTrue(history.size() <= 10, "History should not contain more than 10 items");
    }
}