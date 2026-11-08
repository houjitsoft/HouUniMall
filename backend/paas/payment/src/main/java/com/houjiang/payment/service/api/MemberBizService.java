package com.houjiang.payment.service.api;

import com.houjiang.payment.entity.PayMemberProductEntity;

import java.util.List;
import java.util.Map;

/**
 * 自研会员业务服务（W4 P0，对标通企付会员管理能力，但完全自研）。
 *
 * 命名空间约定：所有方法对应 Controller 路径 /api/payment/member/biz/**。
 *
 * @author Amos
 */
public interface MemberBizService {

    /**
     * 1. 会员信息聚合查询（基础信息 + 余额 + 已开通产品）。
     *
     * @param req 入参 memberId（必填）
     * @return Map: memberId/userMoney/productCount/products
     */
    Map<String, Object> infoQuery(Map<String, Object> req);

    /**
     * 2. 企业会员信息修改（联系人/经营信息等非核心字段）。
     *
     * @param req 入参 memberId/companyName/contactName/contactPhone 等
     * @param operatorId 操作人
     * @return 是否成功
     */
    Boolean enterpriseModify(Map<String, Object> req, String operatorId);

    /**
     * 3. 会员注册状态查询。
     *
     * @param req 入参 memberId
     * @return 状态码：0未注册 1已注册 2待审核 3已注销
     */
    Integer regStatus(Map<String, Object> req);

    /**
     * 4. 会员产品开通/关闭/查询（聚合操作）。
     *
     * @param req 入参 action(open/close/list)/memberId/productCode/...
     * @return action=list 时返回产品列表，否则返回单条记录
     */
    List<PayMemberProductEntity> productManage(Map<String, Object> req, String operatorId);
}
