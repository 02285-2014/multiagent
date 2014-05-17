package custommas.lib;
import java.util.Iterator;
import java.util.NoSuchElementException;

//Source: Algorithms 4th Edition, Sedgewick & Wayne
public class Stack<T> implements Iterable<T> {
    private int N;                // size of the stack
    private Node<T> first;     // top of stack

    // helper linked list class
    private static class Node<Item> {
        private Item item;
        private Node<Item> next;
    }

    public Stack() {
        first = null;
        N = 0;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public int size() {
        return N;
    }

    public void push(T item) {
        Node<T> oldfirst = first;
        first = new Node<T>();
        first.item = item;
        first.next = oldfirst;
        N++;
    }

    public T pop() {
        if (isEmpty()) throw new NoSuchElementException("Stack underflow");
        T item = first.item;        // save item to return
        first = first.next;            // delete first node
        N--;
        return item;                   // return the saved item
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Stack underflow");
        return first.item;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (T item : this)
            s.append(item + " ");
        return s.toString();
    }
       
    public Iterator<T> iterator() {
        return new ListIterator<T>(first);
    }

    // an iterator, doesn't implement remove() since it's optional
    private class ListIterator<TI> implements Iterator<TI> {
        private Node<TI> current;

        public ListIterator(Node<TI> first) {
            current = first;
        }
        public boolean hasNext()  { return current != null;                     }
        public void remove()      { throw new UnsupportedOperationException();  }

        public TI next() {
            if (!hasNext()) throw new NoSuchElementException();
            TI item = current.item;
            current = current.next; 
            return item;
        }
    }
}

