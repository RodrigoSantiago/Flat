package flat.events;

import flat.widget.Widget;

public class PointerEvent extends Event {
    public static final int PRESSED     = 1;
    public static final int RELEASED    = 2;
    public static final int DRAGGED     = 3;
    public static final int MOVED       = 4;
    public static final int ENTERED     = 5;
    public static final int EXITED      = 6;
    public static final int SCROLL      = 7;

    private final PointerData[] pointers;
    private final byte pointerIndex;
    private final byte mouseButton;

    public PointerEvent(Widget source, int type, int pointerIndex, PointerData... pointers) {
        super(source, type);
        this.pointerIndex = (byte) pointerIndex;
        this.pointers = pointers;
        this.mouseButton = -1;
    }

    public PointerEvent(Widget source, int type, int mouseButton, double screenX, double screenY) {
        super(source, type);
        this.pointerIndex = 0;
        this.pointers = new PointerData[]{new PointerData(-1, screenX, screenY, 0, 0, 0.5, false)};
        this.mouseButton = (byte) mouseButton;
    }

    @Override
    public PointerEvent recycle(Widget source) {
        super.recycle(source);
        return this;
    }

    public boolean isMouseEvent() {
        return mouseButton != -1;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public int getPointerCount() {
        return pointers.length;
    }

    public int getPointerIndex() {
        return pointerIndex;
    }

    public int getId() {
        return getId(pointerIndex);
    }

    public double getScreenX() {
        return getScreenX(pointerIndex);
    }

    public double getScreenY() {
        return getScreenY(pointerIndex);
    }

    public double getX() {
        return getX(pointerIndex);
    }

    public double getY() {
        return getY(pointerIndex);
    }

    public double getPressure() {
        return getPressure(pointerIndex);
    }

    public boolean isButtonPressed() {
        return isButtonPressed(pointerIndex);
    }

    public int getId(int pointer) {
        return pointers[pointer].id;
    }

    public double getScreenX(int pointer) {
        return pointers[pointer].screenX;
    }

    public double getScreenY(int pointer) {
        return pointers[pointer].screenY;
    }

    public double getX(int pointer) {
        return pointers[pointer].x;
    }

    public double getY(int pointer) {
        return pointers[pointer].y;
    }

    public double getPressure(int pointer) {
        return pointers[pointer].pressure;
    }

    public boolean isButtonPressed(int pointer) {
        return pointers[pointer].buttonPressed;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (getType() == PRESSED) s.append("PRESSED");
        else if (getType() == RELEASED) s.append("RELEASED");
        else if (getType() == DRAGGED) s.append("DRAGGED");
        else if (getType() == MOVED) s.append("MOVED");
        else if (getType() == ENTERED) s.append("ENTERED");
        else if (getType() == EXITED) s.append("EXITED");
        else if (getType() == SCROLL) s.append("SCROLL");
        for (int i = 0; i < pointers.length; i++) {
            if (i > 0) s.append(",");
            s.append(pointers[i]);
        }
        return s.toString();
    }

    public static class PointerData {
        int id;
        double screenX, screenY;
        double x, y;
        double pressure;
        boolean buttonPressed;

        public PointerData(int id, double screenX, double screenY, double x, double y, double pressure, boolean buttonPressed) {
            set(id, screenX, screenY, x, y, pressure, buttonPressed);
        }

        void set(int id, double screenX, double screenY, double x, double y, double pressure, boolean buttonPressed) {
            this.id = id;
            this.screenX = screenX;
            this.screenY = screenY;
            this.x = x;
            this.y = y;
            this.pressure = pressure;
            this.buttonPressed = buttonPressed;
        }

        @Override
        public String toString() {
            return "[" + x + "," + y + "]";
        }
    }
}
