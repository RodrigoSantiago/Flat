package flat.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import flat.resources.Dimension.*;

public abstract class DimensionStream {

    private String name;
    private ArrayList<Dimension> temp = new ArrayList<>();

    public DimensionStream(String name) {
        this.name = name;
    }

    public abstract List<Dimension> getDimensions();

    public abstract InputStream getStream(Dimension dimension);

    public final String getName() {
        return name;
    }

    public final Dimension getCloserDimension(float width, float height, float dpi) {
        temp.addAll(getDimensions());

        Orientation orientation = Dimension.getOrientation(width, height);
        Size size = Dimension.getSize(width, height, dpi);

        for (int i = 0; i < temp.size(); i++) {
            Dimension t = temp.get(i);
            if ((t.orientation != Orientation.any && t.orientation != orientation)
                    || (t.size != Size.any && t.size != size)) {
                temp.remove(i--);
            }
        }

        for (int i = 0; i < temp.size(); i++) {
            Dimension t = temp.get(i);
            if (t.orientation != Orientation.any) {
                for (int j = 0; j < temp.size(); j++) {
                    if (temp.get(j).orientation == Orientation.any) temp.remove(j--);
                }
                break;
            }
        }

        for (int i = 0; i < temp.size(); i++) {
            Dimension t = temp.get(i);
            if (t.size != Size.any) {
                for (int j = 0; j < temp.size(); j++) {
                    if (temp.get(j).size == Size.any) temp.remove(j--);
                }
                break;
            }
        }

        Dimension choose = null;
        for (int i = 0; i < temp.size(); i++) {
            Dimension t = temp.get(i);
            if (choose == null) {
                choose = t;
            } else if (Math.abs(t.density.dpi - dpi) < Math.abs(choose.density.dpi - dpi)) {
                choose = t;
            }
        }

        temp.clear();
        return choose == null ? null : new Dimension(width, height, dpi, choose.size, choose.density, choose.orientation);
    }
}
