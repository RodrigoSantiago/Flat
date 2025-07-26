package flat.widget.structure;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.symbols.Font;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.uxml.*;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.Visibility;
import flat.widget.stages.Divider;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import flat.widget.text.data.TextBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ToolBar extends Parent {

    private UXListener<ActionEvent> navigationAction;

    private String title;
    private String subtitle;
    private Font titleFont = Font.getDefault();
    private Font subtitleFont = Font.getDefault();
    private float titleSize = 16f;
    private float subtitleSize = 8f;
    private int titleColor = Color.black;
    private int subtitleColor = Color.black;
    private String menuItemStyle = "tool-bar-menu-item";
    private String menuDividerStyle = "tool-bar-divider";

    private boolean invalidTitleSize;
    private boolean invalidSubtitleSize;
    private float titleWidth;
    private float titleHeight;
    private float subtitleWidth;
    private float subtitleHeight;

    private ToolItem overflowItem;
    private ToolItem navigationItem;

    private Menu overflowMenu;
    private List<Widget> menuItems = new ArrayList<>();
    private List<ToolItem> toolItems = new ArrayList<>();
    private List<ToolItem> unmodifiableToolItems;
    private Divider divider;

    private TextBox titleRender = new TextBox();
    private TextBox subtitleRender = new TextBox();

    private boolean overflowVisible;
    private boolean navigationVisible;
    private int drawVisibleItems;
    private int prevHiddenItems;
    private float itemsWidth;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private HorizontalAlign itemsHorizontalAlign = HorizontalAlign.RIGHT;

    public ToolBar() {
        titleRender.setFont(titleFont);
        titleRender.setTextSize(titleSize);
        subtitleRender.setFont(subtitleFont);
        subtitleRender.setTextSize(subtitleSize);
        unmodifiableToolItems = Collections.unmodifiableList(toolItems);
        var overflow = new ToolItem();
        overflow.addStyle("overflow");
        setOverflowItem(overflow);
        // var navigation = new ToolItem();
        // navigation.addStyle("navigation");
        // setNavigationItem(navigation);
    }

    @Override
    public void setContextMenu(Menu contextMenu) {
        super.setContextMenu(contextMenu);
        if (contextMenu != overflowMenu) {
            if (overflowMenu != null) {
                while (!menuItems.isEmpty()) {
                    Widget removed = menuItems.remove(menuItems.size() - 1);
                    if (removed instanceof MenuItem menuItem) {
                        menuItem.setActionListener(null);
                    }
                    overflowMenu.remove(removed);
                }
                if (divider != null) {
                    overflowMenu.remove(divider);
                    divider = null;
                }
            }
            this.overflowMenu = contextMenu;
        }
    }

    @Override
    public void showContextMenu(float x, float y) {

    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);
        List<ToolItem> items = new ArrayList<>();

        for (var child : children) {
            if (child.getWidget() instanceof ToolItem item) {
                if (child.getAttributeBool("overflow-item", false)) {
                    setOverflowItem(item);
                } else if (child.getAttributeBool("navigation-item", false)) {
                    setNavigationItem(item);
                } else {
                    items.add(item);
                }
            }
        }
        addToolItem(items);
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setTitle(attrs.getAttributeString("title", getTitle()));
        setSubtitle(attrs.getAttributeString("subtitle", getSubtitle()));
        setNavigationAction(attrs.getAttributeListener("on-navigation", ActionEvent.class, controller, getNavigationAction()));
        setMenuItemStyle(attrs.getAttributeString("menu-item-style", getMenuItemStyle()));
        setMenuDividerStyle(attrs.getAttributeString("menu-divider-style", getMenuDividerStyle()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setTitleFont(attrs.getFont("title-font", info, getTitleFont()));
        setSubtitleFont(attrs.getFont("subtitle-font", info, getSubtitleFont()));
        setTitleSize(attrs.getSize("title-size", info, getTitleSize()));
        setSubtitleSize(attrs.getSize("subtitle-size", info, getSubtitleSize()));
        setTitleColor(attrs.getColor("title-color", info, getTitleColor()));
        setSubtitleColor(attrs.getColor("subtitle-color", info, getSubtitleColor()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setItemsHorizontalAlign(attrs.getConstant("items-horizontal-align", info, getItemsHorizontalAlign()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        float iw = 0;
        float ih = 0;
        for (ToolItem item : toolItems) {
            item.onMeasure();
            iw += getDefWidth(item);
            ih = Math.max(ih, getDefHeight(item));
        }

        float ow = 0;
        if (overflowItem != null) {
            overflowItem.onMeasure();
            ow = getDefWidth(overflowItem);
            ih = Math.max(ih, getDefHeight(overflowItem));
        }

        float nw = 0;
        if (navigationItem != null) {
            navigationItem.onMeasure();
            nw = getDefWidth(navigationItem);
            ih = Math.max(ih, getDefHeight(navigationItem));
        }

        if (wrapWidth) {
            float tw = Math.max(hasTitle() ? getTitleWidth() : 0, hasSubtitle() ? getSubtitleWidth() : 0);
            mWidth = Math.max(tw + extraWidth + ow + nw + iw, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            float th = (hasTitle() ? getTitleHeight() : 0) + (hasSubtitle() ? getSubtitleHeight() : 0);
            mHeight = Math.max(Math.max(th, ih) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        float inWidth = getInWidth();
        float inHeight = getInHeight();

        float texWidth = Math.min(inWidth, Math.max(getTitleWidth(), getSubtitleWidth()));

        float navWidth;
        if (navigationItem != null) {
            navigationVisible = true;
            float defWidth = Math.min(navigationItem.getMeasureWidth(), navigationItem.getLayoutMaxWidth());
            navWidth = Math.min(inWidth, defWidth);
            navigationItem.setActionListener(this::onNavigationBtnAction);
        } else {
            navigationVisible = false;
            navWidth = 0;
        }

        boolean isAlwaysVisible = hasExtraContextMenuItems();
        float oveWidth;
        if (overflowItem != null) {
            float defWidth = Math.min(overflowItem.getMeasureWidth(), overflowItem.getLayoutMaxWidth());
            float maxWidth = Math.max(0, defWidth == MATCH_PARENT ? inWidth - navWidth - texWidth : inWidth - navWidth);
            oveWidth = Math.min(maxWidth, defWidth);
            overflowItem.setActionListener(this::onOverflowBtnAction);
        } else {
            oveWidth = 0;
        }

        float itemsTotal = Math.max(0, inWidth - texWidth - navWidth - (isAlwaysVisible ? oveWidth : 0));
        float itemsSpace = 0;

        int maxVisibleItems = toolItems.size();
        for (int i = 0; i < toolItems.size(); i++) {
            ToolItem item = toolItems.get(i);
            float defWidth = Math.min(item.getMeasureWidth(), item.getLayoutMaxWidth());
            float itemSpace = defWidth == MATCH_PARENT ? Math.min(itemsTotal, defWidth) : defWidth;
            if (itemsSpace + itemSpace > itemsTotal + 0.0001f) {
                maxVisibleItems = i;
                break;
            } else {
                itemsSpace += itemSpace;
            }
        }

        if (isAlwaysVisible && oveWidth > 0) {
            overflowVisible = true;
        } else if (!isAlwaysVisible && oveWidth > 0 && maxVisibleItems < toolItems.size()) {
            overflowVisible = true;
            itemsTotal = Math.max(0, inWidth - texWidth - navWidth - oveWidth);
            itemsSpace = 0;
            maxVisibleItems = toolItems.size();
            for (int i = 0; i < toolItems.size(); i++) {
                ToolItem item = toolItems.get(i);
                float defWidth = Math.min(item.getMeasureWidth(), item.getLayoutMaxWidth());
                float itemSpace = defWidth == MATCH_PARENT ? Math.min(itemsTotal, defWidth) : defWidth;
                if (itemsSpace + itemSpace > itemsTotal + 0.0001f) {
                    maxVisibleItems = i;
                    break;
                } else {
                    itemsSpace += itemSpace;
                }
            }
        } else {
            overflowVisible = false;
        }
        drawVisibleItems = maxVisibleItems;

        if (overflowVisible && overflowItem != null) {
            float h = Math.min(inHeight, Math.min(overflowItem.getMeasureHeight(), overflowItem.getLayoutMaxHeight()));
            overflowItem.onLayout(oveWidth, h);
            overflowItem.setLayoutPosition(getInX() + getInWidth() - oveWidth, getInY());
        }
        if (navigationVisible && navigationItem != null) {
            float h = Math.min(inHeight, Math.min(navigationItem.getMeasureHeight(), navigationItem.getLayoutMaxHeight()));
            navigationItem.onLayout(navWidth, h);
            navigationItem.setLayoutPosition(getInX(), getInY());
        }
        itemsWidth = itemsSpace;
        float tw = Math.max(hasTitle() ? getTitleWidth() : 0, hasSubtitle() ? getSubtitleWidth() : 0);
        float xpos;
        if (getItemsHorizontalAlign() == HorizontalAlign.LEFT) {
            xpos = Math.max(0, getInX() + (navigationVisible && navigationItem != null ? navigationItem.getLayoutWidth() : 0) + tw);
        } else if (getItemsHorizontalAlign() == HorizontalAlign.RIGHT) {
            xpos = Math.max(0, getInX() + inWidth - (overflowVisible ? oveWidth : 0) - itemsSpace);
        } else {
            float min = Math.max(0, getInX() + (navigationVisible && navigationItem != null ? navigationItem.getLayoutWidth() : 0) + tw);
            float max = Math.max(0, getInX() + inWidth - (overflowVisible ? oveWidth : 0) - itemsSpace);
            xpos = (min + max) / 2f;
        }
        Math.max(0, getInX() + inWidth - (overflowVisible ? oveWidth : 0) - itemsSpace);
        for (ToolItem item : toolItems) {
            float defWidth = Math.min(item.getMeasureWidth(), item.getLayoutMaxWidth());
            float w = defWidth == MATCH_PARENT ? Math.min(itemsTotal, defWidth) : defWidth;
            float h = Math.min(inHeight, Math.min(item.getMeasureHeight(), item.getLayoutMaxHeight()));
            item.onLayout(w, h);
            item.setLayoutPosition(xpos, getInY());
            xpos += w;
        }
        fireLayout();
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (!isCurrentHandleEventsEnabled()
                || getVisibility() == Visibility.GONE
                || (!includeDisabled && !isEnabled())
                || !contains(x, y)) {
            return null;
        }
        for (int i = 0; i < toolItems.size() && i < drawVisibleItems; i++) {
            ToolItem toolItem = toolItems.get(i);
            Widget found = toolItem.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }
        if (overflowVisible && overflowItem != null) {
            Widget found = overflowItem.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }
        if (navigationVisible && navigationItem != null) {
            Widget found = navigationItem.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }

        return isHandlePointerEnabled() ? this : null;
    }

    private boolean hasExtraContextMenuItems() {
        if (getContextMenu() != null) {
            if (getContextMenu().getUnmodifiableItemsList().size() - menuItems.size() - (divider == null ? 0 : 1) > 0) {
                return true;
            }
        }
        return false;
    }

    private void updateMenuItemName(ToolItem toolItem) {
        if (overflowMenu != null) {
            int hiddenItems = Math.max(0, toolItems.size() - drawVisibleItems);
            for (int i = 0; i < hiddenItems; i++) {
                ToolItem item = toolItems.get(toolItems.size() - 1 - i);
                if (item == toolItem) {
                    Widget widget = i >= menuItems.size() ? null : menuItems.get(i);
                    if (widget instanceof MenuItem menuItem) {
                        menuItem.setEnabled(item.isEnabled());
                        menuItem.setText(item.getMenuText());
                        menuItem.setShortcutText(item.getMenuShortcutText());
                        menuItem.setIcon(item.getMenuIcon());
                        menuItem.setActivated(item.isActivated());
                    }
                    break;
                }
            }
        }
    }

    private void updateMenuItems() {
        if (overflowMenu == null) {
            overflowMenu = new Menu();
            setContextMenu(overflowMenu);
        }

        int hiddenItems = Math.max(0, toolItems.size() - drawVisibleItems);
        prevHiddenItems = hiddenItems;

        // Add or Update
        for (int i = 0; i < hiddenItems; i++) {
            ToolItem item = toolItems.get(toolItems.size() - 1 - i);
            Widget widget = i >= menuItems.size() ? null : menuItems.get(i);
            if (item.isDivider()) {
                if (!(widget instanceof Divider)) {
                    if (widget != null) {
                        menuItems.remove(widget);
                        overflowMenu.remove(widget);
                    }
                    var divider = new Divider();
                    if (getMenuDividerStyle() != null) {
                        divider.addStyle(getMenuDividerStyle());
                    }
                    menuItems.add(divider);
                    overflowMenu.addDivider(divider);
                    overflowMenu.moveChild(divider, 0);
                }
            } else {
                if (widget instanceof MenuItem menuItem) {
                    menuItem.setEnabled(item.isEnabled());
                    menuItem.setText(item.getMenuText());
                    menuItem.setShortcutText(item.getMenuShortcutText());
                    menuItem.setIcon(item.getMenuIcon());
                    menuItem.setActivated(item.isActivated());
                } else {
                    if (widget != null) {
                        menuItems.remove(widget);
                        overflowMenu.remove(widget);
                    }
                    var menuItem = new MenuItem();
                    if (getMenuItemStyle() != null) {
                        menuItem.addStyle(getMenuItemStyle());
                    }
                    menuItem.setActionListener(this::onToolItemAction);
                    menuItems.add(menuItem);
                    if (divider == null && hasExtraContextMenuItems()) {
                        divider = new Divider();
                        overflowMenu.addDivider(divider);
                        overflowMenu.moveChild(divider, 0);
                    }
                    overflowMenu.addMenuItem(menuItem);
                    overflowMenu.moveChild(menuItem, 0);
                    menuItem.setEnabled(item.isEnabled());
                    menuItem.setText(item.getMenuText());
                    menuItem.setShortcutText(item.getMenuShortcutText());
                    menuItem.setIcon(item.getMenuIcon());
                    menuItem.setActivated(item.isActivated());
                }
            }
        }

        // Remove excedent
        while (menuItems.size() > hiddenItems) {
            Widget removed = menuItems.remove(menuItems.size() - 1);
            if (removed instanceof MenuItem menuItem) {
                menuItem.setActionListener(null);
            }
            overflowMenu.remove(removed);
        }

        // Remove divider if clear
        if (divider != null && menuItems.size() == 0) {
            overflowMenu.remove(divider);
            divider = null;
        }
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        for (int i = 0; i < toolItems.size() && i < drawVisibleItems; i++) {
            ToolItem toolItem = toolItems.get(i);
            toolItem.onDraw(graphics);
        }

        if (overflowVisible && overflowItem != null) {
            overflowItem.onDraw(graphics);
        }
        if (navigationVisible && navigationItem != null) {
            navigationItem.onDraw(graphics);
        }

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        boolean hasTitle = hasTitle() && getTitleFont() != null && getTitleSize() > 0;
        boolean hasSubtitle = hasSubtitle() && getSubtitleFont() != null && getSubtitleSize() > 0;
        if (hasTitle && hasSubtitle && getTitleHeight() + getSubtitleHeight() > height + 0.001f) {
            hasSubtitle = false;
        }
        if (hasTitle || hasSubtitle) {

            float titleH = (hasTitle ? getTitleHeight() : 0);
            float subtitleH = (hasSubtitle ? getSubtitleHeight() : 0);
            float tw = Math.max(hasTitle ? getTitleWidth() : 0, hasSubtitle ? getSubtitleWidth() : 0);
            float th = (hasTitle ? getTitleHeight() : 0) + (hasSubtitle ? getSubtitleHeight() : 0);
            float ow = overflowVisible && overflowItem != null ? overflowItem.getLayoutWidth() : 0;
            float nw = navigationVisible && navigationItem != null ? navigationItem.getLayoutWidth() : 0;

            float boxX = x + nw;
            float boxWidth = Math.max(0, width - ow - nw - itemsWidth);
            float boxHeight = Math.min(height, titleH + subtitleH);

            graphics.setTransform2D(getTransform());
            graphics.setTextBlur(0);
            if (hasTitle) {
                graphics.setColor(getTitleColor());
                graphics.setTextFont(getTitleFont());
                graphics.setTextSize(getTitleSize());

                float xpos = xOff(boxX, boxX + boxWidth, Math.min(getTitleWidth(), boxWidth));
                float ypos = yOff(y, y + height, boxHeight);
                if (boxWidth > 0 && height > 0) {
                    titleRender.drawText(graphics, xpos, ypos, boxWidth, boxHeight, getHorizontalAlign());
                }
            }
            if (hasSubtitle) {
                graphics.setColor(getSubtitleColor());
                graphics.setTextFont(getSubtitleFont());
                graphics.setTextSize(getSubtitleSize());

                float xpos = xOff(boxX, boxX + boxWidth, Math.min(getSubtitleWidth(), boxWidth));
                float ypos = yOff(y, y + height, boxHeight) + titleH;
                if (boxWidth > 0 && boxHeight - titleH > 0) {
                    subtitleRender.drawText(graphics, xpos, ypos, boxWidth, boxHeight - titleH, getHorizontalAlign());
                }
            }
        }
    }

    public ToolItem getOverflowItem() {
        return overflowItem;
    }

    public void setOverflowItem(ToolItem overflowItem) {
        if (this.overflowItem != overflowItem) {
            if (overflowItem != null) {
                add(overflowItem);
                if (overflowItem.getParent() == this) {
                    var old = this.overflowItem;
                    this.overflowItem = overflowItem;
                    if (old != null) {
                        remove(old);
                    }
                }
            } else {
                var old = this.overflowItem;
                this.overflowItem = null;
                remove(old);
            }
        }
    }

    public ToolItem getNavigationItem() {
        return navigationItem;
    }

    public void setNavigationItem(ToolItem navigationItem) {
        if (this.navigationItem != navigationItem) {
            if (navigationItem != null) {
                add(navigationItem);
                if (navigationItem.getParent() == this) {
                    navigationItem.setActionListener(this::onNavigationBtnAction);
                    var old = this.navigationItem;
                    this.navigationItem = navigationItem;
                    if (old != null) {
                        remove(old);
                    }
                }
            } else {
                var old = this.navigationItem;
                this.navigationItem = null;
                remove(old);
            }
        }
    }

    private void onToolItemAction(ActionEvent actionEvent) {
        for (int i = 0; i < prevHiddenItems; i++) {
            Widget menuItem = i >= menuItems.size() ? null : menuItems.get(i);
            if (menuItem == actionEvent.getSource()) {
                ToolItem item = toolItems.get(toolItems.size() - 1 - i);
                item.action();
                break;
            }
        }
        hideContextMenu();
    }

    private void onNavigationBtnAction(ActionEvent actionEvent) {
        navigationAction();
    }

    private void onOverflowBtnAction(ActionEvent actionEvent) {
        updateMenuItems();
        if (overflowItem != null && overflowMenu != null && getActivity() != null) {
            Vector2 center = localToScreen(
                    overflowItem.getLayoutX() + overflowItem.getLayoutWidth() * 0.5f,
                    overflowItem.getLayoutY() + overflowItem.getLayoutHeight() * 0.5f);
            overflowMenu.show(getActivity(), center.x, center.y, DropdownAlign.SCREEN_SPACE);
        }
    }

    private boolean hasOverflow() {
        return overflowItem != null;
    }

    private boolean hasNavigation() {
        return navigationItem != null;
    }

    private boolean hasTitle() {
        return title != null && !title.isEmpty();
    }

    private boolean hasSubtitle() {
        return subtitle != null && !subtitle.isEmpty();
    }

    public List<ToolItem> getUnmodifiableToolItems() {
        return unmodifiableToolItems;
    }

    public void addToolItem(ToolItem item) {
        TaskList tasks = new TaskList();
        if (attachAndAddChild(item, tasks)) {
            toolItems.add(item);
            tasks.run();
        }
    }

    public void addToolItem(List<ToolItem> items) {
        for (var item : items) {
            addToolItem(item);
        }
    }

    public void addToolItem(ToolItem... items) {
        for (var item : items) {
            addToolItem(item);
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child == overflowItem || child == navigationItem) {
            return false;
        }
        if (child instanceof ToolItem toolItem) {
            toolItems.remove(child);
        }
        return super.detachChild(child);
    }

    void invalidateToolItem(ToolItem item) {
        updateMenuItemName(item);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (!Objects.equals(this.title, title)) {
            this.title = title;
            titleRender.setText(title);
            invalidateTitleSize();
        }
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(Font titleFont) {
        if (this.titleFont != titleFont) {
            this.titleFont = titleFont;
            titleRender.setFont(titleFont);
            invalidateTitleSize();
        }
    }

    public float getTitleSize() {
        return titleSize;
    }

    public void setTitleSize(float titleSize) {
        if (this.titleSize != titleSize) {
            this.titleSize = titleSize;
            titleRender.setTextSize(titleSize);
            invalidateTitleSize();
        }
    }

    public float getTitleWidth() {
        if (invalidTitleSize) {
            invalidTitleSize = false;
            titleWidth = titleRender.getTextWidth();
        }
        return titleWidth;
    }

    public float getTitleHeight() {
        return titleHeight;
    }

    private float getTitleFontHeight() {
        return titleFont == null ? titleSize : titleFont.getHeight(titleSize);
    }

    private void invalidateTitleSize() {
        invalidTitleSize = true;
        titleHeight = titleRender.getTextHeight();
        invalidate(true);
    }

    public int getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(int titleColor) {
        if (this.titleColor != titleColor) {
            this.titleColor = titleColor;
            invalidate(false);
        }
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        if (!Objects.equals(this.subtitle, subtitle)) {
            this.subtitle = subtitle;
            subtitleRender.setText(subtitle);
            invalidateSubtitleSize();
        }
    }

    public Font getSubtitleFont() {
        return subtitleFont;
    }

    public void setSubtitleFont(Font subtitleFont) {
        if (this.subtitleFont != subtitleFont) {
            this.subtitleFont = subtitleFont;
            subtitleRender.setFont(subtitleFont);
            invalidateSubtitleSize();
        }
    }

    public float getSubtitleSize() {
        return subtitleSize;
    }

    public void setSubtitleSize(float subtitleSize) {
        if (this.subtitleSize != subtitleSize) {
            this.subtitleSize = subtitleSize;
            subtitleRender.setTextSize(subtitleSize);
            invalidateSubtitleSize();
        }
    }

    public float getSubtitleWidth() {
        if (invalidSubtitleSize) {
            invalidSubtitleSize = false;
            subtitleWidth = subtitleRender.getTextWidth();
        }
        return subtitleWidth;
    }

    public float getSubtitleHeight() {
        return subtitleHeight;
    }

    private void invalidateSubtitleSize() {
        invalidSubtitleSize = true;
        subtitleHeight = subtitleRender.getTextHeight();
        invalidate(true);
    }

    public int getSubtitleColor() {
        return subtitleColor;
    }

    public void setSubtitleColor(int subtitleColor) {
        if (this.subtitleColor != subtitleColor) {
            this.subtitleColor = subtitleColor;
            invalidate(false);
        }
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(false);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(false);
        }
    }
    
    public HorizontalAlign getItemsHorizontalAlign() {
        return itemsHorizontalAlign;
    }
    
    public void setItemsHorizontalAlign(HorizontalAlign itemsHorizontalAlign) {
        if (itemsHorizontalAlign == null) itemsHorizontalAlign = HorizontalAlign.RIGHT;
        
        if (this.itemsHorizontalAlign != itemsHorizontalAlign) {
            this.itemsHorizontalAlign = itemsHorizontalAlign;
            invalidate(false);
        }
    }
    
    public String getMenuDividerStyle() {
        return menuDividerStyle;
    }
    
    public void setMenuDividerStyle(String menuDividerStyle) {
        if (!Objects.equals(this.menuDividerStyle, menuDividerStyle)) {
            for (var item : menuItems) {
                if (item instanceof Divider) {
                    item.removeStyle(this.menuDividerStyle);
                    item.addStyle(this.menuDividerStyle);
                }
            }
            this.menuDividerStyle = menuDividerStyle;
        }
    }
    
    public String getMenuItemStyle() {
        return menuItemStyle;
    }
    
    public void setMenuItemStyle(String menuItemStyle) {
        if (!Objects.equals(this.menuItemStyle, menuItemStyle)) {
            for (var item : menuItems) {
                if (item instanceof MenuItem) {
                    item.removeStyle(this.menuItemStyle);
                    item.addStyle(this.menuItemStyle);
                }
            }
            this.menuItemStyle = menuItemStyle;
        }
    }
    
    public UXListener<ActionEvent> getNavigationAction() {
        return navigationAction;
    }

    public void setNavigationAction(UXListener<ActionEvent> navigationAction) {
        this.navigationAction = navigationAction;
    }

    public void navigationAction() {
        fireNavigationAction();
    }

    private void fireNavigationAction() {
        if (navigationAction != null) {
            UXListener.safeHandle(navigationAction, new ActionEvent(this));
        }
    }

    protected float xOff(float start, float end, float textWidth) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == HorizontalAlign.RIGHT) return end - textWidth;
        if (horizontalAlign == HorizontalAlign.CENTER) return (start + end - textWidth) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float textHeight) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == VerticalAlign.BOTTOM) return end - textHeight;
        if (verticalAlign == VerticalAlign.MIDDLE) return (start + end - textHeight) / 2f;
        return start;
    }
}
