package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.crontab.CrontabCreateValidate;
import com.houjiang.merchant.validate.crontab.CrontabUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.CrontabDetailVo;
import com.houjiang.merchant.vo.CrontabListedVo;
import com.houjiang.merchant.common.core.PageResult;
import org.quartz.SchedulerException;

/**
 * 计划任务服务接口类
 */
public interface ICrontabService {

    /**
     * 计划任务列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @return PageResult<CrontabListedVo>
     */
    PageResult<CrontabListedVo> list(PageValidate pageValidate);

    /**
     * 计划任务详情
     *
     * @author Amos
     * @param id 主键
     * @return CrontabDetailVo
     */
    CrontabDetailVo detail(Integer id);

    /**
     * 计划任务新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(CrontabCreateValidate createValidate) throws SchedulerException;

    /**
     * 计划任务编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(CrontabUpdateValidate updateValidate) throws SchedulerException;

    /**
     * 计划任务删除
     *
     * @author Amos
     * @param id 主键
     */
    void del(Integer id) throws SchedulerException;

}
