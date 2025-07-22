import model.Epic;
import model.Subtask;
import model.Task;
import taskmanager.*;

/**
 * Пример работы программы
 */
public class Main {
    public static void main(String[] args) {
        // Создаем менеджер задач
        TaskManager taskManager = Managers.getDefault();

        System.out.println("=== Демонстрация работы обновленной истории просмотров ===");
        System.out.println("1. Создаем задачи, эпики и подзадачи");
        // Создаем две обычные задачи
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        System.out.println("Созданы задачи: " + task1.getId() + ", " + task2.getId());

        // Создаем эпик с тремя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);
        System.out.println("Создан эпик: " + epic1.getId());

        Subtask subtask1 = new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 1.2", "Описание подзадачи 1.2", epic1.getId());
        Subtask subtask3 = new Subtask("Подзадача 1.3", "Описание подзадачи 1.3", epic1.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);
        System.out.println("Созданы подзадачи: " + subtask1.getId() + ", " + subtask2.getId() + ", " + subtask3.getId());

        // Создаем эпик без подзадач
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);
        System.out.println("Создан эпик без подзадач: " + epic2.getId());

        // 2. Запрашиваем задачи несколько раз в разном порядке
        System.out.println("\n2. Запрашиваем задачи и проверяем историю просмотров");
        System.out.println("\nЗапрашиваем задачу 1");
        taskManager.getTaskById(task1.getId());
        printHistory(taskManager);
        System.out.println("\nЗапрашиваем эпик 1");
        taskManager.getEpicById(epic1.getId());
        printHistory(taskManager);
        System.out.println("\nЗапрашиваем подзадачу 1.1");
        taskManager.getSubtaskById(subtask1.getId());
        printHistory(taskManager);
        System.out.println("\nЗапрашиваем задачу 2");
        taskManager.getTaskById(task2.getId());
        printHistory(taskManager);
        System.out.println("\nЗапрашиваем эпик 2");
        taskManager.getEpicById(epic2.getId());
        printHistory(taskManager);
        System.out.println("\nЗапрашиваем подзадачу 1.2");
        taskManager.getSubtaskById(subtask2.getId());
        printHistory(taskManager);
        System.out.println("\nЗапрашиваем подзадачу 1.3");
        taskManager.getSubtaskById(subtask3.getId());
        printHistory(taskManager);
        // 3. Повторно запрашиваем некоторые задачи и проверяем, что в истории нет повторов
        System.out.println("\n3. Повторно запрашиваем задачи и проверяем, что в истории нет повторов");
        System.out.println("\nПовторно запрашиваем задачу 1");
        taskManager.getTaskById(task1.getId());
        printHistory(taskManager);
        System.out.println("\nПовторно запрашиваем эпик 1");
        taskManager.getEpicById(epic1.getId());
        printHistory(taskManager);
        System.out.println("\nПовторно запрашиваем подзадачу 1.1");
        taskManager.getSubtaskById(subtask1.getId());
        printHistory(taskManager);
        // 4. Удаляем задачу из истории и проверяем, что она не выводится
        System.out.println("\n4. Удаляем задачу 2 и проверяем, что она удалена из истории");
        taskManager.deleteTaskById(task2.getId());
        System.out.println("Задача 2 удалена");
        printHistory(taskManager);
        // 5. Удаляем эпик с подзадачами и проверяем, что из истории удалились и эпик, и его подзадачи
        System.out.println("\n5. Удаляем эпик 1 с подзадачами и проверяем, что они удалены из истории");
        taskManager.deleteEpicById(epic1.getId());
        System.out.println("Эпик 1 и его подзадачи удалены");
        printHistory(taskManager);
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
}