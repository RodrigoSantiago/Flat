package flat.window.event;

import flat.backend.WLEnums;
import flat.events.EventType;
import flat.events.KeyEvent;
import flat.window.Activity;
import flat.widget.Widget;
import flat.window.Application;
import flat.window.Window;

import java.util.ArrayList;

public class EventDataKey extends EventData {
    private static final ArrayList<EventDataKey> list = new ArrayList<>();

    static EventDataKey get(int key, int scancode, int action, int mods) {
        EventDataKey data = list.size() > 0 ? list.remove(list.size() - 1) : new EventDataKey();
        data.set(key, scancode, action, mods);
        return data;
    }

    private int key, scancode, action, mods;

    private EventDataKey() {

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
    public void handle(Window window) {
        try {
            Activity activity = window.getActivity();
            Widget widget = activity.getKeyFocus();
            if (widget != null) {
                KeyEvent.Type eventType =
                        (action == WLEnums.PRESS) ? KeyEvent.PRESSED :
                                (action == WLEnums.RELEASE) ? KeyEvent.RELEASED : KeyEvent.REPEATED;

                boolean shift = (mods & (WLEnums.MOD_SHIFT)) != 0;
                boolean ctrl = (mods & (WLEnums.MOD_CONTROL)) != 0;
                boolean alt = (mods & (WLEnums.MOD_ALT)) != 0;
                boolean spr = (mods & (WLEnums.MOD_SUPER)) != 0;

                window.setMods(shift, ctrl, alt, spr);

                if (eventType == KeyEvent.PRESSED || eventType == KeyEvent.REPEATED) {
                    KeyEvent event = new KeyEvent(widget, KeyEvent.FILTER, shift, ctrl, alt, spr, "", key);
                    activity.onKeyFilter(event);
                    if (event.isConsumed()) {
                        return;
                    }
                }

                KeyEvent keyEvent = new KeyEvent(widget, eventType, shift, ctrl, alt, spr, "", key);
                widget.fireKey(keyEvent);
            }
        } finally {
            release();
        }
    }
}
