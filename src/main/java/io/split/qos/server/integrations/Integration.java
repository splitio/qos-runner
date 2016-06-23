package io.split.qos.server.integrations;

/**
 * Defines an integration.
 */
public interface Integration extends AutoCloseable {
    /**
     * @return whether the integration is enabled or not.
     */
    boolean isEnabled();
}
