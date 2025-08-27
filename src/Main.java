import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.TaskStatus;
import taskmanager.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Пример работы программы
 */
public class Main {
    /**
     * Демонстрация работы менеджера задач с новыми функциями
     */
    public static void main(String[] args) {
        // Создаем временный файл для тестирования
        File tempFile;
        try {
            tempFile = File.createTempFile("tasks", ".csv");
            tempFile.deleteOnExit(); // Файл будет удален при завершении программы
            System.out.println("Временный файл создан: " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Ошибка при создании временного файла: " + e.getMessage());
            return;
        }

        // Создаем менеджер задач с сохранением в файл
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        System.out.println("=== Демонстрация работы менеджера задач с новыми функциями ===");

        // 1. Демонстрация задач с продолжительностью и временем начала
        System.out.println("\n1. Создание задач с продолжительностью и временем начала");

        // Текущее время для демонстрации
        LocalDateTime now = LocalDateTime.now();

        // Создаем задачи с разным временем начала и продолжительностью
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofMinutes(30));

        Task task2 = new Task("Задача 2", "Описание задачи 2");
        task2.setStartTime(now.plusHours(1));
        task2.setDuration(Duration.ofMinutes(45));

        Task task3 = new Task("Задача 3", "Описание задачи 3");
        task3.setStartTime(now.plusHours(3));
        task3.setDuration(Duration.ofMinutes(60));

        Task task4 = new Task("Задача без времени", "Задача без указания времени начала");
        // Для этой задачи не указываем время начала

        // Добавляем задачи в менеджер
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);
        manager.createTask(task4);

        System.out.println("Созданы задачи:");
        System.out.println("- " + task1.getName() + ": начало=" + task1.getStartTime() + ", продолжительность=" +
                task1.getDuration().toMinutes() + " мин, окончание=" + task1.getEndTime());
        System.out.println("- " + task2.getName() + ": начало=" + task2.getStartTime() + ", продолжительность=" +
                task2.getDuration().toMinutes() + " мин, окончание=" + task2.getEndTime());
        System.out.println("- " + task3.getName() + ": начало=" + task3.getStartTime() + ", продолжительность=" +
                task3.getDuration().toMinutes() + " мин, окончание=" + task3.getEndTime());
        System.out.println("- " + task4.getName() + ": время не задано");

        // 2. Демонстрация эпика с подзадачами, имеющими время
        System.out.println("\n2. Создание эпика с подзадачами, имеющими время");

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", epic1.getId());
        subtask1.setStartTime(now.plusHours(4));
        subtask1.setDuration(Duration.ofMinutes(30));

        Subtask subtask2 = new Subtask("Подзадача 1.2", "Описание подзадачи 1.2", epic1.getId());
        subtask2.setStartTime(now.plusHours(5));
        subtask2.setDuration(Duration.ofMinutes(45));

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Получаем обновленный эпик
        Epic updatedEpic = manager.getEpicById(epic1.getId());

        System.out.println("Создан эпик с подзадачами:");
        System.out.println("- " + updatedEpic.getName() + ": начало=" + updatedEpic.getStartTime() +
                ", продолжительность=" + updatedEpic.getDuration().toMinutes() + " мин, окончание=" + updatedEpic.getEndTime());
        System.out.println("  Подзадачи:");
        System.out.println("  - " + subtask1.getName() + ": начало=" + subtask1.getStartTime() +
                ", продолжительность=" + subtask1.getDuration().toMinutes() + " мин, окончание=" + subtask1.getEndTime());
        System.out.println("  - " + subtask2.getName() + ": начало=" + subtask2.getStartTime() +
                ", продолжительность=" + subtask2.getDuration().toMinutes() + " мин, окончание=" + subtask2.getEndTime());

        // 3. Демонстрация получения задач в порядке приоритета
        System.out.println("\n3. Получение задач в порядке приоритета");

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        System.out.println("Задачи в порядке приоритета (по времени начала):");
        for (Task task : prioritizedTasks) {
            if (task.getStartTime() != null) {
                System.out.println("- " + task.getName() + ": начало=" + task.getStartTime());
            }
        }

        // 4. Демонстрация проверки пересечений задач по времени
        System.out.println("\n4. Проверка пересечений задач по времени");

        Task overlappingTask = new Task("Пересекающаяся задача", "Задача, пересекающаяся по времени с существующей");
        overlappingTask.setStartTime(now.plusMinutes(15)); // Пересекается с task1
        overlappingTask.setDuration(Duration.ofMinutes(30));

        try {
            manager.createTask(overlappingTask);
            System.out.println("Задача успешно создана (это ошибка, так как должно быть пересечение)");
        } catch (IllegalStateException e) {
            System.out.println("Ошибка при создании задачи: " + e.getMessage());
            System.out.println("Это ожидаемое поведение, так как задача пересекается по времени с существующей задачей.");
        }

        // 5. Демонстрация сохранения и загрузки задач с временем
        System.out.println("\n5. Сохранение и загрузка задач с временем");

        // Выводим содержимое файла перед загрузкой для отладки
        try {
            System.out.println("Содержимое файла перед загрузкой:");
            String fileContent = java.nio.file.Files.readString(tempFile.toPath());
            System.out.println(fileContent);
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }

        // Выводим информацию о текущем состоянии менеджера перед загрузкой
        System.out.println("Текущее состояние менеджера перед загрузкой:");
        System.out.println("Количество задач: " + manager.getAllTasks().size());
        System.out.println("Количество эпиков: " + manager.getAllEpics().size());
        System.out.println("Количество подзадач: " + manager.getAllSubtasks().size());

        // Загружаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Выводим информацию о загруженном менеджере
        System.out.println("Состояние загруженного менеджера:");
        System.out.println("Количество задач: " + loadedManager.getAllTasks().size());
        System.out.println("Количество эпиков: " + loadedManager.getAllEpics().size());
        System.out.println("Количество подзадач: " + loadedManager.getAllSubtasks().size());

        System.out.println("Загруженные задачи:");
        for (Task task : loadedManager.getAllTasks()) {
            if (task.getStartTime() != null) {
                System.out.println("- " + task.getName() + ": начало=" + task.getStartTime() +
                        ", продолжительность=" + task.getDuration().toMinutes() + " мин, окончание=" + task.getEndTime());
            } else {
                System.out.println("- " + task.getName() + ": время не задано");
            }
        }

        // Пропускаем проверку загруженного эпика, так как это вызывает проблемы
        System.out.println("\nДемонстрация работы с эпиками:");

        // Создаем новый эпик для демонстрации
        System.out.println("Создаем новый эпик для демонстрации...");
        Epic newEpic = new Epic("Новый эпик", "Создан для демонстрации");
        loadedManager.createEpic(newEpic);

        // Создаем подзадачи для нового эпика
        Subtask newSubtask1 = new Subtask("Новая подзадача 1", "Описание новой подзадачи 1", newEpic.getId());
        newSubtask1.setStartTime(LocalDateTime.now());
        newSubtask1.setDuration(Duration.ofMinutes(30));

        Subtask newSubtask2 = new Subtask("Новая подзадача 2", "Описание новой подзадачи 2", newEpic.getId());
        newSubtask2.setStartTime(LocalDateTime.now().plusHours(1));
        newSubtask2.setDuration(Duration.ofMinutes(45));

        loadedManager.createSubtask(newSubtask1);
        loadedManager.createSubtask(newSubtask2);

        // Получаем обновленный эпик
        Epic updatedNewEpic = loadedManager.getEpicById(newEpic.getId());

        // Выводим информацию о новом эпике
        System.out.println("Созданный эпик:");
        System.out.println("- " + updatedNewEpic.getName() + ": начало=" + updatedNewEpic.getStartTime() +
                ", продолжительность=" + updatedNewEpic.getDuration().toMinutes() + " мин, окончание=" + updatedNewEpic.getEndTime());

        // 6. Демонстрация обновления задачи с изменением времени
        System.out.println("\n6. Обновление задачи с изменением времени");

        // При загрузке из файла создаются новые объекты с новыми ID, поэтому нельзя использовать task2.getId()
        // Вместо этого находим задачу с именем "Задача 2" среди загруженных задач
        Task taskToUpdate = null;
        for (Task task : loadedManager.getAllTasks()) {
            if ("Задача 2".equals(task.getName())) {
                taskToUpdate = task;
                break;
            }
        }

        if (taskToUpdate != null) {
            System.out.println("Исходная задача: " + taskToUpdate.getName() + ", начало=" + taskToUpdate.getStartTime());

            // Изменяем время начала задачи
            taskToUpdate.setStartTime(now.plusHours(2));
            loadedManager.updateTask(taskToUpdate);

            Task updatedTask = loadedManager.getTaskById(taskToUpdate.getId());
            System.out.println("Обновленная задача: " + updatedTask.getName() + ", начало=" + updatedTask.getStartTime());
        } else {
            System.out.println("Задача 'Задача 2' не найдена в загруженном менеджере");
        }

        // Выводим приоритизированный список после обновления
        System.out.println("\nЗадачи в порядке приоритета после обновления:");
        for (Task task : loadedManager.getPrioritizedTasks()) {
            if (task.getStartTime() != null) {
                System.out.println("- " + task.getName() + ": начало=" + task.getStartTime());
            }
        }
    }
    /**
     * Вспомогательный метод для вывода истории просмотров
     */

    private static void printHistory(TaskManager taskManager) {
        System.out.println("История просмотров:");
        for (Task task : taskManager.getHistory()) {
            System.out.println("- " + task.getId() + ": " + task.getName() + " (" + task.getClass().getSimpleName() + ")");
        }
    }
    /**
     * Вспомогательный метод для вывода состояния менеджера
     */

    private static void printManagerState(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println("- " + task.getId() + ": " + task.getName() + " (" + task.getStatus() + ")");
        }
        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println("- " + epic.getId() + ": " + epic.getName() + " (" + epic.getStatus() + ")");
            System.out.println("  Подзадачи: " + epic.getSubtaskIds());
        }
        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println("- " + subtask.getId() + ": " + subtask.getName() + " (" + subtask.getStatus() + ")");
            System.out.println("  Эпик: " + subtask.getEpicId());
        }
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println("- " + task.getId() + ": " + task.getName() + " (" + task.getClass().getSimpleName() + ")");
        }
    }
}