package taskmanager.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.http.HttpTaskServer;
import taskmanager.model.Task;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Обработчик HTTP-запросов для работы с задачами
 */
public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;
    private static final Pattern TASK_ID_PATTERN = Pattern.compile("/tasks/(\\d+)");

    /**
     * Конструктор
     * @param taskManager менеджер задач
     */
    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            // Обработка запросов к конкретной задаче по ID
            if (path.matches("/tasks/\\d+")) {
                int taskId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                
                switch (method) {
                    case "GET":
                        handleGetTaskById(exchange, taskId);
                        break;
                    case "DELETE":
                        handleDeleteTaskById(exchange, taskId);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                        exchange.close();
                }
                return;
            }

            // Обработка запросов ко всем задачам
            if (path.equals("/tasks")) {
                switch (method) {
                    case "GET":
                        handleGetAllTasks(exchange);
                        break;
                    case "POST":
                        handleCreateOrUpdateTask(exchange);
                        break;
                    case "DELETE":
                        handleDeleteAllTasks(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                        exchange.close();
                }
                return;
            }

            // Если путь не соответствует ни одному из обрабатываемых
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    /**
     * Обработать запрос на получение всех задач
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        sendText(exchange, gson.toJson(tasks));
    }

    /**
     * Обработать запрос на получение задачи по ID
     * @param exchange HTTP-обмен
     * @param taskId ID задачи
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetTaskById(HttpExchange exchange, int taskId) throws IOException {
        Task task = taskManager.getTaskById(taskId);
        if (task != null) {
            sendText(exchange, gson.toJson(task));
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обработать запрос на создание или обновление задачи
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        try {
            String body = readText(exchange);
            Task task = gson.fromJson(body, Task.class);
            
            if (task == null) {
                exchange.sendResponseHeaders(400, 0); // Bad Request
                exchange.close();
                return;
            }
            
            if (task.getId() == 0) {
                // Создание новой задачи
                try {
                    taskManager.createTask(task);
                    sendCreated(exchange);
                } catch (IllegalStateException e) {
                    // Задача пересекается с существующими
                    sendHasOverlaps(exchange);
                }
            } else {
                // Обновление существующей задачи
                Task existingTask = taskManager.getTaskById(task.getId());
                if (existingTask == null) {
                    sendNotFound(exchange);
                    return;
                }
                
                try {
                    taskManager.updateTask(task);
                    sendCreated(exchange);
                } catch (IllegalStateException e) {
                    // Задача пересекается с существующими
                    sendHasOverlaps(exchange);
                }
            }
        } catch (JsonSyntaxException e) {
            exchange.sendResponseHeaders(400, 0); // Bad Request
            exchange.close();
        }
    }

    /**
     * Обработать запрос на удаление задачи по ID
     * @param exchange HTTP-обмен
     * @param taskId ID задачи
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDeleteTaskById(HttpExchange exchange, int taskId) throws IOException {
        Task task = taskManager.getTaskById(taskId);
        if (task != null) {
            taskManager.deleteTaskById(taskId);
            sendText(exchange, "{}");
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обработать запрос на удаление всех задач
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendText(exchange, "{}");
    }
}