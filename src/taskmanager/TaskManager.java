package taskmanager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс менеджера для управления задачами, эпиками и подзадачами
 */
public class TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    /**
     * Конструктор для создания нового TaskManager
     */
    public TaskManager() {
    }

    // Сгенерировать новый уникальный идентификатор для задачи
    private int generateId() {
        return nextId++;
    }

    // Получить все задачи
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }


    // Получить задачу по идентификатору
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    // Создать новую задачу
    public void createTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
    }

    // Обновить существующую задачу
    public void updateTask(Task task) {
        int id = task.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, task);
        }
    }

    // Удалить задачу по идентификатору
    public void deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        }
    }

    // Удалить все задачи
    public void deleteAllTasks() {
        tasks.clear();
    }

    // Получить все эпики
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Получить эпик по идентификатору
    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    // Создать новый эпик
    public void createEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
    }

    // Обновить существующий эпик
    public void updateEpic(Epic epic) {
        int id = epic.getId();
        if (epics.containsKey(id)) {
            // Сохранить подзадачи
            List<Integer> subtaskIds = epics.get(id).getSubtaskIds();
            epic.clearSubtasks();
            for (Integer subtaskId : subtaskIds) {
                epic.addSubtaskId(subtaskId);
            }

            // Обновить эпик
            epics.put(id, epic);

            // Обновить статус эпика на основе его подзадач
            updateEpicStatus(id);
        }
    }

    // Удалить эпик по идентификатору
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            // Удалить все подзадачи этого эпика
            Epic epic = epics.get(id);
            List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            for (Integer subtaskId : subtaskIds) {
                subtasks.remove(subtaskId);
            }

            // Удалить эпик
            epics.remove(id);
        }
    }

    // Удалить все эпики и их подзадачи
    public void deleteAllEpics() {
        // Очистить списки подзадач у каждого эпика
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
        }

        // Удалить все подзадачи
        subtasks.clear();

        // Удалить все эпики
        epics.clear();
    }

    // Получить все подзадачи
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }


    // Получить подзадачу по идентификатору
    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            return;
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        Epic epic = epics.get(epicId);
        epic.addSubtaskId(id);

        updateEpicStatus(epicId);
    }

    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        if (subtasks.containsKey(id)) {
            int currentEpicId = subtasks.get(id).getEpicId();

            int newEpicId = subtask.getEpicId();
            if (currentEpicId != newEpicId) {
                if (!epics.containsKey(newEpicId)) {
                    return;
                }

                Epic oldEpic = epics.get(currentEpicId);
                oldEpic.removeSubtaskId(id);
                updateEpicStatus(currentEpicId);

                Epic newEpic = epics.get(newEpicId);
                newEpic.addSubtaskId(id);
            }

            subtasks.put(id, subtask);

            updateEpicStatus(newEpicId);
        }
    }

    // Удалить подзадачу по идентификатору
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            epic.removeSubtaskId(id);

            subtasks.remove(id);

            updateEpicStatus(epicId);
        }
    }

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(TaskStatus.NEW);
        }

        subtasks.clear();
    }

    public List<Subtask> getSubtasksByEpicId(int epicId) {
        if (!epics.containsKey(epicId)) {
            return new ArrayList<>();
        }

        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            result.add(subtasks.get(subtaskId));
        }

        return result;
    }

    // Получить список идентификаторов подзадач для эпика
    public List<Integer> getSubtaskIdsByEpicId(int epicId) {
        if (!epics.containsKey(epicId)) {
            return new ArrayList<>();
        }

        Epic epic = epics.get(epicId);
        return new ArrayList<>(epic.getSubtaskIds());
    }

    private void updateEpicStatus(int epicId) {
        if (!epics.containsKey(epicId)) {
            return;
        }

        Epic epic = epics.get(epicId);
        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            TaskStatus status = subtask.getStatus();

            if (status != TaskStatus.NEW) {
                allNew = false;
            }

            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
