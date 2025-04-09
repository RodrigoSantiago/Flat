package flat.widget.structure;

import flat.data.ObservableList;
import flat.widget.Widget;

public class TreeViewAdapter extends ListViewDefaultAdapter<TreeCell> {
    public TreeViewAdapter(ObservableList<TreeCell> data) {
        super(data);
    }

    @Override
    public Widget createListItem() {
        ListItem item = new ListItem();
        item.addStyle("tree-item");
        return item;
    }

    @Override
    public void buildListItem(int index, Widget item) {
        var widget = (ListItem) item;
        widget.setIndex(index);

        TreeCell treeCell = getData().get(index);
        widget.setText(treeCell.getName());
        widget.setLayers(treeCell.getLevels());
        widget.setChangeStateListener((event) -> {
            if (treeCell.isOpen()) {
                treeCell.close();
            } else {
                treeCell.open();
            }
        });

        widget.removeStyle("tree-item-folder");
        widget.removeStyle("tree-item-folder-open");
        if (treeCell.isFolder()) {
            widget.setFollowStyleProperty("icon", true);
            if (!treeCell.isOpen()) {
                widget.addStyle("tree-item-folder");
            } else {
                widget.addStyle("tree-item-folder-open");
            }
        } else {
            widget.setFollowStyleProperty("icon", false);
            widget.setIcon(treeCell.getIcon());
        }
        widget.applyStyle();
    }
}
