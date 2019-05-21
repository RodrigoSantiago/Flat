package flat.widget.bars;

import flat.events.PointerEvent;
import flat.math.Vector2;
import flat.uxml.UXStyleAttrs;
import flat.widget.Menu;
import flat.widget.Widget;
import flat.widget.dialogs.MenuItem;
import flat.widget.text.Button;

import java.util.List;

public class OverflowMenu extends Button {

    public void setItems(List<ToolItem> items) {
        Menu menu = getContextMenu();
        if (menu == null) {
            menu = new Menu();
            menu.applyAttributes(new UXStyleAttrs("", getStyle().getTheme().getStyle("menu")), null);
            setContextMenu(menu);
        }

        menu.removeAll();
        for (ToolItem item : items) {
            MenuItem menuItem = new MenuItem();
            menuItem.applyAttributes(new UXStyleAttrs("", item.getStyle().getTheme().getStyle("menuitem")), null);
            menuItem.setText(item.getText());
            menu.add(menuItem);
        }
    }

    @Override
    public void showContextMenu(float x, float y) {
        Vector2 p = localToScreen(getOutX() + getOutWidth(), getOutY() + getOutHeight());
        super.showContextMenu(p.x, p.y);
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            if (getContextMenu() != null) {
                showContextMenu(pointerEvent.getX(), pointerEvent.getY());
            }
            if (!pointerEvent.isFocusConsumed() && isFocusable()) {
                pointerEvent.consumeFocus(true);
                requestFocus(true);
            }
        }

        if (getPointerListener() != null) {
            getPointerListener().handle(pointerEvent);
        }
        if (getParent() != null) {
            getParent().firePointer(pointerEvent);
        }
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            fire();
        }
    }
}
