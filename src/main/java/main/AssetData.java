package main;

import flat.widget.structure.TreeItemData;

public class AssetData implements TreeItemData {
    private final String name;
    private final boolean folder;

    public AssetData(String name, boolean folder) {
        this.name = name;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object getId() {
        return name;
    }

    @Override
    public Object getParentId() {
        return name.contains("/") ? name.substring(0, name.lastIndexOf("/")) : null;
    }

    @Override
    public int compareTo(TreeItemData data) {
        return name.compareTo(((AssetData)data).name);
    }

    @Override
    public boolean isFolder() {
        return folder;
    }
}
