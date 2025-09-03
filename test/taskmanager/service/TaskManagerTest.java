package taskmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.TaskStatus;
import taskmanager.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Абстрактный класс для тестирования реализаций TaskManager
 * @param <T> тип менеджера задач
 */
public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    
    /**
     * Метод для создания экземпляра менеджера задач для тестирования
     * @return экземпляр менеджера задач
     */
    protected abstract T createTaskManager();
    
    @BeforeEach
    public void setUp() {
        taskManager = createTaskManager();
    }
    
    @Test
    public void testCreateAndGetTask() {
        Task task = new Task("Test Task", "Test Description");
        taskManager.createTask(task);
        
        Task retrievedTask = taskManager.getTaskById(task.getId());
        assertNotNull(retrievedTask, "Задача не найдена");
        assertEquals(task.getId(), retrievedTask.getId(), "ID задачи не совпадает");
        assertEquals(task.getName(), retrievedTask.getName(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), retrievedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус задачи не совпадает");
    }
    
    @Test
    public void testCreateAndGetTaskWithTimeFields() {
        Task task = new Task("Test Task", "Test Description");
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);
        task.setStartTime(startTime);
        task.setDuration(duration);
        
        taskManager.createTask(task);
        
        Task retrievedTask = taskManager.getTaskById(task.getId());
        assertNotNull(retrievedTask, "Задача не найдена");
        assertEquals(startTime, retrievedTask.getStartTime(), "Время начала задачи не совпадает");
        assertEquals(duration, retrievedTask.getDuration(), "Продолжительность задачи не совпадает");
        assertEquals(startTime.plus(duration), retrievedTask.getEndTime(), "Время окончания задачи не совпадает");
    }
    
    @Test
    public void testUpdateTask() {
        Task task = new Task("Test Task", "Test Description");
        taskManager.createTask(task);
        
        task.setName("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        
        taskManager.updateTask(task);
        
        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals("Updated Task", updatedTask.getName(), "Название задачи не обновилось");
        assertEquals("Updated Description", updatedTask.getDescription(), "Описание задачи не обновилось");
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus(), "Статус задачи не обновился");
    }
    
    @Test
    public void testDeleteTask() {
        Task task = new Task("Test Task", "Test Description");
        taskManager.createTask(task);
        
        taskManager.deleteTaskById(task.getId());
        
        assertNull(taskManager.getTaskById(task.getId()), "Задача не удалена");
    }
    
    @Test
    public void testCreateAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Epic retrievedEpic = taskManager.getEpicById(epic.getId());
        assertNotNull(retrievedEpic, "Эпик не найден");
        assertEquals(epic.getId(), retrievedEpic.getId(), "ID эпика не совпадает");
        assertEquals(epic.getName(), retrievedEpic.getName(), "Название эпика не совпадает");
        assertEquals(epic.getDescription(), retrievedEpic.getDescription(), "Описание эпика не совпадает");
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus(), "Статус эпика не NEW");
    }
    
    @Test
    public void testCreateAndGetSubtask() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId());
        taskManager.createSubtask(subtask);
        
        Subtask retrievedSubtask = taskManager.getSubtaskById(subtask.getId());
        assertNotNull(retrievedSubtask, "Подзадача не найдена");
        assertEquals(subtask.getId(), retrievedSubtask.getId(), "ID подзадачи не совпадает");
        assertEquals(subtask.getName(), retrievedSubtask.getName(), "Название подзадачи не совпадает");
        assertEquals(subtask.getDescription(), retrievedSubtask.getDescription(), "Описание подзадачи не совпадает");
        assertEquals(subtask.getStatus(), retrievedSubtask.getStatus(), "Статус подзадачи не совпадает");
        assertEquals(epic.getId(), retrievedSubtask.getEpicId(), "ID эпика подзадачи не совпадает");
    }
    
    @Test
    public void testGetSubtasksByEpicId() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask1 = new Subtask("Test Subtask 1", "Test Description 1", epic.getId());
        Subtask subtask2 = new Subtask("Test Subtask 2", "Test Description 2", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        
        List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epic.getId());
        assertEquals(2, subtasks.size(), "Неверное количество подзадач");
        assertTrue(subtasks.contains(subtask1), "Подзадача 1 не найдена");
        assertTrue(subtasks.contains(subtask2), "Подзадача 2 не найдена");
    }
    
    @Test
    public void testEpicStatusCalculation_AllNew() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask1 = new Subtask("Test Subtask 1", "Test Description 1", epic.getId());
        Subtask subtask2 = new Subtask("Test Subtask 2", "Test Description 2", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        
        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic.getId()).getStatus(), 
                "Статус эпика должен быть NEW, когда все подзадачи NEW");
    }
    
    @Test
    public void testEpicStatusCalculation_AllDone() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask1 = new Subtask("Test Subtask 1", "Test Description 1", epic.getId());
        Subtask subtask2 = new Subtask("Test Subtask 2", "Test Description 2", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        
        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus(), 
                "Статус эпика должен быть DONE, когда все подзадачи DONE");
    }
    
    @Test
    public void testEpicStatusCalculation_MixedNewAndDone() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask1 = new Subtask("Test Subtask 1", "Test Description 1", epic.getId());
        Subtask subtask2 = new Subtask("Test Subtask 2", "Test Description 2", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(), 
                "Статус эпика должен быть IN_PROGRESS, когда подзадачи имеют статусы NEW и DONE");
    }
    
    @Test
    public void testEpicStatusCalculation_InProgress() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId());
        taskManager.createSubtask(subtask);
        
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(), 
                "Статус эпика должен быть IN_PROGRESS, когда хотя бы одна подзадача IN_PROGRESS");
    }
    
    @Test
    public void testEpicTimeCalculation() {
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        LocalDateTime now = LocalDateTime.now();
        
        Subtask subtask1 = new Subtask("Test Subtask 1", "Test Description 1", epic.getId());
        subtask1.setStartTime(now);
        subtask1.setDuration(Duration.ofMinutes(30));
        
        Subtask subtask2 = new Subtask("Test Subtask 2", "Test Description 2", epic.getId());
        subtask2.setStartTime(now.plusHours(1));
        subtask2.setDuration(Duration.ofMinutes(45));
        
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        
        assertEquals(now, updatedEpic.getStartTime(), "Время начала эпика должно быть равно времени начала самой ранней подзадачи");
        assertEquals(now.plusHours(1).plusMinutes(45), updatedEpic.getEndTime(), "Время окончания эпика должно быть равно времени окончания самой поздней подзадачи");
        assertEquals(Duration.ofMinutes(75), updatedEpic.getDuration(), "Продолжительность эпика должна быть равна сумме продолжительностей подзадач");
    }
    
    @Test
    public void testTaskOverlap() {
        Task task1 = new Task("Task 1", "Description 1");
        LocalDateTime now = LocalDateTime.now();
        task1.setStartTime(now);
        task1.setDuration(Duration.ofMinutes(30));
        taskManager.createTask(task1);
        
        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(now.plusMinutes(15)); // Пересекается с task1
        task2.setDuration(Duration.ofMinutes(30));
        
        assertThrows(IllegalStateException.class, () -> taskManager.createTask(task2),
                "Должно быть выброшено исключение при попытке создать задачу, пересекающуюся по времени с существующей");
    }
    
    @Test
    public void testGetPrioritizedTasks() {
        Task task1 = new Task("Task 1", "Description 1");
        LocalDateTime now = LocalDateTime.now();
        task1.setStartTime(now.plusHours(2));
        task1.setDuration(Duration.ofMinutes(30));
        
        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(now.plusHours(1));
        task2.setDuration(Duration.ofMinutes(30));
        
        Task task3 = new Task("Task 3", "Description 3");
        // Без времени начала
        
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        
        assertEquals(2, prioritizedTasks.size(), "В списке должно быть только 2 задачи с временем начала");
        assertEquals(task2.getId(), prioritizedTasks.get(0).getId(), "Первой должна быть задача с более ранним временем начала");
        assertEquals(task1.getId(), prioritizedTasks.get(1).getId(), "Второй должна быть задача с более поздним временем начала");
    }
    
    @Test
    public void testHistory() {
        Task task = new Task("Test Task", "Test Description");
        taskManager.createTask(task);
        
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId());
        taskManager.createSubtask(subtask);
        
        // Получаем задачи, чтобы они попали в историю
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());
        
        List<Task> history = taskManager.getHistory();
        
        assertEquals(3, history.size(), "В истории должно быть 3 задачи");
        assertEquals(task.getId(), history.get(0).getId(), "Первой в истории должна быть обычная задача");
        assertEquals(epic.getId(), history.get(1).getId(), "Второй в истории должен быть эпик");
        assertEquals(subtask.getId(), history.get(2).getId(), "Третьей в истории должна быть подзадача");
    }
}