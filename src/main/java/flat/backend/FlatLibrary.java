package flat.backend;

import flat.exception.FlatException;

import java.io.File;

public class FlatLibrary {

    private static boolean loaded;

    public static void load(File library) {
        if (loaded) {
            throw new FlatException("Flat library is already loaded.");
        }

        try {
            System.load(library.getAbsolutePath());
        } catch (Exception e) {
            System.loadLibrary("flat");
        }

        loaded = true;
    }
}
