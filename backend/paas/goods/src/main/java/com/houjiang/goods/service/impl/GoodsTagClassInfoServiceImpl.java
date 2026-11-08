package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.goods.entity.GoodsTagClassInfoEntity;
import com.houjiang.goods.entity.GoodsTagInfoEntity;
import com.houjiang.goods.mapper.GoodsTagClassInfoMapper;
import com.houjiang.goods.mapper.GoodsTagInfoMapper;
import com.houjiang.goods.service.GoodsTagClassInfoService;
import com.houjiang.goods.vo.CommunityArticleTagClassVO;
import com.houjiang.goods.vo.CommunityArticleTagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class GoodsTagClassInfoServiceImpl extends ServiceImpl<GoodsTagClassInfoMapper, GoodsTagClassInfoEntity>
    implements GoodsTagClassInfoService{

    @Autowired
    private GoodsTagInfoMapper goodsTagInfoMapper;
    /**
     * 删除社区话题
     *
     * @param map 包含话题分类ID的映射对象
     * @return 返回操作结果的响应对象
     *
     * 本方法用于删除指定分类下的社区话题。首先，从传入的映射对象中提取分类ID，
     * 然后通过基本映射器删除该分类。接着，使用商品标签信息映射器删除与该分类相关的所有商品标签信息。
     * 最后，返回一个表示操作成功的响应结果。
     */
    @Override
    public ResponseResult deleteCommunityTopic(Map<String, Object> map) {
        // 提取并转换分类ID
        Integer classId = Integer.valueOf(map.get("classId").toString());

        // 删除分类
        this.baseMapper.deleteById(classId);

        // 删除与分类相关的所有商品标签信息
        goodsTagInfoMapper.delete(new LambdaQueryWrapper<GoodsTagInfoEntity>().eq(GoodsTagInfoEntity::getClassId,classId));

        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 插入社区话题分类。
     *
     * @param map 包含话题分类名称的映射。
     * @return 如果插入成功，返回操作成功的响应结果；如果插入失败，返回操作失败的响应结果。
     */
    @Override
    public ResponseResult insertCommunityTopic(Map<String, Object> map) {
        // 获取映射中的分类名称
        //获取分类名称
        String className = map.get("className").toString();

        // 如果分类名称不为空
        if (null != className){
            // 创建新的商品标签类信息实体
            GoodsTagClassInfoEntity entity=new GoodsTagClassInfoEntity();
            // 设置实体的分类名称
            entity.setClassName(className);
            // 通过基本Mapper插入实体到数据库
            this.baseMapper.insert(entity);
            // 返回操作成功的响应结果
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }

        // 如果分类名称为空，返回操作失败的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 根据传入的参数查询商品标签分类信息。
     *
     * @param map 包含分页信息和查询条件的参数映射表。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult selectCommunityTopic(Map<String, Object> map) {
        // 使用Lambda表达式和分页插件进行条件查询，初始化分页对象
        IPage<GoodsTagClassInfoEntity> page = this.page(
                new Query<GoodsTagClassInfoEntity>().getPage(map),
                new LambdaQueryWrapper<GoodsTagClassInfoEntity>()
        );

        // 将查询结果包装成响应对象返回
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询标签类别的方法
     *
     * 通过调用底层Mapper查询所有的标签类别，如果查询结果为空，则初始化一个空列表返回。
     * 该方法主要用于获取标签类别的列表，以便在前端展示或进行进一步的业务处理。
     *
     * @param param 一个Map对象，用于传递查询参数。当前实现中未使用此参数，留待后续扩展。
     * @return ResponseResult 对象，包含查询结果的列表。如果查询结果为空，则列表为空。
     */
    @Override
    public ResponseResult queryTagClass(Map<Object, Object> param) {
        // 调用baseMapper查询标签类别
        List<CommunityArticleTagClassVO> tagClassList = this.baseMapper.queryTagClass();
        List<CommunityArticleTagClassVO> result = new ArrayList<>();
        for (CommunityArticleTagClassVO communityArticleTagClassVO : tagClassList) {
            Map<Object, Object> map = new HashMap<>();
            map.put("classId",communityArticleTagClassVO.getClassId());
            List<CommunityArticleTagVO> tagList = goodsTagInfoMapper.queryTag(map);
            if (!CollectionUtils.isEmpty(tagList)) {
                result.add(communityArticleTagClassVO);
            }
        }
        tagClassList = result;
        // 返回查询结果，包含在一个成功的ResponseResult对象中
        return ResponseResult.success(tagClassList);
    }
}




