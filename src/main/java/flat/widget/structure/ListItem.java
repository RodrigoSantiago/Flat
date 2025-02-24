package flat.widget.structure;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.Drawable;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.stroke.BasicStroke;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.ImageFilter;
import flat.widget.text.Button;

public class ListItem extends Button {

    private UXListener<ActionEvent> changeStateListener;

    private boolean open;
    private Drawable stateIconOpen;
    private Drawable stateIconClosed;
    private int stateIconColor = Color.white;
    private ImageFilter stateIconImageFilter = ImageFilter.LINEAR;
    private float stateIconSpacing;
    private float stateIconWidth;
    private float stateIconHeight;

    private int layers;
    private float layerWidth = 8f;
    private int layerLineColor = Color.black;
    private float layerLineWidth = 1f;

    private boolean isHoveringState;
    private Cursor stateIconCursor = Cursor.UNSET;

    private int index = -1;

    private float x1, y1, x2, y2;

    @Override
    public float getLayoutMinWidth() {
        return super.getLayoutMinWidth() + (getLayers() * getLayerWidth());
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setChangeStateListener(attrs.getAttributeListener("on-change-state", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();


        setLayerLineColor(attrs.getColor("layer-line-color", info, getLayerLineColor()));
        setLayerLineWidth(attrs.getSize("layer-line-width", info, getLayerLineWidth()));
        setLayerWidth(attrs.getSize("layer-width", info, getLayerWidth()));

        setStateIconOpen(attrs.getResourceAsDrawable("state-icon-open", info, getStateIconOpen(), false));
        setStateIconClosed(attrs.getResourceAsDrawable("state-icon-closed", info, getStateIconClosed(), false));
        setStateIconColor(attrs.getColor("state-icon-color", info, getStateIconColor()));
        setStateIconWidth(attrs.getSize("state-icon-width", info, getStateIconWidth()));
        setStateIconHeight(attrs.getSize("state-icon-height", info, getStateIconHeight()));
        setStateIconSpacing(attrs.getSize("state-icon-spacing", info, getStateIconSpacing()));
        setStateIconImageFilter(attrs.getConstant("state-icon-image-filter", info, getStateIconImageFilter()));
        setStateIconCursor(attrs.getConstant("state-icon-cursor", info, getStateIconCursor()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();
        float siW = getLayoutStateIconWidth();
        float siH = getLayoutStateIconHeight();

        float layerWidth = (getLayers() * getLayerWidth());
        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth
                    + (iW > 0 ? iW + getIconSpacing() : 0)
                    + (siW > 0 ? siW + getStateIconSpacing() : 0) + layerWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth() + layerWidth, getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(Math.max(getTextHeight(), Math.max(iH, siH)) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        updateStatePosition();
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        context.setTransform2D(getTransform());
        context.setStroker(new BasicStroke(getLayerLineWidth()));
        context.setColor(getLayerLineColor());

        float inX = x;
        float lw = getLayerWidth();
        for (int i = 0; i < getLayers(); i++) {
            if (inX + lw + 0.5f > width) {
                return;
            }
            float xpos = Mathf.floor(inX + lw * 0.5f) + 0.5f;
            context.drawLine(xpos, getLayoutHeight() * -0.25f, xpos, getLayoutHeight() * 0.75f);
            inX += lw;
        }

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();
        float siW = getLayoutStateIconWidth();
        float siH = getLayoutStateIconHeight();

        Drawable stateIcon = isOpen() ? getStateIconOpen() : getStateIconClosed();

        float space = Math.max(0, width - (inX - x));
        float spaceForStateIcon = siW > 0 ? siW + getStateIconSpacing() : 0;
        float spaceForIcon = iW > 0 ? iW + getIconSpacing() : 0;
        float spaceForText = getTextWidth();

        if (spaceForIcon + spaceForStateIcon + spaceForText > space) {
            if (spaceForStateIcon > space) {
                spaceForIcon = 0;
                spaceForText = 0;
                spaceForStateIcon = space;
            } else if (spaceForIcon + spaceForStateIcon > space) {
                spaceForText = 0;
                spaceForIcon = space - spaceForStateIcon;
            } else {
                spaceForText = space - spaceForStateIcon - spaceForIcon;
            }
        }

        float tw = spaceForText;
        float th = Math.min(height, getTextHeight());
        float iw = Math.min(iW, Math.max(spaceForIcon, 0));
        float ih = Math.min(height, iH);
        float siw = Math.min(siW, Math.max(spaceForStateIcon, 0));
        float sih = Math.min(height, siH);

        if (siw > 0 && sih > 0 && stateIcon != null) {
            stateIcon.draw(context
                    , inX
                    , yOff(y, y + height, sih)
                    , siw, sih, getStateIconColor(), getStateIconImageFilter());
        }

        if (iw > 0 && ih > 0 && getIcon() != null) {
            getIcon().draw(context
                    , inX + spaceForStateIcon
                    , yOff(y, y + height, ih)
                    , iw, ih, getIconColor(), getIconImageFilter());
        }

        if (tw > 0 && th > 0) {
            drawText(context, inX + spaceForStateIcon + spaceForIcon, yOff(y, y + height, th), tw, th);
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        if (this.open != open) {
            this.open = open;
            invalidate(false);
        }
    }

    public int getLayers() {
        return layers;
    }

    public void setLayers(int layers) {
        if (this.layers != layers) {
            this.layers = layers;
            invalidate(true);
        }
    }

    public float getLayerWidth() {
        return layerWidth;
    }

    public void setLayerWidth(float layerWidth) {
        if (this.layerWidth != layerWidth) {
            this.layerWidth = layerWidth;
            invalidate(true);
        }
    }

    public int getLayerLineColor() {
        return layerLineColor;
    }

    public void setLayerLineColor(int layerLineColor) {
        if (this.layerLineColor != layerLineColor) {
            this.layerLineColor = layerLineColor;
            invalidate(false);
        }
    }

    public float getLayerLineWidth() {
        return layerLineWidth;
    }

    public void setLayerLineWidth(float layerLineWidth) {
        if (this.layerLineWidth != layerLineWidth) {
            this.layerLineWidth = layerLineWidth;
            invalidate(false);
        }
    }

    public Drawable getStateIconOpen() {
        return stateIconOpen;
    }

    public void setStateIconOpen(Drawable stateIconOpen) {
        if (this.stateIconOpen != stateIconOpen) {
            this.stateIconOpen = stateIconOpen;
            invalidate(isWrapContent());
        }
    }

    public Drawable getStateIconClosed() {
        return stateIconClosed;
    }

    public void setStateIconClosed(Drawable stateIconClosed) {
        if (this.stateIconClosed != stateIconClosed) {
            this.stateIconClosed = stateIconClosed;
            invalidate(isWrapContent());
        }
    }

    public ImageFilter getStateIconImageFilter() {
        return stateIconImageFilter;
    }

    public void setStateIconImageFilter(ImageFilter stateIconImageFilter) {
        if (stateIconImageFilter == null) stateIconImageFilter = ImageFilter.LINEAR;

        if (this.stateIconImageFilter != stateIconImageFilter) {
            this.stateIconImageFilter = stateIconImageFilter;
            invalidate(false);
        }
    }

    public int getStateIconColor() {
        return stateIconColor;
    }

    public void setStateIconColor(int stateIconColor) {
        if (this.stateIconColor != stateIconColor) {
            this.stateIconColor = stateIconColor;
            invalidate(false);
        }
    }

    public float getStateIconSpacing() {
        return stateIconSpacing;
    }

    public void setStateIconSpacing(float stateIconSpacing) {
        if (this.stateIconSpacing != stateIconSpacing) {
            this.stateIconSpacing = stateIconSpacing;
            invalidate(isWrapContent());
        }
    }

    public float getStateIconWidth() {
        return stateIconWidth;
    }

    public void setStateIconWidth(float stateIconWidth) {
        if (this.stateIconWidth != stateIconWidth) {
            this.stateIconWidth = stateIconWidth;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutStateIconWidth() {
        if (stateIconOpen == null && stateIconClosed == null) {
            return 0;
        }
        return stateIconWidth == 0 || stateIconWidth == MATCH_PARENT ? getTextHeight() : stateIconWidth;
    }

    public float getStateIconHeight() {
        return stateIconHeight;
    }

    public void setStateIconHeight(float stateIconHeight) {
        if (this.stateIconHeight != stateIconHeight) {
            this.stateIconHeight = stateIconHeight;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutStateIconHeight() {
        if (stateIconOpen == null && stateIconClosed == null) {
            return 0;
        }
        return stateIconHeight == 0 || stateIconHeight == MATCH_PARENT ? getTextHeight() : stateIconHeight;
    }

    public Cursor getStateIconCursor() {
        return stateIconCursor;
    }

    public void setStateIconCursor(Cursor stateIconCursor) {
        if (stateIconCursor == null) stateIconCursor = Cursor.UNSET;
        this.stateIconCursor = stateIconCursor;
    }

    public UXListener<ActionEvent> getChangeStateListener() {
        return changeStateListener;
    }

    public void setChangeStateListener(UXListener<ActionEvent> changeStateListener) {
        this.changeStateListener = changeStateListener;
    }

    public void changeStateAction() {
        fireChangeStateAction();
    }

    private void fireChangeStateAction() {
        if (changeStateListener != null) {
            UXListener.safeHandle(changeStateListener, new ActionEvent(this));
        }
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (!event.isConsumed() && event.getType() == PointerEvent.RELEASED && event.getPointerID() == 1) {
            if (isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
                changeStateAction();
            } else {
                action();
            }
        }
    }

    @Override
    public void fireHover(HoverEvent event) {
        super.fireHover(event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            isHoveringState = isOverActionButton(screenToLocal(event.getX(), event.getY()));
        }
    }

    @Override
    public Cursor getCurrentCursor() {
        return isHoveringState && stateIconCursor != Cursor.UNSET ? stateIconCursor : super.getCursor();
    }

    private boolean isOverActionButton(Vector2 local) {
        return !(local.x < x1 || local.x > x2 || local.y < y1 || local.y > y2);
    }

    private void updateStatePosition() {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        float iaw = Math.min(width, getLayoutStateIconWidth());
        float iah = Math.min(height, getLayoutStateIconHeight());

        x1 = x + getLayers() * getLayerWidth();
        x2 = x + getLayers() * getLayerWidth() + iaw;
        y1 = yOff(y, y + height, iah);
        y2 = yOff(y, y + height, iah) + iah;
    }

    protected float yOff(float start, float end, float size) {
        return (start + end - size) / 2f;
    }
}
