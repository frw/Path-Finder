package fred.struct;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public final class BinaryHeap<E> extends AbstractCollection<E> implements Queue<E> {

    private final static int DEFAULT_CAPACITY = 13;
    private int size;
    private E[] elements;
    private final boolean isMinHeap;
    private final Comparator<E> comparator;

    public BinaryHeap() {
        this(DEFAULT_CAPACITY, true);
    }

    public BinaryHeap(int capacity) {
        this(capacity, true);
    }

    public BinaryHeap(boolean isMinHeap) {
        this(DEFAULT_CAPACITY, isMinHeap);
    }

    public BinaryHeap(Comparator<E> comparator) {
        this(DEFAULT_CAPACITY, comparator);
    }

    public BinaryHeap(int capacity, Comparator<E> comparator) {
        this(capacity, true, comparator);
    }

    public BinaryHeap(boolean isMinHeap, Comparator<E> comparator) {
        this(DEFAULT_CAPACITY, isMinHeap, comparator);
    }

    public BinaryHeap(int capacity, boolean isMinHeap) {
        this(capacity, isMinHeap, null);
    }

    @SuppressWarnings("unchecked")
    public BinaryHeap(int capacity, boolean isMinHeap, Comparator<E> comparator) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("invalid capacity");
        }
        this.isMinHeap = isMinHeap;
        elements = (E[]) new Object[capacity + 1];
        this.comparator = comparator;
    }

    @Override
    public void clear() {
        if (size > 0) {
            Arrays.fill(elements, 0, size, null);
            size = 0;
        }
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isAtCapacity() {
        return elements.length == size + 1;
    }

    @Override
    public boolean add(E element) {
        if (isAtCapacity()) {
            grow();
        }
        if (isMinHeap) {
            percolateUpMinHeap(element);
        } else {
            percolateUpMaxHeap(element);
        }
        return true;
    }

    @Override
    public boolean offer(E element) {
        return add(element);
    }

    @Override
    public E peek() {
        if (isEmpty()) {
            return null;
        } else {
            return elements[1];
        }
    }

    @Override
    public E element() {
        E result = peek();
        if (result == null) {
            throw new NoSuchElementException();
        } else {
            return result;
        }
    }

    @Override
    public E poll() throws NoSuchElementException {
        E result;
        if (isEmpty()) {
            result = null;
        } else {
            result = elements[1];
            elements[1] = elements[size--];
            elements[size + 1] = null;
            if (size != 0) {
                if (isMinHeap) {
                    percolateDownMinHeap(1);
                } else {
                    percolateDownMaxHeap(1);
                }
            }
        }
        return result;
    }

    public boolean resort(E element) {
        for (int i = 1; i < elements.length; i++) {
            if (elements[i] == element) {
                if (isMinHeap) {
                    percolateUpMinHeap(i);
                } else {
                    percolateUpMaxHeap(i);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public E remove() {
        E result = poll();
        if (result == null) {
            throw new NoSuchElementException();
        } else {
            return result;
        }
    }

    @Override
    public int size() {
        return size;
    }

    protected void percolateDownMinHeap(final int index) {
        final E element = elements[index];
        int hole = index;

        while ((hole * 2) <= size) {
            int child = hole * 2;

            if (child != size && compare(elements[child + 1], elements[child]) < 0) {
                child++;
            }

            if (compare(elements[child], element) >= 0) {
                break;
            }

            elements[hole] = elements[child];
            hole = child;
        }

        elements[hole] = element;
    }

    protected void percolateDownMaxHeap(final int index) {
        final E element = elements[index];
        int hole = index;

        while ((hole * 2) <= size) {
            int child = hole * 2;

            if (child != size && compare(elements[child + 1], elements[child]) > 0) {
                child++;
            }

            if (compare(elements[child], element) <= 0) {
                break;
            }

            elements[hole] = elements[child];
            hole = child;
        }

        elements[hole] = element;
    }

    protected void percolateUpMinHeap(final E element) {
        elements[++size] = element;
        percolateUpMinHeap(size);
    }

    protected void percolateUpMinHeap(final int index) {
        int hole = index;
        E element = elements[hole];
        while (hole > 1 && compare(element, elements[hole / 2]) < 0) {
            final int next = hole / 2;
            elements[hole] = elements[next];
            hole = next;
        }
        elements[hole] = element;
    }

    protected void percolateUpMaxHeap(final E element) {
        elements[++size] = element;
        percolateUpMaxHeap(size);
    }

    protected void percolateUpMaxHeap(final int index) {
        int hole = index;
        E element = elements[hole];

        while (hole > 1 && compare(element, elements[hole / 2]) > 0) {
            final int next = hole / 2;
            elements[hole] = elements[next];
            hole = next;
        }

        elements[hole] = element;
    }

    @SuppressWarnings("unchecked")
    private int compare(E a, E b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        } else {
            return ((Comparable<E>) a).compareTo(b);
        }
    }

    @SuppressWarnings("unchecked")
    protected void grow() {
        E[] elements = (E[]) new Object[this.elements.length * 2];
        System.arraycopy(this.elements, 0, elements, 0, this.elements.length);
        this.elements = elements;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        for (int i = 1; i < size + 1; i++) {
            if (i != 1) {
                sb.append(", ");
            }
            sb.append(elements[i]);
        }
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int index = 1;
            private int lastReturnedIndex = -1;

            @Override
            public boolean hasNext() {
                return index <= size;
            }

            @Override
            public E next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                lastReturnedIndex = index;
                index++;
                return elements[lastReturnedIndex];
            }

            @Override
            public void remove() {
                if (lastReturnedIndex == -1) {
                    throw new IllegalStateException();
                }
                elements[lastReturnedIndex] = elements[size];
                elements[size] = null;
                size--;
                if (size != 0 && lastReturnedIndex <= size) {
                    int compareToParent = 0;
                    if (lastReturnedIndex > 1) {
                        compareToParent = compare(elements[lastReturnedIndex],
                                elements[lastReturnedIndex / 2]);
                    }
                    if (isMinHeap) {
                        if (lastReturnedIndex > 1 && compareToParent < 0) {
                            percolateUpMinHeap(lastReturnedIndex);
                        } else {
                            percolateDownMinHeap(lastReturnedIndex);
                        }
                    } else {
                        if (lastReturnedIndex > 1 && compareToParent > 0) {
                            percolateUpMaxHeap(lastReturnedIndex);
                        } else {
                            percolateDownMaxHeap(lastReturnedIndex);
                        }
                    }
                }
                index--;
                lastReturnedIndex = -1;
            }

        };
    }

}
