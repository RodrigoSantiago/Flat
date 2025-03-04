package flat.graphics.context.paints;

import flat.backend.SVG;
import flat.graphics.context.enums.CycleMethod;
import flat.graphics.context.Paint;
import flat.math.Affine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinearGradient extends Paint {
    private final float x1;
    private final float y1;
    private final float x2;
    private final float y2;
    private final int stopCount;
    private final float[] data;
    private final CycleMethod cycleMethod;
    private final Affine transform;

    LinearGradient(Builder builder) {
        x1 = builder.x1;
        y1 = builder.y1;
        x2 = builder.x2;
        y2 = builder.y2;
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
        SVG.SetPaintLinearGradient(svgId, x1, y1, x2, y2, stopCount, data, cycleMethod.ordinal());
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
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
        private float x1;
        private float y1;
        private float x2;
        private float y2;
        private CycleMethod cycleMethod;
        private Affine transform;
        private ArrayList<GradientStop> stops;

        public Builder(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
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

        public LinearGradient build() {
            return new LinearGradient(this);
        }
    }
}
