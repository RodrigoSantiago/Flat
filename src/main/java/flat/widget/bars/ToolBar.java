package flat.widget.bars;

import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.uxml.*;
import flat.widget.Gadget;
import flat.widget.Menu;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enuns.Visibility;
import flat.widget.text.Button;
import flat.widget.text.TextField;

import java.util.ArrayList;
import java.util.Objects;

public class ToolBar extends Parent {

    private String text;
    private Font font = Font.DEFAULT;
    private float textSize;
    private int textColor;
    private float tWidth, tLayX, tLayWidth;

    private boolean invalidTitleSize;

    private float growHeight;

    private Button navButton;
    private OverflowMenu rightButton;

    private TextField textField;
    private Menu menu = new Menu();
    private ArrayList<ToolItem> items = new ArrayList<>();
    private int itensShown;

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*setText(theme.asString("text", getText()));*/
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        /*UXStyle style = getAttrs();
        if (style == null) return;

        StateInfo info = getStateInfo();

        setFont(style.asFont("font", info, getFont()));
        setTextColor(style.asColor("text-color", info, getTextColor()));
        setTextSize(style.asSize("text-size", info, getTextSize()));*/
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

            childrenWidth += navButton.mWidth();
            childrenMinWidth += navButton.lMinWidth();
            if (navButton.mHeight() > childrenHeight) {
                childrenHeight = navButton.mHeight();
            }
            if (navButton.lMinHeight() > childrenMinHeight) {
                childrenMinHeight = navButton.lMinHeight();
            }
        }

        if (rightButton != null) {
            rightButton.onMeasure();

            childrenWidth += rightButton.mWidth();
            childrenMinWidth += rightButton.lMinWidth();
            if (rightButton.mHeight() > childrenHeight) {
                childrenHeight = rightButton.mHeight();
            }
            if (rightButton.lMinHeight() > childrenMinHeight) {
                childrenMinHeight = rightButton.lMinHeight();
            }
        }

        childrenWidth += getTitleWidth();
        if (font.getHeight(textSize) > childrenHeight) {
            childrenHeight = font.getHeight(textSize);
        }

        for (ToolItem child : items) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.GONE) continue;

            childrenWidth += child.mWidth();
            childrenMinWidth += child.lMinWidth();
            if (child.mHeight() > childrenHeight) {
                childrenHeight = child.mHeight();
            }
            if (child.lMinHeight() > childrenMinHeight) {
                childrenMinHeight = child.lMinHeight();
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
        setLayout(Math.min(width, mWidth()), Math.min(mHeight(), height));
        float x = getInX();
        float w = getInWidth();
        width = getInWidth();

        float rWidth = 0;
        if (items.size() == 1) {
            rWidth = Math.min(w, items.get(0).mWidth());
        } else if (rightButton != null) {
            rWidth = Math.min(w, rightButton.mWidth());
        }

        float nWidth = 0;
        if (navButton != null) {
            navButton.onLayout(Math.min(w - rWidth, navButton.mWidth()), navButton.mHeight());
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
            item.onLayout(Math.min(reaming, item.mWidth()), item.mHeight());
        } else {

            boolean hide = false;
            for (ToolItem item : items) {
                item.onLayout(Math.min(width - nWidth - tWidth, item.mWidth()), item.mHeight());
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
                    rightButton.onLayout(rWidth, rightButton.mHeight());
                    rightButton.setPosition(x + w - rightButton.getWidth(), getInY());
                    rightButton.setVisibility(Visibility.VISIBLE);
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
                    rightButton.setVisibility(Visibility.GONE);
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

        if (text != null && tLayWidth > 0) {
            context.setTransform2D(getTransform());
            context.setTextFont(font);
            context.setTextSize(textSize);
            context.setColor(textColor);
            context.setTextHorizontalAlign(Align.Horizontal.LEFT);
            context.setTextVerticalAlign(Align.Vertical.TOP);
            context.drawTextSlice(tLayX, getInY() + getInHeight() - font.getHeight(textSize), tLayWidth, text);
        }

        for (Widget widget : getChildrenIterable()) {
            if (widget.getVisibility() == Visibility.VISIBLE) {
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
                (getVisibility() == Visibility.VISIBLE || getVisibility() == Visibility.INVISIBLE)) {
            for (Widget child : getChildrenIterableReverse()) {
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
            if (text == null) {
                return tWidth = 0;
            }
            tWidth = font.getWidth(text, textSize, 1);
            invalidTitleSize = false;
        }
        return tWidth;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            invalidTitleSize = true;
            invalidate(true);
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (this.font != font) {
            this.font = font;
            invalidTitleSize = true;
            invalidate(true);
        }
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            invalidTitleSize = true;
            invalidate(true);
        }
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        if (this.textColor != textColor) {
            this.textColor = textColor;
            invalidate(false);
        }
    }
}
