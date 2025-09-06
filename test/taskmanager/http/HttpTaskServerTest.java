package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.TaskStatus;
import taskmanager.service.Managers;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для HTTP-сервера задач
 */
public class HttpTaskServerTest {
    private HttpTaskServer taskServer;
    private TaskManager manager;
    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    // Тесты для задач
    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Тестовая задача", "Описание тестовой задачи", TaskStatus.NEW);
        manager.createTask(task);

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Тестовая задача", tasks.get(0).getName());
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Тестовая задача", "Описание тестовой задачи", TaskStatus.NEW);
        manager.createTask(task);
        int taskId = task.getId();

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(responseTask);
        assertEquals(taskId, responseTask.getId());
        assertEquals("Тестовая задача", responseTask.getName());
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        // Отправляем GET-запрос с несуществующим ID
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Новая задача", "Описание новой задачи", TaskStatus.NEW);
        String taskJson = gson.toJson(task);

        // Отправляем POST-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(201, response.statusCode());

        // Проверяем, что задача создана в менеджере
        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Новая задача", tasks.get(0).getName());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Задача для обновления", "Описание задачи", TaskStatus.NEW);
        manager.createTask(task);
        int taskId = task.getId();

        // Обновляем задачу
        task.setName("Обновленная задача");
        task.setStatus(TaskStatus.IN_PROGRESS);
        String taskJson = gson.toJson(task);

        // Отправляем POST-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(201, response.statusCode());

        // Проверяем, что задача обновлена в менеджере
        Task updatedTask = manager.getTaskById(taskId);
        assertEquals("Обновленная задача", updatedTask.getName());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Задача для удаления", "Описание задачи", TaskStatus.NEW);
        manager.createTask(task);
        int taskId = task.getId();

        // Отправляем DELETE-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем, что задача удалена из менеджера
        assertNull(manager.getTaskById(taskId));
    }

    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        // Создаем несколько задач
        manager.createTask(new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW));
        manager.createTask(new Task("Задача 2", "Описание задачи 2", TaskStatus.NEW));

        // Отправляем DELETE-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем, что все задачи удалены из менеджера
        assertTrue(manager.getAllTasks().isEmpty());
    }

    // Тесты для эпиков
    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        manager.createEpic(epic);

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {}.getType());
        assertNotNull(epics);
        assertEquals(1, epics.size());
        assertEquals("Тестовый эпик", epics.get(0).getName());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        manager.createEpic(epic);
        int epicId = epic.getId();

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(responseEpic);
        assertEquals(epicId, responseEpic.getId());
        assertEquals("Тестовый эпик", responseEpic.getName());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        manager.createEpic(epic);
        int epicId = epic.getId();

        // Создаем подзадачи для эпика
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {}.getType());
        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());
    }

    // Тесты для подзадач
    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        manager.createEpic(epic);
        int epicId = epic.getId();

        // Создаем подзадачу
        Subtask subtask = new Subtask("Тестовая подзадача", "Описание тестовой подзадачи", TaskStatus.NEW, epicId);
        manager.createSubtask(subtask);

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {}.getType());
        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
        assertEquals("Тестовая подзадача", subtasks.get(0).getName());
    }

    // Тесты для истории
    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // Создаем задачу и просматриваем ее
        Task task = new Task("Задача для истории", "Описание задачи", TaskStatus.NEW);
        manager.createTask(task);
        manager.getTaskById(task.getId()); // Добавляем в историю

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

    // Тесты для приоритизированных задач
    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Создаем задачи с разным временем начала
        LocalDateTime now = LocalDateTime.now();
        
        Task task1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        task1.setStartTime(now.plusHours(2));
        task1.setDuration(Duration.ofMinutes(30));
        
        Task task2 = new Task("Задача 2", "Описание задачи 2", TaskStatus.NEW);
        task2.setStartTime(now.plusHours(1));
        task2.setDuration(Duration.ofMinutes(30));
        
        manager.createTask(task1);
        manager.createTask(task2);

        // Отправляем GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());
        assertNotNull(prioritizedTasks);
        assertEquals(2, prioritizedTasks.size());
        // Проверяем, что задачи отсортированы по времени начала
        assertTrue(prioritizedTasks.get(0).getStartTime().isBefore(prioritizedTasks.get(1).getStartTime()));
    }
}