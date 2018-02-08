package flat.widget.text;

import flat.animations.ElevationAnimation;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.uxml.data.Dimension;

public class Button extends Label {

    ActionListener actionListener;
    ElevationAnimation anim = new ElevationAnimation(this);
    private boolean mousePress, mouseIn;

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void fireAction(ActionEvent event) {
        if (actionListener != null) {
            actionListener.handle(event);
        }
    }

    public void fire() {
        fireAction(new ActionEvent(this, ActionEvent.ACTION));
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        if (pointerEvent.getType() == PointerEvent.PRESSED) {
            mousePress = true;
            animeElevation(Dimension.DP(8));
        }
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            mousePress = false;
            animeElevation(Dimension.DP(mouseIn ? 2 : 1));
        }
        super.firePointer(pointerEvent);
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        if (hoverEvent.getType() == HoverEvent.ENTERED) {
            mouseIn = true;
            animeElevation(Dimension.DP(2));
        }
        if (hoverEvent.getType() == HoverEvent.EXITED) {
            mouseIn = false;
            animeElevation(Dimension.DP(1));
        }
        super.fireHover(hoverEvent);
    }

    private void animeElevation(float elevation) {
        anim.stop();
        anim.setDuration(200);
        anim.setFromElevation(getElevation());
        anim.setToElevation(elevation);
        anim.play();
    }
}
