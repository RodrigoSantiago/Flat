package flat.widget.structure;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.math.Vector2;
import flat.uxml.*;
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.Visibility;
import flat.widget.stages.Divider;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import flat.widget.text.data.TextRender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ToolBar extends Group {

    private UXListener<ActionEvent> navigationAction;

    private String title;
    private String subtitle;
    private Font titleFont = Font.getDefault();
    private Font subtitleFont = Font.getDefault();
    private float titleSize = 16f;
    private float subtitleSize = 8f;
    private int titleColor = Color.black;
    private int subtitleColor = Color.black;

    public float iconsWidth;
    public float iconsHeight;
    public float iconsSpacing;

    private boolean invalidTitleSize;
    private boolean invalidSubtitleSize;
    private float titleWidth;
    private float titleHeight;
    private float subtitleWidth;
    private float subtitleHeight;

    private ToolItem overflowItem;
    private ToolItem navigationItem;

    private Menu overflowMenu;
    private List<MenuItem> menuItems = new ArrayList<>();
    private List<ToolItem> toolItems = new ArrayList<>();
    private List<ToolItem> unmodifiableToolItems;
    private Divider divider;

    private TextRender titleRender = new TextRender();
    private TextRender subtitleRender = new TextRender();

    private boolean overflowVisible;
    private boolean navigationVisible;
    private int drawVisibleItems;
    private int prevHiddenItems;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    public ToolBar() {
        titleRender.setFont(titleFont);
        titleRender.setTextSize(titleSize);
        subtitleRender.setFont(subtitleFont);
        subtitleRender.setTextSize(subtitleSize);
        unmodifiableToolItems = Collections.unmodifiableList(toolItems);
        var overflow = new ToolItem();
        overflow.setStyle("tool-item-overflow");
        setOverflowItem(overflow);
        var navigation = new ToolItem();
        navigation.setStyle("tool-item-navigation");
        setNavigationItem(navigation);
    }

    @Override
    public void setContextMenu(Menu contextMenu) {
        super.setContextMenu(contextMenu);
        if (contextMenu != overflowMenu) {
            if (overflowMenu != null) {
                while (menuItems.size() > 0) {
                    MenuItem removed = menuItems.remove(menuItems.size() - 1);
                    removed.setActionListener(null);
                    overflowMenu.remove(removed);
                }
                if (divider != null) {
                    overflowMenu.remove(divider);
                    divider = null;
                }
            }
            this.overflowMenu = contextMenu;
            updateMenuItems();
        }
    }

    @Override
    public void showContextMenu(float x, float y) {

    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);
        List<ToolItem> items = new ArrayList<>();

        UXAttrs attrs = getAttrs();
        String overflowId = attrs.getAttributeString("overflow-item-id", null);
        String navigationId = attrs.getAttributeString("navigation-item-id", null);
        Widget widget;
        while ((widget = children.next()) != null) {
            if (widget instanceof ToolItem item) {
                if (overflowId != null && overflowId.equals(widget.getId())) {
                    setOverflowItem(item);
                } else if (navigationId != null && navigationId.equals(widget.getId())) {
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
        setNavigationAction(attrs.getAttributeListener("on-navigation", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIconsWidth(attrs.getSize("icons-width", info, getIconsWidth()));
        setIconsHeight(attrs.getSize("icons-height", info, getIconsHeight()));
        setIconsSpacing(attrs.getSize("icons-spacing", info, getIconsSpacing()));
        setTitleFont(attrs.getFont("title-font", info, getTitleFont()));
        setSubtitleFont(attrs.getFont("subtitle-font", info, getSubtitleFont()));
        setTitleSize(attrs.getSize("title-size", info, getTitleSize()));
        setSubtitleSize(attrs.getSize("subtitle-size", info, getSubtitleSize()));
        setTitleColor(attrs.getColor("title-color", info, getTitleColor()));
        setSubtitleColor(attrs.getColor("subtitle-color", info, getSubtitleColor()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        float iW = getLayoutIconsWidth() + getIconsSpacing();
        float iH = getLayoutIconsHeight() + getIconsSpacing();
        float ow = hasOverflow() ? iW : 0;
        float nw = hasNavigation() ? iW : 0;

        for (ToolItem item : toolItems) {
            item.onMeasure();
        }
        if (overflowItem != null) {
            overflowItem.onMeasure();
        }
        if (navigationItem != null) {
            navigationItem.onMeasure();
        }

        if (wrapWidth) {
            float tw = Math.max(getTitleWidth(), getSubtitleWidth());
            mWidth = Math.max(tw + extraWidth + ow + nw + toolItems.size() * iW, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            float th = getTitleHeight() + getSubtitleHeight();
            mHeight = Math.max(Math.max(th, iH) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);


        updateNavigationButton();
        updateOverflowButton();
        updateItems();

        if (checkNeedUpdateMenuItems()) {
            if (getActivity() != null) {
                getActivity().getWindow().runSync(this::updateMenuItems);
            }
        }
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (getVisibility() == Visibility.VISIBLE && (includeDisabled || isEnabled()) && contains(x, y)) {
            for (int i = 0; i < toolItems.size() && i < drawVisibleItems; i++) {
                ToolItem toolItem = toolItems.get(i);
                Widget found = toolItem.findByPosition(x, y, includeDisabled);
                if (found != null) return found;
            }
            if (overflowVisible) {
                Widget found = overflowItem.findByPosition(x, y, includeDisabled);
                if (found != null) return found;
            }
            if (navigationVisible) {
                Widget found = navigationItem.findByPosition(x, y, includeDisabled);
                if (found != null) return found;
            }

            return isClickable() ? this : null;
        } else {
            return null;
        }
    }

    private void updateItems() {
        boolean isStyleEnouth = true;

        float tw = Math.max(getTitleWidth(), getSubtitleWidth());
        float iw = getLayoutIconsWidth() + getIconsSpacing();
        float ow = isOverflowButtonVisible() ? iw : 0;
        float nw = hasNavigation() ? iw : 0;
        float itemsSpace = Math.max(0, getInWidth() - tw - ow - nw);

        int maxVisibleItems = iw == 0 ? 0 : (int) (itemsSpace / iw);
        float sx = getInWidth() + getInX() - (Math.min(maxVisibleItems, toolItems.size()) * iw + ow);
        for (int i = 0; i < toolItems.size(); i++) {
            ToolItem item = toolItems.get(i);
            item.onLayout(localIconWidth(), localIconHeight());
            item.setLayoutPosition(sx + (i * iw), getInY());
        }
        drawVisibleItems = Math.min(toolItems.size(), maxVisibleItems);
    }

    private void updateNavigationButton() {
        if (navigationItem != null) {
            navigationVisible = true;
            navigationItem.onLayout(localIconWidth(), localIconHeight());
            navigationItem.setLayoutPosition(getInX(), getInY());
            navigationItem.setActionListener(this::onNavigationBtnAction);
        } else {
            navigationVisible = false;
        }
    }

    private void updateOverflowButton() {
        if (isOverflowButtonVisible()) {
            overflowVisible = true;
            float iw = hasNavigation() ? Math.min(localIconWidth(), getInWidth() - getLayoutIconsWidth()) : localIconWidth();
            overflowItem.onLayout(iw, localIconHeight());
            overflowItem.setLayoutPosition(getInX() + getInWidth() - iw, getInY());
            overflowItem.setActionListener(this::onOverflowBtnAction);
        } else {
            overflowVisible = false;
        }
    }

    private boolean hasExtraContextMenuItems() {
        if (getContextMenu() != null) {
            if (getContextMenu().getChildrenIterable().size() - menuItems.size() > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverflowButtonVisible() {
        if (overflowItem == null) {
            return false;
        }
        if (hasExtraContextMenuItems()) {
            return true;
        }

        float tw = Math.max(getTitleWidth(), getSubtitleWidth());
        float iw = getLayoutIconsWidth() + getIconsSpacing();
        float nw = hasNavigation() ? iw : 0;
        float itemsSpace = Math.max(0, getInWidth() - tw - nw);
        int maxVisibleItems = iw == 0 ? 0 : (int) (itemsSpace / iw);

        return maxVisibleItems < toolItems.size();
    }

    private void updateMenuItemName(ToolItem toolItem) {
        if (overflowMenu != null) {
            float tw = Math.max(getTitleWidth(), getSubtitleWidth());
            float iw = getLayoutIconsWidth() + getIconsSpacing();
            float ow = isOverflowButtonVisible() ? iw : 0;
            float nw = hasNavigation() ? iw : 0;
            float itemsSpace = Math.max(0, getInWidth() - tw - ow - nw);
            int maxVisibleItems = iw == 0 ? 0 : (int) (itemsSpace / iw);

            int hiddenItems = Math.max(0, toolItems.size() - maxVisibleItems);

            for (int i = 0; i < hiddenItems; i++) {
                ToolItem item = toolItems.get(toolItems.size() - 1 - i);
                if (item == toolItem) {
                    MenuItem menuItem = i >= menuItems.size() ? null : menuItems.get(i);
                    if (menuItem != null) {
                        menuItem.setText(item.getMenuText());
                        menuItem.setShortcutText(item.getMenuShortcutText());
                    }
                    break;
                }
            }
        }
    }

    private boolean checkNeedUpdateMenuItems() {
        float tw = Math.max(getTitleWidth(), getSubtitleWidth());
        float iw = getLayoutIconsWidth() + getIconsSpacing();
        float ow = isOverflowButtonVisible() ? iw : 0;
        float nw = hasNavigation() ? iw : 0;
        float itemsSpace = Math.max(0, getInWidth() - tw - ow - nw);
        int maxVisibleItems = iw == 0 ? 0 : (int) (itemsSpace / iw);

        int hiddenItems = Math.max(0, toolItems.size() - maxVisibleItems);
        return hiddenItems != prevHiddenItems || (overflowMenu == null && hiddenItems > 0);
    }

    private void updateMenuItems() {
        if (overflowMenu == null) {
            overflowMenu = new Menu();
            setContextMenu(overflowMenu);
        }

        float tw = Math.max(getTitleWidth(), getSubtitleWidth());
        float iw = getLayoutIconsWidth() + getIconsSpacing();
        float ow = isOverflowButtonVisible() ? iw : 0;
        float nw = hasNavigation() ? iw : 0;
        float itemsSpace = Math.max(0, getInWidth() - tw - ow - nw);
        int maxVisibleItems = iw == 0 ? 0 : (int) (itemsSpace / iw);

        int hiddenItems = Math.max(0, toolItems.size() - maxVisibleItems);
        prevHiddenItems = hiddenItems;

        // Add or Update
        for (int i = 0; i < hiddenItems; i++) {
            MenuItem menuItem = i >= menuItems.size() ? null : menuItems.get(i);
            if (menuItem == null) {
                menuItem = new MenuItem();
                menuItem.setStyle("tool-bar-menu-item");
                menuItem.setActionListener(this::onToolItemAction);
                if (divider == null && hasExtraContextMenuItems()) {
                    divider = new Divider();
                    overflowMenu.addDivider(divider);
                    overflowMenu.moveChild(divider, 0);
                }
                overflowMenu.addMenuItem(menuItem);
                overflowMenu.moveChild(menuItem, 0);
                menuItems.add(menuItem);
            }
            ToolItem item = toolItems.get(toolItems.size() - 1 - i);
            menuItem.setText(item.getMenuText());
            menuItem.setShortcutText(item.getMenuShortcutText());
        }

        // Remove excedent
        while (menuItems.size() > hiddenItems) {
            MenuItem removed = menuItems.remove(menuItems.size() - 1);
            removed.setActionListener(null);
            overflowMenu.remove(removed);
        }

        // Remove divider if clear
        if (divider != null && menuItems.size() == 0) {
            overflowMenu.remove(divider);
            divider = null;
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        for (int i = 0; i < toolItems.size() && i < drawVisibleItems; i++) {
            ToolItem toolItem = toolItems.get(i);
            toolItem.onDraw(context);
        }
        if (overflowVisible) {
            getOverflowItem().onDraw(context);
        }
        if (navigationVisible) {
            getNavigationItem().onDraw(context);
        }

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        boolean hasTitle = hasTitle() && getTitleFont() != null && getTitleSize() > 0;
        boolean hasSubtitle = hasSubtitle() && getSubtitleFont() != null && getSubtitleSize() > 0;
        if (hasTitle || hasSubtitle) {

            float titleH = (hasTitle ? getTitleHeight() : 0);
            float subtitleH = (hasSubtitle ? getSubtitleHeight() : 0);
            float tw = Math.max(hasTitle ? getTitleWidth() : 0, hasSubtitle ? getSubtitleWidth() : 0);
            float th = (hasTitle ? getTitleHeight() : 0) + (hasSubtitle ? getSubtitleHeight() : 0);
            float iw = getLayoutIconsWidth() + getIconsSpacing();
            float ow = hasOverflow() ? iw : 0;
            float nw = hasNavigation() ? iw : 0;
            float itemsSpace = Math.max(0, drawVisibleItems * iw);

            float boxX = x + nw;
            float boxWidth = Math.max(0, width - ow - nw - itemsSpace);
            float boxHeight = Math.min(height, titleH + subtitleH);

            context.setTransform2D(getTransform());
            context.setTextBlur(0);
            if (hasTitle) {
                context.setColor(getTitleColor());
                context.setTextFont(getTitleFont());
                context.setTextSize(getTitleSize());

                float xpos = xOff(boxX, boxX + boxWidth, Math.min(getTitleWidth(), boxWidth));
                float ypos = yOff(y, y + height, boxHeight);
                if (boxWidth > 0 && height > 0) {
                    titleRender.drawText(context, xpos, ypos, boxWidth, boxHeight, getHorizontalAlign());
                }
            }
            if (hasSubtitle) {
                context.setColor(getSubtitleColor());
                context.setTextFont(getSubtitleFont());
                context.setTextSize(getSubtitleSize());

                float xpos = xOff(boxX, boxX + boxWidth, Math.min(getSubtitleWidth(), boxWidth));
                float ypos = yOff(y, y + height, boxHeight) + titleH;
                if (boxWidth > 0 && boxHeight - titleH > 0) {
                    subtitleRender.drawText(context, xpos, ypos, boxWidth, boxHeight - titleH, getHorizontalAlign());
                }
            }
        }
    }

    private float localIconWidth() {
        return Math.min(getInWidth(), getLayoutIconsWidth() + getIconsSpacing());
    }
    
    private float localIconHeight() {
        return Math.min(getInHeight(), getLayoutIconsHeight() + getIconsSpacing());
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
            MenuItem menuItem = i >= menuItems.size() ? null : menuItems.get(i);
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
        if (overflowMenu != null && getActivity() != null) {
            float x = getInX() + getInWidth() - getIconsSpacing() * 0.5f - getIconsWidth() * 0.5f;
            float y = getInY() + getIconsSpacing() * 0.5f + getIconsHeight() * 0.5f;
            Vector2 center = new Vector2(x, y);
            localToScreen(center);
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

    public float getIconsWidth() {
        return iconsWidth;
    }

    public void setIconsWidth(float iconsWidth) {
        if (this.iconsWidth != iconsWidth) {
            this.iconsWidth = iconsWidth;
            invalidate(true);
        }
    }

    private float getLayoutIconsWidth() {
        return iconsWidth == 0 || iconsWidth == MATCH_PARENT ? getTitleFontHeight() : iconsWidth;
    }

    public float getIconsHeight() {
        return iconsHeight;
    }

    public void setIconsHeight(float iconsHeight) {
        if (this.iconsHeight != iconsHeight) {
            this.iconsHeight = iconsHeight;
            invalidate(true);
        }
    }

    private float getLayoutIconsHeight() {
        return iconsHeight == 0 || iconsHeight == MATCH_PARENT ? getTitleFontHeight() : iconsHeight;
    }

    public float getIconsSpacing() {
        return iconsSpacing;
    }

    public void setIconsSpacing(float iconsSpacing) {
        if (this.iconsSpacing != iconsSpacing) {
            this.iconsSpacing = iconsSpacing;
            invalidate(true);
        }
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

    protected boolean isWrapContent() {
        return getPrefWidth() == WRAP_CONTENT || getPrefHeight() == WRAP_CONTENT;
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
