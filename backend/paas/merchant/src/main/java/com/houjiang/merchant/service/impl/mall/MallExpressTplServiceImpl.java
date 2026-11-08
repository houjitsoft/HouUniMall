package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallExpressTpl;
import com.houjiang.merchant.common.enums.ExpressTplEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallExpressTplMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallExpressTplService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplCreateValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplSearchValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplUpdateValidate;
import com.houjiang.merchant.vo.mall.MallExpressTplDetailVo;
import com.houjiang.merchant.vo.mall.MallExpressTplGoodsVo;
import com.houjiang.merchant.vo.mall.MallExpressTplListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家账号实现类
 */
@Service
public class MallExpressTplServiceImpl implements IMallExpressTplService {

    @Resource
    MallExpressTplMapper mallExpressTplMapper;
    @Override
    public List<MallExpressTplGoodsVo> getListByShopId(Integer shopId) {
        List<MallExpressTpl> list = mallExpressTplMapper.selectList(new QueryWrapper<MallExpressTpl>().eq("shop_id", shopId).eq("delete_time", 0).select("id,name"));
        List<MallExpressTplGoodsVo> ret = new ArrayList<MallExpressTplGoodsVo>();
        if (list.size() > 0) {
            for (MallExpressTpl item : list) {
                MallExpressTplGoodsVo vo = new MallExpressTplGoodsVo();
                vo.setId(item.getId());
                vo.setName(item.getName());
                ret.add(vo);
            }
        }
        return ret;
    }


    /**
     * 运费模板列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallExpressTplListedVo>
     */
    @Override
    public PageResult<MallExpressTplListedVo> list(PageValidate pageValidate, MallExpressTplSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallExpressTpl> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id").eq("delete_time", 0);

        mallExpressTplMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "=:shopId@shop_id:int",
                "like:name:str",
                "=:type:int",
                "=:content:str",
                "=:remark:str",
        });

        IPage<MallExpressTpl> iPage = mallExpressTplMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallExpressTplListedVo> list = new LinkedList<>();
        for(MallExpressTpl item : iPage.getRecords()) {
            MallExpressTplListedVo vo = new MallExpressTplListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setTypeText(ExpressTplEnum.getMsgByCode(item.getType()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 运费模板详情
     *
     * @author Amos
     * @param id 主键参数
     * @return MallExpressTpl
     */
    @Override
    public MallExpressTplDetailVo detail(Integer id) {
        MallExpressTpl model = mallExpressTplMapper.selectOne(
                new QueryWrapper<MallExpressTpl>()
                        .eq("id", id).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的运费模板信息");
        }

        MallExpressTplDetailVo vo = new MallExpressTplDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setContent(JSONArray.parseArray(model.getContent()));
        return vo;
    }

    /**
     * 运费模板新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(MallExpressTplCreateValidate createValidate) {
        MallExpressTpl model = new MallExpressTpl();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        model.setType(createValidate.getType());
        model.setContent(JSONArray.toJSONString(createValidate.getContent()));
        model.setRemark(createValidate.getRemark());
        mallExpressTplMapper.insert(model);
    }

    /**
     * 运费模板编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(MallExpressTplUpdateValidate updateValidate) {
        MallExpressTpl model = mallExpressTplMapper.selectOne(
                new QueryWrapper<MallExpressTpl>()
                        .eq("id",  updateValidate.getId()).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的运费模板信息");
        }

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setType(updateValidate.getType());
        model.setContent(JSONArray.toJSONString(updateValidate.getContent()));
        model.setRemark(updateValidate.getRemark());
        mallExpressTplMapper.updateById(model);
    }

    /**
     * 运费模板删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        MallExpressTpl model = mallExpressTplMapper.selectOne(
                new QueryWrapper<MallExpressTpl>()
                        .eq("id", id).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他店铺的运费模板信息");
        }

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallExpressTplMapper.updateById(model);
        //mallExpressTplMapper.delete(new QueryWrapper<MallExpressTpl>().eq("id", id));
    }
}
