package com.houjiang.goods.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DesensitizationUtil;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.GoodsArticleRemarkEntity;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.mapper.GoodsArticleInfoMapper;
import com.houjiang.goods.mapper.GoodsArticleRemarkMapper;
import com.houjiang.goods.service.GoodsArticleRemarkService;
import com.houjiang.goods.vo.CommunityCommentsDetailsVO;
import com.houjiang.goods.vo.CommunityCommentsVO;
import com.houjiang.goods.vo.CommunityRemarkVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class GoodsArticleRemarkServiceImpl extends ServiceImpl<GoodsArticleRemarkMapper, GoodsArticleRemarkEntity>
    implements GoodsArticleRemarkService{

    @Autowired
    private GoodsArticleInfoMapper goodsArticleInfoMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 更新评论状态
     * @param map 包含评论相关信息的映射，其中包含审核状态
     * @return 返回操作结果，如果操作成功，则返回OPERATION_SUCCESSFUL
     */
    @Override
    public ResponseResult updateStatus(Map<Object, Object> map) {
        this.baseMapper.updateCommunityCommentStatus(map);

        // 如果审核状态为1（即通过审核），则更新相关商品的评论数量
        if (Integer.parseInt(map.get("auditState").toString()) == 1) {
            goodsArticleInfoMapper.updateRemarkNum(map);
        }
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据备注ID查询评论详情列表
     * @param map 包含备注ID的映射
     * @return 返回包含评论详情的响应结果
     */
    @Override
    public ResponseResult selectDetailsList(Map<String, Object> map) {
        Integer remarkId = (Integer) map.get("remarkId");
        List<CommunityCommentsDetailsVO> communityCommentsDetailsParams = this.baseMapper.selectDetailsList(remarkId);
        return ResponseResult.success(communityCommentsDetailsParams);
    }

    /**
     * 查询评论列表
     * @param map 包含查询条件的映射
     * @return 返回包含评论列表的响应结果
     */
    @Override
    public ResponseResult selectList(Map<String, Object> map) {
        IPage<CommunityCommentsVO> page = this.baseMapper.selectCommunityComments(new Query<CommunityCommentsVO>().getPage(map), map);

        // 如果有记录，则根据配置决定是否进行脱敏处理，并处理用户昵称为空的情况
        if (page.getRecords() != null && page.getRecords().size() > 0) {
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            for (CommunityCommentsVO communityCommentsParam : page.getRecords()) {
                // 根据配置进行脱敏处理
                if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(communityCommentsParam, CommunityCommentsVO.class), communityCommentsParam);
                }
                // 处理用户昵称为空时，使用手机号作为昵称
                if (communityCommentsParam.getRemarkedUserNickName() == null) {
                    communityCommentsParam.setRemarkedUserNickName(communityCommentsParam.getRemarkedUserPhone());
                }
                if (communityCommentsParam.getRemarkUserNickName() == null) {
                    communityCommentsParam.setRemarkUserNickName(communityCommentsParam.getRemarkUserPhone());
                }
            }
        }

        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询备注列表
     *
     * @param param 查询参数，包含令牌(token)和其他过滤条件
     * @return 返回查询结果，包含备注信息及其相关用户信息
     */
    @Override
    public ResponseResult queryRemarkList(Map<String, Object> param) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息存在
        if(null != userInfo){
            // 将用户ID添加到查询参数中
            param.put("userId",userInfo.getUserId());
            // 查询备注列表
            IPage<CommunityRemarkVO> page = this.baseMapper.getRemarkList(new Query<CommunityRemarkVO>().getPage(param),param);
            List<CommunityRemarkVO> records = page.getRecords();
            // 遍历备注记录，填充用户名称和头像
            for(CommunityRemarkVO remark:records){
                String remarkedContent = null;
                // 根据备注用户的ID获取用户信息
                Map<String,String> remarkUserInfo =   this.baseMapper.getUserInfo(remark.getUserId());
                // 设置备注用户的名称和头像
                remark.setUserName(remarkUserInfo.get("userName"));
                remark.setUserAvatar(remarkUserInfo.get("userAvatar"));
                // 根据备注类型，获取备注内容
                Integer remarkType = remark.getRemarkType();
                // 对心得的评论
                if(1 == remarkType){
                    remarkedContent = goodsArticleInfoMapper.getArticleContent(remark.getRemarkedId());
                }else if(2 == remarkType){
                    // 对评论的评论
                    remarkedContent = this.baseMapper.getRemarkContent(remark.getRemarkedId());
                }
                // 设置备注内容
                remark.setRemarkedContent(remarkedContent);
            }
            // 将所有用户的备注状态设置为已读
            this.baseMapper.update(null,new LambdaUpdateWrapper<GoodsArticleRemarkEntity>()
                    .set(GoodsArticleRemarkEntity::getIsRead, CommonType.commonStatus.YES.getCode())
                    .eq(GoodsArticleRemarkEntity::getRemarkedUser,userInfo.getUserId()));
            // 更新分页信息中的记录列表
            page.setRecords(records);
            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(page));
        }
        // 如果用户信息不存在，返回令牌无效的错误信息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
    }

    /**
     * 添加备注
     *
     * @param param 添加参数，包含令牌(token)、备注内容和其他必要信息
     * @return 返回操作结果，如果成功则返回成功信息，否则返回错误信息
     */
    @Override
    public ResponseResult addRemark(Map<Object, Object> param) {
        // 通过令牌获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息不存在
        if(null == ui){
            // 返回令牌无效的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 将当前用户ID作为备注用户ID添加到参数中
        param.put("remarkUser", ui.getUserId());
        // 判断是否是用户给自己添加备注，如果是，则设置为已读
        if(param.get("remarkUser").equals(param.get("remarkedUser"))){
            param.put("isRead", CommonType.commonStatus.YES.getCode());
        }else {
            param.put("isRead", CommonType.commonStatus.NO.getCode());
        }
        // 添加备注
        this.baseMapper.addRemark(param);
        // 返回成功信息
        return ResponseResult.success();
    }
}




