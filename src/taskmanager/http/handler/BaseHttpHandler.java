package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Базовый класс для HTTP-обработчиков
 * Содержит общие методы для чтения и отправки данных
 */
public class BaseHttpHandler {
    /**
     * Отправить текстовый ответ с кодом 200
     * @param h HTTP-обмен
     * @param text текст ответа
     * @throws IOException если произошла ошибка ввода-вывода
     */
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    /**
     * Отправить текстовый ответ с указанным кодом
     * @param h HTTP-обмен
     * @param text текст ответа
     * @param statusCode код статуса HTTP
     * @throws IOException если произошла ошибка ввода-вывода
     */
    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    /**
     * Отправить ответ с кодом 201 (Created)
     * @param h HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    protected void sendCreated(HttpExchange h) throws IOException {
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(201, 0);
        h.close();
    }

    /**
     * Отправить ответ с кодом 404 (Not Found)
     * @param h HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    protected void sendNotFound(HttpExchange h) throws IOException {
        String text = "{\"error\": \"Requested resource not found\"}";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    /**
     * Отправить ответ с кодом 406 (Not Acceptable) при пересечении задач
     * @param h HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    protected void sendHasOverlaps(HttpExchange h) throws IOException {
        String text = "{\"error\": \"Task overlaps with existing tasks\"}";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    /**
     * Отправить ответ с кодом 500 (Internal Server Error)
     * @param h HTTP-обмен
     * @throws IOException если произошла ошибка ввода-вывода
     */
    protected void sendInternalError(HttpExchange h) throws IOException {
        String text = "{\"error\": \"Internal server error\"}";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(500, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    /**
     * Прочитать тело запроса как строку
     * @param exchange HTTP-обмен
     * @return тело запроса в виде строки
     * @throws IOException если произошла ошибка ввода-вывода
     */
    protected String readText(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}