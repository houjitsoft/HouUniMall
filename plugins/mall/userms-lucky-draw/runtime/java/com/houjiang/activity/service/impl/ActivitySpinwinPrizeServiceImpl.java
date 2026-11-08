package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.entity.ActivitySpinwinPrizeEntity;
import com.houjiang.activity.mapper.ActivitySpinwinPrizeMapper;
import com.houjiang.activity.service.ActivitySpinwinPrizeService;
import com.houjiang.activity.vo.SpinwinPrizeListVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class ActivitySpinwinPrizeServiceImpl extends ServiceImpl<ActivitySpinwinPrizeMapper, ActivitySpinwinPrizeEntity>
    implements ActivitySpinwinPrizeService{

    /**
     * 删除抽奖奖品接口
     *
     * @param param 包含奖品ID的Map对象
     * @return 返回操作结果的ResponseResult对象
     *
     * 本方法通过奖品ID删除抽奖奖品信息。
     * 使用baseMapper的.deleteById方法根据奖品ID删除相应记录。
     * 如果删除成功，返回操作成功的ResponseResult对象。
     */
    @Override
    public ResponseResult deleteSpinwinPrize(Map<String, Object> param) {
        // 调用baseMapper的.deleteById方法，根据奖品ID删除奖品记录
        this.baseMapper.deleteById(param.get("prizeId").toString());
        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 更新抽奖奖项信息。
     * 该方法用于根据传入的参数更新或插入抽奖奖项信息。如果参数中包含奖品ID，则为更新操作；
     * 否则，视为新奖品插入操作。
     *
     * @param param 一个Map对象，包含更新或插入奖品所需的所有信息。
     *               参数中应包含token用于识别操作用户，prizeId用于判断是更新还是插入操作，
     *               以及其他奖品相关信息。
     * @return 返回操作结果，如果操作成功，返回包含成功信息的ResponseResult对象。
     */
    @Override
    public ResponseResult updateSpinwinPrize(Map<String, Object> param) {
        // 通过token获取用户信息，用于记录操作人信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());

        // 将操作人信息和当前时间添加到参数中，用于更新操作记录
        param.put("updateBy", userInfo.getUserRealname());
        param.put("updateDate", new Date());

        // 判断是否为更新操作，如果是新奖品，则插入；否则，更新奖品信息
        if (null == param.get("prizeId") || param.get("prizeId").equals("")) {
            this.baseMapper.insertSpinwinPrize(param);
        } else {
            this.baseMapper.updateSpinwinPrize(param);
        }

        // 返回操作成功的结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }
    /**
     * 查询抽奖奖品列表
     *
     * @param param 查询参数，以Map形式传递，包含各种过滤条件或排序规则等。
     * @return ResponseResult 对象，其中包含查询结果列表。如果查询成功，结果列表将被封装在成功响应对象中返回。
     */
    @Override
    public ResponseResult querySpinwinPrizeList(Map<String, Object> param) {
        // 通过baseMapper的selectSpinwinPrizeList方法查询抽奖奖品列表
        List<SpinwinPrizeListVO> list = this.baseMapper.selectSpinwinPrizeList(param);
        // 返回查询结果的成功响应，将查询到的奖品列表作为数据部分返回给调用方
        return ResponseResult.success(list);
    }

}




