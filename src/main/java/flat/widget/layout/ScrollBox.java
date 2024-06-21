package flat.widget.layout;

import flat.animations.StateInfo;
import flat.events.ScrollEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Gadget;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enuns.Direction;
import flat.widget.enuns.Policy;
import flat.widget.enuns.Visibility;
import flat.widget.value.ScrollBar;

public class ScrollBox extends Parent {

    private float scrollX, scrollY;
    private Widget content;
    private ScrollBar verticalBar, horizontalBar;
    private Policy verticalPolicy = Policy.AS_NEEDED, horizontalPolicy = Policy.AS_NEEDED;

    public ScrollBox() {

    }

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*setScrollX(theme.asNumber("scroll-x", getScrollX()));
        setScrollY(theme.asNumber("scroll-y", getScrollY()));*/
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        /*if (getAttrs() == null) return;

        StateInfo info = getStateInfo();

        setHorizontalPolicy(getAttrs().asConstant("horizontal-policy", info, getHorizontalPolicy()));
        setVerticalPolicy(getAttrs().asConstant("vertical-policy", info, getVerticalPolicy()));*/
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Gadget child;
        while ((child = children.next()) != null ) {
            Widget widget = child.getWidget();
            if (widget != null) {
                if (content == null) {
                    setContent(widget);
                } else if (widget instanceof ScrollBar) {
                    ScrollBar bar = (ScrollBar) widget;
                    if (bar.getDirection() == Direction.HORIZONTAL || bar.getDirection() == Direction.IHORIZONTAL) {
                        if (horizontalBar == null) {
                            setHorizontalBar(bar);
                        }
                    } else {
                        if (verticalBar == null) {
                            setVerticalBar(bar);
                        }
                    }
                }
                if (content != null && horizontalBar != null && verticalBar != null) {
                    break;
                }
            }
        }
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        return contains(x, y) ? super.findByPosition(x, y, includeDisabled) : null;
    }

    @Override
    public void onDraw(SmartContext context) {
        context.setTransform2D(getTransform());
        Shape clip = backgroundClip(context);

        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        if (content != null) {
            content.onDraw(context);
        }
        if (verticalBar != null && verticalBar.getVisibility() == Visibility.VISIBLE) {
            verticalBar.onDraw(context);
        }
        if (horizontalBar != null && horizontalBar.getVisibility() == Visibility.VISIBLE) {
            horizontalBar.onDraw(context);
        }

        context.setTransform2D(null);
        context.setClip(clip);
    }

    @Override
    public void onMeasure() {
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        if (content != null) {
            content.onMeasure();
        }
        if (verticalBar != null) {
            verticalBar.onMeasure();
        }
        if (horizontalBar != null) {
            horizontalBar.onMeasure();
        }

        if (content != null && content.getVisibility() != Visibility.GONE) {
            if (getPrefWidth() == WRAP_CONTENT) {
                mWidth = content.mWidth() + offWidth;
                if (verticalBar != null && verticalPolicy == Policy.ALWAYS) {
                    mWidth += verticalBar.mWidth();
                }
            }
            if (getPrefHeight() == WRAP_CONTENT) {
                mHeight = content.mHeight() + offHeight;
                if (horizontalBar != null && horizontalPolicy == Policy.ALWAYS) {
                    mHeight += horizontalBar.mHeight();
                }
            }
        }
        setMeasure(mWidth + getMarginLeft() + getMarginRight(), mHeight + getMarginTop() + getMarginBottom());
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);

        if (content != null && content.getVisibility() != Visibility.GONE) {
            float cw = content.mWidth();
            float ch = content.mHeight();;
            if (content.mWidth() == MATCH_PARENT) {
                cw = width - (verticalBar != null && verticalPolicy == Policy.ALWAYS ? verticalBar.mWidth() : 0);
            }
            if (content.mHeight() == MATCH_PARENT) {
                ch = height - (horizontalBar != null && horizontalPolicy == Policy.ALWAYS ? horizontalBar.mHeight() : 0);
            }

            content.onLayout(cw, ch);
            if (horizontalBar != null) {
                if (horizontalPolicy == Policy.ALWAYS) {
                    horizontalBar.setVisibility(Visibility.VISIBLE);
                }
                if (getInWidth() >= content.lWidth()) {
                    horizontalBar.setMaxRange(1);
                    horizontalBar.setRange(1);
                    if (horizontalPolicy == Policy.AS_NEEDED) {
                        horizontalBar.setVisibility(Visibility.GONE);
                    }
                } else {
                    horizontalBar.setMaxRange(content.lWidth());
                    horizontalBar.setRange(getInWidth());
                    if (horizontalPolicy != Policy.NEVER) {
                        horizontalBar.setVisibility(Visibility.VISIBLE);
                    }
                }
            }
            if (verticalBar != null) {
                if (verticalPolicy == Policy.ALWAYS) {
                    verticalBar.setVisibility(Visibility.VISIBLE);
                }
                if (getInHeight() >= content.lHeight()) {
                    verticalBar.setMaxRange(1);
                    verticalBar.setRange(1);
                    if (verticalPolicy == Policy.AS_NEEDED) {
                        verticalBar.setVisibility(Visibility.GONE);
                    }
                } else {
                    verticalBar.setMaxRange(content.lHeight());
                    verticalBar.setRange(getInHeight());
                    if (verticalPolicy != Policy.NEVER) {
                        verticalBar.setVisibility(Visibility.VISIBLE);
                    }
                }
            }
            boolean hbar = horizontalBar != null && horizontalBar.getVisibility() != Visibility.GONE;
            boolean vbar = verticalBar != null && verticalBar.getVisibility() != Visibility.GONE;
            float v = 0, h = 0;

            // VERTICAL PRIORITY
            if (vbar) {
                verticalBar.onLayout(
                        Math.min(verticalBar.mWidth(), width),
                        Math.min(verticalBar.mHeight(), height));
                v = verticalBar.lWidth();
            }
            if (hbar) {
                horizontalBar.onLayout(
                        Math.min(horizontalBar.mWidth(), width - v),
                        Math.min(horizontalBar.mHeight(), height));
                h = horizontalBar.lHeight();
            }
            if (vbar) {
                verticalBar.setPosition(width - v, 0);
            }
            if (hbar) {
                horizontalBar.setPosition(0, height - h);
            }

            float mx = Math.max(0, (content.lWidth() + getPaddingLeft() + getPaddingRight() + v) - getInWidth());
            float my = Math.max(0, (content.lHeight() + getPaddingTop() + getPaddingBottom() + h) - getInHeight());
            content.setPosition(-(scrollX * mx) + getPaddingLeft(), -(scrollY * my) + getPaddingTop());
        }
    }

    @Override
    public void fireScroll(ScrollEvent scrollEvent) {
        if (!scrollEvent.isConsumed() && content != null && verticalBar != null
                && verticalBar.getVisibility() == Visibility.VISIBLE) {

            if (content.getHeight() > 0 && getHeight() > 0) {
                verticalBar.setValue(getScrollY() - (scrollEvent.getDeltaY() * getHeight() / content.getHeight()) / 6f);
            } else {
                verticalBar.setValue(getScrollY() - (scrollEvent.getDeltaY() * 0.1f));
            }
            scrollEvent.consume();
        }
        super.fireScroll(scrollEvent);
    }

    @Override
    public void remove(Widget widget) {
        if (widget == content) content = null;
        if (widget == horizontalBar) {
            horizontalBar.setOnValueChange(null);
            horizontalBar = null;
        }
        if (widget == verticalBar) {
            verticalBar.setOnValueChange(null);
            verticalBar = null;
        }
        super.remove(widget);
    }

    public Widget getContent() {
        return content;
    }

    public void setContent(Widget content) {
        if (this.content != content) {
            if (this.content != null) {
                remove(this.content);
            }
            this.content = content;
            if (content != null) {
                add(content);
            }
        }
    }

    public ScrollBar getHorizontalBar() {
        return horizontalBar;
    }

    public void setHorizontalBar(ScrollBar horizontalBar) {
        if (this.horizontalBar != horizontalBar) {
            if (this.horizontalBar != null) {
                this.horizontalBar.setOnValueChange(null);
                remove(this.horizontalBar);
            }
            this.horizontalBar = horizontalBar;
            if (horizontalBar != null) {
                // todo - save as default listener {remove on scene detach}
                horizontalBar.setOnValueChange((event) -> setScrollX(horizontalBar.getValue()));
                add(horizontalBar);
            }
        }
    }

    public ScrollBar getVerticalBar() {
        return verticalBar;
    }

    public void setVerticalBar(ScrollBar verticalBar) {
        if (this.verticalBar != verticalBar) {
            if (this.verticalBar != null) {
                this.verticalBar.setOnValueChange(null);
                remove(this.verticalBar);
            }
            this.verticalBar = verticalBar;
            if (verticalBar != null) {
                verticalBar.setOnValueChange((event) -> setScrollY(verticalBar.getValue()));
                add(verticalBar);
            }
        }
    }

    public float getScrollX() {
        return scrollX;
    }

    public void setScrollX(float scrollX) {
        scrollX = Math.min(1, Math.max(0, scrollX));

        if (this.scrollX != scrollX) {
            this.scrollX = scrollX;
            if (horizontalBar != null) horizontalBar.setValue(scrollX);
            invalidate(true);
        }
    }

    public float getScrollY() {
        return scrollY;
    }

    public void setScrollY(float scrollY) {
        scrollY = Math.min(1, Math.max(0, scrollY));

        if (this.scrollY != scrollY) {
            this.scrollY = scrollY;
            if (verticalBar != null) verticalBar.setValue(scrollY);
            invalidate(true);
        }
    }

    public Policy getHorizontalPolicy() {
        return horizontalPolicy;
    }

    public void setHorizontalPolicy(Policy horizontalPolicy) {
        if (this.horizontalPolicy != horizontalPolicy) {
            this.horizontalPolicy = horizontalPolicy;
            invalidate(true);
        }
    }

    public Policy getVerticalPolicy() {
        return verticalPolicy;
    }

    public void setVerticalPolicy(Policy verticalPolicy) {
        if (this.verticalPolicy != verticalPolicy) {
            this.verticalPolicy = verticalPolicy;
            invalidate(true);
        }
    }
}
