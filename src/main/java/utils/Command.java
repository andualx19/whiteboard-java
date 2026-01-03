package utils;

public enum Command {
    DRAW(1),
    STOP(0),
    CLEAR(2),
    ZOOM(3),
    SCROLL(4);

    public final int value;

    Command(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
