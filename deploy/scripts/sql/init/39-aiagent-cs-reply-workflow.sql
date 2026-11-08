-- ============================================================
-- AI 智能体：IM 客服应答（cs_reply）接入工作流 im_reply_flow
--
-- 依赖：
--   31-aiagent-agent.sql（智能体表，含 mode / workflow_code 列）
--   37-aiagent-workflow.sql（工作流表 + 示例工作流 im_reply_flow）
--
-- 背景：工作流引擎与画布上线后，73 个智能体全部仍是 prompt 模式，
--       没有任何智能体真正挂工作流，示例流程等于没人用、也没被验证过。
--       这里把「IM 会话智能回复」接到示例流程上，让工作流链路在日常使用中被真实跑到。
--
-- 幂等：按 code 定位且带条件更新，重复执行结果一致
-- ============================================================

UPDATE `hou_ai_agent`
   SET `mode`          = 'workflow',
       `workflow_code` = 'im_reply_flow'
 WHERE `code` = 'cs_reply'
   AND (`mode` <> 'workflow' OR IFNULL(`workflow_code`, '') <> 'im_reply_flow');
