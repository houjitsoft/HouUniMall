package com.houjiang.merchant.controller;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ICrontabService;
import com.houjiang.merchant.validate.crontab.CrontabCreateValidate;
import com.houjiang.merchant.validate.crontab.CrontabUpdateValidate;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.CrontabDetailVo;
import com.houjiang.merchant.vo.CrontabListedVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/adminapi/crontab.crontab")
public class CrontabController {

    @Resource
    ICrontabService iCrontabService;

    @GetMapping("/lists")
    @Operation(summary="计划任务列表")
    public AjaxResult< PageResult<CrontabListedVo>> list(@Validated PageValidate pageValidate) {
        PageResult<CrontabListedVo> list = iCrontabService.list(pageValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="计划任务详情")
    public AjaxResult<Object> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        CrontabDetailVo vo = iCrontabService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "计划任务新增")
    @PostMapping("/add")
    @Operation(summary="计划任务新增")
    public AjaxResult<Object> add(@Validated @RequestBody CrontabCreateValidate createValidate) throws SchedulerException {
        iCrontabService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "计划任务编辑")
    @PostMapping("/edit")
    @Operation(summary="计划任务编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody CrontabUpdateValidate updateValidate) throws SchedulerException {
        iCrontabService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "计划任务删除")
    @PostMapping("/delete")
    @Operation(summary="计划任务删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) throws SchedulerException {
        iCrontabService.del(idValidate.getId());
        return AjaxResult.success();
    }


    @GetMapping("/expression")
    @Operation(summary="计划任务列表")
    public AjaxResult<List<Map<String, String>>> expression(@Validated @RequestParam("expression") String expression) {
        return AjaxResult.success(getExpression(expression));
    }


    public static List<Map<String, String>> getExpression(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            List<Date> dates = new ArrayList<>();

            Date date = new Date();
            for (int i = 0; i < 5; i++) {
                // 假设从当前时间开始
                Date nextValidTimeAfter = cron.getNextValidTimeAfter(date);
                if (nextValidTimeAfter != null) {
                    dates.add(nextValidTimeAfter);
                    date = nextValidTimeAfter;
                } else {
                    // 如果没有更多的有效时间，则退出循环
                    break;
                }
            }

            List<Map<String, String>> lists = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < dates.size(); i++) {
                Map<String, String> entry = new HashMap<>();
                entry.put("time", String.valueOf(i + 1));
                entry.put("date", sdf.format(dates.get(i)));
                lists.add(entry);
            }

            // 添加额外的条目
            Map<String, String> extraEntry = new HashMap<>();
            extraEntry.put("time", "x");
            extraEntry.put("date", "……");
            lists.add(extraEntry);

            return lists;
        } catch (Exception e) {
            log.error("Cron表达式解析失败", e);
            throw new OperateException("Cron表达式解析失败，请检查后重试");
        }
    }

}
