package flat.math;

import flat.math.shapes.PathIterator;

import java.util.Arrays;

public class PathList {
    public static final int MOVE = PathIterator.SEG_MOVETO;
    public static final int LINE = PathIterator.SEG_LINETO;
    public static final int QUAD = PathIterator.SEG_QUADTO;
    public static final int CUBIC = PathIterator.SEG_CUBICTO;
    public static final int CLOSE = PathIterator.SEG_CLOSE;

    private static final byte[] emptyTypes = new byte[0];
    private static final int[] emptyIndices = new int[0];
    private static final float[] emptyData = new float[0];

    private static int[] pointShift = {
            2,  // MOVETO
            2,  // LINETO
            4,  // QUADTO
            6,  // CUBICTO
            0   // CLOSE
    };

    private int typeSize;
    private byte[] types;
    private int indexSize;
    private int[] indices;
    private int dataSize;
    private float[] data;

    private float[] parser = new float[6];

    public PathList() {
        this(0);
    }

    public PathList(int initialCapacity) {
        assert (initialCapacity >= 0) : "Invalid Size";

        if (initialCapacity == 0) {
            types = emptyTypes;
            indices = emptyIndices;
            data = emptyData;
        } else {
            types = new byte[initialCapacity];
            indices = new int[initialCapacity];
            data = new float[initialCapacity * 4];
        }
    }

    public PathList(PathIterator iterator) {
        this(0);
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(parser)) {
                case PathList.MOVE:
                    moveTo(parser[0], parser[1]);
                    break;
                case PathList.LINE:
                    lineTo(parser[0], parser[1]);
                    break;
                case PathList.QUAD:
                    quadTo(parser[0], parser[1], parser[2], parser[3]);
                    break;
                case PathList.CUBIC:
                    cubicTo(parser[0], parser[1], parser[2], parser[3], parser[4], parser[5]);
                    break;
                case PathList.CLOSE:
                    close();
                    break;
            }
            iterator.next();
        }
    }

    private void ensureCapacity(int addSize) {
        ensureIndexCapacity(indexSize + 1);
        ensureTypeCapacity(typeSize + 1);
        ensureDataCapacity(dataSize + addSize);
    }

    private void ensureDataCapacity(int length) {
        assert (length >= 0) : "Invalid size";
        if (data.length < length) {
            int size = Math.max(1, data.length);
            while (size < length) size *= 2;
            data = Arrays.copyOf(data, size);
        }
    }

    private void ensureIndexCapacity(int length) {
        assert (length >= 0) : "Invalid size";
        if (indices.length < length) {
            int size = Math.max(1, indices.length);
            while (size < length) size *= 2;
            indices = Arrays.copyOf(indices, size);
        }
    }

    private void ensureTypeCapacity(int length) {
        assert (length >= 0) : "Invalid size";
        if (types.length < length) {
            int size = Math.max(1, types.length);
            while (size < length) size *= 2;
            types = Arrays.copyOf(types, size);
        }
    }

    public int size() {
        return typeSize;
    }

    public int getType(int pos) {
        return types[pos];
    }

    public int read(int pos, float[] data) {
        return read(pos, data, 0);
    }

    public int read(int pos, float[] data, int offset) {
        final int type = types[pos];
        final int index = indices[pos];
        if (type != CLOSE) {
            System.arraycopy(this.data, index, data, offset, pointShift[type]);
        }
        return type;
    }

    public void write(int pos, float[] data) {
        write(pos, data, 0);
    }

    public void write(int pos, float[] data, int offset) {
        final int type = types[pos];
        final int index = indices[pos];
        if (type != CLOSE) {
            System.arraycopy(data, offset, this.data, index, pointShift[type]);
        }
    }

    public void moveTo(float x, float y) {
        ensureCapacity(2);
        types[typeSize++] = MOVE;
        indices[indexSize++] = dataSize;
        data[dataSize++] = x;
        data[dataSize++] = y;
    }

    public void lineTo(float x, float y) {
        ensureCapacity(2);
        types[typeSize++] = LINE;
        indices[indexSize++] = dataSize;
        data[dataSize++] = x;
        data[dataSize++] = y;
    }

    public void quadTo(float cx1, float cy1, float x, float y) {
        ensureCapacity(4);
        types[typeSize++] = QUAD;
        indices[indexSize++] = dataSize;
        data[dataSize++] = cx1;
        data[dataSize++] = cy1;
        data[dataSize++] = x;
        data[dataSize++] = y;
    }

    public void cubicTo(float cx1, float cy1, float cx2, float cy2, float x, float y) {
        ensureCapacity(6);
        types[typeSize++] = CUBIC;
        indices[indexSize++] = dataSize;
        data[dataSize++] = cx1;
        data[dataSize++] = cy1;
        data[dataSize++] = cx2;
        data[dataSize++] = cy2;
        data[dataSize++] = x;
        data[dataSize++] = y;
    }

    public void close() {
        ensureCapacity(0);
        types[typeSize++] = CLOSE;
        indices[indexSize++] = dataSize;
    }

    public void insertMoveTo(int index, float x, float y) {
        parser[0] = x;
        parser[1] = y;
        insert(index, (byte) MOVE, parser);
    }

    public void insertLineTo(int index, float x, float y) {
        parser[0] = x;
        parser[1] = y;
        insert(index, (byte) LINE, parser);
    }

    public void insertQuadTo(int index, float cx1, float cy1, float x, float y) {
        parser[0] = cx1;
        parser[1] = cy1;
        parser[2] = x;
        parser[3] = y;
        insert(index, (byte) QUAD, parser);
    }

    public void insertCubicTo(int index, float cx1, float cy1, float cx2, float cy2, float x, float y) {
        parser[0] = cx1;
        parser[1] = cy1;
        parser[2] = cx2;
        parser[3] = cy2;
        parser[4] = x;
        parser[5] = y;
        insert(index, (byte) CUBIC, parser);
    }

    public void insertClose(int index) {
        insert(index, (byte) CLOSE, parser);
    }

    public void setAsMoveTo(int index, float x, float y) {
        parser[0] = x;
        parser[1] = y;
        replace(index, (byte) MOVE, parser);
    }

    public void setAsLineTo(int index, float x, float y) {
        parser[0] = x;
        parser[1] = y;
        replace(index, (byte) LINE, parser);
    }

    public void setAsQuadTo(int index, float cx1, float cy1, float x, float y) {
        parser[0] = cx1;
        parser[1] = cy1;
        parser[2] = x;
        parser[3] = y;
        replace(index, (byte) QUAD, parser);
    }

    public void setAsCubicTo(int index, float cx1, float cy1, float cx2, float cy2, float x, float y) {
        parser[0] = cx1;
        parser[1] = cy1;
        parser[2] = cx2;
        parser[3] = cy2;
        parser[4] = x;
        parser[5] = y;
        replace(index, (byte) CUBIC, parser);
    }

    public void setAsClose(int index) {
        replace(index, (byte) CLOSE, parser);
    }

    public void insert(int pos, byte type, float[] data) {
        assert (pos >= 0 && pos < indexSize) : "Invalid position";
        assert (type >= MOVE && type <= CLOSE) : "Invalid type";

        final int shift = pointShift[type];
        ensureCapacity(shift);

        // Type
        System.arraycopy(types, pos, types, pos + 1, typeSize - pos);
        types[pos] = type;
        typeSize++;

        // Indices
        for (int i = indexSize - 1; i > pos; i--) {
            indices[i] = indices[i - 1] + shift;
        }
        // indices[pos] = indices[pos];
        indexSize++;

        final int index = indices[pos];
        System.arraycopy(this.data, index, this.data, index + shift, dataSize - index);
        System.arraycopy(data, 0, this.data, index, shift);
        dataSize += shift;
    }

    public void replace(int pos, byte type, float[] data) {
        assert (pos >= 0 && pos < indexSize) : "Invalid position";
        assert (type >= MOVE && type <= CLOSE) : "Invalid type";

        final int pType = types[pos];
        final int pShift = pointShift[pType];
        final int shift = pointShift[type];
        final int dif = pShift - shift;

        // Types
        types[pos] = type;

        // Indices
        for (int i = pos; i < indexSize - 1; i++) {
            indices[i] -= dif;
        }

        // Data
        final int index = indices[pos];
        System.arraycopy(this.data, index + pShift, this.data, index + shift, dataSize - index - pShift);
        System.arraycopy(data, 0, this.data, index, shift);
        dataSize -= dif;
    }

    public void remove(int pos) {
        assert (pos >= 0 && pos < indexSize) : "Invalid position";

        final int type = types[pos];
        final int shift = pointShift[type];

        // Types
        System.arraycopy(types, pos + 1, types, pos, typeSize - pos - 1);
        typeSize--;

        // Indices
        for (int i = pos; i < indexSize - 1; i++) {
            indices[i] = indices[i + 1] - shift;
        }
        indexSize--;

        // Data
        final int index = indices[pos];
        System.arraycopy(data, index + pointShift[type], data, index, dataSize - index - pointShift[type]);
        dataSize -= pointShift[type];
    }

    public void clear() {
        typeSize = 0;
        indexSize = 0;
        dataSize = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PathList : Size "+size()+"\n");
        float[] data  = new float[6];
        for (int i = 0; i < size(); i++) {
            switch (read(i, data)) {
                case PathList.MOVE :
                    sb.append("Move[").append(data[0]).append(", ").append(data[1]).append("]");
                    break;
                case PathList.LINE :
                    sb.append("Line[").append(data[0]).append(", ").append(data[1]).append("]");
                    break;
                case PathList.QUAD :
                    sb.append("Quad[").append(data[0]).append(", ").append(data[1]).append(", ").append(data[2]).append(", ").append(data[3]).append("]");
                    break;
                case PathList.CUBIC :
                    sb.append("Cubic[").append(data[0]).append(", ").append(data[1]).append(", ").append(data[2]).append(", ").append(data[3]).append(", ").append(data[4]).append(", ").append(data[5]).append("]");
                    break;
                case PathList.CLOSE :
                    sb.append("Close[]");
                    break;
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
