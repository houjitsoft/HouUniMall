package com.houjiang.mall.service.setting;

import com.houjiang.mall.validator.setting.PayConfigUpdateParam;
import com.houjiang.mall.vo.setting.SettingMallPayVo;

import java.util.List;

/**
 * 支付配置 Service。
 *
 * @author Amos
 */
public interface SettingPayConfigService {

    /** 支付配置列表（无分页，按 sort+id 倒序） */
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<SettingMallPayVo> list();

    /** 支付配置详情 */
    /**
     * TODO 补充方法说明
     * @param id TODO
     * @return TODO
     */
    SettingMallPayVo detail(Integer id);

    /** 支付配置编辑 */
    /**
     * TODO 补充方法说明
     * @param param TODO
     */
    void edit(PayConfigUpdateParam param);
}
