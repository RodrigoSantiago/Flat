package flat.widget.structure;

import java.util.List;

public class TreeViewDragData {
    private final TreeView source;
    private final List<TreeItemData> items;
    private boolean cancelled;

    public TreeViewDragData(TreeView source, List<TreeItemData> items) {
        this.source = source;
        this.items = items;
    }

    public TreeView getSource() {
        return source;
    }

    public List<TreeItemData> getItems() {
        return items;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled() {
        this.cancelled = true;
    }
}
