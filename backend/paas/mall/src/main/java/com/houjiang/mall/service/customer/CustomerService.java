package com.houjiang.mall.service.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.customer.Customer;
import com.houjiang.mall.entity.customer.CustomerFollow;
import com.houjiang.mall.entity.customer.CustomerGroup;
import com.houjiang.mall.entity.customer.CustomerTag;
import com.houjiang.mall.mapper.customer.CustomerFollowMapper;
import com.houjiang.mall.mapper.customer.CustomerGroupMapper;
import com.houjiang.mall.mapper.customer.CustomerMapper;
import com.houjiang.mall.mapper.customer.CustomerTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户管理服务
 * 聚合客户信息、分组、标签、跟进记录等CRUD操作
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {

    private final CustomerGroupMapper customerGroupMapper;
    private final CustomerTagMapper customerTagMapper;
    private final CustomerFollowMapper customerFollowMapper;

    // ================= 客户信息 =================

    /**
     * 分页查询客户列表
     * 支持按名称、手机号、分组、状态筛选
     *
     * @param params 查询参数：page, size, name, phone, groupId, status
     * @return 分页结果，包含list和total
     */
    public Map<String, Object> pageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (params.get("name") != null && StringUtils.hasText(params.get("name").toString())) {
            wrapper.like(Customer::getName, params.get("name").toString());
        }
        if (params.get("phone") != null && StringUtils.hasText(params.get("phone").toString())) {
            wrapper.like(Customer::getPhone, params.get("phone").toString());
        }
        if (params.get("groupId") != null) {
            wrapper.eq(Customer::getGroupId, Long.parseLong(params.get("groupId").toString()));
        }
        if (params.get("status") != null) {
            wrapper.eq(Customer::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        wrapper.orderByDesc(Customer::getCreateTime);

        IPage<Customer> pageResult = this.page(new Page<>(page, size), wrapper);
        Map<String, Object> result = new HashMap<>(4);
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return result;
    }

    /**
     * 获取客户详情
     *
     * @param id 客户ID
     * @return 客户信息
     */
    public Customer getDetail(Long id) {
        return this.getById(id);
    }

    /**
     * 保存或更新客户信息
     *
     * @param customer 客户实体
     * @return 是否成功
     */
    public boolean saveOrUpdateCustomer(Customer customer) {
        return this.saveOrUpdate(customer);
    }

    /**
     * 删除客户（逻辑删除，由 @TableLogic 控制）
     *
     * @param id 客户ID
     * @return 是否成功
     */
    public boolean removeCustomer(Long id) {
        return this.removeById(id);
    }

    // ================= 客户分组 =================

    /**
     * 获取全部客户分组列表
     *
     * @return 分组列表
     */
    public List<CustomerGroup> getGroupList() {
        LambdaQueryWrapper<CustomerGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CustomerGroup::getSort);
        return customerGroupMapper.selectList(wrapper);
    }

    /**
     * 保存或更新客户分组
     *
     * @param group 分组实体
     * @return 是否成功
     */
    public boolean saveOrUpdateGroup(CustomerGroup group) {
        if (group.getId() != null) {
            return customerGroupMapper.updateById(group) > 0;
        } else {
            return customerGroupMapper.insert(group) > 0;
        }
    }

    /**
     * 删除客户分组
     *
     * @param id 分组ID
     * @return 是否成功
     */
    public boolean removeGroup(Long id) {
        return customerGroupMapper.deleteById(id) > 0;
    }

    // ================= 客户标签 =================

    /**
     * 获取全部客户标签列表
     *
     * @return 标签列表
     */
    public List<CustomerTag> getTagList() {
        LambdaQueryWrapper<CustomerTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CustomerTag::getSort);
        return customerTagMapper.selectList(wrapper);
    }

    /**
     * 保存或更新客户标签
     *
     * @param tag 标签实体
     * @return 是否成功
     */
    public boolean saveOrUpdateTag(CustomerTag tag) {
        if (tag.getId() != null) {
            return customerTagMapper.updateById(tag) > 0;
        } else {
            return customerTagMapper.insert(tag) > 0;
        }
    }

    /**
     * 删除客户标签
     *
     * @param id 标签ID
     * @return 是否成功
     */
    public boolean removeTag(Long id) {
        return customerTagMapper.deleteById(id) > 0;
    }

    // ================= 跟进记录 =================

    /**
     * 分页查询客户跟进记录
     *
     * @param params 查询参数：customerId, page, size
     * @return 分页结果，包含list和total
     */
    public Map<String, Object> getFollowList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;

        LambdaQueryWrapper<CustomerFollow> wrapper = new LambdaQueryWrapper<>();
        if (params.get("customerId") != null) {
            wrapper.eq(CustomerFollow::getCustomerId, Long.parseLong(params.get("customerId").toString()));
        }
        wrapper.orderByDesc(CustomerFollow::getCreateTime);

        IPage<CustomerFollow> pageResult = customerFollowMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, Object> result = new HashMap<>(4);
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return result;
    }

    /**
     * 添加客户跟进记录
     *
     * @param follow 跟进记录实体
     * @return 是否成功
     */
    public boolean addFollow(CustomerFollow follow) {
        return customerFollowMapper.insert(follow) > 0;
    }

    /**
     * 删除客户跟进记录
     *
     * @param id 跟进记录ID
     * @return 是否成功
     */
    public boolean removeFollow(Long id) {
        return customerFollowMapper.deleteById(id) > 0;
    }

    // ================= 统计 =================

    /**
     * 获取客户统计数据
     * 包含：客户总数、活跃客户数、新增客户数等
     *
     * @param params 查询参数（预留扩展）
     * @return 统计数据Map
     */
    public Map<String, Object> getStatistics(Map<String, Object> params) {
        Map<String, Object> stats = new HashMap<>(8);
        // 客户总数
        stats.put("totalCustomers", this.count(new LambdaQueryWrapper<Customer>()));
        // 正常状态客户
        stats.put("activeCustomers", this.count(new LambdaQueryWrapper<Customer>().eq(Customer::getStatus, 1)));
        // 今日新增
        stats.put("newCustomers", this.count(new LambdaQueryWrapper<Customer>()
                .ge(Customer::getCreateTime, LocalDate.now().atStartOfDay())));
        // 本月新增
        stats.put("monthNewCustomers", this.count(new LambdaQueryWrapper<Customer>()
                .ge(Customer::getCreateTime, LocalDate.now().withDayOfMonth(1).atStartOfDay())));
        return stats;
    }
}
