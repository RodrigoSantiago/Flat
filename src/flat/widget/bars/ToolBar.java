package flat.widget.bars;

import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Gadget;
import flat.widget.Menu;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.dialogs.MenuItem;
import flat.widget.enuns.Direction;
import flat.widget.enuns.Visibility;
import flat.widget.text.Button;
import flat.widget.text.TextField;

import java.util.*;

public class ToolBar extends Parent {

    private String title;
    private Font titleFont = Font.DEFAULT;
    private float titleTextSize;
    private int titleTextColor;
    private float tWidth, tLayX, tLayWidth;

    private String subtitle;
    private Font subtitleFont = Font.DEFAULT;
    private float subtitleTextSize;
    private int subtitleTextColor;
    private float sWidth;

    private boolean invalidTitleSize;
    private boolean invalidSubtitleSize;

    private float growHeight;

    private Button navButton;
    private OverflowMenu rightButton;

    private TextField textField;
    private Menu menu = new Menu();
    private ArrayList<ToolItem> items = new ArrayList<>();
    private int itensShown;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setTitle(style.asString("title", getTitle()));
        setSubtitle(style.asString("subtitle", getSubtitle()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        UXStyle style = getStyle();
        if (style == null) return;

        StateInfo info = getStateInfo();

        setTitleFont(style.asFont("title-font", info, getTitleFont()));
        setTitleTextColor(style.asColor("title-text-color", info, getTitleTextColor()));
        setTitleTextSize(style.asSize("title-text-size", info, getTitleTextSize()));
        setSubtitleFont(style.asFont("subtitle-font", info, getSubtitleFont()));
        setSubtitleTextColor(style.asColor("subtitle-text-color", info, getSubtitleTextColor()));
        setSubtitleTextSize(style.asSize("subtitle-text-size", info, getSubtitleTextSize()));
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Gadget child;
        while ((child = children.next()) != null ) {
            if (child instanceof ToolItem) {
                addItem((ToolItem) child);
            } else if (rightButton == null && child instanceof OverflowMenu) {
                setRightButton((OverflowMenu) child);
            } else if (navButton == null && child instanceof Button) {
                setNavButton((Button) child);
            } else if (textField == null && child instanceof TextField) {
                setTextField((TextField) child);
            }
        }
    }

    @Override
    public void onMeasure() {
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        float childrenWidth = 0, childrenMinWidth = 0;
        float childrenHeight = 0, childrenMinHeight = 0;

        if (navButton != null) {
            navButton.onMeasure();

            childrenWidth += navButton.getMeasureWidth();
            childrenMinWidth += navButton.getLayoutMinWidth();
            if (navButton.getMeasureHeight() > childrenHeight) {
                childrenHeight = navButton.getMeasureHeight();
            }
            if (navButton.getLayoutMinHeight() > childrenMinHeight) {
                childrenMinHeight += navButton.getLayoutMinHeight();
            }
        }

        if (rightButton != null) {
            rightButton.onMeasure();

            childrenWidth += rightButton.getMeasureWidth();
            childrenMinWidth += rightButton.getLayoutMinWidth();
            if (rightButton.getMeasureHeight() > childrenHeight) {
                childrenHeight = rightButton.getMeasureHeight();
            }
            if (rightButton.getLayoutMinHeight() > childrenMinHeight) {
                childrenMinHeight += rightButton.getLayoutMinHeight();
            }
        }

        childrenWidth += getTitleWidth();

        for (ToolItem child : items) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.Gone) continue;

            childrenWidth += child.getMeasureWidth();
            childrenMinWidth += child.getLayoutMinWidth();
            if (child.getMeasureHeight() > childrenHeight) {
                childrenHeight = child.getMeasureHeight();
            }
            if (child.getLayoutMinHeight() > childrenMinHeight) {
                childrenMinHeight += child.getLayoutMinHeight();
            }
        }

        if (getPrefWidth() == WRAP_CONTENT) {
            mWidth = childrenWidth + offWidth;
        } else if (mWidth < childrenMinWidth + offWidth) {
            mWidth = childrenMinWidth + offWidth;
        }
        if (getPrefHeight() == WRAP_CONTENT) {
            mHeight = childrenHeight + offHeight;
        } else if (mHeight < childrenMinHeight + offHeight) {
            mHeight = childrenMinHeight + offHeight;
        }

        setMeasure(mWidth + getMarginLeft() + getMarginRight(), mHeight + getMarginTop() + getMarginBottom());
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        float x = getInX();
        float w = getInWidth();
        width = getInWidth();

        float rWidth = 0;
        if (items.size() == 1) {
            rWidth = Math.min(w, items.get(0).getMeasureWidth());
        } else if (rightButton != null) {
            rWidth = Math.min(w, rightButton.getMeasureWidth());
        }

        float nWidth = 0;
        if (navButton != null) {
            navButton.onLayout(Math.min(w - rWidth, navButton.getMeasureWidth()), navButton.getMeasureHeight());
            navButton.setPosition(x, getInY());

            nWidth = navButton.getWidth();
        }

        float tWidth = Math.min(w - rWidth - nWidth, getTitleWidth());
        tLayWidth = tWidth;
        tLayX = navButton == null ? 0 : navButton.getWidth() + getInX();

        int prefItensShown = itensShown;
        itensShown = items.size();

        float reaming = width - nWidth - tWidth;
        if (items.size() == 1) {
            ToolItem item = items.get(0);
            item.onLayout(Math.min(reaming, item.getMeasureWidth()), item.getMeasureHeight());
        } else {

            boolean hide = false;
            for (ToolItem item : items) {
                item.onLayout(Math.min(width - nWidth - tWidth, item.getMeasureWidth()), item.getMeasureHeight());
                reaming -= item.getWidth();
                if (reaming < 0 || !item.isShowAction()) {
                    hide = true;
                    break;
                }
            }
            reaming = width - nWidth - tWidth - rWidth;
            if (hide) {
                float m = 0;
                for (int i = 0; i < items.size(); i++) {
                    ToolItem item = items.get(i);
                    m += item.getWidth();
                    if (m > reaming || !item.isShowAction()) {
                        m -= item.getWidth();
                        if (i == items.size() - 1) {
                            m -= items.get(i - 1).getWidth();
                            itensShown = i - 1;
                        } else {
                            itensShown = i;
                        }
                        break;
                    }
                }

                if (rightButton != null) {
                    rightButton.onLayout(rWidth, rightButton.getMeasureHeight());
                    rightButton.setPosition(x + w - rightButton.getWidth(), getInY());
                    rightButton.setVisibility(Visibility.Visible);
                }

                float off = (rightButton == null ? x + w : rightButton.getX()) - m;
                for (int i = 0; i < itensShown; i++) {
                    ToolItem item = items.get(i);
                    item.setPosition(off, getInY());
                    off += item.getWidth();
                }
            } else {
                float m = 0;
                for (int i = 0; i < items.size(); i++) {
                    ToolItem item = items.get(i);
                    m += item.getWidth();
                }
                float off = x + w - m;
                for (int i = 0; i < itensShown; i++) {
                    ToolItem item = items.get(i);
                    item.setPosition(off, getInY());
                    off += item.getWidth();
                }
                if (rightButton != null) {
                    rightButton.setVisibility(Visibility.Gone);
                }
            }
        }

        if (prefItensShown != itensShown) {
            if (rightButton != null) {
                rightButton.setItems(items.subList(itensShown, items.size()));
            }
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        if (title != null && tLayWidth > 0) {
            context.setTransform2D(getTransform());
            context.setTextFont(titleFont);
            context.setTextSize(titleTextSize);
            context.setColor(titleTextColor);
            context.setTextHorizontalAlign(Align.Horizontal.LEFT);
            context.setTextVerticalAlign(Align.Vertical.TOP);
            context.drawTextSlice(tLayX, getInY(), tLayWidth, title);
        }

        for (Widget widget : getChildren()) {
            if (widget.getVisibility() == Visibility.Visible) {
                if (items.indexOf(widget) < itensShown) {
                    widget.onDraw(context);
                }
            }
        }
    }

    @Override
    public void remove(Widget widget) {
        if (widget == textField) {
            textField = null;
        }
        if (widget == navButton) {
            navButton = null;
        }
        if (widget instanceof ToolItem) {
            items.remove(widget);
        }
        if (widget == rightButton) {
            rightButton = null;
        }
        super.remove(widget);
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.Visible || getVisibility() == Visibility.Invisible)) {
            List<Widget> children = getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                Widget child = children.get(i);
                if (items.indexOf(child) < itensShown) {
                    Widget found = child.findByPosition(x, y, includeDisabled);
                    if (found != null) return found;
                }
            }
            return isClickable() && contains(x, y) ? this : null;
        } else {
            return null;
        }
    }

    public void addItem(ToolItem item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
            add(item);
        }
        invalidate(true);
    }

    public TextField getTextField() {
        return textField;
    }

    public void setTextField(TextField textField) {
        if (this.textField != textField) {
            if (this.textField != null) remove(this.textField);
            this.textField = textField;
            if (textField != null) {
                add(textField);
            }
            invalidate(true);
        }
    }

    public OverflowMenu getRightButton() {
        return rightButton;
    }

    public void setRightButton(OverflowMenu rightButton) {
        if (this.rightButton != rightButton) {
            if (this.rightButton != null) remove(this.rightButton);
            this.rightButton = rightButton;
            if (rightButton != null) {
                add(rightButton);
            }
            invalidate(true);
        }
    }

    public void setNavButton(Button navButton) {
        if (this.navButton != navButton) {
            if (this.navButton != null) remove(this.navButton);
            this.navButton = navButton;
            if (navButton != null) {
                add(navButton);
            }
            invalidate(true);
        }
    }

    public Button getNavButton() {
        return navButton;
    }

    protected float getTitleWidth() {
        if (invalidTitleSize) {
            if (title == null) {
                return tWidth = 0;
            }
            tWidth = titleFont.getWidth(title, titleTextSize, 1);
            invalidTitleSize = false;
        }
        return tWidth;
    }

    protected float getSubtitleWidth() {
        if (invalidSubtitleSize) {
            if (subtitle == null) {
                return sWidth = 0;
            }
            sWidth = subtitleFont.getWidth(subtitle, subtitleTextSize, 1);
            invalidSubtitleSize = false;
        }
        return sWidth;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (!Objects.equals(this.title, title)) {
            this.title = title;
            invalidTitleSize = true;
            invalidate(true);
        }
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(Font titleFont) {
        if (this.titleFont != titleFont) {
            this.titleFont = titleFont;
            invalidTitleSize = true;
            invalidate(true);
        }
    }

    public float getTitleTextSize() {
        return titleTextSize;
    }

    public void setTitleTextSize(float titleTextSize) {
        if (this.titleTextSize != titleTextSize) {
            this.titleTextSize = titleTextSize;
            invalidTitleSize = true;
            invalidate(true);
        }
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColor(int titleTextColor) {
        if (this.titleTextColor != titleTextColor) {
            this.titleTextColor = titleTextColor;
            invalidate(false);
        }
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        if (!Objects.equals(this.subtitle, subtitle)) {
            this.subtitle = subtitle;
            invalidSubtitleSize = true;
            invalidate(true);
        }
    }

    public Font getSubtitleFont() {
        return subtitleFont;
    }

    public void setSubtitleFont(Font subtitleFont) {
        if (this.subtitleFont != subtitleFont) {
            this.subtitleFont = subtitleFont;
            invalidSubtitleSize = true;
            invalidate(true);
        }
    }

    public float getSubtitleTextSize() {
        return subtitleTextSize;
    }

    public void setSubtitleTextSize(float subtitleTextSize) {
        if (this.subtitleTextSize != subtitleTextSize) {
            this.subtitleTextSize = subtitleTextSize;
            invalidSubtitleSize = true;
            invalidate(true);
        }
    }

    public int getSubtitleTextColor() {
        return subtitleTextColor;
    }

    public void setSubtitleTextColor(int subtitleTextColor) {
        if (this.subtitleTextColor != subtitleTextColor) {
            this.subtitleTextColor = subtitleTextColor;
            invalidate(false);
        }
    }
}
