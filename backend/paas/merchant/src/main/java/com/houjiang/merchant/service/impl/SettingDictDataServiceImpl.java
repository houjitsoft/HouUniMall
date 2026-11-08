package com.houjiang.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.service.ISettingDictDataService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.setting.DictDataCreateValidate;
import com.houjiang.merchant.validate.setting.DictDataUpdateValidate;
import com.houjiang.merchant.vo.setting.SettingDictDataVo;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.setting.DictData;
import com.houjiang.merchant.common.entity.setting.DictType;
import com.houjiang.merchant.common.mapper.setting.DictDataMapper;
import com.houjiang.merchant.common.mapper.setting.DictTypeMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 字典数据服务实现类
 */
@Service
public class SettingDictDataServiceImpl implements ISettingDictDataService {

    @Resource
    DictDataMapper dictDataMapper;

    @Resource
    DictTypeMapper dictTypeMapper;

    /**
     * 字典数据所有
     *
     * @author Amos
     * @return List<DictDataVo>
     */
    @Override
    public List<SettingDictDataVo> all(Map<String, String> params) {
        DictType dictType = dictTypeMapper.selectOne(new QueryWrapper<DictType>()
                .eq("dict_type", params.get("dictType"))
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(dictType, "该字典类型不存在!");

        QueryWrapper<DictData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id,type_id,name,type_value, value,remark,sort,status,create_time,update_time");
        queryWrapper.isNull("delete_time");
        queryWrapper.eq("type_id", dictType.getId());
        queryWrapper.orderByDesc("id");

        dictDataMapper.setSearch(queryWrapper, params, new String[]{
                "like:name:str",
                "like:value:str",
                "=:status:int",
        });

        List<DictData> dictDataList = dictDataMapper.selectList(queryWrapper);

        List<SettingDictDataVo> list = new LinkedList<>();
        for (DictData dictData : dictDataList) {
            SettingDictDataVo vo = new SettingDictDataVo();
            BeanUtils.copyProperties(dictData, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(dictData.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(dictData.getUpdateTime()));
            list.add(vo);
        }

        return list;
    }

    /**
     * 字典数据列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param params 搜索参数
     * @return PageResult<DictDataVo>
     */
    @Override
    public PageResult<SettingDictDataVo> list(PageValidate pageValidate, Map<String, String> params) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<DictData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id,type_id,name,type_value,value,remark,sort,status,create_time,update_time");

        queryWrapper.isNull("delete_time");
        queryWrapper.orderByDesc("id");

        if (StringUtils.isNotEmpty(params.get("type_id"))) {
            DictType dictType = dictTypeMapper.selectOne(new QueryWrapper<DictType>()
                    .isNull("delete_time")
                    .eq("id", params.get("type_id"))
                    .last("limit 1"));

            Assert.notNull(dictType, "该字典类型不存在!");

            queryWrapper.eq("type_id", dictType.getId());
        }


        dictDataMapper.setSearch(queryWrapper, params, new String[]{
                "like:name:str",
                "like:value:str",
                "=:status:int",
        });

        IPage<DictData> iPage = dictDataMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<SettingDictDataVo> list = new LinkedList<>();
        for (DictData dictData : iPage.getRecords()) {
            SettingDictDataVo vo = new SettingDictDataVo();
            BeanUtils.copyProperties(dictData, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(dictData.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(dictData.getUpdateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 字典数据详情
     *
     * @author Amos
     * @param id 主键
     * @return DictDataVo
     */
    @Override
    public SettingDictDataVo detail(Integer id) {
        DictData dictData = dictDataMapper.selectOne(new QueryWrapper<DictData>()
                .select("id,type_id,name,value,remark,sort,status,create_time,update_time")
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(dictData, "字典数据不存在！");

        SettingDictDataVo vo = new SettingDictDataVo();
        BeanUtils.copyProperties(dictData, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(dictData.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(dictData.getUpdateTime()));
        return vo;
    }

    /**
     * 字典数据新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(DictDataCreateValidate createValidate) {
        Assert.isNull(dictDataMapper.selectOne(new QueryWrapper<DictData>()
                .select("id")
                .eq("name", createValidate.getName())
                .isNull("delete_time")
                .last("limit 1")), "字典数据已存在！");

        DictData model = new DictData();
        model.setTypeId(createValidate.getTypeId());
        model.setName(createValidate.getName());
        model.setValue(createValidate.getValue());
        model.setTypeValue(createValidate.getTypeValue());
        model.setRemark(createValidate.getRemark());
        model.setSort(createValidate.getSort());
        model.setStatus(createValidate.getStatus());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        dictDataMapper.insert(model);
    }

    /**
     * 字典数据编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(DictDataUpdateValidate updateValidate) {
        DictData model = dictDataMapper.selectOne(new QueryWrapper<DictData>()
                .eq("id", updateValidate.getId())
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(model, "字典数据不存在！");

        Assert.isNull(dictDataMapper.selectOne(new QueryWrapper<DictData>()
                .select("id")
                .ne("id", updateValidate.getId())
                .eq("name", updateValidate.getName())
                .isNull("delete_time")
                .last("limit 1")), "字典数据已存在！");

        model.setName(updateValidate.getName());
        model.setValue(updateValidate.getValue());
        model.setTypeValue(updateValidate.getTypeValue());
        model.setRemark(updateValidate.getRemark());
        model.setSort(updateValidate.getSort());
        model.setStatus(updateValidate.getStatus());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        dictDataMapper.updateById(model);
    }

    /**
     * 字典数据删除
     *
     * @author Amos
     * @param ids 主键
     */
    @Override
    public void del(List<Integer> ids) {
        for (Integer id : ids) {
            DictData model = new DictData();
            model.setId(id);
            model.setDeleteTime(System.currentTimeMillis() / 1000);
            dictDataMapper.updateById(model);
        }
    }

}
