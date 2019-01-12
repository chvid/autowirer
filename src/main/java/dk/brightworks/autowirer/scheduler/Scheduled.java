package dk.brightworks.autowirer.scheduler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {
    String interval();
    String offset() default "";
    int retryCount() default 0;
    String retryInterval() default "PT0S";
}
