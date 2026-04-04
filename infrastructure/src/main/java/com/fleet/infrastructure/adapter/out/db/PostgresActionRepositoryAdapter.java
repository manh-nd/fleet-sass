package com.fleet.infrastructure.adapter.out.db;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;

import com.fleet.domain.notification.model.NotificationAction;
import com.fleet.domain.notification.port.out.NotificationActionRepositoryPort;
import com.fleet.domain.rule.vo.RuleId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostgresActionRepositoryAdapter implements NotificationActionRepositoryPort {

    private final JdbcClient jdbcClient;

    @Override
    public List<NotificationAction> getActionsForRule(RuleId ruleId) {
        String sql = """
                    SELECT rule_id, channel_type, recipient, message_template
                    FROM notification_actions
                    WHERE rule_id = :ruleId
                """;

        return jdbcClient.sql(sql)
                .param("ruleId", ruleId.value())
                .query((rs, rowNum) -> new NotificationAction(
                        ruleId,
                        rs.getString("channel_type"),
                        rs.getString("recipient"),
                        rs.getString("message_template")))
                .list();
    }

}
