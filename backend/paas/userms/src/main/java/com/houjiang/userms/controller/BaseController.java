package com.houjiang.userms.controller;

/**
 * @author Amos
 * @date 2023/5/18 15:03
 */

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.exception.GlobalExceptionEnum;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.service.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础Controller
 */
public class BaseController {

    @Autowired
    protected UserInfoService userInfoService;

    @Autowired
    protected UserInfoDetailService userInfoDetailService;

    @Autowired
    protected UserBusinessInfoService userBusinessInfoService;

    @Autowired
    protected UserPlusInfoService userPlusInfoService;

    @Autowired
    protected UserPlusEquityService userPlusEquityService;

    @Autowired
    protected UserPlusEquityTempService userPlusEquityTempService;

    @Autowired
    protected UserPointsLogService userPointsLogService;

    @Autowired
    protected UserLiveStatisticsService userLiveStatisticsService;

    @Autowired
    protected UserFavoritesGoodsService userFavoritesGoodsService;

    @Autowired
    protected UserFavoritesShopService userFavoritesShopService;

    @Autowired
    protected UserVkService userVkService;

    @Autowired
    protected UserAddressService userAddressService;

    @Autowired
    protected UserSellerBaseService sellerBaseService;

    @Autowired
    protected UserAdminBaseService userAdminBaseService;

    @Autowired
    protected UserPlusOrderService userPlusOrderService;

    @Autowired
    protected UserUploadAlbumService userUploadAlbumService;

    @Autowired
    protected UserUploadBaseService userUploadBaseService;

    @Autowired
    protected UserVkSetService userVkSetService;

    @Autowired
    protected UserBindConnectService userBindConnectService;

    @Autowired
    protected UserLogActionService userLogActionService;

    @Autowired
    protected UserSellerGroupService userSellerGroupService;

    @Autowired
    protected UserAdminDepartmentService userAdminDepartmentService;

    @Autowired
    protected UserAdminRightsGroupService userAdminRightsGroupService;

    @Autowired
    protected UserTagInfoService userTagInfoService;

    @Autowired
    protected UserPlatformMessageService userPlatformMessageService;

    /**
     * 根据token得到userInfo
     *
     * @param token
     * @return
     */
    public ResponseResult getUserInfoByToken(String token) {
        //从redis得到token转换userInfo
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExist) {
            throw new DefinedException(GlobalExceptionEnum.NO_FORBIDDEN.getErrorCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        } else {
            String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            if (dto != null) {
                return ResponseResult.success(dto.getUser());
            }
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());

        }

    }
}
