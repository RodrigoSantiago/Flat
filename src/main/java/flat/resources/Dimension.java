package flat.resources;

import flat.graphics.context.Context;
import flat.window.Application;
import flat.window.Window;

public class Dimension {

    /**
     * small      426 x 320 dp // 532
     * normal     470 x 320 dp // 568
     * large      640 x 480 dp // 800
     * xlarge     960 x 720 dp // 1200
     */
    public enum Size {
        any(-1, -1), small(426, 320), normal(470, 320), large(640, 480), xlarge(960, 720);

        public final int max;
        public final int min;
        public final int val;
        Size(int max, int min) {
            this.max = max;
            this.min = min;
            this.val = max == -1 ? -1 : (int) Math.sqrt(max * max + min * min);
        }
    }

    /**
     * ldpi (low)                 120 dpi
     * mdpi (medium)              160 dpi
     * hdpi (high)                240 dpi
     * xhdpi (extra-high)         320 dpi
     * xxhdpi (x-extra-high)      480 dpi
     * xxxhdpi (x-x-extra-high)   640 dpi
     */
    public static class Density {
        public static Density any = new Density(-1);
        public static Density ldpi = new Density(120);
        public static Density mdpi = new Density(160);
        public static Density hdpi = new Density(240);
        public static Density xhdpi = new Density(320);
        public static Density xxhdpi = new Density(480);
        public static Density xxxhdpi = new Density(640);

        public final int dpi;
        Density(int dpi) {
            this.dpi = dpi;
        }
    }


    /**
     * port     height > width
     * land     width > height
     */
    public enum Orientation {
        any, port, land
    }

    public final float width, height, dpi;
    public final Size size;
    public final Density density;
    public final Orientation orientation;;

    public Dimension(float width, float height, float dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
        this.size = getSize(width, height, dpi);
        this.density = getDensity(dpi);
        this.orientation = getOrientation(width, height);
    }

    public Dimension(Size size, Density density, Orientation orientation) {
        this.width = 0;
        this.height = 0;
        this.dpi = 0;
        this.size = size;
        this.density = density;
        this.orientation = orientation;
    }

    public Dimension(float width, float height, float dpi, Size size, Density density, Orientation orientation) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
        this.size = size;
        this.density = density;
        this.orientation = orientation;
    }

    public int compareSize(Dimension dimension) {
        return dimension.size == Size.any ? -1 :
                Math.abs(dimension.size.val - size.val);
    }

    public int compareOrientation(Dimension dimension) {
        return dimension.orientation == Orientation.any ? -1 :
                dimension.orientation == orientation ? 0 : 1;
    }

    public int compareDensity(Dimension dimension) {
        return dimension.density == Density.any ? -1 :
                Math.abs(dimension.density.dpi - density.dpi);
    }

    public static Size getSize(float width, float height, float dpi) {
        int dp = (int) Math.ceil(Math.sqrt(width * width + height * height) / (dpi / 160));
        return dp < 568 ? Size.small
                : dp < 800 ? Size.normal
                : dp < 1200 ? Size.large
                : Size.xlarge;
    }

    public static Density getDensity(float dpi) {
        return Application.isSystemMobile() && dpi <= 120 ? Density.ldpi     // 120
                : dpi <= 160 ? Density.mdpi   // 160
                : dpi <= 240 ? Density.hdpi   // 240
                : dpi <= 320 ? Density.xhdpi  // 320
                : dpi <= 480 ? Density.xxhdpi // 480
                : Density.xxxhdpi;            // 640
    }


    public static Orientation getOrientation(float width, float height) {
        return height > width ? Orientation.port : Orientation.land;
    }

    @Override
    public int hashCode() {
        return size.hashCode() ^ density.hashCode() ^ orientation.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() == Dimension.class) {
            Dimension other = (Dimension) obj;
            return other.orientation == orientation && other.size == size && other.density == density;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString()+"["+size + ":" + density + ":" + orientation+"]";
    }

    public static float dpPx(float dpi, float dp) {
        return (float) Math.ceil(dp * (getDensity(dpi).dpi / 160f));
    }

    public static float pxDp(float dpi, float px) {
        return (float) Math.ceil(px / (getDensity(dpi).dpi / 160f));
    }
    
    public static float dpPx(float dp) {
        return dpPx(Application.getCurrentWindow().getDpi(), dp);
    }
    
    public static float pxDp(float px) {
        return pxDp(Application.getCurrentWindow().getDpi(), px);
    }
}