package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.gather.MallGatherCreateValidate;
import com.houjiang.merchant.validate.gather.MallGatherSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopJobSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobUpdateValidate;
import com.houjiang.merchant.vo.gather.MallGatherCreateVo;
import com.houjiang.merchant.vo.gather.MallGatherDetailVo;
import com.houjiang.merchant.vo.gather.MallGatherListedVo;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobListedVo;

import java.util.List;

/**
 * 采集控制接口类
 * @author Amos
 */
public interface IMallGatherService {

    /**
     * 商品采集列表
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     */
    PageResult<MallGatherListedVo> list(PageValidate pageValidate, MallGatherSearchValidate searchValidate);

    /**
     * 商品采集详情
     * @author Amos
     * @param id 主键ID
     * @return MallShopJobDetailVo
     */
    MallGatherDetailVo detail(Integer id);

    /**
     * 商品采集新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    MallGatherCreateVo add(MallGatherCreateValidate createValidate);

    /**
     * 商品采集删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * @notes 执行
     * @author Amos
     */
    void deal(Integer id);
}
