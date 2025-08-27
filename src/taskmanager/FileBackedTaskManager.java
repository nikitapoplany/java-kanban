package taskmanager;

import taskmanager.exceptions.ManagerSaveException;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.TaskStatus;
import taskmanager.model.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация менеджера задач с автосохранением в файл
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    /**
     * Конструктор для создания нового FileBackedTaskManager
     * @param file файл для сохранения данных
     */
    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    /**
     * Конструктор для создания нового FileBackedTaskManager с указанным HistoryManager
     * @param file файл для сохранения данных
     * @param historyManager менеджер истории просмотров
     */
    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
    }

    /**
     * Сохранить текущее состояние менеджера в файл
     */
    protected void save() {
        try {
            StringBuilder sb = new StringBuilder();

            // Добавляем заголовок
            sb.append("id,type,name,status,description,epic,duration,startTime\n");

            // Сохраняем задачи
            List<Task> tasks = getAllTasks();
            System.out.println("[DEBUG] Сохраняем " + tasks.size() + " задач");
            for (Task task : tasks) {
                String taskStr = toString(task);
                sb.append(taskStr).append("\n");
            }

            // Сохраняем эпики
            List<Epic> epics = getAllEpics();
            for (Epic epic : epics) {
                String epicStr = toString(epic);
                sb.append(epicStr).append("\n");
            }

            // Сохраняем подзадачи
            List<Subtask> subtasks = getAllSubtasks();
            for (Subtask subtask : subtasks) {
                String subtaskStr = toString(subtask);
                sb.append(subtaskStr).append("\n");
            }

            // Добавляем пустую строку перед историей
            sb.append("\n");

            // Сохраняем историю просмотров
            List<Task> history = getHistory();
            System.out.println("[DEBUG] Сохраняем историю просмотров, размер: " + history.size());
            if (!history.isEmpty()) {
                List<String> historyIds = new ArrayList<>();
                for (Task task : history) {
                    historyIds.add(String.valueOf(task.getId()));
                }
                String historyStr = String.join(",", historyIds);
                System.out.println("[DEBUG] Строка истории: " + historyStr);
                sb.append(historyStr);
            }

            String content = sb.toString();
            Files.writeString(file.toPath(), content);
            System.out.println("[DEBUG] Файл сохранен: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + file.getName(), e);
        }
    }

    /**
     * Преобразовать задачу в строку для сохранения в файл
     * @param task задача для преобразования
     * @return строка, представляющая задачу
     */
    private String toString(Task task) {
        TaskType type;
        String epicId = "";

        if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicId());
        } else {
            type = TaskType.TASK;
        }
        
        // Преобразуем продолжительность в минуты
        String durationMinutes = task.getDuration() != null ? 
                String.valueOf(task.getDuration().toMinutes()) : "";
        
        // Форматируем время начала
        String startTimeStr = task.getStartTime() != null ? 
                task.getStartTime().toString() : "";

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                durationMinutes,
                startTimeStr);
    }

    /**
     * Создать задачу из строки
     * @param value строка, представляющая задачу
     * @return созданная задача
     */
    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        
        // Парсим продолжительность и время начала, если они есть
        java.time.Duration duration = null;
        java.time.LocalDateTime startTime = null;
        
        // Проверяем, есть ли поля продолжительности и времени начала
        if (parts.length > 6 && !parts[6].isEmpty()) {
            long minutes = Long.parseLong(parts[6]);
            duration = java.time.Duration.ofMinutes(minutes);
        }
        
        if (parts.length > 7 && !parts[7].isEmpty()) {
            startTime = java.time.LocalDateTime.parse(parts[7]);
        }

        // Switch используется только для выбора типа задачи, а создание делегируется специализированным методам
        switch (type) {
            case TASK:
                return createTaskFromParts(name, description, id, status, duration, startTime);
            case EPIC:
                return createEpicFromParts(name, description, id, status, duration, startTime);
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                return createSubtaskFromParts(name, description, id, status, epicId, duration, startTime);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    /**
     * Создать обычную задачу из компонентов
     */
    private Task createTaskFromParts(String name, String description, int id, TaskStatus status, 
                                    java.time.Duration duration, java.time.LocalDateTime startTime) {
        Task task = new Task(name, description, id, status);
        if (duration != null) {
            task.setDuration(duration);
        }
        if (startTime != null) {
            task.setStartTime(startTime);
        }
        return task;
    }

    /**
     * Создать эпик из компонентов
     */
    private Epic createEpicFromParts(String name, String description, int id, TaskStatus status,
                                    java.time.Duration duration, java.time.LocalDateTime startTime) {
        Epic epic = new Epic(name, description, id, status);
        if (duration != null) {
            epic.setDuration(duration);
        }
        if (startTime != null) {
            epic.setStartTime(startTime);
        }
        return epic;
    }

    /**
     * Создать подзадачу из компонентов
     */
    private Subtask createSubtaskFromParts(String name, String description, int id, TaskStatus status, int epicId,
                                          java.time.Duration duration, java.time.LocalDateTime startTime) {
        Subtask subtask = new Subtask(name, description, id, status, epicId);
        if (duration != null) {
            subtask.setDuration(duration);
        }
        if (startTime != null) {
            subtask.setStartTime(startTime);
        }
        return subtask;
    }

    /**
     * Чтение содержимого файла
     * @param file файл для чтения
     * @return массив строк из файла
     * @throws IOException если произошла ошибка при чтении файла
     */
    private static String[] readFileLines(File file) throws IOException {
        System.out.println("[DEBUG] Загружаем файл: " + file.getAbsolutePath());
        String content = Files.readString(file.toPath());
        return content.split("\n");
    }

    /**
     * Парсинг задач из строк файла
     * @param manager менеджер задач для парсинга
     * @param lines строки из файла
     * @return объект, содержащий распарсенные задачи и индекс последней строки
     */
    private static TaskParsingResult parseTasksFromLines(FileBackedTaskManager manager, String[] lines) {
        int lineIndex = 1; // Пропускаем заголовок

        // Создаем временные списки для задач, эпиков и подзадач
        Map<Integer, Task> tasksMap = new HashMap<>();
        Map<Integer, Epic> epicsMap = new HashMap<>();
        Map<Integer, Subtask> subtasksMap = new HashMap<>();

        // Сначала загружаем все задачи из файла
        System.out.println("[DEBUG] Начинаем загрузку задач из файла");
        while (lineIndex < lines.length && !lines[lineIndex].isBlank()) {
            String line = lines[lineIndex];
            Task task = manager.fromString(line);

            if (task instanceof Epic) {
                epicsMap.put(task.getId(), (Epic) task);
            } else if (task instanceof Subtask) {
                subtasksMap.put(task.getId(), (Subtask) task);
            } else {
                tasksMap.put(task.getId(), task);
            }

            lineIndex++;
        }

        System.out.println("[DEBUG] Загружено задач: " + tasksMap.size());
        System.out.println("[DEBUG] Загружено эпиков: " + epicsMap.size());
        System.out.println("[DEBUG] Загружено подзадач: " + subtasksMap.size());

        return new TaskParsingResult(tasksMap, epicsMap, subtasksMap, lineIndex);
    }

    /**
     * Восстановление связей между эпиками и подзадачами
     * @param epicsMap карта эпиков
     * @param subtasksMap карта подзадач
     */
    private static void restoreEpicSubtaskLinks(Map<Integer, Epic> epicsMap, Map<Integer, Subtask> subtasksMap) {
        for (Subtask subtask : subtasksMap.values()) {
            Epic epic = epicsMap.get(subtask.getEpicId());
            if (epic != null) {
                epic.addSubtaskId(subtask.getId());
            }
        }
    }

    /**
     * Создание менеджера с отключенным автосохранением
     * @param file файл для сохранения данных
     * @return менеджер с отключенным автосохранением
     */
    private static FileBackedTaskManager createManagerWithDisabledAutoSave(File file) {
        System.out.println("[DEBUG] Создаем новый менеджер с загруженными данными");
        return new FileBackedTaskManager(file) {
            @Override
            protected void save() {
                // Отключаем автосохранение при загрузке
                System.out.println("[DEBUG] Автосохранение отключено при загрузке");
            }
        };
    }

    /**
     * Добавление задач в менеджер с отображением старых ID на новые
     * @param loadedManager менеджер для добавления задач
     * @param tasksMap карта обычных задач
     * @param epicsMap карта эпиков
     * @param subtasksMap карта подзадач
     * @return объект, содержащий карты соответствия старых и новых ID
     */
    private static IdMappingResult addTasksToManager(
            FileBackedTaskManager loadedManager,
            Map<Integer, Task> tasksMap,
            Map<Integer, Epic> epicsMap,
            Map<Integer, Subtask> subtasksMap) {

        // Создаем карты для отслеживания соответствия старых и новых ID
        Map<Integer, Integer> oldToNewTaskIds = new HashMap<>();
        Map<Integer, Integer> oldToNewEpicIds = new HashMap<>();
        Map<Integer, Integer> oldToNewSubtaskIds = new HashMap<>();

        // Добавляем задачи в менеджер в правильном порядке
        // Сначала эпики
        for (Epic epic : epicsMap.values()) {
            int oldId = epic.getId();

            // Создаем новый эпик с тем же именем и описанием
            Epic newEpic = new Epic(epic.getName(), epic.getDescription());
            
            // Копируем время начала и продолжительность
            newEpic.setStartTime(epic.getStartTime());
            newEpic.setDuration(epic.getDuration());
            newEpic.setEndTime(epic.getEndTime());
            
            loadedManager.createEpic(newEpic);
            int newId = newEpic.getId();

            // Запоминаем соответствие старого и нового ID
            oldToNewEpicIds.put(oldId, newId);
        }

        // Затем подзадачи
        for (Subtask subtask : subtasksMap.values()) {
            int oldId = subtask.getId();
            int oldEpicId = subtask.getEpicId();

            // Получаем новый ID эпика
            Integer newEpicId = oldToNewEpicIds.get(oldEpicId);
            if (newEpicId == null) {
                continue;
            }

            // Создаем новую подзадачу с тем же именем и описанием, но с новым ID эпика
            Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(), newEpicId);
            
            // Копируем время начала и продолжительность
            newSubtask.setStartTime(subtask.getStartTime());
            newSubtask.setDuration(subtask.getDuration());
            
            loadedManager.createSubtask(newSubtask);
            int newId = newSubtask.getId();

            // Запоминаем соответствие старого и нового ID
            oldToNewSubtaskIds.put(oldId, newId);
        }

        // И наконец обычные задачи
        for (Task task : tasksMap.values()) {
            int oldId = task.getId();

            // Создаем новую задачу с тем же именем, описанием и статусом
            Task newTask = new Task(task.getName(), task.getDescription(), task.getStatus());
            
            // Копируем время начала и продолжительность
            newTask.setStartTime(task.getStartTime());
            newTask.setDuration(task.getDuration());
            
            loadedManager.createTask(newTask);
            int newId = newTask.getId();

            // Запоминаем соответствие старого и нового ID
            oldToNewTaskIds.put(oldId, newId);
        }

        return new IdMappingResult(oldToNewTaskIds, oldToNewEpicIds, oldToNewSubtaskIds);
    }

    /**
     * Восстановление истории просмотров
     * @param loadedManager менеджер для восстановления истории
     * @param lines строки из файла
     * @param lineIndex индекс строки с историей
     * @param idMappingResult карты соответствия старых и новых ID
     */
    private static void restoreHistory(
            FileBackedTaskManager loadedManager,
            String[] lines,
            int lineIndex,
            IdMappingResult idMappingResult) {

        System.out.println("[DEBUG] Восстанавливаем историю, lineIndex: " + lineIndex + ", lines.length: " + lines.length);

        if (lineIndex >= lines.length - 1) {
            System.out.println("[DEBUG] Нет строки с историей");
            return;
        }

        lineIndex++; // Пропускаем пустую строку
        System.out.println("[DEBUG] Новый lineIndex после пропуска пустой строки: " + lineIndex);

        if (lineIndex < lines.length && !lines[lineIndex].isBlank()) {
            String historyLine = lines[lineIndex];
            System.out.println("[DEBUG] Загружаем историю просмотров: " + historyLine);
            String[] historyIds = historyLine.split(",");
            System.out.println("[DEBUG] Количество ID в истории: " + historyIds.length);

            // Добавляем задачи в историю в правильном порядке
            for (String idStr : historyIds) {
                int oldId = Integer.parseInt(idStr);
                System.out.println("[DEBUG] Обрабатываем ID из истории: " + oldId);

                // Проверяем, есть ли задача с таким ID в карте задач
                Integer newTaskId = idMappingResult.oldToNewTaskIds.get(oldId);
                if (newTaskId != null) {
                    System.out.println("[DEBUG] Найден новый ID задачи: " + newTaskId);
                    Task task = loadedManager.getTaskById(newTaskId);
                    System.out.println("[DEBUG] Задача добавлена в историю: " + (task != null));
                    continue;
                }

                // Проверяем, есть ли эпик с таким ID в карте эпиков
                Integer newEpicId = idMappingResult.oldToNewEpicIds.get(oldId);
                if (newEpicId != null) {
                    System.out.println("[DEBUG] Найден новый ID эпика: " + newEpicId);
                    Epic epic = loadedManager.getEpicById(newEpicId);
                    System.out.println("[DEBUG] Эпик добавлен в историю: " + (epic != null));
                    continue;
                }

                // Проверяем, есть ли подзадача с таким ID в карте подзадач
                Integer newSubtaskId = idMappingResult.oldToNewSubtaskIds.get(oldId);
                if (newSubtaskId != null) {
                    System.out.println("[DEBUG] Найден новый ID подзадачи: " + newSubtaskId);
                    Subtask subtask = loadedManager.getSubtaskById(newSubtaskId);
                    System.out.println("[DEBUG] Подзадача добавлена в историю: " + (subtask != null));
                    continue;
                }

                System.out.println("[DEBUG] Не найдено соответствие для ID: " + oldId);
            }
        } else {
            System.out.println("[DEBUG] Строка с историей пуста или отсутствует");
        }

        // Проверяем историю
        System.out.println("[DEBUG] Проверяем историю");
        System.out.println("[DEBUG] Размер истории: " + loadedManager.getHistory().size());
    }

    /**
     * Вспомогательный класс для хранения результатов парсинга задач
     */
    private static class TaskParsingResult {
        final Map<Integer, Task> tasksMap;
        final Map<Integer, Epic> epicsMap;
        final Map<Integer, Subtask> subtasksMap;
        final int lineIndex;

        TaskParsingResult(Map<Integer, Task> tasksMap, Map<Integer, Epic> epicsMap,
                         Map<Integer, Subtask> subtasksMap, int lineIndex) {
            this.tasksMap = tasksMap;
            this.epicsMap = epicsMap;
            this.subtasksMap = subtasksMap;
            this.lineIndex = lineIndex;
        }
    }

    /**
     * Вспомогательный класс для хранения карт соответствия старых и новых ID
     */
    private static class IdMappingResult {
        final Map<Integer, Integer> oldToNewTaskIds;
        final Map<Integer, Integer> oldToNewEpicIds;
        final Map<Integer, Integer> oldToNewSubtaskIds;

        IdMappingResult(Map<Integer, Integer> oldToNewTaskIds, Map<Integer, Integer> oldToNewEpicIds,
                       Map<Integer, Integer> oldToNewSubtaskIds) {
            this.oldToNewTaskIds = oldToNewTaskIds;
            this.oldToNewEpicIds = oldToNewEpicIds;
            this.oldToNewSubtaskIds = oldToNewSubtaskIds;
        }
    }

    /**
     * Загрузить менеджер задач из файла
     * @param file файл для загрузки данных
     * @return загруженный менеджер задач
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String[] lines = readFileLines(file);

            if (lines.length <= 1) {
                System.out.println("[DEBUG] Файл пустой или содержит только заголовок");
                return manager; // Файл пустой или содержит только заголовок
            }

            // Парсим задачи из строк файла
            TaskParsingResult parsingResult = parseTasksFromLines(manager, lines);

            // Восстанавливаем связи между эпиками и подзадачами
            restoreEpicSubtaskLinks(parsingResult.epicsMap, parsingResult.subtasksMap);

            // Создаем новый менеджер с отключенным автосохранением
            FileBackedTaskManager loadedManager = createManagerWithDisabledAutoSave(file);

            // Добавляем задачи в менеджер
            IdMappingResult idMappingResult = addTasksToManager(
                    loadedManager,
                    parsingResult.tasksMap,
                    parsingResult.epicsMap,
                    parsingResult.subtasksMap);

            // Восстанавливаем историю просмотров
            restoreHistory(loadedManager, lines, parsingResult.lineIndex, idMappingResult);

            // Сохраняем загруженные данные
            System.out.println("[DEBUG] Сохраняем загруженные данные");
            loadedManager.save();

            return loadedManager;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + file.getName(), e);
        }
    }

    // Переопределяем методы, изменяющие состояние менеджера, чтобы вызывать сохранение

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

}
