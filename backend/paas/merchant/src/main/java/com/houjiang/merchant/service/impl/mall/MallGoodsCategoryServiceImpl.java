package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsCategory;
import com.houjiang.merchant.common.entity.mall.MallGoodsCategoryIndex;
import com.houjiang.merchant.common.mapper.mall.MallGoodsCategoryMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.mall.IMallGoodsCategoryIndexService;
import com.houjiang.merchant.service.mall.IMallGoodsCategoryService;
import com.houjiang.merchant.validate.mall.MallGoodsCategorySearchValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 商品分类实现类
 * @author Amos
 */
@Service
public class MallGoodsCategoryServiceImpl implements IMallGoodsCategoryService {
        
    @Resource
    MallGoodsCategoryMapper mallGoodsCategoryMapper;
    @Resource
    IMallGoodsCategoryIndexService iMallGoodsCategoryIndexService;

    /**
     * 商品分类列表
     *
     * @author Amos
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsCategoryListedVo>
     */
    @Override
    public List<MallGoodsCategoryListedVo> list(MallGoodsCategorySearchValidate searchValidate) {
        QueryWrapper<MallGoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", 0);
        queryWrapper.eq("delete_time", 0);
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        List<MallGoodsCategory> lists = mallGoodsCategoryMapper.selectList(queryWrapper);
        List<MallGoodsCategoryListedVo> list = new LinkedList<>();
        for(MallGoodsCategory item : lists) {
            MallGoodsCategoryListedVo vo = new MallGoodsCategoryListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setImage(UrlUtils.toAdminAbsoluteUrl(item.getImage()));
            vo.setHasParent(0);


            List<MallGoodsCategory> sonsList = mallGoodsCategoryMapper.selectList(new QueryWrapper<MallGoodsCategory>().eq("pid", item.getId()).eq("delete_time", 0));
            List<MallGoodsCategoryListedVo> sons = new LinkedList<>();
            for(MallGoodsCategory sonItem : sonsList) {
                MallGoodsCategoryListedVo son = new MallGoodsCategoryListedVo();
                BeanUtils.copyProperties(sonItem, son);
                son.setImage(UrlUtils.toAdminAbsoluteUrl(sonItem.getImage()));

                List<MallGoodsCategory> lv3SonsList = mallGoodsCategoryMapper.selectList(new QueryWrapper<MallGoodsCategory>().eq("pid", sonItem.getId()).eq("delete_time", 0));
                List<MallGoodsCategoryListedVo> lv3Sons = new LinkedList<>();
                for(MallGoodsCategory lv3SonItem : lv3SonsList) {
                    MallGoodsCategoryListedVo lv3Son = new MallGoodsCategoryListedVo();
                    BeanUtils.copyProperties(lv3SonItem, lv3Son);
                    lv3Son.setImage(UrlUtils.toAdminAbsoluteUrl(lv3SonItem.getImage()));
                    lv3Son.setCreateTime(TimeUtils.timestampToDate(lv3SonItem.getCreateTime()));
                    lv3Son.setUpdateTime(TimeUtils.timestampToDate(lv3SonItem.getUpdateTime()));
                    lv3Son.setHasParent(1);
                    lv3Sons.add(lv3Son);
                }

                son.setSons(lv3Sons);
                son.setCreateTime(TimeUtils.timestampToDate(sonItem.getCreateTime()));
                son.setUpdateTime(TimeUtils.timestampToDate(sonItem.getUpdateTime()));
                son.setHasParent(1);
                sons.add(son);
            }

            vo.setSons(sons);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            list.add(vo);
        }

        return list;
    }

    /**
     * 商品分类详情
     *
     * @author Amos
     * @param id 主键参数
     * @return MallGoodsCategory
     */
    @Override
    public MallGoodsCategoryDetailVo detail(Integer id) {
        MallGoodsCategory model = mallGoodsCategoryMapper.selectOne(
                new QueryWrapper<MallGoodsCategory>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        MallGoodsCategoryDetailVo vo = new MallGoodsCategoryDetailVo();

        vo.setHasParent(model.getPid() != null && model.getPid().intValue() != 0 ? 1: 0);

        BeanUtils.copyProperties(model, vo);

        vo.setImage(UrlUtils.toAdminAbsoluteUrl(model.getImage()));
        return vo;
    }

    @Override
    public List<String> getCateNamesByGoodsId(Integer goodsId) {
        List<String> cateNames = new LinkedList<>();
        List<Integer> cateIds = iMallGoodsCategoryIndexService.getCateIdsByGoodsId(goodsId);
        if (cateIds.size() == 0) {
            return cateNames;
        }
        List<MallGoodsCategory> goodsCategoryIndexList = mallGoodsCategoryMapper.selectList(new QueryWrapper<MallGoodsCategory>().in("id", cateIds).eq("delete_time", 0));
        for (MallGoodsCategory category : goodsCategoryIndexList) {
            cateNames.add(category.getName());
        }
        return cateNames;
    }
}
