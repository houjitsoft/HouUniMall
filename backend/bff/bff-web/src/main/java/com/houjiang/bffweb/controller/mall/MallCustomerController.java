package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城会员管理Controller
/**
 * 通过MallServiceFeign调用mall服务客户管理接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/customer")
@RequiredArgsConstructor
public class MallCustomerController {

    private final MallServiceFeign mallServiceFeign;

    /**
     * 获取客户列表
/**
     *
     * @param params 查询参数：page, size, name, phone, groupId, status
     * @return ResponseResult 分页数据
     */
    @PostMapping("/list")
    public ResponseResult getCustomerList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getCustomerList(params);
    }

    /**
     * 获取客户详情
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 客户信息
     */
    @PostMapping("/detail")
    public ResponseResult getCustomerDetail(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getCustomerDetail(params);
    }

    /**
     * 保存或更新客户（带 id 为更新，不带为创建）
/**
     *
     * @param params 客户数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/save")
    public ResponseResult saveCustomer(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveCustomer(params);
    }

    /**
     * 删除客户
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteCustomer(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteCustomer(params);
    }

    /**
     * 获取客户分组列表
/**
     *
     * @return ResponseResult 分组列表
     */
    @PostMapping("/group/list")
    public ResponseResult getGroupList() {
        return mallServiceFeign.getCustomerGroupList();
    }

    /**
     * 保存客户分组
/**
     *
     * @param params 分组数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/group/save")
    public ResponseResult saveGroup(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveCustomerGroup(params);
    }

    /**
     * 删除客户分组
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/group/delete")
    public ResponseResult deleteGroup(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteCustomerGroup(params);
    }

    /**
     * 获取客户标签列表
/**
     *
     * @return ResponseResult 标签列表
     */
    @PostMapping("/tag/list")
    public ResponseResult getTagList() {
        return mallServiceFeign.getCustomerTagList();
    }

    /**
     * 保存客户标签
/**
     *
     * @param params 标签数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/tag/save")
    public ResponseResult saveTag(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveCustomerTag(params);
    }

    /**
     * 删除客户标签
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/tag/delete")
    public ResponseResult deleteTag(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteCustomerTag(params);
    }

    /**
     * 获取客户跟进记录
/**
     *
     * @param params 查询参数：customerId, page, size
     * @return ResponseResult 分页数据
     */
    @PostMapping("/follow/list")
    public ResponseResult getFollowList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getCustomerFollowList(params);
    }

    /**
     * 添加跟进记录
/**
     *
     * @param params 跟进记录数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/follow/add")
    public ResponseResult addFollow(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.addCustomerFollow(params);
    }

    /**
     * 删除客户跟进记录
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/follow/delete")
    public ResponseResult deleteFollow(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteCustomerFollow(params);
    }

    /**
     * 客户统计
/**
     *
     * @param params 查询参数
     * @return ResponseResult 统计数据
     */
    @PostMapping("/statistics")
    public ResponseResult getStatistics(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getCustomerStatistics(params);
    }
}
