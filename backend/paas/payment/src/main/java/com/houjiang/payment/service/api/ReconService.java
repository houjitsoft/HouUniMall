package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.entity.PayReconDiffEntity;
import com.houjiang.payment.entity.PayReconFileEntity;
import com.houjiang.payment.entity.PayReconReportEntity;

import java.util.Map;

/**
 * 自研对账服务接口（W4 完整版）
 *
 * 三层定位：自有平台对账域，对标通企付能力但完全自研。
 * 数据流：底层渠道账单文件 → recon_file → 与本地订单 diff → recon_diff → 汇总 recon_report。
 * 字段语义：本平台自有 local_order_no/channel_order_no/handle_status，与通企付 reqSeqNo/tqfXXX 解耦。
 *
 * @author Amos
 */
public interface ReconService {

    /**
     * 拉取渠道账单文件并入库。
     * 实现逻辑：生成模拟渠道账单数据（基于本地交易 + 随机差异），写入 recon_file 并标记 status=1(已拉取)。
     * 凭证就绪后可替换为真实 SDK 拉取。
     *
     * @param req 入参 channel/billDate/fileType
     * @return 创建的 recon_file 记录
     */
    PayReconFileEntity pullChannelBill(Map<String, Object> req);

    /**
     * 对账核对：比对本地订单与渠道账单，生成差异记录。
     *
     * @param billDate    账单日期 yyyy-MM-dd
     * @param channelCode 渠道编码
     * @return 本次生成的差异数量
     */
    int diffCheck(String billDate, String channelCode);

    /**
     * 差异列表分页查询。
     *
     * @param req 入参 page/size/channel/diffType/handleStatus/localOrderNo
     * @return 分页结果
     */
    IPage<PayReconDiffEntity> diffList(Map<String, Object> req);

    /**
     * 差异处理：标记差异处理结果（补单/退款/忽略/人工核销）。
     *
     * @param req 入参 diffId/handleStatus/handleRemark/handleUserId
     * @return 是否成功
     */
    Boolean diffHandle(Map<String, Object> req);

    /**
     * 月度对账报表生成（按商户+渠道+月份汇总差异统计）。
     *
     * @param req 入参 mchNo/channel/reportMonth(yyyy-MM)
     * @return 创建的 recon_report 记录
     */
    PayReconReportEntity reportGen(Map<String, Object> req);

    /**
     * 对账报表分页查询。
     *
     * @param req 入参 page/size/channel/billMonth
     * @return 分页结果
     */
    IPage<PayReconReportEntity> reportList(Map<String, Object> req);

    /**
     * 手动触发对账（拉取账单 + 差异核对），供管理后台调用。
     *
     * @param req 入参 billDate/channel（channel 为空则触发全部渠道）
     * @return 各渠道差异数量汇总
     */
    Map<String, Object> trigger(Map<String, Object> req);
}
