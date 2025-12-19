package org.meldtech.platform.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties {
    /** Enable the rate limiter globally. */
    private boolean enabled = false;
    /** Max tokens (bucket capacity). Typical per-minute capacity, with a steady leak per second. */
    private int capacity = 120;
    /** Leak rate in tokens per second (how fast the bucket drains). */
    private double leakPerSecond = 2.0; // ~120/min
    /** Keying strategy to identify a client. */
    private KeyStrategy keyStrategy = KeyStrategy.IP;
    /** Headers to consider as device identifier (first non-empty used). */
    private List<String> deviceHeaders = List.of("X-Device-Id", "X-Device-KeyId");
    /** Optional Redis key prefix. */
    private String keyPrefix = "rate-limit";
    /** Optional per-path differentiation (include request a path in the key). */
    private boolean perPath = true;

    public enum KeyStrategy { IP, DEVICE, IP_OR_DEVICE }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getLeakPerSecond() { return leakPerSecond; }
    public void setLeakPerSecond(double leakPerSecond) { this.leakPerSecond = leakPerSecond; }
    public KeyStrategy getKeyStrategy() { return keyStrategy; }
    public void setKeyStrategy(KeyStrategy keyStrategy) { this.keyStrategy = keyStrategy; }
    public List<String> getDeviceHeaders() { return deviceHeaders; }
    public void setDeviceHeaders(List<String> deviceHeaders) { this.deviceHeaders = deviceHeaders; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public boolean isPerPath() { return perPath; }
    public void setPerPath(boolean perPath) { this.perPath = perPath; }
}
