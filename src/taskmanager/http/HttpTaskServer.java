package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import taskmanager.http.handler.*;
import taskmanager.service.Managers;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HTTP-сервер для работы с задачами
 * Обрабатывает запросы к API для управления задачами, подзадачами и эпиками
 */
public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    /**
     * Конструктор по умолчанию, использует InMemoryTaskManager
     */
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    /**
     * Конструктор с указанием TaskManager
     * @param taskManager менеджер задач
     */
    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        
        // Регистрация обработчиков для различных путей
        server.createContext("/tasks", new TaskHandler(taskManager));
        server.createContext("/subtasks", new SubtaskHandler(taskManager));
        server.createContext("/epics", new EpicHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedTasksHandler(taskManager));
    }

    /**
     * Запустить сервер
     */
    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    /**
     * Остановить сервер
     */
    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    /**
     * Получить экземпляр Gson для сериализации/десериализации
     * @return экземпляр Gson
     */
    public static Gson getGson() {
        return gson;
    }

    /**
     * Точка входа в приложение
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();
            System.out.println("Для остановки сервера нажмите Enter");
            System.in.read();
            server.stop();
        } catch (IOException e) {
            System.out.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }
}