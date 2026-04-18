package com.fleet.infrastructure.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that require AWS services via LocalStack.
 *
 * <p>Spins up a single LocalStack 4.x container with SES, SQS, and SNS enabled.
 * Subclasses inherit {@code @Testcontainers} and {@code @DynamicPropertySource}
 * so they only need to extend this class.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @SpringBootTest
 * @ActiveProfiles("test")
 * class SesEmailAdapterTest extends AbstractAwsIntegrationTest {
 *
 *     @Autowired SesEmailAdapter adapter;
 *
 *     @Test
 *     void shouldSendEmailViaSes() {
 *         // LocalStack SES requires email identity verification before sending
 *         sesClient.verifyEmailIdentity(r -> r.emailAddress("noreply@fleet.io"));
 *         adapter.send("user@example.com", "Speed Alert", "Vehicle exceeded 120 km/h");
 *         // assert via SDK list-sent-messages or SQS notification receipt
 *     }
 * }
 * }</pre>
 */
@Testcontainers
public abstract class AbstractAwsIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.14.0"))
        .withServices(
            Service.SES,
            Service.SNS,
            Service.SQS);

    @DynamicPropertySource
    static void configureAws(DynamicPropertyRegistry registry) {
        // Override AWS SDK endpoint to point at LocalStack
        registry.add("aws.region",            localstack::getRegion);
        // Each service exposes the same base endpoint — the SDK routes by service type
        registry.add("aws.endpoint-override", () -> localstack.getEndpointOverride(Service.SQS).toString());

        // Stub credentials (LocalStack accepts any values)
        registry.add("aws.credentials.access-key", () -> "test");
        registry.add("aws.credentials.secret-key", () -> "test");
    }
}
