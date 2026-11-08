-- AI 中台 - 可视化工作流编排（Dify 式）
-- 说明：智能体默认走「提示词模式」；当执行模式切到 workflow 时，按 workflow_code 取画布图执行。
-- 幂等：建表用 IF NOT EXISTS，加列用 information_schema 判断。
-- 来源：scripts/sql/migrations/20261029-aiagent-workflow.sql，2026-10-29 并入 init 链
--       （此前只有迁移、全新装机缺 hou_ai_workflow / hou_ai_workflow_run 两表）。
-- 依赖：31-aiagent-agent.sql 建出的 hou_ai_agent（本脚本给其补 mode / workflow_code 两列）。

-- ---------------- 1. 工作流定义表 ----------------
CREATE TABLE IF NOT EXISTS `hou_ai_workflow` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code`         varchar(64)     NOT NULL                COMMENT '工作流编码，前台/智能体按此引用',
  `name`         varchar(128)    NOT NULL                COMMENT '工作流名称',
  `terminal`     varchar(16)     NOT NULL DEFAULT 'merchant' COMMENT '所属端 merchant/frontend/admin/im',
  `scene`        varchar(64)     NOT NULL DEFAULT ''     COMMENT '场景分组，复用智能体 scene 口径',
  `icon`         varchar(64)     NOT NULL DEFAULT ''     COMMENT 'Element Plus 图标名',
  `description`  varchar(512)    NOT NULL DEFAULT ''     COMMENT '用途说明',
  `status`       tinyint         NOT NULL DEFAULT 0      COMMENT '1 启用 / 0 停用',
  `model_code`   varchar(128)    NOT NULL DEFAULT ''     COMMENT '画布里未单独指定模型时的默认模型',
  `graph`        longtext                                COMMENT '画布 JSON：{nodes:[],edges:[],viewport:{}}',
  `version`      int             NOT NULL DEFAULT 1      COMMENT '每次保存自增，便于回溯',
  `built_in`     tinyint         NOT NULL DEFAULT 0      COMMENT '1 内置，拒绝删除',
  `create_time`  datetime        DEFAULT NULL,
  `update_time`  datetime        DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wf_code` (`code`),
  KEY `idx_wf_terminal` (`terminal`),
  KEY `idx_wf_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 中台 - 工作流（可视化编排）';

-- ---------------- 2. 工作流运行记录 ----------------
CREATE TABLE IF NOT EXISTS `hou_ai_workflow_run` (
  `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `run_no`        varchar(40)     NOT NULL                COMMENT '运行编号，如 WFR20261029xxxx',
  `workflow_code` varchar(64)     NOT NULL                COMMENT '工作流编码',
  `workflow_name` varchar(128)    NOT NULL DEFAULT ''     COMMENT '运行时的名称快照',
  `terminal`      varchar(16)     NOT NULL DEFAULT ''     COMMENT '所属端快照',
  `status`        varchar(16)     NOT NULL DEFAULT ''     COMMENT 'success 成功 / failed 失败 / running 执行中',
  `inputs`        text                                    COMMENT '入参 JSON',
  `outputs`       longtext                                COMMENT '出参 JSON',
  `traces`        longtext                                COMMENT '节点轨迹 JSON 数组：每个节点耗时/入参/出参/错误',
  `error_msg`     varchar(1024)   NOT NULL DEFAULT ''     COMMENT '失败原因',
  `cost_ms`       int             NOT NULL DEFAULT 0      COMMENT '总耗时（毫秒）',
  `token_used`    int             NOT NULL DEFAULT 0      COMMENT '累计 token',
  `create_time`   datetime        DEFAULT NULL,
  `finish_time`   datetime        DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wf_run_no` (`run_no`),
  KEY `idx_wf_run_code` (`workflow_code`),
  KEY `idx_wf_run_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 中台 - 工作流运行记录';

-- ---------------- 3. 智能体：执行模式 + 关联工作流 ----------------
SET @db = DATABASE();
SET @has_mode = (SELECT COUNT(*) FROM information_schema.columns
                 WHERE table_schema=@db AND table_name='hou_ai_agent' AND column_name='mode');
SET @sql_mode = IF(@has_mode = 0,
  'ALTER TABLE `hou_ai_agent` ADD COLUMN `mode` varchar(16) NOT NULL DEFAULT ''prompt'' COMMENT ''执行模式：prompt 提示词 / workflow 工作流'' AFTER `prompt`',
  'SELECT 1');
PREPARE s FROM @sql_mode; EXECUTE s; DEALLOCATE PREPARE s;

SET @has_wf = (SELECT COUNT(*) FROM information_schema.columns
               WHERE table_schema=@db AND table_name='hou_ai_agent' AND column_name='workflow_code');
SET @sql_wf = IF(@has_wf = 0,
  'ALTER TABLE `hou_ai_agent` ADD COLUMN `workflow_code` varchar(64) NOT NULL DEFAULT '''' COMMENT ''关联工作流编码（mode=workflow 时生效）'' AFTER `mode`',
  'SELECT 1');
PREPARE s2 FROM @sql_wf; EXECUTE s2; DEALLOCATE PREPARE s2;

-- ---------------- 4. 预置示例工作流（停用，供后台「从模板创建」与演示用） ----------------
INSERT INTO `hou_ai_workflow`
(`code`, `name`, `terminal`, `scene`, `icon`, `description`, `status`, `model_code`, `graph`, `version`, `built_in`, `create_time`, `update_time`)
VALUES
(
  'im_reply_flow',
  'IM 客服应答工作流（示例）',
  'im',
  'service',
  'ChatDotSquare',
  '示例：先判断用户情绪，负面走安抚分支，其余走标准应答，最后统一收敛成一句客服话术。停用状态仅供参照，复制后启用。',
  0,
  '',
  '{"nodes":[{"id":"start_1","type":"start","name":"开始","x":80,"y":160,"config":{"inputs":[{"name":"question","label":"用户问题","type":"string","default":"我上周买的鞋子还没发货"},{"name":"orderInfo","label":"订单信息","type":"string","default":"订单 SO20261029001，已付款未发货"}]}},{"id":"llm_1","type":"llm","name":"情绪判定","x":320,"y":160,"config":{"modelCode":"","temperature":0.1,"maxTokens":256,"systemPrompt":"你是情绪判定器。只输出一个单词：positive 或 negative。不要解释。","userPrompt":"用户问题：{{question}}","outputVar":"mood"}},{"id":"cond_1","type":"condition","name":"是否负面","x":560,"y":160,"config":{"varName":"mood","operator":"contains","value":"negative"}},{"id":"llm_2","type":"llm","name":"安抚应答","x":800,"y":60,"config":{"modelCode":"","temperature":0.6,"maxTokens":600,"systemPrompt":"你是耐心、有同理心的电商客服。先致歉并承认问题，再给出明确的时间承诺与补偿方案，不超过 120 字。","userPrompt":"用户问题：{{question}}\\n订单信息：{{orderInfo}}","outputVar":"reply"}},{"id":"llm_3","type":"llm","name":"标准应答","x":800,"y":280,"config":{"modelCode":"","temperature":0.6,"maxTokens":600,"systemPrompt":"你是专业的电商客服。直接、准确地回答用户问题，给出下一步动作，不超过 120 字。","userPrompt":"用户问题：{{question}}\\n订单信息：{{orderInfo}}","outputVar":"reply"}},{"id":"end_1","type":"end","name":"结束","x":1060,"y":160,"config":{"outputTemplate":"{{reply}}"}]},"edges":[{"id":"e1","source":"start_1","sourceHandle":"out","target":"llm_1"},{"id":"e2","source":"llm_1","sourceHandle":"out","target":"cond_1"},{"id":"e3","source":"cond_1","sourceHandle":"true","target":"llm_2"},{"id":"e4","source":"cond_1","sourceHandle":"false","target":"llm_3"},{"id":"e5","source":"llm_2","sourceHandle":"out","target":"end_1"},{"id":"e6","source":"llm_3","sourceHandle":"out","target":"end_1"}],"viewport":{"x":0,"y":0,"zoom":1}}',
  1,
  1,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ---------------- 复核 ----------------
SELECT 'workflow_table' AS item, COUNT(*) AS cnt FROM `hou_ai_workflow`
UNION ALL SELECT 'workflow_run_table', COUNT(*) FROM `hou_ai_workflow_run`
UNION ALL SELECT 'agent_has_mode', COUNT(*) FROM information_schema.columns
  WHERE table_schema=@db AND table_name='hou_ai_agent' AND column_name='mode'
UNION ALL SELECT 'agent_has_workflow_code', COUNT(*) FROM information_schema.columns
  WHERE table_schema=@db AND table_name='hou_ai_agent' AND column_name='workflow_code';
