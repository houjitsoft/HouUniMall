package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseArticleBaseDTO;
import com.houjiang.base.entity.BaseArticleBaseEntity;
import com.houjiang.base.mapper.BaseArticleBaseMapper;
import com.houjiang.base.service.BaseArticleBaseService;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseArticleBaseServiceImpl
 * @Description  网站初始化内容设置
 * @Author Amos
 * @Date 2023-06-07 15:17:52
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseArticleBaseServiceImpl extends ServiceImpl<BaseArticleBaseMapper, BaseArticleBaseEntity>
        implements BaseArticleBaseService {


    /**
     * 根据文章组ID查询文章组详情。
     * @param articleGroupId 文章组ID。
     * @return 返回查询到的文章组详情。
     */
    @Override
    public ResponseResult queryArticleGroupDetail(Integer articleGroupId) {
        // 根据文章组ID查询文章列表
        List<BaseArticleBaseEntity> articletitle = this.baseMapper.selectList(new LambdaQueryWrapper<BaseArticleBaseEntity>()
                             .eq(BaseArticleBaseEntity::getArticleGroupId, articleGroupId));
        return ResponseResult.success(articletitle);
    }

    /**
     * 根据文章查询详情。
     * @param param 查询参数，包含文章ID。
     * @return 返回查询到的文章详情。
     */
    @Override
    public ResponseResult articleQueryDetail(BaseArticleBaseDTO param) {
        // 根据文章ID查询文章详情
        BaseArticleBaseEntity baseArticleBaseEntity = this.baseMapper.selectById(param.getArticleId());
        return ResponseResult.success(baseArticleBaseEntity);
    }

    /**
     * 删除文章。
     * @param param 删除参数，包含文章ID和用户令牌。
     * @return 返回删除操作的结果。
     */
    @Override
    public ResponseResult articleDelete(BaseArticleBaseDTO param) {
        // 解析用户令牌，获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            // 根据文章ID删除文章
            // 删除
            this.baseMapper.deleteById(param.getArticleId());
            return ResponseResult.success();
        }
        // 如果用户信息为空，返回令牌非法错误信息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 更新文章信息。
     *
     * @param param 包含文章详细信息的数据传输对象，包括文章描述等。
     * @return 如果更新成功，返回成功的响应结果；如果更新失败或令牌非法，返回失败的响应结果。
     */
    @Override
    public ResponseResult articleUpdate(BaseArticleBaseDTO param) {
        // 通过令牌获取用户信息，用于验证用户身份。
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息不为空，则继续处理更新操作。
        if (null != userInfo) {
            // 设置文章的添加时间。
            param.setArticleAddTime(new Date());
            try {
                // 尝试将文章描述从Base64编码解码，以获取原始文本。
                String data = param.getArticleDesc();
                String fromBase64 = Base64Utils.getFromBase64(data);
                // 更新解码后的文章描述。
                param.setArticleDesc(fromBase64);
            } catch (Exception e) {
                // 如果解码失败，返回失败的响应结果。
                e.printStackTrace();
                return ResponseResult.fail(BizCodeEnume.DECODING_FAILED.getCode(), BizCodeEnume.DECODING_FAILED.getMsg());
            }
            // 插入或更新文章信息。
            // 存在就更新不存在就添加
            this.baseMapper.articleInsert(param);
            // 返回成功的响应结果。
            return ResponseResult.success();
        }
        // 如果用户信息为空，表示令牌验证失败，返回失败的响应结果。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }

    /**
     * 查询文章基础信息。
     *
     * @param param 包含查询条件的数据传输对象，用于筛选文章。
     * @return 如果查询成功，返回包含查询结果的响应结果；如果查询失败或令牌非法，返回失败的响应结果。
     */
    @Override
    public ResponseResult queryArticleBase(BaseArticleBaseDTO param) {
        // 通过令牌获取用户信息，用于验证用户身份。
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        try {
            // 如果用户信息不为空，则继续处理查询操作。
            if (null != userInfo) {
                // 将查询条件对象转换为Map，以便进行查询。
                // 将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 执行分页查询。
                // 分页查询数据
                IPage<BaseArticleBaseEntity> page = this.baseMapper.queryArticleBase(new Query<BaseArticleBaseEntity>().getPage(data), param);
                // 返回包含查询结果的响应结果。
                return ResponseResult.success(PageUtils.getPage(page));
            }
        } catch (IllegalAccessException e) {
            // 如果转换失败，打印异常信息。
            e.printStackTrace();
        }
        // 如果查询失败或令牌非法，返回失败的响应结果。
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }
}
