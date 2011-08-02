import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class CircuitBreaker implements InvocationHandler, CircuitBreakerManagement {


    @SuppressWarnings("unchecked")
    public static<T> T create(Class<T> proxiedInterface, T realSubject) {
        return (T)Proxy.newProxyInstance(CircuitBreaker.class.getClassLoader(), 
                new Class[] { proxiedInterface, CircuitBreakerManagement.class }, 
                new CircuitBreaker(realSubject));
    }
    
    private final Object realSubject;
    private int errorCount;
    private int threshold = 100;
    private long timeoutMillis = 10000;
    private long resetTime;
    private int recoveryTime = 100;
    private long lastDecayTime;

    private CircuitBreaker(Object realSubject) {
        this.realSubject = realSubject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == CircuitBreakerManagement.class) {
            return method.invoke(this, args);
        }
        before();
        try {
            Object result = method.invoke(realSubject, args);
            afterSuccess();
            return result;
        } catch (InvocationTargetException e) {
            afterFailure();
            throw e.getCause();
        }
    }

    private void afterFailure() {
        errorCount++;
        if(isRecovering()) {
            resetTime = System.currentTimeMillis() + timeoutMillis;
        }
    }

    private void afterSuccess() {
        if (isRecovered()) {
            errorCount = 0;
        }
    }

    private void before() {
        if (isBroken()) {
            throw new CircuitBreakerOpenException();
        }
    }
    
    private long getDecayPeriod() {
        return resetTime/threshold;
    }
    
    @Override
    public void setLastDecayTime(long lastDecayTime) {
        this.lastDecayTime = lastDecayTime;
    }

    private boolean isWorking() {
        return errorCount < threshold;
    }

    private boolean isBroken() {
        return !isWorking() && !isResetTimeExceeded();
    }

    private boolean isRecovering() {
        return !isWorking() && isResetTimeExceeded();
    }

    private boolean isRecovered() {
        boolean withinRecoverPeriod =
            System.currentTimeMillis() < resetTime + recoveryTime;
        return isRecovering() && !withinRecoverPeriod;
    }

    private boolean isResetTimeExceeded() {
        return System.currentTimeMillis() > resetTime;
    }

    @Override
    public int getErrorCount() {
        return errorCount;
    }

    @Override
    public int getThreshold() {
        return threshold;
    }
    
    @Override
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public void setErrorCountForTesting(int errorCount) {
        this.errorCount = errorCount;
    }
    
    @Override
    public long getResetTime() {
        return resetTime;
    }
    
    @Override
    public long getTimeout() {
        return timeoutMillis;
    }
    
    @Override
    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }
    
    @Override
    public void setResetTimeForTesting(long resetTime) {
        this.resetTime = resetTime;
    }
}
