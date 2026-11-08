package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.entity.GoodsArticleUserFollowEntity;
import com.houjiang.goods.vo.CommunityUserInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsArticleUserFollowMapper
 * @Description  社区用户关注表
 * @Author Amos
 * @Date 2023-07-18 13:55:32
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsArticleUserFollowMapper extends BaseMapper<GoodsArticleUserFollowEntity> {

    /**
     * 查询是否关注
     * @param userId
     * @param userId1
     * @return
     */
    Integer getFollowerFlag(@Param("articleUserId")String userId, @Param("userId")String userId1);

    /**
     * 查询关注数
     * @param param
     * @return
     */
    Integer getFollowNum(@Param("param")Map<Object, Object> param);

    /**
     * 查询被关注数
     * @param param
     * @return
     */
    Integer getFollowedNum(@Param("param")Map<Object, Object> param);

    /**
     * 查询我的关注
     * @param page
     * @param param
     * @return
     */
    IPage<CommunityUserInfoVO> getFollowUserIds(IPage<String> page, @Param("param")Map<String, Object> param);

    /**
     * 我的粉丝
     * @param page
     * @param param
     * @return
     */
    IPage<CommunityUserInfoVO> getFollowedUserIds(IPage<String> page, @Param("param")Map<String, Object> param);

    /**
     * 查询关注数
     * @param param
     * @return
     */
    Integer queryExistForFollow(@Param("param")Map<Object, Object> param);

    /**
     * 关注
     * @param param
     */
    void insertFollow(@Param("param")Map<Object, Object> param);

    /**
     * 取消关注
     * @param param
     */
    void deleteFollow(@Param("param")Map<Object, Object> param);

    /**
     * 修改粉丝数
     * @param param
     */
    void updateFollowNum(@Param("param")Map<Object, Object> param);



    /**
     * @return 处理结果
     */
    List<String> getFollowUserIds2(@Param("param")Map<Object, Object> param);
}
