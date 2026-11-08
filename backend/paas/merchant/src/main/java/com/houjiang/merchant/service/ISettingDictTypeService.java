package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.setting.DictTypeCreateValidate;
import com.houjiang.merchant.validate.setting.DictTypeUpdateValidate;
import com.houjiang.merchant.vo.setting.SettingDictTypeVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 字典类型服务接口类
 */
public interface ISettingDictTypeService {

    /**
     * 字典类型所有
     *
     * @author Amos
     * @return List<DictTypeVo>
     */
    List<SettingDictTypeVo> all();

    /**
     * 字典类型列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param params 搜索参数
     * @return PageResult<DictDataVo>
     */
    PageResult<SettingDictTypeVo> list(PageValidate pageValidate, Map<String, String> params);

    /**
     * 字典类型详情
     *
     * @author Amos
     * @param id 主键
     * @return DictDataVo
     */
    SettingDictTypeVo detail(Integer id);

    /**
     * 字典类型新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(DictTypeCreateValidate createValidate);

    /**
     * 字典类型编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(DictTypeUpdateValidate updateValidate);

    /**
     * 字典类型删除
     *
     * @author Amos
     * @param ids 主键
     */
    void del(Integer ids);

}
