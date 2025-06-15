package taskmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс менеджера для управления задачами, эпиками и подзадачами
 */
public class TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private int nextId = 1;

    /**
     * Конструктор для создания нового TaskManager
     */
    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
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
    public Task createTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    // Обновить существующую задачу
    public boolean updateTask(Task task) {
        int id = task.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, task);
            return true;
        }
        return false;
    }

    // Удалить задачу по идентификатору
    public boolean deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            return true;
        }
        return false;
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
    public Epic createEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }

    // Обновить существующий эпик
    public boolean updateEpic(Epic epic) {
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

            return true;
        }
        return false;
    }

    // Удалить эпик по идентификатору
    public boolean deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            // Удалить все подзадачи этого эпика
            Epic epic = epics.get(id);
            List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            for (Integer subtaskId : subtaskIds) {
                subtasks.remove(subtaskId);
            }

            // Удалить эпик
            epics.remove(id);
            return true;
        }
        return false;
    }

    // Удалить все эпики и их подзадачи
    public void deleteAllEpics() {
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

    public Subtask createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            return null;
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        Epic epic = epics.get(epicId);
        epic.addSubtaskId(id);

        updateEpicStatus(epicId);

        return subtask;
    }

    public boolean updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        if (subtasks.containsKey(id)) {
            int currentEpicId = subtasks.get(id).getEpicId();

            int newEpicId = subtask.getEpicId();
            if (currentEpicId != newEpicId) {
                if (!epics.containsKey(newEpicId)) {
                    return false;
                }

                Epic oldEpic = epics.get(currentEpicId);
                oldEpic.removeSubtaskId(id);
                updateEpicStatus(currentEpicId);

                Epic newEpic = epics.get(newEpicId);
                newEpic.addSubtaskId(id);
            }

            subtasks.put(id, subtask);

            updateEpicStatus(newEpicId);

            return true;
        }
        return false;
    }

    // Удалить подзадачу по идентификатору
    public boolean deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            epic.removeSubtaskId(id);

            subtasks.remove(id);

            updateEpicStatus(epicId);

            return true;
        }
        return false;
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
