package concurrent.list;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
public class SynchronizedLinkedList<E> implements List<E> {

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
        Node<E> node = node(index);
        return node != null ? remove(node) : null;
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

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Iterator<E> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add(E e) {
        insertLast(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return remove(indexOf(o)) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public E set(int index, E element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void add(int index, E element) {
        if (index == 0) {
            insertFirst(element);
        } else if (index == size) {
            insertLast(element);
        } else {
            insertAt(element, index);
        }
    }

    @Override
    public E remove(int index) {
        return removeAt(index);
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Node<E> x = head; x != null; x = x.next) {
                if (x.element == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = head; x != null; x = x.next) {
                if (o.equals(x.element))
                    return index;
                index++;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ListIterator<E> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }
}