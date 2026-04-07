package com.fleet.domain.rule.port.out;

import com.fleet.domain.entitlement.vo.TenantId;
import com.fleet.domain.rule.model.NotificationRule;
import com.fleet.domain.rule.vo.RuleId;
import com.fleet.domain.shared.pagination.CursorPage;

import java.util.List;

/**
 * Outbound port for managing Notification Rule persistence.
 */
public interface RuleRepositoryPort {

    /** Returns only active rules matching a tenant + event type. Used at rule evaluation time. */
    List<NotificationRule> findActiveRules(TenantId tenantId, String eventType);

    /**
     * Returns ALL rules (active and inactive) for a tenant using cursor-based pagination.
     *
     * <p>The {@code cursor} is an opaque string from the previous page's {@code nextCursor}.
     * Pass {@code null} for the first page.</p>
     *
     * @param tenantId the owning tenant
     * @param cursor   opaque cursor from the previous page, or {@code null} for the first page
     * @param limit    maximum number of items to return per page
     */
    CursorPage<NotificationRule> findAllByTenant(TenantId tenantId, String cursor, int limit);

    void save(NotificationRule rule);

    void update(NotificationRule rule);

    void delete(RuleId ruleId, TenantId tenantId);
}
