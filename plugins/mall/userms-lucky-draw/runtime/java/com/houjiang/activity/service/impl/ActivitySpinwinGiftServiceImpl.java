package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinGiftEntity;
import com.houjiang.activity.mapper.ActivitySpinwinGiftMapper;
import com.houjiang.activity.service.ActivitySpinwinGiftService;
import com.houjiang.activity.vo.SpinwinGiftListVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class ActivitySpinwinGiftServiceImpl extends ServiceImpl<ActivitySpinwinGiftMapper, ActivitySpinwinGiftEntity>
    implements ActivitySpinwinGiftService{

    /**
     * 删除幸运赢取礼品记录
     *
     * @param param 包含礼品ID等信息的参数映射
     * @return 删除操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult deleteSpinwinGift(Map<String, Object> param) {
        // 查询关联的活动数量
        Integer count = this.baseMapper.selectGiftActivityCount(param);
        // 如果存在关联的活动，则删除操作失败
        if (null != count && count.intValue() > 0) {
            return ResponseResult.fail(BizCodeEnume.REMOVE_THE_ASSOCIATED_ACTIVITY);
        }
        // 根据礼品ID删除礼品记录
        this.baseMapper.deleteById(param.get("giftId").toString());

        // 删除操作成功
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 更新大转盘礼品信息。
     * 该方法用于根据传入的参数更新大转盘游戏中的礼品信息。如果礼品ID为空，则插入新礼品信息；
     * 否则，更新现有礼品信息。如果红包ID为空或为0，则将礼品标记为非红包礼品。
     *
     * @param param 包含礼品相关信息的Map对象，其中可能包含礼品ID、红包ID等信息。
     * @return 返回操作结果，如果操作成功，则返回包含成功信息的ResponseResult对象。
     */
    @Override
    public ResponseResult updateSpinwinGift(Map<String, Object> param) {
        // 通过token获取用户信息，用于记录礼品的更新者信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());

        // 更新者姓名和更新时间入参
        param.put("updateBy", userInfo.getUserRealname());
        param.put("updateDate", new Date());

        // 判断是否为红包礼品，如果不是红包或红包ID为空，则设置礼品非红包属性
        //如果红包id为空 修改当前礼包状态为非红包
        if (null == param.get("redpacketTId") || "".equals(param.get("redpacketTId")) || 0 == Integer.parseInt(param.get("redpacketTId").toString())) {
            param.put("giftIsRedpacket", 2);
        }

        // 根据giftId是否存在，决定是插入还是更新礼品信息
        if (StringUtils.isEmpty(param.get("giftId").toString())) {
            this.baseMapper.insertSpinwinGift(param);
        } else {
            this.baseMapper.updateSpinwinGift(param);
        }

        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询大转盘礼品列表
     *
     * @param param 查询参数，包含分页信息和查询条件
     * @return 返回大转盘礼品的分页查询结果
     */
    @Override
    public ResponseResult querySpinwinGiftList(Map<String, Object> param) {
        // 根据查询参数和分页信息查询大转盘礼品列表
        IPage<SpinwinGiftListVO> page = this.baseMapper.selectSpinwinGiftList(new Query<SpinwinGiftListVO>().getPage(param), param);
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询指定活动的大转盘礼品详情
     *
     * @param param 查询参数，包含活动ID等信息
     * @return 返回指定活动的大转盘礼品详情列表
     */
    @Override
    public ResponseResult querySpinWinGift(ActivityExplainParamDTO param) {
        // 根据查询参数查询指定活动的大转盘礼品详情列表
        List<SpinwinGiftListVO> spinWinGiftList = this.baseMapper.selectSpinWinGift(param);
        return ResponseResult.success(spinWinGiftList);
    }
}




