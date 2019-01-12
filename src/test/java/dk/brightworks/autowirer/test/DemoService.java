package dk.brightworks.autowirer.test;

import dk.brightworks.autowirer.wire.Init;
import dk.brightworks.autowirer.wire.Shutdown;

@TestServiceAnnotation
public class DemoService {
    @Init
    public void init() {

    }

    @Shutdown
    public void shutdown() {

    }
}
