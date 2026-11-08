package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.PasswordUtils;
import com.houjiang.commons.util.SnowflakeIdWorker;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.entity.mall.MallShopCategory;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopCategoryMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.validate.mall.MallShopProvisionCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopProvisionUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopProvisionListedVo;
import com.houjiang.merchant.vo.mall.MallShopProvisionResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MallShopProvisionService {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final MallShopMapper shopMapper;
    private final MallShopAdminMapper adminMapper;
    private final MallShopCategoryMapper categoryMapper;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public MallShopProvisionResultVo create(MallShopProvisionCreateValidate request) {
        String mode = text(request.getAccountMode());
        if (!"existing".equals(mode) && !"new".equals(mode)) throw new OperateException("请选择统一账号来源");
        if ("existing".equals(mode)) requiredLong(request.getPlatformUserId(), "请选择已有账号");
        else validateNewAccount(request);

        String name = requiredText(request.getName(), "请输入店铺名称");
        if (shopMapper.selectCount(new QueryWrapper<MallShop>().eq("delete_time", 0).eq("name", name)) > 0) {
            throw new OperateException("店铺名称已存在");
        }
        if (request.getShopCategoryId() != null && categoryMapper.selectOne(new QueryWrapper<MallShopCategory>()
                .select("id").eq("id", request.getShopCategoryId()).eq("delete_time", 0).eq("status", 1)
                .last("limit 1")) == null) throw new OperateException("主营类目不存在或已停用");

        long now = System.currentTimeMillis() / 1000;
        MallShop shop = new MallShop();
        shop.setCreateTime(now); shop.setUpdateTime(now); shop.setDeleteTime(0L);
        shop.setName(name); shop.setLogo(""); shop.setDescStr("");
        shop.setShopCategoryId(request.getShopCategoryId()); shop.setType(request.getType());
        shop.setShopStatus(request.getShopStatus()); shop.setBusinessStatus(request.getBusinessStatus());
        shop.setContact(requiredText(request.getContact(), "请输入联系人"));
        shop.setMobile(JSONArray.toJSONString(Collections.singletonList(requiredText(request.getMobile(), "请输入联系电话"))));
        shop.setAddressInfo(text(request.getAddressInfo())); shop.setLat(text(request.getLat())); shop.setLng(text(request.getLng()));
        shop.setReturnContact(""); shop.setReturnMobile(""); shop.setReturnAddressInfo("");
        shop.setHomeRecommend(0); shop.setGoodsAudit(0); shop.setBusinessLicense(""); shop.setOtherLicense("[]");
        shop.setServiceRatio(BigDecimal.ZERO); shop.setScore(BigDecimal.ZERO); shop.setStar(BigDecimal.ZERO);
        shop.setVisitNum(0); shop.setMoney(BigDecimal.ZERO); shop.setSaleMoney(0); shop.setSaleOrders(0);
        shop.setSaleOnlineGoods(0); shop.setGatherMoney(0);
        shopMapper.insert(shop);

        AccountGrant grant = resolveAccount(request, shop.getId());
        return new MallShopProvisionResultVo(shop.getId(), grant.loginAccount());
    }

    public PageResult<MallShopProvisionListedVo> list(Map<String, Object> params) {
        int pageNo = positiveInt(params.get("page"), 1);
        int pageSize = Math.min(positiveInt(params.get("size"), 15), 100);
        QueryWrapper<MallShop> query = new QueryWrapper<MallShop>().eq("delete_time", 0).orderByDesc("id");
        if (hasText(params.get("shopName"))) query.like("name", text(params.get("shopName")));
        Integer type = nullableInt(params.get("shopType")); if (type != null) query.eq("type", type == 1 ? 0 : 1);
        Integer status = nullableInt(params.get("status")); if (status != null) query.eq("shop_status", status);
        Integer categoryId = nullableInt(params.get("shopCategoryId")); if (categoryId != null) query.eq("shop_category_id", categoryId);
        Integer businessStatus = nullableInt(params.get("businessStatus")); if (businessStatus != null) query.eq("business_status", businessStatus);
        Page<MallShop> result = shopMapper.selectPage(new Page<>(pageNo, pageSize), query);
        List<MallShopProvisionListedVo> rows = result.getRecords().stream().map(this::toListedVo).collect(Collectors.toList());
        return PageResult.iPageHandle(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    public MallShopProvisionListedVo detail(Integer shopId) {
        if (shopId == null || shopId <= 0) throw new OperateException("店铺ID不正确");
        MallShop shop = shopMapper.selectOne(new QueryWrapper<MallShop>()
                .eq("id", shopId).eq("delete_time", 0).last("limit 1"));
        if (shop == null) throw new OperateException("店铺不存在");
        return toListedVo(shop);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(MallShopProvisionUpdateValidate request) {
        MallShop current = shopMapper.selectOne(new QueryWrapper<MallShop>()
                .select("id", "shop_status").eq("id", request.getId())
                .eq("delete_time", 0).last("limit 1"));
        if (current == null) throw new OperateException("店铺不存在");

        String name = requiredText(request.getName(), "请输入店铺名称");
        if (shopMapper.selectCount(new QueryWrapper<MallShop>().eq("delete_time", 0)
                .eq("name", name).ne("id", request.getId())) > 0) {
            throw new OperateException("店铺名称已存在");
        }
        if (request.getShopCategoryId() != null && categoryMapper.selectOne(new QueryWrapper<MallShopCategory>()
                .select("id").eq("id", request.getShopCategoryId()).eq("delete_time", 0)
                .eq("status", 1).last("limit 1")) == null) {
            throw new OperateException("主营类目不存在或已停用");
        }

        long now = System.currentTimeMillis() / 1000;
        UpdateWrapper<MallShop> update = new UpdateWrapper<MallShop>()
                .eq("id", request.getId()).eq("delete_time", 0)
                .set("name", name)
                .set("type", request.getType())
                .set("shop_category_id", request.getShopCategoryId())
                .set("contact", requiredText(request.getContact(), "请输入联系人"))
                .set("mobile", JSONArray.toJSONString(Collections.singletonList(requiredText(request.getMobile(), "请输入联系手机"))))
                .set("address_info", text(request.getAddressInfo()))
                .set("lat", text(request.getLat()))
                .set("lng", text(request.getLng()))
                .set("shop_status", request.getShopStatus())
                .set("business_status", request.getBusinessStatus())
                .set("update_time", now);
        shopMapper.update(null, update);

        if (!request.getShopStatus().equals(current.getShopStatus())) {
            MallShopAdmin admin = adminMapper.selectOne(new QueryWrapper<MallShopAdmin>().select("id")
                    .eq("shop_id", request.getId()).eq("delete_time", 0).eq("super", 1)
                    .orderByAsc("id").last("limit 1"));
            if (admin != null) {
                MallShopAdmin adminUpdate = new MallShopAdmin();
                adminUpdate.setId(admin.getId());
                adminUpdate.setStatus(request.getShopStatus());
                adminUpdate.setUpdateTime(now);
                adminMapper.updateById(adminUpdate);
            }
        }
    }

    public List<Map<String, Object>> options() {
        return categoryMapper.selectList(new QueryWrapper<MallShopCategory>().select("id", "name")
                        .eq("delete_time", 0).eq("status", 1).orderByAsc("sort", "id"))
                .stream().map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName())).collect(Collectors.toList());
    }

    public List<Map<String, Object>> accountOptions(Map<String, Object> params) {
        String keyword = text(params == null ? null : params.get("keyword"));
        List<Object> args = new ArrayList<>(); String filter = "";
        if (hasText(keyword)) {
            filter = " AND (u.user_name LIKE ? OR u.user_mobile LIKE ? OR u.user_nickname LIKE ?)";
            String like = "%" + keyword + "%"; args.add(like); args.add(like); args.add(like);
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT u.user_id AS userId, u.user_name AS userName, u.user_mobile AS userMobile, "
                        + "u.user_nickname AS userNickname, a.merchant_id AS grantedMerchantId, m.name AS grantedMerchantName "
                        + "FROM hou_user_info u LEFT JOIN hou_mall_merchant_account a ON a.user_id = u.user_id "
                        + "LEFT JOIN hou_merchant_mall_shop m ON m.id = a.merchant_id "
                        + "WHERE COALESCE(u.is_delete, 0) = 0 AND u.user_state IN (1, 2) "
                        + "AND u.password IS NOT NULL AND u.password <> ''" + filter + " ORDER BY u.user_id DESC LIMIT 30",
                args.toArray());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String account = preferredLoginAccount(row); if (!hasText(account)) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", String.valueOf(row.get("userId"))); item.put("loginAccount", account);
            item.put("displayName", firstText(row.get("userNickname"), row.get("userName"), account));
            item.put("accountType", EMAIL_PATTERN.matcher(account).matches() ? "email" : "mobile");
            item.put("grantedMerchantId", row.get("grantedMerchantId")); item.put("grantedMerchantName", row.get("grantedMerchantName"));
            result.add(item);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer shopId, Integer status) {
        if (shopId == null || status == null || (status != 0 && status != 1)) throw new OperateException("店铺状态参数不正确");
        MallShop shop = shopMapper.selectOne(new QueryWrapper<MallShop>().select("id").eq("id", shopId)
                .eq("delete_time", 0).last("limit 1"));
        if (shop == null) throw new OperateException("店铺不存在");
        MallShop update = new MallShop(); update.setId(shopId); update.setShopStatus(status); update.setUpdateTime(System.currentTimeMillis() / 1000);
        shopMapper.updateById(update);
        MallShopAdmin admin = adminMapper.selectOne(new QueryWrapper<MallShopAdmin>().select("id").eq("shop_id", shopId)
                .eq("delete_time", 0).eq("super", 1).last("limit 1"));
        if (admin != null) { MallShopAdmin au = new MallShopAdmin(); au.setId(admin.getId()); au.setStatus(status); au.setUpdateTime(System.currentTimeMillis() / 1000); adminMapper.updateById(au); }
    }

    private AccountGrant resolveAccount(MallShopProvisionCreateValidate request, Integer shopId) {
        if ("existing".equals(text(request.getAccountMode()))) {
            long userId = requiredLong(request.getPlatformUserId(), "请选择已有账号");
            Map<String, Object> account = findActiveAccount(userId);
            if (account == null) throw new OperateException("所选账号不存在或已停用");
            String loginAccount = preferredLoginAccount(account); if (!hasText(loginAccount)) throw new OperateException("账号缺少手机号或邮箱");
            grantPermission(userId, firstText(account.get("userNickname"), account.get("userName"), loginAccount), shopId);
            return new AccountGrant(String.valueOf(userId), loginAccount);
        }
        String identifier = requiredText(request.getAccountIdentifier(), "请输入手机号或邮箱");
        if (EMAIL_PATTERN.matcher(identifier).matches()) identifier = identifier.toLowerCase();
        String password = requiredText(request.getAccountPassword(), "请设置初始密码");
        String userId = SnowflakeIdWorker.getSnowId(); String passwordHash = PasswordUtils.BCryptPasswordEncoder(password);
        jdbcTemplate.update("INSERT INTO hou_user_info (user_id, user_mobile, password, international_dialing_code, user_name, user_state, user_integral, user_count_login, user_is_new, user_nickname, create_time, is_business, shop_id, follower_num, follow_num, register_terminal, is_delete, is_admin) VALUES (?, ?, ?, '+86', ?, 1, 0, 0, 1, ?, NOW(), 1, ?, 0, 0, 'houcloud', 0, 0)",
                userId, MOBILE_PATTERN.matcher(identifier).matches() ? identifier : null, passwordHash, identifier, identifier, shopId);
        jdbcTemplate.update("INSERT INTO hou_user_info_detail (user_id, user_reg_time, user_reg_ip, user_mobile_verify, user_parent_id, user_platform) VALUES (?, UNIX_TIMESTAMP(), '127.0.0.1', 1, '0', 5)", userId);
        grantPermission(Long.parseLong(userId), identifier, shopId);
        return new AccountGrant(userId, identifier);
    }

    private void validateNewAccount(MallShopProvisionCreateValidate request) {
        String identifier = text(request.getAccountIdentifier());
        if (!MOBILE_PATTERN.matcher(identifier).matches() && !EMAIL_PATTERN.matcher(identifier).matches()) throw new OperateException("请输入正确的手机号或邮箱");
        Long duplicate = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hou_user_info WHERE COALESCE(is_delete, 0) = 0 AND (user_mobile = ? OR user_name = ?)", Long.class, identifier, identifier);
        if (duplicate != null && duplicate > 0) throw new OperateException("手机号或邮箱已注册，请选择已有账号");
        String password = text(request.getAccountPassword());
        if (password.length() < 6 || password.length() > 32 || !password.equals(text(request.getAccountPasswordAgain()))) throw new OperateException("初始密码长度或确认密码不正确");
    }

    private void grantPermission(long userId, String displayName, Integer shopId) {
        List<Map<String, Object>> grants = jdbcTemplate.queryForList("SELECT merchant_id AS merchantId FROM hou_mall_merchant_account WHERE user_id = ? LIMIT 1", userId);
        if (!grants.isEmpty() && ((Number) grants.get(0).get("merchantId")).longValue() != shopId.longValue()) throw new OperateException("该账号已绑定其他商家，不能重复授权");
        Long adminId = jdbcTemplate.query("SELECT id FROM hou_merchant_mall_shop_admin WHERE delete_time = 0 AND shop_id = ? AND user_id = ? LIMIT 1",
                ps -> { ps.setInt(1, shopId); ps.setLong(2, userId); }, rs -> rs.next() ? rs.getLong("id") : null);
        if (adminId == null) {
            long now = System.currentTimeMillis() / 1000;
            jdbcTemplate.update("INSERT INTO hou_merchant_mall_shop_admin (create_time, update_time, delete_time, shop_id, super, name, face, status, login_all, user_id, login_last_time, login_last_ip) VALUES (?, ?, 0, ?, 1, ?, '/api/static/backend_avatar.png', 1, 0, ?, 0, '')", now, now, shopId, displayName, userId);
            adminId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        }
        if (grants.isEmpty()) jdbcTemplate.update("INSERT INTO hou_mall_merchant_account (merchant_id, user_id, admin_id) VALUES (?, ?, ?)", shopId, userId, adminId);
        jdbcTemplate.update("UPDATE hou_user_info SET is_business = 1, shop_id = ? WHERE user_id = ?", shopId, userId);
    }

    private Map<String, Object> findActiveAccount(long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT user_id AS userId, user_name AS userName, user_mobile AS userMobile, user_nickname AS userNickname, password FROM hou_user_info WHERE user_id = ? AND COALESCE(is_delete, 0) = 0 AND user_state IN (1, 2) AND password IS NOT NULL AND password <> '' LIMIT 1", userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private MallShopProvisionListedVo toListedVo(MallShop shop) {
        MallShopProvisionListedVo vo = new MallShopProvisionListedVo();
        vo.setId(shop.getId()); vo.setName(shop.getName()); vo.setLogo(shop.getLogo()); vo.setType(shop.getType());
        vo.setTypeText(Integer.valueOf(0).equals(shop.getType()) ? "官方自营" : "商家入驻"); vo.setShopStatus(shop.getShopStatus()); vo.setBusinessStatus(shop.getBusinessStatus());
        vo.setContact(shop.getContact()); vo.setMobile(firstMobile(shop.getMobile())); vo.setAddressInfo(shop.getAddressInfo());
        vo.setLat(shop.getLat()); vo.setLng(shop.getLng());
        MallShopAdmin admin = adminMapper.selectOne(new QueryWrapper<MallShopAdmin>().select("id", "user_id", "status").eq("shop_id", shop.getId()).eq("delete_time", 0).eq("super", 1).orderByAsc("id").last("limit 1"));
        if (admin == null) vo.setAccount(Map.of("loginAccount", "-", "status", 0));
        else {
            Map<String, Object> user = jdbcTemplate.queryForList("SELECT user_id AS userId, user_mobile AS userMobile, user_name AS userName FROM hou_user_info WHERE user_id = ? LIMIT 1", admin.getUserId()).stream().findFirst().orElse(Collections.emptyMap());
            vo.setAccount(Map.of("id", admin.getId(), "userId", admin.getUserId(), "loginAccount", preferredLoginAccount(user), "status", admin.getStatus()));
        }
        MallShopCategory category = shop.getShopCategoryId() == null ? null : categoryMapper.selectById(shop.getShopCategoryId());
        vo.setShopCategory(category == null ? Map.of("name", "-") : Map.of("id", category.getId(), "name", category.getName()));
        return vo;
    }

    private String preferredLoginAccount(Map<String, Object> account) { String mobile = text(account.get("userMobile")); return hasText(mobile) ? mobile : text(account.get("userName")); }
    private String firstText(Object... values) { for (Object value : values) if (hasText(value)) return text(value); return ""; }
    private String firstMobile(String json) { if (!hasText(json)) return ""; try { JSONArray values = JSONArray.parseArray(json); return values.isEmpty() ? "" : values.getString(0); } catch (Exception ignored) { return json; } }
    private boolean hasText(Object value) { return StringUtils.hasText(text(value)); }
    private String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private String requiredText(Object value, String message) { String result = text(value); if (!hasText(result)) throw new OperateException(message); return result; }
    private long requiredLong(Object value, String message) { try { long result = Long.parseLong(text(value)); if (result > 0) return result; } catch (Exception ignored) { } throw new OperateException(message); }
    private int positiveInt(Object value, int fallback) { try { int result = Integer.parseInt(text(value)); return result > 0 ? result : fallback; } catch (Exception ignored) { return fallback; } }
    private Integer nullableInt(Object value) { try { return hasText(value) ? Integer.valueOf(text(value)) : null; } catch (Exception ignored) { return null; } }
    private record AccountGrant(String userId, String loginAccount) {}
}
