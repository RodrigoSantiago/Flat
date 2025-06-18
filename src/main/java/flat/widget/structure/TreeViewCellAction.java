package flat.widget.structure;

public class TreeViewCellAction {
    private final TreeView treeView;
    private final TreeItemCell cell;
    private final TreeItemData data;
    private final ListItem item;

    public TreeViewCellAction(TreeView treeView, TreeItemCell cell, TreeItemData data, ListItem item) {
        this.treeView = treeView;
        this.cell = cell;
        this.data = data;
        this.item = item;
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
}
