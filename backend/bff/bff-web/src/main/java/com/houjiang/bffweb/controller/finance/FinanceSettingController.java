package com.houjiang.bffweb.controller.finance;

import com.houjiang.bffweb.feign.FinanceServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 财务设置控制器（BFF层）
/**
 * 提供收入类别、支出类别、账户科目的CRUD接口
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/finance/setting")
@Tag(name = "财务设置", description = "财务设置相关接口")
public class FinanceSettingController {

    @Autowired
    private FinanceServiceFeign financeServiceFeign;

    // ================= 收入类别 =================

    /**
     * 分页查询收入类别
/**
     * @param page 页码
     * @param size 每页大小
     * @return 类别列表
     */
    @GetMapping("/income-type/list")
    @Operation(summary = "分页查询收入类别")
    public ResponseResult getIncomeTypeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("size", size);
        return financeServiceFeign.getIncomeTypeList(param);
    }

    /**
     * 获取所有收入类别
/**
     * @return 类别列表
     */
    @GetMapping("/income-type/all")
    @Operation(summary = "获取所有收入类别")
    public ResponseResult getAllIncomeTypes() {
        return financeServiceFeign.getAllIncomeTypes();
    }

    /**
     * 创建收入类别
/**
     * @param type 类别数据
     * @return 创建结果
     */
    @PostMapping("/income-type/create")
    @Operation(summary = "创建收入类别")
    public ResponseResult createIncomeType(@RequestBody Map<String, Object> type) {
        return financeServiceFeign.createIncomeType(type);
    }

    /**
     * 更新收入类别
/**
     * @param type 类别数据
     * @return 更新结果
     */
    @PostMapping("/income-type/update")
    @Operation(summary = "更新收入类别")
    public ResponseResult updateIncomeType(@RequestBody Map<String, Object> type) {
        return financeServiceFeign.updateIncomeType(type);
    }

    /**
     * 删除收入类别
/**
     * @param id 类别ID
     * @return 删除结果
     */
    @PostMapping("/income-type/delete")
    @Operation(summary = "删除收入类别")
    public ResponseResult deleteIncomeType(@RequestParam Integer id) {
        return financeServiceFeign.deleteIncomeType(id);
    }

    // ================= 支出类别 =================

    /**
     * 分页查询支出类别
/**
     * @param page 页码
     * @param size 每页大小
     * @return 类别列表
     */
    @GetMapping("/spending-type/list")
    @Operation(summary = "分页查询支出类别")
    public ResponseResult getSpendingTypeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("size", size);
        return financeServiceFeign.getSpendingTypeList(param);
    }

    /**
     * 获取所有支出类别
/**
     * @return 类别列表
     */
    @GetMapping("/spending-type/all")
    @Operation(summary = "获取所有支出类别")
    public ResponseResult getAllSpendingTypes() {
        return financeServiceFeign.getAllSpendingTypes();
    }

    /**
     * 创建支出类别
/**
     * @param type 类别数据
     * @return 创建结果
     */
    @PostMapping("/spending-type/create")
    @Operation(summary = "创建支出类别")
    public ResponseResult createSpendingType(@RequestBody Map<String, Object> type) {
        return financeServiceFeign.createSpendingType(type);
    }

    /**
     * 更新支出类别
/**
     * @param type 类别数据
     * @return 更新结果
     */
    @PostMapping("/spending-type/update")
    @Operation(summary = "更新支出类别")
    public ResponseResult updateSpendingType(@RequestBody Map<String, Object> type) {
        return financeServiceFeign.updateSpendingType(type);
    }

    /**
     * 删除支出类别
/**
     * @param id 类别ID
     * @return 删除结果
     */
    @PostMapping("/spending-type/delete")
    @Operation(summary = "删除支出类别")
    public ResponseResult deleteSpendingType(@RequestParam Integer id) {
        return financeServiceFeign.deleteSpendingType(id);
    }

    // ================= 账户科目 =================

    /**
     * 分页查询账户科目
/**
     * @param page 页码
     * @param size 每页大小
     * @param status 状态
     * @return 科目列表
     */
    @GetMapping("/subjects/list")
    @Operation(summary = "分页查询账户科目")
    public ResponseResult getSubjectsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("size", size);
        if (status != null) param.put("status", status);
        return financeServiceFeign.getSubjectsList(param);
    }

    /**
     * 获取所有账户科目
/**
     * @return 科目列表
     */
    @GetMapping("/subjects/all")
    @Operation(summary = "获取所有账户科目")
    public ResponseResult getAllSubjects() {
        return financeServiceFeign.getAllSubjects();
    }

    /**
     * 获取所有账户科目（POST方式，用于前端统一调用）
/**
     * @return 科目列表
     */
    @PostMapping("/subjects")
    @Operation(summary = "获取所有账户科目")
    public ResponseResult getSubjects() {
        return financeServiceFeign.getAllSubjects();
    }

    /**
     * 获取账户科目详情
/**
     * @param id 科目ID
     * @return 科目详情
     */
    @GetMapping("/subjects/detail")
    @Operation(summary = "获取账户科目详情")
    public ResponseResult getSubjectsDetail(@RequestParam Integer id) {
        return financeServiceFeign.getSubjectsDetail(id);
    }

    /**
     * 创建账户科目
/**
     * @param subjects 科目数据
     * @return 创建结果
     */
    @PostMapping("/subjects/create")
    @Operation(summary = "创建账户科目")
    public ResponseResult createSubjects(@RequestBody Map<String, Object> subjects) {
        return financeServiceFeign.createSubjects(subjects);
    }

    @PostMapping("/subjects/batch-create")
    @Operation(summary = "批量创建账户科目")
    public ResponseResult batchCreateSubjects(@RequestBody Map<String, Object> subjects) {
        return financeServiceFeign.batchCreateSubjects(subjects);
    }

    /**
     * 更新账户科目
/**
     * @param subjects 科目数据
     * @return 更新结果
     */
    @PostMapping("/subjects/update")
    @Operation(summary = "更新账户科目")
    public ResponseResult updateSubjects(@RequestBody Map<String, Object> subjects) {
        return financeServiceFeign.updateSubjects(subjects);
    }

    /**
     * 删除账户科目
/**
     * @param id 科目ID
     * @return 删除结果
     */
    @PostMapping("/subjects/delete")
    @Operation(summary = "删除账户科目")
    public ResponseResult deleteSubjects(@RequestParam Integer id) {
        return financeServiceFeign.deleteSubjects(id);
    }

    /**
     * 更新科目状态
/**
     * @param id 科目ID
     * @param status 状态
     * @return 更新结果
     */
    @PostMapping("/subjects/update-status")
    @Operation(summary = "更新科目状态")
    public ResponseResult updateSubjectsStatus(
            @RequestParam Integer id,
            @RequestParam Integer status) {
        return financeServiceFeign.updateSubjectsStatus(id, status);
    }
}
