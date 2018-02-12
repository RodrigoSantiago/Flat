package flat.uxml.data;

import flat.FileUtils;

import java.io.*;
import java.util.*;

public class ResourceStream extends DimensionStream {

    private HashMap<Dimension, String> map = new HashMap<>();
    private ArrayList<Dimension> dimensions = new ArrayList<>();

    private static final String sizeRegex = "(small)|(normal)|(large)|(xlarge)";
    private static final String densityRegex = "(ldpi)|(mdpi)|(hdpi)|(xhdpi)|(xxhdpi)|(xxxhdpi)";
    private static final String orientationRegex = "(port)|(land)";
    private static final String regex = "(-(" + orientationRegex + "|" + sizeRegex + "|" + densityRegex + "))*";

    public ResourceStream(String name) {
        super(name);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            String[] files = FileUtils.getResourceListing(loader, name);
            for (String fileName : files) {
                if (fileName.startsWith(name)) {
                    if (fileName.matches(name + regex + "\\.uxml")) {
                        String[] modifiers = fileName.substring(name.length()).split("(-)|(\\.)");
                        Dimension.Size size = Dimension.Size.any;
                        Dimension.Density density = Dimension.Density.any;
                        Dimension.Orientation orientation = Dimension.Orientation.any;
                        for (int i = 1; i < modifiers.length; i++) {
                            String modifier = modifiers[i];
                            if (modifier.matches(sizeRegex)) {
                                size = Dimension.Size.valueOf(modifier);
                            } else if (modifier.matches(densityRegex)) {
                                density = Dimension.Density.valueOf(modifier);
                            } else if (modifier.matches(orientationRegex)) {
                                orientation = Dimension.Orientation.valueOf(modifier);
                            }
                        }
                        Dimension d = new Dimension(size, density, orientation);
                        dimensions.add(d);
                        map.put(d, fileName);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<Dimension> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }

    @Override
    public InputStream getStream(Dimension dimension) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(getName() + "/" + map.get(dimension));
    }

}
