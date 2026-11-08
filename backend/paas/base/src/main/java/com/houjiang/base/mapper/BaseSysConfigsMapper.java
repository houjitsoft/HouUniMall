package com.houjiang.base.mapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.WebSystemConfigQueryDTO;
import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.base.vo.SystemConfigHallSlideVO;
import com.houjiang.commons.vo.base.WabSystemConfigHallVO;
import com.houjiang.base.vo.index.WebSystemConfigQueryVO;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
* @Entity com.houjiang.base.entity.MerchantSysConfigs
/**
* @author Amos
*/
@Mapper
public interface BaseSysConfigsMapper extends BaseMapper<BaseSysConfigsEntity> {

    /**
     * 根据configKey 查询
     * @param configKey
     * @return
     */
    String selectConfigValue(@Param("configKey") String configKey);

    /**
     * 查询轮播配置
     * @param param
     * @return
     */
    List<WebSystemConfigQueryVO> selectByParam(@Param("param")WebSystemConfigQueryDTO param);

    /**
     * 热搜词
     * @param search
     * @return
     */
    List<WabSystemConfigHallVO> selectByConfigType(@Param("configType")String search);

    /**
     * 根据配置类型搜索系统配置大厅信息
     *
     * @param search 配置类型字符串，用于查询特定类型的系统配置
     * @return 返回一个列表，包含所有匹配的系统配置大厅信息对象
     */
    List<WabSystemConfigHallVO> selectByConfigTypesearch(@Param("configType")String search);

    /**
     * 获取上传图片配置地址
     * @param configKey
     * @return
     */
    String getImageConfigUrl(@Param("configKey")String configKey);

    /**
     * 根据config_type 查询
     * @param integral_image
     * @return
     */
    List<BaseSysConfigsEntity> queryIntegralIndex(String integral_image);

    /**
     * 查询商城客服及邮箱
     *
     * @return
     */
    List<SystemConfigHallSlideVO> selectPhoneAndMail();


    /**
     * 根据配置类型键查询配置信息
     *
     * @param configKey 配置类型键
     * @return 配置信息对象
     */
    BaseSysConfigsVO selectByConfigTypeFeign(String configKey);

    /**
     * 修改设置
     *
     * @param systemConfigAddParam
     */
    void updateSetting(@Param("param") List<BaseSysConfigsEntity> systemConfigAddParam);

    /**
     * 查询配置
     * @param param
     * @return
     */
    String selectBaseWebConfig(@Param("param") String param);


    /**
     * 根据配置键查询配置信息
     * @param baseSysConfigsVO
     */
    void insertByPrimaryConfigKey(@Param("param") BaseSysConfigsVO baseSysConfigsVO);


    /**
     * 根据配置键查询配置信息
     * @param configKey
     * @return
     */
    int insertConfigKey(@Param("list") List<String> configKey);


    /**
     *  根据配置键查询配置信息
     * @param configKey
     * @return
     */
    List<String> queryConfigKey(@Param("param") List<String> configKey);
    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    List<WabSystemConfigHallVO> querySystemConfigHall(@Param("configType")String configKey);


    /**
     * 根据key查询配置
     * @param configKey
     * @return
     */
    default BaseSysConfigsEntity selectByConfigKey(String configKey) {
        /**
         * TODO 补充方法说明
         */
        if (StrUtil.isBlank(configKey)) {
            return null;
        }
        /**
         * TODO 补充方法说明
         * @param QueryWrapper<BaseSysConfigsEntity>( TODO
         * @return TODO
         */
        return selectOne(new QueryWrapper<BaseSysConfigsEntity>()
                .eq("config_key", configKey));
    }
}
