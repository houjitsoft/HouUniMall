package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopDecorate;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopDecorateMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopDecorateService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopDecorateCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopDecorateSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopDecorateUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDecorateDetailVo;
import com.houjiang.merchant.vo.mall.MallShopDecorateListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家银行卡实现类
 * @author Amos
 */
@Service
public class MallShopDecorateServiceImpl implements IMallShopDecorateService {

    @Resource
    MallShopDecorateMapper mallShopDecorateMapper;

    /**
     * 商家银行卡列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopBankListedVo>
     */
    @Override
    public PageResult<MallShopDecorateListedVo> list(PageValidate pageValidate, MallShopDecorateSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallShopDecorate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0);
        if (StringUtils.isNotNull(searchValidate.getShopId())) {
            queryWrapper.eq("shop_id", searchValidate.getShopId());
        }
        if (StringUtils.isNotEmpty(searchValidate.getName())) {
            queryWrapper.like("name", searchValidate.getName());
        }
        if (StringUtils.isNotNull(searchValidate.getType())) {
            queryWrapper.eq("type", searchValidate.getType());
        }
        queryWrapper.orderByDesc("is_home", "id");
        IPage<MallShopDecorate> iPage = mallShopDecorateMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallShopDecorateListedVo> list = new LinkedList<>();
        for(MallShopDecorate item : iPage.getRecords()) {
            MallShopDecorateListedVo vo = new MallShopDecorateListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setContent(StringUtils.isNotEmpty(item.getContent()) ? JSONArray.parseArray(item.getContent()) : new JSONArray());
            vo.setCommon(StringUtils.isNotEmpty(item.getCommon()) ? JSONArray.parseArray(item.getCommon()) : new JSONArray());
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 商家银行卡详情
     *
     * @author Amos
     * @param id 主键参数
     * @return MallShopBank
     */
    @Override
    public MallShopDecorateDetailVo detail(Integer id) {
        MallShopDecorate model = this.getById(id);

        Assert.notNull(model, "数据不存在");

        MallShopDecorateDetailVo vo = new MallShopDecorateDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setContent(StringUtils.isEmpty(model.getContent()) ? new JSONArray() : JSONArray.parseArray(model.getContent().replace("uploads/", GlobalConfig.shopPublicPrefix + "/")));
        //vo.setContent(StringUtils.isNotEmpty(model.getContent()) ? JSONArray.parseArray(model.getContent()) : new JSONArray());
        vo.setCommon(StringUtils.isNotEmpty(model.getCommon()) ? JSONArray.parseArray(model.getCommon()) : new JSONArray());
        return vo;
    }

    /**
     * 商家银行卡新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    @Transactional
    public Integer add(MallShopDecorateCreateValidate createValidate) {
        MallShopDecorate model = new MallShopDecorate();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setDeleteTime(0L);
        model.setIsHome(0);
        model.setType(createValidate.getType());
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        //model.setContent(createValidate.getContent());
        if (StringUtils.isEmpty(createValidate.getContent()) == true) {
            model.setContent("");
        } else {
            String data = createValidate.getContent().replace(UrlUtils.domain() + GlobalConfig.shopPublicPrefix, "uploads");
            model.setContent(data.replace(GlobalConfig.shopPublicPrefix, "uploads"));
        }
        model.setCommon(StringUtils.isEmpty(createValidate.getCommon()) ? "" : createValidate.getCommon());
        mallShopDecorateMapper.insert(model);
        return model.getId();
    }

    /**
     * 商家银行卡编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    @Transactional
    public void edit(MallShopDecorateUpdateValidate updateValidate) {
        MallShopDecorate model = this.getById(updateValidate.getId());

        Assert.notNull(model, "数据不存在!");
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setType(updateValidate.getType());
        model.setName(updateValidate.getName());
        //model.setContent(updateValidate.getContent());
        if (StringUtils.isEmpty(updateValidate.getContent()) == true) {
            model.setContent("");
        } else {
            String data = updateValidate.getContent().replace(UrlUtils.domain() + GlobalConfig.shopPublicPrefix, "uploads");
            model.setContent(data.replace(GlobalConfig.shopPublicPrefix, "uploads"));
        }
        model.setCommon(StringUtils.isEmpty(updateValidate.getCommon()) ? "" : updateValidate.getCommon());
        mallShopDecorateMapper.updateById(model);
    }

    /**
     * 商家银行卡删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        MallShopDecorate model = this.getById(id);
        Assert.notNull(model, "数据不存在!");
        model.setDeleteTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallShopDecorateMapper.updateById(model);
    }

    @Override
    public MallShopDecorate getById(Integer id) {
        MallShopDecorate model = mallShopDecorateMapper.selectOne(new QueryWrapper<MallShopDecorate>().eq("id", id).eq("delete_time", 0));
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的店铺装修");
        }
        return model;
    }

    @Override
    public void setHome(Integer id) {
        MallShopDecorate model = this.getById(id);
        Assert.notNull(model, "数据不存在!");

        QueryWrapper queryWrapper = new QueryWrapper<MallShopDecorate>();
        queryWrapper.eq("shop_id", MerchantThreadLocal.getShopId());
        queryWrapper.eq("is_home", 1);
        queryWrapper.eq("delete_time", 0);
        MallShopDecorate mallShopDecorate = new MallShopDecorate();
        mallShopDecorate.setIsHome(YesNoEnum.NO.getCode());
        mallShopDecorateMapper.update(mallShopDecorate, queryWrapper);

        model.setIsHome(YesNoEnum.YES.getCode());
        mallShopDecorateMapper.update(model, new QueryWrapper<MallShopDecorate>().eq("id", id));

    }
}
