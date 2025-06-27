package test.taskmanager;

import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldBeEqualWhenIdsAreEqual() {
        Task task1 = new Task("Task 1", "Task 1 Description");
        Task task2 = new Task("Task 2", "Task 2 Description");
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2, "Tasks with the same ID should be equal");
    }

    @Test
    void shouldNotBeEqualWhenIdsAreDifferent() {
        Task task1 = new Task("Task 1", "Task 1 Description");
        Task task2 = new Task("Task 1", "Task 1 Description");
        task1.setId(1);
        task2.setId(2);
        assertNotEquals(task1, task2, "Tasks with different IDs should not be equal");
    }

    @Test
    void shouldHaveNewStatusByDefault() {
        Task task = new Task("Task", "Task Description");
        assertEquals(TaskStatus.NEW, task.getStatus(), "Task should have NEW status by default");
    }

    @Test
    void shouldChangeStatus() {
        Task task = new Task("Task", "Task Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus(), "Task status should be changed");
        task.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getStatus(), "Task status should be changed");
    }
}