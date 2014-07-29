package concurrent.list;

import java.util.concurrent.Semaphore;

/**
 * A doubly linked list protected by fair semaphores.
 * 
 * <p>
 * Whenever you make a change in an element:
 * <ol>
 * <li>Acquire the predecessor semaphore (or headSemaphore);</li>
 * <li>Acquire the successor semaphore (or tailSemaphore);</li>
 * <li>Make the change;</li>
 * <li>Release the successor semaphore (or tailSemaphore);</li>
 * <li>Release the predecessor semaphore (or headSemaphore);</li>
 * </ol>
 *
 * @author Raphael Negrisoli Batista
 *
 * @param <E>
 */
public class SemaphorizedLinkedList<E> {

    private Node<E> head = null;
    private Node<E> tail = null;
    private Semaphore headSemaphore = new Semaphore(1, true);
    private Semaphore tailSemaphore = new Semaphore(1, true);
    private int size = 0;

    private static class Node<E> {
        E element;
        Node<E> next;
        Node<E> prev;
        Semaphore semaphore = new Semaphore(1, true);

        Node(Node<E> prev, E element, Node<E> next) {
            this.element = element;
            this.next = next;
            this.prev = prev;
        }
    }

    public void insertFirst(E element) {
        headSemaphore.acquireUninterruptibly();

        final Node<E> oldHead = this.head;

        Semaphore oldHeadSemaphore = oldHead != null ? oldHead.semaphore : tailSemaphore;
        oldHeadSemaphore.acquireUninterruptibly();

        final Node<E> newHead = new Node<>(null, element, oldHead);

        this.head = newHead;

        if (oldHead == null) {
            this.tail = newHead;
        } else {
            oldHead.prev = newHead;
        }

        size++;

        oldHeadSemaphore.release();
        headSemaphore.release();
    }

    public void insertLast(E element) {
        final Node<E> oldTail = this.tail;

        Semaphore oldTailSemaphore = oldTail != null ? oldTail.semaphore : headSemaphore;
        oldTailSemaphore.acquireUninterruptibly();
        tailSemaphore.acquireUninterruptibly();

        final Node<E> newTail = new Node<>(oldTail, element, null);

        this.tail = newTail;

        if (oldTail == null) {
            this.head = newTail;
        } else {
            oldTail.next = newTail;
        }

        size++;

        oldTailSemaphore.release();
        tailSemaphore.release();
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

        Semaphore predSemaphore = pred != null ? pred.semaphore : headSemaphore;
        predSemaphore.acquireUninterruptibly();
        succ.semaphore.acquireUninterruptibly();

        final Node<E> newNode = new Node<>(pred, element, succ);

        succ.prev = newNode;
        if (pred == null) {
            this.head = newNode;
        } else {
            pred.next = newNode;
        }

        size++;

        predSemaphore.release();
        succ.semaphore.release();
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

        Semaphore prevSemaphore = prev != null ? prev.semaphore : headSemaphore;
        Semaphore nextSemaphore = next != null ? next.semaphore : tailSemaphore;
        prevSemaphore.acquireUninterruptibly();
        nextSemaphore.acquireUninterruptibly();

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

        prevSemaphore.release();
        nextSemaphore.release();

        return element;
    }

    public int size() {
        return size;
    }
}