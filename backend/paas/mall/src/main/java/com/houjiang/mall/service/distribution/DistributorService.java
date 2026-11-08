package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.Distributor;
import com.houjiang.mall.entity.distribution.DistributorLevel;
import com.houjiang.mall.mapper.distribution.DistributorMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 分销员服务类
 * 
 * @author Amos
 */
@Service
public class DistributorService extends ServiceImpl<DistributorMapper, Distributor> {

    @Resource
    private DistributorLevelService distributorLevelService;

    /**
     * 分页查询分销员列表
     *
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<Distributor> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        String keyword = (String) params.get("keyword");
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, Distributor::getShopId, shopId);
        wrapper.eq(status != null, Distributor::getStatus, status);
        wrapper.and(StringUtils.hasText(keyword), w -> 
            w.like(Distributor::getRealName, keyword)
             .or().like(Distributor::getPhone, keyword)
             .or().like(Distributor::getDistributorNo, keyword)
        );
        wrapper.orderByDesc(Distributor::getCreateTime);

        IPage<Distributor> result = this.page(new Page<>(page, size), wrapper);
        fillParentNo(result.getRecords());
        return result;
    }

    /**
     * 给列表补「上级邀请码」——后台表格要显示、编辑弹窗要回填，
     * 但实体只有 parentId，这里按需批量查一次，避免前端拿 ID 猜。
     */
    private void fillParentNo(java.util.List<Distributor> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        java.util.Set<Long> parentIds = new java.util.HashSet<>();
        for (Distributor item : records) {
            if (item.getParentId() != null && item.getParentId() > 0) {
                parentIds.add(item.getParentId());
            }
        }
        if (parentIds.isEmpty()) {
            return;
        }
        java.util.Map<Long, String> noMap = new java.util.HashMap<>();
        for (Distributor parent : this.listByIds(parentIds)) {
            noMap.put(parent.getId(), parent.getDistributorNo());
        }
        for (Distributor item : records) {
            if (item.getParentId() != null) {
                item.setParentNo(noMap.get(item.getParentId()));
            }
        }
    }

    /**
     * 申请成为分销员
     *
     * @param distributor 分销员信息
     * @return 是否成功
     */
    public boolean apply(Distributor distributor) {
        distributor.setDistributorNo(generateDistributorNo());
        distributor.setTotalCommission(BigDecimal.ZERO);
        distributor.setWithdrawnCommission(BigDecimal.ZERO);
        distributor.setPendingCommission(BigDecimal.ZERO);
        distributor.setAvailableCommission(BigDecimal.ZERO);
        distributor.setOrderCount(0);
        distributor.setSubCount(0);
        // 待审核
        distributor.setStatus(0);
        distributor.setCreateTime(LocalDateTime.now());
        distributor.setUpdateTime(LocalDateTime.now());
        return this.save(distributor);
    }

    /**
     * 审核分销员
     *
     * @param id 分销员ID
     * @param status 状态 1-通过 2-拒绝
     * @param remark 审核备注
     * @return 是否成功
     */
    public boolean audit(Long id, Integer status, String remark) {
        Distributor distributor = this.getById(id);
        if (distributor == null) {
            return false;
        }
        distributor.setStatus(status);
        distributor.setAuditRemark(remark);
        distributor.setAuditTime(LocalDateTime.now());
        distributor.setUpdateTime(LocalDateTime.now());
        return this.updateById(distributor);
    }

    /**
     * 更新分销员状态
     *
     * @param id 分销员ID
     * @param status 状态
     * @return 是否成功
     */
    public boolean updateStatus(Long id, Integer status) {
        Distributor distributor = new Distributor();
        distributor.setId(id);
        distributor.setStatus(status);
        distributor.setUpdateTime(LocalDateTime.now());
        return this.updateById(distributor);
    }

    /**
     * 生成分销员编号
     */
    private String generateDistributorNo() {
        return "D" + System.currentTimeMillis();
    }

    /**
     * 后台手工创建分销员。
     *
     * <p>与 C 端 {@link #apply} 的区别：不走「待审核」，可直接落为正常状态；可指定
     * 邀请码（分销员编号）、上级、等级；用于线下签约、地推批量导入等场景。
     *
     * @param params userId（必填）/ realName / phone / distributorNo / levelId / parentNo / parentId / status / shopId
     * @return 新建的分销员
     * @throws IllegalArgumentException 参数不合法（重复绑定、上级不存在等）
     */
    @Transactional(rollbackFor = Exception.class)
    public Distributor createByAdmin(Map<String, Object> params) {
        Long userId = parseLong(params.get("userId"));
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 一个用户只能有一个分销员身份
        LambdaQueryWrapper<Distributor> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Distributor::getUserId, userId);
        existWrapper.eq(Distributor::getDeleted, 0);
        existWrapper.last("limit 1");
        Distributor exist = this.getOne(existWrapper);
        if (exist != null) {
            throw new IllegalArgumentException("该用户已是分销员（" + exist.getDistributorNo() + "）");
        }

        Long shopId = parseLong(params.get("shopId"));
        Distributor distributor = new Distributor();
        distributor.setUserId(userId);
        distributor.setShopId(shopId == null ? 0L : shopId);
        distributor.setRealName(blankToEmpty(str(params.get("realName"))));
        distributor.setPhone(blankToEmpty(str(params.get("phone"))));
        // 邀请码：允许自定义（便于线下印制），未填则自动生成；必须唯一
        String no = str(params.get("distributorNo"));
        distributor.setDistributorNo(no == null || no.isBlank() ? generateDistributorNo() : no.trim());
        assertDistributorNoUnique(distributor.getDistributorNo(), null);
        // 等级：未指定则挂默认等级，避免佣金比例解析不到
        Long levelId = parseLong(params.get("levelId"));
        distributor.setLevelId(levelId != null ? levelId : findDefaultLevelId(distributor.getShopId()));
        // 上级：优先 parentId，其次 parentNo（后台表单一般填编号）
        Distributor parent = resolveParent(parseLong(params.get("parentId")), str(params.get("parentNo")));
        if (parent != null) {
            distributor.setParentId(parent.getId());
        }
        // 后台建号默认直接可用，除非显式传了状态
        Long status = parseLong(params.get("status"));
        distributor.setStatus(status == null ? 1 : status.intValue());
        distributor.setTotalCommission(BigDecimal.ZERO);
        distributor.setWithdrawnCommission(BigDecimal.ZERO);
        distributor.setPendingCommission(BigDecimal.ZERO);
        distributor.setAvailableCommission(BigDecimal.ZERO);
        distributor.setOrderCount(0);
        distributor.setSubCount(0);
        distributor.setAuditTime(LocalDateTime.now());
        distributor.setCreateTime(LocalDateTime.now());
        distributor.setUpdateTime(LocalDateTime.now());
        this.save(distributor);

        if (parent != null && distributor.getStatus() != null && distributor.getStatus() != 2) {
            incrSubCount(parent.getId(), 1);
        }
        return distributor;
    }

    /**
     * 后台编辑分销员：可改姓名 / 手机号 / 邀请码 / 等级 / 上级 / 状态。
     *
     * <p>上级变更时同步维护新旧上级的「下级数」；金额与订单计数不允许从这里改。
     *
     * @param params id（必填）+ 可改字段
     * @return 更新后的分销员
     * @throws IllegalArgumentException 参数不合法
     */
    @Transactional(rollbackFor = Exception.class)
    public Distributor updateByAdmin(Map<String, Object> params) {
        Long id = parseLong(params.get("id"));
        if (id == null) {
            throw new IllegalArgumentException("分销员ID不能为空");
        }
        Distributor current = this.getById(id);
        if (current == null) {
            throw new IllegalArgumentException("分销员不存在");
        }
        Long oldParentId = current.getParentId();
        Integer oldStatus = current.getStatus();

        if (params.get("realName") != null) {
            current.setRealName(blankToEmpty(str(params.get("realName"))));
        }
        if (params.get("phone") != null) {
            current.setPhone(blankToEmpty(str(params.get("phone"))));
        }
        if (params.get("distributorNo") != null) {
            String no = str(params.get("distributorNo"));
            if (no == null || no.isBlank()) {
                throw new IllegalArgumentException("邀请码不能为空");
            }
            assertDistributorNoUnique(no.trim(), id);
            current.setDistributorNo(no.trim());
        }
        if (params.get("levelId") != null) {
            current.setLevelId(parseLong(params.get("levelId")));
        }
        if (params.get("status") != null) {
            Long status = parseLong(params.get("status"));
            current.setStatus(status == null ? oldStatus : status.intValue());
        }
        // 上级支持清空：传 parentId=0 或 parentNo 空串
        boolean parentChanged = params.containsKey("parentId") || params.containsKey("parentNo");
        boolean clearParent = false;
        Distributor newParent = null;
        if (parentChanged) {
            Long parentId = parseLong(params.get("parentId"));
            String parentNo = str(params.get("parentNo"));
            boolean clear = (parentId != null && parentId == 0L) || (parentId == null && (parentNo == null || parentNo.isBlank()));
            if (!clear) {
                newParent = resolveParent(parentId, parentNo);
                if (newParent == null) {
                    throw new IllegalArgumentException("上级分销员不存在或未启用");
                }
                if (newParent.getId().equals(id)) {
                    throw new IllegalArgumentException("上级不能是自己");
                }
                current.setParentId(newParent.getId());
            } else {
                current.setParentId(null);
                clearParent = true;
            }
        }
        current.setUpdateTime(LocalDateTime.now());
        this.updateById(current);
        // MyBatis-Plus 的 updateById 默认忽略 null 字段，解除上级必须显式 set null，
        // 否则库里 parent_id 仍指向旧上级，而下面的计数已经减了——两边不一致
        if (clearParent) {
            this.lambdaUpdate()
                    .eq(Distributor::getId, id)
                    .set(Distributor::getParentId, null)
                    .update();
        }

        // 维护「下级数」：只在涉及上级或状态变化时调整
        if (parentChanged) {
            if (oldParentId != null && !oldParentId.equals(current.getParentId())) {
                incrSubCount(oldParentId, -1);
            }
            if (newParent != null && !newParent.getId().equals(oldParentId)) {
                incrSubCount(newParent.getId(), 1);
            }
        }
        if (oldStatus != null && current.getStatus() != null && !oldStatus.equals(current.getStatus())) {
            // 0 → 1 视为新通过审核，给上级补计数
            Long pid = current.getParentId();
            if (pid != null) {
                if (oldStatus == 0 && current.getStatus() == 1) {
                    incrSubCount(pid, 1);
                } else if (oldStatus == 1 && current.getStatus() == 2) {
                    incrSubCount(pid, -1);
                }
            }
        }
        return current;
    }

    /** 校验邀请码（分销员编号）唯一，excludeId 为编辑时的自身 ID */
    private void assertDistributorNoUnique(String distributorNo, Long excludeId) {
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Distributor::getDistributorNo, distributorNo);
        wrapper.eq(Distributor::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(Distributor::getId, excludeId);
        }
        wrapper.last("limit 1");
        if (this.getOne(wrapper) != null) {
            throw new IllegalArgumentException("邀请码已被占用：" + distributorNo);
        }
    }

    /** 解析上级：按 ID 或编号，且必须是正常状态的分销员 */
    private Distributor resolveParent(Long parentId, String parentNo) {
        LambdaQueryWrapper<Distributor> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null && parentId > 0) {
            wrapper.eq(Distributor::getId, parentId);
        } else if (parentNo != null && !parentNo.isBlank()) {
            wrapper.eq(Distributor::getDistributorNo, parentNo.trim());
        } else {
            return null;
        }
        wrapper.eq(Distributor::getStatus, 1);
        wrapper.eq(Distributor::getDeleted, 0);
        wrapper.last("limit 1");
        return this.getOne(wrapper);
    }

    /** 平台默认等级，没配就返回 null（不阻断建号） */
    private Long findDefaultLevelId(Long shopId) {
        LambdaQueryWrapper<DistributorLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributorLevel::getShopId, shopId == null ? 0L : shopId);
        wrapper.eq(DistributorLevel::getDeleted, 0);
        wrapper.orderByDesc(DistributorLevel::getIsDefault);
        wrapper.orderByAsc(DistributorLevel::getSort);
        wrapper.last("limit 1");
        DistributorLevel level = distributorLevelService.getOne(wrapper);
        if (level == null && shopId != null && shopId != 0L) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DistributorLevel::getShopId, 0L);
            wrapper.eq(DistributorLevel::getDeleted, 0);
            wrapper.orderByDesc(DistributorLevel::getIsDefault);
            wrapper.last("limit 1");
            level = distributorLevelService.getOne(wrapper);
        }
        return level == null ? null : level.getId();
    }

    /** 上下级计数增减，最小 0 */
    private void incrSubCount(Long distributorId, int delta) {
        if (distributorId == null || delta == 0) {
            return;
        }
        this.lambdaUpdate()
                .eq(Distributor::getId, distributorId)
                .setSql(delta > 0
                        ? "sub_count = sub_count + " + delta
                        : "sub_count = GREATEST(sub_count - " + (-delta) + ", 0)")
                .update();
    }

    private static Long parseLong(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
