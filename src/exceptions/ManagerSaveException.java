package exceptions;

/**
 * Исключение, выбрасываемое при ошибках сохранения/загрузки менеджера задач
 */
public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message) {
        super(message);
    }

    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}