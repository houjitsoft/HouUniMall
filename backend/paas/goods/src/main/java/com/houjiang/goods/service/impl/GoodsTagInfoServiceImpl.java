package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.goods.entity.GoodsTagInfoEntity;
import com.houjiang.goods.mapper.GoodsTagInfoMapper;
import com.houjiang.goods.service.GoodsTagInfoService;
import com.houjiang.goods.vo.CommunityArticleTagVO;
import com.houjiang.goods.vo.CommunityTopicVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class GoodsTagInfoServiceImpl extends ServiceImpl<GoodsTagInfoMapper, GoodsTagInfoEntity>
    implements GoodsTagInfoService{
    /**
     * 更新社区信息。
     * 该方法用于根据提供的Map中的数据更新社区的相关信息。通过调用baseMapper的updateCommunity方法实现数据库中的信息更新。
     * @param map 包含需要更新的社区信息的Map对象。键值对形式指定要更新的字段及其对应的新值。
     * @return 返回一个ResponseResult对象，表示操作的结果。如果操作成功，返回码为OPERATION_SUCCESSFUL。
     */
    @Override
    public ResponseResult updateCommunity(Map<String, Object> map) {
        // 调用baseMapper的updateCommunity方法更新社区信息
        this.baseMapper.updateCommunity(map);
        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 插入社区信息。
     * 该方法用于向数据库中插入新的社区信息。通过传递一个Map对象，可以灵活地插入各种类型的数据。
     * 使用了BaseMapper的insertCommunity方法来执行实际的插入操作，利用了MyBatis的映射机制。
     *
     * @param map 包含社区信息的Map对象。键值对的形式提供了灵活的数据传递方式。
     * @return ResponseResult 对象，表示操作的结果。如果操作成功，会返回一个成功标志。
     */
    @Override
    public ResponseResult insertCommunity(Map<String, Object> map) {
        // 调用BaseMapper的insertCommunity方法插入社区信息
        this.baseMapper.insertCommunity(map);
        // 返回成功结果，操作码为OPERATION_SUCCESSFUL，表示插入操作成功
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 删除社区话题
     * 该方法用于根据提供的标签ID列表删除相关的话题标签。它首先从传入的Map对象中提取标签ID列表，
     * 然后使用这些ID来构建一个查询包装器，最后通过调用基本Mapper的delete方法来删除匹配的所有话题标签。
     * 如果标签ID列表为空或不存在，则不执行删除操作。
     *
     * @param map 一个包含标签ID列表的Map对象，其中键为"tagIdList"。
     * @return 返回一个表示操作结果的ResponseResult对象，如果操作成功，则返回OPERATION_SUCCESSFUL代码。
     */
    @Override
    public ResponseResult deleteCommunity(Map<String, Object> map) {
        // 从传入的Map中提取标签ID列表
        List<Integer> list = (List<Integer>) map.get("tagIdList");

        // 检查标签ID列表是否为空，如果不为空，则执行删除操作
        if (!CollectionUtils.isEmpty(list)) {
            // 使用LambdaQueryWrapper构建查询条件，指定根据标签ID列表进行删除
            this.baseMapper.delete(new LambdaQueryWrapper<GoodsTagInfoEntity>()
                    .in(GoodsTagInfoEntity::getTagId, list));
        }

        // 返回表示操作成功的ResponseResult对象
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据传入的参数查询社区话题。
     * 该方法通过调用baseMapper的selectCommunity方法，使用分页查询方式获取社区话题列表。
     * 查询结果将被包装成ResponseResult对象返回，其中包含分页信息。
     *
     * @param map 一个Map对象，包含查询所需的参数。这些参数可以影响查询的结果。
     * @return 返回一个ResponseResult对象，其中包含查询结果的分页信息。
     */
    @Override
    public ResponseResult selectCommunity(Map<String, Object> map) {
        // 使用Query对象包装分页信息，并结合map中的查询参数，调用baseMapper进行查询
        IPage<CommunityTopicVO> page = this.baseMapper.selectCommunity(new Query<CommunityTopicVO>().getPage(map), map);

        // 将查询结果的分页信息包装成PageUtils对象，并将该对象作为成功响应的结果返回
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据给定的参数查询话题的总帖子数和阅读数。
     * 此方法主要用于从数据库中检索特定话题标签下的帖子总数和阅读总数。
     *
     * @param param 一个Map对象，其中可能包含用于查询的参数。
     *              特别地，如果param中包含键"tagId"，其值将被用作查询特定话题标签的条件。
     * @return 如果查询成功，返回包含查询结果的ResponseResult对象；
     *         如果param为null，返回一个表示失败的ResponseResult对象，其中包含错误代码0和错误消息"参数不能为null"。
     */
    @Override
    public ResponseResult queryCount(Map<Object, Object> param) {
        // 检查参数是否为空
        if(null != param ){
            // 从参数Map中提取tagId，如果存在
            Integer tagId = (Integer) param.get("tagId") ;
            // 调用baseMapper的queryCount方法查询帖子总数和阅读总数，并将结果存储在一个Map中
            Map<String,Object> map =  this.baseMapper.queryCount(tagId);
            // 返回成功结果，包含查询到的数据
            return ResponseResult.success(map);
        }
        // 如果参数为空，返回失败结果，指出参数不能为空
        return ResponseResult.fail(0, "参数不能为null");
    }

    /**
     * 查询社区话题标签
     *
     * 本方法旨在通过查询数据库，获取社区文章的话题标签列表。如果查询结果为空，
     * 则会返回一个空的标签列表，确保方法的调用者总是收到一个列表对象，而不是null，
     * 以此提高代码的健壮性和易用性。
     *
     * @param param 查询参数，以Map形式传递，包含查询所需的条件。
     * @return ResponseResult 对象，其中包含查询成功时的话题标签列表。
     */
    @Override
    public ResponseResult queryTags(Map<Object, Object> param) {
        // 通过baseMapper查询话题标签列表
        List<CommunityArticleTagVO> tagList = this.baseMapper.queryTag(param);
        // 如果查询结果为空，则初始化一个空的标签列表，避免返回null
        if (CollectionUtils.isEmpty(tagList)) {
            tagList = Lists.newArrayList();
        }
        // 返回查询成功的标志及标签列表
        return ResponseResult.success(tagList);
    }

    /**
     * 查询社区话题
     * 该方法用于查询社区话题，
     * 。
     *
     * @param map 传递给查询的参数映射表，目前未使用。
     * @return 返回查询结果，
     */
    @Override
    public ResponseResult queryCommunity(Map<String, String> map) {
        // 查询最新的6个活跃话题
        List<GoodsTagInfoEntity> params = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsTagInfoEntity>()
                .eq(GoodsTagInfoEntity::getIsActive, CommonType.commonStatus.YES.getCode())
                .orderByAsc(GoodsTagInfoEntity::getTagId).last("limit 6"));
        return ResponseResult.success(params);

    }

}




