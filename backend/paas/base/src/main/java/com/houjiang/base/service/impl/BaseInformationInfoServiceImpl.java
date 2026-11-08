package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseInformationInfoDTO;
import com.houjiang.base.entity.BaseInformationClassifyEntity;
import com.houjiang.base.entity.BaseInformationInfoEntity;
import com.houjiang.base.mapper.BaseInformationClassifyMapper;
import com.houjiang.base.mapper.BaseInformationInfoMapper;
import com.houjiang.base.service.BaseInformationInfoService;
import com.houjiang.base.vo.BaseInformationInfoVO;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资讯文章表(BaseInformationInfo)表服务实现类
 *
 * @author Amos
 * @since 2024-01-18 15:31:10
 */
@Service
public class BaseInformationInfoServiceImpl extends ServiceImpl<BaseInformationInfoMapper, BaseInformationInfoEntity>
        implements BaseInformationInfoService {

    @Resource
    private BaseInformationClassifyMapper informationClassifyMapper;

    /**
     * 根据平台查询信息列表。
     *
     * @param param 查询参数，包含分类ID、页码和每页数量等。
     * @return 返回查询结果，包含分页信息和信息列表。
     */
    @Override
    public ResponseResult queryByPlatform(BaseInformationInfoDTO param) {
        // 如果分类ID不为空，则查询该分类及其子分类的ID列表
        if (param.getClassifyId() != null) {
            List<Integer> classifyIdList = new ArrayList<>();
            // 如果选择的是一级分类，查询一级分类及下级分类id
            BaseInformationClassifyEntity classify = informationClassifyMapper.selectOne(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                    .eq(BaseInformationClassifyEntity::getClassifyId, param.getClassifyId())
                    .eq(BaseInformationClassifyEntity::getClassifyLevel, 1));
            if (classify != null) {
                List<BaseInformationClassifyEntity> list = informationClassifyMapper.selectList(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                        .eq(BaseInformationClassifyEntity::getClassifyParentId, param.getClassifyId()));
                classifyIdList = list.stream().map(BaseInformationClassifyEntity::getClassifyId).collect(Collectors.toList());
            }
            classifyIdList.add(param.getClassifyId());
            param.setClassifyIdList(classifyIdList);
        }
        // 分页查询信息
        // 分页查询
        Page<BaseInformationInfoEntity> page = new Page<>(param.getPage(), param.getLimit());
        IPage<BaseInformationInfoEntity> result = this.baseMapper.queryByPlatform(page, param);
        // 如果查询到记录，拼接分类名称
        if (!result.getRecords().isEmpty()) {
            for (BaseInformationInfoEntity info : result.getRecords()) {
                if (info.getClassifyParentId() != 0) {
                    // 得到二级分类名称
                    String classifyName = informationClassifyMapper.selectOne(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                            .eq(BaseInformationClassifyEntity::getClassifyId, info.getClassifyParentId())).getClassifyName();
                    info.setClassifyName(classifyName + "/" + info.getClassifyName());
                }
            }
        }
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 根据信息ID查询平台上的信息详情。
     *
     * @param param 查询参数，包含信息ID。
     * @return 返回查询结果，包含信息的详细信息。
     */
    @Override
    public ResponseResult queryByPlatformByInformationId(BaseInformationInfoDTO param) {
        // 查询指定信息的详细信息
        //查询当前咨询的游览量
        BaseInformationInfoEntity baseInformationInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseInformationInfoEntity>()
                .eq(BaseInformationInfoEntity::getInformationId, param.getInformationId()));

        BaseInformationInfoVO entity = new BaseInformationInfoVO();
        if (null != baseInformationInfoEntity) {
            BeanUtils.copyProperties(baseInformationInfoEntity, entity);
            // 如果信息有封面图片，解析并设置封面图片列表
            if (baseInformationInfoEntity.getInformationCover() != null) {
                List<String> list = JSONObject.parseArray(baseInformationInfoEntity.getInformationCover(), String.class);
                entity.setInformationCoverList(list);
            }
        }
        return ResponseResult.success(entity);
    }
    /**
     * 添加或更新基础信息。
     *
     * @param param 包含基础信息的数据传输对象。
     * @return 如果操作成功，返回带有结果的响应对象；否则，返回失败的响应对象。
     */
    @Override
    public ResponseResult add(BaseInformationInfoDTO param) {

        // 创建一个新的实体对象
        BaseInformationInfoEntity entity = new BaseInformationInfoEntity();
        // 将DTO的属性复制到实体对象
        BeanUtils.copyProperties(param, entity);

        // 初始化标志变量
        Boolean flag;

        // 处理信息封面图片列表
        if (param.getInformationCoverList() != null && param.getInformationCoverList().size() > 0 ) {
            entity.setInformationCover(JSON.toJSONString(param.getInformationCoverList()));
        } else {
            entity.setInformationCover(null);
        }

        // 如果信息ID为空，则为新增信息；否则为更新信息
        if (param.getInformationId() == null) {
            // 设置创建时间和更新时间
            entity.setCreateTime(DateUtils.getNowDateToDf());
            entity.setUpdateTime(DateUtils.getNowDateToDf());
            // 初始化阅读量为设置的值
            entity.setPageView(entity.getPageViewSet());
            // 尝试插入实体到数据库，并根据影响行数判断操作是否成功
            flag = this.baseMapper.insert(entity) > 0;
        } else {
            // 更新信息的更新时间
            entity.setUpdateTime(DateUtils.getNowDateToDf());
            // 如果设置了阅读量，更新阅读量
            if(null != entity.getPageViewSet()){
                // 查询当前信息的详细情况
                BaseInformationInfoEntity baseInformationInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseInformationInfoEntity>()
                        .eq(BaseInformationInfoEntity::getInformationId, entity.getInformationId()));
                // 修改前的设置阅读量
                Integer pageViewSet = baseInformationInfoEntity.getPageViewSet();
                // 修改前的实际阅读量
                Integer pageView = baseInformationInfoEntity.getPageView();
                // 前端传入的新设置阅读量
                Integer pageViewSetNew = entity.getPageViewSet();
                // 如果新的设置阅读量与旧的不同，则更新阅读量
                if(null != pageViewSet && null != pageViewSetNew &&  !pageViewSet.equals(pageViewSetNew)){
                    Integer updatepageView = pageViewSetNew  - pageViewSet ;
                    entity.setPageViewSet(pageViewSet + updatepageView );
                    entity.setPageView(pageView + updatepageView);
                }
            }
            // 尝试更新数据库中的实体，并根据影响行数判断操作是否成功
            flag = this.baseMapper.updateById(entity) > 0;
        }
        // 根据操作是否成功，返回相应的响应结果
        return flag ? ResponseResult.success(entity) : ResponseResult.fail();
    }

    /**
     * 更新信息的阅读量。
     *
     * @param param 包含需要更新阅读量的信息ID和新的阅读量的数据传输对象。
     * @return 如果操作成功，返回带有结果的响应对象；否则，返回失败的响应对象。
     */
    @Override
    public ResponseResult updatePageView(BaseInformationInfoDTO param) {
        // 根据信息ID查询当前信息的详细情况
        // 查询当前咨询的详细
        BaseInformationInfoEntity baseInformationInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseInformationInfoEntity>()
                .eq(BaseInformationInfoEntity::getInformationId, param.getInformationId()));
        // 创建一个新的实体对象用于更新
        BaseInformationInfoEntity entity = new BaseInformationInfoEntity();
        // 将DTO的属性复制到实体对象
        BeanUtils.copyProperties(param, entity);
        // 计算平台设置的阅读量
        // 前端传入的游览量 - 自然游览量 = 平台设置的游览量
        Integer pageViewSet = param.getPageView() - baseInformationInfoEntity.getPageViewNatural();
        // 确保平台设置的阅读量不小于0
        if(pageViewSet < 0){
            pageViewSet = 0;
        }
        // 设置平台设置的阅读量
        entity.setPageViewSet(pageViewSet);
        // 尝试更新数据库中的实体，并根据影响行数判断操作是否成功
        Boolean flag = this.baseMapper.updateById(entity) > 0;
        // 根据操作是否成功，返回相应的响应结果
        return flag ? ResponseResult.success() : ResponseResult.fail();
    }

    /**
     * 删除指定信息。
     *
     * @param param 包含待删除信息ID的数据传输对象。
     * @return 删除操作的结果，成功返回带有结果的响应对象，失败返回失败的响应对象。
     */
    /**
     * 删除指定的基础信息。
     *
     * @param param 包含待删除信息的ID的数据传输对象。
     * @return 如果删除成功，返回带有结果的响应对象；否则，返回失败的响应对象。
     */
    @Override
    public ResponseResult delete(BaseInformationInfoDTO param) {
        // 根据信息ID删除信息
        // 删除咨询
        this.baseMapper.deleteInformationInfoById(param.getInformationId());
        // 返回删除成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 用户查询所有信息。
     *
     * @return 包含所有信息的列表的响应结果。
     */
    @Override
    public ResponseResult userQueryInformationInfo() {
        List<BaseInformationInfoVO>  baseInformationInfoVOS = new ArrayList<>();
        List<BaseInformationInfoEntity> baseInformationInfoEntities = this.baseMapper.userQueryInformationInfo();
        if(null != baseInformationInfoEntities && baseInformationInfoEntities.size() > 0){
            for (BaseInformationInfoEntity baseInformationInfoEntity : baseInformationInfoEntities) {
                BaseInformationInfoVO entity = new BaseInformationInfoVO();
                BeanUtils.copyProperties(baseInformationInfoEntity,entity);
                List<String> list = JSONObject.parseArray(baseInformationInfoEntity.getInformationCover(), String.class);
                entity.setInformationCoverList(list);
                baseInformationInfoVOS.add(entity);
            }
        }
        return ResponseResult.success(baseInformationInfoVOS);
    }

    /**
     * 根据ID查询用户指定的信息。
     *
     * @param param 包含待查询信息ID和其他查询条件的数据传输对象。
     * @return 包含指定信息的列表的响应结果。
     */
    @Override
    public ResponseResult userQueryInformationInfoById(BaseInformationInfoDTO param) {
        if (param.getClassifyId() != null) {
            List<Integer> classifyIdList = new ArrayList<>();
            // 如果选择的是一级分类，查询一级分类及下级分类id
            BaseInformationClassifyEntity classify = informationClassifyMapper.selectOne(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                    .eq(BaseInformationClassifyEntity::getClassifyId, param.getClassifyId())
                    .eq(BaseInformationClassifyEntity::getClassifyLevel, 1));
            if (classify != null) {
                List<BaseInformationClassifyEntity> list = informationClassifyMapper.selectList(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                        .eq(BaseInformationClassifyEntity::getClassifyParentId, param.getClassifyId()));
                classifyIdList = list.stream().map(BaseInformationClassifyEntity::getClassifyId).collect(Collectors.toList());
            }
            classifyIdList.add(param.getClassifyId());
            param.setClassifyIdList(classifyIdList);
            param.setIsAll(2);
        }else{
            param.setIsAll(1);
        }
        // 分页查询
        Page<BaseInformationInfoEntity> page = new Page<>(param.getPage(), param.getLimit());
        IPage<BaseInformationInfoEntity> result = this.baseMapper.userQueryInformationInfoById(page, param);
        if (!result.getRecords().isEmpty()) {
            for (BaseInformationInfoEntity info : result.getRecords()) {
                if(info.getInformationCover() != null){
                    List<String> list = JSONObject.parseArray(info.getInformationCover(), String.class);
                    info.setInformationCoverList(list);
                }
            }
        }
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 根据信息ID查询信息详情。
     * @param param 查询参数，包含信息ID。
     * @return 返回查询到的信息详情。
     */
    @Override
    public ResponseResult queryInformationInfo(BaseInformationInfoDTO param) {
        // 根据信息ID查询信息实体
        //查询当前咨询的详细
        BaseInformationInfoEntity baseInformationInfoEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseInformationInfoEntity>()
                .eq(BaseInformationInfoEntity::getInformationId, param.getInformationId()));
        // 更新信息的浏览量
        //更新游览量
        this.baseMapper.updateInformationPageViewById(param);
        // 返回查询结果
        return ResponseResult.success(baseInformationInfoEntity);
    }

    /**
     * 查询指定分类下下一篇文章的信息。
     * @param param 查询参数，包含当前文章的分类ID和文章ID。
     * @return 返回包含下一篇文章、上一篇文章和底部文章列表的结果。
     */
    @Override
    public ResponseResult queryInformationInfoNextChapter(BaseInformationInfoDTO param) {
        Map<String, Object> resultMap = new HashMap<>();

        // 如果分类ID不为空
        if (param.getClassifyId() != null) {
            List<Integer> classifyIdList = new ArrayList<>();
            // 查询指定分类ID是否为一级分类，并获取其下所有子分类ID
            // 如果选择的是一级分类，查询一级分类及下级分类id
            BaseInformationClassifyEntity classify = informationClassifyMapper.selectOne(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                    .eq(BaseInformationClassifyEntity::getClassifyId, param.getClassifyId())
                    .eq(BaseInformationClassifyEntity::getClassifyLevel, 1));
            if (classify != null) {
                List<BaseInformationClassifyEntity> list = informationClassifyMapper.selectList(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                        .eq(BaseInformationClassifyEntity::getClassifyParentId, param.getClassifyId()));
                classifyIdList = list.stream().map(BaseInformationClassifyEntity::getClassifyId).collect(Collectors.toList());
            }
            // 将当前分类ID也加入列表
            classifyIdList.add(param.getClassifyId());
            // 设置分类ID列表参数
            param.setClassifyIdList(classifyIdList);
            // 根据参数查询信息列表
            //查询当前分类下所有咨询
            List<BaseInformationInfoEntity> result = this.baseMapper.queryInformationInfoBottom(param);
            List<BaseInformationInfoEntity> bottomResult = new ArrayList<>();
            BaseInformationInfoEntity previous = new BaseInformationInfoEntity();
            BaseInformationInfoEntity next = new BaseInformationInfoEntity();
            Boolean flag = false;
            // 处理查询结果，获取上一篇、下一篇和底部文章列表
            if(null != result && result.size() > 0){
                for (int i = 0; i < result.size(); i++) {
                    // 解析信息封面图片列表
                    //解析咨询图片
                    List<String> list = JSONObject.parseArray(result.get(i).getInformationCover(), String.class);
                    result.get(i).setInformationCoverList(list);

                    // 当前文章匹配时，记录上一篇和下一篇信息
                    //当咨询id相等时 获取当前咨询的上一篇 和下一 篇
                    if(result.get(i).getInformationId().equals(param.getInformationId())){
                        // 上一篇
                        if( i-1 >=0 ){
                            previous = result.get(i-1);
                        }
                        // 下一篇
                        if( i+1 <= (result.size() - 1)){
                            next = result.get(i+1);
                        }
                        flag = true;
                    }

                    // 封装底部6条信息
                    //封装底部6条数据
                    if(i <= 5){
                        bottomResult.add(result.get(i));
                    }
                    // 底部信息和上下文信息都已获取，结束循环
                    //底部6条数据封装完成  并且 当前咨询 的 上一篇 和 下一篇 也封装完成 退出循环
                    if(i == 5 && flag){
                        break;
                    }
                }
            }
            // 构建返回结果
            resultMap.put("bottom", bottomResult);
            resultMap.put("previous", previous);
            resultMap.put("next", next);
        }
        // 返回处理后的结果
        return ResponseResult.success(resultMap);
    }
}
