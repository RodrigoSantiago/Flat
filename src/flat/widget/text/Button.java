package flat.widget.text;

import flat.animations.ElevationAnimation;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.uxml.UXAttributes;
import flat.uxml.data.Dimension;

public class Button extends Label {

    private ActionListener actionListener;
    private boolean elevationEffect;

    private ElevationAnimation anim;
    private boolean mousePress, mouseIn;

    @Override
    public void applyAttributes(Object controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        float dp2 = Dimension.dpPx(2);
        float dp8 = Dimension.dpPx(8);
        float dp16 = Dimension.dpPx(16);

        setMinWidth(attributes.asSize("minWidth", Dimension.dpPx(88)));
        setMinHeight(attributes.asSize("minHeight", Dimension.dpPx(36)));

        setShadowEffectEnabled(attributes.asBoolean("shadowEffect", true));
        setRippleEffectEnabled(attributes.asBoolean("rippleEffect", true));

        setPadding(attributes.asSize("paddingTop", dp8), attributes.asSize("paddingRight", dp16),
                attributes.asSize("paddingBottom", dp8), attributes.asSize("paddingLeft", dp16));

        setBackgroundColor(attributes.asColor("backgroundColor", 0xFFFFFFFF));
        setBackgroundCorners(
                attributes.asNumber("backgroundCornerTop", dp2), attributes.asNumber("backgroundCornerRight", dp2),
                attributes.asNumber("backgroundCornerBottom", dp2),attributes.asNumber("backgroundCornerLeft", dp2));

        setElevationEffectEnabled(attributes.asBoolean("elevationEffect", true));

        setFont(attributes.asFont("font", Font.DEFAULT));
        setFontSize(attributes.asSize("fontSize", Dimension.ptPx(13)));
        setTextAllCaps(attributes.asBoolean("textAllCaps", true));

        setVerticalAlign(attributes.asConstant("verticalAlign", Align.Vertical.class, Align.Vertical.MIDDLE));
        setHorizontalAlign(attributes.asConstant("horizontalAlign", Align.Horizontal.class, Align.Horizontal.CENTER));
    }

    public boolean isElevationEffectEnabled() {
        return elevationEffect;
    }

    public void setElevationEffectEnabled(boolean enabled) {
        if (!this.elevationEffect == enabled) {
            this.elevationEffect = enabled;
            invalidate(false);
            animeElevation(Dimension.dpPx(mousePress ? 8 : mouseIn ? 2 : 1));
        }
    }

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
            animeElevation(Dimension.dpPx(8));
        }
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            mousePress = false;
            animeElevation(Dimension.dpPx(mouseIn ? 2 : 1));
        }
        super.firePointer(pointerEvent);
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        if (hoverEvent.getType() == HoverEvent.ENTERED) {
            mouseIn = true;
            animeElevation(Dimension.dpPx(2));
        }
        if (hoverEvent.getType() == HoverEvent.EXITED) {
            mouseIn = false;
            animeElevation(Dimension.dpPx(1));
        }
        super.fireHover(hoverEvent);
    }

    private void animeElevation(float elevation) {
        if (anim == null) anim = new ElevationAnimation(this);
        anim.stop();
        if (elevationEffect) {
            anim.setDuration(200);
            anim.setFromElevation(getElevation());
            anim.setToElevation(elevation);
            anim.play();
        }
    }
}
