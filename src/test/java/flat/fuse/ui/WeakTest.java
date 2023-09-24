package flat.fuse.ui;

import flat.Weak;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeakTest {
    
    @Test
    void testEquals() {
        Object obj = new Object();
        Weak<Object> weak1 = new Weak<>(obj);
        Weak<Object> weak2 = new Weak<>(obj);
        assertEquals(weak1, weak2, "Invalid Equals");
    }
}