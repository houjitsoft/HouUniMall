package com.houjiang.commons.aspect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsResult {

	private Boolean flag;

	/**
	 * 店铺id
	 */
	private String shopId;

	/**
	 * 店铺类型
	 */
	private Integer shopType;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 手机号
	 */
	private String userMobile;

	/**
	 * 最后登录时间
	 */
	private String userLastloginTime;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 店铺logo
	 */
	private String wapShopLogo;

	/**
	 * 用户昵称
	 */
	private String userNickname;



}
