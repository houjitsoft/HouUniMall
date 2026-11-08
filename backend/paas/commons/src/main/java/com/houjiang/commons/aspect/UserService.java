package com.houjiang.commons.aspect;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 判断用户权限
 * 
 * @author Amos
 *
 */
public class UserService 
{
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	/** 判断是否包含权限 */
	public static UserPermissionsResult judgePermissions(String token) {
		UserPermissionsResult result = new UserPermissionsResult();
		try {
			// 根据code存储用户信息 - 统一使用HOU_USER_TOKEN_USER
			String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);

			UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
			UserInfoVO userInfo=dto.getUser();
			logger.info("userInfo{}", JSON.toJSONString(userInfo));
			// 判断是否为管理员，最大权限
			if (null != userInfo.getSellerIsAdmin() && userInfo.getSellerIsAdmin() == 1) {
				result.setUserName(userInfo.getUserName());
				result.setUserMobile(userInfo.getUserMobile());
				result.setUserId(userInfo.getUserId());
				result.setShopId(userInfo.getShopId());
				result.setShopType(userInfo.getShopType());
				result.setUserNickname(userInfo.getUserNickname());
				result.setFlag(true);
				return result;
			}
			//判断是否是门店用户
			if(null == userInfo.getSellerIsAdmin() && null!= userInfo.getShopId()){
				result.setUserName(userInfo.getUserName());
				result.setUserMobile(userInfo.getUserMobile());
				result.setUserId(userInfo.getUserId());
				result.setShopId(userInfo.getShopId());
				result.setShopType(userInfo.getShopType());
				result.setUserNickname(userInfo.getUserNickname());
				result.setFlag(true);
				logger.info("UserPermissionsResult{}", JSON.toJSONString(result));
				return result;
			}

			result.setUserName(userInfo.getUserName());
			result.setUserMobile(userInfo.getUserMobile());
			result.setUserId(userInfo.getUserId());
			result.setShopId(userInfo.getShopId());
			result.setShopType(userInfo.getShopType());
			result.setUserNickname(userInfo.getUserNickname());
			result.setFlag(true);
			return result;
		} catch (Exception e) {
			result.setFlag(false);
		}

		return result;
	}



}
