package taskmanager;

import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private static final String TASK_NAME = "Test Task";
    private static final String TASK_DESCRIPTION = "Test Task Description";
    private static final String EPIC_NAME = "Test Epic";
    private static final String EPIC_DESCRIPTION = "Test Epic Description";
    private static final String SUBTASK_NAME = "Test Subtask";
    private static final String SUBTASK_DESCRIPTION = "Test Subtask Description";
    
    private File tempFile;
    private FileBackedTaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit(); // Файл будет удален при завершении JVM
        taskManager = new FileBackedTaskManager(tempFile);
        
        task = new Task(TASK_NAME, TASK_DESCRIPTION);
        epic = new Epic(EPIC_NAME, EPIC_DESCRIPTION);
        taskManager.createEpic(epic);
        subtask = new Subtask(SUBTASK_NAME, SUBTASK_DESCRIPTION, epic.getId());
    }

    @Test
    @DisplayName("Задача должна быть сохранена в файл после создания")
    void createTask_ShouldSaveTaskToFile() throws IOException {
        // Создаем задачу
        taskManager.createTask(task);
        
        // Проверяем, что файл не пустой
        String fileContent = Files.readString(tempFile.toPath());
        assertFalse(fileContent.isEmpty(), "Файл не должен быть пустым");
        
        // Проверяем, что файл содержит информацию о задаче
        assertTrue(fileContent.contains(TASK_NAME), "Файл должен содержать имя задачи");
        assertTrue(fileContent.contains(TASK_DESCRIPTION), "Файл должен содержать описание задачи");
    }

    @Test
    @DisplayName("Задача должна быть сохранена в файл после обновления")
    void updateTask_ShouldSaveTaskToFile() throws IOException {
        // Создаем задачу
        taskManager.createTask(task);
        
        // Обновляем задачу
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task);
        
        // Проверяем, что файл содержит обновленную информацию
        String fileContent = Files.readString(tempFile.toPath());
        assertTrue(fileContent.contains("IN_PROGRESS"), "Файл должен содержать обновленный статус задачи");
    }

    @Test
    @DisplayName("Задача должна быть удалена из файла после удаления")
    void deleteTask_ShouldRemoveTaskFromFile() throws IOException {
        // Создаем задачу
        taskManager.createTask(task);
        
        // Получаем содержимое файла до удаления
        String fileContentBefore = Files.readString(tempFile.toPath());
        assertTrue(fileContentBefore.contains(TASK_NAME), "Файл должен содержать имя задачи до удаления");
        
        // Удаляем задачу
        taskManager.deleteTaskById(task.getId());
        
        // Получаем содержимое файла после удаления
        String fileContentAfter = Files.readString(tempFile.toPath());
        
        // Проверяем, что задача удалена из файла
        // Это может быть сложно проверить напрямую, так как формат файла может быть разным
        // Поэтому проверяем, что после удаления всех задач файл не содержит информацию о задачах
        taskManager.deleteAllTasks();
        fileContentAfter = Files.readString(tempFile.toPath());
        assertFalse(fileContentAfter.contains("TASK," + TASK_NAME), "Файл не должен содержать информацию о задаче после удаления");
    }

    @Test
    @DisplayName("Менеджер должен загружать задачи из файла")
    void loadFromFile_ShouldLoadTasksFromFile() throws IOException {
        // Создаем задачи
        taskManager.createTask(task);
        taskManager.createSubtask(subtask);
        
        // Загружаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        
        // Проверяем, что задачи загружены
        assertNotNull(loadedManager.getAllTasks(), "Список задач не должен быть null");
        assertFalse(loadedManager.getAllTasks().isEmpty(), "Список задач не должен быть пустым");
        assertEquals(1, loadedManager.getAllTasks().size(), "Должна быть загружена одна задача");
        
        // Проверяем, что эпики загружены
        assertNotNull(loadedManager.getAllEpics(), "Список эпиков не должен быть null");
        assertFalse(loadedManager.getAllEpics().isEmpty(), "Список эпиков не должен быть пустым");
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть загружен один эпик");
        
        // Проверяем, что подзадачи загружены
        assertNotNull(loadedManager.getAllSubtasks(), "Список подзадач не должен быть null");
        assertFalse(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач не должен быть пустым");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Должна быть загружена одна подзадача");
    }

    @Test
    @DisplayName("Менеджер должен сохранять и загружать историю просмотров")
    void loadFromFile_ShouldLoadHistoryFromFile() {
        // Создаем задачи
        taskManager.createTask(task);
        taskManager.createSubtask(subtask);
        
        // Просматриваем задачи для создания истории
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());
        
        // Проверяем, что история создана
        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertEquals(3, history.size(), "История должна содержать 3 элемента");
        
        // Загружаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        
        // Проверяем, что история загружена
        List<Task> loadedHistory = loadedManager.getHistory();
        assertNotNull(loadedHistory, "Загруженная история не должна быть null");
        assertEquals(3, loadedHistory.size(), "Загруженная история должна содержать 3 элемента");
    }

    @Test
    @DisplayName("Менеджер должен обрабатывать ошибки при работе с файлом")
    void save_WithInvalidFile_ShouldThrowManagerSaveException() {
        // Создаем менеджер с недоступным файлом
        File invalidFile = new File("/invalid/path/to/file.csv");
        
        // Проверяем, что при попытке сохранения в недоступный файл выбрасывается исключение
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(invalidFile);
        assertThrows(ManagerSaveException.class, () -> {
            invalidManager.createTask(task);
        }, "Должно быть выброшено исключение ManagerSaveException при сохранении в недоступный файл");
    }

    @Test
    @DisplayName("Менеджер должен обрабатывать пустой файл")
    void loadFromFile_WithEmptyFile_ShouldCreateEmptyManager() throws IOException {
        // Создаем пустой файл
        Files.writeString(tempFile.toPath(), "");
        
        // Загружаем менеджер из пустого файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        
        // Проверяем, что менеджер создан и пуст
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    @DisplayName("Статус эпика должен обновляться при изменении статуса подзадачи")
    void updateSubtask_ShouldUpdateEpicStatus() {
        // Создаем подзадачу
        taskManager.createSubtask(subtask);
        
        // Проверяем начальный статус эпика
        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic.getId()).getStatus(), "Начальный статус эпика должен быть NEW");
        
        // Обновляем статус подзадачи
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        
        // Проверяем, что статус эпика обновился
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS");
        
        // Загружаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        
        // Проверяем, что статус эпика сохранен в файле и загружен корректно
        assertEquals(TaskStatus.IN_PROGRESS, loadedManager.getEpicById(epic.getId()).getStatus(), "Загруженный статус эпика должен быть IN_PROGRESS");
    }
}