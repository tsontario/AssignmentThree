public class LinkedQueue<E> implements Queue<E> {

    private static class Elem<T> {

        private T value;
        private Elem<T> next;

        private Elem( T value, Elem<T> next ) {
            this.value = value;
            this.next = next;
        }
    }

    private Elem<E> front;
    private Elem<E> rear;

    public E peek() throws EmptyQueueException {

        return front.value;
    }

    public void enqueue( E o ) {
        Elem<E> newElem;
        newElem = new Elem<E>( o, null );

        if ( rear == null ) {
            front = rear = newElem;
        } else {
            rear.next = newElem;
            rear = newElem;
        }
    }

    public E dequeue() throws EmptyQueueException {

        try {

            if (isEmpty()) {
                throw new EmptyQueueException();
            }
            E result = front.value;
            if ( front != null & front.next == null ) {
                front = rear = null;
            } else {
                front = front.next;
            }
            return result;
        }
        catch (EmptyQueueException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean isEmpty() {
        return front == null;
    }

}
