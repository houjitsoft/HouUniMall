package com.houjiang.mall.service.setting;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.mall.dto.PageParam;
import com.houjiang.mall.validator.setting.DictDataCreateParam;
import com.houjiang.mall.validator.setting.DictDataUpdateParam;
import com.houjiang.mall.vo.setting.SettingDictDataVo;

import java.util.List;
import java.util.Map;

/**
 * 字典数据 Service。
 *
 * @author Amos
 */
public interface SettingDictDataService {

    /** 全部数据（按 typeId 过滤） */
    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    List<SettingDictDataVo> all(Map<String, String> params);

    /** 分页列表 */
    /**
     * TODO 补充方法说明
     * @param pageParam TODO
     * @param params TODO
     * @return TODO
     */
    IPage<SettingDictDataVo> list(PageParam pageParam, Map<String, String> params);

    /** 详情 */
    /**
     * TODO 补充方法说明
     * @param id TODO
     * @return TODO
     */
    SettingDictDataVo detail(Integer id);

    /** 新增 */
    /**
     * TODO 补充方法说明
     * @param param TODO
     */
    void add(DictDataCreateParam param);

    /** 编辑 */
    /**
     * TODO 补充方法说明
     * @param param TODO
     */
    void edit(DictDataUpdateParam param);

    /** 删除（软删） */
    /**
     * TODO 补充方法说明
     * @param id TODO
     */
    void del(Integer id);
}
