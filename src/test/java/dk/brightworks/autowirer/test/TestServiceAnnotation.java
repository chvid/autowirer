package dk.brightworks.autowirer.test;

import dk.brightworks.autowirer.wire.Service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Service
@Retention(RetentionPolicy.RUNTIME)
public @interface TestServiceAnnotation {
}
