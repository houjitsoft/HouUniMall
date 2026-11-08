package com.houjiang.merchant.service.impl.user;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.validate.user.UserSearchValidate;
import com.houjiang.merchant.validate.user.UserUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.user.UserWalletValidate;
import com.houjiang.merchant.vo.user.UserListExportVo;
import com.houjiang.merchant.vo.user.UserVo;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.enums.ClientEnum;
import com.houjiang.merchant.common.enums.LogMoneyEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.log.UserAccountLogMapper;
import com.houjiang.merchant.common.mapper.user.UserMapper;
import com.houjiang.merchant.common.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    UserMapper userMapper;

    @Resource
    UserAccountLogMapper logMoneyMapper;

    /**
     * 用户列表
     *
     * @author Amos
     * @param pageValidate (分页参数)
     * @param searchValidate (搜索参数)
     * @return PageResult<UserVo>
     */
    @Override
    public PageResult<UserVo> list(PageValidate pageValidate, UserSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("delete_time");
        queryWrapper.orderByDesc("id");
        queryWrapper.select(User.class, info->
                !info.getColumn().equals("delete_time") &&
                !info.getColumn().equals("update_time") &&
                !info.getColumn().equals("password") &&
                !info.getColumn().equals("salt")
        );

        if (StringUtils.isNotNull(searchValidate.getKey_word()) && StringUtils.isNotEmpty(searchValidate.getKey_word())) {
            String keyword = searchValidate.getKey_word();
            queryWrapper.nested(wq->wq
                    .like("sn", keyword).or()
                    .like("account", keyword).or()
                    .like("nickname", keyword).or()
                    .like("mobile", keyword));
        }

        userMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "=:channel:int",
                "datetime:shop_follow_start-shop_follow_end@create_time:str"
        });

        IPage<User> iPage = userMapper.selectPage( new Page<>(pageNo, pageSize), queryWrapper);

        List<UserVo> list = new LinkedList<>();
        for (User user : iPage.getRecords()) {
            UserVo vo = new UserVo();
            BeanUtils.copyProperties(user, vo);

            vo.setSex(user.getSex());
            vo.setChannelText(ClientEnum.getMsgByCode(user.getChannel()));
            vo.setAvatar(UrlUtils.toShopAbsoluteUrl(user.getAvatar()));
            vo.setLoginTime(TimeUtils.timestampToDate(user.getLoginTime()));
            vo.setCreateTime(TimeUtils.timestampToDate(user.getCreateTime()));
            vo.setShopFollowTime(TimeUtils.timestampToDate(user.getCreateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 用户详情
     *
     * @author Amos
     * @param id 主键
     * @return UserVo
     */
    @Override
    public UserVo detail(Integer id) {
        Assert.notNull(
                userMapper.selectOne(new QueryWrapper<User>()
                        .select("id")
                        .eq("id", id)
                        .isNull("delete_time")
                        .last("limit 1")
                ), "数据不存在!");


        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select(User.class, info->
                    !info.getColumn().equals("delete_time") &&
                    !info.getColumn().equals("update_time") &&
                    !info.getColumn().equals("password") &&
                    !info.getColumn().equals("salt")
                )
                .eq("id", id)
                .last("limit 1"));

        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);

        vo.setSex(user.getSex());
        vo.setAvatar(UrlUtils.toShopAbsoluteUrl(user.getAvatar()));
        vo.setChannelText(ClientEnum.getMsgByCode(user.getChannel()));
        vo.setCreateTime(TimeUtils.timestampToDate(user.getCreateTime()));
        if (user.getLoginTime() <= 0) {
            vo.setLoginTime("无");
        } else {
            vo.setLoginTime(TimeUtils.timestampToDate(user.getLoginTime()));
        }
        return vo;
    }

    /**
     * 用户编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(UserUpdateValidate updateValidate) {
        Integer id = updateValidate.getId();
        String field = updateValidate.getField();
        String value = updateValidate.getValue();

        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(user, "用户不存在!");

        switch (field) {
            case "account":
                if (!user.getAccount().equals(value)) {
                    User u = userMapper.selectOne(new QueryWrapper<User>()
                            .eq("account", value)
                                    .isNull("delete_time")
                            .last("limit 1"));

                    if (StringUtils.isNotNull(u) && !u.getId().equals(id)) {
                        throw new OperateException("当前账号已存在!");
                    }
                }
                Assert.isTrue(value.length() <= 32,"账号不能超过32个字符");
                user.setAccount(value);
                break;
            case "real_name":
                Assert.isTrue(value.length() <= 32,"真实姓名不能超过32个字符");
                user.setRealName(value);
                break;
            case "sex":
                user.setSex(Integer.parseInt(value));
                break;
            case "mobile":
                if (!Pattern.matches("^[1][3-9][0-9]{9}$", value)) {
                    throw new OperateException("手机号格式不正确!");
                }
                User uMobile = userMapper.selectOne(new QueryWrapper<User>()
                        .eq("mobile", value)
                        .isNull("delete_time")
                        .last("limit 1"));
                if (StringUtils.isNotNull(uMobile) && uMobile.getId().equals(id) == false) {
                    throw new OperateException("手机号已被其它账号绑定!");
                }
                user.setMobile(value);
                break;
            default:
                throw new OperateException("不被支持的字段类型!");
        }

        user.setUpdateTime(System.currentTimeMillis() / 1000);
        userMapper.updateById(user);
    }

    /**
     * 余额调整
     *
     * @author Amos
     * @param userWalletValidate 余额
     */
    @Override
    @Transactional
    public void adjustWallet(UserWalletValidate userWalletValidate) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("id", userWalletValidate.getUserId())
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(user,"用户不存在!");

        BigDecimal userMoney = user.getUserMoney();
        BigDecimal amount = userWalletValidate.getNum();
        BigDecimal surplusAmount;
        int changeType;

        if(userWalletValidate.getAction().equals(2) ){
            surplusAmount = userMoney.subtract(amount);
            if(surplusAmount.compareTo(BigDecimal.ZERO) < 0){
                throw new OperateException("用户余额仅剩："+ userMoney);
            }
            changeType = LogMoneyEnum.USER_MONEY_SYS_DEC.getCode();
            logMoneyMapper.dec(user.getId(), changeType, amount, 0, "", userWalletValidate.getRemark(), null);
        }else{
            surplusAmount = userMoney.add(amount);
            changeType = LogMoneyEnum.USER_MONEY_SYS_INC.getCode();
            logMoneyMapper.add(user.getId(), changeType, amount, 0, "", userWalletValidate.getRemark(), null);
        }

        user.setUserMoney(surplusAmount);
        userMapper.updateById(user);
    }

    @Override
    public JSONObject getExportData(PageValidate pageValidate, UserSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();
        PageResult<UserVo> userVoPageResult = this.list(pageValidate, searchValidate);
        JSONObject ret  = ToolUtils.getExportData(userVoPageResult.getCount(), limit, searchValidate.getPage_start(), searchValidate.getPage_end(),"用户记录列表");
        return ret;
    }

    @Override
    public String export(UserSearchValidate searchValidate) {
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
        List<UserListExportVo> excellist = this.getExcellist(isAll, pageValidate, searchValidate);
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
                .head(UserListExportVo.class)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("用户记录")
                .doWrite(excellist);
        return UrlUtils.toShopAbsoluteUrl(path);
    }

    @Override
    public User getById(Integer id) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("id", id).isNull("delete_time"));
    }

    private List<UserListExportVo> getExcellist(boolean isAll, PageValidate pageValidate, UserSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("delete_time");
        queryWrapper.orderByDesc("id");
        queryWrapper.select(User.class, info->
                !info.getColumn().equals("delete_time") &&
                        !info.getColumn().equals("update_time") &&
                        !info.getColumn().equals("password") &&
                        !info.getColumn().equals("salt")
        );

        if (StringUtils.isNotNull(searchValidate.getKey_word()) && StringUtils.isNotEmpty(searchValidate.getKey_word())) {
            String keyword = searchValidate.getKey_word();
            queryWrapper.nested(wq->wq
                    .like("sn", keyword).or()
                    .like("nickname", keyword).or()
                    .like("mobile", keyword));
        }

        userMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "=:channel:int",
                "datetime:createTimeStart-createTimeEnd@create_time:str"
        });

        List<UserListExportVo> retList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        if (isAll) {
            userList = userMapper.selectList(queryWrapper);
        } else {
            IPage<User> iPage = userMapper.selectPage( new Page<>(page, limit), queryWrapper);
            userList = iPage.getRecords();
        }
        for (User user : userList) {
            UserListExportVo vo = new UserListExportVo();
            BeanUtils.copyProperties(user, vo);
            vo.setSex(user.getSex());
            vo.setChannel(ClientEnum.getMsgByCode(user.getChannel()));
            vo.setAvatar(UrlUtils.toShopAbsoluteUrl(user.getAvatar()));
            vo.setLoginTime(TimeUtils.timestampToDate(user.getLoginTime()));
            vo.setCreateTime(TimeUtils.timestampToDate(user.getCreateTime()));
            retList.add(vo);
        }

        return retList;
    }
}
