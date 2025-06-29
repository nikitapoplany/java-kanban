package taskmanager;

import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static final String EPIC_NAME = "Test Epic";
    private static final String EPIC_DESCRIPTION = "Test Epic Description";
    private static final String SUBTASK_1_NAME = "Test Subtask 1";
    private static final String SUBTASK_1_DESCRIPTION = "Test Subtask 1 Description";
    private static final String SUBTASK_2_NAME = "Test Subtask 2";
    private static final String SUBTASK_2_DESCRIPTION = "Test Subtask 2 Description";
    private static final String SELF_SUBTASK_NAME = "Self Subtask";
    private static final String SELF_SUBTASK_DESCRIPTION = "Self Subtask Description";
    private static final String EPIC_1_NAME = "Epic 1";
    private static final String EPIC_1_DESCRIPTION = "Epic 1 Description";
    private static final String EPIC_2_NAME = "Epic 2";
    private static final String EPIC_2_DESCRIPTION = "Epic 2 Description";
    private static final int EPIC_ID_1 = 1;

    private TaskManager taskManager;
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        epic = new Epic(EPIC_NAME, EPIC_DESCRIPTION);
        taskManager.createEpic(epic);
        subtask1 = new Subtask(SUBTASK_1_NAME, SUBTASK_1_DESCRIPTION, epic.getId());
        subtask2 = new Subtask(SUBTASK_2_NAME, SUBTASK_2_DESCRIPTION, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
    }

    @Test
    @DisplayName("Эпик должен иметь статус NEW при создании")
    void getStatus_WhenCreated_ShouldBeNew() {
        TaskStatus status = epic.getStatus();

        assertEquals(TaskStatus.NEW, status, "Статус эпика должен быть NEW при создании");
    }

    @Test
    @DisplayName("Эпик должен иметь статус NEW, когда все подзадачи имеют статус NEW")
    void getStatus_WhenAllSubtasksAreNew_ShouldBeNew() {
        TaskStatus status = epic.getStatus();

        assertEquals(TaskStatus.NEW, status, "Статус эпика должен быть NEW, когда все подзадачи имеют статус NEW");
    }

    @Test
    @DisplayName("Эпик должен иметь статус IN_PROGRESS, когда некоторые подзадачи имеют статус IN_PROGRESS")
    void getStatus_WhenSomeSubtasksAreInProgress_ShouldBeInProgress() {
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS, когда некоторые подзадачи имеют статус IN_PROGRESS");
    }

    @Test
    @DisplayName("Эпик должен иметь статус IN_PROGRESS, когда некоторые подзадачи имеют статус DONE, а некоторые - NEW")
    void getStatus_WhenSomeSubtasksAreDoneAndSomeAreNew_ShouldBeInProgress() {
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS, когда некоторые подзадачи имеют статус DONE, а некоторые - NEW");
    }

    @Test
    @DisplayName("Эпик должен иметь статус DONE, когда все подзадачи имеют статус DONE")
    void getStatus_WhenAllSubtasksAreDone_ShouldBeDone() {
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика должен быть DONE, когда все подзадачи имеют статус DONE");
    }

    @Test
    @DisplayName("Эпик не должен быть добавлен сам к себе в качестве подзадачи")
    void updateSubtask_WhenEpicAddedToItself_ShouldNotAddEpicToItself() {
        Subtask selfSubtask = new Subtask(SELF_SUBTASK_NAME, SELF_SUBTASK_DESCRIPTION, epic.getId());
        selfSubtask.setId(epic.getId());

        int subtasksCountBefore = taskManager.getAllSubtasks().size();

        taskManager.updateSubtask(selfSubtask);

        int subtasksCountAfter = taskManager.getAllSubtasks().size();
        assertEquals(subtasksCountBefore, subtasksCountAfter, "Эпик не должен быть добавлен сам к себе в качестве подзадачи");
    }

    @Test
    @DisplayName("Эпики с одинаковым ID должны быть равны")
    void equals_WhenIdsAreEqual_ShouldBeEqual() {
        Epic epic1 = new Epic(EPIC_1_NAME, EPIC_1_DESCRIPTION);
        Epic epic2 = new Epic(EPIC_2_NAME, EPIC_2_DESCRIPTION);

        epic1.setId(EPIC_ID_1);
        epic2.setId(EPIC_ID_1);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
    }
}
