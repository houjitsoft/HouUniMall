package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopBank;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopBankMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopBankService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopBankCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopBankSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopBankUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopBankDetailVo;
import com.houjiang.merchant.vo.mall.MallShopBankListedVo;
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
public class MallShopBankServiceImpl implements IMallShopBankService {
        
    @Resource
    MallShopBankMapper mallShopBankMapper;

    /**
     * 商家银行卡列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopBankListedVo>
     */
    @Override
    public PageResult<MallShopBankListedVo> list(PageValidate pageValidate, MallShopBankSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallShopBank> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id").eq("delete_time", 0);

        mallShopBankMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:shopId@shop_id:int",
            "like:name:str",
            "=:branch:str",
            "=:isDefault@is_default:int",
            "like:username:str",
            "=:cardNumber@card_number:str",
        });

        IPage<MallShopBank> iPage = mallShopBankMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallShopBankListedVo> list = new LinkedList<>();
        for(MallShopBank item : iPage.getRecords()) {
            MallShopBankListedVo vo = new MallShopBankListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
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
    public MallShopBankDetailVo detail(Integer id) {
        MallShopBank model = mallShopBankMapper.selectOne(
                new QueryWrapper<MallShopBank>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的提现信息");
        }

        MallShopBankDetailVo vo = new MallShopBankDetailVo();
        BeanUtils.copyProperties(model, vo);
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
    public void add(MallShopBankCreateValidate createValidate) {
        MallShopBank model = new MallShopBank();
        if (createValidate.getIsDefault().equals(YesNoEnum.YES.getCode())) {
            MallShopBank def = new MallShopBank();
            def.setIsDefault(YesNoEnum.NO.getCode());
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("shop_id", createValidate.getShopId());
            mallShopBankMapper.update(def, wrapper);
        }
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        model.setBranch(createValidate.getBranch());
        model.setIsDefault(createValidate.getIsDefault());
        model.setUsername(createValidate.getUsername());
        model.setCardNumber(createValidate.getCardNumber());
        mallShopBankMapper.insert(model);
    }

    /**
     * 商家银行卡编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    @Transactional
    public void edit(MallShopBankUpdateValidate updateValidate) {
        MallShopBank model = mallShopBankMapper.selectOne(
                new QueryWrapper<MallShopBank>()
                    .eq("id",  updateValidate.getId()).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的提现信息");
        }

        if (updateValidate.getIsDefault().equals(YesNoEnum.YES.getCode())) {
            MallShopBank def = new MallShopBank();
            def.setIsDefault(YesNoEnum.NO.getCode());
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("shop_id", model.getShopId());
            mallShopBankMapper.update(def, wrapper);
        }

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setBranch(updateValidate.getBranch());
        model.setIsDefault(updateValidate.getIsDefault());
        model.setUsername(updateValidate.getUsername());
        model.setCardNumber(updateValidate.getCardNumber());
        mallShopBankMapper.updateById(model);
    }

    /**
     * 商家银行卡删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        MallShopBank model = mallShopBankMapper.selectOne(
                new QueryWrapper<MallShopBank>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的提现信息");
        }
        model.setDeleteTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallShopBankMapper.updateById(model);
        //mallShopBankMapper.delete(new QueryWrapper<MallShopBank>().eq("id", id));
    }

    @Override
    public MallShopBank getById(Integer id) {
        return mallShopBankMapper.selectOne(new QueryWrapper<MallShopBank>().eq("id", id).eq("delete_time", 0));
    }
}
