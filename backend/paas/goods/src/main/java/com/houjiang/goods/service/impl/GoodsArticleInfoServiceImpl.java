package com.houjiang.goods.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.constant.RedisConstants;
import com.houjiang.commons.dto.goods.CommunityArticleListDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.CommunityArticleVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.CommunityArticleAddDTO;
import com.houjiang.goods.dto.CommunityContentReviewDTO;
import com.houjiang.goods.entity.GoodsArticleInfoEntity;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.GoodsArticleInfoService;
import com.houjiang.goods.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
@Service
public class GoodsArticleInfoServiceImpl extends ServiceImpl<GoodsArticleInfoMapper, GoodsArticleInfoEntity>
        implements GoodsArticleInfoService {

    @Autowired
    private GoodsArticleResourceMapper goodsArticleResourceMapper;
    @Autowired
    private GoodsArticleRemarkMapper goodsArticleRemarkMapper;
    @Autowired
    private GoodsArticleLikeMapper goodsArticleLikeMapper;
    @Autowired
    private GoodsArticleStarMapper goodsArticleStarMapper;
    @Autowired
    private GoodsArticleUserFollowMapper goodsArticleUserFollowMapper;
    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 更新社区审核状态
     *
     * @param param 审核状态更新参数
     * @return 返回操作结果
     */
    @Override
    public ResponseResult updateCommunityAuditStatus(CommunityContentReviewDTO param) {
        this.baseMapper.updateCommunityAuditStatus(param);
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询内容列表
     *
     * @param map 查询参数
     * @return 返回操作结果，包含分页数据
     */
    @Override
    public ResponseResult selectContentList(Map<String, Object> map) {
        // 根据查询参数获取内容审核信息的分页数据
        IPage<CommunityContentReviewVO> page = this.baseMapper.selectCommunityContentReview2(new Query<CommunityContentReviewVO>().getPage(map),
                map);
        // 获取脱敏开关的配置值
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        // 对分页数据中的每条记录进行处理，根据脱敏开关的状态进行脱敏处理
        for (CommunityContentReviewVO tmp : page.getRecords()) {
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(tmp, CommunityContentReviewVO.class), tmp);
            }
            // 查询并设置每条记录对应的图片URL列表
            // 查询心得 图片
            List<String> imageUrls = this.baseMapper.getImageReources(tmp.getArticleId());
            // 查询并设置每条记录对应的视频URL
            // 查询心得视频
            String resourceUrl = this.baseMapper.getVideoReources(tmp.getArticleId());
            tmp.setImageUrl(imageUrls);
            tmp.setResourceUrl(resourceUrl);
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 删除文章
     *
     * @param param 删除参数
     * @return 返回操作结果
     */
    @Override
    public ResponseResult deleteArticle(Map<Object, Object> param) {
        // 删除文章本身
        this.baseMapper.deleteArticle(param);
        // 删除文章关联的资源
        goodsArticleResourceMapper.deleteArticleResources(param);
        // 删除文章的评论
        goodsArticleRemarkMapper.deleteRemarks(param);
        // 删除文章的点赞记录
        goodsArticleLikeMapper.deleteLikes(param);
        return ResponseResult.success();
    }

    /**
     * 根据文章ID获取文章详情
     *
     * @param param 包含文章ID和其他查询参数的Map
     * @return 返回文章详情的ResponseResult对象
     */
    @Override
    public ResponseResult getArticleDetails(Map<Object, Object> param) {
        // 从参数中尝试获取用户ID，用于后续的权限验证和个性化设置
        String userId = null;
        if (null != param.get("token")) {
            String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + param.get("token").toString());
            if (null != userInfoStr) {
                UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
                userId = dto.getUser().getUserId();
            }
        }

        // 更新文章的阅读量
        this.baseMapper.updateReadNum(param);

        // 获取文章基本信息
        String articleId = (String) param.get("articleId");
        param.put("remarkedId", articleId);
        Map<Object, Object> tmpMap = new HashMap<>();
        tmpMap.put("userId", userId);
        tmpMap.put("articleId", articleId);
        // 判断文章是否被当前用户点赞
        Integer starFlag = goodsArticleStarMapper.getStarFlag(tmpMap) > 0 ? 1 : 0;
        CommunityArticleDetailVO communityArticleDetail = this.baseMapper.queryArticleInfo(param);

        // 计算价格
        ResponseResult<PointsCashRatioVo> pointsCashRatio = baseServiceClient.getPointsCashRatio();
        if (pointsCashRatio.getCode() == 1) {

            Double pointsRate = communityArticleDetail.getPointsRate();
            if (Objects.nonNull(pointsRate)) {
                PointsCashRatioVo cashRatiVo = pointsCashRatio.getData();
                PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(communityArticleDetail.getCommonPrice(), pointsRate);
                communityArticleDetail.setCashPrice(ratio.getCashPrice());
            }
        }

        communityArticleDetail.setHasStarred(starFlag);

        // 设置文章是否为当前用户发布
        if (null != communityArticleDetail.getUserId() && communityArticleDetail.getUserId().equals(userId)) {
            communityArticleDetail.setIsMine(1);
            communityArticleDetail.setIsFollowed(null);
        } else {
            communityArticleDetail.setIsMine(0);
            // 判断当前用户是否关注了文章作者
            Integer followFlag = goodsArticleUserFollowMapper.getFollowerFlag(communityArticleDetail.getUserId(), userId);
            if (followFlag > 0) {
                communityArticleDetail.setIsFollowed(1);
            } else {
                communityArticleDetail.setIsFollowed(0);
            }
        }

        // 判断文章是否被当前用户点赞
        param.put("userId", userId);
        param.put("likedId", articleId);
        param.put("likeType", 1);
        Integer likeId = goodsArticleLikeMapper.getArticleIsLiked(param);
        if (null != likeId && likeId > 0) {
            communityArticleDetail.setArticleIsLiked(1);
            communityArticleDetail.setLikeId(likeId);
        } else {
            communityArticleDetail.setArticleIsLiked(0);
            communityArticleDetail.setLikeId(null);
        }

        // 获取文章作者信息
        //获取作者基本信息
        Map<String, String> userInfo = this.baseMapper.getUserInfo(communityArticleDetail.getUserId());
        if (null != userInfo) {
            communityArticleDetail.setUserName(userInfo.get("userName"));
            communityArticleDetail.setUserAvatar(userInfo.get("userAvatar"));
            // 获取文章的媒体资源，包括图片和视频
        }
        //查询心得 图片
        List<String> imageUrls = goodsArticleResourceMapper.getImageReources(param.get("articleId").toString());
        communityArticleDetail.setImageUrls(imageUrls);
        //查询心得视频
        String resourceUrl = goodsArticleResourceMapper.getVideoReources(param.get("articleId").toString());
        communityArticleDetail.setResourceUrl(resourceUrl);

        // 获取文章的一级评论
        param.put("remarkType", 1);
        List<CommunityArticleRemarkEntityVO> mainRemarks = goodsArticleRemarkMapper.queryRemarks(param);
        if (null != mainRemarks && !mainRemarks.isEmpty()) {
            getUserInfo(mainRemarks, userId);
            for (CommunityArticleRemarkEntityVO mainRemark : mainRemarks) {
                // 获取文章二级评论
                param.put("remarkedId", mainRemark.getRemarkId());
                param.put("remarkType", 2);
                param.put("userId", userId);
                Integer likeIdForRemark = goodsArticleLikeMapper.getRemarkisLiked(param);
                if (null != likeIdForRemark && likeIdForRemark > 0) {
                    mainRemark.setIsLiked(1);
                    mainRemark.setLikeId(likeIdForRemark);
                } else {
                    mainRemark.setIsLiked(0);
                    mainRemark.setLikeId(null);
                }
                List<CommunityArticleRemarkEntityVO> subRemarks = goodsArticleRemarkMapper.queryRemarks(param);
                for (CommunityArticleRemarkEntityVO subRemark : subRemarks) {
                    param.put("remarkedId", subRemark.getRemarkId());
                    param.put("remarkType", 2);
                    param.put("userId", userId);
                    Integer likeIdForSRemark = goodsArticleLikeMapper.getRemarkisLiked(param);
                    if (null != likeIdForSRemark && likeIdForSRemark > 0) {
                        subRemark.setIsLiked(1);
                        subRemark.setLikeId(likeIdForSRemark);
                    } else {
                        subRemark.setIsLiked(0);
                        subRemark.setLikeId(null);
                    }
                }
                getUserInfo(subRemarks, userId);
                mainRemark.setRemarkList(subRemarks);
            }
        }
        if (CollectionUtils.isEmpty(mainRemarks)) {
            mainRemarks = Lists.newArrayList();
        }
        communityArticleDetail.setMainRemarks(mainRemarks);

        // 返回处理后的文章详情
        return ResponseResult.success(communityArticleDetail);
    }

    /**
     * 根据文章评论实体列表和用户ID，填充用户信息和是否为自己评论的标记。
     * 此方法用于在显示文章评论时，获取评论者的用户信息和被评论者的用户信息，并标记评论是否为自己做出。
     *
     * @param mainRemarks 文章评论实体列表，包含评论和被评论的信息。
     * @param userId      当前用户的ID，用于比较评论者是否为当前用户。
     */
    private void getUserInfo(List<CommunityArticleRemarkEntityVO> mainRemarks, String userId) {
        for (CommunityArticleRemarkEntityVO mainRemark : mainRemarks) {
            // 获取评论用户的用户信息
            Map<String, String> remarkUserInfo = this.baseMapper.getUserInfo(mainRemark.getRemarkUser());
            if (null != remarkUserInfo) {
                mainRemark.setRemarkUserName(remarkUserInfo.get("userName"));
                mainRemark.setRemarkUserAvatar(remarkUserInfo.get("userAvatar"));
            }
            // 获取被评论用户的用户信息
            Map<String, String> remarkedUserInfo = this.baseMapper.getUserInfo(mainRemark.getRemarkedUser());
            if (null != remarkedUserInfo) {
                mainRemark.setRemarkedUserName(remarkedUserInfo.get("userName"));
                mainRemark.setRemarkedUserAvatar(remarkedUserInfo.get("userAvatar"));
            }
            // 判断评论是否为自己做出，设置标记
            if (mainRemark.getRemarkUser().equals(userId)) {
                mainRemark.setOwnRemark(1);
            } else {
                mainRemark.setOwnRemark(0);
            }
        }
    }

    /**
     * 根据传入的参数查询用户信息，并统计用户的点赞、关注、被关注和收藏数量。
     * 此方法用于在用户详情页显示用户的各项统计信息，如点赞数、关注数、被关注数和收藏数。
     *
     * @param param 包含查询用户信息所需参数的映射。
     * @return 返回包含用户信息和统计信息的响应结果。
     */
    @Override
    public ResponseResult queryUserInfo(Map<Object, Object> param) {
        // 若参数中未包含用户ID，尝试从token中获取用户ID
        //若查看他人心得列表，则需要传入UserId;
        //若查看自己的心得列表，则需要传入token;
        if (param.get("userId") == null) {
            UserInfoVO ui = ParseTokenUtils.getUserInfoByTokenNoIsExist(param.get("token").toString());
            if (null != ui) {
                param.put("userId", ui.getUserId());
            }
        }
        // 查询用户基本信息
        CommunityUserInfoVO userInfo = this.baseMapper.getUserInfoEntity(param);
        // 统计用户的点赞数量
        Integer likedNum = goodsArticleLikeMapper.getLikedNum(param);
        userInfo.setLikedNum(likedNum);
        // 统计用户的关注数量
        Integer followNum = goodsArticleUserFollowMapper.getFollowNum(param);
        userInfo.setFollowNum(followNum);
        // 统计用户被关注的数量
        Integer followedNum = goodsArticleUserFollowMapper.getFollowedNum(param);
        userInfo.setFollowedNum(followedNum);
        // 统计用户的收藏数量
        Integer starNum = goodsArticleStarMapper.getStarNum(param);
        userInfo.setStarNum(starNum);
        // 如果用户信息不为空且用户签名为空，则设置默认签名
        if (null != userInfo && null == userInfo.getUserSignature()) {
            userInfo.setUserSignature("还来不及增加介绍呢！");
        }
        // 返回包含用户信息和统计信息的响应结果
        return ResponseResult.success(userInfo);
    }


    /**
     * 根据用户条件搜索用户列表。
     *
     * @param param 查询参数，包含token、当前页码、每页数量等。
     * @return 返回搜索结果，包含用户列表和总数。
     */
    @Override
    public ResponseResult searchUserList(Map<Object, Object> param) {
        List<CommunityUserInfoVO> resultList = new ArrayList<>();
        // 从参数中获取token
        String token = (String) param.get("token");
        String userId = "";
        // 从参数中获取当前页码和每页数量
        Integer currentPage = (Integer) param.get("page");
        Integer pageSize = (Integer) param.get("limit");
        boolean loginFlag = false;
        // 置空参数中的currentPage和pageSize，以获取所有用户ID
        //置空全查已关注用户
        param.put("currentPage", null);
        param.put("pageSize", null);
        // 初始化关注的用户ID列表和其他用户ID列表
        List<String> followUserIds = new ArrayList<>();
        List<String> resultAllUserIds = new ArrayList<>();
        List<String> resultAllUserIdsTmp = new ArrayList<>();
        List<String> hasArticleUserIds = new ArrayList<>();
        List<String> allUserIds = null;
        // 获取所有用户ID和发表过文章的用户ID
        allUserIds = this.baseMapper.getAllUserIds(param);
        //发表过心得且审核通过的用户id列表
        hasArticleUserIds = this.baseMapper.getHasArticleUserIds(param);
        // 从所有用户ID中移除发表过文章的用户ID
        allUserIds.removeAll(hasArticleUserIds);
        // 将发表过文章的用户ID和剩余的用户ID合并
        //resultAllUserIdsTmp所有用户列表,优先展示发表过心得的用户
        resultAllUserIdsTmp.addAll(hasArticleUserIds);
        resultAllUserIdsTmp.addAll(allUserIds);
        resultAllUserIds.addAll(resultAllUserIdsTmp);
        // 如果token不为空，检查token是否有效，并获取用户ID
        //token不为空则为用户登录状态,首先展示发表过心得用户，最后是未发表过心得的用户
        if (null != token && !"".equals(token)) {
            boolean isExist = RedisUtil.exists(RedisConstants.USER_TOKEN_USER + token);
            if (!isExist) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
            }
            userId = RedisUtil.get(RedisConstants.USER_TOKEN_USERID + token);
            param.put("userId", userId);
            loginFlag = true;
            // 如果当前用户在结果列表中，将其移除并单独处理
            if (null != userId) {
                if (resultAllUserIds.contains(userId)) {
                    resultAllUserIdsTmp.add(userId);
                    resultAllUserIds.removeAll(resultAllUserIdsTmp);
                }
            }
            // 获取当前用户关注的用户ID列表
            followUserIds = goodsArticleUserFollowMapper.getFollowUserIds2(param);
            // 如果token为空，则直接使用之前准备的所有用户ID列表
        } else {
            resultAllUserIds = resultAllUserIdsTmp;
        }

        // 分页处理用户ID列表
        List<String> pagedResultAllUserIds = Lists.newArrayList();
        if (CollectionUtils.isEmpty(resultAllUserIds)) {
            pagedResultAllUserIds = Lists.newArrayList();
        }
        int totalCount = resultAllUserIds.size();
        if (totalCount > 0) {
            int fromIndex = (currentPage - 1) * pageSize;
            int toIndex = currentPage * pageSize;
            if (toIndex > totalCount) {
                toIndex = totalCount;
            }
            pagedResultAllUserIds = resultAllUserIds.subList(fromIndex, toIndex);
        }

        // 根据分页后的用户ID列表，查询每个用户的信息和文章列表
        for (int i = 0; i < pagedResultAllUserIds.size(); i++) {
            CommunityUserInfoVO userInfo = new CommunityUserInfoVO();
            String uid = pagedResultAllUserIds.get(i);
            Map<String, String> tmpMap = this.baseMapper.getUserInfo(uid);
            userInfo.setUserId(uid);
            userInfo.setUserName(tmpMap.get("userName"));
            userInfo.setUserAvatar(tmpMap.get("userAvatar"));
            Map<Object, Object> queryMap = new HashMap<>();
            queryMap.put("userId", uid);
            Integer likedNum = goodsArticleLikeMapper.getLikedNum(queryMap);
            Integer articleNum = this.baseMapper.getArticleNum(queryMap);
            if (articleNum > 3) {
                articleNum = 3;
            }
            userInfo.setLikedNum(likedNum);
            userInfo.setArticleNum(articleNum);
            // 根据登录状态和关注列表，设置用户是否被关注
            if (loginFlag) {
                if (followUserIds.contains(uid)) {
                    userInfo.setIsFollowed(1);
                } else {
                    userInfo.setIsFollowed(0);
                }
                //若未登录则都显示未关注
            } else {
                userInfo.setIsFollowed(0);
            }
            List<CommunityArticleSmallEntityVO> articles = this.baseMapper.getArticleSmallEntity(uid);
            if (null == articles || articles.isEmpty()) {
                articles = new ArrayList<>();
            }
            userInfo.setArticleList(articles);
            resultList.add(userInfo);
        }
        // 构造返回结果，包含用户列表和总数
        Map<String, Object> map = new HashMap<>();
        map.put("resultList", resultList);
        map.put("totalCount", totalCount);
        return ResponseResult.success(map);
    }

    /**
     * 根据参数查询商品信息。
     *
     * @param param 查询参数，包含页码、每页数量、token等信息。
     * @return 返回查询结果，包含商品列表和总数。
     */
    @Override
    public ResponseResult queryGoods(Map<Object, Object> param) {
        // 解析页码和每页数量
        Integer currentPage = (Integer) param.get("page");
        Integer pageSize = (Integer) param.get("limit");
        // 存储最终查询结果的映射
        Map<String, Object> resultMap = new HashMap<>();

        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        if (ui == null) {
            // 如果token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        } else {
            // 添加用户ID到查询参数
            param.put("userId", ui.getUserId());
            // 根据参数查询订单ID列表
            List<String> orderIds = this.baseMapper.queryOrder(param);
            // 存储所有商品详情的列表
            List<GoodsDetailVO> allGoodsList = new ArrayList<>();
            // 根据订单ID查询商品详情，并合并到所有商品列表中
            for (String orderId : orderIds) {
                List<GoodsDetailVO> goodsDetails = this.baseMapper.queryGoods(orderId);
                allGoodsList.addAll(goodsDetails);
            }
            // 去除重复的商品详情
            List<GoodsDetailVO> allDistinctGoodsList = allGoodsList.stream().distinct().collect(Collectors.toList());
            // 如果去重后的商品列表为空，初始化为空列表
            if (CollectionUtils.isEmpty(allDistinctGoodsList)) {
                allDistinctGoodsList = Lists.newArrayList();
            }
            // 计算商品总数
            Integer totalCount = allDistinctGoodsList.size();
            // 添加商品总数到结果映射中
            resultMap.put("totalCount", totalCount);
            // 存储分页后的商品列表
            List<GoodsDetailVO> pageList = Lists.newArrayList();
            // 如果商品列表不为空，进行分页
            if (!allDistinctGoodsList.isEmpty()) {
                // 计算分页的起始和结束索引
                int fromIndex = (currentPage - 1) * pageSize;
                int toIndex = currentPage * pageSize;
                // 如果结束索引超过总数，调整为总数
                if (toIndex > totalCount) {
                    toIndex = totalCount;
                }
                // 获取分页后的商品列表
                pageList = allDistinctGoodsList.subList(fromIndex, toIndex);
            }
            // 添加分页后的商品列表到结果映射中
            resultMap.put("goodsList", pageList);
        }
        // 返回查询成功的结果，包含商品列表和总数
        return ResponseResult.success(resultMap);
    }

    /**
     * 添加社区文章。
     *
     * @param param 添加文章的参数，包含文章内容、token等信息。
     * @return 返回添加结果。
     */
    @Override
    public ResponseResult addArticle(CommunityArticleAddDTO param) {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (ui == null) {
            // 如果token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        } else {
            // 设置用户ID
            param.setUserId(ui.getUserId());
        }
        // 生成文章ID
        String uuid = UUIDUtil.getUUID();
        param.setArticleId(uuid);
        // 根据是否有图片，设置文章类型和资源类型
        if (param.getImageUrl() != null && param.getImageUrl().size() > 0) {
            param.setResourceType(1);
            param.setArticleType(1);
        } else {
            param.setResourceType(2);
            param.setArticleType(2);
        }
        // 添加文章信息
        this.baseMapper.addArticleInfo(param);
        // 如果是图片类型的文章，添加图片资源信息
        if (1 == param.getResourceType()) {
            if (null != param.getResourceUrl()) {
                param.setResourceType(2);
                goodsArticleResourceMapper.addArticleResources(param);
            }
            for (String imageUrl : param.getImageUrl()) {
                param.setResourceUrl(imageUrl);
                param.setResourceType(1);
                goodsArticleResourceMapper.addArticleResources(param);
            }
        } else {
            // 如果是视频类型的文章，添加视频资源信息
            goodsArticleResourceMapper.addArticleResources(param);
        }
        // 返回添加成功的结果
        return ResponseResult.success();
    }

    /**
     * 根据参数获取文章列表。
     *
     * @param param 包含分页信息和查询条件的DTO对象。
     * @return 返回包含文章列表的响应结果。
     */
    @Override
    public ResponseResult getArticleList(CommunityArticleListDTO param) {
        // 验证用户身份，通过token获取用户信息并设置到查询参数中
        // 若查看他人心得列表，则需要传入UserId;
        // 若查看自己的心得列表，则需要传入token;
        if ((param.getUserId() == null || param.getUserId().isEmpty()) && param.getToken() != null && !param.getToken().isEmpty()) {
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
            if (ui == null) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
            } else {
                param.setUserId(ui.getUserId());
                // 查看自己心得列表
                param.setAuditState(null);
            }
        } else {
            // 查看他人心得列表
            param.setAuditState(CommonType.articleStart.AUDIT_SUCCESS.getCode());
        }
        Page<CommunityArticleVO> page = new Page<>(param.getPage(), param.getLimit());
        IPage<CommunityArticleVO> result = this.baseMapper.getArticleList(page, param);
        // 设置文章作者信息
        if (!CollectionUtils.isEmpty(result.getRecords())) {
            for (CommunityArticleVO communityArticleResult : result.getRecords()) {
                Map<String, String> userInfo = this.baseMapper.getUserInfo(communityArticleResult.getUserId());
                communityArticleResult.setUserName(userInfo.get("userName"));
                communityArticleResult.setUserAvatar(userInfo.get("userAvatar"));
            }
        }
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 根据参数获取文章列表。
     *
     * @param param 包含分页信息和查询条件的DTO对象。
     * @return 返回包含文章列表的响应结果。
     */
    @Override
    public ResponseResult getAllArticleList(CommunityArticleListDTO param) {
        // 查看首页全部的心得列表
        Page<CommunityArticleVO> page = new Page<>(param.getPage(), param.getLimit());
        param.setAuditState(CommonType.articleStart.AUDIT_SUCCESS.getCode());
        IPage<CommunityArticleVO> result = this.baseMapper.getArticleList(page, param);
        // 设置文章作者信息
        if (!CollectionUtils.isEmpty(result.getRecords())) {
            for (CommunityArticleVO communityArticleResult : result.getRecords()) {
                Map<String, String> userInfo = this.baseMapper.getUserInfo(communityArticleResult.getUserId());
                communityArticleResult.setUserName(userInfo.get("userName"));
                communityArticleResult.setUserAvatar(userInfo.get("userAvatar"));
            }
        }
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 查询社区主题分类。
     *
     * @return 返回社区主题分类列表的响应结果。
     */
    @Override
    public ResponseResult queryCommunityTopics() {
        List<CommunityArticleTagClassVO> params = this.baseMapper.queryGoodsCats();
        return ResponseResult.success(params);
    }

    /**
     * 查询随机邀请文章。
     *
     * @param param 包含查询条件的Map对象，主要用于获取用户ID。
     * @return 返回随机邀请文章列表的响应结果。
     */
    @Override
    public ResponseResult queryRandomInvitation(Map<Object, Object> param) {
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + param.get("token").toString());
        List<CommunityArticleVO> communityArticleResultsList = this.baseMapper.queryRandomInvitation(userId);
        // 设置文章作者信息和关注状态
        for (CommunityArticleVO communityArticleResult : communityArticleResultsList) {
            // 获取用户头像 名称
            Map<String, String> userInfo = this.baseMapper.getUserInfo(communityArticleResult.getUserId());
            communityArticleResult.setUserName(userInfo.get("userName"));
            communityArticleResult.setUserAvatar(userInfo.get("userAvatar"));
            // 默认未关注
            communityArticleResult.setHasFollowed(0);
            Map<Object, Object> tmMap = new HashMap<>();
            tmMap.put("userId", communityArticleResult.getUserId());
            // 获取关注数
            Integer followerNum = this.baseMapper.getFollowedNum(tmMap);
            communityArticleResult.setFollowerNum(followerNum);
        }
        return ResponseResult.success(communityArticleResultsList);
    }

    /**
     * 根据用户关注的用户列表获取文章列表。
     *
     * @param param 包含分页信息和用户token的DTO对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult getFollowArticleList(CommunityArticleListDTO param) {
        // 通过token获取用户信息。
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 若用户信息为空，则返回非法token的错误信息。
        if (ui == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 构建查询条件，用于获取用户关注的用户ID列表。
        Map<Object, Object> tmpMap = new HashMap<>();
        tmpMap.put("userId", ui.getUserId());
        // 根据用户ID列表查询关注的用户的文章ID列表。
        List<String> followUserIds = this.baseMapper.getFollowUserIds(tmpMap);
        // 若关注的用户列表为空，则返回空数据的响应结果。
        if (null == followUserIds || followUserIds.isEmpty()) {
            return ResponseResult.success(BizCodeEnume.SELECT_DATA_NULL);
        }
        // 设置查询条件中的关注用户ID列表。
        param.setFollowUserIds(followUserIds);
        // 构建分页对象。
        Page<CommunityArticleVO> page = new Page<>(param.getPage(), param.getLimit());
        // 根据分页条件和关注用户ID列表查询文章列表。
        IPage<CommunityArticleVO> result = this.baseMapper.getFollowArticleList(page, param);
        // 若查询结果为空，则返回空数据的响应结果。
        if (CollectionUtils.isEmpty(result.getRecords())) {
            return ResponseResult.success(BizCodeEnume.SELECT_DATA_NULL);
        }
        // 遍历查询结果，补充文章作者信息和关注状态。
        for (CommunityArticleVO communityArticleResult : result.getRecords()) {
            // 根据文章作者ID获取作者信息。
            Map<String, String> userInfo = this.baseMapper.getUserInfo(communityArticleResult.getUserId());
            // 设置文章作者信息。
            communityArticleResult.setUserName(userInfo.get("userName"));
            communityArticleResult.setUserAvatar(userInfo.get("userAvatar"));
            // 默认设置用户已关注文章作者。
            communityArticleResult.setHasFollowed(1);
            // 构建查询条件，用于获取文章作者的粉丝数。
            Map<Object, Object> tmMap = new HashMap<>();
            tmMap.put("userId", communityArticleResult.getUserId());
            // 查询并设置文章作者的粉丝数。
            Integer followerNum = this.baseMapper.getFollowedNum(tmMap);
            communityArticleResult.setFollowerNum(followerNum);
        }
        // 返回查询结果的响应结果。
        return ResponseResult.success(PageUtils.getPage(result));
    }

}




