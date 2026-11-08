-- Mall plugin market catalog and initial official SKU data.
-- Price is stored in price_cents (1 CNY = 100 cents). Purchase state is stored
-- in hou_market_plugin_license and must never be hard-coded in a page or manifest.

CREATE TABLE IF NOT EXISTS hou_market_plugin (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plugin_code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    author_id BIGINT NOT NULL DEFAULT 1,
    category VARCHAR(64) DEFAULT NULL,
    cate_id INT NOT NULL DEFAULT 0 COMMENT '分类引用 hou_addon_cate.id；0=尚未归类',
    description TEXT,
    short_desc VARCHAR(256) DEFAULT NULL,
    icon_url VARCHAR(512) DEFAULT NULL,
    screenshots_json TEXT,
    latest_version VARCHAR(32) DEFAULT NULL,
    license_type VARCHAR(16) NOT NULL DEFAULT 'FREE',
    price_cents INT NOT NULL DEFAULT 0,
    trial_days INT NOT NULL DEFAULT 0,
    download_count INT NOT NULL DEFAULT 0,
    install_count INT NOT NULL DEFAULT 0,
    rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    rating_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    min_framework_version VARCHAR(32) DEFAULT NULL,
    tags_json TEXT,
    terminals_json TEXT,
    dependencies_json TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_plugin_code (plugin_code),
    KEY idx_market_plugin_status_category (status, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场商品与定价';

CREATE TABLE IF NOT EXISTS hou_market_plugin_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plugin_code VARCHAR(64) NOT NULL,
    version VARCHAR(32) NOT NULL,
    jar_url VARCHAR(512) NOT NULL,
    jar_md5 VARCHAR(64) NOT NULL,
    jar_size BIGINT DEFAULT NULL,
    jar_signature TEXT,
    changelog TEXT,
    schema_sql TEXT,
    rollback_sql TEXT,
    plugin_yaml TEXT,
    compatibility_json TEXT,
    status TINYINT NOT NULL DEFAULT 0,
    review_note TEXT,
    publish_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_plugin_version (plugin_code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场版本制品';

CREATE TABLE IF NOT EXISTS hou_market_plugin_license (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plugin_code VARCHAR(64) NOT NULL,
    buyer_id BIGINT NOT NULL DEFAULT 1,
    instance_id VARCHAR(128) DEFAULT NULL,
    license_key VARCHAR(256) NOT NULL,
    license_type VARCHAR(16) NOT NULL,
    max_instances INT NOT NULL DEFAULT 1,
    expire_time DATETIME DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_license_key (license_key),
    KEY idx_market_license_buyer_plugin (buyer_id, plugin_code),
    KEY idx_market_license_instance (instance_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场购买授权';

-- Initial official catalog. All currently migrated business plugins are paid
-- at 30,000 CNY by default. This migration seeds an empty catalog only; when
-- a row already exists, its operational pricing and license type are preserved.
INSERT INTO hou_market_plugin
    (plugin_code, name, author_id, category, description, short_desc, latest_version,
     license_type, price_cents, trial_days, status, min_framework_version,
     tags_json, terminals_json, dependencies_json, deleted)
VALUES
    ('erp', 'ERP', 1, '供应链与生产', 'Enterprise resource planning', 'Products, sales, inventory, purchasing and finance', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('webpos', 'WebPOS', 1, '门店与履约', 'Point of sale and cashier operations', 'Point of sale and cashier operations', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin","cashier"]', '[]', 0),
    ('oa', 'OA', 1, '办公与财务', 'Office automation, contracts, attendance and HR', 'Office automation and HR', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('mes', 'MES', 1, '供应链与生产', 'Manufacturing execution and production operations', 'Manufacturing execution and production operations', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('finance', 'Finance', 1, '办公与财务', 'Ledger, vouchers, receivables and financial reports', 'Financial accounting and reporting', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('activity', 'Activity', 1, '营销与客户', 'Coupons, campaigns, promotions and marketing activities', 'Marketing campaigns and promotions', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin","seller","mobile"]', '[]', 0),
    ('mall-advertising', '广告管理', 1, '营销与客户', '平台广告位管理、商家投放申请与商城广告轮播', '登录页轮播广告与商家投放审核', '1.0.0', 'PAID', 1, 0, 2, '1.0.0', '["official","advertising"]', '["admin","seller","pc"]', '[]', 0),
    ('cms', 'CMS', 1, '内容与沟通', 'Sites, channels, articles and content operations', 'Content management and publishing', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('crm', 'CRM', 1, '营销与客户', 'Customers, contacts, opportunities and service tickets', 'Customer relationship management', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('im', 'IM', 1, '内容与沟通', 'Realtime customer service, conversations and ticket handling', 'Customer service workspace', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('logging', 'Logging', 1, '开发与运维', 'Operation, access, business and ELK log management', 'Audit and runtime log management', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('geo', 'GEO', 1, '营销与客户', 'Generative engine optimization and content intelligence', 'Content intelligence and GEO', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('orderfood', 'Order Food', 1, '门店与履约', 'Restaurant ordering and delivery operations', 'Restaurant ordering and delivery operations', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0),
    ('pm', 'Project Management', 1, '办公与财务', 'Projects, tasks, testing and delivery workflows', 'Project, task and delivery management', '1.0.0', 'PAID', 3000000, 0, 2, '1.0.0', '["official"]', '["admin"]', '[]', 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    -- Category belongs to the operator after initial creation; manifest defaults never reset it.
    description = VALUES(description),
    short_desc = VALUES(short_desc),
    latest_version = VALUES(latest_version),
    -- license_type, price_cents and trial_days are intentionally omitted here:
    -- they are operator-managed data and must not be overwritten on rerun.
    status = VALUES(status),
    min_framework_version = VALUES(min_framework_version),
    tags_json = VALUES(tags_json),
    terminals_json = VALUES(terminals_json),
    dependencies_json = VALUES(dependencies_json),
    deleted = 0,
    update_time = NOW();

-- Do not insert a blanket license here. A purchase is represented by a row in
-- hou_market_plugin_license created after payment or platform approval, and
-- the plugin center reads that row to calculate PURCHASED/NOT_PURCHASED.

-- base owns both catalogs. Resolve the shared dictionary after seed insertion; retain
-- existing operator choices, and use the site catalog as the canonical assignment.
UPDATE hou_market_plugin m
 JOIN hou_addon_plugin p ON p.plugin_code COLLATE utf8mb4_general_ci=m.plugin_code COLLATE utf8mb4_general_ci AND p.deleted=0
 JOIN hou_addon_cate c ON c.id=p.cate_id
 SET m.cate_id=p.cate_id,m.category=c.name
 WHERE m.cate_id<>p.cate_id OR NOT(m.category COLLATE utf8mb4_general_ci<=>c.name COLLATE utf8mb4_general_ci);
UPDATE hou_market_plugin m
 JOIN (SELECT name,MIN(id) AS id FROM hou_addon_cate GROUP BY name) c
   ON c.name COLLATE utf8mb4_general_ci=m.category COLLATE utf8mb4_general_ci
 SET m.cate_id=c.id
 WHERE COALESCE(m.cate_id,0)=0;
