package flat.widget.selection;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.math.Mathf;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.widget.State;
import flat.widget.Widget;

public class CheckBox extends Widget {

    // Properties
    private UXListener<ActionEvent> toggleListener;
    private CheckGroup leaderGroup;
    private CheckGroup group;
    private boolean indeterminate;

    private Drawable icon;
    private Drawable indeterminateIcon;
    private int color;

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*setActivated(getAttrs().asBool("activated", isActivated()));
        setIndeterminate(getAttrs().asBool("indeterminate", isIndeterminate()));

        theme.link("group", (gadget) -> setGroup((CheckGroup) gadget));*/
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        /*if (getAttrs() == null) return;

        StateInfo info = getStateInfo();

        setColor(getAttrs().asColor("color", info, getColor()));

        Resource res = getAttrs().asResource("icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIcon(drawable);
            }
        }
        res = getAttrs().asResource("icon-indeterminate", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIndeterminateIcon(drawable);
            }
        }*/
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), 0, context);
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        context.setTransform2D(getTransform());
        context.setColor(color);

        StateInfo info = getStateInfo();
        Drawable ic = indeterminate && indeterminateIcon != null ? indeterminateIcon : icon;
        ic.draw(context, x, y, width, height, info.get(State.ACTIVATED));

        if (isRippleEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransform());
            getRipple().drawRipple(context, null, getRippleColor());
            context.setTransform2D(null);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEnabled()) {
            getRipple().setSize(Mathf.sqrt(getInWidth() * getInWidth() + getInHeight() * getInHeight()) * 0.75f);
            getRipple().fire(getInX() + getInWidth() / 2f, getInY() + getInHeight() / 2f);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            toggle();
        }
    }

    public UXListener<ActionEvent> getToggleListener() {
        return toggleListener;
    }

    public void setToggleListener(UXListener<ActionEvent> toggleListener) {
        this.toggleListener = toggleListener;
    }

    public void toggle() {
        setActivated(isIndeterminate() || !isActivated());
    }

    public void fireToggle(ActionEvent event) {
        if (toggleListener != null) {
            toggleListener.handle(event);
        }
    }

    public CheckGroup getGroup() {
        return group;
    }

    public void setGroup(CheckGroup group) {
        if (this.group != group) {
            CheckGroup oldGroup = this.group;

            this.group = group;

            if (oldGroup != null) {
                oldGroup.remove(this);
            }

            if (group != null) {
                group.add(this);
            }
        }
    }

    CheckGroup getLeaderGroup() {
        return leaderGroup;
    }

    void _setLeaderGroup(CheckGroup group) {
        this.leaderGroup = group;
    }

    void setLeaderGroup(CheckGroup group) {
        if (this.leaderGroup != group) {
            CheckGroup oldGroup = this.leaderGroup;

            this.leaderGroup = group;

            if (oldGroup != null) {
                oldGroup.setRoot(null);
            }

            if (group != null) {
                group.setRoot(this);
            }
        }
    }

    void _setActivated(boolean activated) {
        if (activated != isActivated()) {
            super.setActivated(activated);

            fireToggle(new ActionEvent(this));
        }
    }

    public void setActivated(boolean actived) {
        if (this.isActivated() != actived || isIndeterminate()) {
            indeterminate = false;

            if (group == null) {
                _setActivated(actived);
            } else {
                group.checkboxSetActive(this, actived);
            }
            if (leaderGroup != null) {
                leaderGroup.checkboxSetActive(this, actived);
            }
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (this.indeterminate != indeterminate) {
            this.indeterminate = indeterminate;
            invalidate(false);
        }
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            invalidate(false);
        }
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(false);
        }
    }

    public Drawable getIndeterminateIcon() {
        return indeterminateIcon;
    }

    public void setIndeterminateIcon(Drawable indeterminateIcon) {
        if (this.indeterminateIcon != indeterminateIcon) {
            this.indeterminateIcon = indeterminateIcon;
            invalidate(false);
        }
    }
}
