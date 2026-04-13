package com.fleet.infrastructure.adapter.out.db;

import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;

import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL implementation of {@link SubscriptionCheckPort}.
 *
 * <p>
 * Query logic: Find the subscription row matching the given email where the
 * rule_id
 * matches the current rule OR is NULL (representing a global/system-wide
 * setting).
 * {@code ORDER BY rule_id NULLS LAST} ensures the rule-specific setting takes
 * precedence
 * over the global fallback.
 * </p>
 *
 * <p>
 * If no row exists, the subscriber is treated as PENDING (double opt-in not yet
 * confirmed),
 * which blocks sending and enforces strict anti-spam compliance.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class PostgresSubscriptionCheckAdapter implements SubscriptionCheckPort {

    private final JdbcClient jdbcClient;

    @Override
    public EmailSubscription getSubscriptionStatus(EmailAddress email, RuleId ruleId) {
        String sql = """
                    SELECT email_address, rule_id, status
                    FROM email_subscriptions
                    WHERE email_address = :email
                      AND (rule_id = :ruleId OR rule_id IS NULL)
                    ORDER BY rule_id NULLS LAST
                    LIMIT 1
                """;

        return jdbcClient.sql(sql)
                .param("email", email.value())
                .param("ruleId", ruleId.value())
                .query((rs, rowNum) -> {
                    EmailSubscription.SubscriptionStatus status = EmailSubscription.SubscriptionStatus
                            .valueOf(rs.getString("status"));

                    Object dbRuleId = rs.getObject("rule_id");
                    RuleId mappedRuleId = (dbRuleId != null) ? new RuleId((UUID) dbRuleId) : null;

                    return new EmailSubscription(
                            new EmailAddress(rs.getString("email_address")),
                            mappedRuleId,
                            status);
                })
                .optional()
                // No record found → treat as PENDING (double opt-in not confirmed)
                .orElseGet(() -> new EmailSubscription(email, ruleId, EmailSubscription.SubscriptionStatus.PENDING));
    }
}