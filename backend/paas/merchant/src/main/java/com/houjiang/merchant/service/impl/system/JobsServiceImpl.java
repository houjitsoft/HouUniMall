package com.houjiang.merchant.service.impl.system;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.service.system.IJobsService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.JobsCreateValidate;
import com.houjiang.merchant.validate.system.JobsSearchValidate;
import com.houjiang.merchant.validate.system.JobsUpdateValidate;
import com.houjiang.merchant.vo.system.JobsExportVo;
import com.houjiang.merchant.vo.system.JobsVo;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.admin.Admin;
import com.houjiang.merchant.common.entity.system.Jobs;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.admin.AdminMapper;
import com.houjiang.merchant.common.mapper.admin.JobsMapper;
import com.houjiang.merchant.common.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 系统岗位服务实现类
 */
@Service
public class JobsServiceImpl implements IJobsService {

    @Resource
    JobsMapper jobsMapper;

    @Resource
    AdminMapper adminMapper;

    /**
     * 岗位所有
     *
     * @author Amos
     * @return List<SystemPostVo>
     */
    @Override
    public List<JobsVo> all() {
        List<Jobs> jobs = jobsMapper.selectList(new QueryWrapper<Jobs>()
                .isNull("delete_time")
                .orderByDesc((Arrays.asList("sort", "id"))));

        List<JobsVo> adminVoArrayList = new ArrayList<>();
        for (Jobs job : jobs) {
            JobsVo vo = new JobsVo();
            BeanUtils.copyProperties(job, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(job.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(job.getUpdateTime()));
            adminVoArrayList.add(vo);
        }

        return adminVoArrayList;
    }

    /**
     * 岗位列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<SystemPostVo>
     */
    @Override
    public PageResult<JobsVo> list(PageValidate pageValidate, JobsSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<Jobs> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(Jobs.class, info->
                                !info.getColumn().equals("delete_time"))
                .isNull("delete_time")
                .orderByDesc(Arrays.asList("sort", "id"));

        jobsMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:code:str",
                "like:name:str",
                "=:status@status:int"
        });

        IPage<Jobs> iPage = jobsMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<JobsVo> list = new ArrayList<>();
        for (Jobs job : iPage.getRecords()) {
            JobsVo vo = new JobsVo();
            BeanUtils.copyProperties(job, vo);
            vo.setStatusDesc(job.getStatus() != null && job.getStatus().intValue() == 1 ? "正常" : "停用");
            vo.setCreateTime(TimeUtils.timestampToDate(job.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(job.getUpdateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 岗位详情
     *
     * @author Amos
     * @param id 主键
     * @return SystemPostVo
     */
    @Override
    public JobsVo detail(Integer id) {
        Jobs systemAuthPost = jobsMapper.selectOne(new QueryWrapper<Jobs>()
                .select(Jobs.class, info ->
                                !info.getColumn().equals("delete_time"))
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(systemAuthPost, "岗位不存在");

        JobsVo vo = new JobsVo();
        BeanUtils.copyProperties(systemAuthPost, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(systemAuthPost.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthPost.getUpdateTime()));

        return vo;
    }

    /**
     * 岗位新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(JobsCreateValidate createValidate) {
        Assert.isNull(jobsMapper.selectOne(new QueryWrapper<Jobs>()
                .select("id,code,name")
                .nested(
                        wq->wq.eq("code", createValidate.getCode())
                                .or()
                                .eq("name", createValidate.getName())
                )
                .isNull("delete_time")
                .last("limit 1")), "该岗位已存在");

        Jobs model = new Jobs();
        model.setCode(createValidate.getCode());
        model.setName(createValidate.getName());
        model.setSort(createValidate.getSort());
        model.setRemark(createValidate.getRemark());
        model.setStatus(createValidate.getStatus());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        jobsMapper.insert(model);
    }

    /**
     * 岗位编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(JobsUpdateValidate updateValidate) {
        Jobs model = jobsMapper.selectOne(new QueryWrapper<Jobs>()
                .select(Jobs.class, info ->
                                !info.getColumn().equals("delete_time"))
                .eq("id", updateValidate.getId())
                .isNull("delete_time")
                .last("limit 1"));
        Assert.notNull(model, "岗位不存在");
        Assert.isNull(jobsMapper.selectOne(new QueryWrapper<Jobs>()
                .select("id,code,name")
                .ne("id", updateValidate.getId())
                .nested(
                        wq->wq.eq("code", updateValidate.getCode())
                                .or()
                                .eq("name", updateValidate.getName())
                )
                .isNull("delete_time")
                .last("limit 1")), "该岗位已存在");
        model.setCode(updateValidate.getCode());
        model.setName(updateValidate.getName());
        model.setSort(updateValidate.getSort());
        model.setRemark(updateValidate.getRemark());
        model.setStatus(updateValidate.getStatus());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        jobsMapper.updateById(model);
    }

    /**
     * 岗位删除
     *
     * @author Amos
     * @param id 主键
     */
    @Override
    public void del(Integer id) {
        Jobs model = jobsMapper.selectOne(new QueryWrapper<Jobs>()
                .select("id,code,name")
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(model, "岗位不存在");

        List<Admin> admins = adminMapper.getByJobs(id);

        Assert.isTrue(admins.isEmpty(), "该岗位已被管理员使用,请先移除");

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        jobsMapper.updateById(model);
    }

    @Override
    public JSONObject getExportData(PageValidate pageValidate, JobsSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();
        PageResult<JobsVo> userVoPageResult = this.list(pageValidate, searchValidate);
        JSONObject ret  = ToolUtils.getExportData(userVoPageResult.getCount(), limit, searchValidate.getPage_start(), searchValidate.getPage_end(),"岗位记录列表");
        return ret;
    }

    @Override
    public String export(JobsSearchValidate searchValidate) {
        PageValidate pageValidate = new PageValidate();
        if (StringUtils.isNotNull(searchValidate.getPage_start())) {
            pageValidate.setPage_no(searchValidate.getPage_start());
        } else {
            pageValidate.setPage_no(1);
        }

        if (StringUtils.isNotNull(searchValidate.getPage_end()) && StringUtils.isNotNull(searchValidate.getPage_size())) {
            pageValidate.setPage_size(searchValidate.getPage_end() * searchValidate.getPage_size());
        } else {
            pageValidate.setPage_size(20);
        }
        Boolean isAll = StringUtils.isNull(searchValidate.getPage_type()) || searchValidate.getPage_type().equals(0) ? true : false;
        List<JobsExportVo> excellist = this.getExcellist(isAll, pageValidate, searchValidate);
        String fileName = StringUtils.isNull(searchValidate.getFile_name()) ? ToolUtils.makeUUID() : searchValidate.getFile_name();
        String folderPath = "/excel/export/"+ TimeUtils.timestampToDay(System.currentTimeMillis() / 1000) +"/" ;
        String path =  folderPath +  fileName +".xlsx";
        String filePath =  YmlUtils.get("merchant.upload-directory") + path;
        File folder = new File(YmlUtils.get("merchant.upload-directory") + folderPath);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new OperateException("创建文件夹失败");
            }
        }
        EasyExcel.write(filePath)
                .head(JobsExportVo.class)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("用户记录")
                .doWrite(excellist);
        return UrlUtils.toShopAbsoluteUrl(path);
    }
    private List<JobsExportVo> getExcellist(boolean isAll, PageValidate pageValidate, JobsSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<Jobs> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(Jobs.class, info->
                        !info.getColumn().equals("delete_time"))
                .isNull("delete_time")
                .orderByDesc(Arrays.asList("sort", "id"));

        jobsMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:code:str",
                "like:name:str",
                "=:status@status:int"
        });
        List<Jobs> jobsList = new ArrayList<>();
        if (isAll) {
            jobsList = jobsMapper.selectList(queryWrapper);
        } else {
            IPage<Jobs> iPage = jobsMapper.selectPage(new Page<>(page, limit), queryWrapper);
            jobsList = iPage.getRecords();
        }

        List<JobsExportVo> list = new ArrayList<>();
        for (Jobs job : jobsList) {
            JobsExportVo vo = new JobsExportVo();
            BeanUtils.copyProperties(job, vo);
            vo.setStatusDesc(job.getStatus() != null && job.getStatus().intValue() == 1 ? "正常" : "停用");
            vo.setCreateTime(TimeUtils.timestampToDate(job.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(job.getUpdateTime()));
            list.add(vo);
        }
        return list;
    }
}
