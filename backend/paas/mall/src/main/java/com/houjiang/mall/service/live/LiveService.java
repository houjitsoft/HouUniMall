package com.houjiang.mall.service.live;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.live.LiveAccount;
import com.houjiang.mall.entity.live.LiveActivity;
import com.houjiang.mall.entity.live.LiveProductAudit;
import com.houjiang.mall.mapper.live.LiveAccountMapper;
import com.houjiang.mall.mapper.live.LiveActivityMapper;
import com.houjiang.mall.mapper.live.LiveProductAuditMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 直播互动服务
 * 聚合直播活动、商品审核、账号管理功能
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveService extends ServiceImpl<LiveActivityMapper, LiveActivity> {

    private final LiveProductAuditMapper productAuditMapper;
    private final LiveAccountMapper accountMapper;

    // ========== 直播活动 ==========

    /**
     * 分页查询直播活动列表
     * @param params page, size, title, status
     * @return 分页结果
     */
    public IPage<LiveActivity> pageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<LiveActivity> w = new LambdaQueryWrapper<>();
        if (params.get("title") != null && StringUtils.hasText(params.get("title").toString())) {
            w.like(LiveActivity::getTitle, params.get("title").toString());
        }
        if (params.get("status") != null) {
            w.eq(LiveActivity::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByDesc(LiveActivity::getCreateTime);
        return this.page(new Page<>(page, size), w);
    }

    /**
     * 开始直播（状态改为1）
     * @param id 直播ID
     * @return 是否成功
     */
    public boolean startLive(Long id) {
        LiveActivity a = this.getById(id);
        if (a == null) return false;
        a.setStatus(1);
        return this.updateById(a);
    }

    /**
     * 结束直播（状态改为2）
     * @param id 直播ID
     * @return 是否成功
     */
    public boolean endLive(Long id) {
        LiveActivity a = this.getById(id);
        if (a == null) return false;
        a.setStatus(2);
        return this.updateById(a);
    }

    // ========== 直播商品审核 ==========

    /**
     * 分页查询直播商品审核列表
     * @param params liveId, auditStatus, page, size
     * @return 分页结果
     */
    public IPage<LiveProductAudit> productAuditPageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<LiveProductAudit> w = new LambdaQueryWrapper<>();
        if (params.get("liveId") != null) {
            w.eq(LiveProductAudit::getLiveId, Long.parseLong(params.get("liveId").toString()));
        }
        if (params.get("auditStatus") != null) {
            w.eq(LiveProductAudit::getAuditStatus, Integer.parseInt(params.get("auditStatus").toString()));
        }
        w.orderByDesc(LiveProductAudit::getCreateTime);
        return productAuditMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取直播商品审核详情
     * @param id 审核ID
     * @return 审核记录
     */
    public LiveProductAudit getProductAuditDetail(Long id) {
        return productAuditMapper.selectById(id);
    }

    /**
     * 提交直播商品审核
     * @param audit 审核实体
     * @return 是否成功
     */
    public boolean submitProductAudit(LiveProductAudit audit) {
        return productAuditMapper.insert(audit) > 0;
    }

    // ========== 直播账号 ==========

    /**
     * 分页查询直播账号列表
     * @param params page, size, accountName, status
     * @return 分页结果
     */
    public IPage<LiveAccount> accountPageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<LiveAccount> w = new LambdaQueryWrapper<>();
        if (params.get("accountName") != null && StringUtils.hasText(params.get("accountName").toString())) {
            w.like(LiveAccount::getAccountName, params.get("accountName").toString());
        }
        if (params.get("status") != null) {
            w.eq(LiveAccount::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByDesc(LiveAccount::getCreateTime);
        return accountMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取直播账号详情
     * @param id 账号ID
     * @return 账号信息
     */
    public LiveAccount getAccountDetail(Long id) {
        return accountMapper.selectById(id);
    }

    /**
     * 创建直播账号
     * @param account 账号实体
     * @return 是否成功
     */
    public boolean createAccount(LiveAccount account) {
        return accountMapper.insert(account) > 0;
    }

    /**
     * 更新直播账号
     * @param account 账号实体
     * @return 是否成功
     */
    public boolean updateAccount(LiveAccount account) {
        return accountMapper.updateById(account) > 0;
    }

    /**
     * 删除直播账号
     * @param id 账号ID
     * @return 是否成功
     */
    public boolean deleteAccount(Long id) {
        return accountMapper.deleteById(id) > 0;
    }

    /**
     * 切换直播账号状态
     * @param id 账号ID
     * @return 是否成功
     */
    public boolean toggleAccountStatus(Long id) {
        LiveAccount a = accountMapper.selectById(id);
        if (a == null) return false;
        a.setStatus(a.getStatus() == 1 ? 0 : 1);
        return accountMapper.updateById(a) > 0;
    }
}
