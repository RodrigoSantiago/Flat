package flat.widget.text.styled;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TreeListTest {
    @Test
    public void testInsertAndGet() {
        TreeList<Integer> list = new TreeList<>();
        list.add(0, 10);
        list.add(1, 20);
        list.add(1, 15);
        
        
        assertEquals(3, list.size());
        assertEquals(10, (int) list.get(0));
        assertEquals(15, (int) list.get(1));
        assertEquals(20, (int) list.get(2));
    }
    
    
    @Test
    public void testInsertRange() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4));
        list.addAll(2, Arrays.asList(10,11));
        
        
        assertEquals(Arrays.asList(1,2,10,11,3,4), toList(list));
    }
    
    
    @Test
    public void testRemove() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(5,6,7));
        
        
        int r = list.remove(1);
        assertEquals(6, r);
        assertEquals(Arrays.asList(5, 7), toList(list));
    }
    
    
    @Test
    public void testRemoveRange() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4,5,6));
        
        
        List<Integer> removed = list.removeFromRange(2,5);
        assertEquals(Arrays.asList(3,4,5), removed);
        assertEquals(Arrays.asList(1,2,6), toList(list));
    }
    
    
    @Test
    public void testIterator() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3));
        
        
        List<Integer> out = new ArrayList<>();
        for (int x : list) out.add(x);
        
        
        assertEquals(Arrays.asList(1,2,3), out);
    }
    
    
    @Test
    public void testLargeBulkInsertAndRemove() {
        TreeList<Integer> list = new TreeList<>();
        List<Integer> bulk = new ArrayList<>();
        for (int i = 0; i < 1000; i++) bulk.add(i);
        
        
        list.addAll(0, bulk);
        assertEquals(1000, list.size());
        
        
        List<Integer> removed = list.removeFromRange(200, 800);
        assertEquals(600, removed.size());
        assertEquals(400, list.size());
    }
    
    @Test
    public void testRemoveRangeOddCount() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4,5));
        
        List<Integer> removed = list.removeFromRange(1,4); // remove 2,3,4
        assertEquals(Arrays.asList(2,3,4), removed);
        assertEquals(Arrays.asList(1,5), toList(list));
    }
    
    @Test
    public void testRemoveRangeAtBeginning() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(10,20,30,40,50));
        
        List<Integer> removed = list.removeFromRange(0,2); // remove 10,20
        assertEquals(Arrays.asList(10,20), removed);
        assertEquals(Arrays.asList(30,40,50), toList(list));
    }
    
    @Test
    public void testRemoveRangeAtEnd() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4,5));
        
        List<Integer> removed = list.removeFromRange(3,5); // remove 4,5
        assertEquals(Arrays.asList(4,5), removed);
        assertEquals(Arrays.asList(1,2,3), toList(list));
    }
    
    @Test
    public void testRemoveRangeSingleElement() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4));
        
        List<Integer> removed = list.removeFromRange(2,3); // remove only 3
        assertEquals(Arrays.asList(3), removed);
        assertEquals(Arrays.asList(1,2,4), toList(list));
    }
    
    @Test
    public void testRemoveRangeZeroElements() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3));
        
        List<Integer> removed = list.removeFromRange(1,1); // range vazio
        assertTrue(removed.isEmpty());
        assertEquals(Arrays.asList(1,2,3), toList(list));
    }
    
    @Test
    public void testRemoveEntireList() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(5,6,7));
        
        List<Integer> removed = list.removeFromRange(0,3);
        assertEquals(Arrays.asList(5,6,7), removed);
        assertEquals(0, list.size());
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveRangeOutOfBoundsLow() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3));
        
        list.removeFromRange(-1, 2);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveRangeOutOfBoundsHigh() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3));
        
        list.removeFromRange(2, 10);
    }
    
    @Test
    public void testRemoveRangeStartEqualsEndAtListSizeEdge() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3));
        
        List<Integer> removed = list.removeFromRange(3,3); // válido, vazio
        assertTrue(removed.isEmpty());
        assertEquals(Arrays.asList(1,2,3), toList(list));
    }
    
    @Test
    public void testRemoveMiddleChunkLeavingTwoSides() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4,5,6,7));
        
        List<Integer> removed = list.removeFromRange(2,5); // 3,4,5
        assertEquals(Arrays.asList(3,4,5), removed);
        assertEquals(Arrays.asList(1,2,6,7), toList(list));
    }
    
    @Test
    public void testRemoveRangeLargeOddRange() {
        TreeList<Integer> list = new TreeList<>();
        List<Integer> base = new ArrayList<>();
        for (int i = 0; i < 101; i++) base.add(i);
        list.addAll(0, base);
        
        // remove intervalo ímpar de 33 itens
        List<Integer> removed = list.removeFromRange(50, 83);
        
        assertEquals(33, removed.size());
        assertEquals(68, list.size());
    }
    
    @Test
    public void testAddAfterRemoveAtBeginning() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3));
        
        list.removeFromRange(0,1); // remove 1
        
        list.add(0, 10); // adiciona no início
        assertEquals(Arrays.asList(10,2,3), toList(list));
    }
    
    @Test
    public void testAddAfterRemoveInMiddle() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4,5));
        
        list.removeFromRange(2,4); // remove 3,4
        
        list.add(2, 99); // adiciona exatamente no buraco do meio
        
        assertEquals(Arrays.asList(1,2,99,5), toList(list));
    }
    
    @Test
    public void testAddAfterRemoveAtEnd() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(10,20,30));
        
        list.removeFromRange(2,3); // remove último (30)
        
        list.add(2, 40); // adiciona novo último
        
        assertEquals(Arrays.asList(10,20,40), toList(list));
    }
    
    @Test
    public void testAddAllAfterRemoveMiddleChunk() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4,5,6));
        
        list.removeFromRange(2,5); // remove 3,4,5
        
        list.addAll(2, Arrays.asList(7,8,9)); // insere novo bloco no meio
        
        assertEquals(Arrays.asList(1,2,7,8,9,6), toList(list));
    }
    
    @Test
    public void testAddAfterRemoveEntireList() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(5,6,7,8));
        
        list.removeFromRange(0,4); // remove tudo
        
        list.add(0, 42);           // adiciona na lista vazia
        assertEquals(Arrays.asList(42), toList(list));
    }
    
    @Test
    public void testAddAllAfterRemoveEntireList() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3));
        
        list.removeFromRange(0,3); // esvazia
        
        list.addAll(0, Arrays.asList(10,20)); // reinicia com outro bloco
        
        assertEquals(Arrays.asList(10,20), toList(list));
    }
    
    @Test
    public void testAddAfterRemoveLargeRange() {
        TreeList<Integer> list = new TreeList<>();
        List<Integer> base = new ArrayList<>();
        for (int i = 0; i < 100; i++) base.add(i);
        list.addAll(0, base);
        
        list.removeFromRange(20, 80); // remove 60 elementos
        
        list.add(10, 9999); // adiciona bem dentro do que sobrou
        
        List<Integer> arr = toList(list);
        assertEquals(41, arr.size());    // 100 - 60 + 1
        
        assertEquals((Integer) 9999, arr.get(10));
    }
    
    @Test
    public void testRemoveThenAddThenRemoveAgain() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4,5,6));
        
        list.removeFromRange(1,5); // remove 2,3,4,5 → sobra (1,6)
        
        list.addAll(1, Arrays.asList(10,11,12)); // fica (1,10,11,12,6)
        
        List<Integer> removedAgain = list.removeFromRange(2,4); // remove 11,12
        
        assertEquals(Arrays.asList(11,12), removedAgain);
        assertEquals(Arrays.asList(1,10,6), toList(list));
    }
    
    @Test
    public void testAddMultipleTimesAfterMultipleRemovals() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(0, Arrays.asList(1,2,3,4));
        
        list.removeFromRange(1,3); // remove 2,3 → (1,4)
        
        list.add(1, 99);           // (1,99,4)
        list.add(3, 100);          // (1,99,4,100)
        
        list.removeFromRange(0,1); // remove 1 → (99,4,100)
        
        list.add(0, 500);          // (500,99,4,100)
        
        assertEquals(Arrays.asList(500,99,4,100), toList(list));
    }
    
    
    private <T> List<T> toList(TreeList<T> l) {
        List<T> x = new ArrayList<>();
        for (T v : l) x.add(v);
        return x;
    }
}