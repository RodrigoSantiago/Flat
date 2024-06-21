package flat.widget.layout;

import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXTheme;
import flat.widget.Widget;

import java.util.Objects;

public class Page extends Box {

    private String name;
    private Drawable icon;

    private Tab tab;
    private ActionListener onActivated, onDeactivated;

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        //setName(theme.asString("name", getName()));
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }

    @Override
    public void setActivated(boolean actived) {
        if (isEnabled() && tab != null) {
            super.setActivated(actived);
            if (actived && onActivated != null) {
                onActivated.handle(new ActionEvent(this));
            } else if (!actived && onDeactivated != null) {
                onDeactivated.handle(new ActionEvent(this));
            }
        }
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        Widget widget = super.findByPosition(x, y, includeDisabled);
        if (widget == this) {
            return null;
        } else {
            return widget;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!Objects.equals(this.name, name)) {
            this.name = name;
            if (tab != null) {
                tab.setLabelValue(this, name);
            }
            invalidate(true);
        }
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            if (tab != null) {
                //tab.setLabelValue(this, name); ?
            }
            invalidate(true);
        }
    }

    public ActionListener getOnActivated() {
        return onActivated;
    }

    public void setOnActivated(ActionListener onActivated) {
        this.onActivated = onActivated;
    }

    public ActionListener getOnDeactivated() {
        return onDeactivated;
    }

    public void setOnDeactivated(ActionListener onDeactivated) {
        this.onDeactivated = onDeactivated;
    }

    void setTab(Tab tab) {
        this.tab = tab;
    }
}
