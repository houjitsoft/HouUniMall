package com.houjiang.mall.controller.setting;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.PageParam;
import com.houjiang.mall.service.setting.SettingDictDataService;
import com.houjiang.mall.validator.BaseValidator;
import com.houjiang.mall.validator.setting.DictDataCreateParam;
import com.houjiang.mall.validator.setting.DictDataUpdateParam;
import com.houjiang.mall.vo.setting.SettingDictDataVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 字典数据 Controller
 * 对应 like-admin SettingDictDataController（/adminapi/setting.dict.dict_data/*）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/dict_data")
@RequiredArgsConstructor
public class MallSettingDictDataController {

    private final SettingDictDataService dictDataService;

    /**
     * 字典数据全部（按 typeId 过滤）
     * GET /mall/setting/dict_data/all?type_id=1
     */
    @GetMapping("/all")
    public ResponseResult<List<SettingDictDataVo>> all(@RequestParam(required = false) Map<String, String> params) {
        Map<String, String> p = params == null ? new HashMap<>() : new HashMap<>(params);
        return ResponseResult.success(dictDataService.all(p));
    }

    /**
     * 字典数据分页列表
     * GET /mall/setting/dict_data/lists?page_no=1&page_size=15&type_id=1&name=xx&status=1
     */
    @GetMapping("/lists")
    public ResponseResult<IPage<SettingDictDataVo>> lists(@RequestParam(required = false) Map<String, String> params) {
        Map<String, String> searchParams = params == null ? new HashMap<>() : new HashMap<>(params);
        PageParam pageParam = extractPageParam(searchParams);
        return ResponseResult.success(dictDataService.list(pageParam, searchParams));
    }

    /**
     * 字典数据详情
     * GET /mall/setting/dict_data/detail?id=1
     */
    @GetMapping("/detail")
    public ResponseResult<SettingDictDataVo> detail(@RequestParam("id") Integer id) {
        BaseValidator.mustId(id, "id");
        return ResponseResult.success(dictDataService.detail(id));
    }

    /**
     * 字典数据新增
     * POST /mall/setting/dict_data/add
     */
    @PostMapping("/add")
    public ResponseResult<Void> add(@RequestBody DictDataCreateParam param) {
        BaseValidator.validate(param);
        dictDataService.add(param);
        return ResponseResult.success();
    }

    /**
     * 字典数据编辑
     * POST /mall/setting/dict_data/edit
     */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody DictDataUpdateParam param) {
        BaseValidator.validate(param);
        dictDataService.edit(param);
        return ResponseResult.success();
    }

    /**
     * 字典数据删除
     * POST /mall/setting/dict_data/delete   body: {"id": 1}
     */
    @PostMapping("/delete")
    public ResponseResult<Void> del(@RequestBody Map<String, Integer> body) {
        Integer id = body == null ? null : body.get("id");
        BaseValidator.mustId(id, "id");
        dictDataService.del(id);
        return ResponseResult.success();
    }

    /**
     * 从查询参数中提取分页参数，并从原 map 中移除（避免污染搜索条件）。
     */
    private PageParam extractPageParam(Map<String, String> params) {
        PageParam pp = new PageParam();
        String pageNo = firstNonNull(params.remove("pageNo"), params.remove("page_no"));
        String pageSize = firstNonNull(params.remove("pageSize"), params.remove("page_size"));
        if (pageNo != null && !pageNo.isEmpty()) {
            try { pp.setPageNo(Integer.parseInt(pageNo)); } catch (NumberFormatException ignored) {}
        }
        if (pageSize != null && !pageSize.isEmpty()) {
            try { pp.setPageSize(Integer.parseInt(pageSize)); } catch (NumberFormatException ignored) {}
        }
        return pp;
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}
