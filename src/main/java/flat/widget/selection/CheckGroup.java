package flat.widget.selection;

import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyleAttrs;
import flat.widget.Gadget;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CheckGroup implements Gadget {

    private String id;
    private CheckBox root;
    private ArrayList<CheckBox> children = new ArrayList<>();

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        if (style == null) return;

        String id = style.asString("id");
        if (id != null) {
            this.id = id;
            if (controller != null) {
                controller.assign(id, this);
            }
        }

        style.link("root", (gadget) -> setRoot((CheckBox) gadget));
    }

    @Override
    public void applyChildren(UXChildren children) {

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Widget getWidget() {
        return null;
    }

    public void setRoot(CheckBox root) {
        if (this.root != root) {
            if (this.root != null) {
                this.root._setLeaderGroup(null);
            }
            this.root = root;

            if (this.root != null) {
                this.root._setLeaderGroup(this);
                checkboxSetActive(this.root, this.root.isActivated());
            }

            updateRoot();
        }
    }

    public CheckBox getRoot() {
        return root;
    }

    public List<CheckBox> checkboxes() {
        return Collections.unmodifiableList(children);
    }

    public void add(CheckBox child) {
        if (child != null) {
            if (!children.contains(child)) {
                children.add(child);
                updateRoot();
            }
        }
    }

    public void remove(CheckBox child) {
        if (child != null){
            if (children.remove(child)) {
                updateRoot();
            }
        }
    }

    void checkboxSetActive(CheckBox checkbox, boolean active) {
        if (checkbox == root) {
            for (CheckBox child : children) {
                child.setActivated(active);
            }
            checkbox._setActivated(active);
            updateRoot();
        } else if (children.contains(checkbox)) {
            checkbox._setActivated(active);
            updateRoot();
        }
    }

    private void updateRoot() {
        if (root != null && children.size() > 0) {
            boolean defined = true;
            boolean activated = children.get(0).isActivated();
            for (int i = 1; i < children.size(); i++) {
                CheckBox child = children.get(i);
                if (child.isActivated() != activated || child.isIndeterminate()) {
                    defined = false;
                    break;
                }
            }
            if (defined) {
                root.setActivated(activated);
            } else {
                root._setActivated(true);
                root.setIndeterminate(true);
            }
            if (root.getGroup() != null) {
                root.getGroup().updateRoot();
            }
        }
    }
}
