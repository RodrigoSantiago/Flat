package flat.animations;

public final class StateBitset implements StateInfo {
    private static StateBitset[] states = new StateBitset[256];

    static {
        for (int i = 0; i < 256; i++) {
            states[i] = new StateBitset(i);
        }
    }

    final int bitset;

    private StateBitset(int bitset) {
        this.bitset = bitset;
    }

    public static StateBitset getState(byte bitset) {
        return states[bitset];
    }

    @Override
    public float get(int index) {
        return (bitset & (1 << index)) == (1 << index) ? 1 : 0;
    }

}
