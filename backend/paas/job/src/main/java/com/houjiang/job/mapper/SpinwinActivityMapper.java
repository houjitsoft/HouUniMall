package com.houjiang.job.mapper;

import com.houjiang.commons.vo.activity.EffectiveActivityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName SpinwinActivityMapper
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:58
 * @Version 1.0
 * @author Amos
 */

@Mapper
public interface SpinwinActivityMapper {

    /**
     * 选择过期的活动。
     * <p>
     * 该方法用于从数据库中检索已经过期的活动信息。过期活动是指那些已经超过设定有效期的活动。
     * 返回的结果列表中包含过期活动的ID，这些ID可以用于进一步的操作，比如从数据库中删除这些活动。
     *
     * @return List<Integer> 包含过期活动ID的列表。
     */
    List<Integer> selectExpireActivity();

    /**
     * 更新幸运抽奖活动。
     * <p>
     * 该方法用于更新指定的幸运抽奖活动。通过传递一个活动ID列表，可以批量更新这些活动的状态或者其它相关信息。
     * 参数中的@Param注解用于指定参数在SQL语句中的名称，这对于使用命名参数的查询非常有用。
     *
     * @param activityIdList List<Integer> 幸运抽奖活动的ID列表，用于指定需要更新的活动。
     */
    void updateLuckyDraw(@Param("activityIdList") List<Integer> activityIdList);

    /**
     * 更新活动状态。
     * <p>
     * 该方法用于更新指定活动的状态。通过传递一个活动ID列表，可以批量更新这些活动的状态到指定的新状态。
     * 参数中的@Param注解用于指定参数在SQL语句中的名称，这对于使用命名参数的查询非常有用。
     *
     * @param activityIdList List<Integer> 活动的ID列表，用于指定需要更新状态的活动。
     */
    void updateActivityStatus(@Param("activityIdList") List<Integer> activityIdList);

    /**
     * 选择有效的活动。
     * <p>
     * 该方法用于从数据库中检索仍然有效的活动信息。有效的活动是指那些还没有超过设定有效期的活动。
     * 返回的结果列表中包含有效活动的详细信息，这些信息可以用于展示给用户或者进行其它业务操作。
     *
     * @return List<EffectiveActivityVO> 包含有效活动信息的列表。
     */
    List<EffectiveActivityVO> selectEffectiveActivity();

    /**
     * 更新每日抽奖活动。
     * <p>
     * 该方法用于更新指定的每日抽奖活动。每日抽奖活动是一种特殊的活动类型，需要定期更新其配置或者状态。
     * 通过传递一个包含活动详细信息的EffectiveActivityVO对象，可以更新活动的相关信息。
     *
     * @param activity EffectiveActivityVO 包含需要更新的每日抽奖活动信息的对象。
     */
    void updateDailyLottery(EffectiveActivityVO activity);
}
