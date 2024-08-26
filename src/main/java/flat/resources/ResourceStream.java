package flat.resources;

import flat.window.Application;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;

public class ResourceStream {

    private String name;
    private String resourceName;

    public ResourceStream(String name) {
        this.name = name;

        if (Application.getResourcesManager().exists(name)) {
            this.resourceName = name;

        } else if (name.matches(".*\\.[a-zA-Z_0-9]+")) {
            String[] files = Application.getResourcesManager().listFiles(name);
            for (String fileName : files) {
                if (fileName.matches(name)) {
                    this.resourceName = fileName;
                    break;
                }
            }

        } else {
            String regex = ".*\\Q" + name +"\\E\\.[a-zA-Z_0-9]+";
            String[] files = Application.getResourcesManager().listFiles(name);
            for (String fileName : files) {
                if (fileName.endsWith(name)) {
                    this.resourceName = fileName;
                    break;
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getResourceName() {
        return resourceName;
    }

    public InputStream getStream() {
        return Application.getResourcesManager().getInput(resourceName);
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
        return Objects.equals(name, stream.name) && Objects.equals(resourceName, stream.resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resourceName);
    }
}
