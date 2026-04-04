package com.fleet.infrastructure.adapter.out.db;

import com.fleet.domain.notification.model.EmailSubscription;
import com.fleet.domain.notification.port.out.SubscriptionCheckPort;
import com.fleet.domain.notification.vo.EmailAddress;
import com.fleet.domain.rule.vo.RuleId;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class PostgresSubscriptionCheckAdapter implements SubscriptionCheckPort {

    private final JdbcClient jdbcClient;

    public PostgresSubscriptionCheckAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public EmailSubscription getSubscriptionStatus(EmailAddress email, RuleId ruleId) {
        /*
         * LOGIC TRUY VẤN:
         * Chúng ta tìm kiếm bản ghi khớp với email và có rule_id trùng với rule hiện
         * tại,
         * HOẶC rule_id là NULL (đại diện cho Global Settings - Cấu hình chung).
         * * Lệnh `ORDER BY rule_id NULLS LAST` đảm bảo:
         * - Nếu có cấu hình riêng cho Rule này (rule_id khác NULL), nó sẽ được xếp lên
         * đầu.
         * - Nếu không có cấu hình riêng, nó sẽ lấy cấu hình Global (rule_id = NULL).
         */
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
                    // 1. Parse Status từ String trong DB sang Enum của Domain
                    String statusStr = rs.getString("status");
                    EmailSubscription.SubscriptionStatus status = EmailSubscription.SubscriptionStatus
                            .valueOf(statusStr);

                    // 2. Xử lý rule_id có thể bị NULL (Global rule)
                    Object dbRuleId = rs.getObject("rule_id");
                    RuleId mappedRuleId = (dbRuleId != null) ? new RuleId((UUID) dbRuleId) : null;

                    // 3. Khởi tạo Domain Model
                    return new EmailSubscription(
                            new EmailAddress(rs.getString("email_address")),
                            mappedRuleId,
                            status);
                })
                .optional() // Trả về Optional<EmailSubscription>
                // 4. Fallback Logic: Quyết định nếu không có dữ liệu
                .orElseGet(() ->
                // Nếu DB không có bản ghi nào, tức là khách chưa từng xác nhận (Double Opt-in).
                // Trả về trạng thái PENDING để hệ thống chặn gửi mail, tuân thủ nghiêm ngặt
                // Anti-spam.
                new EmailSubscription(email, ruleId, EmailSubscription.SubscriptionStatus.PENDING));
    }
}