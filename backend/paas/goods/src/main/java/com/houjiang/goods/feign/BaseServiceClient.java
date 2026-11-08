package com.houjiang.goods.feign;

import com.houjiang.commons.dto.base.DealerGoodsUserInfoDTO;
import com.houjiang.commons.dto.goods.GoodsMerchantSearchWordDTO;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.dto.sensitive.BaseSensitiveRecordDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.base.DealerApplyInfoVO;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.base.WabSystemConfigHallVO;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Amos
 * @date 2023/3/21 10:59
 */
@FeignClient("base")
public interface BaseServiceClient {
    /**
     * 根据configKey 查询配置 feign
     */
    @PostMapping("base/sysConfig/queryConfigKeyFeign")
    String selectConfigValueFeign(@RequestParam String configKey);

    /**
     * 查询首页logo
     *
     * @param gdbNavSortLogoDTO
     * @return
     */
    @PostMapping("/base/sysConfig/quertNavLogo")
    ResponseResult quertNavLogo(@RequestBody NavSortLogoDTO gdbNavSortLogoDTO);

    /**
     * 查询plus 会员规则  feign
     */
    @PostMapping("base/merchantPlusRule/query")
    ResponseResult queryMerchantPlusRule();

    /**
     * 查询热搜词 feign
     *
     * @return
     */
    @PostMapping("base/baseSearchWord/queryHotSearchWords")
    GoodsMerchantSearchWordDTO queryHotSearchWords(@RequestParam String commonName);

    /**
     * 修改热搜词 feign
     *
     * @return
     */
    @PostMapping("base/baseSearchWord/updateHotSearchWords")
    Integer updateHotSearchWords(@RequestBody GoodsMerchantSearchWordDTO goodsMerchantSearchWordDto);

    /**
     * 添加热搜词 feign
     *
     * @return
     */
    @PostMapping("base/baseSearchWord/addHotSearchWords")
    Integer addHotSearchWords(@RequestBody GoodsMerchantSearchWordDTO goodsMerchantSearchWordDto);

    /**
     * 查询经销商品配置
     *
     * @param commonId
     * @return
     */
    @PostMapping("base/delayGoodsConfig/queryDelayGoodsConfig")
    public BaseGoodsConfigVO queryDelayGoodsConfig(@RequestParam("commonId") Integer commonId);

    /**
     * 查询店铺是否需要经销审核
     *
     * @param goodsUserInfo
     * @return
     */
    @PostMapping("base/dealerApplyInfo/queryDealerApplyByShop")
    DealerApplyInfoVO queryDealerApplyByShop(@RequestBody DealerGoodsUserInfoDTO goodsUserInfo);


    /**
     * 查询所有敏感词 feign
     *
     * @return
     */
    @PostMapping("/baseSensitive/selectAllSensitive")
    List<BaseSensitiveVO> selectAllSensitive();

    /**
     * 根据configKey查configValue
     *
     * @param configKey
     * @return
     */
    @GetMapping("/base/sysConfig/selectConfigValueByKey")
    String selectConfigValueByKey(@RequestParam("configKey") String configKey);

    /**
     * 添加敏感词命中记录
     *
     * @param param
     * @return
     */
    @PostMapping("baseSensitiveRecord/addFeign")
    ResponseResult addbaseSensitiveRecord(@RequestBody BaseSensitiveRecordDTO param);

    /**
     * 得到地址信息
     *
     * @param districtId
     * @return
     */
    @GetMapping("/base/district/getAddressById")
    String getAddressById(@RequestParam("districtId") Integer districtId);

    /**
     * 得到地址信息
     *
     * @param provinceId
     * @param cityId
     * @param districtId
     * @return
     */
    @GetMapping("/base/district/getAddressString")
    ResponseResult getAddressString(@RequestParam("province") Integer provinceId, @RequestParam("city") Integer cityId,
                                    @RequestParam("county") Integer districtId);

    /**
     * 根据configKey查询config
     * @param configKey
     * @return
     */
    @GetMapping( "base/sysConfig/feign/querySystemConfigHall")
    List<WabSystemConfigHallVO> querySystemConfigHall(@RequestParam("configKey") String configKey);

    /**
     * 获取积分与现金的兑换比例
     * @return
     */
    @GetMapping("/base/sysConfig/getPointsCashRatio")
    ResponseResult<PointsCashRatioVo> getPointsCashRatio();
}
