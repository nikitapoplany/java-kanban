package taskmanager.service.impl;

import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.TaskStatus;
import taskmanager.service.HistoryManager;
import taskmanager.service.TaskManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Класс менеджера для управления задачами, эпиками и подзадачами в памяти
 */
public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private int nextId = 1;
    private final HistoryManager historyManager;
    // Отсортированный набор задач и подзадач по времени начала
    private final Set<Task> prioritizedTasks;

    /**
     * Конструктор для создания нового InMemoryTaskManager
     */
    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = new InMemoryHistoryManager();
        // Инициализация отсортированного набора задач
        prioritizedTasks = new TreeSet<>(Comparator.comparing(
                task -> task.getStartTime(), // Сортировка по времени начала
                Comparator.nullsLast(Comparator.naturalOrder()) // Задачи без времени начала в конце
        ));
    }

    /**
     * Конструктор для создания нового InMemoryTaskManager с указанным HistoryManager
     */
    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
        // Инициализация отсортированного набора задач
        prioritizedTasks = new TreeSet<>(Comparator.comparing(
                task -> task.getStartTime(), // Сортировка по времени начала
                Comparator.nullsLast(Comparator.naturalOrder()) // Задачи без времени начала в конце
        ));
    }
    /**
     * Получить список задач и подзадач, отсортированных по времени начала
     * Задачи без времени начала не включаются в список
     * @return отсортированный список задач и подзадач
     */

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Сгенерировать новый уникальный идентификатор для задачи
    private int generateId() {
        return nextId++;
    }

    // Обновить счетчик nextId, если переданное значение больше текущего
    protected void updateNextId(int id) {
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    // Добавить задачу с предопределенным ID
    protected void addTaskWithId(Task task) {
        int id = task.getId();
        // Обновляем nextId, если нужно
        updateNextId(id);
        tasks.put(id, task);
        // Добавляем задачу в отсортированный набор, если у нее есть время начала
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    // Добавить эпик с предопределенным ID
    protected void addEpicWithId(Epic epic) {
        int id = epic.getId();
        // Обновляем nextId, если нужно
        updateNextId(id);
        epics.put(id, epic);
    }

    // Добавить подзадачу с предопределенным ID
    protected void addSubtaskWithId(Subtask subtask) {
        int id = subtask.getId();
        int epicId = subtask.getEpicId();
        // Обновляем nextId, если нужно
        updateNextId(id);

        // Проверяем, существует ли эпик
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        subtasks.put(id, subtask);
        epic.addSubtaskId(id);

        // Добавляем подзадачу в отсортированный набор, если у нее есть время начала
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        // Обновляем статус и временные поля эпика
        updateEpicStatus(epicId);
        updateEpicTimeFields(epicId);
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
        // Проверяем, не пересекается ли задача с существующими задачами
        if (task.getStartTime() != null && hasOverlaps(task)) {
            throw new IllegalStateException("Задача пересекается по времени с уже существующими задачами");
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        // Добавляем задачу в отсортированный набор, если у нее есть время начала
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    // Обновить существующую задачу
    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        if (tasks.containsKey(id)) {
            Task oldTask = tasks.get(id);
            // Удаляем старую версию задачи из отсортированного набора
            prioritizedTasks.remove(oldTask);
            // Проверяем, не пересекается ли обновленная задача с существующими задачами
            if (task.getStartTime() != null && hasOverlaps(task)) {
                // Возвращаем старую версию задачи в отсортированный набор
                if (oldTask.getStartTime() != null) {
                    prioritizedTasks.add(oldTask);
                }
                throw new IllegalStateException("Задача пересекается по времени с уже существующими задачами");
            }
            // Обновляем задачу
            tasks.put(id, task);
            // Добавляем обновленную задачу в отсортированный набор, если у нее есть время начала
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        }
    }

    // Удалить задачу по идентификатору
    @Override
    public void deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            // Удаляем задачу из отсортированного набора
            prioritizedTasks.remove(task);
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
        // Удаляем все задачи из отсортированного набора
        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
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
            // Сохраняем список подзадач из старого эпика
            Epic oldEpic = epics.get(id);
            List<Integer> subtaskIds = oldEpic.getSubtaskIds();

            // Обновляем эпик, но сохраняем его подзадачи
            for (Integer subtaskId : subtaskIds) {
                epic.addSubtaskId(subtaskId);
            }

            epics.put(id, epic);

            // Обновляем статус эпика на основе статусов его подзадач
            updateEpicStatus(id);
            // Обновляем временные поля эпика на основе его подзадач
            updateEpicTimeFields(id);
        }
    }

    // Удалить эпик по идентификатору
    @Override
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);

            // Удаляем все подзадачи этого эпика
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    // Удаляем подзадачу из отсортированного набора
                    prioritizedTasks.remove(subtask);
                }
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }

            epics.remove(id);
            historyManager.remove(id);
        }
    }

    // Удалить все эпики
    @Override
    public void deleteAllEpics() {
        // Удаляем все эпики и их подзадачи из истории просмотров перед очисткой
        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);

            Epic epic = epics.get(epicId);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                historyManager.remove(subtaskId);
            }
        }

        // Удаляем все подзадачи из отсортированного набора
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
        }

        // Очищаем коллекции эпиков и подзадач
        epics.clear();
        subtasks.clear();
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

    // Создать новую подзадачу
    @Override
    public void createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        // Проверяем, существует ли эпик
        if (epic == null) {
            return;
        }

        // Проверяем, не пересекается ли подзадача с существующими задачами
        if (subtask.getStartTime() != null && hasOverlaps(subtask)) {
            throw new IllegalStateException("Подзадача пересекается по времени с уже существующими задачами");
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        // Добавляем подзадачу в эпик
        epic.addSubtaskId(id);

        // Добавляем подзадачу в отсортированный набор, если у нее есть время начала
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        // Обновляем статус эпика
        updateEpicStatus(epicId);
        // Обновляем временные поля эпика
        updateEpicTimeFields(epicId);
    }

    // Обновить существующую подзадачу
    @Override
    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        if (subtasks.containsKey(id)) {
            Subtask oldSubtask = subtasks.get(id);
            int epicId = subtask.getEpicId();

            // Проверяем, существует ли эпик
            Epic epic = epics.get(epicId);
            if (epic == null) {
                return;
            }

            // Удаляем старую версию подзадачи из отсортированного набора
            prioritizedTasks.remove(oldSubtask);

            // Проверяем, не пересекается ли обновленная подзадача с существующими задачами
            if (subtask.getStartTime() != null && hasOverlaps(subtask)) {
                // Возвращаем старую версию подзадачи в отсортированный набор
                if (oldSubtask.getStartTime() != null) {
                    prioritizedTasks.add(oldSubtask);
                }
                throw new IllegalStateException("Подзадача пересекается по времени с уже существующими задачами");
            }

            // Если изменился эпик, обновляем связи
            int oldEpicId = oldSubtask.getEpicId();
            if (oldEpicId != epicId) {
                Epic oldEpic = epics.get(oldEpicId);
                if (oldEpic != null) {
                    oldEpic.removeSubtaskId(id);
                    updateEpicStatus(oldEpicId);
                    updateEpicTimeFields(oldEpicId);
                }
                epic.addSubtaskId(id);
            }

            // Обновляем подзадачу
            subtasks.put(id, subtask);

            // Добавляем обновленную подзадачу в отсортированный набор, если у нее есть время начала
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }

            // Обновляем статус эпика
            updateEpicStatus(epicId);
            // Обновляем временные поля эпика
            updateEpicTimeFields(epicId);
        }
    }

    // Удалить подзадачу по идентификатору
    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            int epicId = subtask.getEpicId();

            // Удаляем подзадачу из отсортированного набора
            prioritizedTasks.remove(subtask);

            // Удаляем подзадачу
            subtasks.remove(id);
            historyManager.remove(id);

            // Удаляем подзадачу из эпика и обновляем его статус
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epicId);
                updateEpicTimeFields(epicId);
            }
        }
    }

    // Удалить все подзадачи
    @Override
    public void deleteAllSubtasks() {
        // Удаляем все подзадачи из истории просмотров перед очисткой
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }

        // Удаляем все подзадачи из отсортированного набора
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
        }

        // Очищаем список подзадач
        subtasks.clear();

        // Очищаем списки подзадач у всех эпиков и обновляем их статусы
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
            updateEpicTimeFields(epic.getId());
        }
    }

    // Получить список подзадач для эпика
    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);

        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
        }

        return result;
    }

    // Обновить статус эпика на основе статусов его подзадач
    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();

        // Если у эпика нет подзадач, его статус - NEW
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        // Проверяем статусы всех подзадач
        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            TaskStatus status = subtask.getStatus();

            if (status != TaskStatus.NEW) {
                allNew = false;
            }

            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        // Определяем статус эпика на основе статусов подзадач
        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    // Обновить временные поля эпика на основе его подзадач
    protected void updateEpicTimeFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();

        // Если у эпика нет подзадач, сбрасываем временные поля
        if (subtaskIds.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
            return;
        }

        // Находим минимальное время начала и максимальное время завершения среди подзадач
        java.time.LocalDateTime minStartTime = null;
        java.time.LocalDateTime maxEndTime = null;
        java.time.Duration totalDuration = java.time.Duration.ZERO;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null || subtask.getStartTime() == null) {
                continue;
            }

            java.time.LocalDateTime startTime = subtask.getStartTime();
            java.time.LocalDateTime endTime = subtask.getEndTime();
            java.time.Duration duration = subtask.getDuration();

            // Обновляем минимальное время начала
            if (minStartTime == null || startTime.isBefore(minStartTime)) {
                minStartTime = startTime;
            }

            // Обновляем максимальное время завершения
            if (maxEndTime == null || endTime.isAfter(maxEndTime)) {
                maxEndTime = endTime;
            }

            // Суммируем продолжительности подзадач
            totalDuration = totalDuration.plus(duration);
        }

        // Устанавливаем временные поля эпика
        epic.setStartTime(minStartTime);
        epic.setDuration(totalDuration);
        epic.setEndTime(maxEndTime);
    }

    // Получить историю просмотров
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Проверить, пересекаются ли две задачи по времени
    protected boolean tasksOverlap(Task task1, Task task2) {
        // Если у одной из задач нет времени начала, они не пересекаются
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }

        // Если это одна и та же задача, она не пересекается сама с собой
        if (task1.getId() == task2.getId()) {
            return false;
        }

        // Проверяем, пересекаются ли интервалы времени задач
        java.time.LocalDateTime start1 = task1.getStartTime();
        java.time.LocalDateTime end1 = task1.getEndTime();
        java.time.LocalDateTime start2 = task2.getStartTime();
        java.time.LocalDateTime end2 = task2.getEndTime();

        // Задачи пересекаются, если:
        // - начало одной задачи находится внутри интервала другой задачи, или
        // - конец одной задачи находится внутри интервала другой задачи
        return (start1.isBefore(end2) && start2.isBefore(end1));
    }

    // Проверить, пересекается ли задача с другими задачами
    protected boolean hasOverlaps(Task task) {
        // Если у задачи нет времени начала, она не может пересекаться с другими задачами
        if (task.getStartTime() == null) {
            return false;
        }

        // Проверяем пересечения с обычными задачами
        for (Task otherTask : tasks.values()) {
            if (tasksOverlap(task, otherTask)) {
                return true;
            }
        }

        // Проверяем пересечения с подзадачами
        for (Subtask otherSubtask : subtasks.values()) {
            if (tasksOverlap(task, otherSubtask)) {
                return true;
            }
        }

        return false;
    }
}