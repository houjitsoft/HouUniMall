package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopGoodsCategory;
import com.houjiang.merchant.common.mapper.mall.MallShopGoodsCategoryMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.service.mall.IMallShopGoodsCategoryService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategorySearchValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryStatusValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryDetailVo;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryListedVo;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryOtherListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家商品分类实现类
 * @author Amos
 */
@Service
public class MallShopGoodsCategoryServiceImpl implements IMallShopGoodsCategoryService {

    @Resource
    IMallGoodsService iMallGoodsService;
    @Resource
    MallShopGoodsCategoryMapper mallShopGoodsCategoryMapper;

    /**
     * 商家商品分类列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopGoodsCategoryListedVo>
     */
    @Override
    public PageResult<MallShopGoodsCategoryListedVo> list(PageValidate pageValidate, MallShopGoodsCategorySearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallShopGoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));

        mallShopGoodsCategoryMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:shopId@shop_id:int",
            "like:name:str",
            "=:image:str",
            "=:sort:int",
            "=:isShow@is_show:int",
        });

        queryWrapper.eq("delete_time", 0);

        IPage<MallShopGoodsCategory> iPage = mallShopGoodsCategoryMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallShopGoodsCategoryListedVo> list = new LinkedList<>();
        for(MallShopGoodsCategory item : iPage.getRecords()) {
            MallShopGoodsCategoryListedVo vo = new MallShopGoodsCategoryListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toShopAbsoluteUrl(item.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setGoodsNum(iMallGoodsService.getShopGoodsCategoryCountsCateId(item.getShopId(), item.getId()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 商家商品分类详情
     *
     * @author Amos
     * @param id 主键参数
     * @return MallShopGoodsCategory
     */
    @Override
    public MallShopGoodsCategoryDetailVo detail(Integer id) {
        MallShopGoodsCategory model = mallShopGoodsCategoryMapper.selectOne(
                new QueryWrapper<MallShopGoodsCategory>()
                        .eq("id", id)
                        .eq("delete_time", 0)
                        .eq("shop_id", MerchantThreadLocal.getShopId())
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        MallShopGoodsCategoryDetailVo vo = new MallShopGoodsCategoryDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setImage(UrlUtils.toShopAbsoluteUrl(model.getImage()));
        return vo;
    }

    /**
     * 商家商品分类新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(MallShopGoodsCategoryCreateValidate createValidate) {
        Assert.isTrue(titleUnique(createValidate.getName(), createValidate.getShopId()), "分类名称已存在");
        MallShopGoodsCategory model = new MallShopGoodsCategory();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        model.setImage(UrlUtils.toShopRelativeUrl(createValidate.getImage()));
        model.setSort(createValidate.getSort());
        model.setIsShow(createValidate.getIsShow());
        model.setDeleteTime(0L);
        mallShopGoodsCategoryMapper.insert(model);
    }

    /**
     * 商家商品分类编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(MallShopGoodsCategoryUpdateValidate updateValidate) {
        Assert.isTrue(titleUnique(updateValidate.getName(), updateValidate.getId()), "分类名称已存在");
        MallShopGoodsCategory model = mallShopGoodsCategoryMapper.selectOne(
                new QueryWrapper<MallShopGoodsCategory>()
                        .eq("id",  updateValidate.getId())
                        .eq("shop_id", updateValidate.getShopId())
                        .eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setImage(UrlUtils.toShopRelativeUrl(updateValidate.getImage()));
        model.setSort(updateValidate.getSort());
        model.setIsShow(updateValidate.getIsShow());
        mallShopGoodsCategoryMapper.updateById(model);
    }

    /**
     * 商家商品分类删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        Assert.isTrue(!iMallGoodsService.checkShopGoodsCategoryHas(id), "有商品正在使用商品分类，不能删除");
        MallShopGoodsCategory model = mallShopGoodsCategoryMapper.selectOne(
                new QueryWrapper<MallShopGoodsCategory>()
                        .eq("id", id)
                        .eq("shop_id", MerchantThreadLocal.getShopId())
                        .eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        model.setDeleteTime(System.currentTimeMillis() / 1000);
        mallShopGoodsCategoryMapper.updateById(model);
    }

    @Override
    public Boolean titleUnique(String name, Integer shopId) {
        return  getTitleUnique(name, shopId,0);
    }

    @Override
    public Boolean titleUnique(String name, Integer shopId, Integer id) {
        return  getTitleUnique(name, shopId, id);
    }

    @Override
    public void status(MallShopGoodsCategoryStatusValidate statusValidate) {
        MallShopGoodsCategory model = this.getById(statusValidate.getId());

        Assert.notNull(model, "数据不存在!");
        model.setIsShow(statusValidate.getIsShow());
        mallShopGoodsCategoryMapper.updateById(model);
    }

    @Override
    public MallShopGoodsCategory getById(Integer id) {
        return  mallShopGoodsCategoryMapper.selectOne(
                new QueryWrapper<MallShopGoodsCategory>()
                        .eq("id", id)
                        .eq("shop_id", MerchantThreadLocal.getShopId())
                        .eq("delete_time", 0)
                        .last("limit 1"));
    }

    @Override
    public List<MallShopGoodsCategoryOtherListedVo> otherList(Integer shopId) {
        List<MallShopGoodsCategory> retlist = mallShopGoodsCategoryMapper.selectList(new QueryWrapper<MallShopGoodsCategory>().eq("shop_id", shopId).eq("delete_time", 0).orderByDesc("sort").orderByAsc("id"));
        List<MallShopGoodsCategoryOtherListedVo> list = new ArrayList<MallShopGoodsCategoryOtherListedVo>();
        for (MallShopGoodsCategory item : retlist) {
            MallShopGoodsCategoryOtherListedVo vo = new MallShopGoodsCategoryOtherListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toShopAbsoluteUrl(vo.getImage()));
            list.add(vo);
        }
        return list;
    }

    /**
     * 返回 true 代表唯一
     * 返回 false 代表已有记录
     * @param name
     * @param id
     * @return
     */
    private Boolean getTitleUnique(String name, Integer shopId, Integer id) {
        QueryWrapper<MallShopGoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId).eq("name", name).select("id");
        if (id != 0) {
            queryWrapper.ne("id", id);
        }
        queryWrapper.eq("delete_time", 0).last("limit 1");
        MallShopGoodsCategory model = mallShopGoodsCategoryMapper.selectOne(queryWrapper);
        return StringUtils.isNull(model) ? true : false;
    }

}
