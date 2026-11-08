package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinPersonEntity;
import com.houjiang.activity.mapper.ActivitySpinwinPersonMapper;
import com.houjiang.activity.service.ActivitySpinwinPersonService;
import com.houjiang.activity.vo.PrizeRecordResultVO;
import com.houjiang.activity.vo.SpinwinPersonListVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class ActivitySpinwinPersonServiceImpl extends ServiceImpl<ActivitySpinwinPersonMapper, ActivitySpinwinPersonEntity>
    implements ActivitySpinwinPersonService{

    /**
     * 查询抽奖人员列表
     *
     * @param param 查询参数，以Map形式传入，可包含分页信息和其他查询条件
     * @return 返回查询结果，包含分页信息和抽奖人员列表
     *
     * 本方法通过调用baseMapper的querySpinwinPersonList方法查询抽奖人员列表，
     * 并返回包装了查询结果的ResponseResult对象。
     * 使用Query对象的getPage方法结合传入的param参数来获取分页信息，
     * 并将分页信息和查询结果一起传递给PageUtils的getPage方法进行分页处理。
     */
    @Override
    public ResponseResult querySpinwinPersonList(Map<String, Object> param) {
        // 根据查询参数和分页信息查询抽奖人员列表
        IPage<SpinwinPersonListVO> page = this.baseMapper.querySpinwinPersonList(
                new Query<SpinwinPersonListVO>().getPage(param), param);

        // 返回包装了分页查询结果的ResponseResult对象
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据活动说明参数查询奖品记录。
     *
     * @param param 活动解释参数数据传输对象，包含查询条件和令牌信息。
     * @return 返回查询结果，包含奖品记录列表和记录总数。
     */
    @Override
    public ResponseResult queryPrizeRecord(ActivityExplainParamDTO param) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息为空，则返回令牌非法的错误结果
        if (userInfo == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        Map<String, Object> map = null;
        try {
            // 设置查询参数中的用户ID
            param.setUserId(userInfo.getUserId());
            // 将查询参数转换为Map格式
            //将对象转换为map
            Map data = ParseMapUtils.beanToMap(param);
            // 执行查询，获取分页结果
            //分页查询数据
            IPage<PrizeRecordResultVO> page = this.baseMapper.selectPrizeRecord(new Query<PrizeRecordResultVO>().getPage(data), param);
            List<PrizeRecordResultVO> records = page.getRecords();

            // 如果查询结果为空，则返回没有获奖记录的错误结果
            if (CollectionUtils.isEmpty(records)) {
                return ResponseResult.fail(0,"暂未有获奖记录");
            }

            // 如果查询参数表示查询个人记录，则对每条记录进行处理
            // 个人的抽奖记录
            if (param.getIsPersonRecord() == 1) {
                for (PrizeRecordResultVO houRedpacketTemplate : records) {

                    // 计算红包过期时间
                    houRedpacketTemplate.setOverplusTime(
                            DateUtils.getOverplusTime(new Date(), houRedpacketTemplate.getUpdateDate()));

                    // 设置红包领取状态
                    Integer redpacketState = houRedpacketTemplate.getRedpacketState();
                    // 如果未领取则可以领取 红包状态(1-未用,2-已用,3-过期
                    // ReceiveOrNot:0未领取1立即使用2不可领取3已用4过期
                    if (null == redpacketState) {
                        houRedpacketTemplate.setReceiveOrNot(0);
                    } else if (redpacketState == 2) {

                        houRedpacketTemplate.setReceiveOrNot(3);
                    } else if (redpacketState == 3) {
                        houRedpacketTemplate.setReceiveOrNot(4);
                    } else {
                        houRedpacketTemplate.setReceiveOrNot(1);
                    }

                }

            }
            // 构建返回结果Map，包含奖品记录列表和记录总数
            map = new HashMap<>();
            map.put("prizeRecord", records);
            map.put("prizeRecordCount", page.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回查询成功的结果，包含奖品记录信息
        return ResponseResult.success(map);
    }
}




