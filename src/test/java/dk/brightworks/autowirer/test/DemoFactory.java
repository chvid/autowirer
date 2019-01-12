package dk.brightworks.autowirer.test;

import dk.brightworks.autowirer.wire.Factory;
import dk.brightworks.autowirer.wire.Init;
import dk.brightworks.autowirer.wire.Service;
import dk.brightworks.autowirer.wire.Shutdown;

@Service(order = 1)
public class DemoFactory {
    public class SimpleBanana {
        String banana = "beautiful";
    }

    public static int state = 0;

    @Init
    public void init() {
        state = 1;
    }

    @Shutdown
    public void shutdown() {
        state = 2;
    }

    @Factory
    public StringBuffer createStringBuffer() {
        return new StringBuffer();
    }

    @Factory
    SimpleBanana simpleBanana = new SimpleBanana();
}
