package flat.widget.text;

import flat.animations.ElevationAnimation;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.image.Image;
import flat.graphics.text.Align;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.resources.Dimension;
import flat.widget.enuns.ElevateStyle;

import java.lang.reflect.Method;

public class Button extends Label {

    private ActionListener actionListener;

    private boolean elevationEffect;
    private ElevateStyle elevateStyle;
    private Image image;
    private float imageMargin;
    private Align.Horizontal imageAlign;

    private int elevationState;
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

        setElevateStyle(attributes.asEnum("elevateStyle", ElevateStyle.class, ElevateStyle.FLAT));

        setImage(attributes.asImage("image"));
        setImageAlign(attributes.asEnum("imageAlign", Align.Horizontal.class, Align.Horizontal.LEFT));
        setImageMargin(attributes.asSize("imageMargin", 8));
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

    public boolean isElevationEffectEnabled() {
        return elevationEffect;
    }

    public void setElevationEffectEnabled(boolean enabled) {
        if (!this.elevationEffect == enabled) {
            this.elevationEffect = enabled;
            setElevationState(elevationState);
            invalidate(false);
        }
    }

    public ElevateStyle getElevateStyle() {
        return elevateStyle;
    }

    public void setElevateStyle(ElevateStyle elevateStyle) {
        if (elevateStyle != this.elevateStyle) {
            this.elevateStyle = elevateStyle;
            setElevationState(elevationState);
            invalidate(false);
        }
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public float getImageMargin() {
        return imageMargin;
    }

    public void setImageMargin(float imageMargin) {
        this.imageMargin = imageMargin;
    }

    public Align.Horizontal getImageAlign() {
        return imageAlign;
    }

    public void setImageAlign(Align.Horizontal imageAlign) {
        this.imageAlign = imageAlign;
    }

    @Override
    public void onDraw(SmartContext context) {
        if (image == null) {
            super.onDraw(context);
        } else {
            backgroundDraw(context);
            context.setTransform2D(getTransformView());

            final float x = getInX();
            final float y = getInY();
            final float width = getInWidth();
            final float height = getInHeight();

            if (getShowText() != null && !getShowText().isEmpty()) {
                context.setColor(getTextColor());
                context.setTextFont(getFont());
                context.setTextSize(getFontSize());
                context.setTextVerticalAlign(Align.Vertical.TOP);
                context.setTextHorizontalAlign(Align.Horizontal.LEFT);

                float xoff = xOff(x, x + width, Math.min(getTextWidth() + imageMargin + image.getWidth(), width));

                if (imageAlign == Align.Horizontal.RIGHT) {
                    context.drawTextSlice(xoff,
                            yOff(y, y + height, Math.min(getFontSize(), height)),
                            width - imageMargin - image.getWidth(), getShowText());
                    xoff += width - image.getWidth();
                    image.draw(context, xoff,
                            yOff(y, y + height, image.getHeight()),
                            image.getWidth(), image.getHeight(), 0);
                } else {
                    image.draw(context, xoff,
                            yOff(y, y + height, image.getHeight()),
                            image.getWidth(), image.getHeight(), 0);
                    xoff += image.getWidth() + imageMargin;
                    context.drawTextSlice(xoff,
                            yOff(y, y + height, Math.min(getFontSize(), height)),
                            width - imageMargin - image.getWidth(), getShowText());
                }
            } else {
                image.draw(context,
                        xOff(x, x + width, image.getWidth()),
                        yOff(y, y + height, image.getHeight()), image.getWidth(), image.getHeight(), 0);
            }
        }
    }

    @Override
    public void onMeasure() {
        if (image == null) {
            super.onMeasure();
        } else {
            float mWidth = getPrefWidth();
            float mHeight = getPrefHeight();
            mWidth = mWidth == WRAP_CONTENT ? getTextWidth() + image.getWidth() + imageMargin : mWidth;
            mHeight = mHeight == WRAP_CONTENT ? Math.max(getFontSize(), image.getHeight()) : mHeight;
            mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
            mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
            setMeasure(mWidth, mHeight);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        if (pointerEvent.getType() == PointerEvent.PRESSED) {
            setElevationState(2);
        }
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            setElevationState(mouseIn ? 1 : 0);
        }
        super.firePointer(pointerEvent);
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        if (hoverEvent.getType() == HoverEvent.ENTERED) {
            mouseIn = true;
            setElevationState(1);
        }
        if (hoverEvent.getType() == HoverEvent.EXITED) {
            mouseIn = false;
            setElevationState(0);
        }
        super.fireHover(hoverEvent);
    }

    private void setElevationState(int state) {
        elevationState = state;
        float elevation;
        if (state == 0) {           // RELEASED - OUT
            elevation = Dimension.dpPx(elevateStyle == ElevateStyle.RAISED ? 1 : elevateStyle == ElevateStyle.FLOAT ? 6 : 0);
        } else if (state == 1) {    // RELEASED - IN
            elevation = Dimension.dpPx(elevateStyle == ElevateStyle.RAISED ? 2 : elevateStyle == ElevateStyle.FLOAT ? 6 : 0);
        } else {                    // PRESSED
            elevation = Dimension.dpPx(elevateStyle == ElevateStyle.RAISED ? 8 : elevateStyle == ElevateStyle.FLOAT ? 12 : 0);
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
