package taskmanager.model;

import taskmanager.model.Task;
import taskmanager.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private static final String TASK_NAME = "Task";
    private static final String TASK_DESCRIPTION = "Task Description";
    private static final String TASK_1_NAME = "Task 1";
    private static final String TASK_1_DESCRIPTION = "Task 1 Description";
    private static final String TASK_2_NAME = "Task 2";
    private static final String TASK_2_DESCRIPTION = "Task 2 Description";
    private static final int TASK_ID_1 = 1;
    private static final int TASK_ID_2 = 2;

    @Test
    @DisplayName("Задачи с одинаковым ID должны быть равны")
    void equals_WhenIdsAreEqual_ShouldBeEqual() {
        Task task1 = new Task(TASK_1_NAME, TASK_1_DESCRIPTION);
        Task task2 = new Task(TASK_2_NAME, TASK_2_DESCRIPTION);

        task1.setId(TASK_ID_1);
        task2.setId(TASK_ID_1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    @DisplayName("Задачи с разными ID не должны быть равны")
    void equals_WhenIdsAreDifferent_ShouldNotBeEqual() {
        Task task1 = new Task(TASK_1_NAME, TASK_1_DESCRIPTION);
        Task task2 = new Task(TASK_1_NAME, TASK_1_DESCRIPTION);

        task1.setId(TASK_ID_1);
        task2.setId(TASK_ID_2);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны");
    }

    @Test
    @DisplayName("Новая задача должна иметь статус NEW по умолчанию")
    void getStatus_ForNewTask_ShouldBeNewByDefault() {
        Task task = new Task(TASK_NAME, TASK_DESCRIPTION);

        TaskStatus status = task.getStatus();

        assertEquals(TaskStatus.NEW, status, "Задача должна иметь статус NEW по умолчанию");
    }

    @Test
    @DisplayName("Статус задачи должен быть изменяемым")
    void setStatus_WhenChangingStatus_ShouldUpdateStatus() {
        Task task = new Task(TASK_NAME, TASK_DESCRIPTION);

        task.setStatus(TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus(), "Статус задачи должен быть изменен на IN_PROGRESS");

        task.setStatus(TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, task.getStatus(), "Статус задачи должен быть изменен на DONE");
    }
}
