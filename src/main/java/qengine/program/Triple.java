package qengine.program;

public class Triple<T, S, U> {
    private final T first;
    private final S second;
    private final U third;

    public Triple(T first, S second, U third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public U getThird() {
        return third;
    }
}
