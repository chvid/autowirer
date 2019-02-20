package dk.brightworks.autowirer;

import dk.brightworks.autowirer.invocation.MethodInvocation;
import dk.brightworks.autowirer.invocation.MethodInvocationFilter;
import dk.brightworks.autowirer.scheduler.SchedulerService;
import dk.brightworks.autowirer.utils.AutowirerUtils;
import dk.brightworks.autowirer.wire.Factory;
import dk.brightworks.autowirer.wire.Init;
import dk.brightworks.autowirer.wire.Shutdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dk.brightworks.autowirer.utils.AutowirerUtils.*;

/**
 * Steps in autowirer - init:
 * <p>
 * 1. Scan packages for classes annotated with Service annotation (or an annotation annotated with Service).
 * 2. Instantiate service classes.
 * 3. Scan service for methods with Factory annotation.
 * 4. Instantiate factory objects.
 * 5. Wire all services.
 * 6. Call init at all services.
 * 7. Optional: Wire and init additional objects.
 * 8. Optional: Find services with given annotation.
 * <p>
 * Shutdown:
 * <p>
 * 1. Call shutdown at all services.
 */
@SuppressWarnings("unchecked")
public class Autowirer {
    private List<Object> services = new ArrayList<>();

    public Autowirer() {
        services.add(this);
        services.add(new SchedulerService());
    }

    public <T> T lookupInstance(Class<T> klass) {
        return (T)lookupInstanceByClass(services, klass);
    }

    public <T> List<T> lookupInstances(Class<T> klass) {
        return (List<T>)sortServicesByOrder((List<Object>)lookupInstancesByClass(services, klass));
    }

    public List<Object> findServicesWithAnnotation(Class annotation) {
        return sortServicesByOrder(lookupInstancesByAnnotation(services, annotation));
    }

    public void add(Object ... objects) {
        services.addAll(Arrays.asList(objects));
    }

    public void addPackage(String packageToScan) {
        try {
            services.addAll(instantiateClasses(scanForServiceClasses(packageToScan)));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void init() {
        try {
            List<Object> servicesFromFactory = new ArrayList<>();
            servicesFromFactory.addAll(readFactoryFields(services));
            servicesFromFactory.addAll(invokeMethodsWithAnnontation(Factory.class, services));
            services.addAll(servicesFromFactory);
            AutowirerUtils.autowireFields(services);
            invokeMethodsWithAnnontation(Init.class, sortServicesByOrder(services));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void shutdown() {
        try {
            invokeMethodsWithAnnontation(Shutdown.class, reverse(sortServicesByOrder(services)));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void invoke(MethodInvocation invocation) {
        invocation.invokeWithFilters(lookupInstances(MethodInvocationFilter.class));
    }

    public void autowireFields(Object object) {
        try {
            AutowirerUtils.autowireFields(object, services);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
