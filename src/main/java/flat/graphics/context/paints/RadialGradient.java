package flat.graphics.context.paints;

import flat.backend.SVG;
import flat.graphics.context.enums.CycleMethod;
import flat.graphics.context.Paint;
import flat.math.Affine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RadialGradient extends Paint {
    private final float x;
    private final float y;
    private final float radiusIn;
    private final float radiusOut;
    private final float fx;
    private final float fy;
    private final int stopCount;
    private final float[] data;
    private final CycleMethod cycleMethod;
    private final Affine transform;

    RadialGradient(Builder builder) {
        x = builder.x;
        y = builder.y;
        fx = builder.fx;
        fy = builder.fy;
        radiusIn = builder.radiusIn;
        radiusOut = builder.radiusOut;
        cycleMethod = builder.cycleMethod == null ? CycleMethod.CLAMP : builder.cycleMethod;
        transform = builder.transform == null ? new Affine() : builder.transform;
        data = new float[38];
        data[0] = transform.m00;
        data[1] = transform.m10;
        data[2] = transform.m01;
        data[3] = transform.m11;
        data[4] = transform.m02;
        data[5] = transform.m12;
        if (builder.stops != null) {
            builder.stops.sort((o1, o2) -> Float.compare(o1.getStep(), o2.getStep()));
            stopCount = Math.min(16, builder.stops.size());
            for (int i = 0; i < stopCount; i++) {
                data[6 + i] = builder.stops.get(i).getStep();
            }
            for (int i = 0; i < stopCount; i++) {
                data[6 + 16 + i] = Float.intBitsToFloat(builder.stops.get(i).getColor());
            }
        } else {
            stopCount = 0;
        }
    }

    @Override
    protected void setInternal(long svgId)  {
        SVG.SetPaintRadialGradient(svgId, x, y, fx, fy, radiusIn, radiusOut, stopCount, data, cycleMethod.ordinal());
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadiusIn() {
        return radiusIn;
    }

    public float getRadiusOut() {
        return radiusOut;
    }

    public int getStopCount() {
        return stopCount;
    }

    public CycleMethod getCycleMethod() {
        return cycleMethod;
    }

    public Affine getTransform() {
        return new Affine(transform);
    }

    public List<GradientStop> getStops() {
        if (stopCount == 0) {
            return Collections.emptyList();
        } else {
            List<GradientStop> stops = new ArrayList<>(stopCount);
            for (int i = 0; i < stopCount; i++) {
                stops.add(new GradientStop(data[6 + i], Float.floatToRawIntBits(data[6 + 16 + i])));
            }
            return stops;
        }
    }

    public static class Builder {
        private float x;
        private float y;
        private float fx;
        private float fy;
        private float radiusIn;
        private float radiusOut;
        private CycleMethod cycleMethod;
        private Affine transform;
        private ArrayList<GradientStop> stops;

        public Builder(float x, float y, float radiusIn, float radiusOut) {
            this.x = x;
            this.y = y;
            this.fx = x;
            this.fy = y;
            this.radiusIn = radiusIn;
            this.radiusOut = radiusOut;
        }

        public Builder focus(float x, float y) {
            this.fx = x;
            this.fy = y;
            return this;
        }

        public Builder cycleMethod(CycleMethod cycleMethod) {
            this.cycleMethod = cycleMethod;
            return this;
        }

        public Builder transform(Affine transform) {
            this.transform = transform;
            return this;
        }

        public Builder stop(GradientStop stop) {
            if (stops == null) {
                stops = new ArrayList<>();
            }
            stops.add(stop);
            return this;
        }

        public RadialGradient build() {
            return new RadialGradient(this);
        }
    }
}
