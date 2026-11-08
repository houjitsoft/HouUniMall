-- ============================================================
-- AI 智能体：回填「使用位置」说明（usage_page）
--
-- 依赖：31-aiagent-agent.sql（已建表并含 usage_page 列）
-- 幂等：按 code 定位，重复执行结果一致；未列出的编码保持原值
-- ============================================================

UPDATE `hou_ai_agent`
SET `usage_page` = CASE `code`
  -- ---------- 商家端（merchant） ----------
  WHEN 'copy_generate'      THEN '商品管理 → 商品编辑页「AI 生成文案」'
  WHEN 'marketing_plan'     THEN '营销中心 → 活动创建页「AI 策划」'
  WHEN 'stock_alert'        THEN '库存管理 → 补货预警列表（定时任务生成）'
  WHEN 'comment_sentiment'  THEN '商品管理 → 评价管理「AI 分析」'
  WHEN 'aftersale_ticket'   THEN '售后工单 → 工单详情「AI 处理建议」'
  WHEN 'price_suggest'      THEN '商品编辑 → 价格「AI 定价建议」'
  WHEN 'promo_copy'         THEN '营销中心 → 活动文案「AI 生成」'
  WHEN 'customer_rfm'       THEN '客户管理 → 客户分层「AI 分层与召回」'
  WHEN 'refund_reason'      THEN '售后 → 退款单详情「AI 归因」'
  WHEN 'qa_generate'        THEN '商品编辑 → 商品问答「AI 生成」'
  WHEN 'spec_generate'      THEN '商品编辑 → 商品规格「AI 生成」'
  WHEN 'live_script'        THEN '直播管理 → 直播脚本「AI 生成」'
  WHEN 'decoration_copy'    THEN '店铺装修 → 装修文案「AI 生成」'
  WHEN 'shop_diagnose'      THEN '店铺首页 → 经营体检卡片'
  WHEN 'short_video_script' THEN '内容中心 → 短视频「AI 分镜脚本」'
  WHEN 'bad_review_reply'   THEN '评价管理 → 差评「AI 回复」'
  WHEN 'bundle_suggest'     THEN '营销 → 搭配购「AI 推荐组合」'
  WHEN 'logistics_plan'     THEN '订单发货 → 发货方案「AI 建议」'
  WHEN 'slow_move_clear'    THEN '库存管理 → 滞销商品「AI 清仓策略」'
  WHEN 'ad_copy'            THEN '推广投放 → 广告文案「AI 生成」'
  WHEN 'member_benefit'     THEN '会员管理 → 等级与权益「AI 设计」'

  -- ---------- 前端 / C 端（frontend） ----------
  WHEN 'goods_recommend'    THEN '商城首页 / 商品详情 → 「猜你喜欢」推荐位'
  WHEN 'home_personal'      THEN '商城首页 → 「为你推荐」楼层'
  WHEN 'order_smart'        THEN '下单页 / 订单详情 → 「智能下单与改单」'
  WHEN 'intent_recognize'   THEN '全站搜索框 / 智能导购入口'
  WHEN 'search_rewrite'     THEN '搜索结果页 → 「你是不是要找」改写提示'
  WHEN 'goods_compare'      THEN '商品对比页 → 「AI 对比解读」'
  WHEN 'size_advisor'       THEN '商品详情 → 「尺码助手」'
  WHEN 'review_summary'     THEN '商品详情 → 评价区「AI 买家摘要」'
  WHEN 'cart_recovery'      THEN '购物车页 / 购物车召回推送'
  WHEN 'gift_advisor'       THEN '送礼专区 → 「AI 选礼」'
  WHEN 'demand_collect'     THEN '找货 / 需求发布页 → 需求澄清对话'
  WHEN 'budget_plan'        THEN '清单页 → 「按预算配齐」'
  WHEN 'usage_guide'        THEN '商品详情「使用指南」/ 订单完成页'
  WHEN 'return_help'        THEN '售后中心 → 「自助退换指引」'
  WHEN 'logistics_explain'  THEN '物流详情页 → 「物流异常解读」'
  WHEN 'taste_profile'      THEN '选品助手 → 「问答式选品」'

  -- ---------- 管理后台（admin） ----------
  WHEN 'bi_insight'         THEN '数据中台 → 仪表盘「AI 解读」'
  WHEN 'operation_advice'   THEN '数据中台 → 运营策略建议卡片'
  WHEN 'goods_audit'        THEN '商品管理 → 商品审核页「AI 审核」'
  WHEN 'merchant_audit'     THEN '商家管理 → 入驻审核「AI 辅助」'
  WHEN 'risk_detect'        THEN '交易管理 → 订单风险标记'
  WHEN 'category_map'       THEN '商品导入 / 类目映射工具'
  WHEN 'campaign_review'    THEN '营销活动 → 活动详情「AI 复盘」'
  WHEN 'seo_keywords'       THEN '商品编辑 → SEO 设置「AI 生成关键词」'
  WHEN 'finance_recon'      THEN '财务管理 → 对账「差异分析」'
  WHEN 'compliance_scan'    THEN '商品管理 → 合规巡检（定时任务）'
  WHEN 'price_monitor'      THEN '商品管理 → 价格异常监控（定时任务）'
  WHEN 'content_moderate'   THEN '评价管理 → 内容审核「AI 初筛」'
  WHEN 'data_quality'       THEN '商品管理 → 数据质量体检'
  WHEN 'merchant_health'    THEN '商家管理 → 商家健康度评分'
  WHEN 'ticket_triage'      THEN '工单列表 → 「智能分派与优先级」'

  -- ---------- IM 客服 / 消息通知（im） ----------
  WHEN 'cs_reply'           THEN 'IM 会话窗口 → 「AI 回复」'
  WHEN 'im_intent_route'    THEN 'IM 会话接入 → 意图识别与智能路由'
  WHEN 'im_greeting'        THEN 'IM 会话首次接入 → 自动开场白'
  WHEN 'im_reply_multi'     THEN 'IM 会话中 → 多轮追问与澄清'
  WHEN 'im_objection'       THEN 'IM 会话 → 议价 / 异议处理场景'
  WHEN 'im_emotion'         THEN 'IM 会话 → 情绪识别与安抚提示'
  WHEN 'im_close'           THEN 'IM 会话结束前 → 收尾与引导'
  WHEN 'im_summary'         THEN 'IM 会话结束 → 自动摘要与工单沉淀'
  WHEN 'im_quick_reply'     THEN 'IM 后台 → 快捷语库「AI 生成」'
  WHEN 'im_auto_keyword'    THEN 'IM 后台 → 关键词自动回复配置'
  WHEN 'im_knowledge'       THEN 'IM 后台 → 会话知识抽取入库'
  WHEN 'im_quality'         THEN 'IM 后台 → 客服会话质检'
  WHEN 'im_cs_coach'        THEN 'IM 后台 → 客服话术辅导'
  WHEN 'notify_plan'        THEN '消息中心 → 通知任务时机与频次编排'
  WHEN 'notify_channel'     THEN '消息中心 → 通知渠道选择与降级'
  WHEN 'notify_personalize' THEN '消息中心 → 通知文案个性化'
  WHEN 'notify_fatigue'     THEN '消息中心 → 打扰度评估与合并'
  WHEN 'notify_recall'      THEN '消息中心 → 沉默用户召回任务'
  WHEN 'tpl_generate'       THEN '消息中心 → 模板管理「AI 生成」'
  WHEN 'tpl_variable'       THEN '消息中心 → 模板变量校验'
  WHEN 'tpl_optimize'       THEN '消息中心 → 模板效果诊断与改写'

  ELSE `usage_page`
END
WHERE `code` IN (
  'copy_generate','marketing_plan','stock_alert','comment_sentiment','aftersale_ticket',
  'price_suggest','promo_copy','customer_rfm','refund_reason','qa_generate','spec_generate',
  'live_script','decoration_copy','shop_diagnose','short_video_script','bad_review_reply',
  'bundle_suggest','logistics_plan','slow_move_clear','ad_copy','member_benefit',
  'goods_recommend','home_personal','order_smart','intent_recognize','search_rewrite',
  'goods_compare','size_advisor','review_summary','cart_recovery','gift_advisor',
  'demand_collect','budget_plan','usage_guide','return_help','logistics_explain','taste_profile',
  'bi_insight','operation_advice','goods_audit','merchant_audit','risk_detect','category_map',
  'campaign_review','seo_keywords','finance_recon','compliance_scan','price_monitor',
  'content_moderate','data_quality','merchant_health','ticket_triage',
  'cs_reply','im_intent_route','im_greeting','im_reply_multi','im_objection','im_emotion',
  'im_close','im_summary','im_quick_reply','im_auto_keyword','im_knowledge','im_quality',
  'im_cs_coach','notify_plan','notify_channel','notify_personalize','notify_fatigue',
  'notify_recall','tpl_generate','tpl_variable','tpl_optimize'
);
