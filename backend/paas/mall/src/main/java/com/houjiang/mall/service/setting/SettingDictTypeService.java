package com.houjiang.mall.service.setting;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.mall.dto.PageParam;
import com.houjiang.mall.validator.setting.DictTypeCreateParam;
import com.houjiang.mall.validator.setting.DictTypeUpdateParam;
import com.houjiang.mall.vo.setting.SettingDictTypeVo;

import java.util.List;
import java.util.Map;

/**
 * 字典类型 Service 接口
 *
 * @author Amos
 */
public interface SettingDictTypeService {

    /**
     * 查询全部字典类型（未删除、按 id 倒序）
     */
    List<SettingDictTypeVo> all();

    /**
     * 分页查询字典类型
     *
     * @param pageParam 分页参数
     * @param params    搜索参数（name/type 模糊，status 等于）
     * @return MyBatis-Plus IPage 结果
     */
    IPage<SettingDictTypeVo> list(PageParam pageParam, Map<String, String> params);

    /**
     * 查询字典类型详情
     */
    SettingDictTypeVo detail(Integer id);

    /**
     * 新增字典类型
     */
    void add(DictTypeCreateParam param);

    /**
     * 编辑字典类型
     */
    void edit(DictTypeUpdateParam param);

    /**
     * 删除字典类型（软删除）
     */
    void del(Integer id);
}
