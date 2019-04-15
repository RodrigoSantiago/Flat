package flat.widget.layout;

import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.uxml.Controller;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

import java.util.Objects;

public class Page extends Box {
    //[rect header][rect body]{show or hidden}

    private String name;
    private Drawable icon;

    private Tab tab;
    private ActionListener onActivated, onDeactivated;

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        Widget widget = super.findByPosition(x, y, includeDisabled);
        if (widget == this) {
            return null;
        } else {
            return widget;
        }
    }

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setName(style.asString("name", getName()));
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
                onActivated.handle(new ActionEvent(this, ActionEvent.ACTION));
            } else if (!actived && onDeactivated != null) {
                onDeactivated.handle(new ActionEvent(this, ActionEvent.ACTION));
            }
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
