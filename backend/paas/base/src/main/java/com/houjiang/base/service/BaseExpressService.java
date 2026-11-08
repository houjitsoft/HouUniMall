package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseExpressEntity;
import com.houjiang.base.vo.ManageExpressQueryVO;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.util.ResponseResult;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface BaseExpressService extends IService<BaseExpressEntity> {

    /**
     * 查询快递公司
     *
     * @return
     */
    ResponseResult queryNoToken();

    /**
     * 查询快递公司列表
     *
     * @param map
     * @return
     */
    ResponseResult queryExpressList(Map<String, Object> map);

    /**
     * 根据id查询快递信息
     *
     * @param expressId
     * @return
     */
    ResponseResult getBaseExpressInfoById(Integer expressId);

    /**
     * 查询快递公司 feign
     *
     * @return
     */
    List<ManageExpressQueryVO> queryNoTokenTwo();

    /**
     * 新增快递公司
     *
     * @param baseExpressReq
     * @return
     */
    ResponseResult addExpress(BaseExpressDTO baseExpressReq);

    /**
     * 修改快递公司
     *
     * @param baseExpressReq
     * @return
     */
    ResponseResult updateExpress(BaseExpressDTO baseExpressReq);

    /**
     * 删除快递公司
     *
     * @param baseExpressReq
     * @return
     */
    ResponseResult deleteExpress(BaseExpressDTO baseExpressReq);

    /**
     * 查询快递设置
     * @param paraMap
     * @return
     */
    ResponseResult queryByShopDpexpress(Map<String, Object> paraMap);

    /**
     * 根据物流公司Id查找名称
     * @param orderShippingExpressId
     * @return
     */
    String queryBaseExpressById(Integer orderShippingExpressId);
}
