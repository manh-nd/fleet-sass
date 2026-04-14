package com.fleet.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for the Fleet platform.
 * Maps to the 'fleet' prefix in application files.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "fleet")
public class FleetProperties {

    /**
     * Security related configurations.
     */
    private final Security security = new Security();

    /**
     * Keycloak administrative client configurations.
     */
    private final Keycloak keycloak = new Keycloak();

    /**
     * Push notification configurations.
     */
    private final Push push = new Push();

    @Getter
    @Setter
    public static class Security {
        /**
         * Whether security (Authentication & Authorization) is enabled.
         */
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Keycloak {
        /**
         * URL of the Keycloak server.
         */
        private String serverUrl;

        /**
         * Keycloak realm name.
         */
        private String realm;

        /**
         * Client ID for the administrative client.
         */
        private String adminClientId;

        /**
         * Client secret for the administrative client.
         */
        private String adminClientSecret;
    }

    @Getter
    @Setter
    public static class Push {
        private final Fcm fcm = new Fcm();

        @Getter
        @Setter
        public static class Fcm {
            /**
             * Whether Firebase Cloud Messaging is enabled.
             */
            private boolean enabled = false;
        }
    }
}
