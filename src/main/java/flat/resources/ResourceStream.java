package flat.resources;

import flat.exception.FlatException;
import flat.window.Application;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ResourceStream {

    private String resourceName;
    private File file;
    private boolean folder;

    ResourceStream(String name, boolean folder) {
        this.resourceName = !name.isEmpty() && name.charAt(0) == '/' ? name : "/" + name;
        this.folder = folder;
        if (folder && !this.resourceName.endsWith("/")) {
            this.resourceName += "/";
        }
    }

    public ResourceStream(String name) {
        this.resourceName = !name.isEmpty() && name.charAt(0) == '/' ? name : "/" + name;
        this.folder = Application.getResourcesManager().isFolder(resourceName);
        if (folder && !this.resourceName.endsWith("/")) {
            this.resourceName += "/";
        }
    }

    public ResourceStream(File file) {
        this.file = file;
        this.resourceName = file.getAbsolutePath().replaceAll("\\\\", "/");
        this.folder = file.isDirectory();
        if (folder && !this.resourceName.endsWith("/")) {
            this.resourceName += "/";
        }
    }

    public boolean isFolder() {
        return folder;
    }

    public List<ResourceStream> getFiles() {
        if (isFolder()) {
            if (file != null) {
                File[] files = file.listFiles();
                if (files != null) {
                    ArrayList<ResourceStream> list = new ArrayList<>();
                    for (var child : files) {
                        list.add(new ResourceStream(child));
                    }
                    return list;
                }
            } else {
                return Application.getResourcesManager().listFiles(resourceName);
            }
        }
        return new ArrayList<>();
    }

    public String getResourceName() {
        return resourceName;
    }

    public byte[] readData() {
        if (isFolder()) {
            return null;
        } else if (file != null) {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                return null;
            }
        } else {
            return Application.getResourcesManager().getData(resourceName);
        }
    }

    public void clearCache() {
        Application.getResourcesManager().clearResourceCache(resourceName);
    }

    public void putCache(Object cache) {
        if (file != null) {
            Application.getResourcesManager().putResourceCache(resourceName, cache, file.lastModified());
        } else {
            Application.getResourcesManager().putResourceCache(resourceName, cache, 0);
        }
    }

    public Object getCache() {
        if (file != null) {
            return Application.getResourcesManager().getResourceCache(resourceName, file.lastModified());
        } else {
            return Application.getResourcesManager().getResourceCache(resourceName, 0);
        }
    }

    public ResourceStream getRelative(String path) {
        if (file != null) {
            return new ResourceStream(file.toPath().resolve(Paths.get(path)).normalize().toFile());
        }
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
