package taskmanager;

/**
 * Утилитарный класс для создания менеджеров задач и истории
 */
public class Managers {
    /**
     * Получить менеджер задач по умолчанию
     * @return объект, реализующий интерфейс TaskManager
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    /**
     * Получить менеджер истории по умолчанию
     * @return объект, реализующий интерфейс HistoryManager
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
