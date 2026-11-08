package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.mch.MerchantAccountUpdateRequest;
import com.houjiang.payment.dto.mch.MerchantDetailVO;
import com.houjiang.payment.dto.mch.MerchantFeeConfigRequest;
import com.houjiang.payment.dto.mch.MerchantListRequest;
import com.houjiang.payment.dto.mch.MerchantProductOpenRequest;
import com.houjiang.payment.dto.mch.MerchantRegRequest;
import com.houjiang.payment.dto.mch.MerchantStatusChangeRequest;
import com.houjiang.payment.dto.mch.MerchantUpdateRequest;
import com.houjiang.payment.entity.PayMerchantEntity;
import com.houjiang.payment.entity.PayMerchantFeeEntity;
import com.houjiang.payment.entity.PayMerchantProductEntity;

import java.util.List;
import java.util.Map;

/**
 * 平台商户管理服务接口
 * W1 任务：商户进件 / 列表 / 详情 / 更新 / 状态变更 / 费率配置 / 结算账户 / 产品开通
 * @author Amos
 */
public interface MerchantService {

    /**
     * 商户进件
     *
     * @param request 进件参数
     * @param operatorId 操作人（审核人、创建人）
     * @return 生成的商户主数据
     */
    PayMerchantEntity register(MerchantRegRequest request, String operatorId);

    /**
     * 商户列表（分页）
     */
    IPage<PayMerchantEntity> list(MerchantListRequest request);

    /**
     * 商户详情（聚合产品+费率）
     */
    MerchantDetailVO detail(String mchNo);

    /**
     * 商户信息更新（仅非核心字段）
     */
    boolean update(MerchantUpdateRequest request, String operatorId);

    /**
     * 商户状态变更（审核通过/驳回/冻结/解冻/注销）
     */
    boolean changeStatus(MerchantStatusChangeRequest request, String operatorId);

    /**
     * 商户费率配置（新增或更新）
     */
    PayMerchantFeeEntity configFee(MerchantFeeConfigRequest request, String operatorId);

    /**
     * 商户结算账户更新
     */
    boolean updateSettleAccount(MerchantAccountUpdateRequest request, String operatorId);

    /**
     * 商户产品开通/停用
     */
    PayMerchantProductEntity openProduct(MerchantProductOpenRequest request, String operatorId);

    // ============================ W4 自研补全 3 接口 ============================

    /**
     * 个人商户进件（自然人/个体工商户专用）。
     * 与 register() 区分：personType=PERSON/INDIVIDUAL，资质审核流程更简化。
     * TODO: 真实实现需对接实名核验/银行卡四要素/活体检测。
     *
     * @param req 入参 mchName/personIdNo/personName/contactPhone/bankCardNo 等
     * @param operatorId 操作人
     * @return 商户实体
     */
    PayMerchantEntity regPerson(Map<String, Object> req, String operatorId);

    /**
     * 资质补件（追加/更新 hou_pay_merchant_qual_cert）。
     *
     * @param req 入参 mchNo/certType/certNo/certUrl/expireDate
     * @param operatorId 操作人
     * @return 是否成功
     */
    Boolean qualSupplement(Map<String, Object> req, String operatorId);

    /**
     * 子商户/子账户列表（按主商户号查子账户配置）。
     *
     * @param req 入参 mchNo（主商户号）
     * @return 子账户列表
     */
    List<Map<String, Object>> subAccountList(Map<String, Object> req);
}
