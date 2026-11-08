package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.vo.mall.MallExpressTplGoodsVo;
import com.houjiang.merchant.vo.mall.MallOrderLogListedVo;

import java.util.List;

/**
 *  订单日志接口类
 * @author Amos
 */
public interface IMallOrderLogService {

    /**
     * 返回单位的配送列表
     * @param shopId
     * @return
     */
    List<MallOrderLogListedVo> getListByOId(Integer orderId);

    /**
     * 返回操作员名称
     * @param operatorType
     * @param operatorId
     * @return
     */
    String getOperatorAttr(Integer operatorType, Integer operatorId);
}
