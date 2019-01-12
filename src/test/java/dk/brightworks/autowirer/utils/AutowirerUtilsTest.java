package dk.brightworks.autowirer.utils;

import dk.brightworks.autowirer.invocation.MethodInvocation;
import dk.brightworks.autowirer.scheduler.SchedulerService;
import dk.brightworks.autowirer.test.DemoService;
import org.junit.Test;

import java.util.List;

import static dk.brightworks.autowirer.utils.AutowirerUtils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AutowirerUtilsTest {
    @Test
    public void sunshineScanForServiceClasses() {
        List<Class> classes = scanForServiceClasses("dk.brightworks");
        assertTrue(classes.contains(SchedulerService.class));
        assertTrue(classes.contains(DemoService.class));
        assertFalse(classes.contains(MethodInvocation.class));
    }
}
