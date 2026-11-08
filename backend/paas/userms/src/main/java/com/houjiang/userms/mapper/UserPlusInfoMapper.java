package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import com.houjiang.commons.vo.base.PlusThaliEntityVO;
import com.houjiang.userms.entity.UserPlusInfoEntity;
import com.houjiang.userms.entity.UserPlusOrderEntity;
import com.houjiang.userms.vo.PlusMemberInfoVO;
import com.houjiang.userms.vo.PlusRenewEntityVO;
import com.houjiang.userms.vo.PlusRuleEntityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserPlusInfo
 * @author Amos
 */
@Mapper
public interface UserPlusInfoMapper extends BaseMapper<UserPlusInfoEntity> {
    /**
     * 查询plus 会员信息
     * @param userId
     * @return
     */
    UserPlusInfoEntity selectplusmember(String userId);

    /**
     * 修改当前用户 plus信息表已用额度
     * @param map
     */
    void updateUserPluslimit(@Param("param")HashMap<Object, Object> map);

    /**
     * 查询用户开通过
     * @param userId
     * @return
     */
    PlusMemberInfoVO selectByUserId(String userId);

    /**
     * 插入plus 会员信息
     * @param map
     * @return
     */
    Integer insertplusinfo(@Param("param") Map<String,Object> map);

    /**
     * 更新plus 会员权益
     * @param map
     * @return
     */
    Integer updateplusinfo(@Param("param") Map<String,Object> map);

    /**
     * 更新短信通知状态
     * @param userId
     */
    void updateplusinfo1(@Param("userId")String userId);

    /**
     * 查询规则
     * @return
     */
    PlusRuleEntityVO selectplusrule();

    /**
     * 查询套餐表
     * @return
     */
    List<PlusThaliEntityVO> selectthaliList();

    /**
     * 查询plus 会员是否过期
     * @param userId
     * @return
     */
    PlusThaliEntityVO selectthaliId(@Param("userId")String userId);
    /**
     * 查询过期会员信息
     * @return
     */
    List<PlusMemberInfoVO> selectplus();

    /**
     * 判断该用户是否有续费信息
     * @param userId
     * @return
     */
    PlusRenewEntityVO selectRenewNewUser(String userId);

    /**
     * 查询套餐权益
     * @param appIdList
     * @return
     */
    List<PlusEquityEntityVO> selectplusequity(@Param("list")List<Integer> appIdList);

    /**
     * 查询额度不为零的plus会员
     * @return
     */
    List<PlusMemberInfoVO> selectplusinfo();


    /**
     * 查询红包发放次数不为0的会员
     * @param month
     * @return
     */
    List<PlusMemberInfoVO> selectred(Integer month);

    /**
     * 查询用户昵称
     * @param userId
     * @return
     */
    String queryUserName(@Param("userId") String userId);

    /**
     * 查询用户昵称
     * @param userId
     * @return
     */
    String selectPhone(@Param("userId") String userId);

}




