package taskmanager.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.http.HttpTaskServer;
import taskmanager.model.Subtask;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Обработчик HTTP-запросов для работы с подзадачами
 */
public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;
    private static final Pattern SUBTASK_ID_PATTERN = Pattern.compile("/subtasks/(\\d+)");

    /**
     * Конструктор
     * @param taskManager менеджер задач
     */
    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            // Обработка запросов к конкретной подзадаче по ID
            if (path.matches("/subtasks/\\d+")) {
                int subtaskId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                
                switch (method) {
                    case "GET":
                        handleGetSubtaskById(exchange, subtaskId);
                        break;
                    case "DELETE":
                        handleDeleteSubtaskById(exchange, subtaskId);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                        exchange.close();
                }
                return;
            }

            // Обработка запросов ко всем подзадачам
            if (path.equals("/subtasks")) {
                switch (method) {
                    case "GET":
                        handleGetAllSubtasks(exchange);
                        break;
                    case "POST":
                        handleCreateOrUpdateSubtask(exchange);
                        break;
                    case "DELETE":
                        handleDeleteAllSubtasks(exchange);
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
     * Обработать запрос на получение всех подзадач
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        sendText(exchange, gson.toJson(subtasks));
    }

    /**
     * Обработать запрос на получение подзадачи по ID
     * @param exchange HTTP-обмен
     * @param subtaskId ID подзадачи
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetSubtaskById(HttpExchange exchange, int subtaskId) throws IOException {
        Subtask subtask = taskManager.getSubtaskById(subtaskId);
        if (subtask != null) {
            sendText(exchange, gson.toJson(subtask));
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обработать запрос на создание или обновление подзадачи
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        try {
            String body = readText(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            
            if (subtask == null) {
                exchange.sendResponseHeaders(400, 0); // Bad Request
                exchange.close();
                return;
            }
            
            if (subtask.getId() == 0) {
                // Создание новой подзадачи
                try {
                    taskManager.createSubtask(subtask);
                    sendCreated(exchange);
                } catch (IllegalStateException e) {
                    // Подзадача пересекается с существующими
                    sendHasOverlaps(exchange);
                }
            } else {
                // Обновление существующей подзадачи
                Subtask existingSubtask = taskManager.getSubtaskById(subtask.getId());
                if (existingSubtask == null) {
                    sendNotFound(exchange);
                    return;
                }
                
                try {
                    taskManager.updateSubtask(subtask);
                    sendCreated(exchange);
                } catch (IllegalStateException e) {
                    // Подзадача пересекается с существующими
                    sendHasOverlaps(exchange);
                }
            }
        } catch (JsonSyntaxException e) {
            exchange.sendResponseHeaders(400, 0); // Bad Request
            exchange.close();
        }
    }

    /**
     * Обработать запрос на удаление подзадачи по ID
     * @param exchange HTTP-обмен
     * @param subtaskId ID подзадачи
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDeleteSubtaskById(HttpExchange exchange, int subtaskId) throws IOException {
        Subtask subtask = taskManager.getSubtaskById(subtaskId);
        if (subtask != null) {
            taskManager.deleteSubtaskById(subtaskId);
            sendText(exchange, "{}");
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обработать запрос на удаление всех подзадач
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllSubtasks();
        sendText(exchange, "{}");
    }
}