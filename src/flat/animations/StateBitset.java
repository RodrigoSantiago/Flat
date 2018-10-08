package flat.animations;

public class StateBitset implements StateInfo {
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

    @Override
    public float get(int index) {
        return (bitset & (1 << index)) << index;
    }

    @Override
    public boolean isSimple() {
        return true;
    }
}
