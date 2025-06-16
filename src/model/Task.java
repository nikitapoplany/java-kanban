package model;

import java.util.Objects;

/**
 * Базовый класс, представляющий задачу в трекере задач
 */
public class Task {
    private String name;
    private String description;
    private int id;
    private TaskStatus status;

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
                '}';
    }
}
