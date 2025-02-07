package flat.widget.selection;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.SelectionState;

public class CheckBox extends Widget {

    private UXListener<ActionEvent> actionListener;
    private SelectionState selectionState = SelectionState.INDETERMINATE;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private Drawable iconInactive;
    private Drawable iconActive;
    private Drawable iconIdeterminate;
    private Drawable currentIcon;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setActionListener(attrs.getAttributeListener("on-action", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIconInactive(attrs.getResourceAsDrawable("icon-inactive", info, getIconInactive(), false));
        setIconActive(attrs.getResourceAsDrawable("icon-active", info, getIconActive(), false));
        setIconIdeterminate(attrs.getResourceAsDrawable("icon-indeterminate", info, getIconIdeterminate(), false));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(context);
        context.setTransform2D(getTransform());

        if (currentIcon == null) return;

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        float icoWidth = Math.min(currentIcon.getWidth(), width);
        float icoHeight = Math.min(currentIcon.getHeight(), height);

        context.setColor(0xFF0000FF);
        currentIcon.draw(context
                , (x + width - icoWidth) * 0.5f
                , (y + height - icoHeight) * 0.5f
                , width, height, 0, iconImageFilter);
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getPrefHeight() == WRAP_CONTENT;

        float iW = currentIcon == null ? 0 : currentIcon.getWidth();
        float iH = currentIcon == null ? 0 : currentIcon.getHeight();

        if (wrapWidth) {
            mWidth = Math.max(iW + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(iH + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            fire();
        }
    }

    private void setCurrentIcon() {
        Drawable nextIcon = isActive() ? iconActive : isIndeterminate() ? iconIdeterminate : iconInactive;
        if (nextIcon == null) {
            nextIcon = iconInactive;
        }
        if (currentIcon == nextIcon) {
            return;
        }

        float width = currentIcon == null ? 0 : currentIcon.getWidth();
        float height = currentIcon == null ? 0 : currentIcon.getHeight();
        float nextWidth = nextIcon == null ? 0 : nextIcon.getWidth();
        float nextHeight = nextIcon == null ? 0 : nextIcon.getHeight();
        currentIcon = nextIcon;

        invalidate(width != nextWidth || height != nextHeight);
    }

    public SelectionState getSelectionState() {
        return selectionState;
    }

    public void setSelectionState(SelectionState selectionState) {
        if (selectionState == null) selectionState = SelectionState.INDETERMINATE;

        if (this.selectionState != selectionState) {
            this.selectionState = selectionState;

            setCurrentIcon();
        }
    }

    public void setIconImageFilter(ImageFilter iconImageFilter) {
        if (iconImageFilter == null) iconImageFilter = ImageFilter.NEAREST;

        if (this.iconImageFilter != iconImageFilter) {
            this.iconImageFilter = iconImageFilter;
            invalidate(false);
        }
    }

    public Drawable getIconInactive() {
        return iconInactive;
    }

    public void setIconInactive(Drawable iconInactive) {
        if (this.iconInactive != iconInactive) {
            this.iconInactive = iconInactive;

            setCurrentIcon();
        }
    }

    public Drawable getIconActive() {
        return iconActive;
    }

    public void setIconActive(Drawable iconActive) {
        if (this.iconActive != iconActive) {
            this.iconActive = iconActive;

            setCurrentIcon();
        }
    }

    public Drawable getIconIdeterminate() {
        return iconIdeterminate;
    }

    public void setIconIdeterminate(Drawable iconIdeterminate) {
        if (this.iconIdeterminate != iconIdeterminate) {
            this.iconIdeterminate = iconIdeterminate;

            setCurrentIcon();
        }
    }

    public boolean isInactive() {
        return getSelectionState() == SelectionState.INACTIVE;
    }

    public boolean isActive() {
        return getSelectionState() == SelectionState.ACTIVE;
    }

    public boolean isIndeterminate() {
        return getSelectionState() == SelectionState.INDETERMINATE;
    }

    public UXListener<ActionEvent> getActionListener() {
        return actionListener;
    }

    public void setActionListener(UXListener<ActionEvent> actionListener) {
        this.actionListener = actionListener;
    }

    public void fireAction(ActionEvent event) {
        if (actionListener != null) {
            actionListener.handle(event);
        }
    }

    public void fire() {
        setSelectionState(isActive() ? SelectionState.INACTIVE : SelectionState.ACTIVE);
        fireAction(new ActionEvent(this));
    }
}
