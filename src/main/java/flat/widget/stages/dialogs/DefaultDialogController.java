package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.PointerEvent;
import flat.uxml.Controller;
import flat.widget.stages.Dialog;

public class DefaultDialogController extends Controller {

    protected final Dialog dialog;

    private float startX, startY;
    private float dragX, dragY;
    private boolean pressed;

    public DefaultDialogController(Dialog dialog) {
        this.dialog = dialog;
    }

    @Flat
    public void onHeaderPointer(PointerEvent event) {
        if (event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            pressed = true;
            startX = dialog.getMoveCenterX();
            startY = dialog.getMoveCenterY();
            dragX = event.getX();
            dragY = event.getY();
        }
        if (pressed && event.getType() == PointerEvent.DRAGGED) {
            dialog.moveTo(startX + (event.getX() - dragX), startY + (event.getY() - dragY));
        }
        if (event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            pressed = false;
        }
    }
}
