package taskmanager;

import model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация интерфейса HistoryManager для хранения истории просмотров задач в памяти
 */
public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history;
    private static final int MAX_HISTORY_SIZE = 10;

    /**
     * Конструктор для создания нового InMemoryHistoryManager
     */
    public InMemoryHistoryManager() {
        history = new ArrayList<>();
    }

    /**
     * Добавить задачу в историю просмотров
     * @param task задача, которая была просмотрена
     */
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        
        history.add(task);
        // Ограничиваем историю до MAX_HISTORY_SIZE элементов
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    /**
     * Получить историю просмотров задач
     * @return список задач в порядке их просмотра (от самых старых к самым новым)
     */
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}