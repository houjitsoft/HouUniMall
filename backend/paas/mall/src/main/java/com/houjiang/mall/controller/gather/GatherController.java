package com.houjiang.mall.controller.gather;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.gather.Gather;
import com.houjiang.mall.service.gather.GatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 集采业务控制器
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/gather")
@RequiredArgsConstructor
public class GatherController {

    private final GatherService gatherService;

    /**
     * 获取集采列表
     * POST /mall/gather/list
     */
    @PostMapping("/list")
    public ResponseResult<List<Gather>> gatherLists(@RequestBody Map<String, Object> params) {
        try {
            List<Gather> list = gatherService.list(params);
            return ResponseResult.success(list);
        } catch (Exception e) {
            log.error("获取集采列表失败", e);
            return ResponseResult.error("获取失败");
        }
    }

    /**
     * 获取集采详情
     * POST /mall/gather/detail
     */
    @PostMapping("/detail")
    public ResponseResult<Gather> gatherDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = getLong(params, "id");
            if (id == null) {
                return ResponseResult.error("缺少 id 参数");
            }
            
            Gather gather = gatherService.getDetail(id);
            if (gather == null) {
                return ResponseResult.error("集采配置不存在");
            }
            
            return ResponseResult.success(gather);
        } catch (Exception e) {
            log.error("获取集采详情失败", e);
            return ResponseResult.error("获取失败");
        }
    }

    /**
     * 保存集采配置
     * POST /mall/gather/save
     */
    @PostMapping("/save")
    public ResponseResult<Gather> gatherSave(@RequestBody Map<String, Object> params) {
        try {
            Gather gather = convertToGather(params);
            
            // 必填参数验证
            if (gather.getGoodsId() == null || gather.getShopId() == null) {
                return ResponseResult.error("缺少必填参数：goods_id 或 shop_id");
            }
            
            boolean result = gatherService.saveConfig(gather);
            if (result) {
                return ResponseResult.success(gather);
            } else {
                return ResponseResult.error("保存失败");
            }
        } catch (Exception e) {
            log.error("保存集采配置失败", e);
            return ResponseResult.error("保存失败");
        }
    }

    /**
     * 删除集采配置
     * POST /mall/gather/delete
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> gatherDelete(@RequestBody Map<String, Object> params) {
        try {
            Long id = getLong(params, "id");
            if (id == null) {
                return ResponseResult.error("缺少 id 参数");
            }
            
            boolean result = gatherService.deleteConfig(id);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("删除集采配置失败", e);
            return ResponseResult.error("删除失败");
        }
    }

    /**
     * 将 Map 转换为 Gather 对象
     */
    private Gather convertToGather(Map<String, Object> params) {
        Gather gather = new Gather();
        
        Object idObj = params.get("id");
        if (idObj != null) {
            gather.setId(Long.parseLong(idObj.toString()));
        }
        
        Object goodsIdObj = params.get("goods_id");
        if (goodsIdObj != null) {
            gather.setGoodsId(Long.parseLong(goodsIdObj.toString()));
        }
        
        Object shopIdObj = params.get("shop_id");
        if (shopIdObj != null) {
            gather.setShopId(Long.parseLong(shopIdObj.toString()));
        }
        
        Object gatherPriceObj = params.get("gather_price");
        if (gatherPriceObj != null) {
            gather.setGatherPrice(new BigDecimal(gatherPriceObj.toString()));
        }
        
        Object gatherQuantityObj = params.get("gather_quantity");
        if (gatherQuantityObj != null) {
            gather.setGatherQuantity(Integer.parseInt(gatherQuantityObj.toString()));
        }
        
        Object statusObj = params.get("status");
        if (statusObj != null) {
            gather.setStatus(Integer.parseInt(statusObj.toString()));
        }
        
        gather.setRemark((String) params.get("remark"));
        
        return gather;
    }

    /**
     * 从 Map 中提取 Long 值
     */
    private Long getLong(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}
