package flat.widget.structure;

public class TreeDropPos {
    private final ListItem item;
    private final TreeItemCell cell;
    private final float pos;
    private final float realPos;

    TreeDropPos(ListItem item, TreeItemCell cell, float pos, float realPos) {
        this.item = item;
        this.cell = cell;
        this.pos = pos;
        this.realPos = realPos;
    }

    public TreeItemCell getCell() {
        return cell;
    }

    public float getPos() {
        return pos;
    }
    
    public float getRealPos() {
        return realPos;
    }
    
    ListItem getItem() {
        return item;
    }
}
