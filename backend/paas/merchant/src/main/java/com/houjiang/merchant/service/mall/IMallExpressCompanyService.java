package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallExpressCompany;
import com.houjiang.merchant.vo.mall.MallExpressCompanyListedVo;
import com.houjiang.merchant.vo.mall.MallExpressCompanySingleDetailVo;
import com.houjiang.merchant.vo.mall.MallExpressTplGoodsVo;

import java.util.List;

/**
 *  快递公司模型接口类
 * @author Amos
 */
public interface IMallExpressCompanyService {

    /**
     * 获取配送公司
     * @param id
     * @return
     */
    MallExpressCompanySingleDetailVo getSingleDetail(Integer id);

    /**
     * 根据ID返回信息
     * @param id
     * @return
     */
    MallExpressCompany getById(Integer id);

    /**
     * 快递公司模型列表
     * @return
     */
    List<MallExpressCompanyListedVo> getLists();
}
