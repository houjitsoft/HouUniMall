package com.houjiang.mall.controller.customer;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.customer.Customer;
import com.houjiang.mall.entity.customer.CustomerFollow;
import com.houjiang.mall.entity.customer.CustomerGroup;
import com.houjiang.mall.entity.customer.CustomerTag;
import com.houjiang.mall.service.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 客户管理Controller
 * 提供客户信息CRUD、分组、标签、跟进记录、统计等接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 分页查询客户列表
     *
     * @param params 查询参数：page, size, name, phone, groupId, status
     * @return ResponseResult 包含分页数据
     */
    @PostMapping("/list")
    public ResponseResult getCustomerList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(customerService.pageList(params));
        } catch (Exception e) {
            log.error("查询客户列表失败", e);
            return ResponseResult.fail(500, "查询客户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户详情
     *
     * @param params 请求参数，包含id字段
     * @return ResponseResult 包含客户信息
     */
    @PostMapping("/detail")
    public ResponseResult getCustomerDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            Customer customer = customerService.getDetail(id);
            if (customer == null) {
                return ResponseResult.fail(404, "客户不存在");
            }
            return ResponseResult.success(customer);
        } catch (Exception e) {
            log.error("查询客户详情失败", e);
            return ResponseResult.fail(500, "查询客户详情失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新客户信息（带 id 为更新，不带为创建）
     *
     * @param customer 客户实体
     * @return ResponseResult 操作结果
     */
    @PostMapping("/save")
    public ResponseResult saveCustomer(@RequestBody Customer customer) {
        try {
            boolean result = customerService.saveOrUpdateCustomer(customer);
            return result ? ResponseResult.success("保存成功") : ResponseResult.fail(500, "保存失败");
        } catch (Exception e) {
            log.error("保存客户失败", e);
            return ResponseResult.fail(500, "保存客户失败: " + e.getMessage());
        }
    }

    /**
     * 删除客户（逻辑删除）
     *
     * @param params 请求参数，包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteCustomer(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            boolean result = customerService.removeCustomer(id);
            return result ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除客户失败", e);
            return ResponseResult.fail(500, "删除客户失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户分组列表
     *
     * @return ResponseResult 包含分组列表
     */
    @PostMapping("/group/list")
    public ResponseResult getGroupList() {
        try {
            return ResponseResult.success(customerService.getGroupList());
        } catch (Exception e) {
            log.error("查询客户分组列表失败", e);
            return ResponseResult.fail(500, "查询客户分组列表失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新客户分组
     *
     * @param group 分组实体
     * @return ResponseResult 操作结果
     */
    @PostMapping("/group/save")
    public ResponseResult saveGroup(@RequestBody CustomerGroup group) {
        try {
            boolean result = customerService.saveOrUpdateGroup(group);
            return result ? ResponseResult.success("保存成功") : ResponseResult.fail(500, "保存失败");
        } catch (Exception e) {
            log.error("保存客户分组失败", e);
            return ResponseResult.fail(500, "保存客户分组失败: " + e.getMessage());
        }
    }

    /**
     * 删除客户分组
     *
     * @param params 请求参数，包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/group/delete")
    public ResponseResult deleteGroup(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            boolean result = customerService.removeGroup(id);
            return result ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除客户分组失败", e);
            return ResponseResult.fail(500, "删除客户分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户标签列表
     *
     * @return ResponseResult 包含标签列表
     */
    @PostMapping("/tag/list")
    public ResponseResult getTagList() {
        try {
            return ResponseResult.success(customerService.getTagList());
        } catch (Exception e) {
            log.error("查询客户标签列表失败", e);
            return ResponseResult.fail(500, "查询客户标签列表失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新客户标签
     *
     * @param tag 标签实体
     * @return ResponseResult 操作结果
     */
    @PostMapping("/tag/save")
    public ResponseResult saveTag(@RequestBody CustomerTag tag) {
        try {
            boolean result = customerService.saveOrUpdateTag(tag);
            return result ? ResponseResult.success("保存成功") : ResponseResult.fail(500, "保存失败");
        } catch (Exception e) {
            log.error("保存客户标签失败", e);
            return ResponseResult.fail(500, "保存客户标签失败: " + e.getMessage());
        }
    }

    /**
     * 删除客户标签
     *
     * @param params 请求参数，包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/tag/delete")
    public ResponseResult deleteTag(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            boolean result = customerService.removeTag(id);
            return result ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除客户标签失败", e);
            return ResponseResult.fail(500, "删除客户标签失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询客户跟进记录
     *
     * @param params 查询参数：customerId, page, size
     * @return ResponseResult 包含分页数据
     */
    @PostMapping("/follow/list")
    public ResponseResult getFollowList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(customerService.getFollowList(params));
        } catch (Exception e) {
            log.error("查询客户跟进记录失败", e);
            return ResponseResult.fail(500, "查询客户跟进记录失败: " + e.getMessage());
        }
    }

    /**
     * 添加客户跟进记录
     *
     * @param follow 跟进记录实体
     * @return ResponseResult 操作结果
     */
    @PostMapping("/follow/add")
    public ResponseResult addFollow(@RequestBody CustomerFollow follow) {
        try {
            boolean result = customerService.addFollow(follow);
            return result ? ResponseResult.success("添加成功") : ResponseResult.fail(500, "添加失败");
        } catch (Exception e) {
            log.error("添加客户跟进记录失败", e);
            return ResponseResult.fail(500, "添加客户跟进记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除客户跟进记录
     *
     * @param params 请求参数，包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/follow/delete")
    public ResponseResult deleteFollow(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            boolean result = customerService.removeFollow(id);
            return result ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除客户跟进记录失败", e);
            return ResponseResult.fail(500, "删除客户跟进记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户统计数据
     *
     * @param params 查询参数（预留扩展）
     * @return ResponseResult 包含统计数据
     */
    @PostMapping("/statistics")
    public ResponseResult getStatistics(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(customerService.getStatistics(params));
        } catch (Exception e) {
            log.error("查询客户统计数据失败", e);
            return ResponseResult.fail(500, "查询客户统计数据失败: " + e.getMessage());
        }
    }
}
