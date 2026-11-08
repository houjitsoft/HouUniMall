package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplCreateValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplSearchValidate;
import com.houjiang.merchant.validate.mall.MallExpressTplUpdateValidate;
import com.houjiang.merchant.vo.mall.MallExpressTplDetailVo;
import com.houjiang.merchant.vo.mall.MallExpressTplGoodsVo;
import com.houjiang.merchant.vo.mall.MallExpressTplListedVo;
import com.houjiang.merchant.vo.mall.MallShopDetailVo;

import java.util.List;

/**
 *  运费模板接口类
 */
public interface IMallExpressTplService {

    /**
     * 返回单位的配送列表
     * @param shopId
     * @return
     */
    List<MallExpressTplGoodsVo> getListByShopId(Integer shopId);


    /**
     * 运费模板列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallExpressTplListedVo>
     */
    PageResult<MallExpressTplListedVo> list(PageValidate pageValidate, MallExpressTplSearchValidate searchValidate);

    /**
     * 运费模板详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallExpressTplDetailVo
     */
    MallExpressTplDetailVo detail(Integer id);

    /**
     * 运费模板新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(MallExpressTplCreateValidate createValidate);

    /**
     * 运费模板编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallExpressTplUpdateValidate updateValidate);

    /**
     * 运费模板删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);
}
