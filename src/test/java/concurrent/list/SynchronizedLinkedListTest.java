package concurrent.list;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class SynchronizedLinkedListTest {

    @Test
    public void testInserFisrt() throws Exception {
        SynchronizedLinkedList<Integer> list = new SynchronizedLinkedList<>();
        list.insertFirst(3);
        list.insertFirst(2);
        list.insertFirst(1);

        assertEquals(list.get(0), new Integer(1));
        assertEquals(list.get(1), new Integer(2));
        assertEquals(list.get(2), new Integer(3));
    }

    @Test
    public void testInsertFirstConcurrency() throws Exception {
        final SynchronizedLinkedList<Integer> list = new SynchronizedLinkedList<>();
        Runnable inserter = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    list.insertFirst(i);
                }
            }
        };

        Thread i0 = new Thread(inserter);
        Thread i1 = new Thread(inserter);
        Thread i2 = new Thread(inserter);
        Thread i3 = new Thread(inserter);
        Thread i4 = new Thread(inserter);
        Thread i5 = new Thread(inserter);
        Thread i6 = new Thread(inserter);
        Thread i7 = new Thread(inserter);
        Thread i8 = new Thread(inserter);
        Thread i9 = new Thread(inserter);

        i0.start();
        i1.start();
        i2.start();
        i3.start();
        i4.start();
        i5.start();
        i6.start();
        i7.start();
        i8.start();
        i9.start();

        i0.join(1000);
        i1.join(1000);
        i2.join(1000);
        i3.join(1000);
        i4.join(1000);
        i5.join(1000);
        i6.join(1000);
        i7.join(1000);
        i8.join(1000);
        i9.join(1000);

        assertEquals(10000, list.size());
    }

    @Test
    public void testTime() throws Exception {
        SynchronizedLinkedList<Integer> list0 = new SynchronizedLinkedList<>();
        List<Integer> list1 = Collections.synchronizedList(new LinkedList<Integer>());

        List<Inserter> inserters = Arrays.asList(new BeginInserter(list0, 1000),
             new BeginInserter(list0, 1000),
             new BeginInserter(list0, 1000),
             new BeginInserter(list0, 1000),
             new BeginInserter(list0, 1000),
             new BeginInserter(list0, 1000),
             new EndInserter(list0, 1000),
             new EndInserter(list0, 1000),
             new EndInserter(list0, 1000),
             new EndInserter(list0, 1000),
             new EndInserter(list0, 1000));

        long time0 = System.currentTimeMillis();
        for (Inserter inserter : inserters) {
            inserter.start();
        }

        for (Inserter inserter : inserters) {
            inserter.join();
        }
        time0 = System.currentTimeMillis() - time0;

        List<Inserter> inserters2 = Arrays.asList(new BeginInserter(list1, 1000),
                new BeginInserter(list1, 1000),
                new BeginInserter(list1, 1000),
                new BeginInserter(list1, 1000),
                new BeginInserter(list1, 1000),
                new BeginInserter(list1, 1000),
                new EndInserter(list1, 1000),
                new EndInserter(list1, 1000),
                new EndInserter(list1, 1000),
                new EndInserter(list1, 1000),
                new EndInserter(list1, 1000));

        long time1 = System.currentTimeMillis();
        for (Inserter inserter : inserters2) {
            inserter.start();
        }

        for (Inserter inserter : inserters2) {
            inserter.join();
        }
        time1 = System.currentTimeMillis() - time1;

        System.out.println(time0);
        System.out.println(time1);

        assertTrue(time0 <= time1);
    }

    abstract class Inserter extends Thread {
        protected List<Integer> list;
        protected int size;

        public Inserter(List<Integer> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public void run() {
            for (int i = 0; i < size; i++) {
                insert(i);
            }
        }

        abstract void insert(Integer i);
    }

    class BeginInserter extends Inserter {

        public BeginInserter(List<Integer> list, int size) {
            super(list, size);
        }

        @Override
        void insert(Integer i) {
            list.add(0, i);
        }
    }

    class EndInserter extends Inserter {

        public EndInserter(List<Integer> list, int size) {
            super(list, size);
        }

        @Override
        void insert(Integer i) {
            list.add(i);
        }
    }

    class MiddleInserter extends Inserter {

        public MiddleInserter(List<Integer> list, int size) {
            super(list, size);
        }

        @Override
        void insert(Integer i) {
            list.add(i, list.size() / 2);
        }

    }
}
