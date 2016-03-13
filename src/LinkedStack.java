
public class LinkedStack<E> implements Stack<E> {

    private static class Elem<T>{
        private T info;
        private Elem<T> next;
        private Elem( T info, Elem<T> next) {
            this.info = info;
            this.next = next;
        }
    }

    private Elem<E> top; // instance variable

    public LinkedStack() {

        top = null;
    }

    public boolean isEmpty() {

        return top == null;
    }

    public void push( E info ) {

        if (info == null) {
            throw new NullPointerException();
        }
        top = new Elem<E>(info, top);


    }

    public E peek()  {
        if (isEmpty()) {
            throw new NullPointerException();
        }
        return top.info;
    }

    public E pop() {

        try {
            if (isEmpty()) {
                throw new EmptyStackException();
            }
            E savedInfo = top.info;

            Elem<E> oldTop = top;
            Elem<E> newTop = top.next;

            top = newTop;

            oldTop.info = null; // scrubbing the memory
            oldTop.next = null; // scrubbing the memory

            return savedInfo;
        }
        catch (EmptyStackException e) {
            System.out.println("Error, stack is empty!");
        }
        return null;
    }
}
