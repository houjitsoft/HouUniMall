package com.houjiang.merchant.service.system;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.JobsCreateValidate;
import com.houjiang.merchant.validate.system.JobsSearchValidate;
import com.houjiang.merchant.validate.system.JobsUpdateValidate;
import com.houjiang.merchant.vo.system.JobsVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.List;

/**
 * 系统岗位服务接口类
 */
public interface IJobsService {

    /**
     * 岗位所有
     *
     * @author Amos
     * @return List<SystemPostVo>
     */
    List<JobsVo> all();

    /**
     * 岗位列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<SystemPostVo>
     */
    PageResult<JobsVo> list(PageValidate pageValidate, JobsSearchValidate searchValidate);

    /**
     * 岗位详情
     *
     * @author Amos
     * @param id 主键
     * @return SystemPostVo
     */
    JobsVo detail(Integer id);

    /**
     * 岗位新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(JobsCreateValidate createValidate);

    /**
     * 岗位编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(JobsUpdateValidate updateValidate);

    /**
     * 岗位删除
     *
     * @author Amos
     * @param id 主键
     */
    void del(Integer id);

    /**
     * 返回导出格式
     * @return
     */
    JSONObject getExportData(PageValidate pageValidate, JobsSearchValidate searchValidate);

    /**
     * 导出
     */
    String export(JobsSearchValidate searchValidate);
}
