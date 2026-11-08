package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.GoodsArticleStarEntity;
import com.houjiang.goods.mapper.GoodsArticleInfoMapper;
import com.houjiang.goods.mapper.GoodsArticleStarMapper;
import com.houjiang.goods.service.GoodsArticleStarService;
import com.houjiang.goods.vo.StarredArticleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsArticleStarServiceImpl
 * @Description  社区心得收藏表
 * @Author Amos
 * @Date 2023-07-18 15:03:07
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsArticleStarServiceImpl extends ServiceImpl<GoodsArticleStarMapper, GoodsArticleStarEntity>
        implements GoodsArticleStarService {

    @Autowired
    private GoodsArticleInfoMapper goodsArticleInfoMapper;

    /**
     * 对文章进行收藏或取消收藏操作。
     *
     * @param param 包含操作所需参数的Map对象，其中必须包含token和starFlag。
     * @return 根据操作结果返回相应的ResponseResult对象。
     */
    @Override
    public ResponseResult starArticle(Map<Object, Object> param) {
        // 通过token获取用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息为空，则返回认证失败的响应
        if(null == ui){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 将用户ID添加到参数中，用于后续的收藏操作
        param.put("starUser",ui.getUserId());
        // 获取收藏操作的标志（1为收藏，其他为取消收藏）
        int starFlag = (int) param.get("starFlag");
        if (1 == starFlag) {
            // 检查当前文章是否已被收藏
            //判断当前用户是否已经收藏
            Integer count = this.baseMapper.queryExistForStar(param);
            // 如果已收藏，则返回成功响应，提示已收藏
            if (count > 0) {
                return ResponseResult.success("已收藏");
            }
            // 如果未收藏，则插入收藏记录
            //插入收藏记录表
            this.baseMapper.insertStar(param);
        } else {
            // 取消收藏操作
            //删除收藏记录表
            this.baseMapper.deleteStar(param);
        }
        // 更新文章的收藏数
        //更新帖子收藏数
        goodsArticleInfoMapper.updateStarNum(param);
        // 返回操作成功的响应
        return ResponseResult.success();
    }

    /**
     * 查询用户的收藏文章列表。
     *
     * @param param 包含查询所需参数的Map对象，其中必须包含token。
     * @return 根据查询结果返回相应的ResponseResult对象。
     */
    @Override
    public ResponseResult queryStarList(Map<String, Object> param) {
        try {
            // 通过token获取用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
            // 如果用户信息为空，则返回认证失败的响应
            if(null == ui){
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
            } else {
                // 将用户ID添加到参数中，用于后续的查询
                param.put("userId", ui.getUserId());
            }
            // 执行查询，获取收藏文章的分页结果
            IPage<StarredArticleVO> page = this.baseMapper.getStarredArticleList(new Query<String>().getPage(param),param);
            // 处理分页结果，封装成PageUtils对象
            PageUtils result = PageUtils.getPage(page);
            if(!page.getRecords().isEmpty()){
                // 提取所有收藏文章的ID
                List<String> starredArticleIds = page.getRecords().stream().map(StarredArticleVO::getArticleId).collect(Collectors.toList());
                // 根据文章ID查询每篇文章的详细信息
                List<StarredArticleVO> resultList = new ArrayList<>();
                for (String articleId : starredArticleIds) {
                    param.put("articleId", articleId);
                    StarredArticleVO article = goodsArticleInfoMapper.queryStarArticleInfo(param);
                    resultList.add(article);
                }
                // 计算总页数，确保分页没有余数
                Integer pageSize = Integer.parseInt(page.getSize()+"");
                result.setTotalCount((result.getTotalCount() % pageSize) == 0 ? result.getTotalCount() / pageSize : result.getTotalCount() / pageSize + 1);
                // 更新查询结果列表
                result.setList(resultList);
            }
            // 返回查询成功的响应，包含分页结果
            return ResponseResult.success(result);
        } catch (Exception e) {
            // 如果发生异常，则返回系统错误的响应
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }
}
