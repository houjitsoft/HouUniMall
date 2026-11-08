package com.houjiang.job.service.impl;


import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.mapper.UpdateCommonStatuJobMapper;
import com.houjiang.job.service.UpdateCommonStatuJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Amos
 * @date 2023/5/17 14:52
 */
@Service
public class UpdateCommonStatuJobServiceImpl implements UpdateCommonStatuJobService {

    @Autowired
    private UpdateCommonStatuJobMapper updateCommonStatuJobMapper;

    /**
     * 根据状态查询商品ID，并更新状态。
     * 本方法用于查询common表和goods表中状态为"0"的商品ID，分别进行状态更新，并将goods表中状态为"0"的商品ID发送至MQ用于进一步处理。
     *
     * @return 返回操作结果，成功则返回操作成功的消息。
     */
    @Override
    public ResponseResult queryGoodsIdByState() {


        // 初始化用于存储goods表中状态为"0"的商品ID的列表
        List<Integer> commonIdsEs = new ArrayList<>();

        // 从goods表中查询状态为"0"的商品ID
        List<Map<String, String>> goodsIdsBase = updateCommonStatuJobMapper.queryGoodsIdByBase();
        if(goodsIdsBase != null && goodsIdsBase.size() > 0){
            List<String> commonIds = goodsIdsBase.stream()
                    // 提取 commonId
                    .map(map -> String.valueOf(map.get("commonId")))
                    .collect(Collectors.toList());
            for (String commonId : commonIds) {
                // 判断当前coommonId 下是否还有库存不为0的商品
                // 从common表中查询状态为"0"的商品ID
                Integer count = updateCommonStatuJobMapper.queryCommonIdBystock(commonId);
                if(count == 0){
                    updateCommonStatuJobMapper.updateCommonStatu(commonId);
                    commonIdsEs.add(Integer.valueOf(commonId));
                }
            }
            // 下架商品库存为 0 的 sku
            for (Map<String, String> goodsId : goodsIdsBase) {
                updateCommonStatuJobMapper.updateGoodsStatuGoodsId(String.valueOf(goodsId.get("goodsId")));
            }
        }

        if(commonIdsEs.size() > 0 ){
            Map<String, Object> map = new HashMap<>();
            // 将goods表中状态为"0"的商品ID列表及类型信息发送至MQ
            map.put("commonIdS", commonIdsEs);
            // 1 是添加  2是删除
            map.put("type", 2);
            MqSendUtils.mqSendGoods(map);
        }
        // 返回操作成功的结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
    }
}
