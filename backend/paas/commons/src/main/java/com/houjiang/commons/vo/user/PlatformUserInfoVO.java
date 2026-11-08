package com.houjiang.commons.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/*
token中获取信息对应实体类
 */

/**
 * @author Amos
 */
@Data
public class PlatformUserInfoVO {

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 用户账户
	 */
	private String userAccount;

	/**
	 * 密码(后台系统)
	 */
	private String userPassword;

	/**
	 * 用户密钥
	 */
	private String userKey;

	/**
	 * 是否被封禁，0：未封禁，1封禁
	 */
	private Integer userDelete;

	/**
	 * 用户权限组id(后台系统)
	 */
	private Integer rightsGroupId;

	/**
	 * 所属子站， 0总站(后台系统)
	 */
	private Integer subSiteId;

	/**
	 * 省ID(后台系统)
	 */
	private Integer provinceId;

	/**
	 * 市ID(后台系统)
	 */
	private Integer cityId;

	/**
	 * 区ID(后台系统)
	 */
	private Integer districtId;

	/**
	 * 地址(后台系统)
	 */
	private String address;

	/**
	 * 邀请子站点ID
	 */
	private int inviteSubsiteId;

	/**
	 * 用户父店铺ID
	 */
	private int userParentShopId;

	/**
	 * 权限组名称(后台系统)
	 */
	private String rightsGroupName;

	/**
	 * 权限列表(后台系统)
	 */
	private String rightsGroupRightsIds;

	/**
	 * 创建时间(后台系统)
	 */
	private String rightsGroupAddTime;

	/**
	 * 逗号分隔所有权限(后台系统)
	 */
	private String rightsLimitsAll;

	/**
	 * 权限状态(后台系统)
	 */
	private String rightsLimitsState;

	/**
	 * 用户头像(后台系统)
	 */
	private String headPortrait;

	/**
	 * 真实姓名(后台系统)
	 */
	private String userRealname;

	/**
	 * token
	 */
	private String token;

	/**
	 * 登录时间
	 */
	private Date loginStartTime;

	/**
	 * 用户图片
	 */
	private String userImg;

	/**
	 * 骑手名称
	 */
	private String riderName;

	/**
	 * 评分
	 */
	private Integer distributionScore;

	/**
	 * 收益
	 */
	private BigDecimal profit;

	/**
	 * 待抢单
	 */
	private Integer pendingOrdersCount;

	/**
	 * 处理中
	 */
	private Integer processingOrdersCount;

	/**
	 * 已完成
	 */
	private Integer completedOrdersCount;

	/**
	 * 1-5分 总评分
	 */
	private Integer scores;

	/**
	 * 总收益
	 */
	private BigDecimal settlementSum;
	/**
	 * 0：未认证，1：审核中；2：已认证 3审核失败
	 */
	private Integer reall;
	
	/**
	 * 骑手电话
	 */
	private String qishouTel;
	
	/**
	 * 骑手名称
	 */
	private String qishouName;
}
