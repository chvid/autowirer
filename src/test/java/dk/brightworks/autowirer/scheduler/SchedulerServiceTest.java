package dk.brightworks.autowirer.scheduler;

import dk.brightworks.autowirer.Autowirer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SchedulerServiceTest {
    private int count;

    @Before
    public void setup() {
        count = 0;
    }

    public class C1 {
        @Scheduled(
                interval = "PT1M",
                retryInterval = "PT5S",
                retryCount = 3
        )
        public void execute() {
            count ++;
            throw new RuntimeException();
        }
    }

    @Ignore
    @Test
    public void twoRetriesEarlyShutdown() throws InterruptedException {
        Autowirer autowirer = new Autowirer();
        autowirer.add(new C1());
        autowirer.init();
        Thread.sleep(6000L);
        autowirer.shutdown();
        assertEquals(2, count);
    }

    public class C2 {
        @Scheduled(
                interval = "PT24H",
                offset = "00:05"
        )
        public void execute() {
            count ++;
        }
    }

    @Ignore
    @Test
    public void offset() throws InterruptedException {
        Autowirer autowirer = new Autowirer();
        autowirer.add(new C2());
        autowirer.init();
        Thread.sleep(1000L);
        autowirer.shutdown();
        assertEquals(0, count);
    }

    public class C3 {
        @Scheduled(
                interval = "PT2S"
        )
        public void exectute() throws InterruptedException {
            count ++;
            Thread.sleep(10000);
        }
    }

    @Ignore
    @Test
    public void doNotInterruptStalledThread() throws InterruptedException {
        Autowirer autowirer = new Autowirer();
        autowirer.add(new C3());
        autowirer.init();
        Thread.sleep(3000L);
        autowirer.shutdown();
        assertEquals(1, count);
    }

    public class C4 {
        @Scheduled(
                interval = "PT2S",
                interruptStalledThread = true
        )
        public void exectute() throws InterruptedException {
            count ++;
            Thread.sleep(10000);
        }
    }

    @Ignore
    @Test
    public void doInterruptStalledThread() throws InterruptedException {
        Autowirer autowirer = new Autowirer();
        autowirer.add(new C4());
        autowirer.init();
        Thread.sleep(3000L);
        autowirer.shutdown();
        assertEquals(2, count);
    }

    public class C5 {
        @Scheduled(
                interval = "PT1S",
                interruptStalledThread = true
        )
        public void exectute() throws InterruptedException {
            count ++;
        }
    }

    @Ignore
    @Test
    public void multipleRequests() throws InterruptedException {
        Autowirer autowirer = new Autowirer();
        autowirer.add(new C5());
        autowirer.init();
        Thread.sleep(4500L);
        autowirer.shutdown();
        assertEquals(5, count);
    }

    @Test
    public void schecduleItem() {
        ScheduledItem si = new ScheduledItem(null, null, 0, 1000, 0, 0, false);
        assertEquals(true, si.isTime(1000));
        assertEquals(false, si.isTime(1001));
        assertEquals(1, si.timeToNext(1001));
        assertEquals(1000, si.timeToNext(1000));
    }
}
