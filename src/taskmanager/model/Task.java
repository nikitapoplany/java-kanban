package taskmanager.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Базовый класс, представляющий задачу в трекере задач
 */
public class Task {
    private String name;
    private String description;
    private int id;
    private TaskStatus status;
    private Duration duration; // Продолжительность задачи в минутах
    private LocalDateTime startTime; // Дата и время начала задачи

    /**
     * Конструктор для создания новой задачи
     *
     * @param name        Название задачи
     * @param description Описание задачи
     */
    public Task(String name, String description) {
        this.name = name;
        this.description = description;

        // Статус по умолчанию для новых задач
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
    }

    /**
     * Конструктор для создания задачи с определенным статусом
     *
     * @param name        Название задачи
     * @param description Описание задачи
     * @param status      Статус задачи
     */
    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = Duration.ZERO;
    }

    /**
     * Конструктор для создания задачи со всеми полями
     *
     * @param name        Название задачи
     * @param description Описание задачи
     * @param id          Уникальный идентификатор задачи
     * @param status      Статус задачи
     */
    public Task(String name, String description, int id, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = Duration.ZERO;
    }
    
    /**
     * Конструктор для создания задачи со всеми полями, включая продолжительность и время начала
     *
     * @param name        Название задачи
     * @param description Описание задачи
     * @param id          Уникальный идентификатор задачи
     * @param status      Статус задачи
     * @param duration    Продолжительность задачи
     * @param startTime   Время начала задачи
     */
    public Task(String name, String description, int id, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration != null ? duration : Duration.ZERO;
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    /**
     * Получить продолжительность задачи
     * @return продолжительность задачи
     */
    public Duration getDuration() {
        return duration;
    }
    
    /**
     * Установить продолжительность задачи
     * @param duration продолжительность задачи
     */
    public void setDuration(Duration duration) {
        this.duration = duration != null ? duration : Duration.ZERO;
    }
    
    /**
     * Получить время начала задачи
     * @return время начала задачи
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Установить время начала задачи
     * @param startTime время начала задачи
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Получить время завершения задачи
     * @return время завершения задачи или null, если время начала не задано
     */
    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;

        // Задачи равны, если у них одинаковый ID
        return id == task.id;
    }

    // Хэшкод на основе ID
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
