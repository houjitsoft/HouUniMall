package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.NavSortLogoVO;
import com.houjiang.base.dto.SystemConfigHallDTO;
import com.houjiang.base.dto.WebConfigDTO;
import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import com.houjiang.commons.vo.base.WabSystemConfigHallVO;

import java.util.List;
import java.util.Map;


/**
 * @ClassName BaseSysConfigsService
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
public interface BaseSysConfigsService extends IService<BaseSysConfigsEntity> {
    /**
     * 根据configKey 查询配置 feign
     */
    String selectConfigValueFeign(String configKey);

    /**
     * 查询首页logo
     */
    ResponseResult quertNavLogo(NavSortLogoDTO gdbNavSortLogoDTO);

    /**
     * 获取上传图片配置地址
     */
    ResponseResult getImageConfigUrl(String configKey);

    /**
     * 根据configKey获取配置信息
     *
     * @param configKey
     * @return
     */
    ResponseResult selectByConfigKey(String configKey);

    /**
     * 查询 pc h5 积分商城首页
     *
     * @param param
     * @return
     */
    ResponseResult queryIntegralIndex(Map<String, String> param);

    /**
     * 查询商城客服及邮箱
     *
     * @param param
     * @return
     */
    ResponseResult queryPhoneAndMail(UserTokenBeanDTO param);

    /**
     * 开启配置
     *
     * @param configKey
     * @return
     */
    ResponseResult updateConfigEnableByConfigKey(String configKey, String configEnable);

    /**
     * 更新轮播下广告
     *
     * @param param
     * @return
     */
    ResponseResult updateAdPage(SystemConfigHallDTO param);

    /**
     * 品牌店铺热搜词
     *
     * @param param
     * @return
     */
    ResponseResult selecthotword(Map<String, Object> param);

    /**
     * 平台 商城配置  商城设置  修改
     *
     * @param dto
     * @return
     */
    ResponseResult updateSysConfigByKey(SystemConfigHallDTO dto);

    /**
     * 平台 商城配置  商城设置 查询
     *
     * @param map
     * @return
     */
    ResponseResult querySysConfigByKey(Map<String, Object> map);

    /**
     * 获取企业注册密码规则
     *
     * @return
     */
    ResponseResult businessPasswordConfig();

    /**
     * 获取注册开关规则
     *
     * @return
     */
    ResponseResult queryBusinessConfig();

    /**
     * 获取configValue
     *
     * @param configKey
     * @param configType
     * @return
     */
    ResponseResult getConfigValue(String configKey, String configType);

    /**
     * 平台 主题设置  移动端 查询
     *
     * @param param
     * @return
     */
    ResponseResult queryThemeH5ColorSetting(Map<String, Object> param);

    /**
     * 平台 主题设置  pc商城主题  修改
     *
     * @param webConfig
     * @return
     */
    ResponseResult updateThemeColorPageSetting(WebConfigDTO webConfig);

    /**
     * 平台 商城配置  文案设置修改
     *
     * @param gdbNavSortLogoDTOs
     * @return
     */
    ResponseResult updateNavCommonSortLogo(NavSortLogoVO gdbNavSortLogoDTOs);

    /**
     * 平台 商城配置  文案设置修改
     *
     * @param param
     * @return
     */
    ResponseResult updateNavCommonSortLogo2(Map<String, String> param);

    /**
     * 广告图查询
     *
     * @param param
     * @return
     */
    ResponseResult queryadvertising(Map<String, Object> param);

    /**
     * 广告图修改
     *
     * @param param
     * @return
     */
    ResponseResult updateAdvertising(Map<String, Object> param);

    /**
     * 热门搜索词更新
     *
     * @param param
     * @return
     */
    ResponseResult updatehotword(Map<String, Object> param);

    /**
     * feign 通过key查询广告位
     *
     * @return
     */
    ResponseResult queryMePage(Map<String, String> paraMap);

    /**
     * 获取颜色
     *
     * @param
     * @return
     */
    ResponseResult queryWapColorForUniApp();

    /**
     * 查询平台经营规则
     *
     * @param
     * @return
     */
    ResponseResult queryplatformoperation();

    /**
     * 更新平台经营规则
     */
    ResponseResult updateoperation(Map<String, Object> map);

    /**
     * @Param: 设置质保金通用额度
     */
    ResponseResult updatecatmoney(Map<String, Object> map);

    /**
     * @Param: 查询质保金通用额度
     */
    ResponseResult querycatmoney();

    /**
     * 查询资讯首页轮播
     *
     * @param gdbNavSortLogoDTO
     * @return
     */
    ResponseResult queryInformationSlider(NavSortLogoDTO gdbNavSortLogoDTO);

    /**
     * 修改资讯首页轮播
     *
     * @param gdbNavSortLogoDTOs
     * @return
     */
    ResponseResult updateInformationSlider(NavSortLogoVO gdbNavSortLogoDTOs);

    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    /**
     * 根据配置键选择配置值
     *
     * @param configKey 配置键，用于唯一标识一个配置项
     * @return 对应配置键的配置值
     */
    String selectConfigValueByKey(String configKey);

    /**
     * 上传语言配置
     *
     * @param map 包含语言配置信息的映射
     * @return 响应结果，包括上传状态和相关信息
     */
    ResponseResult uploadLanguage(Map<String, Object> map);

    /**
     * 查询订单配置
     *
     * @param map 包含查询条件的映射
     * @return 响应结果，包括查询状态和订单配置信息
     */
    ResponseResult selectOrderConfig(Map<String, Object> map);

    /**
     * 更新订单配置
     *
     * @param map 包含更新内容的映射
     * @return 响应结果，包括更新状态和相关信息
     */
    ResponseResult updateOrderConfig(Map<String, Object> map);

    /**
     * 查询供应链指数滑动配置
     *
     * @param param 包含查询条件的映射
     * @return 响应结果，包括查询状态和供应链指数滑动配置信息
     */
    ResponseResult querySupplyIndexSlide(Map<String, Object> param);

    /**
     * 根据配置类型选择配置项
     *
     * @param configKey 配置类型键，用于标识配置类型
     * @return 对应配置类型的配置项对象
     */
    BaseSysConfigsVO selectByConfigType(String configKey);


    /**
     * 根据configKey查询config
     * @param baseSysConfigsVO
     */
    void insertByPrimaryConfigKey(BaseSysConfigsVO baseSysConfigsVO);

    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    int insertConfigKey(List<String> configKey);


    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    List<String> queryConfigKey(List<String> configKey);
    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    List<WabSystemConfigHallVO> querySystemConfigHall(String configKey);

    /**
     * 积分与现金的兑换比例
     */
    void addOrUpdatePointsCashRatio(PointsCashRatioVo req);

    /**
     * 获取积分与现金的兑换比例
     */
    PointsCashRatioVo getPointsCashRatio();

    /**
     * 获取RSA公钥
     */
    ResponseResult<String> getRSAPublicKey();

    /**
     * 获取RSA密钥配置
     */
    ResponseResult<Map<String, String>> getRSAKeys();

    /**
     * 保存RSA密钥配置
     */
    ResponseResult<String> saveRSAKeys(Map<String, String> keys);

    /**
     * 生成RSA密钥对
     */
    ResponseResult<Map<String, String>> generateRSAKeys(int keySize);

    /**
     * 测试RSA加密解密
     */
    ResponseResult<Map<String, Object>> testRSAEncryption(Map<String, String> keys);
}
