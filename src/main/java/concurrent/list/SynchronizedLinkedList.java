package concurrent.list;

/**
 * A doubly linked list protected by synchronized monitors.
 * 
 * <p>
 * Whenever you make a change in an element:
 * <ol>
 * <li>Acquire the predecessor lock (or headLock);</li>
 * <li>Acquire the successor lock (or tailLock);</li>
 * <li>Make the change;</li>
 * <li>Release the successor lock (or tailLock);</li>
 * <li>Release the predecessor lock (or headLock);</li>
 * </ol>
 *
 * @author Raphael Negrisoli Batista
 *
 * @param <E>
 */
public class SynchronizedLinkedList<E> {

    private Node<E> head = null;
    private Node<E> tail = null;
    private Object headLock = new Object();
    private Object tailLock = new Object();
    private int size = 0;

    private static class Node<E> {
        E element;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.element = element;
            this.next = next;
            this.prev = prev;
        }
    }

    public void insertFirst(E element) {
        synchronized (headLock) {

            final Node<E> oldHead = this.head;

            Object oldHeadLock = oldHead != null ? oldHead : tailLock;
            synchronized (oldHeadLock) {

                final Node<E> newHead = new Node<>(null, element, oldHead);

                this.head = newHead;

                if (oldHead == null) {
                    this.tail = newHead;
                } else {
                    oldHead.prev = newHead;
                }

                size++;

            }
        }
    }

    public void insertLast(E element) {
        final Node<E> oldTail = this.tail;

        Object oldTailSemaphore = oldTail != null ? oldTail : headLock;
        synchronized (oldTailSemaphore) {
            synchronized (tailLock) {

                final Node<E> newTail = new Node<>(oldTail, element, null);

                this.tail = newTail;

                if (oldTail == null) {
                    this.head = newTail;
                } else {
                    oldTail.next = newTail;
                }

                size++;
            }
        }
    }

    public void insertAt(E element, int index) {
        checkPositionIndex(index);

        if (index == size) {
            insertLast(element);
        } else {
            insertBefore(element, node(index));
        }
    }

    public E removeFirst() {
        return remove(head);
    }

    public E removeLast() {
        return remove(tail);
    }

    public E removeAt(int index) {
        return remove(node(index));
    }

    public E getFirst() {
        final Node<E> f = head;
        return f.element;
    }

    public E getLast() {
        final Node<E> l = tail;
        return l.element;
    }

    public E get(int index) {
        final Node<E> node = node(index);
        return node.element;
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index)) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    private void insertBefore(E element, Node<E> succ) {
        final Node<E> pred = succ.prev;

        Object predSemaphore = pred != null ? pred : headLock;
        synchronized (predSemaphore) {
            synchronized (succ) {

                final Node<E> newNode = new Node<>(pred, element, succ);

                succ.prev = newNode;
                if (pred == null) {
                    this.head = newNode;
                } else {
                    pred.next = newNode;
                }

                size++;

            }
        }
    }

    private Node<E> node(int index) {
        Node<E> node;
        if (index < (size >> 1)) {
            node = this.head;

            for (int i = 0; i < index; i++) {
                node = node.next;
            }
        } else {
            node = this.tail;

            for (int i = size - 1; i > index; i--) {
                node = node.prev;
            }
        }

        return node;
    }

    private E remove(Node<E> node) {
        final E element = node.element;
        final Node<E> next = node.next;
        final Node<E> prev = node.prev;

        Object prevSemaphore = prev != null ? prev : headLock;
        Object nextSemaphore = next != null ? next: tailLock;
        synchronized (prevSemaphore) {
            synchronized (nextSemaphore) {

                if (prev == null) {
                    this.head = next;
                } else {
                    prev.next = next;
                    node.prev = null;
                }

                if (next == null) {
                    this.tail = prev;
                } else {
                    next.prev = prev;
                    node.next = null;
                }

                node.element = null;
                size--;
            }
        }

        return element;
    }

    public int size() {
        return size;
    }
}