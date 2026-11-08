package com.houjiang.mall.controller.setting;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.PageParam;
import com.houjiang.mall.service.setting.SettingDictTypeService;
import com.houjiang.mall.validator.BaseValidator;
import com.houjiang.mall.validator.setting.DictTypeCreateParam;
import com.houjiang.mall.validator.setting.DictTypeUpdateParam;
import com.houjiang.mall.vo.setting.SettingDictTypeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 字典类型 Controller
 * 对应 like-admin SettingDictTypeController（/adminapi/setting.dict.dict_type/*）
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/dict_type")
@RequiredArgsConstructor
public class MallSettingDictTypeController {

    private final SettingDictTypeService dictTypeService;

    /**
     * 字典类型全部
     * GET /mall/setting/dict_type/all
     */
    @GetMapping("/all")
    public ResponseResult<List<SettingDictTypeVo>> all() {
        return ResponseResult.success(dictTypeService.all());
    }

    /**
     * 字典类型列表（分页）
     * GET /mall/setting/dict_type/lists?page_no=1&page_size=15&name=xx&type=yy&status=1
     */
    @GetMapping("/lists")
    public ResponseResult<IPage<SettingDictTypeVo>> lists(@RequestParam(required = false) Map<String, String> params) {
        Map<String, String> searchParams = params == null ? new HashMap<>() : new HashMap<>(params);
        PageParam pageParam = extractPageParam(searchParams);
        return ResponseResult.success(dictTypeService.list(pageParam, searchParams));
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

    /**
     * 字典类型详情
     * GET /mall/setting/dict_type/detail?id=1
     */
    @GetMapping("/detail")
    public ResponseResult<SettingDictTypeVo> detail(@RequestParam("id") Integer id) {
        BaseValidator.mustId(id, "id");
        return ResponseResult.success(dictTypeService.detail(id));
    }

    /**
     * 字典类型新增
     * POST /mall/setting/dict_type/add
     */
    @PostMapping("/add")
    public ResponseResult<Void> add(@RequestBody DictTypeCreateParam param) {
        BaseValidator.validate(param);
        dictTypeService.add(param);
        return ResponseResult.success();
    }

    /**
     * 字典类型编辑
     * POST /mall/setting/dict_type/edit
     */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody DictTypeUpdateParam param) {
        BaseValidator.validate(param);
        dictTypeService.edit(param);
        return ResponseResult.success();
    }

    /**
     * 字典类型删除
     * POST /mall/setting/dict_type/delete  body: {"id": 1}
     */
    @PostMapping("/delete")
    public ResponseResult<Void> del(@RequestBody Map<String, Integer> body) {
        Integer id = body == null ? null : body.get("id");
        BaseValidator.mustId(id, "id");
        dictTypeService.del(id);
        return ResponseResult.success();
    }
}
