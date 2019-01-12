package dk.brightworks.autowirer.invocation;

import dk.brightworks.autowirer.wire.Autowired;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

public class MethodInvocationTest {
    public interface IA {
        String demo();
    }

    public static class A implements IA {
        public String demo() {
            return "a";
        }
    }

    public class TestClass {
        public String demo(@Autowired Integer number, @Autowired IA a, String o1, String o2) {
            return "hello " + o1 + o2 + " (" + number + ") " + a.demo();
        }
    }

    public static class F1 implements MethodInvocationFilter {
        public void before(MethodInvocation context) {
            context.addWiredObject(Integer.valueOf("42"));
            context.addWiredObject(new A());
        }

        public void after(MethodInvocation context) {

        }
    }

    @Test
    public void sunshine() throws NoSuchMethodException {
        TestClass tc = new TestClass();
        Method method = TestClass.class.getMethod("demo", Integer.class, IA.class, String.class, String.class);
        MethodInvocation invocation = new MethodInvocation(method, tc, "buh", "huh");
        invocation.invokeWithFilters(Arrays.asList(new F1()));
        assertEquals("hello buhhuh (42) a", invocation.getResultObject());
    }
}
