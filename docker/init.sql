-- 1. Khởi tạo Bảng
CREATE TABLE notification_rules (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    service_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    conditions_json JSONB NOT NULL,
    cooldown_minutes INT DEFAULT 5,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo index để tăng tốc độ truy vấn
CREATE INDEX idx_rules_tenant_event ON notification_rules (tenant_id, event_type);

-- 2. Chèn Mock Data
-- Giả sử chúng ta có công ty "Avocado Transport" với Tenant ID toàn số 1
-- Công ty này mua gói 'REMODUL' và thiết lập quy tắc:
-- "Cảnh báo nếu Tốc độ > 80 VÀ Cruise Control đang Tắt"

INSERT INTO notification_rules (id, tenant_id, service_id, event_type, conditions_json, cooldown_minutes, is_active)
VALUES (
    '22222222-2222-2222-2222-222222222222', -- Rule ID
    '11111111-1111-1111-1111-111111111111', -- Tenant ID (Avocado Transport)
    'REMODUL',
    'SPEED_NO_CRUISE',
    '{
      "type": "LOGICAL",
      "operator": "AND",
      "children": [
        {
          "type": "CONDITION",
          "field": "speed",
          "operator": ">",
          "value": 80
        },
        {
          "type": "CONDITION",
          "field": "cruise_control",
          "operator": "==",
          "value": false
        }
      ]
    }'::jsonb,
    5,
    true
);