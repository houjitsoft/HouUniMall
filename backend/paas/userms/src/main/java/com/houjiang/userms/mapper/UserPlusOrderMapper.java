package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.userms.entity.UserPlusOrderEntity;
import com.houjiang.userms.vo.PlusOrderEntityExpVO;
import com.houjiang.userms.vo.PlusOrderEntityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName UserPlusOrderMapper
 * @Description plus会员充值订单表
 * @Author Amos
 * @Date 2023-06-29 09:28:31
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserPlusOrderMapper extends BaseMapper<UserPlusOrderEntity> {

    /**
     * plus 会员充值订单表
     * @param map
     * @return
     */
    Integer insertplusorder(@Param("param") Map<String,Object> map);

    /**
     * 查询会员plus充值记录
     * @param page
     * @param pageUserTokenBeanDTO
     * @return
     */
    IPage<PlusOrderEntityVO> selectplusorder(IPage<PlusOrderEntityVO> page, @Param("param") PageUserTokenBeanDTO pageUserTokenBeanDTO);
    /**
     * 查询会员充值列表
     * @param page
     * @param param
     * @return
     */
    IPage<PlusOrderEntityExpVO> selectrechargelistexp(IPage<PlusOrderEntityExpVO> page , @Param("param")Map<String, Object> param);
    /**
     *查询会员充值列表
/**
     * @param page
     * @param param
     * @return
     */
    IPage<Map<String,Object>> selectplusorderMap(IPage<Map<String,Object>> page , @Param("param") Map<String,Object> param);

    /**
     * 查询会员充值金额汇总
     * @param map
     * @return
     */
    Map<String,Object> selectPriceTotal(@Param("param") Map<String,Object> map);

    /**
     * 查询plus 会员订单是否支付成功
     * @param userId
     * @param plusOrder
     * @return
     */
    Integer queryplusorder(@Param("userId")String userId,@Param("plusOrder")String plusOrder);

    /**
     * 根据用户ID查询用户的Plus会员状态。
     *
     * @param userId 用户的唯一标识，用于查询用户的Plus会员状态。
     * @return 返回用户的Plus会员状态。具体状态码的含义需要参考相关文档或枚举定义。
     */
    Integer queryStatusByPlus(@Param("userId") String userId);

}
