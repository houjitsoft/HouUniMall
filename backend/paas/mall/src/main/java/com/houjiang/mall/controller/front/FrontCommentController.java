package com.houjiang.mall.controller.front;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.mapper.front.FrontCommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城前台商品评论接口（供 BFF 内部调用）
 *
 * 路径前缀 /mall/front/** 已在白名单，匿名可访问。
 * 数据为空时返回空列表，由前端展示空态，不伪造数据。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/front/comment")
@RequiredArgsConstructor
public class FrontCommentController {

    private final FrontCommentMapper frontCommentMapper;

    /**
     * 商品评论分页列表
     *
     * @param param 查询参数：goods_id（必填）、page、limit
     * @return {lists, count, page_no, page_size}
     */
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> param,
                               @RequestParam(required = false) Map<String, Object> query) {
        Map<String, Object> params = new HashMap<>();
        if (query != null) {
            params.putAll(query);
        }
        if (param != null) {
            params.putAll(param);
        }
        Object goodsIdObj = params.get("goods_id") != null ? params.get("goods_id") : params.get("goodsId");
        if (goodsIdObj == null) {
            return ResponseResult.fail("商品ID不能为空");
        }
        long goodsId;
        try {
            goodsId = Long.parseLong(String.valueOf(goodsIdObj));
        } catch (NumberFormatException e) {
            return ResponseResult.fail("商品ID格式错误");
        }
        // 设置分页默认值，防止前端未传参导致NPE
        int page = toInt(params.get("page_no") != null ? params.get("page_no") : params.get("page"), 1);
        int limit = toInt(params.get("page_size") != null ? params.get("page_size") : params.get("limit"), 10);

        long count = frontCommentMapper.countGoodsComments(goodsId);
        List<Map<String, Object>> rows = count == 0
                ? List.of()
                : frontCommentMapper.selectGoodsComments(goodsId, limit, (page - 1) * limit);

        List<Map<String, Object>> lists = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>(row);
            item.put("level", row.get("score"));
            item.put("nickname", "匿名用户");
            item.put("avatar", "");
            // images 字段为逗号分隔字符串，转为数组供前端直接使用
            String images = row.get("images") == null ? "" : String.valueOf(row.get("images"));
            item.put("images", images.isEmpty() ? List.of() : List.of(images.split(",")));
            lists.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("lists", lists);
        result.put("count", count);
        result.put("page_no", page);
        result.put("page_size", limit);
        return ResponseResult.success(result);
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
