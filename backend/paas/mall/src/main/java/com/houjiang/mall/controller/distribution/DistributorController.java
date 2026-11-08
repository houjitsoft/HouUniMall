package com.houjiang.mall.controller.distribution;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.Distributor;
import com.houjiang.mall.service.distribution.DistributorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分销员控制器
 * 提供分销员的列表查询、详情、申请、审核、状态更新等功能
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/distribution/distributor")
@RequiredArgsConstructor
public class DistributorController {

    private final DistributorService distributorService;

    /**
     * 分页查询分销员列表
     *
     * @param params 查询参数（page, size, shopId, keyword, status）
     * @return 分页结果
     */
    @PostMapping("/list")
    public ResponseResult<IPage<Distributor>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(distributorService.pageList(params));
    }

    /**
     * 获取分销员详情
     *
     * @param params 参数（包含 id）
     * @return 分销员详情
     */
    @PostMapping("/detail")
    public ResponseResult<Distributor> detail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributorService.getById(id));
    }

    /**
     * 申请成为分销员
     *
     * @param distributor 分销员申请信息
     * @return 是否成功
     */
    @PostMapping("/apply")
    public ResponseResult<Boolean> apply(@RequestBody Distributor distributor) {
        return ResponseResult.success(distributorService.apply(distributor));
    }

    /**
     * 审核分销员
     *
     * @param params 参数（包含 id, status, remark）
     * @return 是否成功
     */
    @PostMapping("/audit")
    public ResponseResult<Boolean> audit(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        String remark = (String) params.getOrDefault("remark", "");
        return ResponseResult.success(distributorService.audit(id, status, remark));
    }

    /**
     * 更新分销员状态
     *
     * @param params 参数（包含 id, status）
     * @return 是否成功
     */
    @PostMapping("/status")
    public ResponseResult<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(distributorService.updateStatus(id, status));
    }

    /**
     * 后台手工创建分销员（不走待审核，可直接指定邀请码 / 上级 / 等级）
     *
     * @param params userId（必填）/ realName / phone / distributorNo / levelId / parentNo / parentId / status / shopId
     * @return 新建的分销员；参数不合法时返回 fail 与原因
     */
    @PostMapping("/create")
    public ResponseResult<Distributor> create(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(distributorService.createByAdmin(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 后台编辑分销员（可改姓名 / 手机号 / 邀请码 / 等级 / 上级 / 状态）
     *
     * @param params id（必填）+ 可改字段
     * @return 更新后的分销员；参数不合法时返回 fail 与原因
     */
    @PostMapping("/edit")
    public ResponseResult<Distributor> edit(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(distributorService.updateByAdmin(params));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), e.getMessage());
        }
    }

    /**
     * 更新分销员信息
     *
     * @param distributor 分销员信息
     * @return 是否成功
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody Distributor distributor) {
        return ResponseResult.success(distributorService.updateById(distributor));
    }

    /**
     * 删除分销员
     *
     * @param params 参数（包含 id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributorService.removeById(id));
    }
}
