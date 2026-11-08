package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseExpressEntity;
import com.houjiang.base.feign.BusinessClient;
import com.houjiang.base.mapper.BaseExpressMapper;
import com.houjiang.base.service.BaseExpressService;
import com.houjiang.base.vo.ManageExpressQueryVO;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.KdniaoExpressVO;
import com.houjiang.commons.vo.shop.ExpressQueryByShopDpexpressVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class BaseExpressServiceImpl extends ServiceImpl<BaseExpressMapper, BaseExpressEntity>
    implements BaseExpressService{

    @Autowired
    private BusinessClient businessClient;;

    /**
     * 无Token查询快递公司列表。
     * 该方法用于在不需要用户认证的情况下，查询所有处于激活状态的快递公司信息。
     *
     * @return ResponseResult 包含查询结果的响应对象。
     */
    @Override
    public ResponseResult queryNoToken() {
        // 根据快递状态查询快递公司列表
        //查询快递公司 列表
        List<BaseExpressEntity> expressList = this.baseMapper.selectList(new LambdaQueryWrapper<BaseExpressEntity>()
                .eq(BaseExpressEntity::getExpressStatus, CommonType.commonStatus.YES.getCode()));
        return ResponseResult.success(expressList);
    }

    /**
     * 带Token查询快递公司列表。
     * 该方法用于在用户认证后，根据用户权限查询特定状态的快递公司信息。
     *
     * @param map 查询参数，包括token和expressStatus。
     * @return ResponseResult 包含查询结果的响应对象。
     */
    @Override
    public ResponseResult queryExpressList(Map<String, Object> map) {
        // 检查token是否存在
        if (null != map && null != map.get("token")) {
            // 校验token
            //校验token
            String token = map.get("token").toString();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null != userInfo) {
                LambdaQueryWrapper<BaseExpressEntity> query = new LambdaQueryWrapper<>();
                // 根据快递状态查询，如果状态参数存在
                if (map.get("expressStatus") != null) {
                    query.eq(BaseExpressEntity::getExpressStatus, map.get("expressStatus"));
                }
                //查询快递公司 列表
                List<BaseExpressEntity> expressList = this.baseMapper.selectList(query);
                return ResponseResult.success(expressList);
            }
            // token校验失败
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 参数校验失败
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());
    }

    /**
     * 根据ID查询单个快递公司信息。
     * 该方法用于查询特定ID的快递公司详细信息。
     *
     * @param expressId 快递公司ID。
     * @return ResponseResult 包含查询结果的响应对象。
     */
    @Override
    public ResponseResult getBaseExpressInfoById(Integer expressId) {
        // 根据ID查询快递公司信息
        BaseExpressEntity expressEntity = this.baseMapper.selectById(expressId);
        return ResponseResult.success(expressEntity);
    }

    /**
     * 无需token验证的快递查询方法。
     * 通过此方法查询快递信息，不需要提供token进行身份验证。
     *
     * @return 返回查询到的快递信息列表。
     */
    @Override
    public List<ManageExpressQueryVO> queryNoTokenTwo() {
        // 通过baseMapper查询无需token验证的快递信息
        List<ManageExpressQueryVO> manageExpressQueryVOS =  this.baseMapper.queryNoTokenTwo();
        return manageExpressQueryVOS;
    }

    /**
     * 添加快递公司信息。
     * 此方法用于添加新的快递公司信息到系统中，需要提供有效的token进行身份验证。
     *
     * @param baseExpressReq 包含新增快递公司信息的请求对象。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult addExpress(BaseExpressDTO baseExpressReq) {
        // 验证token的有效性
        if (null != baseExpressReq && null != baseExpressReq.getToken()) {
            // 根据token获取用户信息
            // 校验token
            String token = baseExpressReq.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null != userInfo) {
                // 检查快递公司名称或首字母是否已存在
                // 检验快递公司或者快递公司首字符是否重复
                Integer express = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<BaseExpressEntity>()
                        .eq(BaseExpressEntity::getExpressName, baseExpressReq.getExpressName())
                        .or()
                        .eq(BaseExpressEntity::getExpressPinyin, baseExpressReq.getExpressPinyin())));
                if (express > 0) {
                    // 若已存在，返回失败结果
                    return ResponseResult.fail(BizCodeEnume.FIRST_CHARACTER_NOT_REPEAT);
                }
                // 添加快递公司信息
                // 新增快递公司
                this.baseMapper.addExpress(baseExpressReq);
                // 添加成功，返回成功结果
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
            // token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 参数无效，返回失败结果
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 更新快递公司信息。
     * 此方法用于更新已存在的快递公司信息，需要提供有效的token进行身份验证。
     *
     * @param baseExpressReq 包含更新后的快递公司信息的请求对象。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult updateExpress(BaseExpressDTO baseExpressReq) {
        // 验证token的有效性
        if (null != baseExpressReq && null != baseExpressReq.getToken()) {
            // 根据token获取用户信息
            // 校验token
            String token = baseExpressReq.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null != userInfo) {
                // 检查除当前记录外，是否有同名或同首字母的快递公司存在
                // 检验快递公司或者快递公司首字符是否重复
                Integer count = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<BaseExpressEntity>()
                        .ne(BaseExpressEntity::getExpressId, baseExpressReq.getExpressId())
                        .and(wp -> wp.eq(BaseExpressEntity::getExpressName, baseExpressReq.getExpressName())
                                .or()
                                .eq(BaseExpressEntity::getExpressPinyin, baseExpressReq.getExpressPinyin()))
                ));
                if (count > 0) {
                    // 若存在，返回失败结果
                    return ResponseResult.fail(BizCodeEnume.FIRST_CHARACTER_NOT_REPEAT);
                }
                // 更新快递公司信息
                // 修改快递公司
                this.baseMapper.updateExpress(baseExpressReq);
                // 更新成功，返回成功结果
                return ResponseResult.success();
            }
            // token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 参数无效，返回失败结果
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 删除快递公司信息
     * @param baseExpressReq 包含快递公司ID和token的信息
     * @return 删除操作的结果
     */
    @Override
    public ResponseResult deleteExpress(BaseExpressDTO baseExpressReq) {
        // 校验token和请求参数是否为空
        if (null != baseExpressReq && null != baseExpressReq.getToken()) {
            // 校验token并获取用户信息
            //校验token
            String token = baseExpressReq.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null != userInfo) {
                // 删除快递公司信息
                //删除快递公司
                this.baseMapper.deleteById(baseExpressReq.getExpressId());
                return ResponseResult.success();
            }
            // token非法
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 请求参数异常
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
    }

    /**
     * 根据店铺ID查询快递公司信息
     * @param paraMap 包含店铺ID的参数映射
     * @return 查询结果列表
     */
    @Override
    public ResponseResult queryByShopDpexpress(Map<String, Object> paraMap) {
        // 检查店铺ID是否为空
        if (paraMap.get("shopId") == null || paraMap.get("shopId").equals("")) {
            return ResponseResult.fail(2,"请求参数不能为空");
        }
        Integer shopId = (Integer) paraMap.get("shopId");
        List<ExpressQueryByShopDpexpressVO> list = businessClient.newQueryByShopDpexpress(shopId);
        for(ExpressQueryByShopDpexpressVO se:list) {
            int num=se.getExpressId();
            KdniaoExpressVO querykdniaoexpress = this.baseMapper.newQuerykdniaoexpress(num);
            if(null != querykdniaoexpress) {
                se.setExpressName(querykdniaoexpress.getExpressName());
                se.setSendSite(querykdniaoexpress.getExpressPinyin());
            }
        }
        return ResponseResult.success(list);
    }

    /**
     * 根据订单配送ID查询快递公司名称
     * @param orderShippingExpressId 订单配送ID
     * @return 快递公司名称
     */
    @Override
    public String queryBaseExpressById(Integer orderShippingExpressId) {
        return this.baseMapper.selectById(orderShippingExpressId).getExpressName();
    }
}




