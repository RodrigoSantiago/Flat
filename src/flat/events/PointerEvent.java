package flat.events;

import flat.widget.Widget;

public class PointerEvent extends Event {
    public static final int PRESSED     = 14;
    public static final int RELEASED    = 15;
    public static final int DRAGGED     = 16;
    public static final int MOVED       = 17;
    public static final int ENTERED     = 18;
    public static final int EXITED      = 19;

    private final PointerData[] pointers;
    private final byte pointerIndex;
    private final byte mouseButton;

    public PointerEvent(Widget source, int type, int pointerIndex, PointerData... pointers) {
        super(source, type);
        this.pointerIndex = (byte) pointerIndex;
        this.pointers = pointers;
        this.mouseButton = -1;
    }

    public PointerEvent(Widget source, int type, int mouseButton, float x, float y) {
        super(source, type);
        this.pointerIndex = 0;
        this.pointers = new PointerData[]{new PointerData(-1, x, y, 0.5f, false)};
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

    public float getX() {
        return getX(pointerIndex);
    }

    public float getY() {
        return getY(pointerIndex);
    }

    public float getPressure() {
        return getPressure(pointerIndex);
    }

    public boolean isButtonPressed() {
        return isButtonPressed(pointerIndex);
    }

    public int getId(int pointer) {
        return pointers[pointer].id;
    }

    public float getX(int pointer) {
        return pointers[pointer].x;
    }

    public float getY(int pointer) {
        return pointers[pointer].y;
    }

    public float getPressure(int pointer) {
        return pointers[pointer].pressure;
    }

    public boolean isButtonPressed(int pointer) {
        return pointers[pointer].buttonPressed;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("PointerEvent ");
        if (getType() == PRESSED) s.append("[PRESSED]");
        else if (getType() == RELEASED) s.append("[RELEASED]");
        else if (getType() == DRAGGED) s.append("[DRAGGED]");
        else if (getType() == MOVED) s.append("[MOVED]");
        else if (getType() == ENTERED) s.append("[ENTERED]");
        else if (getType() == EXITED) s.append("[EXITED]");
        for (PointerData pointer : pointers) {
            s.append(pointer);
        }
        return s.toString();
    }

    public static class PointerData {
        int id;
        float x, y;
        float pressure;
        boolean buttonPressed;

        public PointerData(int id, float x, float y, float pressure, boolean buttonPressed) {
            set(id, x, y, pressure, buttonPressed);
        }

        void set(int id, float x, float y, float pressure, boolean buttonPressed) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.pressure = pressure;
            this.buttonPressed = buttonPressed;
        }

        @Override
        public String toString() {
            return "[" + x + ", " + y + "]";
        }
    }
}
