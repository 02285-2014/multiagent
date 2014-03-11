package custommas.lib;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

// Source: Algorithms 4th Edition, Sedgewick & Wayne
public class Queue<T> implements Iterable<T> {
    private int N;               // number of elements on queue
    private Node<T> first;    // beginning of queue
    private Node<T> last;     // end of queue

    // helper linked list class
    private static class Node<T> {
        private T item;
        private Node<T> next;
    }

    public Queue() {
        first = null;
        last  = null;
        N = 0;
    }
    
    public Queue(List<T> fromList){
    	this();
    	for(T i : fromList){
    		enqueue(i);
    	}
    }

    public boolean isEmpty() {
        return first == null;
    }

    public int size() {
        return N;     
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Queue underflow");
        return first.item;
    }

    public void enqueue(T item) {
        Node<T> oldlast = last;
        last = new Node<T>();
        last.item = item;
        last.next = null;
        if (isEmpty()){
        	first = last;
        } else{
        	oldlast.next = last;
        }
        N++;
    }

    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Queue underflow");
        T item = first.item;
        first = first.next;
        N--;
        if (isEmpty()) last = null;   // to avoid loitering
        return item;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (T item : this)
            s.append(item + " ");
        return s.toString();
    } 

    public Iterator<T> iterator()  {
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