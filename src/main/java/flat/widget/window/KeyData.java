package flat.widget.window;

import flat.backend.WLEnums;
import flat.events.KeyEvent;
import flat.widget.Activity;
import flat.widget.Widget;
import flat.widget.Window;

import java.util.ArrayList;

public class KeyData extends EventData {
    private static final ArrayList<KeyData> list = new ArrayList<>();

    static KeyData get(int key, int scancode, int action, int mods) {
        KeyData data = list.size() > 0 ? list.remove(list.size() - 1) : new KeyData();
        data.set(key, scancode, action, mods);
        return data;
    }

    private int key, scancode, action, mods;

    private KeyData() {

    }

    private void set(int key, int scancode, int action, int mods) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.mods = mods;
    }

    private void release() {
        set(0, 0, 0, 0);
        list.add(this);
    }

    @Override
    void handle(Window window) {
        Activity activity = window.getActivity();
        Widget widget = activity.getFocus();
        if (widget != null) {
            int eventType =
                    (action == WLEnums.PRESS) ? KeyEvent.PRESSED :
                    (action == WLEnums.RELEASE) ? KeyEvent.RELEASED : KeyEvent.REPEATED;

            boolean shift = (mods & (WLEnums.MOD_SHIFT)) != 0;
            boolean ctrl = (mods & (WLEnums.MOD_CONTROL)) != 0;
            boolean alt = (mods & (WLEnums.MOD_ALT)) != 0;
            boolean spr = (mods & (WLEnums.MOD_SUPER)) != 0;

            KeyEvent keyEvent = new KeyEvent(widget, eventType, shift, ctrl, alt, spr, "", key);
            widget.fireKey(keyEvent);

            if (!keyEvent.isConsumed()) {
                activity.onKeyPress(keyEvent);
            }
        }

        release();
    }
}
