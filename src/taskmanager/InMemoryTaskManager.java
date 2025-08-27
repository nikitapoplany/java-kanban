package taskmanager;

import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.TaskStatus;

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
        // Проверяем, не пересекается ли подзадача с существующими задачами
        if (subtask.getStartTime() != null && hasOverlaps(subtask)) {
            throw new IllegalStateException("Подзадача пересекается по времени с уже существующими задачами");
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        // Добавляем подзадачу в отсортированный набор, если у нее есть время начала
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        Epic epic = epics.get(epicId);
        epic.addSubtaskId(id);

        updateEpicStatus(epicId);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        if (subtasks.containsKey(id)) {
            Subtask oldSubtask = subtasks.get(id);
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
            int currentEpicId = oldSubtask.getEpicId();

            int newEpicId = subtask.getEpicId();
            if (currentEpicId != newEpicId) {
                if (!epics.containsKey(newEpicId)) {
                    // Возвращаем старую версию подзадачи в отсортированный набор
                    if (oldSubtask.getStartTime() != null) {
                        prioritizedTasks.add(oldSubtask);
                    }
                    return;
                }

                Epic oldEpic = epics.get(currentEpicId);
                oldEpic.removeSubtaskId(id);
                updateEpicStatus(currentEpicId);

                Epic newEpic = epics.get(newEpicId);
                newEpic.addSubtaskId(id);
            }

            subtasks.put(id, subtask);
            // Добавляем обновленную подзадачу в отсортированный набор, если у нее есть время начала
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }

            updateEpicStatus(newEpicId);
        }
    }

    // Удалить подзадачу по идентификатору
    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            // Удаляем подзадачу из отсортированного набора
            prioritizedTasks.remove(subtask);
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
        // Удаляем все подзадачи из отсортированного набора
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
        }
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(TaskStatus.NEW);
            // Сбрасываем время и продолжительность для эпиков
            epic.setStartTime(null);
            epic.setDuration(java.time.Duration.ZERO);
            epic.setEndTime(null);
        }

        subtasks.clear();
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        if (!epics.containsKey(epicId)) {
            return new ArrayList<>();
        }

        Epic epic = epics.get(epicId);
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .collect(java.util.stream.Collectors.toList());
    }

    private void updateEpicStatus(int epicId) {
        if (!epics.containsKey(epicId)) {
            return;
        }

        Epic epic = epics.get(epicId);
        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            // Сбрасываем время и продолжительность для пустого эпика
            epic.setStartTime(null);
            epic.setDuration(java.time.Duration.ZERO);
            epic.setEndTime(null);
            return;
        }

        // Используем Stream API для проверки статусов подзадач
        boolean allNew = subtaskIds.stream()
                .map(subtasks::get)
                .map(Task::getStatus)
                .allMatch(status -> status == TaskStatus.NEW);
        boolean allDone = subtaskIds.stream()
                .map(subtasks::get)
                .map(Task::getStatus)
                .allMatch(status -> status == TaskStatus.DONE);

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
        
        // Обновляем время и продолжительность эпика
        updateEpicTimeFields(epicId);
    }
    
    /**
     * Обновляет поля времени эпика на основе его подзадач
     * @param epicId идентификатор эпика
     */
    private void updateEpicTimeFields(int epicId) {
        if (!epics.containsKey(epicId)) {
            return;
        }

        Epic epic = epics.get(epicId);
        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(java.time.Duration.ZERO);
            epic.setEndTime(null);
            return;
        }

        // Получаем все подзадачи эпика
        List<Subtask> epicSubtasks = subtaskIds.stream()
                .map(subtasks::get)
                .collect(java.util.stream.Collectors.toList());
        
        // Находим самое раннее время начала среди подзадач с непустым временем начала
        java.util.Optional<java.time.LocalDateTime> earliestStart = epicSubtasks.stream()
                .map(Task::getStartTime)
                .filter(java.util.Objects::nonNull)
                .min(java.time.LocalDateTime::compareTo);
        
        // Находим самое позднее время окончания среди подзадач с непустым временем окончания
        java.util.Optional<java.time.LocalDateTime> latestEnd = epicSubtasks.stream()
                .map(Task::getEndTime)
                .filter(java.util.Objects::nonNull)
                .max(java.time.LocalDateTime::compareTo);
        
        // Суммируем продолжительности всех подзадач
        java.time.Duration totalDuration = epicSubtasks.stream()
                .map(Task::getDuration)
                .reduce(java.time.Duration.ZERO, java.time.Duration::plus);

        // Устанавливаем поля времени эпика
        epic.setStartTime(earliestStart.orElse(null));
        epic.setDuration(totalDuration);
        epic.setEndTime(latestEnd.orElse(null));
    }

    // Получить историю просмотров
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
    
    /**
     * Проверяет, пересекаются ли две задачи по времени выполнения
     * @param task1 первая задача
     * @param task2 вторая задача
     * @return true, если задачи пересекаются, иначе false
     */
    private boolean tasksOverlap(Task task1, Task task2) {
        // Если у какой-то из задач нет времени начала или продолжительности, они не могут пересекаться
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }
        
        // Получаем время начала и окончания для обеих задач
        java.time.LocalDateTime start1 = task1.getStartTime();
        java.time.LocalDateTime end1 = task1.getEndTime();
        java.time.LocalDateTime start2 = task2.getStartTime();
        java.time.LocalDateTime end2 = task2.getEndTime();
        
        // Проверяем пересечение с помощью метода наложения отрезков
        // Задачи пересекаются, если начало одной задачи находится между началом и концом другой задачи
        // или если конец одной задачи находится между началом и концом другой задачи
        return (start1.isBefore(end2) && start2.isBefore(end1));
    }
    
    /**
     * Проверяет, пересекается ли задача с любой другой задачей в списке менеджера
     * @param task задача для проверки
     * @return true, если задача пересекается с какой-либо другой задачей, иначе false
     */
    private boolean hasOverlaps(Task task) {
        // Если у задачи нет времени начала, она не может пересекаться с другими задачами
        if (task.getStartTime() == null) {
            return false;
        }
        
        // Используем Stream API для проверки пересечений
        return getPrioritizedTasks().stream()
                .filter(t -> t.getId() != task.getId()) // Исключаем саму задачу из проверки
                .anyMatch(t -> tasksOverlap(task, t));
    }
}