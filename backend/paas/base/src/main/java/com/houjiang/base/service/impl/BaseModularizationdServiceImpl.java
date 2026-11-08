package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.base.entity.BaseModularizationdEntity;
import com.houjiang.base.feign.ActivityClient;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.mapper.BaseModularizationdMapper;
import com.houjiang.base.service.BaseModularizationdService;
import com.houjiang.base.service.BaseSysConfigsService;
import com.houjiang.base.service.template.MobilePageTemplates;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.PointsRatioUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.ModularGoodsVO;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.commons.vo.goods.ModularizationdGoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName BaseModularizationdServiceImpl
 * @Description 组件化 页面
 * @Author Amos
 * @Date 2023-07-04 17:58:52
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseModularizationdServiceImpl extends ServiceImpl<BaseModularizationdMapper, BaseModularizationdEntity>
        implements BaseModularizationdService {
    @Autowired
    private ActivityClient activityClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BaseSysConfigsService configsService;

    /**
     * 增加或更新模块化数据。
     *
     * @param param 包含模块化信息的DTO对象。
     * @return 如果操作成功，返回成功的响应结果；否则返回失败的响应结果。
     */
    @Override
    public ResponseResult ModularizationdAddOrUPdate(ModularizationdDTO param) {
        Integer result = null;
        try {
            // 初始化页面类型和页面开始标志
            if (null == param.getPageType()) {
                param.setPageType(0);
            }
            if (null == param.getPageId()) {
                // 处理商品全ID和页面是否为二级页面的逻辑
                if (null != param.getGoodsAllId() && param.getGoodsAllId().size() > 0) {
                    param.setGoodsAllIdJson(JsonUtils.toString(param.getGoodsAllId()));
                }
                if (null != param.getPageIsIndex() && 2 == param.getPageIsIndex()) {
                    param.setPageStart(1);
                } else {
                    param.setPageStart(0);
                }
                // 添加模块化数据
                result = this.baseMapper.addModularizationd(param);
                if (0 == result) {
                    return ResponseResult.fail();
                }
            } else {
                // 处理商品全ID，然后更新模块化数据
                if (null != param.getGoodsAllId() && param.getGoodsAllId().size() > 0) {
                    param.setGoodsAllIdJson(JsonUtils.toString(param.getGoodsAllId()));
                }
                result = this.baseMapper.updateModularizationd(param);
                if (0 == result) {
                    return ResponseResult.fail();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.success();
    }

    /**
     * 查询所有模块化数据。
     *
     * @param param 包含查询条件的DTO对象。
     * @return 包含查询结果的响应结果对象。
     */
    @Override
    public ResponseResult queryModularizationdAll(ModularizationdDTO param) {
        Map map = null;
        try {
            map = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        IPage<ModularizationdDTO> page = this.baseMapper.queryModularizationdAll(new Query<ModularizationdDTO>().getPage(map), param);
        List<ModularizationdDTO> records = page.getRecords();
        // 处理二级页面的URL
        if (null != param.getPageIsIndex() && 2 == param.getPageIsIndex()) {
            if (!CollectionUtils.isEmpty(records)) {
                for (ModularizationdDTO modularizationdParam : records) {
                    if (null != modularizationdParam.getPageStart() && 1 == modularizationdParam.getPageStart()) {
                        modularizationdParam.setPageUrl("/general/diy/diy?pageId=" + modularizationdParam.getPageId());
                    }
                }
            }
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据ID查询模块化数据。
     *
     * @param param 包含模块化ID的DTO对象。
     * @return 包含查询结果的响应结果对象。
     */
    @Override
    public ResponseResult queryModularizationdById(ModularizationdDTO param) {
        ModularizationdDTO modularizationdParam = this.baseMapper.queryModularizationdById(param);
        List<ModularizationdGoodsVO> modularizationdGoodsParams = queryModularizationdGoodsInfo(modularizationdParam.getGoodsAllIdJson());
        modularizationdParam.setGoodsList(modularizationdGoodsParams);
        return ResponseResult.success(modularizationdParam);
    }

    /**
     * 删除模块化数据。
     *
     * @param param 包含待删除模块化数据ID的DTO对象。
     * @return 如果删除成功，返回成功的响应结果；否则返回失败的响应结果。
     */
    @Override
    public ResponseResult modularizationdDelete(ModularizationdDTO param) {
        Integer result = this.baseMapper.modularizationdDelete(param);
        if (result > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 更新模块化页面的启动状态。
     * 当页面启动状态被设置为首页时，取消其他页面的首页设置。
     *
     * @param param 包含页面ID和页面启动位置的参数对象。
     * @return 如果操作成功，返回成功的响应结果；否则，返回失败的响应结果。
     */
    @Override
    public ResponseResult modularizationdUpdateStart(ModularizationdDTO param) {
        // 检查页面ID和页面启动位置是否为空，如果为空，则返回缺失参数的错误响应
        if (null == param.getPageId() || null == param.getPageStart()) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), BizCodeEnume.MISSING_PARAMETER.getMsg());
        }
        // 取消其他页面的首页设置
        this.baseMapper.updateStartAll();
        // 更新指定页面的启动状态
        Integer result = this.baseMapper.UpdateStart(param);
        // 如果更新成功（影响行数大于0），返回成功响应；否则，返回失败响应
        if (result > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 根据页面ID查询模块化页面的默认设置。
     *
     * @param param 包含页面ID的参数对象。
     * @return 如果查询成功，返回包含默认模块化页面信息的响应结果。
     */
    @Override
    public ResponseResult ModularizationdDefaultByPageId(ModularizationdDTO param) {
        ModularizationdDTO modularizationdParam = this.baseMapper.queryModularizationdDefault(param);
        return ResponseResult.success(modularizationdParam);
    }

    /**
     * 更新二级页面的启动状态。
     *
     * @param param 包含页面ID和页面启动位置的参数对象。
     * @return 如果操作成功，返回成功的响应结果；否则，返回失败的响应结果。
     */
    @Override
    public ResponseResult UpdateSecondLevelStart(ModularizationdDTO param) {
        Integer result = null;
        try {
            // 检查页面ID和页面启动位置是否为空，如果为空，则返回缺失参数的错误响应
            if (null == param.getPageId()) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }
            if (null == param.getPageStart()) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }
            // 更新指定页面的启动状态
            result = this.baseMapper.update(null, new LambdaUpdateWrapper<BaseModularizationdEntity>()
                    .set(BaseModularizationdEntity::getPageStart, param.getPageStart())
                    .eq(BaseModularizationdEntity::getPageId, param.getPageId()));
            // 如果更新失败（影响行数为0），返回操作失败的响应
            if (0 == result) {
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
            }
            // 返回操作成功的响应
            return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果操作过程中发生异常，返回包含异常信息的失败响应
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 更新模块化页面索引。
     * 该方法旨在根据条件查询模块化页面信息，并更新相关的商品数据。
     * 如果找不到特定的索引页面，它将尝试查找默认页面并更新其商品信息。
     *
     * @return 返回更新后的模块化页面数据的结果。
     */
    @Override
    public ResponseResult modularizationdUpdateIndex() {
        try {
            // 查询首页数据
            BaseModularizationdEntity modularizationdParam = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseModularizationdEntity>()
                    .eq(BaseModularizationdEntity::getPageStart, 1)
                    .eq(BaseModularizationdEntity::getPageIsIndex, 1));
            // 如果位null就查询系统默认的页面
            if (null == modularizationdParam) {
                modularizationdParam = new BaseModularizationdEntity();
                List<BaseModularizationdEntity> modularizationdParams = this.baseMapper.selectList(new LambdaQueryWrapper<BaseModularizationdEntity>()
                        .eq(BaseModularizationdEntity::getPageDefault, 1));
                if (null != modularizationdParams && !modularizationdParams.isEmpty()) {
                    List<ModularizationdGoodsVO> modularizationdGoodsParams = queryModularizationdGoodsInfo(modularizationdParams.getFirst().getGoodsallidjson());
                    modularizationdParam.setGoodsList(modularizationdGoodsParams);
                    attachTemplateView(modularizationdParams.getFirst());
                    return ResponseResult.success(modularizationdParams.getFirst());
                } else {
                    return ResponseResult.fail(BizCodeEnume.SELECT_DATA_NULL);
                }
            }
            List<ModularizationdGoodsVO> modularizationdGoodsParams = queryModularizationdGoodsInfo(modularizationdParam.getGoodsallidjson());

            // 计算价格
            PointsCashRatioVo pointsCashRatio = configsService.getPointsCashRatio();
            modularizationdGoodsParams.forEach(goods -> {
                BigDecimal commonPrice = goods.getCommonPrice();
                // 根据设置比例，计算商品价格
                Double pointsRate = goods.getPointsRate();
                if (Objects.nonNull(pointsRate)) {

                    PointsRatioUtil.PointsCashRatioRes ratio = PointsRatioUtil.getPointsRatio(commonPrice, pointsRate);

                    // 设置现金金额
                    goods.setCashPrice(ratio.getCashPrice());
                }
            });
            modularizationdParam.setGoodsList(modularizationdGoodsParams);
            attachTemplateView(modularizationdParam);
            return ResponseResult.success(modularizationdParam);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 按页面所选模板补齐前台渲染视图。
     *
     * <p>code 为 null（未设置）时下发默认模板 {@link MobilePageTemplates#DEFAULT_CODE}；
     * 空串表示后台显式选择「不套用」，此时空 style，前台沿用 pageData 原始版式。
     *
     * @param page 页面实体（须已从库中查出，携带 pageTemplateCode）
     */
    private void attachTemplateView(BaseModularizationdEntity page) {
        if (page == null) {
            return;
        }
        String code = page.getPageTemplateCode();
        if (code != null && code.isBlank()) {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("templateCode", "");
            view.put("style", "");
            view.put("themeColor", "");
            view.put("columns", 1);
            page.setTemplateView(view);
            return;
        }
        page.setTemplateView(MobilePageTemplates.toFrontView(code));
    }

    /**
     * 查询模块化页面商品信息。
     * 根据商品ID列表查询相关商品的信息，并处理这些商品的活动状态。
     *
     * @param goodsAllIdJson 商品ID列表的JSON字符串
     * @return 返回处理后的模块化页面商品信息列表
     */
    public List<ModularizationdGoodsVO> queryModularizationdGoodsInfo(String goodsAllIdJson) {
        List<ModularizationdGoodsVO> list = new ArrayList<>();
        if (null == goodsAllIdJson) {
            return null;
        }
        List<String> goodsAllIdJsons = JsonUtils.string2Obj(goodsAllIdJson, List.class, String.class);
        for (String goodsId : goodsAllIdJsons) {
            ModularizationdGoodsVO modularizationdGoodsParam;
            // 查询商品 价格 销量
            modularizationdGoodsParam = goodsClient.queryModularizationdGoodsInfo(goodsId);
            Map<String, Object> activityMap = dealActivityStatus(Integer.parseInt(goodsId));
            if (null == modularizationdGoodsParam) {
                modularizationdGoodsParam = new ModularizationdGoodsVO();
                // 商品状态 1 上架 2 下架
                modularizationdGoodsParam.setGoodsStart(2);
                // 商品状态 1 上架 2 下架
                modularizationdGoodsParam.setGoodsId(Integer.valueOf(goodsId));
            } else {
                // 商品状态 1 上架 2 下架
                modularizationdGoodsParam.setGoodsStart(1);
            }
            if (null != activityMap.get("activityStatus") && 1 == (Integer) activityMap.get("activityStatus")) {
                modularizationdGoodsParam.setActivityStatus(1);
                if (null != activityMap.get("activityPrice")) {
                    modularizationdGoodsParam.setCommonPrice((BigDecimal) activityMap.get("activityPrice"));
                    modularizationdGoodsParam.setStartTime(activityMap.get("startTime").toString());
                    modularizationdGoodsParam.setEndTime(activityMap.get("endTime").toString());
                }
            } else {
                modularizationdGoodsParam.setActivityStatus(2);
            }
            list.add(modularizationdGoodsParam);
        }
        return list;
    }

    /**
     * 处理商品的活动状态。
     * 根据商品ID查询并处理商品的活动状态，如秒杀、限时折扣、拼团、满减赠折等。
     *
     * @param goodsId 商品ID
     * @return 返回包含商品活动状态和详细信息的映射
     */
    public Map<String, Object> dealActivityStatus(Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        // 活动类型 0不参加,2秒杀,3限时折扣,4拼团,5满减赠折
        Integer commonIsXian = null;
        ModularGoodsVO modularGoodsParam = null;
        try {
            commonIsXian = this.baseMapper.queryCommonIsXian(goodsId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // 普通商品
        if (null == commonIsXian || 0 == commonIsXian) {
            // 活动状态 0-结束 1-正常
            map.put("activityStatus", 0);
            return map;
        // 秒杀商品
        } else if (2 == commonIsXian) {
            modularGoodsParam = JSONObject.parseObject(JSON.toJSONString(activityClient.seckillGoodsPrice(goodsId).getData()), ModularGoodsVO.class);

        // 限时折扣商品
        } else if (3 == commonIsXian) {
            modularGoodsParam = JSONObject.parseObject(JSON.toJSONString(activityClient.selectPriceByGoodsId(goodsId).getData()), ModularGoodsVO.class);

        // 拼团商品
        } else if (4 == commonIsXian) {
            modularGoodsParam = JSONObject.parseObject(JSON.toJSONString(activityClient.pinTuanGoodsPrice(goodsId).getData()), ModularGoodsVO.class);

        // 满减赠折商品
        } else if (5 == commonIsXian) {
            modularGoodsParam = JSONObject.parseObject(JSON.toJSONString(activityClient.manJianZengZheGoodsPrice(goodsId).getData()), ModularGoodsVO.class);

        }
        // 如果活动价 不为null 且大于0 证明该活动未过期
        if (null != modularGoodsParam && null != modularGoodsParam.getActivityPrice() && 0 <= modularGoodsParam.getActivityPrice().compareTo(BigDecimal.ZERO)) {
            // 活动状态 0-结束 1-正常
            map.put("activityStatus", 1);
            // 活动状态 0-结束 1-正常
            map.put("activityPrice", modularGoodsParam.getActivityPrice());
            map.put("startTime", modularGoodsParam.getStartTime());
            map.put("endTime", modularGoodsParam.getEndTime());
        } else {
            // 活动状态 0-结束 1-正常
            map.put("activityStatus", 0);
        }
        return map;
    }

    /**
     * 查询底部导航信息
     *
     * @param param 查询参数，此处未具体使用，可能用于未来扩展或其它逻辑
     * @return 包含底部导航信息的响应结果，如果查询到数据则成功返回，否则返回失败
     */
    @Override
    public ResponseResult querybottomNavigation(Map<String, Object> param) {
        // 初始化用于存放响应数据的Map
        HashMap<String, Object> map = new HashMap<>();
        // 使用MyBatis Plus的Lambda查询方式，查询页面索引为3的模块化实体数据
        BaseModularizationdEntity list = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseModularizationdEntity>()
                .eq(BaseModularizationdEntity::getPageIsIndex, 3));
        // 判断查询结果是否为空
        if (list != null) {
            // 如果查询到数据，将页面数据和页面ID放入响应数据Map中
            map.put("pagedata", list.getPageData());
            map.put("pageId", list.getPageId());
            // 返回成功响应结果，携带底部导航数据
            return ResponseResult.success(map);
        } else {
            // 如果没有查询到数据，返回失败响应结果，说明数据查询为空
            return ResponseResult.success();
        }
    }

    /**
     * 处理底部导航的增/改操作
     *
     * @param param 导航参数封装对象
     * @return 响应结果，包含操作成功或失败的信息
     */
    @Override
    public ResponseResult bottomNavigation(ModularizationdDTO param) {
        // 设置默认的页面类型和起始页
        param.setPageType(1);
        param.setPageStart(1);
        // 设置非首页的标志
        param.setPageIsIndex(3);

        // 判断是新增还是更新操作
        if (param.getPageId() == null || param.getPageId() == 0) {
            // 新增操作
            int result = this.baseMapper.addBottomNavigation(param);
            if (result == 0) {
                // 添加失败
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
            }
            // 添加成功
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } else {
            // 更新操作
            int result = this.baseMapper.updateBottomNavigation(param);
            if (result == 0) {
                // 更新失败
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
            }
            // 更新成功
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
    }

    @Override
    public Integer add(ModularizationdDTO param) {
        Integer result = this.baseMapper.add(param);
        return result;
    }

    @Override
    public ResponseResult queryPageTemplates() {
        return ResponseResult.success(MobilePageTemplates.toAdminList());
    }

    @Override
    public ResponseResult queryPageTemplateCode(ModularizationdDTO param) {
        Integer pageId = param == null ? null : param.getPageId();
        if (pageId == null) {
            return ResponseResult.fail("缺少页面 id");
        }
        BaseModularizationdEntity page = this.baseMapper.selectById(pageId);
        if (page == null) {
            return ResponseResult.fail("页面不存在");
        }
        return ResponseResult.success(page.getPageTemplateCode() == null ? "" : page.getPageTemplateCode());
    }

    @Override
    public ResponseResult applyPageTemplate(ModularizationdDTO param) {
        if (param == null || param.getPageId() == null) {
            return ResponseResult.fail("缺少页面 id");
        }
        String code = param.getTemplateCode();
        // 允许显式传空串表示取消套用模板，回到页面自身版式
        if (code != null && !code.isBlank() && !MobilePageTemplates.isValid(code)) {
            return ResponseResult.fail("未知模板：" + code);
        }
        BaseModularizationdEntity page = this.baseMapper.selectById(param.getPageId());
        if (page == null) {
            return ResponseResult.fail("页面不存在");
        }
        String normalized = (code == null || code.isBlank()) ? null : MobilePageTemplates.find(code).code();
        LambdaUpdateWrapper<BaseModularizationdEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BaseModularizationdEntity::getPageId, param.getPageId());
        if (normalized == null) {
            wrapper.set(BaseModularizationdEntity::getPageTemplateCode, null);
        } else {
            wrapper.set(BaseModularizationdEntity::getPageTemplateCode, normalized);
        }
        this.baseMapper.update(null, wrapper);
        return ResponseResult.success(normalized == null ? "" : normalized);
    }

}
