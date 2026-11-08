package com.houjiang.merchant.service.ms;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.ms.Ms;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsCreateValidate;
import com.houjiang.merchant.validate.ms.MsOpenValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.validate.ms.MsUpdateValidate;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.ms.MsDetailVo;
import com.houjiang.merchant.vo.ms.MsListedVo;

/**
 *  秒杀活动接口类
 */
public interface IMsService {
    /**
     * 返回商品拼团信息
     * @param goodsId
     * @return
     */
    GoodsMsVo getGoodsMs(Integer goodsId);

    /**
     * @notes 检测商品是否存在秒杀
     * @param $goods_id
     * @param $id
     * @return bool
     * @author Amos
     */
    Boolean goodsHas(Integer goodsId, Integer id);

    /**
     * 秒杀列表
     * Class MsLists
     */
    PageResult<MsListedVo> list(PageValidate pageValidate, MsSearchValidate msSearchValidate);

    /**
     * 秒杀详情
     * @param id
     * @return
     */
    MsDetailVo detail(Integer id);

    /**
     * 根据id返回秒杀信息
     * @param id
     * @return
     */
    Ms getById(Integer id);

    /**
     * @notes 删除秒杀
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
     * 新建秒杀
     * @param msCreateValidate
     */
    void add(MsCreateValidate msCreateValidate);

    /**
     * @notes 编辑秒杀
     * @author Amos
     */
    void edit(MsUpdateValidate msUpdateValidate);

    /**
     * 开启活动
     * @notes 审核秒杀
     * @author Amos
     */
    void open(MsOpenValidate validate);
}
