package flat.resources;

import flat.graphics.image.Drawable;
import flat.graphics.image.PixelMap;
import flat.graphics.image.DrawableReader;
import flat.graphics.image.LineMap;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ResourcesManager {
    private static ZipFile zip;
    private static File dir;

    private static HashMap<String, SoftReference<Resource>> resources = new HashMap<>();

    private ResourcesManager() {
    }

    public static void setResources(File file) {
        if (dir != null || zip != null) {
            throw new RuntimeException("Resources redefinition not allowed");
        }

        if (file == null) {

        } else if (file.isFile()) {
            try {
                zip = new ZipFile(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            dir = file;
        }
    }

    public synchronized static void unloadResources() {
        resources.clear();
    }

    public synchronized static Resource getResource(String pathName) {
        SoftReference<Resource> resRef = resources.get(pathName);
        if (resRef != null) {
            Resource res = resRef.get();
            if (res != null) {
                return res;
            }
        }

        Resource res;
        if (pathName.endsWith(".png")) {
            res = new Resource() {
                Drawable drawable;

                @Override
                public Drawable getDrawable() {
                    if (drawable == null) {
                        try {
                            drawable = DrawableReader.loadPixelMap(getInput(pathName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return drawable;
                }
            };
        } else if (pathName.endsWith(".svg")) {
            res = new Resource() {
                Drawable drawable;

                @Override
                public Drawable getDrawable() {
                    if (drawable == null) {
                        try {
                            drawable = DrawableReader.loadLineMap(getInput(pathName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return drawable;
                }
            };
        } else {
            return null;
        }
        resources.put(pathName, new SoftReference<>(res));
        return res;
    }

    public synchronized static InputStream getInput(String pathName) {
        try {
            if (zip != null) {
                ZipEntry zipEntry = zip.getEntry(pathName);
                return zip.getInputStream(zipEntry);
            } else if (dir != null) {
                File entry = new File(dir, pathName);
                return new FileInputStream(entry);
            } else {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathName);
            }
        } catch (Exception e) {
            System.out.println("Erro at: Input File");
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static byte[] getData(String pathName) {
        InputStream is = getInput(pathName);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                buffer.close();
            } catch (IOException ignored) {
            }
        }
    }

    public synchronized static String[] listFiles(String pathName) {
        if (pathName.charAt(pathName.length() - 1) != '/') {
            pathName = pathName + "/";
        }
        try {
            if (zip != null) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                Set<String> result = new HashSet<>();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(pathName)) {
                        String entry = name.substring(pathName.length());
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir >= 0) {
                            entry = entry.substring(0, checkSubdir);
                        }
                        result.add(entry);
                    }
                }
                return result.toArray(new String[result.size()]);
            } else if (dir != null) {
                File subDir = new File(dir, pathName);
                String[] sub = subDir.list();
                if (sub == null) {
                    return new String[0];
                } else {
                    return sub;
                }
            } else {
                URL dirURL = Thread.currentThread().getContextClassLoader().getResource(pathName);
                if (dirURL != null && dirURL.getProtocol().equals("file")) {
                    String[] sub = new File(dirURL.toURI()).list();
                    if (sub == null) {
                        return new String[0];
                    } else {
                        return sub;
                    }
                }

                if (dirURL != null && dirURL.getProtocol().equals("jar")) {
                    String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
                    JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                    Enumeration<JarEntry> entries = jar.entries();
                    Set<String> result = new HashSet<>();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith(pathName)) {
                            String entry = name.substring(pathName.length());
                            int checkSubdir = entry.indexOf("/");
                            if (checkSubdir >= 0) {
                                entry = entry.substring(0, checkSubdir);
                            }
                            result.add(entry);
                        }
                    }
                    return result.toArray(new String[result.size()]);
                }

                throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro at: Getting Input Files");
            return null;
        }
    }

    /**
     * Check if the resources files contains certain file
     *
     * @param pathName Resource path
     * @return true-false
     */
    public synchronized static boolean exists(String pathName) {
        try {
            if (zip != null) {
                return zip.getEntry(pathName) != null;
            } else if (dir != null) {
                return new File(dir, pathName).exists();
            } else {
                return Thread.currentThread().getContextClassLoader().getResource(pathName) != null;
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
     * @throws Exception
     */
    public static File[] zipUnpack(File zipFile, File outDir) throws Exception {
        outDir.mkdir();
        if (!outDir.exists() || outDir.isFile() ){
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
                    try(FileOutputStream outstream = new FileOutputStream(newFile)){
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
     * @throws Exception
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
     * @throws Exception
     */
    public static void zipPack(File zipFile, File dir) throws Exception {
        List<File> fileList = new ArrayList<>();
        getAllFiles(dir, fileList);

        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for (File file : fileList) {
                String path = file.getCanonicalPath().substring(
                        dir.getCanonicalPath().length() + 1, file.getCanonicalPath().length() );
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
        } finally {
            if( zos != null) try{
                zos.close();
            }catch (IOException ignored){
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
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(file);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized static String readPersistentData(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return new String(buffer.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                buffer.close();
            } catch (IOException ignored) {
            }
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
