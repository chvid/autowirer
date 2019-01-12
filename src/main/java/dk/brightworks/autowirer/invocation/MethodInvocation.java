package dk.brightworks.autowirer.invocation;

import dk.brightworks.autowirer.wire.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings(value = "unchecked")
public class MethodInvocation {

    enum State {
        INITIAL,
        OBJECT_RESPONSE,
        EXCEPTION_RESPONSE
    }

    private State state = State.INITIAL;
    private Object resultObject;
    private Throwable resultException;
    private Object targetObject;
    private Method targetMethod;
    private List<Object> requestObjects;
    private List<Object> wiredObjects = new ArrayList<>();

    public MethodInvocation(Method targetMethod, Object targetObject, Object... requestObjects) {
        this.targetMethod = targetMethod;
        this.targetObject = targetObject;
        this.requestObjects = Arrays.asList(requestObjects);
    }

    public List<Object> getRequestObjects() {
        return requestObjects;
    }

    public Object getResultObject() {
        return resultObject;
    }

    public Throwable getResultException() {
        return resultException;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public boolean hasResult() {
        return state == State.OBJECT_RESPONSE || state == State.EXCEPTION_RESPONSE;
    }

    public void respondWithObject(Object object) {
        state = State.OBJECT_RESPONSE;
        resultObject = object;
    }

    public void respondWithException(Throwable throwable) {
        state = State.EXCEPTION_RESPONSE;
        resultException = throwable;
    }

    public void addWiredObject(Object object) {
        wiredObjects.add(object);
    }

    @SuppressWarnings("unchecked")
    public <T> T getWiredObject(Class<T> pt) {
        for (Object wo : wiredObjects) {
            if (pt.isAssignableFrom(wo.getClass())) {
                return (T) wo;
            }
        }
        return null;
    }

    public void invoke() {
        try {
            List<Object> arguments = new ArrayList<>();
            Annotation[][] pas = targetMethod.getParameterAnnotations();
            Class[] pts = targetMethod.getParameterTypes();
            int roIndex = 0;
            for (int i = 0; i < pas.length; i++) {
                if (hasAutowired(pas[i])) {
                    arguments.add(getWiredObject(pts[i]));
                } else {
                    if (roIndex < requestObjects.size()) {
                        arguments.add(requestObjects.get(roIndex));
                        roIndex++;
                    } else {
                        arguments.add(null);
                    }
                }
            }
            respondWithObject(targetMethod.invoke(targetObject, arguments.toArray()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            respondWithException(e.getCause());
        }
    }

    private static boolean hasAutowired(Annotation[] as) {
        for (Annotation a : as) {
            if (a.annotationType().equals(Autowired.class)) {
                return true;
            }
        }
        return false;
    }

    public void invokeWithFilters(List<MethodInvocationFilter> filters) {
        int filterIndex = 0;

        while (filterIndex < filters.size() && !this.hasResult()) {
            try {
                filters.get(filterIndex).before(this);
                filterIndex++;
            } catch (Throwable t) {
                this.respondWithException(t);
            }
        }

        if (!this.hasResult()) {
            this.invoke();
        }

        while (filterIndex > 0) {
            try {
                filterIndex--;
                filters.get(filterIndex).after(this);
            } catch (Throwable t) {
                this.respondWithException(t);
            }
        }
    }
}
