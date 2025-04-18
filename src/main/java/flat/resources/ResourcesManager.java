package flat.resources;

import flat.Flat;
import flat.exception.FlatException;
import flat.window.Application;
import flat.window.SystemType;

import java.io.*;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ResourcesManager {

    private static class CacheObj {
        WeakReference<Object> obj;
        long modifyTime;
        public CacheObj(Object obj, long modifyTime) {
            this.obj = new WeakReference<>(obj);
            this.modifyTime = modifyTime;
        }
    }

    private static boolean libraryLoaded;

    private final ZipFile zip;
    private final File dir;
    private final HashMap<String, CacheObj> resources = new HashMap<>();

    public ResourcesManager() {
        this.dir = null;
        this.zip = null;
    }

    public ResourcesManager(File file) {
        if (file == null) {
            this.dir = null;
            this.zip = null;
        } else if (file.isFile()) {
            try {
                this.dir = null;
                this.zip = new ZipFile(file);
            } catch (Exception e) {
                throw new FlatException(e);
            }
        } else {
            this.dir = file;
            this.zip = null;
        }
    }

    public File getFlatLibraryFile() {
        try {
            String libName = "/flat." + switch(Application.getSystemType()) {
                case WINDOWS  -> "dll";
                case MAC -> "dylib";
                default -> "so";
            };

            InputStream in = Flat.class.getResourceAsStream(libName);
            if (in == null) {
                return null;
            }

            File temp = new File(Paths.get("").toAbsolutePath().toFile(), libName);
            if (temp.exists()) {
                if (!temp.delete()) {
                    return null;
                }
            }

            if (!temp.createNewFile()) {
                return null;
            }

            byte[] buffer = new byte[1024];
            int read;
            FileOutputStream fos = new FileOutputStream(temp);
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            in.close();
            temp.deleteOnExit();

            return temp;
        } catch (Exception e) {
            return null;
        }
    }

    public ResourceStream getResource(String value) {
        if (exists(value)) {
            return new ResourceStream(value);
        }
        return null;
    }

    public void unloadResources() {
        resources.clear();
    }

    public void clearResourceCache(String pathName) {
        resources.remove(pathName);
    }

    public void putResourceCache(String pathName, Object cache, long modifyTime) {
        resources.put(pathName, new CacheObj(cache, modifyTime));
    }

    public Object getResourceCache(String pathName, long modifyTime) {
        CacheObj cache = resources.get(pathName);
        if (cache != null) {
            Object obj = cache.obj.get();
            if (obj == null || modifyTime != cache.modifyTime) {
                resources.remove(pathName);
            } else {
                return obj;
            }
        }

        return null;
    }

    public InputStream getInput(String pathName) {
        if (zip != null) {
            try {
                ZipEntry zipEntry = zip.getEntry(pathName);
                return zip.getInputStream(zipEntry);
            } catch (Exception ignored) {
            }
        }

        if (dir != null) {
            try {
                File entry = new File(dir, pathName);
                return new FileInputStream(entry);
            } catch (Exception ignored) {
            }
        }

        try {
            return Flat.class.getResourceAsStream(pathName.startsWith("/") ? pathName : "/" + pathName);
        } catch (Exception ignored) {
        }

        return null;
    }

    public boolean isFolder(String pathName) {
        pathName = pathName.startsWith("/") ? pathName : "/" + pathName;

        if (zip != null) {
            try {
                return (zip.getEntry(pathName.endsWith("/") ? pathName : pathName + "/") != null);
            } catch (Exception ignored) {
            }
        }

        if (dir != null) {
            try {
                File entry = new File(dir, pathName);
                return entry.isDirectory();
            } catch (Exception ignored) {
            }
        }

        try {
            URL dirURL = Flat.class.getResource(pathName);

            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                return new File(dirURL.toURI()).isDirectory();
            }

            if (dirURL != null && dirURL.getProtocol().equals("jar")) {
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));

                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                    pathName = pathName.substring(1);
                    return jar.getEntry(pathName.endsWith("/") ? pathName : pathName + "/") != null;
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    public byte[] getData(String pathName) {
        InputStream is = getInput(pathName);
        if (is == null) {
            return null;
        }

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new FlatException(e);
        }
    }

    public List<ResourceStream> listFiles(String pathName) {
        if (pathName.charAt(pathName.length() - 1) != '/') {
            pathName = pathName + "/";
        }

        List<ResourceStream> list = new ArrayList<>();
        try {
            if (zip != null) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.equals(pathName) && name.startsWith(pathName)) {
                        int indexOf = name.indexOf("/", pathName.length());
                        int indexOf2 = indexOf == -1 ? -1 : name.indexOf("/", indexOf + 1);
                        if (indexOf == -1 || indexOf2 == -1) {
                            list.add(new ResourceStream(name, entry.isDirectory()));
                        }
                    }
                }

            } else if (dir != null) {
                File subDir = new File(dir, pathName);
                String[] files = subDir.list();
                if (files != null) {
                    for (var file : files) {
                        list.add(new ResourceStream(pathName + file, new File(subDir, file).isDirectory()));
                    }
                }

            } else {
                URL dirURL = Flat.class.getResource(pathName.startsWith("/") ? pathName : "/" + pathName);

                if (dirURL != null && dirURL.getProtocol().equals("file")) {
                    File subDir = new File(dirURL.toURI());
                    String[] files = subDir.list();
                    if (files != null) {
                        for (var file : files) {
                            list.add(new ResourceStream(pathName + file, new File(subDir, file).isDirectory()));
                        }
                    }

                } else if (dirURL != null && dirURL.getProtocol().equals("jar")) {

                    String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));

                    try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                        Enumeration<JarEntry> entries = jar.entries();
                        pathName = pathName.startsWith("/") ? pathName.substring(1) : pathName;
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (!name.equals(pathName) && name.startsWith(pathName)) {
                                int indexOf = name.indexOf("/", pathName.length());
                                int indexOf2 = indexOf == -1 ? -1 : name.indexOf("/", indexOf + 1);
                                if (indexOf == -1 || indexOf2 == -1) {
                                    list.add(new ResourceStream(name, entry.isDirectory()));
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new UnsupportedOperationException("Cannot list files for URL " + pathName);
        }
        return list;
    }

    /**
     * Check if the resources files contains certain file
     *
     * @param pathName Resource path
     * @return true-false
     */
    public boolean exists(String pathName) {
        try {
            try (var input = getInput(pathName)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract all files inside the zip to the output directoy
     *
     * @param zipFile Source Zip
     * @param outDir Target Directory
     * @return All extracted files
     *
     */
    public static File[] zipUnpack(File zipFile, File outDir) throws Exception {
        if (!outDir.mkdir() || !outDir.exists() || outDir.isFile() ){
            throw new Exception("Invalid Directory");
        }

        byte[] buffer = new byte[1024];
        ArrayList<File> files = new ArrayList<>();

        ZipInputStream zinstream = null;
        try {
            zinstream = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry zentry = zinstream.getNextEntry();

            while (zentry != null) {
                String entryName = zentry.getName();
                File newFile = new File(outDir + "/" + entryName);
                if (zentry.isDirectory()) {
                    newFile.mkdir();
                } else {
                    try (FileOutputStream outstream = new FileOutputStream(newFile)){
                        int n;
                        while ((n = zinstream.read(buffer)) > -1) {
                            outstream.write(buffer, 0, n);
                        }
                        files.add(newFile);
                    }
                }
                zinstream.closeEntry();
                zentry = zinstream.getNextEntry();
            }
        } finally {
            if(zinstream != null) try {
                zinstream.close();
            }catch (IOException ignored){
            }
        }

        File[] retFile = new File[files.size()];
        retFile = files.toArray(retFile);
        return retFile;
    }

    /**
     * Zip all input files (ignore directories)
     *
     * @param zipFile Target zip file
     * @param srcFiles Source files
     *
     */
    public static void zipPack(File zipFile, File... srcFiles) throws Exception{
        byte[] buffer = new byte[1024];
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            // File loop
            for (File file : srcFiles) {
                if (file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        // Data package
                        zos.putNextEntry(new ZipEntry(file.getName()));
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                        zos.closeEntry();
                    }
                }
            }
        } finally {
            if( zos!= null) try{
                zos.close();
            }catch (IOException ignored){
            }
        }
    }

    /**
     * Zip all files inside dir
     *
     * @param zipFile Target zip file
     * @param dir Source directory
     *
     */
    public static void zipPack(File zipFile, File dir) throws Exception {
        List<File> fileList = new ArrayList<>();
        getAllFiles(dir, fileList);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : fileList) {
                String path = file.getCanonicalPath().substring(dir.getCanonicalPath().length() + 1);
                // Folder
                if (file.isDirectory()) {
                    zos.putNextEntry(new ZipEntry(path + "/"));
                } else {
                    // File content
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(path);
                        zos.putNextEntry(zipEntry);

                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fis.read(bytes)) >= 0) {
                            zos.write(bytes, 0, length);
                        }

                        zos.closeEntry();
                    }
                }
            }
        }
    }

    /**
     * Chech if the file is a valid zip
     *
     * @param file File
     * @return true-false
     */
    public static boolean zipCheck(final File file) {
        try (ZipFile zipfile = new ZipFile(file)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void getAllFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file);
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                }
            }
        }
    }
}
