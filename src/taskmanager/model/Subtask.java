package taskmanager.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Класс, представляющий Подзадачу, которая является частью Эпика
 */
public class Subtask extends Task {
    // Идентификатор эпика, к которому принадлежит эта подзадача
    private int epicId;

    /**
     * Конструктор для создания новой Подзадачи
     *
     * @param name        Название подзадачи
     * @param description Описание подзадачи
     * @param epicId      Идентификатор эпика, к которому принадлежит эта подзадача
     */
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    /**
     * Конструктор для создания Подзадачи с определенным статусом
     *
     * @param name        Название подзадачи
     * @param description Описание подзадачи
     * @param status      Статус подзадачи
     * @param epicId      Идентификатор эпика, к которому принадлежит эта подзадача
     */
    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    /**
     * Конструктор для создания Подзадачи со всеми полями
     *
     * @param name        Название подзадачи
     * @param description Описание подзадачи
     * @param id          Уникальный идентификатор подзадачи
     * @param status      Статус подзадачи
     * @param epicId      Идентификатор эпика, к которому принадлежит эта подзадача
     */
    public Subtask(String name, String description, int id, TaskStatus status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    /**
     * Конструктор для создания Подзадачи со всеми полями, включая продолжительность и время начала
     *
     * @param name        Название подзадачи
     * @param description Описание подзадачи
     * @param id          Уникальный идентификатор подзадачи
     * @param status      Статус подзадачи
     * @param epicId      Идентификатор эпика, к которому принадлежит эта подзадача
     * @param duration    Продолжительность подзадачи
     * @param startTime   Время начала подзадачи
     */
    public Subtask(String name, String description, int id, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, id, status, duration, startTime);
        this.epicId = epicId;
    }

    //Получить идентификатор эпика, к которому принадлежит эта подзадача
    public int getEpicId() {
        return epicId;
    }

    //Установить идентификатор эпика, к которому принадлежит эта подзадача
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}
