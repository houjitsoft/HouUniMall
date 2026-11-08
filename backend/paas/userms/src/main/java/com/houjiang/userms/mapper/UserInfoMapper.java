package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.userms.UserPointLogDTO;
import com.houjiang.commons.vo.fx.VKUserInfoVO;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.user.MemberMessageDetailVO;
import com.houjiang.commons.vo.user.MembershipGradeVO;
import com.houjiang.commons.vo.user.TagInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.MemberMessageListDTO;
import com.houjiang.userms.entity.UserInfoEntity;
import com.houjiang.userms.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;

import java.util.List;
import java.util.Map;


/**
* @Entity com.houjiang.userms.entity.UserInfo
/**
* @author Amos
*/
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfoEntity> {


    /**
     * 根据手机号查询非商家用户信息
     * @param userMobile
     * @return
     */
    UserInfoVO getUserInfoByPhoneNumber(@Param("userMobile") String userMobile);

    /**
     * @param userMobile
     * @param shopId
     * @return
     */
    UserInfoVO getUserInfoByPhoneNumber1(@Param("userMobile") String userMobile,@Param("shopId")String shopId);


    /**
     * 查询用户信息及其关联表
     * @param vo
     * @return
     */
    UserInfoVO querUserInfoByParams(@Param("param") UserInfoVO vo);

    /**
     * 更新用户新人状态
     * @param userId
     * @return
     */
    Integer updateUserIsNew(String userId);

    /**
     * 根据用户id 查询当前用户有多少积分
     * @param userId
     * @return
     */
    Integer queryMemberMessageListIntegral(String userId);

    /**
     * 查询用户信息
     * @param toString
     * @return
     */
    UserInfoDetailVO getUserInfoDetail(String toString);

    /**
     * 根据userId查询用户信息
     * @param userId
     * @return
     */
    UserInfoDetailVO getUserInfoDetail2(String userId);

    /**
     * 更新微客用户绑定店铺信息
     * @param shopId
     * @param storeId
     * @param userId
     */
    void updateUserBindShop(@Param("shopId") Integer shopId, @Param("storeId") Integer storeId, @Param("userId") String userId);

    /**
     * 查询用户等级
     * @param userId
     * @return
     */
    Map<String, Object> queryPersonalDiscount(String userId);

    /**
     * 校验用户是否已经注册
     * @param paraMap
     * @return
     */
    String checkPhoneNumber(@Param("param") Map<String, String> paraMap);

    /**
     * 为用户分配默认角色
     * @param userId 用户ID（String类型，MyBatis会自动转换为bigint）
     * @param roleId 角色ID
     */
    @Insert("INSERT INTO hou_permission_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") String userId, @Param("roleId") Integer roleId);

    /**
     * 更新为企业用户
     * @param businessegMap
     */
    void businessUpdate(Map<String, Object> businessegMap);

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    UserInfoVO getUserInfoByUserId(String userId);

    /**
     *  转账确认转账方信息
     * @param userMobile
     * @return
     */
    String queryPayeeAccountUserId(@Param("userMobile") String userMobile);

    /**
     *  判断用户是否微客
     * @param userId
     * @return
     */
    VKUserInfoVO queryVkUserInfo(String userId);

    /**
     * 获取用户信息
     *
     * @param userMobile
     * @return
     */
    UserInfoVO queryUserId(@Param("userMobile") String userMobile);



    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    UserInfoVO getUserInfoByUserId2(String userId);

    /**
     * 获取账户信息
     *
     * @param userId
     * @return
     */
    UserSettingVO getUcenterSettingByUserId(@Param("userId") String userId);

    /**
     * 根据店铺id查询用户信息
     * @param shopId
     * @return
     */
    UserInfoVO queryShopUserInfo(Integer shopId);

    /**
     * 分页查询商家客户
     * @param page
     * @param paraMap
     * @return
     */
    IPage<MyCustomerVO> listCustomerByShopId(IPage<MyCustomerVO> page , @Param("param")Map<String, Object> paraMap);


    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    UserInfoForUpdateVO getUserInfo(String userId);

    /**
     * 更新是否是新人
     * @param paraMap
     */
    void updateIsNewFeign(Map<String, String> paraMap);


    /**
     * 添加修改用户积分记录
     */
    Integer addPointsLog(@Param("param") UserPointLogDTO param);

    /**
     * 查询当前用户是否有正在开通店铺
     * @param userId
     * @return
     */
    Integer queryShopJoinBase(@Param("userId")String userId);


    /**
     * 根据手机号查门店表
     * @param phone
     * @return
     */
    Integer queryGatePhoneNumCount(String phone);

    /**
     * 根据手机号查门店表
     * @param phone
     * @return
     */
    Integer queryGatePhoneNumCount1(@Param("phone")String phone,@Param("shopId")String shopId);

    /**
     * 根据手机号查门店表
     * @param phone
     * @return
     */
    Integer queryGatePhoneNumCount2(String phone);

    /**
     * 平台 查询待审核企业用户信息
     *
     * @param
     * @return
     */
    IPage<BusinessUserListVO> queryAuditList(IPage<BusinessUserListVO> page,@Param("param") Map<String, Object> param);

    /**
     * 企业用户信息审核 状态更新
     * */
    /**
     * TODO 补充方法说明
     * @param param TODO
     */
    void updateBusinessUserStatus(Map<String, Object> param);

    /**
     * 企业用户信息审核 状态更新
     * */
    /**
     * TODO 补充方法说明
     * @param param TODO
     */
    void updateUserInfoStatus(Map<String, Object> param);

    /**
     * 查询全部用户量
     *
     * @return
     */
    Integer queryAllUserCount();

    /**
     * 查询当日用户量
     * 刪除DATE_FORMAT( FROM_UNIXTIME(uui.user_reg_time),'%Y年%m月%d日') ,
     * @return
     */
    Integer queryTodayUserCount();

    /**
     * 查询本周用户量
     *
     * @return
     */
    Integer queryWeekUserCount();

    /**
     * 查询本月用户量
     *
     * @return
     */
    Integer queryMonthUserCount();

    /**
     * 查询本年用户量
     *
     * @return
     */
   Integer queryYearUserCount();

    /**
     * 删除用户信息
     * @param param
     */
    void deleteUserInfo(Map<String, Object> param);

    /**
     * 删除用户详情
     * @param param
     */
    void deleteUserDetailInfo(Map<String, Object> param);

    /**
     * 删除用户绑定信息
     * @param param
     */
    void deleteUserBindConnect(Map<String, Object> param);

    /**
     * 删除卖家信息
     * @param param
     */
    void deleteUserSellerBase(Map<String, Object> param);

    /**
     * 删除微客信息
     * @param param
     */
    void deleteShopVkUser(Map<String, Object> param);

    /**
     * 删除企业用户信息
     * @param param
     */
    void deleteBusinessInfo(Map<String, Object> param);

    /**
     * 查询会员
     * @param page
     * @param param
     * @return
     */
    IPage<MemberMessageListVO> queryUserTurePort(IPage<MemberMessageListVO> page , @Param("param") Map<String, Object> param);

    /**
     * 修改社区用户信息
     * @param param
     */
    void updateArticleUserInfo(@Param("param")Map<Object, Object> param);


    /**
     * 查询用户下家列表用户列表
     *
     * @param param
     * @return
     */
    IPage<MemberMessageListVO> queryShareUserListPort(IPage<MemberMessageListVO> page , @Param("param") Map<String, Object> param);


    /**
     * 修改支付密码
     * @param param
     */
    void updateuserPa(Map<String, Object> param);

    /**
     * 修改会员信息
     * @param param
     */
    void updateuserInfoPort(@Param("param")Map<String, Object> param);

    /**
     * 修改标签
     * @param tagClass
     * @return
     */
    Integer updateTag(@Param("param")Map<String, Object> tagClass);


    /**
     * 更新管理员信息
     *
     * @param param
     */
    void updatePersonalBase(UserPersonalBaseVO param);

    /**
     * 查询平台用户或者品牌店铺用户信息
     * @param checkPhoneMap
     * @return
     */
    UserInfoVO getUserInfoByPhoneNum(@Param("param") Map<String, String> checkPhoneMap);

    /**
     * 修改用户昵称
     * @param updateUserParam
     */
    void updateUserNickName(@Param("param") Map<String, Object> updateUserParam);

    /**
     * 更改是否为商家
     *
     * @param userId
     * @param isShop
     */
    void updateUserIsShop(String userId, Integer isShop);

    /**
     * 根据userId获取全部用户信息
     *
     * @param userId
     * @return
     */
    UserInfoVO getUserInfoByUserIdNoState(@Param("userId") String userId);


    /**
     * 分页查询用户
     *
     * @param page
     * @param param
     * @return
     */
    IPage<MemberMessageListVO> queryUserTrueListPort(IPage<MemberMessageListVO> page, @Param("param") Map<String, Object> param);



    /**
     * 查询用户详情
     *
     * @param userId
     * @return
     */
    MemberMessageDetailVO queryMemberMessageDetailPort(String userId);

    /**
     * 查询id
     *
     * @param valueOf
     * @return
     */
    String getTagId(String valueOf);


    /**
     * 查询分类信息
     *
     * @param id
     * @return
     */
    TagInfoVO getTagInfoByid(@Param("id") String id);

    /**
     * 查询会员列表
     * @param param
     * @return
     */
    List<MemberMessageListVO> queryMemberMessageListPortExp(@Param("param") MemberMessageListDTO param);

    /**
     * 更新实名认证状态
     */
    void updateUserStart(@Param("userId") String userId);


    /**
     * 查询会员等级信息
     *
     * @param membershipGrade
     * @return
     */
    List<MembershipGradeVO> queryMembershipGradeById(@Param("param") MembershipGradeVO membershipGrade);


    /**
     * 查询平台会员列表
     * @param page
     * @param param
     * @return
     */
    IPage<MemberMessageListVO> queryMemberMessageListPort(IPage<MemberMessageListVO> page , @Param("param") MemberMessageListDTO param);

    /**
     * 查询平台会员列表
     * @param page
     * @param param
     * @return
     */
    IPage<MemberMessageListVO> queryMemberMessageListPort1(IPage<MemberMessageListVO> page , @Param("param") MemberMessageListDTO param);

    /**
     * 门店注册接口
     * @param param
     * @return
     */
    Integer registerGateStore(@Param("param") Map<String, Object> param);


    /**
     * 品牌店铺查询会员
     * @param page
     * @param param
     * @return
     */
    IPage<BrandMemberVO> querymember(IPage<BrandMemberVO> page , @Param("param") Map<String, Object> param);

    /**
     * 分页查询会员信息
     * @param page
     * @param param
     * @return
     */
    IPage<MemberMessageListVO> querymember1(IPage<MemberMessageListVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询手机号审核信息
     *
     * @param userMobile
     * @return
     */
    Map checkphone(String userMobile);

    /**
     * 查询账号状态
     *
     * @param userId
     * @return
     */
    Integer checkbusinessphone(String userId);

    /**
     *查询旧密码
     */
    String selectOldPasswordByUserMobile(@Param("param") Map map);

    /**
     * 根据手机号查用户表是否存在
     * @param phone
     * @return
     */
    String queryPhoneNumCount(String phone);

    /**
     * 更新最后登录时间 登录次数
     * @param userId
     * @return
     */
    Integer updateLoginTimeCount(String userId);


    /**
     * 定时任务-根据userId获取用户
     *
     * @param userId
     * @return
     */
    AcquiescenceVO queryUserInfoByjob(String userId);


    /**
     * 根据shopId查询店铺信息
     * @param shopId
     * @return
     */
    Map<String, Object> selectByShopInfo(String shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    UserInfoDetailVO getShreUserInfoDetail(@Param("userId") String shareUserId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String queryGateUserId(@Param("phone") String phone);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer checkUserIsRegister(@Param("userMobile") String userMobile, @Param("shopId") String shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer updateUserPassword(@Param("daoMap") Map<String, String> daoMap);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Boolean queryshopRiderSupport(@Param("shopId")String shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer queryMemberFrozenIntegral(@Param("userId")String userId);

    /***
     * 查询用户信息
     * @param shopUserDTO
     * @return
     */
    ShopUserDTO shopUserQueryUserInfo(@Param("param")ShopUserDTO shopUserDTO);

    /**
     * 据用户ID查询用户的手机号码
     * @param userId
     * @return
     */
    Map<String,String> queryUserMobileByuserId(@Param("userId")String userId);

    /**
     * 根据商城用户ID查询即时通讯用户信息
     *
     * @param mallUserId 商城用户ID，用于识别和查询对应的即时通讯用户信息
     * @return 返回包含用户即时通讯信息的ImUserInfoVO对象如果找不到对应的用户信息，
     *         则返回null或空对象，具体行为取决于实现逻辑
     */
    ImUserInfoVO imQueryUserInfo(@Param("mallUserId") String mallUserId);

    /**
     * 更新用户密码
     *
     * @param paraMap
     * @return
     */
    Integer forgotPasswordFeign(Map<String, Object> paraMap);

    /**
     * 查询用户id
     *
     * @param userMobile
     * @return
     */
    String queryUserInfoUserId(@Param("userMobile") String userMobile);


    /**
     * 根据userid查询未锁定状态的用户
     * @param userId
     * @return
     */
    UserInfoVO getUserInfoByPhoneNumberFeign(@Param("userId") String userId);



    /**
     * 校验手机号是否已注册
     *
     * @param param
     * @return
     */
    Integer checkPhoneNumberFeign(@Param("param") Map<String, Object> param);


    /**
     * 查询创建人ID
     * @param userId
     * @return
     */
    String getPrimaryAccountId(@Param("userId") String userId);


    /**
     * 查询用户手机号
     * @param userId
     * @return
     */
    String selectuserphone(@Param("param") String userId);


    /**
     * 获取用户信息
     *
     * @param userMobile
     * @return
     */
    UserInfoVO getUserInfoByMobileFeign(@Param("userMobile") String userMobile);

    /**
     * 根据用户ID查找用户名称
     *
     * @param userId 用户Id
     * @return 用户名称
     */
    String queryUserNameByUserId(String userId);

    /**
     * 根据组织ID获取组织信息
     *
     * @param orgId 组织ID
     * @return 组织信息映射
     */
    Map<String, Object> getOrgInfoById(@Param("orgId") String orgId);

    /**
     * 根据用户ID获取用户的所有组织信息
     *
     * @param userId 用户ID
     * @return 组织信息列表
     */
    List<Map<String, Object>> getUserOrgList(@Param("userId") String userId);
}
