package flat.widget.selection;

import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyleAttrs;
import flat.widget.Gadget;
import flat.widget.Widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CheckboxGroup implements Gadget {

    private String id;
    private Checkbox root;
    private ArrayList<Checkbox> children = new ArrayList<>();

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

        style.link("root", (gadget) -> setRoot((Checkbox) gadget));
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

    public void setRoot(Checkbox root) {
        if (this.root != root) {
            this.root = root;

            if (this.root != null) {
                checkboxSetActive(this.root, this.root.isActivated());
            }

            updateRoot();
        }
    }

    public Checkbox getRoot() {
        return root;
    }

    public void checkboxAdd(Checkbox child) {
        if (child != null) {
            if (!children.contains(child)) {
                children.add(child);
                updateRoot();
            }
        }
    }

    public void checkboxRemove(Checkbox child) {
        if (child != null){
            if (children.remove(child)) {
                updateRoot();
            }
        }
    }

    public List<Checkbox> checkboxes() {
        return Collections.unmodifiableList(children);
    }

    void checkboxSetActive(Checkbox checkbox, boolean active) {
        if (children.contains(checkbox)) {
            if (checkbox == root) {
                for (Checkbox child : children) {
                    child._setActivated(active);
                }
            }
            checkbox._setActivated(active);
            updateRoot();
        }
    }

    private void updateRoot() {
        if (root != null && children.size() > 0) {
            boolean defined = true;
            boolean activated = children.get(0).isActivated();
            for (int i = 1; i < children.size(); i++) {
                Checkbox child = children.get(i);
                if (child.isActivated() != activated) {
                    defined = false;
                    break;
                }
            }
            if (defined) {
                root._setActivated(activated);
            } else {
                root._setActivated(false);
                root.setUndefined(true);
            }
        }
    }
}
