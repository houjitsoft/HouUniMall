package com.houjiang.job.service.impl;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.order.CommissionSettleVO;
import com.houjiang.commons.vo.payment.PayFxRecordVO;
import com.houjiang.job.mapper.OrderCommissionMapper;
import com.houjiang.job.mapper.PayFxAccountMapper;
import com.houjiang.job.mapper.PayFxRecordMapper;
import com.houjiang.job.service.PayFxService;
import com.houjiang.job.service.PayFxTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName PayFxTaskServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:25
 * @Version 1.0
 * @author Amos
 */
@Service
public class PayFxTaskServiceImpl implements PayFxTaskService {

    private static final Logger logger = LoggerFactory.getLogger(PayFxTaskServiceImpl.class);

    @Autowired
    private PayFxAccountMapper payFxAccountMapper;
    @Autowired
    private PayFxRecordMapper payFxRecordMapper;
    @Autowired
    private OrderCommissionMapper orderCommissionMapper;
    @Autowired
    private PayFxService payFxService;

    /**
     * 使用 ForkJoinPool 并行处理订单佣金结算。
     * 该方法利用 ForkJoinPool 的并行计算能力来提高处理大量订单佣金结算的效率。
     * 它提交一个 VKOrderCommissionTaskUtils 任务，该任务将订单佣金结算列表分批处理。
     *
     * @param commissionSettleList 委托结算列表，包含需要结算的佣金信息。
     * @param commissionType       佣金类型，用于指定不同类型的佣金结算规则。
     * @return 返回一个布尔值，表示佣金结算金额是否成功更新。
     */
    @Override
    public Boolean vkOrderCommission(List<CommissionSettleVO> commissionSettleList, Integer commissionType) {
        // 初始化状态标志，用于表示佣金金额是否成功更新。
        Boolean updateAmountStatus = false;
        // 初始化 ForkJoinPool，用于执行并行计算任务。
        ForkJoinPool forkJoinPool = null;
        try {
            // 创建一个新的 ForkJoinPool。
            forkJoinPool = new ForkJoinPool();
            // 提交一个 VKOrderCommissionTaskUtils 任务，并等待任务完成。
            // 任务将根据佣金类型和列表的指定范围处理佣金结算。
            ForkJoinTask<Boolean> submit = forkJoinPool.submit(new VKOrderCommissionTaskUtils(commissionSettleList, commissionType, 0, commissionSettleList.size() - 1));
            // 获取任务的处理结果，表示佣金金额是否成功更新。
            updateAmountStatus = submit.get();
            // 等待 ForkJoinPool 中的所有任务完成。
            // 阻塞当前线程直到 ForkJoinPool 中所有的任务都执行结束
            forkJoinPool.awaitTermination(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 记录异常信息。
            logger.error(e.getMessage());
        } finally {
            // 关闭 ForkJoinPool。
            if (forkJoinPool != null) {
                forkJoinPool.shutdown();
            }
        }
        // 返回佣金金额是否成功更新的状态。
        return updateAmountStatus;
    }


    /**
     * 拆分任务
     */
    class VKOrderCommissionTaskUtils extends RecursiveTask<Boolean> {
        /**
         * 每个"小任务"最多只处理的个数
         */
        private static final int MAX = 100;
        List<CommissionSettleVO> taskList;
        Integer commissionType;
        private int start;
        private int end;

        public VKOrderCommissionTaskUtils(List<CommissionSettleVO> taskList, Integer commissionType, int start, int end) {
            this.taskList = taskList;
            this.commissionType = commissionType;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Boolean compute() {
            Boolean flag = false;
            if ((end - start) <= MAX) {
                List<CommissionSettleVO> commissionSettleList = taskList.subList(start, end + 1);

                if (!CollectionUtils.isEmpty(commissionSettleList)) {

                    flag = orderCommission(commissionSettleList);

                    return flag;
                }
            } else {
                logger.info("=====任务分解======");
                // 使用二分法分配任务
                int middle = (start + end) / 2;
                PayFxTaskServiceImpl.VKOrderCommissionTaskUtils left = new PayFxTaskServiceImpl.VKOrderCommissionTaskUtils(taskList, commissionType, start, middle);
                PayFxTaskServiceImpl.VKOrderCommissionTaskUtils right = new PayFxTaskServiceImpl.VKOrderCommissionTaskUtils(taskList, commissionType, middle + 1, end);

                // 并行执行两个小任务
                left.fork();
                right.fork();

                Boolean rightFlag = right.join();
                Boolean leftFlag = left.join();
                if (rightFlag && leftFlag) {
                    flag = true;
                }
            }
            return flag;
        }
    }

    /**
     * 佣金结算记录
     * 批量处理佣金结算，包括更新用户提现金额和生成结算交易单
     *
     * @param commissionSettleList 佣金结算信息列表，包含待处理的佣金结算数据
     * @return 返回处理结果，true表示成功，false表示失败
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean orderCommission(List<CommissionSettleVO> commissionSettleList) {
        boolean flag = true;
        try {
            // 遍历佣金结算信息列表，处理每个佣金结算记录
            for (CommissionSettleVO commissionSettle : commissionSettleList) {
                // 根据佣金结算信息，构建并保存佣金结算记录
                List<PayFxRecordVO> payFxRecordList = payFxService.buildFxRecord(commissionSettle);
                payFxRecordMapper.insertBatch(payFxRecordList);

                // 更新分享用户的分享佣金信息
                if (commissionSettle.getFxCommissionMoney() != null && !StringUtils.isEmpty(commissionSettle.getShareUserId())) {
                    // 查询分享用户当前的佣金信息
                    CommissionSettleVO vo = payFxAccountMapper.selectVkUserFxCommission(commissionSettle.getShareUserId());
                    // 更新分享用户的总分享佣金和待提现佣金
                    vo.setTotalShareCommission(vo.getTotalShareCommission().add(commissionSettle.getFxCommissionMoney()).setScale(2, RoundingMode.HALF_UP));
                    vo.setPendingWithdrawnCommission(vo.getPendingWithdrawnCommission().add(commissionSettle.getFxCommissionMoney()).setScale(2, RoundingMode.HALF_UP));
                    // 保存更新后的分享用户佣金信息
                    payFxAccountMapper.updateVkUserFxCommission(vo.getTotalShareCommission().setScale(2, RoundingMode.HALF_UP),
                            vo.getPendingWithdrawnCommission().setScale(2, RoundingMode.HALF_UP), commissionSettle.getShareUserId());
                }

                // 更新一级推广用户的推广佣金信息
                if (commissionSettle.getOneTgCommissionMoney() != null && !StringUtils.isEmpty(commissionSettle.getTgOneUserId())) {
                    // 查询一级推广用户当前的佣金信息
                    CommissionSettleVO vo = payFxAccountMapper.selectVkUserFxCommission(commissionSettle.getTgOneUserId());
                    // 更新一级推广用户的总推广佣金和待提现佣金
                    vo.setTotalTgCommission(vo.getTotalTgCommission().add(commissionSettle.getOneTgCommissionMoney()).setScale(2, RoundingMode.HALF_UP));
                    vo.setPendingWithdrawnCommission(vo.getPendingWithdrawnCommission().add(commissionSettle.getOneTgCommissionMoney()).setScale(2, RoundingMode.HALF_UP));
                    // 保存更新后的一级推广用户佣金信息
                    payFxAccountMapper.updateVkUserTgCommission(vo.getTotalTgCommission().setScale(2, RoundingMode.HALF_UP),
                            vo.getPendingWithdrawnCommission().setScale(2, RoundingMode.HALF_UP), commissionSettle.getTgOneUserId());
                }

                // 更新二级推广用户的推广佣金信息
                if (commissionSettle.getTwoTgCommissionMoney() != null && !StringUtils.isEmpty(commissionSettle.getTgTwoUserId())) {
                    // 查询二级推广用户当前的佣金信息
                    CommissionSettleVO vo = payFxAccountMapper.selectVkUserFxCommission(commissionSettle.getTgTwoUserId());
                    // 更新二级推广用户的总推广佣金和待提现佣金
                    vo.setTotalTgCommission(vo.getTotalTgCommission().add(commissionSettle.getTwoTgCommissionMoney()).setScale(2, RoundingMode.HALF_UP));
                    vo.setPendingWithdrawnCommission(vo.getPendingWithdrawnCommission().add(commissionSettle.getTwoTgCommissionMoney()).setScale(2, RoundingMode.HALF_UP));
                    // 保存更新后的二级推广用户佣金信息
                    payFxAccountMapper.updateVkUserTgCommission(vo.getTotalTgCommission().setScale(2, RoundingMode.HALF_UP),
                            vo.getPendingWithdrawnCommission().setScale(2, RoundingMode.HALF_UP), commissionSettle.getTgTwoUserId());
                }

                // 更新当前用户订单对应的结算单号
                String recordIds = JSON.toJSONString(payFxRecordList.stream().map(PayFxRecordVO::getRecordId).collect(Collectors.toList()));
                orderCommissionMapper.updateOrderSettleBatch(commissionSettle.getOrderId(), recordIds);
            }

        } catch (Exception e) {
            // 异常处理：设置标志为失败，并记录日志，同时回滚事务
            flag = false;
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        // 返回处理结果
        return flag;
    }

}
