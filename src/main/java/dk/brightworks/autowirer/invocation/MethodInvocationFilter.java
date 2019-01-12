package dk.brightworks.autowirer.invocation;

public interface MethodInvocationFilter {
    void before(MethodInvocation context);

    void after(MethodInvocation context);
}
