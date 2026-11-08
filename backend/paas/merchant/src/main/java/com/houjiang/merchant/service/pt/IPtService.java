package com.houjiang.merchant.service.pt;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.pt.Pt;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.pt.PtCreateValidate;
import com.houjiang.merchant.validate.pt.PtSearchValidate;
import com.houjiang.merchant.validate.pt.PtUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDetailVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.pt.GoodsPtVo;
import com.houjiang.merchant.vo.pt.PtDetailVo;
import com.houjiang.merchant.vo.pt.PtListedVo;
import com.houjiang.merchant.vo.pt.PtOpenDetailVo;

/**
 *  拼团活动接口类
 */
public interface IPtService {

    /**
     * 返回商品拼团信息
     * @param goodsId
     * @return
     */
    GoodsPtVo getGoodsPt(Integer goodsId);

    /**
     * @notes 检测商品是否存在拼团
     * @param $goods_id
     * @param $id
     * @return bool
     * @author Amos
     */
    Boolean goodsHas(Integer goodsId, Integer id);

    /**
     * 返回拼团信息
     * @param goodsId
     * @return
     */
    PtOpenDetailVo getPtOpen(Integer orderId);

    /**
     * 检测开团是否成功
     * @param id
     * @return
     */
    Boolean checkIsSuccess(Integer id);

    /**
     * @notes 获取拼团列表
     * @return array
     * @author Amos
     */
    PageResult<PtListedVo> list(PageValidate pageValidate, PtSearchValidate searchValidate);

    /**
     * 拼团详情
     * @param id
     * @return
     */
    PtDetailVo detail(Integer id);

    /**
     * TODO 补充方法说明
     * @param id TODO
     * @return TODO
     */
    Pt getById(Integer id);
    /**
     * @notes 拼团
     * @param array $params
     * @return bool
     * @author Amos
     */
    void del(Integer id);

    /**
     * @notes 结束活动
     * @author Amos
     */
    void close(Integer id);

    /**
     * @notes 提交审核
     * @author Amos
     */
    void submit(Integer id);

    /**
     * @notes 开启活动
     * @return bool
     * @author Amos
     */
    void open(Integer id);

    /**
     * 新建拼团
     * @param createValidate
     */
    void add(PtCreateValidate createValidate);

    /**
     * 编辑拼团
     * @param updateValidate
     */
    void edit(PtUpdateValidate updateValidate);
}
