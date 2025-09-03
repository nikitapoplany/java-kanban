package taskmanager.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.http.HttpTaskServer;
import taskmanager.model.Task;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.util.List;

/**
 * Обработчик HTTP-запросов для работы с приоритизированными задачами
 */
public class PrioritizedTasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    /**
     * Конструктор
     * @param taskManager менеджер задач
     */
    public PrioritizedTasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            // Обработка запросов к приоритизированным задачам
            if (path.equals("/prioritized")) {
                if (method.equals("GET")) {
                    handleGetPrioritizedTasks(exchange);
                } else {
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
     * Обработать запрос на получение приоритизированных задач
     * @param exchange HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        sendText(exchange, gson.toJson(prioritizedTasks));
    }
}