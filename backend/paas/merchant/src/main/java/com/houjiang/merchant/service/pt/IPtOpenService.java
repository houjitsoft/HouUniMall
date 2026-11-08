package com.houjiang.merchant.service.pt;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.pt.PtOpen;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.pt.PtOpenSearchValidate;
import com.houjiang.merchant.vo.pt.GoodsPtVo;
import com.houjiang.merchant.vo.pt.PtOpenDetailVo;
import com.houjiang.merchant.vo.pt.PtOpenListedVo;
import com.houjiang.merchant.vo.pt.PtOpenVo;

/**
 * 拼团活动接口类
 */
public interface IPtOpenService {

    /**
     * @notes 支付订单 检测拼团成功
     * @param openId
     * @return bool
     * @author Amos
     */
    Boolean orderPayCheckStatus(Integer id);

    /**
     * 根据id返回拼团信息
     * @param id
     * @return
     */
    PtOpen getById(Integer id);

    /**
     * @notes 获取拼团列表列表
     * @return array
     * @author Amos
     */
    PageResult<PtOpenListedVo> list(PageValidate pageValidate, PtOpenSearchValidate msSearchValidate);

    /**
     * @notes 获取拼团列表详情
     * @param $params
     * @return array
     * @author Amos
     */
    PtOpenVo detail(Integer id);

    /**
     * 关门拼团
     * @param id
     */
    void close(Integer id);


    /**
     * @notes 取消订单 变为拼团失败
     */
    void orderCancelDeal(Integer ptOpenId, Integer ptOpenJoinId);
}
