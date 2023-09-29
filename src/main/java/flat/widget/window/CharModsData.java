package flat.widget.window;

import flat.backend.WLEnums;
import flat.events.KeyEvent;
import flat.widget.Activity;
import flat.widget.Widget;
import flat.widget.Window;

import java.util.ArrayList;

public class CharModsData extends EventData {
    private static final ArrayList<CharModsData> list = new ArrayList<>();

    static CharModsData get(int codepoint, int mods) {
        CharModsData data = list.size() > 0 ? list.remove(list.size() - 1) : new CharModsData();
        data.set(codepoint, mods);
        return data;
    }

    private int codepoint, mods;

    private CharModsData() {

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
    void handle(Window window) {
        Activity activity = window.getActivity();
        Widget widget = activity.getFocus();
        if (widget != null) {

            boolean shift = (mods & (WLEnums.MOD_SHIFT)) != 0;
            boolean ctrl = (mods & (WLEnums.MOD_CONTROL)) != 0;
            boolean alt = (mods & (WLEnums.MOD_ALT)) != 0;
            boolean spr = (mods & (WLEnums.MOD_SUPER)) != 0;

            String value = new String(Character.toChars(codepoint));
            widget.fireKey(new KeyEvent(widget, KeyEvent.TYPED, shift, ctrl, alt, spr, value, -1));
        }

        release();
    }
}
