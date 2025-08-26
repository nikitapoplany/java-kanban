import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.TaskStatus;
import taskmanager.*;

import java.io.File;
import java.io.IOException;

/**
 * Пример работы программы
 */
public class Main {
    /**
     * Демонстрация работы FileBackedTaskManager
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
        System.out.println("=== Демонстрация работы FileBackedTaskManager ===");
        System.out.println("1. Создаем задачи, эпики и подзадачи");
        // Создаем две обычные задачи
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        manager.createTask(task1);
        manager.createTask(task2);
        System.out.println("Созданы задачи: " + task1.getId() + ", " + task2.getId());
        // Создаем эпик с тремя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1);
        System.out.println("Создан эпик: " + epic1.getId());
        Subtask subtask1 = new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 1.2", "Описание подзадачи 1.2", epic1.getId());
        Subtask subtask3 = new Subtask("Подзадача 1.3", "Описание подзадачи 1.3", epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);
        System.out.println("Созданы подзадачи: " + subtask1.getId() + ", " + subtask2.getId() + ", " + subtask3.getId());
        // Создаем эпик без подзадач
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.createEpic(epic2);
        System.out.println("Создан эпик без подзадач: " + epic2.getId());
        // Формируем историю просмотров
        System.out.println("\n2. Формируем историю просмотров");
        System.out.println("Просматриваем задачу 1 (ID: " + task1.getId() + ")");
        Task viewedTask1 = manager.getTaskById(task1.getId());
        System.out.println("Просматриваем эпик 1 (ID: " + epic1.getId() + ")");
        Epic viewedEpic1 = manager.getEpicById(epic1.getId());
        System.out.println("Просматриваем подзадачу 1.1 (ID: " + subtask1.getId() + ")");
        Subtask viewedSubtask1 = manager.getSubtaskById(subtask1.getId());
        System.out.println("Просматриваем задачу 2 (ID: " + task2.getId() + ")");
        Task viewedTask2 = manager.getTaskById(task2.getId());
        System.out.println("Просматриваем эпик 2 (ID: " + epic2.getId() + ")");
        Epic viewedEpic2 = manager.getEpicById(epic2.getId());
        System.out.println("История просмотров сформирована, размер: " + manager.getHistory().size());
        // Выводим текущее состояние менеджера
        System.out.println("\n3. Текущее состояние менеджера:");
        printManagerState(manager);
        // Загружаем менеджер из файла
        System.out.println("\n4. Загружаем менеджер из файла");
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        // Выводим состояние загруженного менеджера
        System.out.println("\n5. Состояние загруженного менеджера:");
        printManagerState(loadedManager);
        // Проверяем, что данные в загруженном менеджере корректны
        System.out.println("\n6. Проверяем, что данные в загруженном менеджере корректны");
        // Проверяем количество задач, эпиков и подзадач
        System.out.println("Количество задач: " + loadedManager.getAllTasks().size() + " (ожидается: " + manager.getAllTasks().size() + ")");
        System.out.println("Количество эпиков: " + loadedManager.getAllEpics().size() + " (ожидается: " + manager.getAllEpics().size() + ")");
        System.out.println("Количество подзадач: " + loadedManager.getAllSubtasks().size() + " (ожидается: " + manager.getAllSubtasks().size() + ")");
        // Проверяем, что все задачи имеют правильные имена и статусы
        System.out.println("\nПроверка задач:");
        for (Task task : loadedManager.getAllTasks()) {
            System.out.println("- " + task.getId() + ": " + task.getName() + " (" + task.getStatus() + ")");
        }
        // Проверяем, что все эпики имеют правильные имена, статусы и подзадачи
        System.out.println("\nПроверка эпиков:");
        for (Epic epic : loadedManager.getAllEpics()) {
            System.out.println("- " + epic.getId() + ": " + epic.getName() + " (" + epic.getStatus() + ")");
            System.out.println("  Подзадачи: " + epic.getSubtaskIds());
        }
        // Проверяем, что все подзадачи имеют правильные имена, статусы и эпики
        System.out.println("\nПроверка подзадач:");
        for (Subtask subtask : loadedManager.getAllSubtasks()) {
            System.out.println("- " + subtask.getId() + ": " + subtask.getName() + " (" + subtask.getStatus() + ")");
            System.out.println("  Эпик: " + subtask.getEpicId());
        }
        // Проверяем историю просмотров
        System.out.println("\nПроверка истории просмотров:");
        System.out.println("Размер истории: " + loadedManager.getHistory().size() + " (ожидается: " + manager.getHistory().size() + ")");
        for (Task task : loadedManager.getHistory()) {
            System.out.println("- " + task.getId() + ": " + task.getName() + " (" + task.getClass().getSimpleName() + ")");
        }
        System.out.println("\n7. Изменяем состояние задачи и проверяем сохранение");
        // Находим задачу с именем "Задача 1" в загруженном менеджере
        Task loadedTask1 = null;
        for (Task task : loadedManager.getAllTasks()) {
            if (task.getName().equals("Задача 1")) {
                loadedTask1 = task;
                break;
            }
        }
        if (loadedTask1 != null) {
            System.out.println("Найдена задача 'Задача 1' с ID: " + loadedTask1.getId() + ", статус: " + loadedTask1.getStatus());
            System.out.println("Изменяем статус задачи на IN_PROGRESS");
            loadedTask1.setStatus(TaskStatus.IN_PROGRESS);
            loadedManager.updateTask(loadedTask1);
            System.out.println("Статус задачи после обновления: " + loadedTask1.getStatus());
            // Проверяем, что задача обновлена в менеджере
            Task updatedTask = loadedManager.getTaskById(loadedTask1.getId());
            System.out.println("Статус задачи в менеджере после обновления: " + updatedTask.getStatus());
            // Создаем новый менеджер из того же файла
            System.out.println("Создаем новый менеджер из файла");
            FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(tempFile);
            // Находим задачу с именем "Задача 1" в перезагруженном менеджере
            Task reloadedTask1 = null;
            for (Task task : reloadedManager.getAllTasks()) {
                if (task.getName().equals("Задача 1")) {
                    reloadedTask1 = task;
                    break;
                }
            }
            if (reloadedTask1 != null) {
                System.out.println("Найдена задача 'Задача 1' с ID: " + reloadedTask1.getId() + ", статус: " + reloadedTask1.getStatus());
            } else {
                System.out.println("Ошибка: задача 'Задача 1' не найдена в перезагруженном менеджере");
            }
        } else {
            System.out.println("Ошибка: задача 'Задача 1' не найдена в загруженном менеджере");
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