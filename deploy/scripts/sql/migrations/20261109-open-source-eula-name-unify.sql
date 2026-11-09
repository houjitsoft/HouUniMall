-- 2026-11-09：开源 EULA 正文中的产品名统一为 HouUniMall。
-- 背景：产品定名由「UniMall 多商户商城系统」变更为「HouUniMall 多商户商城系统」，
--       EULA 正文首段仍写「UniMall / HouCloud」，需一并订正。
-- 性质：纯名称订正，权利义务、责任上限、接受方式均无实质变化，
--       故不新增版本号、不触发已安装站点重新确认。
-- 范围：hou_base_agreement_version 中 OPEN_SOURCE_EULA 的全部历史版本正文。
-- 幂等：REPLACE 后再次执行结果不变；无匹配行时不报错。

UPDATE `hou_base_agreement_version`
SET `content` = REPLACE(`content`, 'UniMall / HouCloud', 'HouUniMall')
WHERE `agreement_code` = 'OPEN_SOURCE_EULA'
  AND `content` LIKE '%UniMall / HouCloud%';

-- 变更说明同步订正（该列会展示在后台协议版本列表，避免列表里仍显示旧名）。
UPDATE `hou_base_agreement_version`
SET `change_note` = REPLACE(`change_note`, 'UniMall / HouCloud', 'HouUniMall')
WHERE `agreement_code` = 'OPEN_SOURCE_EULA'
  AND `change_note` LIKE '%UniMall / HouCloud%';
