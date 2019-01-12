package dk.brightworks.autowirer;


import dk.brightworks.autowirer.test.DemoFactory;
import dk.brightworks.autowirer.test.DemoService;
import dk.brightworks.autowirer.test.TestServiceAnnotation;
import dk.brightworks.autowirer.wire.Service;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutowirerTest {
    @Test
    public void sunshine() {
        assertEquals(0, DemoFactory.state);
        Autowirer autowirer = new Autowirer();
        autowirer.addPackage("dk.brightworks.autowirer.test");
        autowirer.init();
        assertEquals(1, DemoFactory.state);
        assertEquals(true, autowirer.findServicesWithAnnotation(TestServiceAnnotation.class).get(0) instanceof DemoService);
        assertEquals(true, autowirer.lookupInstance(StringBuffer.class) instanceof StringBuffer);
        assertEquals(true, autowirer.lookupInstance(DemoFactory.SimpleBanana.class) instanceof DemoFactory.SimpleBanana);
        autowirer.shutdown();
        assertEquals(2, DemoFactory.state);
    }

    interface IA {

    }

    @Service
    public class A implements IA {

    }

    @Test
    public void lookupByInterface() {
        Autowirer autowirer = new Autowirer();
        autowirer.add(new A());
        autowirer.init();
        IA ia = autowirer.lookupInstance(IA.class);
        assertTrue(ia.getClass().isAssignableFrom(A.class));
    }

}
