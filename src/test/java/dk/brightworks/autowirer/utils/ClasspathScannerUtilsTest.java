package dk.brightworks.autowirer.utils;

import dk.brightworks.autowirer.wire.Service;
import org.junit.Test;

import java.util.List;

import static dk.brightworks.autowirer.utils.ClasspathScannerUtils.*;
import static org.junit.Assert.assertTrue;

public class ClasspathScannerUtilsTest {
    @Test
    public void scanFileSystemClasspath() {
        List<Class> classes = scanClasspath("dk.brightworks");
        assertTrue(classes.contains(ClasspathScannerUtilsTest.class));
        assertTrue(classes.contains(Service.class));
    }

    @Test
    public void scanJarClasspath() {
        assertTrue(scanClasspath("org.junit").contains(Test.class));
    }
}
