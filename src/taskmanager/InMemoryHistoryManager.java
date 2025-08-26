package taskmanager;

import taskmanager.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация интерфейса HistoryManager для хранения истории просмотров задач в памяти
 * с использованием двусвязного списка и HashMap для эффективного удаления задач из истории
 */
public class InMemoryHistoryManager implements HistoryManager {
    /**
     * Узел двусвязного списка
     */
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
            this.prev = null;
            this.next = null;
        }
    }

    /**
     * Двусвязный список для хранения истории просмотров
     */
    private static class CustomLinkedList {
        private Node head;
        private Node tail;
        private int size;

        CustomLinkedList() {
            this.head = null;
            this.tail = null;
            this.size = 0;
        }

        /**
         * Добавить задачу в конец списка
         * @param task задача для добавления
         * @return узел, в который была добавлена задача
         */
        Node linkLast(Task task) {
            Node newNode = new Node(task);
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                newNode.prev = tail;
                tail.next = newNode;
                tail = newNode;
            }
            size++;
            return newNode;
        }

        /**
         * Удалить узел из списка
         * @param node узел для удаления
         */
        void removeNode(Node node) {
            if (node == null) {
                return;
            }

            if (node.prev != null) {
                node.prev.next = node.next;
            } else {
                head = node.next;
            }

            if (node.next != null) {
                node.next.prev = node.prev;
            } else {
                tail = node.prev;
            }

            size--;
        }

        /**
         * Получить все задачи из списка
         * @return список задач
         */
        List<Task> getTasks() {
            List<Task> tasks = new ArrayList<>(size);
            Node current = head;
            while (current != null) {
                tasks.add(current.task);
                current = current.next;
            }
            return tasks;
        }
    }

    private final CustomLinkedList linkedList;
    private final Map<Integer, Node> nodeMap;

    /**
     * Конструктор для создания нового InMemoryHistoryManager
     */
    public InMemoryHistoryManager() {
        linkedList = new CustomLinkedList();
        nodeMap = new HashMap<>();
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
        // Удаляем предыдущий просмотр этой задачи, если он был
        remove(task.getId());
        // Добавляем задачу в конец списка и сохраняем узел в HashMap
        Node node = linkedList.linkLast(task);
        nodeMap.put(task.getId(), node);
    }

    /**
     * Удалить задачу из истории просмотров по идентификатору
     * @param id идентификатор задачи, которую нужно удалить из истории
     */
    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            linkedList.removeNode(node);
        }
    }

    /**
     * Получить историю просмотров задач
     * @return список задач в порядке их просмотра (от самых старых к самым новым)
     */
    @Override
    public List<Task> getHistory() {
        return linkedList.getTasks();
    }
}