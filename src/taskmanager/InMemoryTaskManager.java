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
 * Класс менеджера для управления задачами, эпиками и подзадачами в памяти
 */
public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private int nextId = 1;
    private final HistoryManager historyManager;

    /**
     * Конструктор для создания нового InMemoryTaskManager
     */
    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = new InMemoryHistoryManager();
    }

    /**
     * Конструктор для создания нового InMemoryTaskManager с указанным HistoryManager
     */
    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
    }

    // Сгенерировать новый уникальный идентификатор для задачи
    private int generateId() {
        return nextId++;
    }

    // Получить все задачи
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Получить задачу по идентификатору
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Создать новую задачу
    @Override
    public void createTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
    }

    // Обновить существующую задачу
    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, task);
        }
    }

    // Удалить задачу по идентификатору
    @Override
    public void deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            historyManager.remove(id);
        }
    }

    // Удалить все задачи
    @Override
    public void deleteAllTasks() {
        // Удаляем все задачи из истории просмотров перед очисткой
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    // Получить все эпики
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Получить эпик по идентификатору
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    // Создать новый эпик
    @Override
    public void createEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
    }

    // Обновить существующий эпик
    @Override
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
    @Override
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            // Удалить все подзадачи этого эпика
            Epic epic = epics.get(id);
            List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            for (Integer subtaskId : subtaskIds) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId); // Удаляем подзадачу из истории просмотров
            }

            // Удалить эпик
            epics.remove(id);
            historyManager.remove(id); // Удаляем эпик из истории просмотров
        }
    }

    // Удалить все эпики и их подзадачи
    @Override
    public void deleteAllEpics() {
        // Удаляем все подзадачи из истории просмотров перед очисткой
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        
        // Удаляем все эпики из истории просмотров перед очисткой
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        
        // Удалить все подзадачи
        subtasks.clear();

        // Удалить все эпики
        epics.clear();
    }

    // Получить все подзадачи
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получить подзадачу по идентификатору
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
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

    @Override
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
    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            epic.removeSubtaskId(id);

            subtasks.remove(id);
            historyManager.remove(id); // Удаляем подзадачу из истории просмотров

            updateEpicStatus(epicId);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        // Удаляем все подзадачи из истории просмотров перед очисткой
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(TaskStatus.NEW);
        }

        subtasks.clear();
    }

    @Override
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

    // Получить историю просмотров
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}