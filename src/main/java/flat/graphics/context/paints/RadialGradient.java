package flat.graphics.context.paints;

import flat.backend.SVG;
import flat.exception.FlatException;
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
    private final CycleMethod cycleMethod;
    private final float[] data;

    RadialGradient(Builder builder) {
        x = builder.x;
        y = builder.y;
        fx = builder.fx;
        fy = builder.fy;
        radiusIn = builder.radiusIn;
        radiusOut = builder.radiusOut;
        cycleMethod = builder.cycleMethod == null ? CycleMethod.CLAMP : builder.cycleMethod;
        stopCount = builder.stopCount;
        data = builder.data;
    }

    @Override
    protected void setInternal(long svgId)  {
        SVG.SetPaintRadialGradient(svgId, x, y, fx, fy, radiusIn, radiusOut, stopCount, data, cycleMethod.ordinal());
    }

    @Override
    public Paint multiply(int color) {
        return this; // todo - implement
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
        return new Affine(data[0], data[2], data[1], data[3], data[4], data[5]);
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
        private static final int stop0 = 6;
        private static final int color0 = 6 + 16;

        private float x;
        private float y;
        private float fx;
        private float fy;
        private float radiusIn;
        private float radiusOut;
        private CycleMethod cycleMethod;
        private float[] data = new float[6 + 16 + 16];
        private int stopCount;
        private boolean readOnly;

        public Builder(float x, float y, float radiusOut) {
            this.x = x;
            this.y = y;
            this.fx = x;
            this.fy = y;
            this.radiusOut = radiusOut;
            data[0] = 1;
            data[1] = 0;
            data[2] = 0;
            data[3] = 1;
            data[4] = 0;
            data[5] = 0;
        }

        private void checkReadOnly() {
            if (readOnly) {
                throw new FlatException("The builder is read only after building");
            }
        }

        public Builder radiusIn(float radiusIn) {
            checkReadOnly();
            this.radiusIn = radiusIn;
            return this;
        }

        public Builder focus(float x, float y) {
            checkReadOnly();
            this.fx = x;
            this.fy = y;
            return this;
        }

        public Builder cycleMethod(CycleMethod cycleMethod) {
            checkReadOnly();
            this.cycleMethod = cycleMethod;
            return this;
        }

        public Builder transform(Affine transform) {
            checkReadOnly();
            data[0] = transform.m00;
            data[1] = transform.m10;
            data[2] = transform.m01;
            data[3] = transform.m11;
            data[4] = transform.m02;
            data[5] = transform.m12;
            return this;
        }

        public Builder stop(float stop, int color) {
            checkReadOnly();
            if (stop < 0 || stop > 1) {
                throw new FlatException("Stop must be between 0 and 1.");
            }

            if (stopCount == 0) {
                data[stop0] = stop;
                data[color0] = Float.intBitsToFloat(color);
                stopCount++;
                return this;
            }

            int insertPos = 0;
            while (insertPos < stopCount && data[stop0 + insertPos] < stop) {
                insertPos++;
            }

            if (stopCount == 16) {
                stopCount--;
            }

            for (int i = stopCount; i > insertPos; i--) {
                data[stop0 + i] = data[stop0 + i - 1];
                data[color0 + i] = data[color0 + i - 1];
            }

            data[stop0 + insertPos] = stop;
            data[color0 + insertPos] = Float.intBitsToFloat(color);

            stopCount++;
            return this;
        }

        public RadialGradient build() {
            checkReadOnly();
            readOnly = true;
            return new RadialGradient(this);
        }
    }
}
