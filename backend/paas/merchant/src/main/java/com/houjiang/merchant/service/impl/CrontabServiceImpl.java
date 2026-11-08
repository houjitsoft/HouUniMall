package com.houjiang.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.config.quartz.QuartzUtils;
import com.houjiang.merchant.service.ICrontabService;
import com.houjiang.merchant.validate.crontab.CrontabCreateValidate;
import com.houjiang.merchant.validate.crontab.CrontabUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.CrontabDetailVo;
import com.houjiang.merchant.vo.CrontabListedVo;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.DevCrontab;
import com.houjiang.merchant.common.mapper.DevCrontabMapper;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 计划任务服务实现类
 */
@Service
public class CrontabServiceImpl implements ICrontabService {

    @Resource
    Scheduler scheduler;

    @Resource
    DevCrontabMapper crontabMapper;

    /**
     * 项目启动初始化任务
     *
     * @author Amos
     * @throws SchedulerException 异常
     */
    @PostConstruct
    public void init() throws SchedulerException {
        // scheduler.clear();
        // List<DevCrontab> jobs = crontabMapper.selectList(new QueryWrapper<DevCrontab>().isNull("delete_time"));
        // for (DevCrontab crontab : jobs) {
        //     QuartzUtils.createScheduleJob(scheduler, crontab);
        // }
    }

    /**
     * 计划任务列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @return PageResult<CrontabListedVo>
     */
    @Override
    public PageResult<CrontabListedVo> list(PageValidate pageValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        IPage<DevCrontab> iPage = crontabMapper.selectPage(new Page<>(pageNo, pageSize),
                new QueryWrapper<DevCrontab>()
                    .isNull("delete_time")
                    .orderByDesc("id"));

        List<CrontabListedVo> list = new LinkedList<>();
        for (DevCrontab crontab : iPage.getRecords()) {
            CrontabListedVo vo = new CrontabListedVo();
            BeanUtils.copyProperties(crontab, vo);

            vo.setTypeDesc("定时任务");
            vo.setStatusDesc(vo.getStatus() == null ? "未运行" : (vo.getStatus().equals(1) ? "运行" : (vo.getStatus().equals(2) ? "停止" : "错误")));

            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 计划任务详情
     *
     * @author Amos
     * @param id 主键
     * @return CrontabDetailVo
     */
    @Override
    public CrontabDetailVo detail(Integer id) {
        DevCrontab crontab = crontabMapper.selectOne(
                new QueryWrapper<DevCrontab>()
                        .eq("id", id)
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.notNull(crontab, "数据不存在!");

        CrontabDetailVo vo = new CrontabDetailVo();
        BeanUtils.copyProperties(crontab, vo);
        return vo;
    }

    /**
     * 计划任务新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    @Transactional
    public void add(CrontabCreateValidate createValidate) throws SchedulerException {
        DevCrontab crontab = new DevCrontab();
        crontab.setName(createValidate.getName());
        crontab.setType(createValidate.getType());
        crontab.setCommand(createValidate.getCommand());
        crontab.setExpression(createValidate.getExpression());
        crontab.setStatus(createValidate.getStatus());
        crontab.setRemark(createValidate.getRemark());
        crontab.setParams(createValidate.getParams());
        crontab.setCreateTime(System.currentTimeMillis() / 1000);
        crontab.setUpdateTime(System.currentTimeMillis() / 1000);
        crontabMapper.insert(crontab);
        if (createValidate.getStatus() != null && createValidate.getStatus().equals(1)) {
            QuartzUtils.createScheduleJob(scheduler, crontab);
        }
    }

    /**
     * 计划任务编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    @Transactional
    public void edit(CrontabUpdateValidate updateValidate) throws SchedulerException {
        DevCrontab crontab = crontabMapper.selectOne(
                new QueryWrapper<DevCrontab>()
                        .eq("id", updateValidate.getId())
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.notNull(crontab, "数据不存在!");

        crontab.setName(updateValidate.getName());
        crontab.setCommand(updateValidate.getCommand());
        crontab.setType(updateValidate.getType());
        crontab.setExpression(updateValidate.getExpression());
        crontab.setStatus(updateValidate.getStatus());
        crontab.setRemark(updateValidate.getRemark());
        crontab.setParams(updateValidate.getParams());
        crontab.setUpdateTime(System.currentTimeMillis() / 1000);
        crontabMapper.updateById(crontab);
        if (updateValidate.getStatus() != null && updateValidate.getStatus().equals(1)) {
            QuartzUtils.createScheduleJob(scheduler, crontab);
        } else {
            scheduler.deleteJob(QuartzUtils.getJobKey(crontab.getId(), crontab.getType().toString()));
        }

    }

    /**
     * 计划任务删除
     *
     * @author Amos
     * @param id 主键
     */
    @Override
    @Transactional
    public void del(Integer id) throws SchedulerException {
        DevCrontab crontab = crontabMapper.selectOne(
                new QueryWrapper<DevCrontab>()
                        .eq("id", id)
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.notNull(crontab, "数据不存在!");

        crontab.setDeleteTime(System.currentTimeMillis() / 1000);
        crontabMapper.updateById(crontab);

        scheduler.deleteJob(QuartzUtils.getJobKey(crontab.getId(), crontab.getType().toString()));
    }

}
