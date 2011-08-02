public interface CircuitBreakerManagement {

    int getErrorCount();
    void setErrorCountForTesting(int errorCount);

    int getThreshold();
    void setThreshold(int threshold);

    long getTimeout();
    void setTimeoutMillis(int timeoutMillis);

    long getResetTime();
    void setResetTimeForTesting(long l);

    void setLastDecayTime(long lastDecayTime);
}
