package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.events.TextEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.Drawable;
import flat.math.Vector2;
import flat.math.shapes.RoundRectangle;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.ImageFilter;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DropDown extends TextField {

    private UXListener<ActionEvent> actionListener;
    private UXListener<TextEvent> optionSelectedListener;

    private Drawable actionIcon;
    private int actionIconColor = Color.white;
    private ImageFilter actionIconImageFilter = ImageFilter.LINEAR;
    private float actionIconSpacing;
    private float actionIconWidth;
    private float actionIconHeight;
    private int actionIconBgColor = Color.transparent;
    private Cursor actionIconCursor = Cursor.UNSET;
    private boolean isHoveringAction;

    private boolean invalidSubmenuItems;

    private Menu subMenu;
    private List<String> options = new ArrayList<>();
    private List<String> unmodifiableOptions;
    private List<MenuItem> menuItems = new ArrayList<>();

    private float x1, y1, x2, y2;

    public DropDown() {
        unmodifiableOptions = Collections.unmodifiableList(options);
        updateSubmenu();
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
        String content = attrs.getAttributeString("content", null);
        if (content != null) {
            List<String> opts = new ArrayList<>();
            for (var option : content.trim().split("\n")) {
                option = option.trim();
                if (!option.isEmpty()) {
                    opts.add(option);
                }
            }
            setOptions(opts);
        }

        setActionListener(attrs.getAttributeListener("on-action", ActionEvent.class, controller));
        setOptionSelectedListener(attrs.getAttributeListener("on-option-selected", TextEvent.class, controller));
    }
    
    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setActionIcon(attrs.getResourceAsDrawable("action-icon", info, getActionIcon(), false));
        setActionIconColor(attrs.getColor("action-icon-color", info, getActionIconColor()));
        setActionIconBgColor(attrs.getColor("action-icon-bg-color", info, getActionIconBgColor()));
        setActionIconWidth(attrs.getSize("action-icon-width", info, getActionIconWidth()));
        setActionIconHeight(attrs.getSize("action-icon-height", info, getActionIconHeight()));
        setActionIconSpacing(attrs.getSize("action-icon-spacing", info, getActionIconSpacing()));
        setActionIconImageFilter(attrs.getConstant("action-icon-image-filter", info, getActionIconImageFilter()));
        setActionIconCursor(attrs.getConstant("action-icon-cursor", info, getActionIconCursor()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        if (getHorizontalBar() != null) {
            getHorizontalBar().onMeasure();
        }
        if (getVerticalBar() != null) {
            getVerticalBar().onMeasure();
        }

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;
        float iconWidth = getLayoutActionIconWidth();
        float iconHeight = getLayoutActionIconHeight();
        if (iconWidth > 0) {
            iconWidth += getActionIconSpacing();
        }

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + iconWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
            mHeight = Math.max(Math.max(getTextHeight(), iconHeight) + titleHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public Vector2 onLayoutViewDimension(float width, float height) {
        if (getLayoutActionIconWidth() > 0) {
            Vector2 fromBase = super.onLayoutViewDimension(width, height);
            fromBase.x = Math.max(0, fromBase.x - getLayoutActionIconWidth() - getActionIconSpacing());
            return fromBase;
        }
        return super.onLayoutViewDimension(width, height);
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        updateActionPosition();
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        if (getOutWidth() <= 0 || getOutHeight() <= 0) return;

        float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        float aiw = getLayoutActionIconWidth();
        float aih = getLayoutActionIconHeight();

        if (aiw > 0 && aih > 0 && getActionIcon() != null) {
            float xpos = x + width - Math.min(width, aiw);
            float ypos = yOff(y + titleHeight, y + height, Math.min(height - titleHeight, aih));

            if (isHoveringAction) {
                context.setColor(getActionIconBgColor());
                context.drawEllipse(xpos, ypos, aiw, aih, true);
            }
            getActionIcon().draw(context, xpos, ypos, aiw, aih, getActionIconColor(), getActionIconImageFilter());
        }

        if (aiw > 0) {
            width = Math.max(0, width - aiw - getActionIconSpacing());
        }

        if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
            float off = getPaddingTop() + titleHeight;
            RoundRectangle bg = getBackgroundShape();
            bg.y += off;
            bg.height = bg.height - off;
            bg.width = Math.max(0, bg.width - aiw - getActionIconSpacing() - getPaddingRight());

            if (bg.height > 0) {
                context.pushClip(bg);
                onDrawText(context, x, y + titleHeight, width, Math.max(0, getInHeight() - titleHeight));
                context.popClip();
            }

        } else {
            onDrawText(context, x, y + titleHeight, width, Math.max(0, getInHeight() - titleHeight));
        }

        onDrawTitle(context, x, y, width, height);
        onDrawTextDivider(context, getOutX(), getOutY() + getOutHeight(), getOutWidth(), getTextDividerSize());

        if (getHorizontalBar() != null && isHorizontalVisible()) {
            getHorizontalBar().onDraw(context);
        }

        if (getVerticalBar() != null && isVerticalVisible()) {
            getVerticalBar().onDraw(context);
        }
    }

    private void updateActionPosition() {
        float titleSize = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
        float x = getInX();
        float y = getInY() + titleSize;
        float width = getInWidth();
        float height = getInHeight() - titleSize;

        float iaw = Math.min(width, getLayoutActionIconWidth());
        float iah = Math.min(height, getLayoutActionIconHeight());

        x1 = x + width - iaw;
        x2 = x + width;
        y1 = yOff(y, y + height, iah);
        y2 = yOff(y, y + height, iah) + iah;
    }

    public List<String> getUnmodifiableOptions() {
        return unmodifiableOptions;
    }

    public void setOptions(List<String> options) {
        this.options.clear();
        this.options.addAll(options);
        updateSubmenu();
    }

    public void addOption(String option) {
        options.add(option);
        updateSubmenu();
    }

    public void addOption(List<String> options) {
        this.options.addAll(options);
        updateSubmenu();
    }

    public void addOption(String... options) {
        for (var option : options) {
            addOption(option);
        }
        updateSubmenu();
    }

    public void removeOption(String option) {
        options.remove(option);
        updateSubmenu();
    }

    public void clearOptions() {
        options.clear();
        updateSubmenu();
    }

    private void updateSubmenu() {
        if (subMenu != null && subMenu.isShown()) {
            createMenuItems();
        } else {
            invalidSubmenuItems = true;
        }
    }

    private void createMenuItems() {
        if (subMenu == null) {
            subMenu = new Menu();
        }
        for (var menuItem : menuItems) {
            menuItem.setActionListener(null);
        }
        menuItems.clear();
        subMenu.removeAll();
        for (var option : options) {
            MenuItem menuItem = new MenuItem();
            menuItem.setStyle(getStyle());
            menuItem.setText(option);
            menuItem.setActionListener(this::onMenuItemAction);
            subMenu.addMenuitem(menuItem);
            menuItems.add(menuItem);
        }
        invalidSubmenuItems = false;
    }

    protected Menu getSubMenu() {
        createMenuItems();
        return subMenu;
    }

    private void onMenuItemAction(ActionEvent actionEvent) {
        int index = menuItems.indexOf(actionEvent.getSource());
        if (index > -1) {
            selectOption(index);
        }
        hideSubMenu();
    }

    public void showSubMenu() {
        if (invalidSubmenuItems) {
            createMenuItems();
        }

        Activity act = getActivity();
        if (act != null && subMenu != null) {
            float x = getOutX();
            float y = getOutY();
            float width = getOutWidth();
            float height = getOutHeight();
            Vector2 screen1 = localToScreen(x, y);
            Vector2 screen2 = localToScreen(x + width, y + height);
            subMenu.setMinWidth(getWidth());
            subMenu.show(act, screen2.x, screen2.y, DropdownAlign.TOP_RIGHT);
        }
    }

    public void hideSubMenu() {
        if (subMenu != null) {
            subMenu.hide();
        }
    }

    public void selectOption(int index) {
        if (index >= 0 && index < options.size()) {
            setText(options.get(index));
            fireOptionSelected(options.get(index));
        }
    }

    @Override
    public void fireHover(HoverEvent event) {
        super.fireHover(event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            if (isOverActionButton(screenToLocal(event.getX(), event.getY())) != isHoveringAction) {
                isHoveringAction = !isHoveringAction;
                invalidate(false);
            }
        }
        if (!event.isConsumed() && event.getType() == HoverEvent.EXITED) {
            isHoveringAction = false;
            invalidate(false);
        }
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (isHoveringAction && event.getPointerID() == 1
                && !event.isConsumed() && event.getType() == PointerEvent.RELEASED) {
            action();
        }
    }

    @Override
    protected float getVisibleTextX() {
        return getInX();
    }

    @Override
    protected float getVisibleTextWidth() {
        float aiw = getLayoutActionIconWidth();
        return getInWidth() - (aiw <= 0 ? 0 : aiw + getActionIconSpacing());
    }

    @Override
    protected void textPointer(PointerEvent event, Vector2 point) {
        if (!isHoveringAction) {
            super.textPointer(event, point);
        }
    }

    @Override
    public Cursor getCurrentCursor() {
        return isHoveringAction && actionIconCursor != Cursor.UNSET ? actionIconCursor : super.getCursor();
    }

    public void action() {
        showSubMenu();
        fireAction();
    }

    private void fireAction() {
        if (actionListener != null) {
            UXListener.safeHandle(actionListener, new ActionEvent(this));
        }
    }

    public UXListener<ActionEvent> getActionListener() {
        return actionListener;
    }

    public void setActionListener(UXListener<ActionEvent> actionListener) {
        this.actionListener = actionListener;
    }

    private boolean isOverActionButton(Vector2 local) {
        return actionIcon != null && !(local.x < x1 || local.x > x2 || local.y < y1 || local.y > y2);
    }

    public UXListener<TextEvent> getOptionSelectedListener() {
        return optionSelectedListener;
    }

    public void setOptionSelectedListener(UXListener<TextEvent> optionSelectedListener) {
        this.optionSelectedListener = optionSelectedListener;
    }

    private void fireOptionSelected(String option) {
        if (optionSelectedListener != null) {
            UXListener.safeHandle(optionSelectedListener,
                    new TextEvent(this, TextEvent.CHANGE, 0, getLastCaretPosition(), option));
        }
    }

    public int getActionIconColor() {
        return actionIconColor;
    }

    public void setActionIconColor(int actionIconColor) {
        if (this.actionIconColor != actionIconColor) {
            this.actionIconColor = actionIconColor;
            invalidate(false);
        }
    }

    public int getActionIconBgColor() {
        return actionIconBgColor;
    }

    public void setActionIconBgColor(int actionIconBgColor) {
        if (this.actionIconBgColor != actionIconBgColor) {
            this.actionIconBgColor = actionIconBgColor;
            invalidate(false);
        }
    }

    public Drawable getActionIcon() {
        return actionIcon;
    }

    public void setActionIcon(Drawable actionIcon) {
        if (this.actionIcon != actionIcon) {
            this.actionIcon = actionIcon;
            invalidate(isWrapContent());
        }
    }

    public float getActionIconSpacing() {
        return actionIconSpacing;
    }

    public void setActionIconSpacing(float actionIconSpacing) {
        if (this.actionIconSpacing != actionIconSpacing) {
            this.actionIconSpacing = actionIconSpacing;
            invalidate(isWrapContent());
        }
    }

    public float getActionIconWidth() {
        return actionIconWidth;
    }

    public void setActionIconWidth(float actionIconWidth) {
        if (this.actionIconWidth != actionIconWidth) {
            this.actionIconWidth = actionIconWidth;
            invalidate(isWrapContent());
        }
    }

    public Cursor getActionIconCursor() {
        return actionIconCursor;
    }

    public void setActionIconCursor(Cursor actionIconCursor) {
        if (actionIconCursor == null) actionIconCursor = Cursor.UNSET;

        this.actionIconCursor = actionIconCursor;
    }

    private float getLayoutActionIconWidth() {
        if (actionIcon == null) {
            return 0;
        }
        return actionIconWidth == 0 || actionIconWidth == MATCH_PARENT ? getLineHeight() : actionIconWidth;
    }

    public float getActionIconHeight() {
        return actionIconHeight;
    }

    public void setActionIconHeight(float actionIconHeight) {
        if (this.actionIconHeight != actionIconHeight) {
            this.actionIconHeight = actionIconHeight;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutActionIconHeight() {
        if (actionIcon == null) {
            return 0;
        }
        return actionIconHeight == 0 || actionIconHeight == MATCH_PARENT ? getLineHeight() : actionIconHeight;
    }

    public ImageFilter getActionIconImageFilter() {
        return actionIconImageFilter;
    }

    public void setActionIconImageFilter(ImageFilter actionIconImageFilter) {
        if (actionIconImageFilter == null) actionIconImageFilter = ImageFilter.LINEAR;

        if (this.actionIconImageFilter != actionIconImageFilter) {
            this.actionIconImageFilter = actionIconImageFilter;
            invalidate(false);
        }
    }
}
