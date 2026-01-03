package utils;

public interface WhiteboardListener {
    void onPointReceived(PointData point);
    void onCommandReceived(Command cmd, Object data);
    void onError(Exception e);
}
