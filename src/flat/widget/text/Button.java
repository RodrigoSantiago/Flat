package flat.widget.text;

import flat.animations.ElevationAnimation;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.uxml.data.Dimension;

import java.lang.reflect.Method;
import java.util.HashMap;

public class Button extends Label {

    public static final int FLAT = 0;
    public static final int RAISED = 1;
    public static final int FLOAT = 2;

    private static final HashMap<String, Integer> styles = UXAttributes.atts(
            "FLAT", FLAT,
            "RAISED", RAISED,
            "FLOAT", FLOAT
    );

    private ActionListener actionListener;

    private int style, elevationState;
    private boolean elevationEffect;

    private ElevationAnimation anim;
    private boolean mouseIn;

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
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

        setVerticalAlign(attributes.asEnum("verticalAlign", Align.Vertical.class, Align.Vertical.MIDDLE));
        setHorizontalAlign(attributes.asEnum("horizontalAlign", Align.Horizontal.class, Align.Horizontal.CENTER));

        Method handle = attributes.asListener("onAction", ActionEvent.class, controller);
        if (handle != null) {
            setActionListener(new ActionListener.AutoActionListener(controller, handle));
        }

        setStyle(attributes.asConstant("style", styles, 0));
    }

    @Override
    public void onDraw(SmartContext context) {
        int bgColor = getBackgroundColor();
        if (style != RAISED && style != FLOAT) {
            setBackgroundColor(0);
        }
        super.onDraw(context);
        if (style != RAISED && style != FLOAT) {
            setBackgroundColor(bgColor);
        }
    }

    public boolean isElevationEffectEnabled() {
        return elevationEffect;
    }

    public void setElevationEffectEnabled(boolean enabled) {
        if (!this.elevationEffect == enabled) {
            this.elevationEffect = enabled;
            animeElevation(elevationState);
            invalidate(false);
        }
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        if (style != this.style) {
            this.style = style;
            animeElevation(elevationState);
            invalidate(false);
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
            animeElevation(2);
        }
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            animeElevation(mouseIn ? 1 : 0);
        }
        super.firePointer(pointerEvent);
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        if (hoverEvent.getType() == HoverEvent.ENTERED) {
            mouseIn = true;
            animeElevation(1);
        }
        if (hoverEvent.getType() == HoverEvent.EXITED) {
            mouseIn = false;
            animeElevation(0);
        }
        super.fireHover(hoverEvent);
    }

    private void animeElevation(int state) {
        elevationState = state;
        float elevation;
        if (state == 0) {           // RELEASED - OUT
            elevation = Dimension.dpPx(style == RAISED ? 1 : style == FLOAT ? 6 : 0);
        } else if (state == 1) {    // RELEASED - IN
            elevation = Dimension.dpPx(style == RAISED ? 2 : style == FLOAT ? 6 : 0);
        } else {                    // PRESSED
            elevation = Dimension.dpPx(style == RAISED ? 8 : style == FLOAT ? 12 : 0);
        }

        if (anim == null) {
            anim = new ElevationAnimation(this);
        }
        anim.stop();
        if (elevationEffect && elevation != getElevation()) {
            anim.setDuration(200);
            anim.setFromElevation(getElevation());
            anim.setToElevation(elevation);
            anim.play();
        }
    }
}
