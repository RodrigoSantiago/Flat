package flat.widget.structure;

public class TreeViewStyle {
    private final TreeView treeView;
    private final TreeItemCell cell;
    private final TreeItemData data;
    private final ListItem item;
    private final boolean floating;
    private final boolean multiselection;

    public TreeViewStyle(TreeView treeView, TreeItemCell cell, TreeItemData data, ListItem item, boolean floating, boolean multiselection) {
        this.treeView = treeView;
        this.cell = cell;
        this.data = data;
        this.item = item;
        this.floating = floating;
        this.multiselection = multiselection;
    }

    public TreeView getTreeView() {
        return treeView;
    }

    public TreeItemCell getCell() {
        return cell;
    }

    public TreeItemData getData() {
        return data;
    }

    public ListItem getItem() {
        return item;
    }

    public boolean isFloating() {
        return floating;
    }

    public boolean isMultiselection() {
        return multiselection;
    }
}
