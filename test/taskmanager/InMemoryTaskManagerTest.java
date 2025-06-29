package taskmanager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private static final String TASK_NAME = "Test Task";
    private static final String TASK_DESCRIPTION = "Test Task Description";
    private static final String EPIC_NAME = "Test Epic";
    private static final String EPIC_DESCRIPTION = "Test Epic Description";
    private static final String SUBTASK_NAME = "Test Subtask";
    private static final String SUBTASK_DESCRIPTION = "Test Subtask Description";
    private static final String UPDATED_EPIC_NAME = "Updated Epic";
    private static final int MAX_HISTORY_SIZE = 10;

    private TaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {

        taskManager = Managers.getDefault();
        task = new Task(TASK_NAME, TASK_DESCRIPTION);
        epic = new Epic(EPIC_NAME, EPIC_DESCRIPTION);
        taskManager.createEpic(epic);
        subtask = new Subtask(SUBTASK_NAME, SUBTASK_DESCRIPTION, epic.getId());
    }

    @Test
    @DisplayName("Задача должна быть создана со сгенерированным ID")
    void createTask_WithValidTask_ShouldAddTaskWithGeneratedId() {

        taskManager.createTask(task);

        assertNotEquals(0, task.getId(), "ID задачи должен быть сгенерирован");
        assertEquals(task, taskManager.getTaskById(task.getId()), "Задача должна быть найдена по ID");
    }

    @Test
    @DisplayName("Эпик должен быть создан со сгенерированным ID")
    void createEpic_WithValidEpic_ShouldAddEpicWithGeneratedId() {
        int epicId = epic.getId();

        assertNotEquals(0, epicId, "ID эпика должен быть сгенерирован");
        assertEquals(epic, taskManager.getEpicById(epicId), "Эпик должен быть найден по ID");
    }

    @Test
    @DisplayName("Подзадача должна быть создана со сгенерированным ID")
    void createSubtask_WithValidSubtask_ShouldAddSubtaskWithGeneratedId() {
        taskManager.createSubtask(subtask);

        assertNotEquals(0, subtask.getId(), "ID подзадачи должен быть сгенерирован");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Подзадача должна быть найдена по ID");
    }

    @Test
    @DisplayName("Задача должна быть обновлена с новым статусом")
    void updateTask_WithNewStatus_ShouldUpdateTask() {
        taskManager.createTask(task);

        task.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskById(task.getId()).getStatus(), "Статус задачи должен быть обновлен");
    }

    @Test
    @DisplayName("Эпик должен быть обновлен с новым именем")
    void updateEpic_WithNewName_ShouldUpdateEpic() {
        epic.setName(UPDATED_EPIC_NAME);
        taskManager.updateEpic(epic);

        assertEquals(UPDATED_EPIC_NAME, taskManager.getEpicById(epic.getId()).getName(), "Имя эпика должно быть обновлено");
    }

    @Test
    @DisplayName("Подзадача должна быть обновлена с новым статусом")
    void updateSubtask_WithNewStatus_ShouldUpdateSubtask() {
        taskManager.createSubtask(subtask);

        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtaskById(subtask.getId()).getStatus(), "Статус подзадачи должен быть обновлен");
    }

    @Test
    @DisplayName("Задача должна быть удалена по ID")
    void deleteTaskById_WithValidId_ShouldDeleteTask() {
        taskManager.createTask(task);
        int taskId = task.getId();

        taskManager.deleteTaskById(taskId);

        assertNull(taskManager.getTaskById(taskId), "Задача не должна быть найдена после удаления");
    }

    @Test
    @DisplayName("Эпик должен быть удален по ID")
    void deleteEpicById_WithValidId_ShouldDeleteEpic() {
        int epicId = epic.getId();

        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getEpicById(epicId), "Эпик не должен быть найден после удаления");
    }

    @Test
    @DisplayName("Подзадача должна быть удалена по ID")
    void deleteSubtaskById_WithValidId_ShouldDeleteSubtask() {
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        taskManager.deleteSubtaskById(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача не должна быть найдена после удаления");
    }

    @Test
    @DisplayName("Задача должна быть добавлена в историю при просмотре")
    void getTaskById_WhenTaskViewed_ShouldAddTaskToHistory() {
        taskManager.createTask(task);

        taskManager.getTaskById(task.getId());
        List<Task> history = taskManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertFalse(history.isEmpty(), "История не должна быть пустой");
        assertTrue(history.contains(task), "Задача должна быть добавлена в историю");
    }

    @Test
    @DisplayName("История не должна содержать более 10 элементов")
    void getTaskById_WhenSameTaskViewedMultipleTimes_ShouldNotAddSameTaskToHistoryMoreThanTenTimes() {
        taskManager.createTask(task);

        for (int i = 0; i < 15; i++) {
            taskManager.getTaskById(task.getId());
        }
        List<Task> history = taskManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertTrue(history.size() <= MAX_HISTORY_SIZE, "История не должна содержать более 10 элементов");
    }
}
