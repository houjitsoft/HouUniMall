package com.houjiang.merchant.service.user;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.validate.user.UserSearchValidate;
import com.houjiang.merchant.validate.user.UserUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.user.UserWalletValidate;
import com.houjiang.merchant.vo.user.UserVo;
import com.houjiang.merchant.common.core.PageResult;

/**
 * 用户服务接口类
 */
public interface IUserService {

    /**
     * 用户列表
     *
     * @author Amos
     * @param pageValidate (分页参数)
     * @param searchValidate (搜索参数)
     * @return PageResult<UserVo>
     */
    PageResult<UserVo> list(PageValidate pageValidate, UserSearchValidate searchValidate);

    /**
     * 用户详情
     *
     * @author Amos
     * @param id 主键
     * @return UserVo
     */
    UserVo detail(Integer id);

    /**
     * 用户编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(UserUpdateValidate updateValidate);

    /**
     * 余额调整
     *
     * @author Amos
     * @param userWalletValidate 余额
     */
    void adjustWallet(UserWalletValidate userWalletValidate);

    /**
     * 返回导出格式
     * @return
     */
    JSONObject getExportData(PageValidate pageValidate, UserSearchValidate searchValidate);

    /**
     * 导出
     */
    String export(UserSearchValidate searchValidate);

    /**
     * 根据ID返回用户信息
     * @param id
     * @return
     */
    User getById(Integer id);
}
