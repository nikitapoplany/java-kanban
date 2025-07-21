package taskmanager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

/**
 * Интерфейс менеджера для управления задачами, эпиками и подзадачами
 */
public interface TaskManager {
    // Методы для работы с задачами
    List<Task> getAllTasks();
    Task getTaskById(int id);

    void createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(int id);

    void deleteAllTasks();

    // Методы для работы с эпиками
    List<Epic> getAllEpics();

    Epic getEpicById(int id);

    void createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicById(int id);

    void deleteAllEpics();

    // Методы для работы с подзадачами
    List<Subtask> getAllSubtasks();
    Subtask getSubtaskById(int id);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    void deleteAllSubtasks();

    List<Subtask> getSubtasksByEpicId(int epicId);

    // Метод для получения истории просмотров
    List<Task> getHistory();
}
