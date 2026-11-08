package com.houjiang.job.service.impl;

import com.houjiang.job.mapper.UpdateSalesPromStateMapper;
import com.houjiang.job.service.UpdateSalesPromStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Amos
 * @date 2023/5/17 11:31
 */
@Service
public class UpdateSalesPromStateServiceImpl implements UpdateSalesPromStateService {

    @Autowired
    private UpdateSalesPromStateMapper updateSalesPromStateMapper;

    /**
     * 更新销售促销状态的方法。
     * 该方法主要用于更新红包和代金券的临时和基础状态，通过调用对应的Mapper方法来实现。
     * 返回值为更新结果的描述字符串，详细说明了更新的红包和代金券的数量。
     *
     * @return 更新结果的描述字符串。
     */
    @Override
    public String updateState() {
        // 更新红包临时状态，记录更新数量
        Integer redPacTempInteger = updateSalesPromStateMapper.updateRedpkTempState();
        // 更新红包基础状态，记录更新数量
        Integer redPacBaseInteger = updateSalesPromStateMapper.updateRedpkBaseState();
        // 更新代金券临时状态，记录更新数量
        Integer voucherTempInteger = updateSalesPromStateMapper.updateVouchrTempState();
        // 更新代金券基础状态，记录更新数量
        Integer voucherBaseInteger = updateSalesPromStateMapper.updateVouchrBaseState();

        // 构造并返回更新结果的描述字符串
        return "本次更新代金券模板  " + redPacTempInteger + " 张，" +
                "用户已领的代金券  " + redPacBaseInteger + " 张。" +
                "更新红包模板  " + voucherTempInteger + " 张，" +
                "用户已领的红包  " + voucherBaseInteger + " 张";
    }

}
