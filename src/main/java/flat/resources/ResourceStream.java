package flat.resources;

import flat.window.Application;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;

public class ResourceStream {

    private String resourceName;
    private boolean folder;

    ResourceStream(String name, boolean folder) {
        this.resourceName = name;
        this.folder = folder;
    }

    public ResourceStream(String name) {
        this.resourceName = name;
        this.folder = Application.getResourcesManager().isFolder(resourceName);
    }

    public boolean isFolder() {
        return folder;
    }

    public List<ResourceStream> getFiles() {
        if (isFolder()) {
            return Application.getResourcesManager().listFiles(resourceName);

        } else {
            return new ArrayList<>();
        }
    }

    public String getResourceName() {
        return resourceName;
    }

    public InputStream getStream() {
        if (isFolder()) {
            return null;
        } else {
            return Application.getResourcesManager().getInput(resourceName);
        }
    }

    public void putCache(Object cache) {
        Application.getResourcesManager().putResourceCache(resourceName, cache);
    }

    public Object getCache() {
        return Application.getResourcesManager().getResourceCache(resourceName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceStream stream)) return false;
        return Objects.equals(resourceName, stream.resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName);
    }
}
