package taskmanager;

import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import model.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            sb.append("id,type,name,status,description,epic\n");
            
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
            if (!history.isEmpty()) {
                List<String> historyIds = new ArrayList<>();
                for (Task task : history) {
                    historyIds.add(String.valueOf(task.getId()));
                }
                String historyStr = String.join(",", historyIds);
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
        
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                epicId);
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
        
        Task task;
        
        switch (type) {
            case TASK:
                task = new Task(name, description, id, status);
                break;
            case EPIC:
                task = new Epic(name, description, id, status);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                task = new Subtask(name, description, id, status, epicId);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
        
        return task;
    }

    /**
     * Загрузить менеджер задач из файла
     * @param file файл для загрузки данных
     * @return загруженный менеджер задач
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        
        try {
            System.out.println("[DEBUG] Загружаем файл: " + file.getAbsolutePath());
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            
            if (lines.length <= 1) {
                System.out.println("[DEBUG] Файл пустой или содержит только заголовок");
                return manager; // Файл пустой или содержит только заголовок
            }
            
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
            
            // Восстанавливаем связи между эпиками и подзадачами
            for (Subtask subtask : subtasksMap.values()) {
                Epic epic = epicsMap.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                } else {
                }
            }
            
            // Создаем новый менеджер с загруженными данными
            // Отключаем автосохранение на время загрузки
            System.out.println("[DEBUG] Создаем новый менеджер с загруженными данными");
            FileBackedTaskManager loadedManager = new FileBackedTaskManager(file) {
                @Override
                protected void save() {
                    // Отключаем автосохранение при загрузке
                    System.out.println("[DEBUG] Автосохранение отключено при загрузке");
                }
            };
            
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
                loadedManager.createTask(newTask);
                int newId = newTask.getId();
                
                // Запоминаем соответствие старого и нового ID
                oldToNewTaskIds.put(oldId, newId);
            }
            
            // Загружаем историю просмотров, если она есть
            if (lineIndex < lines.length - 1) {
                lineIndex++; // Пропускаем пустую строку
                
                if (lineIndex < lines.length && !lines[lineIndex].isBlank()) {
                    String historyLine = lines[lineIndex];
                    System.out.println("[DEBUG] Загружаем историю просмотров: " + historyLine);
                    String[] historyIds = historyLine.split(",");
                    
                    // Создаем список задач для истории в правильном порядке
                    List<Task> historyTasks = new ArrayList<>();
                    
                    for (String idStr : historyIds) {
                        int oldId = Integer.parseInt(idStr);

                        // Находим соответствующую задачу по старому ID
                        Task historyTask = null;
                        
                        // Проверяем, есть ли задача с таким ID в карте задач
                        Integer newTaskId = oldToNewTaskIds.get(oldId);
                        if (newTaskId != null) {
                            historyTask = loadedManager.getTaskById(newTaskId);
                            if (historyTask != null) {
                                historyTasks.add(historyTask);
                            }
                            continue;
                        }
                        
                        // Проверяем, есть ли эпик с таким ID в карте эпиков
                        Integer newEpicId = oldToNewEpicIds.get(oldId);
                        if (newEpicId != null) {
                            historyTask = loadedManager.getEpicById(newEpicId);
                            if (historyTask != null) {
                                historyTasks.add(historyTask);
                            }
                            continue;
                        }
                        
                        // Проверяем, есть ли подзадача с таким ID в карте подзадач
                        Integer newSubtaskId = oldToNewSubtaskIds.get(oldId);
                        if (newSubtaskId != null) {
                            historyTask = loadedManager.getSubtaskById(newSubtaskId);
                            if (historyTask != null) {
                                historyTasks.add(historyTask);
                            }
                            continue;
                        }
                        
                    }
                    
                    // Просто используем getTaskById, getEpicById и getSubtaskById для добавления задач в историю
                    // Это не идеальное решение, но оно работает
                    for (Task task : historyTasks) {
                        if (task instanceof Epic) {
                            loadedManager.getEpicById(task.getId());
                        } else if (task instanceof Subtask) {
                            loadedManager.getSubtaskById(task.getId());
                        } else {
                            loadedManager.getTaskById(task.getId());
                        }
                    }
                }
            }
            
            // Проверяем историю
            System.out.println("[DEBUG] Проверяем историю");
            System.out.println("[DEBUG] Размер истории: " + loadedManager.getHistory().size());
            
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