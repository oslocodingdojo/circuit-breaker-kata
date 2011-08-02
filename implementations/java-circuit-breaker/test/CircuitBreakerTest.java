import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;


public class CircuitBreakerTest {
    
    private Subject realSubject = new RealSubject();
    private Subject proxy = CircuitBreaker.create(Subject.class, realSubject);

    public class RealSubject implements Subject {

        @Override
        public String doAction() {
            return "foo";
        }
        
        @Override
        public void methodWhichThrowsException() throws IOException {
            throw new IOException();
        }
    }

    public interface Subject {

        String doAction();
        
        void methodWhichThrowsException() throws IOException;

    }

    @Test
    public void should_delegate_to_real_subject() {
        assertEquals(realSubject.doAction(), successfulMethodCall());
    }
    
    @Test(expected=IOException.class)
    public void should_throw_exception() throws IOException {
        proxy.methodWhichThrowsException();
    }

    @Test
    public void should_increment_error_counter() {
        int preErrorCount = getCircuitBreakerManagement().getErrorCount();
        unsuccessfulMethodCall();
        int postErrorCount = getCircuitBreakerManagement().getErrorCount();
        assertEquals(postErrorCount, preErrorCount+1);
    }
    
    @Test(expected=CircuitBreakerOpenException.class)
    public void should_throttle_message_calls_when_threshold_is_reached() {
        simulateBrokenCircuit();
        successfulMethodCall();
    }

    @Test
    public void should_set_timeout_when_threshold_is_reached() {
        getCircuitBreakerManagement().setTimeoutMillis(1000);
        getCircuitBreakerManagement().setThreshold(1);
        
        unsuccessfulMethodCall();
        
        assertEquals(System.currentTimeMillis()+1000.0,
                getCircuitBreakerManagement().getResetTime(),
                10.0);
    }
    
    @Test
    public void should_reset_on_successful_call_after_timeout() {
        simulateBrokenCircuit();
        getCircuitBreakerManagement().setResetTimeForTesting(System.currentTimeMillis() - 1 - 100);
        successfulMethodCall();
        assertEquals(0, getCircuitBreakerManagement().getErrorCount());
    }
    
    @Test
    public void should_trip_on_unsuccessful_call_after_timeout() {
        simulateBrokenCircuit();
        getCircuitBreakerManagement().setResetTimeForTesting(System.currentTimeMillis() - 1);
        unsuccessfulMethodCall();
        
        assertTrue(getCircuitBreakerManagement().getResetTime() > System.currentTimeMillis());
    }

    @Test
    public void should_have_same_errorcount_on_success_when_closed() {
        getCircuitBreakerManagement().setErrorCountForTesting(3);
        successfulMethodCall();
        assertEquals(3, getCircuitBreakerManagement().getErrorCount());
    }
    
    @Test
    public void should_restart_timeout_on_error_when_half_open() {
        simulateBrokenCircuit();
        getCircuitBreakerManagement().setTimeoutMillis(10000);
        getCircuitBreakerManagement().setResetTimeForTesting(System.currentTimeMillis() - 1);
        
        successfulMethodCall();
        unsuccessfulMethodCall();
        
        assertEquals(getCircuitBreakerManagement().getTimeout(),
                getCircuitBreakerManagement().getResetTime() - System.currentTimeMillis(),
                10.0);
    }
    
    
    @Test
    @Ignore
    public void should_decay_error_count() {
        int decayPeriod = 1000;
        getCircuitBreakerManagement().setResetTimeForTesting(10*decayPeriod);
        getCircuitBreakerManagement().setThreshold(10);
        getCircuitBreakerManagement().setErrorCountForTesting(9);
        getCircuitBreakerManagement().setLastDecayTime(System.currentTimeMillis()
                - (int)(decayPeriod*4.5));
        
        successfulMethodCall();
        assertEquals(5, getCircuitBreakerManagement().getErrorCount());
//        assertEquals(5, 
//                getCircuitBreakerManagement().getLastDecayTime());
    }

    private String successfulMethodCall() {
        return proxy.doAction();
    }
    
    private void unsuccessfulMethodCall() {
        try {
            proxy.methodWhichThrowsException();
        } catch (IOException expected) {
        }
    }
    
    private CircuitBreakerManagement getCircuitBreakerManagement() {
        return ((CircuitBreakerManagement)proxy);
    }
    
    private void simulateBrokenCircuit() {
        CircuitBreakerManagement management = getCircuitBreakerManagement();
        management.setThreshold(5);
        management.setErrorCountForTesting(5);
        management.setResetTimeForTesting(
                System.currentTimeMillis()+management.getTimeout());
    }
}
