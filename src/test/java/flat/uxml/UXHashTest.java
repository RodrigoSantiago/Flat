package flat.uxml;

import org.junit.Test;

import static org.junit.Assert.*;

public class UXHashTest {

    @Test
    public void getHash() {
        int hashA = UXHash.getHash("property-a");
        int hashB = UXHash.getHash("property-b");
        int hashC = UXHash.getHash("property-c");
        int hashD = UXHash.getHash("property-a");
        assertEquals(0, hashA);
        assertEquals(1, hashB);
        assertEquals(2, hashC);
        assertEquals(0, hashD);
    }
}