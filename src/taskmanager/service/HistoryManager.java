package taskmanager.service;

import taskmanager.model.Task;

import java.util.List;

/**
 * Интерфейс для управления историей просмотров задач
 */
public interface HistoryManager {
    /**
     * Добавить задачу в историю просмотров
     * @param task задача, которая была просмотрена
     */
    void add(Task task);

    /**
     * Удалить задачу из истории просмотров по идентификатору
     * @param id идентификатор задачи, которую нужно удалить из истории
     */
    void remove(int id);

    /**
     * Получить историю просмотров задач
     * @return список задач в порядке их просмотра (от самых старых к самым новым)
     */
    List<Task> getHistory();
}