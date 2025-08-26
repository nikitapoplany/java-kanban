package taskmanager;

import java.io.File;

/**
 * Утилитарный класс для создания менеджеров задач и истории
 */
public class Managers {
    /**
     * Получить менеджер задач по умолчанию (в памяти)
     * @return объект, реализующий интерфейс TaskManager
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    /**
     * Получить менеджер задач с сохранением в файл
     * @param file файл для сохранения данных
     * @return объект, реализующий интерфейс TaskManager с автосохранением в файл
     */
    public static TaskManager getFileBacked(File file) {
        return new FileBackedTaskManager(file, getDefaultHistory());
    }

    /**
     * Загрузить менеджер задач из файла
     * @param file файл для загрузки данных
     * @return объект, реализующий интерфейс TaskManager с автосохранением в файл
     */
    public static TaskManager loadFromFile(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }

    /**
     * Получить менеджер истории по умолчанию
     * @return объект, реализующий интерфейс HistoryManager
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
