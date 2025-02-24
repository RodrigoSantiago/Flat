package flat.window.event;

import flat.backend.WLEnums;
import flat.events.KeyEvent;
import flat.window.Activity;
import flat.widget.Widget;
import flat.window.Application;
import flat.window.Window;

import java.util.ArrayList;

public class EventDataCharMods extends EventData {
    private static final ArrayList<EventDataCharMods> list = new ArrayList<>();

    static EventDataCharMods get(int codepoint, int mods) {
        EventDataCharMods data = list.size() > 0 ? list.remove(list.size() - 1) : new EventDataCharMods();
        data.set(codepoint, mods);
        return data;
    }

    private int codepoint, mods;

    private EventDataCharMods() {

    }

    private void set(int codepoint, int mods) {
        this.codepoint = codepoint;
        this.mods = mods;
    }

    private void release() {
        set(0, 0);
        list.add(this);
    }

    @Override
    public void handle(Window window) {
        try {
            Activity activity = window.getActivity();
            Widget widget = activity.getKeyFocus();
            if (widget != null) {

                boolean shift = (mods & (WLEnums.MOD_SHIFT)) != 0;
                boolean ctrl = (mods & (WLEnums.MOD_CONTROL)) != 0;
                boolean alt = (mods & (WLEnums.MOD_ALT)) != 0;
                boolean spr = (mods & (WLEnums.MOD_SUPER)) != 0;

                String value = new String(Character.toChars(codepoint));
                widget.fireKey(new KeyEvent(widget, KeyEvent.TYPED, shift, ctrl, alt, spr, value, codepoint));
            }
        } finally {
            release();
        }
    }
}
