package flat.animations;

import flat.widget.State;

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
        return states[bitset & 0xFF];
    }

    @Override
    public float get(State state) {
        return (bitset & state.bitset()) != 0 ? 1 : 0;
    }

}
