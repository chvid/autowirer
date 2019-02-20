package dk.brightworks.autowirer.utils;

import dk.brightworks.autowirer.wire.Autowired;
import dk.brightworks.autowirer.wire.Factory;
import dk.brightworks.autowirer.wire.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static dk.brightworks.autowirer.utils.ClasspathScannerUtils.scanClasspath;

@SuppressWarnings("unchecked")
public class AutowirerUtils {
    public static int findServiceOrder(Object o) {
        if (o.getClass().isAnnotationPresent(Service.class)) {
            return o.getClass().getAnnotation(Service.class).order();
        }
        for (Annotation a : o.getClass().getAnnotations()) {
            if (a.getClass().isAnnotationPresent(Service.class)) {
                return a.getClass().getAnnotation(Service.class).order();
            }
        }
        return 0;
    }

    public static <T> List<T> reverse(List<T> list) {
        ArrayList<T> result = new ArrayList<>(list);
        Collections.reverse(result);
        return result;
    }

    public static List<Object> sortServicesByOrder(List<Object> services) {
        ArrayList<Object> result = new ArrayList<>(services);
        result.sort(Comparator.comparingInt(AutowirerUtils::findServiceOrder));
        return result;
    }

    public static <T> T lookupInstanceByClass(List<Object> services, Class<T> klass) {
        for (Object o : services) {
            if (klass.isAssignableFrom(o.getClass())) return (T) o;
        }
        throw new RuntimeException("Unable to find service of qualification " + klass.getName());
    }

    public static <T> List<T> lookupInstancesByClass(List<Object> services, Class<T> klass) {
        List<T> result = new ArrayList<>();
        for (Object o : services) {
            if (klass.isAssignableFrom(o.getClass())) {
                result.add((T) o);
            }
        }
        return result;
    }

    public static List<Object> lookupInstancesByAnnotation(List<Object> services, Class<? extends Annotation> annotation) {
        List<Object> result = new ArrayList<>();

        for (Object s : services) {
            if (s.getClass().isAnnotationPresent(annotation)) result.add(s);
        }

        return result;
    }

    private static boolean isService(Class c) {
        if (c.isAnnotation()) return false;
        if (c.isInterface()) return false;
        boolean service = false;
        for (Annotation a : c.getAnnotations()) {
            service |= a.annotationType().equals(Service.class);
            service |= a.annotationType().isAnnotationPresent(Service.class);
        }
        return service;
    }

    public static List<Class> scanForServiceClasses(String packageToScan) {
        return scanClasspath(packageToScan).stream().filter(c -> isService(c)).collect(Collectors.toList());
    }

    public static List<Object> instantiateClasses(List<Class> classes) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        List<Object> result = new ArrayList<>();
        for (Class c : classes) {
            Constructor dc = c.getDeclaredConstructor();
            dc.setAccessible(true);
            result.add(dc.newInstance());
        }
        return result;
    }

    public static List<Object> readFactoryFields(List<Object> services) throws InvocationTargetException, IllegalAccessException {
        List<Object> result = new ArrayList<>();
        for (Object o : services) {
            for (Field f : o.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(Factory.class)) {
                    f.setAccessible(true);
                    Object value = f.get(o);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    public static List<Object> invokeMethodsWithAnnontation(Class<? extends Annotation> klass, List<Object> services) throws InvocationTargetException, IllegalAccessException {
        List<Object> result = new ArrayList<>();
        for (Object o : services) {
            for (Method m : o.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(klass)) {
                    m.setAccessible(true);
                    if (!m.getReturnType().equals(Void.TYPE)) {
                        result.add(m.invoke(o));
                    } else {
                        m.invoke(o);
                    }
                }
            }
        }
        return result;
    }

    public static void autowireFields(Object object, List<Object> services) throws IllegalAccessException {
        for (Field f : object.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Autowired.class)) {
                f.setAccessible(true);
                f.set(object, lookupInstanceByClass(services, f.getType()));
            }
        }

    }

    public static void autowireFields(List<Object> services) throws IllegalAccessException {
        for (Object o : services) {
            autowireFields(o, services);
        }
    }
}
