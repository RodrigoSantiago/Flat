package flat.widget.structure;

import flat.data.ObservableList;
import flat.exception.FlatException;
import flat.graphics.image.Drawable;

import java.util.ArrayList;
import java.util.List;

public class TreeCell {
    private final ObservableList<TreeCell> root;
    private final ArrayList<TreeCell> children = new ArrayList<>();
    private boolean open;
    private TreeCell parent;
    private String name;
    private boolean folder;
    private Drawable icon;

    public TreeCell(ObservableList<TreeCell> root, String name, boolean folder, Drawable icon) {
        this.root = root;
        this.name = name;
        this.folder = folder;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public boolean isFolder() {
        return folder;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getLevels() {
        return parent == null ? 0 : 1 + parent.getLevels();
    }

    private void setParent(TreeCell parent) {
        if (this.parent != null) {
            throw new FlatException("No");
        }
        this.parent = parent;
        this.open = false;
    }

    public void add(TreeCell item) {
        item.setParent(this);

        int index = children.size();
        children.add(item);
        if (isOpen()) {
            root.add(root.indexOf(this) + index + 1, item);
        }
    }

    public void add(int index, TreeCell item) {
        item.setParent(this);

        children.add(index, item);
        if (isOpen()) {
            root.add(root.indexOf(this) + index + 1, item);
        }
    }

    public void addAll(List<TreeCell> item) {
        for (var i : item) {
            i.setParent(this);
        }
        int index = children.size();
        children.addAll(item);
        if (isOpen()) {
            root.addAll(root.indexOf(this) + index + 1, item);
        }
    }

    public void addAll(int index, List<TreeCell> item) {
        for (var i : item) {
            i.setParent(this);
        }
        children.addAll(item);
        if (isOpen()) {
            root.addAll(root.indexOf(this) + index + 1, item);
        }
    }

    public void remove(TreeCell item) {
        item.setParent(null);
        item.close();
        int index = children.indexOf(item);
        children.remove(item);
        if (isOpen()) {
            root.remove(root.indexOf(this) + index + 1);
        }
    }

    public void clear(List<TreeCell> item) {
        for (var i : item) {
            i.setParent(null);
        }
        if (isOpen()) {
            root.removeRange(root.indexOf(this) + 1, getTotalOpenItems());
        }
        children.clear();
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        if (!isOpen()) {
            open = true;
            root.addAll(root.indexOf(this) + 1, children);
        }
    }

    public void close() {
        if (isOpen()) {
            int total = getTotalOpenItems();
            for (var i : children) {
                i.open = false;
            }
            open = false;
            root.removeRange(root.indexOf(this) + 1, total);
        }
    }

    public int getTotalOpenItems() {
        int count = 0;
        if (isOpen()) {
            for (TreeCell child : children) {
                count += child.getTotalOpenItems() + 1;
            }
        }
        return count;
    }
}
