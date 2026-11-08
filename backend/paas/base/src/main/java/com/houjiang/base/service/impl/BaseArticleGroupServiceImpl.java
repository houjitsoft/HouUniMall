package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseArticleGroupDTO;
import com.houjiang.base.entity.BaseArticleGroupEntity;
import com.houjiang.base.mapper.BaseArticleGroupMapper;
import com.houjiang.base.service.BaseArticleGroupService;
import com.houjiang.base.vo.index.WebIndexArticleGroupVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName BaseArticleGroupServiceImpl
 * @Description 网站初始化内容分组表
 * @Author Amos
 * @Date 2023-06-07 14:50:02
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseArticleGroupServiceImpl extends ServiceImpl<BaseArticleGroupMapper, BaseArticleGroupEntity>
        implements BaseArticleGroupService {

    /**
     * 查询所有文章分组信息。
     *
     * @return 查询结果，包含所有文章分组的信息。
     */
    @Override
    public ResponseResult queryArticleGroup() {
        // 通过基础Mapper查询所有文章分组
        // 查询所有的组
        List<WebIndexArticleGroupVO> articleGroups = this.baseMapper.selectAllGroup();

        // 返回查询结果的成功响应
        return ResponseResult.success(articleGroups);
    }

    /**
     * 更新文章分组信息。
     *
     * @param baseArticleGroupReq 包含文章分组更新信息的请求对象。
     * @return 如果更新成功，返回成功响应；如果更新失败或令牌非法，返回失败响应。
     */
    @Override
    public ResponseResult updateArticleGroup(BaseArticleGroupDTO baseArticleGroupReq) {
        // 通过令牌解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseArticleGroupReq.getToken());
        // 检查用户信息是否有效
        if (null != userInfo) {
            // 插入新的文章分组信息
            this.baseMapper.insertArticleGroup(baseArticleGroupReq);
            // 返回操作成功的响应
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        // 如果令牌无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 删除指定文章分组。
     *
     * @param baseArticleGroupReq 包含待删除文章分组ID的请求对象。
     * @return 如果删除成功，返回成功响应；如果删除失败或令牌非法，返回失败响应。
     */
    @Override
    public ResponseResult deleteArticleGroup(BaseArticleGroupDTO baseArticleGroupReq) {
        // 通过令牌解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(baseArticleGroupReq.getToken());
        // 检查用户信息是否有效
        if (null != userInfo) {
            // 通过ID删除指定的文章分组
            this.baseMapper.deleteById(baseArticleGroupReq.getArticleGroupId());
            // 返回删除成功的响应
            return ResponseResult.success();
        }
        // 如果令牌无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }
}
