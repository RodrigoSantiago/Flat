package flat.widget.dialogs;

import flat.events.PointerEvent;
import flat.widget.Menu;

public class DropdownMenu extends Menu {
    DropdownListener listener;

    public void setListener(DropdownListener listener) {
        this.listener = listener;
    }

    public DropdownListener getListener() {
        return listener;
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (pointerEvent.getSource() == getChooseItem() && pointerEvent.getType() == PointerEvent.RELEASED) {
            if (listener != null && getChooseItem().getSubMenu() == null) {
                listener.onItemSelected(getChooseItem());
            }
        }
    }
}
