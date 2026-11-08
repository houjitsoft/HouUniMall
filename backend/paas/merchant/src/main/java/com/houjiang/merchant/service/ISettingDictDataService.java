package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.setting.DictDataCreateValidate;
import com.houjiang.merchant.validate.setting.DictDataUpdateValidate;
import com.houjiang.merchant.vo.setting.SettingDictDataVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 字典数据服务接口类
 */
public interface ISettingDictDataService {

    /**
     * 字典数据所有
     *
     * @author Amos
     * @return List<DictDataVo>
     */
    List<SettingDictDataVo> all(Map<String, String> params);

    /**
     * 字典数据列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param params 搜索参数
     * @return PageResult<DictDataVo>
     */
    PageResult<SettingDictDataVo> list(PageValidate pageValidate, Map<String, String> params);

    /**
     * 字典数据详情
     *
     * @author Amos
     * @param id 主键
     * @return DictDataVo
     */
    SettingDictDataVo detail(Integer id);

    /**
     * 字典数据新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(DictDataCreateValidate createValidate);

    /**
     * 字典数据编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(DictDataUpdateValidate updateValidate);

    /**
     * 字典数据删除
     *
     * @author Amos
     * @param ids 主键
     */
    void del(List<Integer> ids);

}
