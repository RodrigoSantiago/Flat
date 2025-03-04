package flat.resources;

import flat.window.Application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;

public class ResourceStream {

    private String resourceName;
    private boolean folder;

    ResourceStream(String name, boolean folder) {
        this.resourceName = name.length() > 0 && name.charAt(0) == '/' ? name : "/" + name;
        this.folder = folder;
        if (folder && !this.resourceName.endsWith("/")) {
            this.resourceName += "/";
        }
    }

    public ResourceStream(String name) {
        this.resourceName = name.length() > 0 && name.charAt(0) == '/' ? name : "/" + name;
        this.folder = Application.getResourcesManager().isFolder(resourceName);
        if (folder && !this.resourceName.endsWith("/")) {
            this.resourceName += "/";
        }
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

    public byte[] readData() {
        if (isFolder()) {
            return null;
        } else {
            return Application.getResourcesManager().getData(resourceName);
        }
    }

    public void clearCache() {
        Application.getResourcesManager().clearResourceCache(resourceName);
    }

    public void putCache(Object cache) {
        Application.getResourcesManager().putResourceCache(resourceName, cache);
    }

    public Object getCache() {
        return Application.getResourcesManager().getResourceCache(resourceName);
    }

    public ResourceStream getRelative(String path) {
        if (path.startsWith("/")) {
            return new ResourceStream(path);
        }
        if (!path.contains("..")) {
            if (isFolder()) {
                return new ResourceStream(resourceName + path);
            } else {
                int index = resourceName.lastIndexOf("/");
                if (index == -1) {
                    return new ResourceStream("/" + path);
                } else {
                    return new ResourceStream(resourceName.substring(0, index) + "/" + path);
                }
            }
        }

        String[] currentPathParts = resourceName.split("/");
        String[] relativePathParts = path.split("/");
        List<String> pathList = new ArrayList<>(Arrays.asList(currentPathParts));
        if (!isFolder() && pathList.size() > 0 && !pathList.get(pathList.size() - 1).isEmpty()) {
            pathList.remove(pathList.size() - 1);
        }
        for (String part : relativePathParts) {
            if (part.equals("..")) {
                if (pathList.size() > 1) {
                    pathList.remove(pathList.size() - 1);
                }
            } else if (!part.equals(".") && !part.isEmpty()) {
                pathList.add(part);
            }
        }
        String resolvedPath = String.join("/", pathList);
        if (!resolvedPath.startsWith("/")) {
            resolvedPath = "/" + resolvedPath;
        }
        return new ResourceStream(resolvedPath);
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
