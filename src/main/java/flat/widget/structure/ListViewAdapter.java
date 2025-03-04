package flat.widget.structure;

import flat.widget.Widget;

public abstract class ListViewAdapter<T> {

    private ListView listView;

    public int size() {
        return 0;
    }

    public abstract Widget createListItem();

    public void buildListItem(int index, Widget item) {

    }

    public ListView getListView() {
        return listView;
    }

    void setListView(ListView listView) {
        this.listView = listView;
    }
}
