package flat.widget.bars;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

import java.lang.reflect.Method;
import java.util.Objects;

public class ToolItem extends Widget {

    private ActionListener actionListener;

    private Drawable iconImage;
    private float iconSpacing;

    private Align.Vertical verticalAlign = Align.Vertical.TOP;
    private Align.Horizontal horizontalAlign = Align.Horizontal.LEFT;

    private String text;
    private boolean showAction = true;
    private boolean invalidText = true;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setText(style.asString("text", getText()));
        setShowAction(style.asBool("show-action", isShowAction()));

        Method handle = style.asListener("on-action", ActionEvent.class, controller);
        if (handle != null) {
            setActionListener(new ActionListener.AutoActionListener(controller, handle));
        }
    }

    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        setVerticalAlign(getStyle().asConstant("vertical-align", info, getVerticalAlign()));
        setHorizontalAlign(getStyle().asConstant("horizontal-align", info, getHorizontalAlign()));

        Resource res = getStyle().asResource("icon-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIconImage(drawable);
            }
        }

        setIconSpacing(getStyle().asSize("icon-spacing", info, getIconSpacing()));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        context.setTransform2D(getTransform());
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (iconImage != null) {
            context.setColor(0x000000FF);
            iconImage.draw(context,
                    xOff(x, x + width, iconImage.getWidth()),
                    yOff(y, y + height, iconImage.getHeight()), iconImage.getWidth(), iconImage.getHeight(), 0);
        }
    }

    @Override
    public void onMeasure() {
        if (iconImage == null) {
            super.onMeasure();
        } else {
            float mWidth = getPrefWidth();
            float mHeight = getPrefHeight();
            mWidth = mWidth == WRAP_CONTENT ? iconImage.getWidth() + iconSpacing : mWidth;
            mHeight = mHeight == WRAP_CONTENT ? iconImage.getHeight() : mHeight;
            mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
            mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
            setMeasure(mWidth, mHeight);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            fire();
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
        fireAction(new ActionEvent(this));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            this.invalidText = true;
            invalidate(true);
        }
    }

    public Drawable getIconImage() {
        return iconImage;
    }

    public void setIconImage(Drawable iconImage) {
        if (this.iconImage != iconImage) {
            this.iconImage = iconImage;
            invalidate(true);
        }
    }

    public float getIconSpacing() {
        return iconSpacing;
    }

    public void setIconSpacing(float iconSpacing) {
        if (this.iconSpacing != iconSpacing) {
            this.iconSpacing = iconSpacing;
            invalidate(true);
        }
    }

    public boolean isShowAction() {
        return showAction;
    }

    public void setShowAction(boolean showAction) {
        if (this.showAction != showAction) {
            this.showAction = showAction;
            invalidate(true);
        }
    }

    protected boolean isTextIvalided() {
        boolean ti = invalidText;
        invalidText = false;
        return ti;
    }

    public Align.Vertical getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(Align.Vertical verticalAlign) {
        if (verticalAlign == null) verticalAlign = Align.Vertical.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(false);
        }
    }

    public Align.Horizontal getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(Align.Horizontal horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = Align.Horizontal.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(false);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        Vector2 p = localToScreen(getInX() + getInWidth() / 2, getInY() + getHeight() / 2);
        getRipple().setSize(getInWidth());
        super.fireRipple(p.x, p.y);
    }

    protected float xOff(float start, float end, float textWidth) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == Align.Horizontal.RIGHT) return end - textWidth;
        if (horizontalAlign == Align.Horizontal.CENTER) return (start + end - textWidth) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float textHeight) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == Align.Vertical.BOTTOM || verticalAlign == Align.Vertical.BASELINE) return end - textHeight;
        if (verticalAlign == Align.Vertical.MIDDLE) return (start + end - textHeight) / 2f;
        return start;
    }
}
