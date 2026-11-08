package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGoodsCategoryIndex;
import com.houjiang.merchant.common.mapper.mall.MallGoodsCategoryIndexMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallGoodsCategoryIndexService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCategoryIndexSearchValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryIndexDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryIndexListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 商品分类关联实现类
 * @author Amos
 */
@Service
public class MallGoodsCategoryIndexServiceImpl implements IMallGoodsCategoryIndexService {
        
    @Resource
    MallGoodsCategoryIndexMapper mallGoodsCategoryIndexMapper;

    /**
     * 商品分类关联列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsCategoryIndexListedVo>
     */
    @Override
    public PageResult<MallGoodsCategoryIndexListedVo> list(PageValidate pageValidate, MallGoodsCategoryIndexSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallGoodsCategoryIndex> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        mallGoodsCategoryIndexMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:goodsId@goods_id:int",
            "=:categoryId@category_id:int",
        });

        IPage<MallGoodsCategoryIndex> iPage = mallGoodsCategoryIndexMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallGoodsCategoryIndexListedVo> list = new LinkedList<>();
        for(MallGoodsCategoryIndex item : iPage.getRecords()) {
            MallGoodsCategoryIndexListedVo vo = new MallGoodsCategoryIndexListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 商品分类关联详情
     *
     * @author Amos
     * @param id 主键参数
     * @return MallGoodsCategoryIndex
     */
    @Override
    public MallGoodsCategoryIndexDetailVo detail(Integer id) {
        MallGoodsCategoryIndex model = mallGoodsCategoryIndexMapper.selectOne(
                new QueryWrapper<MallGoodsCategoryIndex>()
                    .eq("id", id)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        MallGoodsCategoryIndexDetailVo vo = new MallGoodsCategoryIndexDetailVo();
        BeanUtils.copyProperties(model, vo);
        return vo;
    }

    @Override
    public List<Integer> getCateIdsByGoodsId(Integer goodsId) {
        List<MallGoodsCategoryIndex> goodsCategoryIndexList = mallGoodsCategoryIndexMapper.selectList(new QueryWrapper<MallGoodsCategoryIndex>().eq("goods_id", goodsId).eq("delete_time", 0));
        List<Integer> goodsCategory = new LinkedList<>();
        for (MallGoodsCategoryIndex goodsCategoryIndex : goodsCategoryIndexList) {
            goodsCategory.add(goodsCategoryIndex.getCategoryId());
        }
        return goodsCategory;
    }

    @Override
    public List<Integer> getGoodsIdsByCid(Integer categoryId) {
        List<MallGoodsCategoryIndex> goodsCategoryIndexList = mallGoodsCategoryIndexMapper.selectList(new QueryWrapper<MallGoodsCategoryIndex>().eq("category_id", categoryId).eq("delete_time", 0).select("goods_id"));
        List<Integer> ids = new LinkedList<>();
        for (MallGoodsCategoryIndex goodsCategoryIndex : goodsCategoryIndexList) {
            ids.add(goodsCategoryIndex.getGoodsId());
        }
        return ids;
    }
}
