package com.houjiang.merchant.common.modelAttr;

import com.houjiang.merchant.common.enums.UserWithDrawEnum;

import java.util.Arrays;
import java.util.Map;

public class UserWithdrawAttr {
    /**
     * @notes can_transfer 可转账
     * @param fieldValue 字段值
     * @param data 数据
     * @return int
     * @author Amos
     * @datetime 2023-04-21 16:21:04
     */
    public static int getCanTransferAttr(Map<String, Object> data) {
        // 获取状态和类型
        Integer status = (Integer) data.get("status");
        Integer type = (Integer) data.get("type");

        // 获取可转账的类型数组
        int[] transferArr = UserWithDrawEnum.WithdrawType.getTransferArr();

        // 判断条件：状态为提现中(1) 且 类型在可转账数组中
        boolean canTransfer = status != null &&
                type != null &&
                status.equals(UserWithDrawEnum.WithdrawStatus.STATUS_ING.getValue()) &&
                Arrays.stream(transferArr).anyMatch(t -> t == type);

        return canTransfer ? 1 : 0;
    }
}
