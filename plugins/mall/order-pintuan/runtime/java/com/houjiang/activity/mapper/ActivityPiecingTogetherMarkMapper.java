package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.activity.entity.ActivityPiecingTogetherMarkEntity;
import com.houjiang.activity.vo.GoodsPinTuanResultVO;
import com.houjiang.activity.vo.PintuanMarkMessageVO;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName ActivityPiecingTogetherMarkMapper
 * @Description  拼团，团长标识-我的拼团
 * @Author Amos
 * @Date 2023-06-01 11:57:59
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityPiecingTogetherMarkMapper extends BaseMapper<ActivityPiecingTogetherMarkEntity> {

    /**
     * 插入团长
     *
     * @param markMessage
     */
    void insertPintuanMark(@Param("param") PintuanMarkMessageVO markMessage);

    /**
     * 更新团长信息
     *
     * @param markMessage
     */
    void updateMarkMessage(@Param("param") PintuanMarkMessageVO markMessage);

    /**
     * 查询拼团信息
     *
     * @param markId
     * @return
     */
    GoodsPinTuanResultVO queryPintByMarkId(@Param("markId") String markId);


    /**
     * 选择未形成的标记ID
     * 通过特定的拼团ID来查询数据库中未形成的标记ID
     * 这些标记ID代表了可能需要进一步处理或关注的数据项
     *
     * @param pintuanId 拼团ID，用于定位特定拼团活动
     * @return 返回一个字符串列表，包含未形成的标记ID
     */
    List<String> selectUnformedMarkId(@Param("pintuanId") Integer pintuanId);

    /**
     * 更新标记状态
     * 根据提供的更新数据对象来更新数据库中标记的状态
     * 这通常用于更新标记是否已过期或其他状态变更
     *
     * @param param 包含更新信息的数据传输对象，用于指定哪些标记需要更新及其新的状态
     */
    void updateMarkStatus(@Param("param") ExpireMarkIdUpdateDTO param);

    /**
     * 查询当前团 还差几人成团
     *
     * @param markId
     * @return
     */
    Integer querySurplusNum(@Param("markId") String markId);

    /**
     * 查询除指定订单外的其他订单是否已支付
     *
     * @param markId  用于标识订单的标记ID
     * @param orderId 需要排除的订单ID
     * @return 返回一个整数，表示除指定订单外的其他订单是否已支付，非零表示已支付
     */
    Integer otherOrderIsPay(@Param("markId") String markId, @Param("orderId") String orderId);


    /**
     * 根据标记ID查询订单ID列表
     *
     * @param markId 用于标识订单的标记ID
     * @return 返回一个包含多个订单ID的列表
     */
    List<String> selectByMarkId(@Param("markId") String markId);

    /**
     * 按已付款人数成团：置团状态为已成团（幂等：仅当当前 status=0）
     */
    int updateMarkFormedByMarkId(@Param("markId") String markId);

    /**
     * 查询某团配置的成团人数
     */
    Integer selectPersonNumByMarkId(@Param("markId") String markId);

}

