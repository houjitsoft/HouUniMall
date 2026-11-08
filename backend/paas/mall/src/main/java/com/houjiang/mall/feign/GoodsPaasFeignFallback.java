package com.houjiang.mall.feign;

import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * goods 服务不可达时快速降级，避免 mall 接口阻塞 30s。
 * @author Amos
 */
@Slf4j
@Component
public class GoodsPaasFeignFallback implements GoodsPaasFeign {

    @Override
    public ResponseResult goodsLists(Map<String, Object> body) {
        log.warn("[GoodsPaasFeign] goodsLists 降级，body={}", body);
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("pageNo", 1);
        empty.put("pageSize", 15);
        empty.put("count", 0);
        empty.put("lists", Collections.emptyList());
        empty.put("extend", defaultGoodsExtend());
        return ResponseResult.success(empty);
    }

    @Override
    public ResponseResult goodsDetail(Integer id) {
        return ResponseResult.fail("商品服务暂时不可用");
    }

    @Override
    public ResponseResult categoryLists(String name) {
        return ResponseResult.success(Collections.emptyList());
    }

    private static Map<String, Object> defaultGoodsExtend() {
        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("count_sale", 0);
        extend.put("count_not_sale", 0);
        extend.put("count_audit", 0);
        extend.put("count_audit_refuse", 0);
        extend.put("count_recycle", 0);
        return extend;
    }
}
