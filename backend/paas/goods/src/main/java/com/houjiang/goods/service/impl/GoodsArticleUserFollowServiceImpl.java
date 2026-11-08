package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.GoodsArticleUserFollowEntity;
import com.houjiang.goods.mapper.GoodsArticleInfoMapper;
import com.houjiang.goods.mapper.GoodsArticleUserFollowMapper;
import com.houjiang.goods.service.GoodsArticleUserFollowService;
import com.houjiang.goods.vo.CommunityUserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsArticleUserFollowServiceImpl
 * @Description  社区用户关注表
 * @Author Amos
 * @Date 2023-07-18 16:05:02
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsArticleUserFollowServiceImpl extends ServiceImpl<GoodsArticleUserFollowMapper, GoodsArticleUserFollowEntity>
        implements GoodsArticleUserFollowService {

    @Autowired
    private GoodsArticleInfoMapper goodsArticleInfoMapper;

    /**
     * 根据查询标志查询关注或粉丝列表。
     *
     * @param param 查询参数，包含查询标志、令牌等信息。
     * @return 查询结果，包含关注或粉丝的详细信息。
     */
    @Override
    public ResponseResult queryfollowList(Map<String, Object> param) {
        // 检查查询标志是否为空
        if (null == param.get("queryFlag")) {
            return ResponseResult.fail(0, "查询标识不能为空");
        }
        // 查询标志：1 表示查询关注列表，0 表示查询粉丝列表
        //查询标识: 1-查询我的关注;0-查询粉丝
        Integer queryFlag = (Integer) param.get("queryFlag");
        try {
            // 根据令牌解析用户信息
            UserInfoVO rr = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
            // 如果令牌无效，返回错误信息
            if (rr == null) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
            } else {
                // 将用户ID添加到参数中
                param.put("userId", rr.getUserId());
            }
            List<CommunityUserInfoVO> data = new ArrayList<>();
            // 初始化分页对象
            IPage<CommunityUserInfoVO> page = new Page<>();
            // 根据查询标志查询关注或粉丝的用户ID
            if (1 == queryFlag) {
                // 查询关注列表
                //我的关注
                page = this.baseMapper.getFollowUserIds(new Query<String>().getPage(param),param);
            } else if (0 == queryFlag) {
                // 查询粉丝列表
                //我的粉丝
                page = this.baseMapper.getFollowedUserIds(new Query<String>().getPage(param),param);
            }
            // 遍历查询结果，组装用户详细信息
            Map<Object,Object> tmpMap = new HashMap<>();
            for (CommunityUserInfoVO vo : page.getRecords()) {
                String userId = vo.getUserId();
                // 根据用户ID查询用户基本信息
                Map<String, String> map = goodsArticleInfoMapper.getUserInfo(userId);
                CommunityUserInfoVO userInfo = new CommunityUserInfoVO();
                userInfo.setUserId(userId);
                userInfo.setUserAvatar(map.get("userAvatar"));
                userInfo.setUserName(map.get("userName"));
                // 查询当前用户是否关注了该用户
                Integer flag = this.baseMapper.getFollowerFlag(userId,param.get("userId").toString());
                if(flag > 0){
                    userInfo.setIsFollowed(1);
                }else{
                    userInfo.setIsFollowed(0);
                }
                tmpMap.put("userId",userId);
                // 查询该用户关注的人数
                Integer followedNum = this.baseMapper.getFollowedNum(tmpMap);
                userInfo.setFollowedNum(followedNum);
                data.add(userInfo);
            }
            // 更新分页对象的记录列表
            page.setRecords(data);
            // 返回查询成功的响应结果
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 捕获异常，返回系统错误的响应结果
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 关注或取消关注用户。
     * 根据传入的参数判断是执行关注操作还是取消关注操作。
     * 如果是关注操作，检查是否已存在关注关系，避免重复关注。
     * 对于批量操作，遍历每个用户ID执行关注或取消关注的操作。
     *
     * @param param 包含操作所需信息的Map对象，如用户令牌(token)、关注标志(followFlag)等。
     * @return 返回操作结果，成功或失败，并给出相应的提示信息。
     */
    @Override
    public ResponseResult followUser(Map<Object, Object> param) {
        // 通过令牌获取用户信息，检查令牌有效性
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        if (ui == null) {
            // 令牌无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 将当前操作用户的ID放入参数中
        param.put("followUserId", ui.getUserId());
        // 判断是关注还是取消关注
        int followFlag = (int) param.get("followFlag");
        if (1 == followFlag) {
            // 查询是否已关注，避免重复关注
            Integer count = this.baseMapper.queryExistForFollow(param);
            if (count > 0) {
                // 已关注，返回失败结果
                return ResponseResult.fail(0, "请勿重复操作！");
            }
            // 执行关注操作
            this.baseMapper.insertFollow(param);
        } else {
            // 执行取消关注操作
            this.baseMapper.deleteFollow(param);
        }
        // 更新关注数量
        this.baseMapper.updateFollowNum(param);

        // 操作成功，返回成功结果
        return ResponseResult.success();
    }

    /**
     * 批量关注或取消关注用户。
     * 与单个关注操作类似，但针对多个用户ID执行操作。
     *
     * @param param 包含操作所需信息的Map对象，如用户令牌(token)、关注标志(followFlag)、关注的用户ID列表(followedUserIds)等。
     * @return 返回操作结果，成功或失败，并给出相应的提示信息。
     */
    @Override
    public ResponseResult batchfollowUser(Map<Object, Object> param) {
        // 通过令牌获取用户信息，检查令牌有效性
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        if (ui == null) {
            // 令牌无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 将当前操作用户的ID放入参数中
        param.put("followUserId", ui.getUserId());
        // 判断是关注还是取消关注
        int followFlag = (int) param.get("followFlag");
        // 获取需批量操作的用户ID列表
        ArrayList<String> followedUserIds = (ArrayList<String>) param.get("followedUserIds");
        // 遍历用户ID列表，对每个用户执行关注或取消关注操作
        for (String followedUserId : followedUserIds) {
            // 设置当前操作的目标用户ID
            param.put("followedUserId", followedUserId);
            if (1 == followFlag) {
                // 查询是否已关注，避免重复关注
                Integer count = this.baseMapper.queryExistForFollow(param);
                if (count > 0) {
                    // 已关注，返回失败结果
                    return ResponseResult.fail(0, "请勿重复操作！");
                }
                // 执行关注操作
                this.baseMapper.insertFollow(param);
            } else {
                // 执行取消关注操作
                this.baseMapper.deleteFollow(param);
            }
            // 更新关注数量
            this.baseMapper.updateFollowNum(param);
        }
        // 批量操作成功，返回成功结果
        return ResponseResult.success();
    }

    /**
     * 根据用户token和参数中的userId检查用户信息。
     * 该方法主要用于确定当前请求用户是否为所查询用户本人，以及查询用户是否关注了当前用户。
     *
     * @param param 包含token和其他参数的映射对象。
     * @return 返回一个包含查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult checkUserInfo(Map<Object, Object> param) {
        // 通过token获取当前登录的用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByTokenNoIsExist(param.get("token").toString());
        String userId = null;
        if(null != ui){
            // 当前登录用户的userId
            userId = ui.getUserId();
        }

        Map<String,Integer> resultMap = new HashMap<>();
        // 判断参数中的userId是否等于当前登录用户的userId
        if( param.get("userId").equals(userId)){
            // 如果是本人，则isMe为1，isFollowed为空
            resultMap.put("isMe",1);
            resultMap.put("isFollowed",null);
        }else{
            // 如果不是本人，则判断当前用户是否关注了参数中的用户
            resultMap.put("isMe",0);
            Integer followFlag = this.baseMapper.getFollowerFlag((String)param.get("userId"),userId);
            if(followFlag >0 ){
                // 如果关注了，则isFollowed为1
                resultMap.put("isFollowed",1);
            }else{
                // 如果没有关注，则isFollowed为0
                resultMap.put("isFollowed",0);
            }
        }
        // 返回查询结果
        return ResponseResult.success(resultMap);
    }
}
