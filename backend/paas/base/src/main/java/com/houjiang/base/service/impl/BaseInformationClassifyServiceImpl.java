package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseInformationClassifyDTO;
import com.houjiang.base.entity.BaseInformationClassifyEntity;
import com.houjiang.base.mapper.BaseInformationClassifyMapper;
import com.houjiang.base.mapper.BaseInformationInfoMapper;
import com.houjiang.base.service.BaseInformationClassifyService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资讯分类表(BaseInformationClassify)表服务实现类
 *
 * @author Amos
 * @since 2024-01-17 13:50:42
 */
@Service
public class BaseInformationClassifyServiceImpl extends ServiceImpl<BaseInformationClassifyMapper, BaseInformationClassifyEntity>
        implements BaseInformationClassifyService {

    @Resource
    private BaseInformationInfoMapper baseInformationInfoMapper;

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    /**
     * 查询所有信息分类，包括父分类和子分类。
     * 该方法用于获取系统中的所有信息分类，以供前端展示使用。
     * 分类数据按照排序顺序获取，并缓存到Redis中，以提高数据访问效率。
     *
     * @return ResponseResult 包含所有信息分类的列表。
     */
    @Override
    public ResponseResult queryAllClassify() {
        // 查询显示状态为启用的父分类，并按排序顺序升序排列
        // 查询所有分类
        List<BaseInformationClassifyEntity> rootMenus = this.baseMapper.selectList(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                .eq(BaseInformationClassifyEntity::getClassifyShowStatus, CommonType.commonStatus.YES.getCode())
                .orderByAsc(BaseInformationClassifyEntity::getClassifySort));
        // 获取所有父分类对应的子分类
        // 得到子分类
        List<BaseInformationClassifyEntity> menuLists = getMenuList(rootMenus);

        // 将分类数据缓存到Redis中，过期时间为1个月
        // 存入缓存
        redisCache.setCacheObject(HouConfig.HOU_INFORMATION_CLASSIFY_PC_ALL, JsonUtils.toString(menuLists));
        redisCache.expire(HouConfig.HOU_INFORMATION_CLASSIFY_PC_ALL, RedisUtil.EXRP_MONTH, java.util.concurrent.TimeUnit.SECONDS);
        return ResponseResult.success(menuLists);
    }

    /**
     * 根据平台类型查询所有信息分类，包括父分类和子分类。
     * 该方法用于根据用户平台类型获取所有信息分类，以供前端展示使用。
     * 分类数据按照排序顺序获取，并缓存到Redis中，以提高数据访问效率。
     *
     * @param dto 用户令牌信息，包含平台类型信息。
     * @return ResponseResult 包含所有信息分类的列表。
     */
    @Override
    public ResponseResult queryAllClassifyByPlatform(UserTokenBeanDTO dto) {
        // 查询显示状态为启用的父分类，并按排序顺序升序排列
        // 查询所有分类
        List<BaseInformationClassifyEntity> rootMenus = this.baseMapper.selectList(new LambdaQueryWrapper<BaseInformationClassifyEntity>()
                .eq(BaseInformationClassifyEntity::getClassifyShowStatus, CommonType.commonStatus.YES.getCode())
                .orderByAsc(BaseInformationClassifyEntity::getClassifySort));
        // 获取所有父分类对应的子分类
        // 得到子分类
        List<BaseInformationClassifyEntity> menuLists = getMenuList(rootMenus);

        // 将分类数据缓存到Redis中，过期时间为1个月
        // 存入缓存
        redisCache.setCacheObject(HouConfig.HOU_INFORMATION_CLASSIFY_ADMIN_ALL, JsonUtils.toString(menuLists));
        redisCache.expire(HouConfig.HOU_INFORMATION_CLASSIFY_ADMIN_ALL, RedisUtil.EXRP_MONTH, java.util.concurrent.TimeUnit.SECONDS);
        return ResponseResult.success(menuLists);
    }

    private List<BaseInformationClassifyEntity> getMenuList(List<BaseInformationClassifyEntity> rootMenus) {
        List<BaseInformationClassifyEntity> list = new ArrayList<>(rootMenus);
        // 先找到所有的一级菜单
        list = list.stream().filter(item -> item.getClassifyParentId() == 0).collect(Collectors.toList());
        // 查找二级菜单
        for (BaseInformationClassifyEntity menu : list) {
            // 子菜单
            List<BaseInformationClassifyEntity> childList = new ArrayList<>();
            for (BaseInformationClassifyEntity rootMenu : rootMenus) {
                // 遍历所有节点，将父菜单id与传过来的id比较
                if (!StringUtils.isEmpty(rootMenu.getClassifyParentId().toString())) {
                    if (rootMenu.getClassifyParentId().equals(menu.getClassifyId())) {
                        childList.add(rootMenu);
                    }
                }
            }
            menu.setChildMenu(childList);
        }
        return list;
    }

    /**
     * 添加信息分类
     *
     * @param param 分类信息的DTO对象
     * @return 返回操作结果
     */
    @Override
    public ResponseResult addInformationClassify(BaseInformationClassifyDTO param) {
        BaseInformationClassifyEntity entity = new BaseInformationClassifyEntity();
        BeanUtils.copyProperties(param, entity);
        // 如果分类ID不为空，则更新已存在的分类信息
        if (null != entity.getClassifyId()) {
            this.baseMapper.updateById(entity);
        } else {
            // 判断是否为顶级分类，设置分类级别
            // 判读当前添加的类型是否是顶级类型
            if (null == entity.getClassifyParentId() || CommonType.catStart.GRADE_ZERO.getCode().equals(entity.getClassifyParentId())) {
                // 是顶级类型
                entity.setClassifyLevel(CommonType.catStart.GRADE_ONE.getCode());
            } else {
                // 根据他的上级id
                entity.setClassifyLevel(CommonType.catStart.GRADE_TWO.getCode());
            }
            // 插入新的分类信息
            this.baseMapper.insert(entity);
        }
        // 删除缓存中的分类信息，以保证数据一致性
        // 删除分类的缓存
        redisCache.deleteObject(HouConfig.HOU_INFORMATION_CLASSIFY_PC_ALL);
        redisCache.deleteObject(HouConfig.HOU_INFORMATION_CLASSIFY_ADMIN_ALL);
        return ResponseResult.success();
    }

    /**
     * 删除信息分类
     *
     * @param param 分类信息的DTO对象，包含要删除的分类ID
     * @return 返回操作结果
     */
    @Override
    public ResponseResult deleteInformationClassify(BaseInformationClassifyDTO param) {
        List<Integer> classifyIdList = new ArrayList<>();
        // 根据ClassifyId获取分类信息
        BaseInformationClassifyEntity classify = this.baseMapper.selectById(param.getClassifyId());
        // 如果当前分类不是二级分类，则获取其子分类ID
        if (!classify.getClassifyLevel().equals(CommonType.catStart.GRADE_TWO.getCode())) {
            // 查询子类ClassifyId
            classifyIdList = this.baseMapper.selectClassifyLevel(param.getClassifyId());
        }
        // 将当前分类ID加入到待删除的分类ID列表中
        classifyIdList.add(param.getClassifyId());
        // 查询待删除分类下是否有信息，如果有则不能删除
        Integer informationCount = baseInformationInfoMapper.selectInformationCountByClassifyIds(classifyIdList);
        if (informationCount == 0) {
            // 删除分类
            this.baseMapper.deleteByClassifyIds(classifyIdList);
        } else {
            return ResponseResult.error("当前分类或子分类正在使用中，请勿删除！");
        }
        // 删除缓存中的分类信息，以保证数据一致性
        // 删除分类的缓存
        redisCache.deleteObject(HouConfig.HOU_INFORMATION_CLASSIFY_PC_ALL);
        redisCache.deleteObject(HouConfig.HOU_INFORMATION_CLASSIFY_ADMIN_ALL);
        return ResponseResult.success();
    }

    @Override
    public Integer checkSupplySelf(Map<String, Object> param) {
        Integer result=this.baseMapper.checkSupplySelf(param);
        return result;
    }
}
