package com.houjiang.base.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.base.dto.FloorDTO;
import com.houjiang.base.dto.FloorListDTO;
import com.houjiang.base.dto.GdbPcIndexInfoDTO;
import com.houjiang.base.entity.BaseFloorAdpageEntity;
import com.houjiang.base.entity.BasePcIndexConfigEntity;
import com.houjiang.base.mapper.BaseFloorAdpageMapper;
import com.houjiang.base.mapper.BasePcIndexConfigMapper;
import com.houjiang.base.service.BaseFloorAdpageService;
import com.houjiang.base.vo.index.GdbPcIndexVO;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.RedisPrefixUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Service
public class BaseFloorAdpageServiceImpl extends ServiceImpl<BaseFloorAdpageMapper, BaseFloorAdpageEntity>
    implements BaseFloorAdpageService {

    @Value("${system.pcIndexConfig}")
    private  String pcIndexConfig ;

    private static String pcIndexConfig2;


    @PostConstruct
    public  String getPcIndexConfig2() {
        return pcIndexConfig2 = this.pcIndexConfig;
    }

    @Autowired
    private BasePcIndexConfigMapper basePcIndexConfigMapper;

    @Autowired
    private RedisCache redisCache;
    /**
     * 更新PC端首页排序信息。
     * 该方法用于根据提供的DTO更新PC端首页的广告页和配置信息的排序。针对不同的页面类型，
     * 选择性地更新广告页排序或配置信息排序，并清除相关的Redis缓存。
     *
     * @param dto 包含需要更新的广告页和配置信息的DTO。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult updateQueryPcIndexSort(GdbPcIndexInfoDTO dto) {

        // 获取待更新的广告页和配置信息列表
        List<GdbPcIndexVO> gdbPcIndexDTOList = dto.getGdbPcIndexDTOList();
        // 当列表不为空且不为空列表时，进行更新操作
        if(null != gdbPcIndexDTOList && gdbPcIndexDTOList.size() > 0 ){
            for (GdbPcIndexVO gdbPcIndexDTO : gdbPcIndexDTOList) {
                // 根据页面类型决定更新广告页排序还是配置信息排序
                if(5 == gdbPcIndexDTO.getPageType() || 6 == gdbPcIndexDTO.getPageType()){
                    // 更新广告页排序
                    //更新gdb_merchant_floor_adpage表数据排序
                      this.baseMapper.update(null,new LambdaUpdateWrapper<BaseFloorAdpageEntity>()
                                                .set(BaseFloorAdpageEntity::getFloorAdpageOrder,gdbPcIndexDTO.getPageSort())
                                                .eq(BaseFloorAdpageEntity::getFloorType, CommonType.commonStatus.YES.getCode())
                                                .eq(gdbPcIndexDTO.getPageType()!=null,BaseFloorAdpageEntity::getPageType,gdbPcIndexDTO.getPageType())
                                                .eq(gdbPcIndexDTO.getFloorAdpageId()!=null,BaseFloorAdpageEntity::getFloorAdpageId,gdbPcIndexDTO.getFloorAdpageId()));
                }else{
                    // 更新配置信息排序
                    //更新gdb_pc_index_config数据排序
                        basePcIndexConfigMapper.update(null,new LambdaUpdateWrapper<BasePcIndexConfigEntity>()
                                                .set(BasePcIndexConfigEntity::getPageSort,gdbPcIndexDTO.getPageSort())
                                                .eq(gdbPcIndexDTO.getPageType()!=null,BasePcIndexConfigEntity::getPageType,gdbPcIndexDTO.getPageType()));
                }
            }
            // 清除Redis缓存
            //存入redis
            redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig);
            redisCache.deleteObject(RedisPrefixUtils.pcIndexConfig2);
        }

        // 返回操作成功结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 更新广告页面布局和内容。
     * 该方法用于处理广告页面的更新和新增操作，根据输入的DTO更新或新增广告页面信息，并清除相关的Redis缓存。
     *
     * @param param 包含需要更新或新增的广告页面信息的DTO。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult updateAdpageFloor(FloorListDTO param) {
        // 初始化更新和新增的广告页面列表
        List<FloorDTO> updateFloorAdpage = Lists.newArrayList();
        List<FloorDTO> addFloorAdpage = Lists.newArrayList();

        // 遍历待处理的广告页面
        for (FloorDTO floor : param.getFloorUpdateList()) {
            // 校验商品ID是否有效
            if (null == floor.getRightRec().getRightAdpageNavGoodsId()) {
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());

            }
            // 处理商品ID的格式
            String str = floor.getRightRec().getRightAdpageNavGoodsId().replace('，', ',');
            floor.getRightRec().setRightAdpageNavGoodsId(str);
            // 校验商品ID数量是否超过限制
            //获取楼层增加代饿goodsId长度，如果超过8个，则不允许添加
            String[] arr;
            arr = str.split(",");
            if (arr.length > 8) {
                return ResponseResult.fail(BizCodeEnume.UPDATE_ADPAGE_Floor_MAX_ERROP.getCode(), BizCodeEnume.UPDATE_ADPAGE_Floor_MAX_ERROP.getMsg());

            } else if (arr.length == 0 || "".equals(arr[0])) {
                return ResponseResult.fail(BizCodeEnume.UPDATE_ADPAGE_Floor_MIN__ERROP.getCode(), BizCodeEnume.UPDATE_ADPAGE_Floor_MIN__ERROP.getMsg());

            }

            // 处理其他信息并准备更新或新增
            floor.setFloorAdpageName(JsonUtils.toString(floor.getFloorName()));
            floor.setLeftAdpageNav(JsonUtils.toString(floor.getLeftAdpageNavList()));
            floor.setRightHotRecommend(JsonUtils.toString(floor.getRightRec()));
            floor.setRightAdpageNav(JsonUtils.toString(floor.getRightAdpageNavList()));
            floor.setFloorUpdateTime(new Date());
            // 根据是否有ID，决定是更新还是新增
            if (null == floor.getFloorAdpageId()) {
                addFloorAdpage.add(floor);
            } else {
                updateFloorAdpage.add(floor);
            }
        }

        // 执行更新和新增操作
        if (!CollectionUtils.isEmpty(updateFloorAdpage)) {
            this.baseMapper.updateFloorAdpage(updateFloorAdpage);
        }
        if (!CollectionUtils.isEmpty(addFloorAdpage)) {
            this.baseMapper.insertFloorAdpage(addFloorAdpage);
        }
        // 清除Redis缓存
        redisCache.deleteObject("PC_INDEX_CONFIG_TEST");
        return ResponseResult.success();
    }
    /**
     * 根据楼层广告页ID查询对应的楼层信息。
     * <p>
     * 本方法通过指定的楼层广告页ID，从数据库中查询并返回相应的楼层广告页信息。
     * 主要用于在前端展示楼层广告时，根据ID获取具体的楼层布局和广告内容。
     *
     * @param floorAdpageId 楼层广告页的唯一标识ID
     * @return 返回包含指定楼层广告页信息的ResponseResult对象
     */
    @Override
    public ResponseResult queryWebIndexFloor(Integer floorAdpageId) {
        // 根据ID查询楼层广告页信息
        BaseFloorAdpageEntity floorAdpage = this.baseMapper.selectById(floorAdpageId);
        // 返回查询结果，如果查询成功，则将结果包装在ResponseResult中返回
        return ResponseResult.success(floorAdpage);
    }

    /**
     * 查询所有楼层广告页的列表信息，并按照排序顺序返回。
     * <p>
     * 本方法用于查询系统中所有的楼层广告页列表，按照楼层广告页的排序顺序进行升序排列。
     * 主要用于在前端页面上展示所有的楼层广告，以便用户浏览和选择。
     *
     * @return 返回包含所有楼层广告页信息的列表的ResponseResult对象
     */
    @Override
    public ResponseResult queryWebIndexFloorList() {
        // 使用Lambda表达式构建查询条件，按照楼层广告页的排序顺序进行升序排列
        List<BaseFloorAdpageEntity> results = this.baseMapper.selectList(new LambdaQueryWrapper<BaseFloorAdpageEntity>()
                                                         .orderByAsc(BaseFloorAdpageEntity::getFloorAdpageOrder));
        // 返回查询结果，将所有楼层广告页的列表包装在ResponseResult中返回
        return ResponseResult.success(results);
    }


}




