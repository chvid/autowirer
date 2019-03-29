package dk.brightworks.autowirer.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduledItem {
    private static Logger logger = Logger.getLogger(ScheduledItem.class.getName());

    private Object targetObject;
    private Method targetMethod;
    private long offset;
    private long interval;
    private int retryCount;
    private long retryInterval;
    private boolean interruptStalledThread;
    private Thread thread;
    private boolean interrupted = false;

    public ScheduledItem(Object targetObject, Method targetMethod, long offset, long interval, int retryCount, long retryInterval, boolean interruptStalledThread) {
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.offset = offset;
        this.interval = interval;
        this.retryCount = retryCount;
        this.retryInterval = retryInterval;
        this.interruptStalledThread = interruptStalledThread;
    }

    public boolean isTime(long currentTime) {
        return (currentTime - offset) % interval == 0;
    }

    public long timeToNext(long currentTime) {
        return isTime(currentTime) ? interval : (currentTime - offset) % interval;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public long getOffset() {
        return offset;
    }

    public long getInterval() {
        return interval;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public String toString() {
        return targetObject.getClass().getName() + "::" + targetMethod.getName();
    }

    public void interrupt() {
        if (thread != null) {
            interrupted = true;
            thread.interrupt();
        }
    }

    public void invoke() {
        if (interrupted) {
            return;
        }
        if (thread == null || interruptStalledThread) {
            if (thread != null) {
                logger.info("Interrupting stalled invoker thread.");
                try {
                    thread.interrupt();
                    thread = null;
                } catch (Throwable t) {
                    logger.info("Ignoring exception " + t + " interrupting thread.");
                }
            }

            thread = new Thread(() -> {
                try {
                    int count = 0;
                    do {
                        try {
                            logger.info("Invoking " + this + " (attempt #" + (count + 1) + " out of maxium " + retryCount + ").");
                            targetMethod.invoke(targetObject);
                            return;
                        } catch (Throwable e) {
                            if (e instanceof InvocationTargetException) {
                                logger.log(Level.INFO, "Exception invoking " + this, e.getCause());
                            } else {
                                logger.log(Level.INFO, "Exception invoking " + this, e);
                            }
                        }
                        if (interrupted) return;
                        count++;
                        if (count < retryCount) {
                            try {
                                logger.info("Sleeping " + Duration.ofMillis(retryInterval) + " until next retry.");
                                Thread.sleep(retryInterval);
                            } catch (InterruptedException e) {
                                if (interrupted) return;
                            }
                        } else {
                            logger.info("No more retries.");
                            return;
                        }
                    } while (true);
                } finally {
                    thread = null;
                }
            });
            thread.start();
        } else {
            logger.info("Skipping invocation of " + this + " because of already running invoker thread.");
        }
    }
}
