package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAccountLog;
import com.houjiang.merchant.common.enums.ShopAccountEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopAccountLogMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallShopAccountLogService;
import com.houjiang.merchant.service.mall.IMallShopService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopAccountLogSearchValidate;
import com.houjiang.merchant.vo.mall.MallShopAccountLogListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家流水实现类
 * @author Amos
 */
@Service
public class MallShopAccountLogServiceImpl implements IMallShopAccountLogService {

    @Resource
    IMallShopService iMallShopService;
    @Resource
    MallShopAccountLogMapper mallShopAccountLogMapper;

    /**
     * 商家流水列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopAccountLogListedVo>
     */
    @Override
    public PageResult<MallShopAccountLogListedVo> list(PageValidate pageValidate, MallShopAccountLogSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallShopAccountLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                queryWrapper.ge("create_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                queryWrapper.le("create_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }

        if (StringUtils.isNotNull(searchValidate.getChange_type())) {
            queryWrapper.eq("change_type", searchValidate.getChange_type());
        } else {

            if (searchValidate.getIs_gold() != null && searchValidate.getIs_gold().intValue() == 1) {
                queryWrapper.in("change_type", 2020, 2010, 2000);
            } else {
                queryWrapper.in("change_type", 1000, 1020, 1010, 1021);
            }
        }

        if (StringUtils.isNotNull(searchValidate.getShopId())) {
            queryWrapper.eq("shop_id", searchValidate.getShopId());
        } else if (searchValidate.getShopIds() != null) {
            if (searchValidate.getShopIds().isEmpty()) {
                queryWrapper.apply("1 = 0");
            } else {
                queryWrapper.in("shop_id", searchValidate.getShopIds());
            }
        }
        IPage<MallShopAccountLog> iPage = mallShopAccountLogMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallShopAccountLogListedVo> list = new LinkedList<>();
        for(MallShopAccountLog item : iPage.getRecords()) {
            MallShopAccountLogListedVo vo = new MallShopAccountLogListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setShopName(iMallShopService.getNameById(item.getShopId()));
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setChangeTypeText(ShopAccountEnum.getMsgByCode(item.getChangeType()));
            vo.setChangeObjectText(ShopAccountEnum.getMsgByCode(item.getChangeObject()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    @Override
    public void createAccountLog(MallShopAccountLog log) {
        MallShop shop = iMallShopService.getById(log.getShopId());
        if (StringUtils.isNull(shop)) {
            throw new OperateException("找不到店铺信息");
        }
        BigDecimal leftAmount = new BigDecimal(0);
        if (log.getChangeObject().equals(ShopAccountEnum.CHANGE_OBJECT_SHOP_MONEY.getCode())) {
            leftAmount = shop.getMoney();
        } else if (log.getChangeObject().equals(ShopAccountEnum.CHANGE_OBJECT_SHOP_GATHER_MONEY.getCode())) {
            leftAmount = new BigDecimal(shop.getGatherMoney());
        }
        log.setSn(mallShopAccountLogMapper.randMakeOrderSn("sn"));
        log.setCreateTime(System.currentTimeMillis() / 1000);
        log.setUpdateTime(System.currentTimeMillis() / 1000);
        log.setLeftAmount(leftAmount);
        mallShopAccountLogMapper.insert(log);
    }
}
