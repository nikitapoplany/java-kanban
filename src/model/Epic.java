package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий Эпик-задачу, которая может содержать несколько подзадач
 * Статус Эпика определяется статусом его подзадач
 */
public class Epic extends Task {
    // Список идентификаторов подзадач, принадлежащих этому эпику
    private final List<Integer> subtaskIds;

    /**
     * Конструктор для создания нового Эпика
     *
     * @param name        Название эпика
     * @param description Описание эпика
     */
    public Epic(String name, String description) {
        super(name, description);
        this.subtaskIds = new ArrayList<>();
    }

    /**
     * Конструктор для создания Эпика со всеми полями
     *
     * @param name        Название эпика
     * @param description Описание эпика
     * @param id          Уникальный идентификатор эпика
     * @param status      Статус эпика
     */
    public Epic(String name, String description, int id, TaskStatus status) {
        super(name, description, id, status);
        this.subtaskIds = new ArrayList<>();
    }

    // Получить список идентификаторов подзадач для этого эпика
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds); // Возвращаем копию, чтобы предотвратить внешнюю модификацию
    }

    // Добавить подзадачу к этому эпику
    public void addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }


    // Удалить подзадачу из этого эпика
    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    //Очистить все подзадачи из этого эпика
    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
