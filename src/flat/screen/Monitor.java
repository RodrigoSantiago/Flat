package flat.screen;

import java.util.List;

public class Monitor {
    public static List<Monitor> getMonitors() {
        return null;
    }

    public static Monitor getPrimaryMonitor() {
        return null;
    }

    public String getName() {
        return null;
    }

    public List<VideoMode> getVideoModes() {
        return null;
    }

    public int getX() {
        return 0;
    }

    public int getY() {
        return 0;
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    public int getPhysicalWidth() {
        return 0;
    }

    public int getPhysicalHeight() {
        return 0;
    }

    public static final class VideoMode {
        public final int width, height, red, green, blue, refreshRate;

        public VideoMode(int width, int height, int red, int green, int blue, int refreshRate) {
            this.width = width;
            this.height = height;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.refreshRate = refreshRate;
        }
    }
}
