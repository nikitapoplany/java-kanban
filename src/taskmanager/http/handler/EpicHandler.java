package taskmanager.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.http.HttpTaskServer;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Обработчик HTTP-запросов для работы с эпиками
 */
public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;
    private static final Pattern EPIC_ID_PATTERN = Pattern.compile("/epics/(\\d+)");
    private static final Pattern EPIC_SUBTASKS_PATTERN = Pattern.compile("/epics/(\\d+)/subtasks");

    /**
     * Конструктор
     * @param taskManager менеджер задач
     */
    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            // Обработка запросов к подзадачам эпика
            if (path.matches("/epics/\\d+/subtasks")) {
                if (method.equals("GET")) {
                    int epicId = Integer.parseInt(path.split("/")[2]);
                    handleGetEpicSubtasks(exchange, epicId);
                } else {
                    exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                    exchange.close();
                }
                return;
            }

            // Обработка запросов к конкретному эпику по ID
            if (path.matches("/epics/\\d+")) {
                int epicId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));

                switch (method) {
                    case "GET":
                        handleGetEpicById(exchange, epicId);
                        break;
                    case "DELETE":
                        handleDeleteEpicById(exchange, epicId);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                        exchange.close();
                }
                return;
            }

            // Обработка запросов ко всем эпикам
            if (path.equals("/epics")) {
                switch (method) {
                    case "GET":
                        handleGetAllEpics(exchange);
                        break;
                    case "POST":
                        handleCreateOrUpdateEpic(exchange);
                        break;
                    case "DELETE":
                        handleDeleteAllEpics(exchange);
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
     * Обработать запрос на получение всех эпиков
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        sendText(exchange, gson.toJson(epics));
    }

    /**
     * Обработать запрос на получение эпика по ID
     * @param exchange HTTP-обмен
     * @param epicId ID эпика
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetEpicById(HttpExchange exchange, int epicId) throws IOException {
        Epic epic = taskManager.getEpicById(epicId);
        if (epic != null) {
            sendText(exchange, gson.toJson(epic));
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обработать запрос на получение подзадач эпика
     * @param exchange HTTP-обмен
     * @param epicId ID эпика
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetEpicSubtasks(HttpExchange exchange, int epicId) throws IOException {
        Epic epic = taskManager.getEpicById(epicId);
        if (epic != null) {
            List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epicId);
            sendText(exchange, gson.toJson(subtasks));
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обработать запрос на создание или обновление эпика
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        try {
            String body = readText(exchange);
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic == null) {
                exchange.sendResponseHeaders(400, 0); // Bad Request
                exchange.close();
                return;
            }

            if (epic.getId() == 0) {
                // Создание нового эпика
                taskManager.createEpic(epic);
                sendCreated(exchange);
            } else {
                // Обновление существующего эпика
                Epic existingEpic = taskManager.getEpicById(epic.getId());
                if (existingEpic == null) {
                    sendNotFound(exchange);
                    return;
                }

                taskManager.updateEpic(epic);
                sendCreated(exchange);
            }
        } catch (JsonSyntaxException e) {
            exchange.sendResponseHeaders(400, 0); // Bad Request
            exchange.close();
        }
    }

    /**
     * Обработать запрос на удаление эпика по ID
     * @param exchange HTTP-обмен
     * @param epicId ID эпика
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDeleteEpicById(HttpExchange exchange, int epicId) throws IOException {
        Epic epic = taskManager.getEpicById(epicId);
        if (epic != null) {
            taskManager.deleteEpicById(epicId);
            sendText(exchange, "{}");
        } else {
            sendNotFound(exchange);
        }
    }

    /**
     * Обработать запрос на удаление всех эпиков
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        taskManager.deleteAllEpics();
        sendText(exchange, "{}");
    }
}