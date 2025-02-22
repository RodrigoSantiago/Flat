package flat.data;

import java.util.*;

public class ObservableList<T> implements List<T> {

    private ArrayList<T> data;
    private List<T> unmodifiableData;
    private ListChangeListener<T> changeListener;

    public ObservableList() {
        this.data = new ArrayList<>();
        this.unmodifiableData = Collections.unmodifiableList(data);
    }

    public ListChangeListener<T> getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(ListChangeListener<T> changeListener) {
        this.changeListener = changeListener;
    }

    private void handle(int index, int length, ListChangeListener.Operation operation) {
        if (changeListener != null) {
            changeListener.handle(index, length, operation);
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return unmodifiableData.iterator();
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return data.toArray(a);
    }

    @Override
    public boolean add(T t) {
        if (data.add(t)) {
            this.handle(size() - 1, 1, ListChangeListener.Operation.INSERT);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index > -1) {
            data.remove(index);
            this.handle(index, 1, ListChangeListener.Operation.DELETE);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int size = data.size();
        if (data.addAll(c)) {
            this.handle(size, c.size(), ListChangeListener.Operation.INSERT);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (data.addAll(index, c)) {
            this.handle(index, c.size(), ListChangeListener.Operation.INSERT);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int minIndex = -1;
        int maxIndex = -1;
        for (var item : c) {
            int index = indexOf(item);
            if (index > -1) {
                int lastIndex = lastIndexOf(item);
                if (minIndex == -1 || index < minIndex) minIndex = index;
                if (maxIndex == -1 || lastIndex > maxIndex) maxIndex = lastIndex;
            }
        }
        if (minIndex > -1) {
            data.removeAll(c);
            this.handle(minIndex, (maxIndex - minIndex) + 1, ListChangeListener.Operation.RANGE);
            return true;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int minIndex = -1;
        int maxIndex = -1;
        for (int i = 0; i < data.size(); i++) {
            if (!c.contains(data.get(i))) {
                minIndex = i;
                break;
            }
        }
        if (minIndex > -1) {
            for (int i = data.size() - 1; i >= 0; i--) {
                if (!c.contains(data.get(i))) {
                    maxIndex = i;
                    break;
                }
            }
            data.retainAll(c);
            this.handle(minIndex, (maxIndex - minIndex) + 1, ListChangeListener.Operation.RANGE);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        int size = data.size();
        if (size > 0) {
            data.clear();
            this.handle(0, size, ListChangeListener.Operation.DELETE);
        }
    }

    @Override
    public T get(int index) {
        return data.get(index);
    }

    @Override
    public T set(int index, T element) {
        T old = data.set(index, element);
        this.handle(index, 1, ListChangeListener.Operation.UPDATE);
        return old;
    }

    @Override
    public void add(int index, T element) {
        data.add(index, element);
        this.handle(index, 1, ListChangeListener.Operation.INSERT);
    }

    @Override
    public T remove(int index) {
        T old = data.remove(index);
        this.handle(index, 1, ListChangeListener.Operation.DELETE);
        return old;
    }

    @Override
    public int indexOf(Object o) {
        return data.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return data.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return unmodifiableData.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return unmodifiableData.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return unmodifiableData.subList(fromIndex, toIndex);
    }
}
