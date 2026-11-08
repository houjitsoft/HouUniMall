package com.houjiang.merchant.service.impl.mall;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.PasswordUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.util.RequestUtils;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.common.util.YmlUtils;
import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.service.mall.IMallShopAdminDeptService;
import com.houjiang.merchant.service.mall.IMallShopAdminJobsService;
import com.houjiang.merchant.service.mall.IMallShopAdminRoleService;
import com.houjiang.merchant.service.mall.IMallShopAdminService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopAdminCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopAdminSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopAdminUpdateValidate;
import com.houjiang.merchant.validate.shop.MallShopAccountUpdateValidate;
import com.houjiang.merchant.vo.admin.AdminAccountDetailVo;
import com.houjiang.merchant.vo.mall.MallShopAdminDetailVo;
import com.houjiang.merchant.vo.mall.MallShopAdminListedVo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class MallShopAdminServiceImpl implements IMallShopAdminService {

    @Resource private IMallShopAdminJobsService jobsService;
    @Resource private IMallShopAdminDeptService deptService;
    @Resource private IMallShopAdminRoleService roleService;
    @Resource private MallShopAdminMapper mallShopAdminMapper;
    @Resource private JdbcTemplate jdbcTemplate;

    @Override
    public AdminAccountDetailVo accountDetail(Integer adminId) {
        MallShopAdmin admin = findAdmin(adminId);
        AdminAccountDetailVo vo = new AdminAccountDetailVo();
        BeanUtils.copyProperties(admin, vo);
        fillAccount(vo, admin);
        vo.setFace(UrlUtils.toShopAbsoluteUrl(admin.getFace()));
        vo.setCreateTime(TimeUtils.timestampToDate(admin.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(admin.getUpdateTime()));
        vo.setLoginLastTime(TimeUtils.timestampToDate(admin.getLoginLastTime()));
        vo.setRoleId(roleService.getRoleIdAttr(admin.getId()));
        vo.setRoleArr(roleService.getRoleNameAttr(admin.getId()));
        vo.setJobId(jobsService.getJobIdAttr(admin.getId()));
        vo.setJobArr(jobsService.getJobNameAttr(admin.getId()));
        vo.setDeptId(deptService.getDeptIdAttr(admin.getId()));
        vo.setDeptArr(deptService.getDeptNameAttr(admin.getId()));
        return vo;
    }

    @Override
    public MallShopAdmin getById(Integer id) {
        return findAdmin(id);
    }

    @Override
    @Transactional
    public void accountUpdate(MallShopAccountUpdateValidate request, Integer id) {
        MallShopAdmin admin = findAdmin(id);
        String avatar = StrUtil.isNotEmpty(request.getFace())
                ? UrlUtils.toShopRelativeUrl(request.getFace()) : "/api/static/backend_avatar.jpg";
        admin.setFace(avatar);
        admin.setName(request.getName());
        admin.setUpdateTime(System.currentTimeMillis() / 1000);

        if (StrUtil.isNotEmpty(request.getUpdateNewPassword())) {
            Map<String, Object> user = findUser(admin.getUserId());
            String stored = user == null ? null : stringValue(user.get("password"));
            boolean matched = stored != null && (stored.startsWith("$2")
                    ? Boolean.TRUE.equals(PasswordUtils.BCryptPasswordMatches(request.getUpdateCurrentPassword(), stored))
                    : ToolUtils.makePassword(request.getUpdateCurrentPassword()).equals(stored));
            if (!matched) throw new OperateException("当前密码不正确");
            if (request.getUpdateNewPassword().length() < 6 || request.getUpdateNewPassword().length() > 32) {
                throw new OperateException("新密码长度必须为6到32位");
            }
            if (!request.getUpdateNewPassword().equals(request.getUpdateConfirmPassword())) {
                throw new OperateException("确认密码必须和新密码一致");
            }
            jdbcTemplate.update("UPDATE hou_user_info SET password = ? WHERE user_id = ?",
                    PasswordUtils.BCryptPasswordEncoder(request.getUpdateNewPassword().trim()), admin.getUserId());
        }
        mallShopAdminMapper.updateById(admin);
        if (StrUtil.isNotEmpty(request.getUpdateNewPassword())) {
            String token = RequestUtils.handler().getHeader(YmlUtils.get("sa-token.token-name"));
            RedisCache cache = SpringUtils.getBean(RedisCache.class);
            String tokenName = YmlUtils.get("sa-token.token-name");
            cache.deleteObject("merchant:" + tokenName + ":login:session:" + id);
            cache.deleteObject("merchant:" + tokenName + ":login:token:" + token);
        }
    }

    @Override
    public PageResult<MallShopAdminListedVo> list(PageValidate page, MallShopAdminSearchValidate search) {
        QueryWrapper<MallShopAdmin> query = buildQuery(search);
        IPage<MallShopAdmin> result = mallShopAdminMapper.selectPage(
                new Page<>(page.getPage_no(), page.getPage_size()), query);
        List<MallShopAdminListedVo> rows = new LinkedList<>();
        for (MallShopAdmin admin : result.getRecords()) rows.add(toListed(admin));
        return PageResult.iPageHandle(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public List<MallShopAdminListedVo> all(MallShopAdminSearchValidate search) {
        List<MallShopAdminListedVo> rows = new LinkedList<>();
        for (MallShopAdmin admin : mallShopAdminMapper.selectList(buildQuery(search))) rows.add(toListed(admin));
        return rows;
    }

    @Override
    public List<Map<String, Object>> accountOptions(String keyword) {
        String filter = "";
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            filter = " AND (user_name LIKE ? OR user_mobile LIKE ? OR user_nickname LIKE ?)";
            String like = "%" + keyword.trim() + "%";
            args.add(like); args.add(like); args.add(like);
        }
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT user_id AS userId, user_name AS userName, user_mobile AS userMobile, "
                        + "user_nickname AS userNickname FROM hou_user_info "
                        + "WHERE COALESCE(is_delete, 0) = 0 AND user_state IN (1, 2) "
                        + "AND password IS NOT NULL AND password <> ''" + filter
                        + " ORDER BY user_id DESC LIMIT 50", args.toArray());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> user : users) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", user.get("userId"));
            item.put("loginAccount", firstText(user.get("userMobile"), user.get("userName")));
            item.put("displayName", firstText(user.get("userNickname"), user.get("userName")));
            result.add(item);
        }
        return result;
    }

    @Override
    public MallShopAdminDetailVo detail(Integer id) {
        MallShopAdmin admin = findAdmin(id);
        checkShop(admin);
        MallShopAdminDetailVo vo = new MallShopAdminDetailVo();
        BeanUtils.copyProperties(admin, vo);
        fillAccount(vo, admin);
        vo.setDeptArr(deptService.getDeptNameAttr(id)); vo.setDeptId(deptService.getDeptIdAttr(id));
        vo.setJobsArr(jobsService.getJobNameAttr(id)); vo.setJobsId(jobsService.getJobIdAttr(id));
        vo.setRoleArr(roleService.getRoleNameAttr(id)); vo.setRoleId(roleService.getRoleIdAttr(id));
        vo.setFace(UrlUtils.toShopAbsoluteUrl(admin.getFace()));
        return vo;
    }

    @Override
    @Transactional
    public void add(MallShopAdminCreateValidate request) {
        checkUser(request.getUserId());
        Assert.isNull(mallShopAdminMapper.selectOne(new QueryWrapper<MallShopAdmin>().select("id")
                .eq("shop_id", request.getShopId()).eq("user_id", request.getUserId())
                .eq("delete_time", 0).last("limit 1")), "该统一用户已经是当前店铺管理员");
        MallShopAdmin admin = new MallShopAdmin();
        long now = System.currentTimeMillis() / 1000;
        admin.setCreateTime(now); admin.setUpdateTime(now); admin.setDeleteTime(0L);
        admin.setShopId(request.getShopId()); admin.setUserId(request.getUserId());
        admin.setIsSuper(request.getIsSuper()); admin.setStatus(request.getStatus());
        admin.setLoginAll(request.getLoginAll()); admin.setName(request.getName());
        admin.setFace(StrUtil.isNotEmpty(request.getFace()) ? UrlUtils.toRelativeUrl(request.getFace()) : "/api/static/backend_avatar.png");
        mallShopAdminMapper.insert(admin);
        deptService.batchInsert(admin.getId(), request.getDeptId(), admin.getShopId());
        jobsService.batchInsert(admin.getId(), request.getJobsId(), admin.getShopId());
        roleService.batchInsert(admin.getId(), request.getRoleId(), admin.getShopId());
    }

    @Override
    @Transactional
    public void edit(MallShopAdminUpdateValidate request) {
        MallShopAdmin current = findAdmin(request.getId());
        checkShop(current);
        if (request.getUserId() != null && !request.getUserId().equals(current.getUserId())) {
            checkUser(request.getUserId());
            Assert.isNull(mallShopAdminMapper.selectOne(new QueryWrapper<MallShopAdmin>().select("id")
                    .eq("shop_id", current.getShopId()).eq("user_id", request.getUserId())
                    .eq("delete_time", 0).ne("id", request.getId()).last("limit 1")),
                    "该统一用户已经是当前店铺管理员");
        }
        MallShopAdmin update = new MallShopAdmin();
        update.setId(request.getId()); update.setUpdateTime(System.currentTimeMillis() / 1000);
        update.setUserId(request.getUserId() == null ? current.getUserId() : request.getUserId());
        update.setName(request.getName()); update.setStatus(request.getStatus()); update.setLoginAll(request.getLoginAll());
        if (request.getFace() != null) update.setFace(UrlUtils.toShopRelativeUrl(request.getFace()));
        mallShopAdminMapper.updateById(update);
        if (request.getDeptId() != null) { deptService.deleteByAdminId(current.getId()); deptService.batchInsert(current.getId(), request.getDeptId(), current.getShopId()); }
        if (request.getJobsId() != null) { jobsService.deleteByAdminId(current.getId()); jobsService.batchInsert(current.getId(), request.getJobsId(), current.getShopId()); }
        if (request.getRoleId() != null) { roleService.deleteByAdminId(current.getId()); roleService.batchInsert(current.getId(), request.getRoleId(), current.getShopId()); }
    }

    @Override
    public void del(Integer id) {
        MallShopAdmin admin = findAdmin(id);
        checkShop(admin);
        if (Integer.valueOf(1).equals(admin.getIsSuper())) throw new OperateException("不能删除商家主账号");
        admin.setDeleteTime(System.currentTimeMillis() / 1000);
        admin.setUpdateTime(System.currentTimeMillis() / 1000);
        mallShopAdminMapper.updateById(admin);
    }

    private QueryWrapper<MallShopAdmin> buildQuery(MallShopAdminSearchValidate search) {
        QueryWrapper<MallShopAdmin> query = new QueryWrapper<MallShopAdmin>().eq("delete_time", 0).orderByDesc("id");
        if (search.getShopId() != null) query.eq("shop_id", search.getShopId());
        if (search.getStatus() != null) query.eq("status", search.getStatus());
        if (search.getName() != null && !search.getName().isBlank()) query.like("name", search.getName());
        if (search.getRole_id() != null) {
            List<Integer> ids = roleService.getAdminIdsByRoleId(search.getRole_id()); ids.add(0); query.in("id", ids);
        }
        if (search.getAccount() != null && !search.getAccount().isBlank()) {
            String like = "%" + search.getAccount().trim() + "%";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                    "SELECT user_id FROM hou_user_info WHERE user_mobile LIKE ? OR user_name LIKE ?", like, like);
            List<Long> ids = new ArrayList<>(); for (Map<String, Object> user : users) ids.add(((Number) user.get("user_id")).longValue());
            ids.add(0L); query.in("user_id", ids);
        }
        return query;
    }

    private MallShopAdmin findAdmin(Integer id) {
        MallShopAdmin result = mallShopAdminMapper.selectOne(new QueryWrapper<MallShopAdmin>().eq("id", id).eq("delete_time", 0).last("limit 1"));
        Assert.notNull(result, "账号不存在");
        return result;
    }

    private void checkShop(MallShopAdmin admin) {
        if (!admin.getShopId().equals(MerchantThreadLocal.getShopId())) throw new OperateException("不能操作其他店铺的账号");
    }

    private void checkUser(Long userId) {
        if (userId == null || findUser(userId) == null) throw new OperateException("统一用户不存在或已停用");
    }

    private Map<String, Object> findUser(Long userId) {
        return jdbcTemplate.queryForList("SELECT user_id, user_mobile, user_name, user_nickname, password "
                        + "FROM hou_user_info WHERE user_id = ? AND COALESCE(is_delete, 0) = 0 "
                        + "AND user_state IN (1, 2) AND password IS NOT NULL AND password <> '' LIMIT 1", userId)
                .stream().findFirst().orElse(null);
    }

    private void fillAccount(Object vo, MallShopAdmin admin) {
        Map<String, Object> user = findUser(admin.getUserId());
        if (vo instanceof AdminAccountDetailVo detail) {
            detail.setUserId(admin.getUserId()); detail.setLoginAccount(firstText(userValue(user, "user_mobile"), userValue(user, "user_name")));
            detail.setUserMobile(userValue(user, "user_mobile"));
        } else if (vo instanceof MallShopAdminListedVo listed) {
            listed.setUserId(admin.getUserId()); listed.setLoginAccount(firstText(userValue(user, "user_mobile"), userValue(user, "user_name")));
            listed.setUserMobile(userValue(user, "user_mobile")); listed.setUserName(userValue(user, "user_name"));
        } else if (vo instanceof MallShopAdminDetailVo detail) {
            detail.setUserId(admin.getUserId()); detail.setLoginAccount(firstText(userValue(user, "user_mobile"), userValue(user, "user_name")));
            detail.setUserMobile(userValue(user, "user_mobile")); detail.setUserName(userValue(user, "user_name"));
        }
    }

    private MallShopAdminListedVo toListed(MallShopAdmin admin) {
        MallShopAdminListedVo vo = new MallShopAdminListedVo(); BeanUtils.copyProperties(admin, vo); fillAccount(vo, admin);
        vo.setCreateTime(TimeUtils.timestampToDate(admin.getCreateTime())); vo.setUpdateTime(TimeUtils.timestampToDate(admin.getUpdateTime()));
        vo.setLoginLastTime(admin.getLoginLastTime() == null || admin.getLoginLastTime() == 0 ? "-" : TimeUtils.timestampToDate(admin.getLoginLastTime()));
        vo.setDeptArr(deptService.getDeptNameAttr(admin.getId())); vo.setDeptId(deptService.getDeptIdAttr(admin.getId()));
        vo.setJobsArr(jobsService.getJobNameAttr(admin.getId())); vo.setJobsId(jobsService.getJobIdAttr(admin.getId()));
        vo.setRoleArr(roleService.getRoleNameAttr(admin.getId())); vo.setRoleId(roleService.getRoleIdAttr(admin.getId()));
        vo.setFace(UrlUtils.toShopAbsoluteUrl(admin.getFace())); return vo;
    }

    private String userValue(Map<String, Object> user, String key) { return user == null ? "" : stringValue(user.get(key)); }
    private String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
    private String firstText(Object... values) { for (Object value : values) { String text = stringValue(value); if (!text.isBlank()) return text; } return ""; }
}
