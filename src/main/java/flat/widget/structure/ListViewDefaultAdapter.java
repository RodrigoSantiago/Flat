package flat.widget.structure;

import flat.data.ListChangeListener;
import flat.data.ObservableList;
import flat.widget.Widget;

public class ListViewDefaultAdapter<T> extends ListViewAdapter<T> {

    private ObservableList<T> data;

    public ListViewDefaultAdapter(ObservableList<T> data) {
        this.data = data;
        this.data.setChangeListener((index, length, operation) -> {
            if (getListView() != null) {
                if (operation == ListChangeListener.Operation.UPDATE) {
                    getListView().refreshItems(index, length);
                } else {
                    getListView().refreshItems(index);
                }
            }
        });
    }

    public ObservableList<T> getData() {
        return data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Widget createListItem() {
        return new ListItem();
    }

    @Override
    public void buildListItem(int index, Widget item) {
        var label = (ListItem) item;
        label.setIndex(index);
        label.setText(String.valueOf(data.get(index)));
    }
}
