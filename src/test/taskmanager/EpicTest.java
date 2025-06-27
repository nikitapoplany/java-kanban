package test.taskmanager;

import model.Epic;
import model.Subtask;
import model.TaskStatus;
import taskmanager.Managers;
import taskmanager.TaskManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private TaskManager taskManager;
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        epic = new Epic("Test Epic", "Test Epic Description");
        taskManager.createEpic(epic);
        subtask1 = new Subtask("Test Subtask 1", "Test Subtask 1 Description", epic.getId());
        subtask2 = new Subtask("Test Subtask 2", "Test Subtask 2 Description", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
    }

    @Test
    void shouldBeNewWhenCreated() {
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Epic status should be NEW when created");
    }

    @Test
    void shouldBeNewWhenAllSubtasksAreNew() {
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Epic status should be NEW when all subtasks are NEW");
    }

    @Test
    void shouldBeInProgressWhenSomeSubtasksAreInProgress() {
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Epic status should be IN_PROGRESS when some subtasks are IN_PROGRESS");
    }

    @Test
    void shouldBeInProgressWhenSomeSubtasksAreDoneAndSomeAreNew() {
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Epic status should be IN_PROGRESS when some subtasks are DONE and some are NEW");
    }

    @Test
    void shouldBeDoneWhenAllSubtasksAreDone() {
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Epic status should be DONE when all subtasks are DONE");
    }

    @Test
    void shouldNotAddEpicToItself() {
        // Проверяем, что эпик нельзя добавить в самого себя в виде подзадачи
        Subtask selfSubtask = new Subtask("Self Subtask", "Self Subtask Description", epic.getId());
        selfSubtask.setId(epic.getId());

        // Сохраняем текущее количество подзадач
        int subtasksCountBefore = taskManager.getAllSubtasks().size();

        // Пытаемся обновить подзадачу
        taskManager.updateSubtask(selfSubtask);

        // Проверяем, что количество подзадач не изменилось
        int subtasksCountAfter = taskManager.getAllSubtasks().size();

        assertEquals(subtasksCountBefore, subtasksCountAfter, "Epic should not be added to itself as a subtask");
    }

    @Test
    void shouldBeEqualWhenIdsAreEqual() {
        Epic epic1 = new Epic("Epic 1", "Epic 1 Description");
        Epic epic2 = new Epic("Epic 2", "Epic 2 Description");
        epic1.setId(1);
        epic2.setId(1);
        assertEquals(epic1, epic2, "Epics with the same ID should be equal");
    }
}