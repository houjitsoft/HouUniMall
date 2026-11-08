package com.houjiang.merchant.service.pt;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.pt.PtOpen;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.pt.PtOpenSearchValidate;
import com.houjiang.merchant.vo.pt.PtOpenJoinListedVo;
import com.houjiang.merchant.vo.pt.PtOpenListedVo;
import com.houjiang.merchant.vo.pt.PtOpenVo;

import java.util.List;

/**
 * 拼团活动 开团-参团接口类
 */
public interface IPtOpenJoinService {

    /**
     * @notes 获取拼团列表详情
     * @param $params
     * @return array
     * @author Amos
     */
    List<PtOpenJoinListedVo> getJoinList(Integer ptOpenId);
}
