package flat.widget.structure;

public class TreeDropPos {
    private final ListItem item;
    private final TreeItemCell cell;
    private final float pos;

    TreeDropPos(ListItem item, TreeItemCell cell, float pos) {
        this.item = item;
        this.cell = cell;
        this.pos = pos;
    }

    public TreeItemCell getCell() {
        return cell;
    }

    public float getPos() {
        return pos;
    }

    ListItem getItem() {
        return item;
    }
}
