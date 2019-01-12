package dk.brightworks.autowirer.scheduler;

import dk.brightworks.autowirer.Autowirer;
import dk.brightworks.autowirer.wire.Autowired;
import dk.brightworks.autowirer.wire.Init;
import dk.brightworks.autowirer.wire.Service;
import dk.brightworks.autowirer.wire.Shutdown;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SchedulerService {
    private static Logger logger = Logger.getLogger(SchedulerService.class.getName());

    @Autowired
    Autowirer autowirer;

    private Thread runner;
    private List<ScheduledItem> items = new ArrayList<>();
    private long currentTime;

    @Init()
    public void init() {
        currentTime = System.currentTimeMillis();
        List<Object> allServices = autowirer.lookupInstances(Object.class);
        for (Object service : allServices) {
            for (Method method : service.getClass().getMethods()) {
                Scheduled annotation = method.getAnnotation(Scheduled.class);
                if (annotation != null) {
                    items.add(new ScheduledItem(
                            service,
                            method,
                            annotation.offset().equals("") ? currentTime : java.time.LocalTime.parse(annotation.offset()).toSecondOfDay() * 1000L,
                            Duration.parse(annotation.interval()).toMillis(),
                            annotation.retryCount(),
                            Duration.parse(annotation.retryInterval()).toMillis()
                    ));
                }
            }
        }
        if (items.size() > 0) {
            runner = new Thread(() -> {
                logger.info("Runner thread started (" + items.size() + " items) ...");
                try {
                    while (true) {
                        for (ScheduledItem item : items) {
                            if (item.isTime(currentTime)) {
                                item.invoke();
                            }
                        }

                        long timeToSleep = Long.MAX_VALUE;

                        for (ScheduledItem item : items) {
                            timeToSleep = Math.min(timeToSleep, item.timeToNext(currentTime));
                        }

                        logger.info("Sleeping " + Duration.ofMillis(timeToSleep) + " until " + new Date(timeToSleep + currentTime) + " ...");

                        Thread.sleep(Math.max(0, timeToSleep - System.currentTimeMillis() + currentTime));
                        currentTime += timeToSleep;
                    }
                } catch (InterruptedException t) {
                    // ignored
                }
                logger.info("Runner thread stopped.");
            });
            runner.start();
        }
    }

    @Shutdown
    public void shutdown() {
        if (runner != null) {
            runner.interrupt();
            for (ScheduledItem item : items) {
                item.interrupt();
            }
            logger.info("Runner threads stopped.");
        }
    }
}
