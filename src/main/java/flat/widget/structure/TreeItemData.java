package flat.widget.structure;

public interface TreeItemData {
    Object getId();
    Object getParentId();
    int compareTo(TreeItemData data);
    boolean isFolder();
}
