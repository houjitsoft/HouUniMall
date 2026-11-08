package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.GoodsArticleLikeEntity;
import com.houjiang.goods.entity.GoodsArticleRemarkEntity;
import com.houjiang.goods.mapper.GoodsArticleInfoMapper;
import com.houjiang.goods.mapper.GoodsArticleLikeMapper;
import com.houjiang.goods.mapper.GoodsArticleRemarkMapper;
import com.houjiang.goods.service.GoodsArticleLikeService;
import com.houjiang.goods.vo.CommunityLikeEntityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsArticleLikeServiceImpl
 * @Description  社区心得点赞表
 * @Author Amos
 * @Date 2023-07-18 14:51:33
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsArticleLikeServiceImpl extends ServiceImpl<GoodsArticleLikeMapper, GoodsArticleLikeEntity>
        implements GoodsArticleLikeService {

    @Autowired
    private GoodsArticleInfoMapper goodsArticleInfoMapper;
    @Autowired
    private GoodsArticleRemarkMapper goodsArticleRemarkMapper;

    /**
     * 点赞或取消点赞文章。
     *
     * @param param 包含点赞相关信息的Map对象。
     * @return 返回操作结果。
     */
    @Override
    public ResponseResult likeArticle(Map<Object, Object> param) {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息为空，返回令牌无效的响应
        if(null == ui){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 将用户ID添加到参数中
        param.put("likeUser",ui.getUserId());
        param.put("userId",ui.getUserId());
        // 获取点赞标志
        int likeFlag = (int) param.get("likeFlag");
        // 如果是点赞操作
        if (1 == likeFlag) {
            // 检查是否已点赞
            Integer count = this.baseMapper.getArticleIsLiked(param);
            // 如果已点赞，返回已点赞的响应
            if(count != null){
                return ResponseResult.success("已点赞");
            }
            // 根据点赞者和被点赞者是否相同，设置是否已读标志
            if(param.get("likeUser").equals(param.get("likedUser"))){
                param.put("isRead", CommonType.commonStatus.YES.getCode());
            }else {
                param.put("isRead", CommonType.commonStatus.NO.getCode());
            }
            // 插入点赞记录
            //点赞
            this.baseMapper.insertLike(param);
        } else {
            // 取消点赞操作
            //取消
            this.baseMapper.deleteLike(param);
        }
        // 获取点赞类型
        //点赞类型:1-心得;2-评论
        int likeType = (int) param.get("likeType");
        // 根据点赞类型，更新对应的心得或评论点赞数
        if(1 == likeType) {
             //对心得点赞
             goodsArticleInfoMapper.updateLikeNum(param);

        }else{
             //对评论点赞
            goodsArticleRemarkMapper.updateLikeNumForComments(param);

        }
        // 操作成功，返回成功响应
        return ResponseResult.success();
    }

    /**
     * 查询点赞列表。
     *
     * @param param 包含查询相关信息的Map对象。
     * @return 返回查询结果的响应。
     */
    @Override
    public ResponseResult queryLikeList(Map<String, Object> param) {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息为空，返回令牌无效的响应
        if(null == ui){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }else{
            // 将用户ID添加到参数中
            param.put("userId",ui.getUserId());
            // 获取点赞列表
            IPage<CommunityLikeEntityVO> page = this.baseMapper.getLikeList(new Query<CommunityLikeEntityVO>().getPage(param),param);
            List<CommunityLikeEntityVO> likeList = page.getRecords();
            // 遍历点赞列表，补充点赞者和被点赞者的信息
            for(CommunityLikeEntityVO communityLikeEntity:likeList){
                Map<String,String> likeUserInfo = goodsArticleInfoMapper.getUserInfo(communityLikeEntity.getLikeUser());
                communityLikeEntity.setLikeUserName(likeUserInfo.get("userName"));
                communityLikeEntity.setLikeUserAvatar(likeUserInfo.get("userAvatar"));
                Integer likeType = communityLikeEntity.getLikeType();
                // 根据点赞类型，补充点赞内容的信息
                //点赞心得
                if(1 == likeType){
                    String articleId = communityLikeEntity.getArticleId();
                    String articleTitle = goodsArticleInfoMapper.getArticleTitle(articleId);
                    communityLikeEntity.setArticleTitle(articleTitle);
                    //点赞评论
                }else if(2 == likeType){
                    String remarkId = communityLikeEntity.getLikedId();
                    String remarkContent = goodsArticleRemarkMapper.getRemarkContent(remarkId);
                    communityLikeEntity.setRemarkContent(remarkContent);
                }
            }
            // 更新所有被当前用户点赞的内容为已读
            this.baseMapper.update(null,new LambdaUpdateWrapper<GoodsArticleLikeEntity>()
                    .set(GoodsArticleLikeEntity::getIsRead,CommonType.commonStatus.YES.getCode())
                    .eq(GoodsArticleLikeEntity::getLikedUser,ui.getUserId()));
            page.setRecords(likeList);
            // 返回查询结果的响应
            return ResponseResult.success(PageUtils.getPage(page));
        }
    }

    /**
     * 根据用户token查询喜欢和评论的数量。
     *
     * @param param 包含用户token和其他可能参数的映射。
     * @return 返回查询结果，包括喜欢和评论的数量。
     * 如果用户token无效，返回失败响应。
     */
    @Override
    public ResponseResult queryLikeAndRemarkCount(Map<String, Object> param) {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());

        // 检查用户信息是否有效
        if (ui == null) {
            // 如果token无效，返回相应的错误响应
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        } else {
            // 初始化一个映射用于存放查询结果
            Map<String, Object> map = new HashMap<>();

            // 查询用户未读的喜欢数量
            Integer likeCount = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<GoodsArticleLikeEntity>()
                    .eq(GoodsArticleLikeEntity::getIsRead, CommonType.commonStatus.NO.getCode())
                    .eq(GoodsArticleLikeEntity::getLikedUser, ui.getUserId())));

            // 查询用户未读的评论数量
            Integer remarkCount = Math.toIntExact(goodsArticleRemarkMapper.selectCount(new LambdaQueryWrapper<GoodsArticleRemarkEntity>()
                    .eq(GoodsArticleRemarkEntity::getIsRead, CommonType.commonStatus.NO.getCode())
                    .eq(GoodsArticleRemarkEntity::getRemarkedUser, ui.getUserId())));

            // 将查询结果放入映射中
            map.put("likeCount", likeCount);
            map.put("remarkCount", remarkCount);

            // 返回成功的响应，包含查询结果
            return ResponseResult.success(map);
        }
    }
}
