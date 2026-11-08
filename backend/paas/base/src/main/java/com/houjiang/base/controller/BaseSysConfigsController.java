package com.houjiang.base.controller;

import com.houjiang.base.dto.NavSortLogoVO;
import com.houjiang.base.dto.SystemConfigHallDTO;
import com.houjiang.base.dto.WebConfigDTO;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.WabSystemConfigHallVO;
import org.springframework.web.bind.annotation.*;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseSysConfigsController
 * @Description 基础配置
 * @Author Amos
 * @Date 2023/5/19 17:24
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/sysConfig")
public class BaseSysConfigsController extends BaseController {

    /**
     * 根据configKey 查询根据configValue
     */
    @PostMapping("queryConfigKeyFeign")
    public String selectConfigValueFeign(@RequestParam("configKey") String configKey) {

        return baseSysConfigsService.selectConfigValueFeign(configKey);
    }

    /**
     * 查询首页logo
     */
    @PostMapping("/quertNavLogo")
    public ResponseResult quertNavLogo(@RequestBody NavSortLogoDTO gdbNavSortLogoDTO) {

        return baseSysConfigsService.quertNavLogo(gdbNavSortLogoDTO);
    }

    /**
     * 平台 商城配置  商城设置 查询
     *
     * @return
     */
    @PostMapping("/setting/query")
    public ResponseResult querySetting() {

        return baseSysConfigsService.selectByConfigKey(CommonType.configKey.PLATFROM_SETTING.getDesc());
    }

    /**
     * 根据configKey 查询配置
     *
     * @param configKey
     * @return
     */
    @GetMapping("/selectByConfigKey")
    public ResponseResult selectByConfigKey(@RequestParam("configKey") String configKey) {
        return baseSysConfigsService.selectByConfigKey(configKey);
    }
    /**
     * 查询 pc h5 积分商城首页
     */
    @PostMapping("/integral/queryIndex")
    public ResponseResult queryIntegralIndex(@RequestBody Map<String,String> param){
        return baseSysConfigsService.queryIntegralIndex(param);
    }

    /**
     * 查询商城客服及邮箱
     *
     * @param param
     * @return
     */
    @PostMapping("/queryPhoneAndMail")
    public ResponseResult queryPhoneAndMail(@RequestBody UserTokenBeanDTO param) {
        ResponseResult result = null;
        try {
            result = baseSysConfigsService.queryPhoneAndMail(param);
        } catch (Exception e) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),e.getMessage());
        }
        return result;
    }

    /**
     * 开启配置
     *
     * @param configKey
     * @return
     */
    @GetMapping("/updateConfig")
    public ResponseResult updateConfigEnableByConfigKey(@RequestParam("configKey") String configKey,@RequestParam("configEnable") String configEnable) {
        return baseSysConfigsService.updateConfigEnableByConfigKey(configKey,configEnable);
    }

    /**
     * 更新轮播下广告
     *
     * @param param
     * @return
     */
    @PostMapping("/adPage/update")
    public ResponseResult updateAdPage(@RequestBody SystemConfigHallDTO param) {
        try {
            ResponseResult result = baseSysConfigsService.updateAdPage(param);
            return result;
        } catch (Exception e) {
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 品牌店铺热搜词
     */
    @PostMapping("/selectHotWord")
    public ResponseResult selecthotword(@RequestBody Map<String,Object> param) {
        return baseSysConfigsService.selecthotword(param);
    }

    /**
     * 平台修改入驻设置
     *
     * @param dto
     * @return
     */
    @PostMapping("/joinSetting/update")
    public ResponseResult updateJoinSetting(@RequestBody SystemConfigHallDTO dto) {
        return baseSysConfigsService.updateSysConfigByKey(dto);
    }

    /**
     * 平台查询入驻设置
     *
     * @param param
     * @return
     */
    @PostMapping("/joinSetting/query")
    public ResponseResult queryjoinSetting(@RequestBody Map<String,Object> param) {
        return baseSysConfigsService.querySysConfigByKey(param);

    }

    /**
     * 获取企业注册密码规则
     *
     * @return
     */
    @PostMapping("/business/passwordConfig")
    public ResponseResult businessPasswordConfig() {
        return baseSysConfigsService.businessPasswordConfig();
    }

    /**
     * 获取企业注册密码规则
     *
     * @return
     */
    @PostMapping("/queryBusinessConfig")
    public ResponseResult queryBusinessConfig() {
        return baseSysConfigsService.queryBusinessConfig();
    }

    /**
     * 获取configValue
     *
     * @param configKey
     * @param configType
     * @return
     */
    @GetMapping("/selectByKeyAndType")
    public ResponseResult getConfigValue(@RequestParam("configKey") String configKey,
                                         @RequestParam("configType") String configType) {
        return baseSysConfigsService.getConfigValue(configKey,configType);
    }

    /**
     * 平台 主题设置  移动端 查询
     *
     * @param param
     * @return
     */
    @PostMapping("/themeH5Color/query")
    public ResponseResult queryThemeH5ColorSetting(@RequestBody Map<String,Object> param) {
        return baseSysConfigsService.queryThemeH5ColorSetting(param);
    }

    /**
     * 平台 主题设置  pc商城主题  修改
     *
     * @param webConfig
     * @return
     */
    @PostMapping("/themeColor/update")
    public ResponseResult updateThemeColorPageSetting(@RequestBody WebConfigDTO webConfig) {
        return baseSysConfigsService.updateThemeColorPageSetting(webConfig);
    }

    /**
     * 平台 商城配置  文案设置修改
     *
     * @param gdbNavSortLogoDTOs
     * @return
     */
    @PostMapping("/commonSort/logo")
    public ResponseResult updateNavCommonSortLogo(@RequestBody NavSortLogoVO gdbNavSortLogoDTOs) {
        return baseSysConfigsService.updateNavCommonSortLogo(gdbNavSortLogoDTOs);
    }

    /**
     * 平台 商城配置  文案设置修改
     *
     * @param map
     * @return
     */
    @PostMapping(value="/commonSort/logoString")
    public ResponseResult updateNavCommonSortLogo2(@RequestBody Map<String, String> map){
        return baseSysConfigsService.updateNavCommonSortLogo2(map);
    }

    /**
     * 广告图查询
     * @param param
     * @return
     */
    @PostMapping("/queryAdvertising")
    public ResponseResult queryadvertising(@RequestBody Map<String,Object> param) {
        return baseSysConfigsService.queryadvertising(param);
    }

    /**
     * 广告图修改
     * @param param
     * @return
     */
    @PostMapping("/updateAdvertising")
    public ResponseResult updateAdvertising(@RequestBody Map<String,Object> param) {
        return baseSysConfigsService.updateAdvertising(param);
    }

    /**
     * 热门搜索词更新
     * @param param
     * @return
     */
    @PostMapping("/updateHotWord")
    public ResponseResult updatehotword(@RequestBody Map<String,Object> param) {
        return baseSysConfigsService.updatehotword(param);
    }

    /**
     * feign 通过key查询广告位
     *
     * @return
     */
    @PostMapping("/queryMePage")
    public ResponseResult queryMePage(@RequestBody Map<String, String> paraMap) {
        return baseSysConfigsService.queryMePage(paraMap);
    }

    /**
     * 获取颜色
     * @param webConfig
     * @return
     */
    @PostMapping("/queryWapColorForUniApp")
    public ResponseResult queryWapColorForUniApp(@RequestBody WebConfigDTO webConfig) {
        return baseSysConfigsService.queryWapColorForUniApp();
    }


    /**
     *   查询平台经营规则
     */
    @PostMapping("/queryOperation")
    public ResponseResult queryplatformoperation(){
        return baseSysConfigsService.queryplatformoperation();
    }


    /**
     *   更新平台经营规则
     */
    @PostMapping("/updateOperation")
    public ResponseResult updateoperation(@RequestBody Map<String,Object> map){
        return baseSysConfigsService.updateoperation(map);
    }


    /**
     * @Param:  设置质保金通用额度
     */
    @PostMapping("/updateCatMoney")
    public ResponseResult updatecatmoney(@RequestBody Map<String,Object> map){
        return  baseSysConfigsService.updatecatmoney(map);

    }

    /**
     * @Param:  查询质保金通用额度
     */
    @PostMapping("/queryCatMoney")
    public ResponseResult querycatmoney(){
        return  baseSysConfigsService.querycatmoney();
    }


    /**
     * 查询资讯首页轮播
     *
     * @param gdbNavSortLogoDTO
     * @return
     */
    @PostMapping("/information/slider/query")
    public ResponseResult queryInformationSlider(@RequestBody NavSortLogoDTO gdbNavSortLogoDTO) {
        return baseSysConfigsService.queryInformationSlider(gdbNavSortLogoDTO);
    }

    /**
     * 修改资讯首页轮播
     *
     * @param gdbNavSortLogoDTOs
     * @return
     */
    @PostMapping("/information/slider/update")
    public ResponseResult updateInformationSlider(@RequestBody NavSortLogoVO gdbNavSortLogoDTOs) {
        return baseSysConfigsService.updateInformationSlider(gdbNavSortLogoDTOs);
    }

    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    @GetMapping("/selectConfigValueByKey")
    public String selectConfigValueByKey(@RequestParam("configKey") String configKey){
        return baseSysConfigsService.selectConfigValueByKey(configKey);
    }

    /**
     * 根据configKey查configValue
     * @return
     */
    @GetMapping("/app/shop/selectServiceAgreement")
    public ResponseResult selectServiceAgreement(){
        return baseSysConfigsService.selectByConfigKey(CommonType.configKey.SHOP_APP_SERVICE_AGREEMENT.getDesc());
    }

    /**
     * 根据configKey查configValue
     * @return
     */
    @GetMapping("/app/shop/selectPrivacyAgreement")
    public ResponseResult selectPrivacyAgreement(){
        return baseSysConfigsService.selectByConfigKey(CommonType.configKey.SHOP_APP_PRIVACY_AGREEMENT.getDesc());
    }

    /**
     * 根据configKey查询config
     *
     * @param configKey
     * @return
     */
    @GetMapping("/switch/queryLanguage")
    public ResponseResult queryLanguage(@RequestParam("configKey") String configKey) {
        return baseSysConfigsService.selectByConfigKey(configKey);
    }


    /**
     * 应用中心根据configKey更新config
     *
     * @param map
     * @return
     */
    @PostMapping("/switch/uploadLanguage")
    public ResponseResult uploadLanguage(@RequestBody Map<String,Object> map) {
        return baseSysConfigsService.uploadLanguage(map);
    }

    /**
     * 查询订单配置
     *
     * @param map
     * @return
     */
    @PostMapping("/selectOrderConfig")
    public ResponseResult selectOrderConfig(@RequestBody Map<String,Object> map) {
        return baseSysConfigsService.selectOrderConfig(map);
    }

    /**
     * 修改订单配置
     *
     * @param map
     * @return
     */
    @PostMapping("/updateOrderConfig")
    public ResponseResult updateOrderConfig(@RequestBody Map<String,Object> map) {
        return baseSysConfigsService.updateOrderConfig(map);
    }

    /**
     * 查询app首页轮播
     *
     * @param param
     * @return
     */
    @PostMapping( "/supply/slide/query")
    public ResponseResult querySupplyIndexSlide(@RequestBody Map<String,Object> param) {
        return baseSysConfigsService.querySupplyIndexSlide(param);
    }

    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    @GetMapping( "/feign/selectByConfigType")
    public BaseSysConfigsVO selectByConfigType(@RequestParam("configKey") String configKey){
        BaseSysConfigsVO baseSysConfigsVO = baseSysConfigsService.selectByConfigType(configKey);
        return baseSysConfigsVO;
    }

    /**
     * 根据configKey查询config
     * @param baseSysConfigsVO
     */
    @PostMapping( "/feign/insertByPrimaryConfigKey")
    public void insertByPrimaryConfigKey(@RequestBody BaseSysConfigsVO baseSysConfigsVO){
        baseSysConfigsService.insertByPrimaryConfigKey(baseSysConfigsVO);
    }

    /**
     * 批量新增
     * @param configKey
     * @return
     */
    @PostMapping( "/feign/insertConfigKey")
    public int insertConfigKey(@RequestBody List<String> configKey){
        int count = baseSysConfigsService.insertConfigKey(configKey);
        return count;
    }

    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    @GetMapping( "/feign/queryConfigKey")
    public List<String> queryConfigKey(@RequestBody List<String> configKey){
        List<String> list = baseSysConfigsService.queryConfigKey(configKey);
        return list;
    }
    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    @GetMapping( "/feign/querySystemConfigHall")
    public  List<WabSystemConfigHallVO> querySystemConfigHall(@RequestParam("configKey") String configKey){
        List<WabSystemConfigHallVO> list = baseSysConfigsService.querySystemConfigHall(configKey);
        return list;
    }


    /**
     * 积分与现金的兑换比例
     * @param req
     * @return
     */
    @PostMapping("/addOrUpdatePointsCashRatio")
    public ResponseResult addOrUpdatePointsCashRatio(@Valid @RequestBody PointsCashRatioVo req) {
        baseSysConfigsService.addOrUpdatePointsCashRatio(req);
        return ResponseResult.success("成功");
    }

   /**
     * 获取积分与现金的兑换比例
     * @return
     */
    @GetMapping("/getPointsCashRatio")
    public ResponseResult<PointsCashRatioVo> getPointsCashRatio() {
        return ResponseResult.success(baseSysConfigsService.getPointsCashRatio());
    }

    /**
     * 获取RSA公钥
     * @return RSA公钥
     */
    @GetMapping("/rsa/publicKey")
    public ResponseResult<String> getRSAPublicKey() {
        return baseSysConfigsService.getRSAPublicKey();
    }

    /**
     * 获取RSA密钥配置（仅管理员可用）
     * @return RSA密钥配置
     */
    @GetMapping("/rsa/keys")
    public ResponseResult<Map<String, String>> getRSAKeys() {
        return baseSysConfigsService.getRSAKeys();
    }

    /**
     * 保存RSA密钥配置
     * @param keys RSA密钥配置
     * @return 操作结果
     */
    @PostMapping("/rsa/keys")
    public ResponseResult<String> saveRSAKeys(@RequestBody Map<String, String> keys) {
        return baseSysConfigsService.saveRSAKeys(keys);
    }

    /**
     * 生成RSA密钥对
     * @param keySize 密钥长度（默认2048）
     * @return 生成的密钥对
     */
    @PostMapping("/rsa/generate")
    public ResponseResult<Map<String, String>> generateRSAKeys(@RequestParam(defaultValue = "2048") int keySize) {
        return baseSysConfigsService.generateRSAKeys(keySize);
    }

    /**
     * 测试RSA加密解密
     * @param keys RSA密钥配置
     * @return 测试结果
     */
    @PostMapping("/rsa/test")
    public ResponseResult<Map<String, Object>> testRSAEncryption(@RequestBody Map<String, String> keys) {
        return baseSysConfigsService.testRSAEncryption(keys);
    }
}