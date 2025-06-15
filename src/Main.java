import taskmanager.*;

/**
 * Пример работы программы
 */
public class Main {
    public static void main(String[] args) {
        // Создаем менеджер задач
        TaskManager taskManager = new TaskManager();

        // Создаем две обычные задачи
        Task task1 = new Task("Задача 1", "Пусть здесь будет описание задачи 1");
        Task task2 = new Task("Задача 2", "Очевидно, что здесь должно быть описание задачи 2");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Создаем эпик с двумя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 1.2", "Описание подзадачи 1.2", epic1.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Создаем эпик с одной подзадачей
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Подзадача 2.1", "Описание подзадачи 2.1", epic2.getId());
        taskManager.createSubtask(subtask3);

        // Выводим списки задач, эпиков и подзадач
        System.out.println("=== Все задачи ===");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\n=== Все эпики ===");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\n=== Все подзадачи ===");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Выводим подзадачи эпика 1
        System.out.println("\n=== Подзадачи эпика 1 ===");
        for (Subtask subtask : taskManager.getSubtasksByEpicId(epic1.getId())) {
            System.out.println(subtask);
        }

        // Изменяем статусы задач и проверяем, что статус эпика корректно обновляется
        System.out.println("\n=== Изменение статусов ===");

        // Меняем статус первой подзадачи эпика 1 на IN_PROGRESS
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        System.out.println("Статус эпика 1 после изменения статуса подзадачи 1.1 на IN_PROGRESS: " +
                taskManager.getEpicById(epic1.getId()).getStatus());

        // Меняем статус второй подзадачи эпика 1 на DONE
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        System.out.println("Статус эпика 1 после изменения статуса подзадачи 1.2 на DONE: " +
                taskManager.getEpicById(epic1.getId()).getStatus());

        // Меняем статус первой подзадачи эпика 1 на DONE
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        System.out.println("Статус эпика 1 после изменения статуса подзадачи 1.1 на DONE: " +
                taskManager.getEpicById(epic1.getId()).getStatus());

        // Меняем статус подзадачи эпика 2 на IN_PROGRESS
        subtask3.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask3);
        System.out.println("Статус эпика 2 после изменения статуса подзадачи 2.1 на IN_PROGRESS: " +
                taskManager.getEpicById(epic2.getId()).getStatus());

        // Удаляем задачу и эпик
        System.out.println("\n=== Удаление задач ===");

        // Удаляем задачу 1
        taskManager.deleteTaskById(task1.getId());
        System.out.println("Задачи после удаления задачи 1:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        // Удаляем эпик 2
        taskManager.deleteEpicById(epic2.getId());
        System.out.println("\nЭпики после удаления эпика 2:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nПодзадачи после удаления эпика 2:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}
