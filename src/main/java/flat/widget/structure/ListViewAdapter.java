package flat.widget.structure;

import flat.widget.Widget;

public abstract class ListViewAdapter<T> {

    private RecycleView listView;

    public int size() {
        return 0;
    }

    public abstract Widget createListItem();

    public void buildListItem(int index, Widget item) {

    }

    public RecycleView getListView() {
        return listView;
    }

    void setListView(RecycleView listView) {
        this.listView = listView;
    }
}
