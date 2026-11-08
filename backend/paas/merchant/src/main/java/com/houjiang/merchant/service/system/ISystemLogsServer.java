package com.houjiang.merchant.service.system;

import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemSearchLoginsValidate;
import com.houjiang.merchant.validate.system.SystemSearchOperateValidate;
import com.houjiang.merchant.vo.system.SystemLogLoginVo;
import com.houjiang.merchant.vo.system.SystemLogOperateVo;
import com.houjiang.merchant.common.core.PageResult;

/**
 * 系统日志服务类接口类
 */
public interface ISystemLogsServer {

    /**
     * 系统操作日志
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<LogOperateVo>
     */
    PageResult<SystemLogOperateVo> operate(PageValidate pageValidate, SystemSearchOperateValidate searchValidate);

    /**
     * 系统登录日志
     *
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<LogLoginVo>
     */
    PageResult<SystemLogLoginVo> login(PageValidate pageValidate, SystemSearchLoginsValidate searchValidate);

}
