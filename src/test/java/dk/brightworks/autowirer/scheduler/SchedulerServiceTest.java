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
}
