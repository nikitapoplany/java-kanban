package taskmanager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import taskmanager.exceptions.ManagerSaveException;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Специфические тесты для FileBackedTaskManager, которые не покрываются в TaskManagerTest
 */
class FileBackedTaskManagerSpecificTest extends TaskManagerTest<FileBackedTaskManager> {
    @TempDir
    Path tempDir;
    
    private File file;
    
    /**
     * Создает экземпляр FileBackedTaskManager для тестирования
     * @return экземпляр FileBackedTaskManager
     */
    @Override
    protected FileBackedTaskManager createTaskManager() {
        file = tempDir.resolve("tasks.csv").toFile();
        return new FileBackedTaskManager(file);
    }
    
    @AfterEach
    void tearDown() {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
    
    /**
     * Тест на сохранение и загрузку задач из файла
     */
    @Test
    @DisplayName("Задачи должны сохраняться в файл и загружаться из него")
    void saveAndLoad_WithTasks_ShouldPersistTasksToFile() {
        // Создаем задачи
        Task task = new Task("Test Task", "Test Description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofMinutes(30));
        taskManager.createTask(task);
        
        Epic epic = new Epic("Test Epic", "Test Description");
        taskManager.createEpic(epic);
        
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId());
        subtask.setStartTime(LocalDateTime.now().plusHours(1));
        subtask.setDuration(Duration.ofMinutes(45));
        taskManager.createSubtask(subtask);
        
        // Просматриваем задачи, чтобы они попали в историю
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());
        
        // Принудительно сохраняем состояние менеджера
        // Это нужно, чтобы убедиться, что история просмотров сохранена в файл
        try {
            // Используем рефлексию для вызова метода save
            java.lang.reflect.Method saveMethod = FileBackedTaskManager.class.getDeclaredMethod("save");
            saveMethod.setAccessible(true);
            saveMethod.invoke(taskManager);
        } catch (Exception e) {
            System.out.println("[DEBUG] Ошибка при вызове метода save: " + e.getMessage());
        }
        
        // Загружаем задачи из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        
        // Получаем загруженные задачи
        List<Task> loadedTasks = loadedManager.getAllTasks();
        List<Epic> loadedEpics = loadedManager.getAllEpics();
        List<Subtask> loadedSubtasks = loadedManager.getAllSubtasks();
        
        // Проверяем, что задачи загружены корректно
        assertEquals(1, loadedTasks.size(), "Должна быть загружена одна задача");
        assertEquals(1, loadedEpics.size(), "Должен быть загружен один эпик");
        assertEquals(1, loadedSubtasks.size(), "Должна быть загружена одна подзадача");
        
        Task loadedTask = loadedTasks.get(0);
        Epic loadedEpic = loadedEpics.get(0);
        Subtask loadedSubtask = loadedSubtasks.get(0);
        
        // Проверяем, что имена и описания задач загружены корректно
        assertEquals(task.getName(), loadedTask.getName(), "Имя задачи должно совпадать");
        assertEquals(epic.getName(), loadedEpic.getName(), "Имя эпика должно совпадать");
        assertEquals(subtask.getName(), loadedSubtask.getName(), "Имя подзадачи должно совпадать");
        
        // Проверяем, что время и продолжительность загружены корректно
        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время начала задачи должно совпадать");
        assertEquals(task.getDuration(), loadedTask.getDuration(), "Продолжительность задачи должна совпадать");
        assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime(), "Время начала подзадачи должно совпадать");
        assertEquals(subtask.getDuration(), loadedSubtask.getDuration(), "Продолжительность подзадачи должна совпадать");
        
        // Проверяем, что история загружена корректно
        List<Task> history = loadedManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 элемента");
        
        // Проверяем, что в истории есть задачи с правильными именами
        assertTrue(history.stream().anyMatch(t -> t.getName().equals(task.getName())), "В истории должна быть задача с именем " + task.getName());
        assertTrue(history.stream().anyMatch(t -> t.getName().equals(epic.getName())), "В истории должен быть эпик с именем " + epic.getName());
        assertTrue(history.stream().anyMatch(t -> t.getName().equals(subtask.getName())), "В истории должна быть подзадача с именем " + subtask.getName());
    }
    
    /**
     * Тест на обработку исключений при сохранении в недоступный файл
     */
    @Test
    @DisplayName("Должно выбрасываться исключение при сохранении в недоступный файл")
    void save_WithInvalidFile_ShouldThrowManagerSaveException() {
        // Создаем файл в недоступной директории
        File invalidFile = new File("/invalid/path/tasks.csv");
        
        // Создаем менеджер с недоступным файлом
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(invalidFile);
        
        // Создаем задачу
        Task task = new Task("Test Task", "Test Description");
        
        // Проверяем, что при попытке сохранения выбрасывается исключение
        assertThrows(ManagerSaveException.class, () -> {
            invalidManager.createTask(task);
        });
    }
    
    /**
     * Тест на загрузку из пустого файла
     */
    @Test
    @DisplayName("Загрузка из пустого файла должна создавать пустой менеджер")
    void loadFromFile_WithEmptyFile_ShouldCreateEmptyManager() throws IOException {
        // Создаем пустой файл
        File emptyFile = tempDir.resolve("empty.csv").toFile();
        Files.writeString(emptyFile.toPath(), "id,type,name,status,description,epic,duration,startTime\n");
        
        // Загружаем менеджер из пустого файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(emptyFile);
        
        // Проверяем, что менеджер пустой
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
    }
}