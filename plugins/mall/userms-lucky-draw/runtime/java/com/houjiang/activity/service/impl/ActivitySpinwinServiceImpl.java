package com.houjiang.activity.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.dto.GiftDTO;
import com.houjiang.activity.entity.ActivitySpinwinEntity;
import com.houjiang.activity.mapper.ActivitySpinwinLuckyDrawTimesMapper;
import com.houjiang.activity.mapper.ActivitySpinwinMapper;
import com.houjiang.activity.mapper.ActivitySpinwinPersonMapper;
import com.houjiang.activity.mapper.ActivitySpinwinPrizeMapper;
import com.houjiang.activity.service.ActivitySpinwinService;
import com.houjiang.plugin.api.extension.activity.ActivityRedpacketGrantExtension;
import com.houjiang.activity.utils.LotteryUtils;
import com.houjiang.activity.vo.DrawTimesDetailsVO;
import com.houjiang.activity.vo.SpinWinDrawDetailVO;
import com.houjiang.activity.vo.SpinWinDrawTimesVO;
import com.houjiang.activity.vo.SpinwinLuckyDrawVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisLock;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class ActivitySpinwinServiceImpl extends ServiceImpl<ActivitySpinwinMapper, ActivitySpinwinEntity>
        implements ActivitySpinwinService {
    private static final Logger log = LoggerFactory.getLogger(ActivitySpinwinServiceImpl.class);

    /**
     * ActivityRedpacketGrantExtension 扩展点容器：红包发放由红包插件自行执行。
     * <p>拆分为独立插件后，插件间不允许互相 import，也不得共享数据层，
     * 故抽奖中奖发红包一律走扩展点。
     */
    private final java.util.Map<String, ActivityRedpacketGrantExtension> redpacketGrants =
            new java.util.concurrent.ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void registerRedpacketGrants(java.util.List<ActivityRedpacketGrantExtension> grants) {
        if (grants == null) {
            return;
        }
        for (ActivityRedpacketGrantExtension impl : grants) {
            String k = impl.scene();
            if (k != null && !k.isBlank()) {
                redpacketGrants.putIfAbsent(k, impl);
            }
        }
    }

    /**
     * 请求发放一张红包；无实现或发放失败时返回 null（本次不发放），**不中断抽奖主流程**。
     */
    private Integer grantRedpacket(String scene, Integer templateId, Map<String, String> params) {
        ActivityRedpacketGrantExtension ext = redpacketGrants.get(scene);
        if (ext == null) {
            log.warn("红包发放扩展缺失，跳过发放: scene={}", scene);
            return null;
        }
        ActivityRedpacketGrantExtension.Context ctx = new ActivityRedpacketGrantExtension.Context();
        ctx.scene = scene;
        ctx.redpacketTemplateId = templateId;
        if (params != null) {
            ctx.params.putAll(params);
        }
        try {
            ext.execute(ctx);
        } catch (RuntimeException ex) {
            log.error("红包发放扩展执行失败: scene=" + scene, ex);
            return null;
        }
        if (ctx.isAborted()) {
            log.error("红包发放失败: scene=" + scene + ", reason=" + ctx.getAbortReason());
            return null;
        }
        return ctx.redpacketId;
    }

    @Autowired
    private ActivitySpinwinLuckyDrawTimesMapper activitySpinwinLuckyDrawTimesMapper;
    @Autowired
    private ActivitySpinwinPersonMapper activitySpinwinPersonMapper;
    @Autowired
    private ActivitySpinwinPrizeMapper activitySpinwinPrizeMapper;

    @Override
    public ResponseResult updateSpinwinLimit(Map<String, Object> param) {
        this.baseMapper.updateSpinwinLimit(param);
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据活动ID查询抽奖限制信息。
     *
     * @param param 包含活动ID的Map对象。
     * @return 返回查询结果的ResponseResult对象，其中包含抽奖限制实体。
     */
    @Override
    public ResponseResult querySpinwinLimit(Map<String, Object> param) {
        // 通过活动ID查询抽奖限制实体
        ActivitySpinwinEntity entity = this.baseMapper.selectById(param.get("activityId").toString());
        // 返回查询结果的成功响应
        return ResponseResult.success(entity);
    }

    /**
     * 删除抽奖活动接口
     *
     * @param param 包含活动ID的Map对象
     * @return 返回操作结果的ResponseResult对象
     * <p>
     * 本方法通过活动ID删除指定的抽奖活动。
     * 删除操作通过调用baseMapper的DeleteById方法实现，使用活动ID作为删除条件。
     * 删除成功后，返回表示操作成功的ResponseResult对象。
     */
    @Override
    public ResponseResult deleteSpinwinActivity(Map<String, Object> param) {
        // 根据活动ID删除抽奖活动
        this.baseMapper.deleteById(param.get("activityId").toString());
        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询抽奖活动详情
     *
     * @param param 包含活动ID的Map对象
     * @return 返回抽奖活动详情的响应结果
     * <p>
     * 本方法通过活动ID查询抽奖活动的详细信息，并将查询结果封装成响应结果返回。
     * 使用了BaseMapper的selectById方法来获取特定ID的抽奖活动实体。
     */
    @Override
    public ResponseResult querySpinwinActivityDetail(Map<String, Object> param) {
        // 根据活动ID查询抽奖活动实体
        ActivitySpinwinEntity entity = this.baseMapper.selectById(param.get("activityId").toString());
        // 返回查询结果成功的响应，包含抽奖活动实体
        return ResponseResult.success(entity);
    }

    /**
     * 查询抽奖活动列表
     *
     * @param param 查询参数，包含分页信息和过滤条件
     * @return 返回抽奖活动列表的响应结果
     */
    @Override
    public ResponseResult querySpinwinActivityList(Map<String, Object> param) {
        // 根据查询参数和分页信息获取抽奖活动列表
        IPage<ActivitySpinwinEntity> page = this.baseMapper.selectSpinwinActivityList(
                new Query<ActivitySpinwinEntity>().getPage(param), param);

        // 将分页结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 更新抽奖活动信息。
     * 通过传入的参数更新抽奖活动的数据，如果活动ID不存在，则插入新的抽奖活动；否则，更新已存在的抽奖活动。
     *
     * @param param 一个Map对象，包含所有要更新的抽奖活动属性。
     * @return 返回一个表示操作结果的ResponseResult对象，成功时包含操作成功的代码。
     */
    @Override
    public ResponseResult updateSpinwinActivity(Map<String, Object> param) {
        // 从参数中提取用户令牌，并解析出用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());

        // 将当前操作用户的姓名和操作时间更新到参数中
        param.put("updateBy", userInfo.getUserRealname());
        param.put("updateDate", new Date());

        // 判断活动ID是否存在，如果不存在则插入新的活动，否则更新现有活动
        if (null == param.get("activityId") || param.get("activityId").equals("")) {
            this.baseMapper.insertSpinwinActivity(param);
        } else {
            this.baseMapper.updateSpinwinActivity(param);
        }

        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询抽奖次数和参与次数的接口。
     *
     * @param param 包含活动解释参数的DTO对象。
     * @return 返回抽奖次数和参与次数等相关信息的结果对象。
     */
    @Override
    @Transactional
    public ResponseResult querySpinWinDrawTimes(ActivityExplainParamDTO param) {
        // 初始化返回的抽奖次数和参与次数信息
        SpinWinDrawTimesVO result = new SpinWinDrawTimesVO();

        // 根据参数查询抽奖详情
        SpinWinDrawDetailVO detail = this.baseMapper.selectDrawDetail(param);

        // 如果抽奖详情为空，则表示暂无可用活动
        if (null == detail) {
            return ResponseResult.fail(0, "暂未有可用的活动");
        }

        // 解析token获取用户信息，用于后续设置用户ID和用户名
        // 获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        param.setUserId(userInfo.getUserId());
        param.setUserName(userInfo.getUserName());

        // 获取抽奖详情中的参与次数、总抽奖次数限制和每人中奖次数限制
        Integer partakeNum = detail.getPartakeNum();
        Integer totalSweepstakes = detail.getTotalSweepstakesLimit();
        Integer winningTimesPerPerson = detail.getWinningTimesPerPerson();

        // 如果显示参与次数，则设置到结果对象中
        if (detail.getPartakeNumIsDisplay() == 1) {
            result.setPartakeNum(partakeNum);
        }

        // 如果参与次数有限制且已达到限制次数，则返回活动人数已超上限的提示
        if (detail.getPartakeNumIsLimit() == 2 && partakeNum >= detail.getPartakeNumLimit()) {
            return ResponseResult.success(0, "活动人数已超上限");
        }

        // 查询用户的抽奖次数详情
        // 查询抽奖次数
        Integer surplusDrawTimes = 0;
        Integer surplusTimes = 0;
        DrawTimesDetailsVO drawTimesDetails = activitySpinwinLuckyDrawTimesMapper.selectDrawTimes(param);

        // 如果抽奖次数详情存在，则更新抽奖次数和剩余抽奖次数
        if (null != drawTimesDetails) {
            surplusDrawTimes = drawTimesDetails.getSurplusDrawTimes();
            surplusTimes = drawTimesDetails.getNumberOfDailyLottery() - drawTimesDetails.getNumSweepstakesToday();
        } else {
            // 如果抽奖次数详情不存在，则初始化抽奖次数和剩余抽奖次数，并插入新的抽奖次数记录
            surplusDrawTimes = totalSweepstakes;
            param.setSurplusDrawTimes(surplusDrawTimes);
            param.setLuckyDrawTimes(totalSweepstakes);
            param.setNumberOfDailyLottery(winningTimesPerPerson);
            surplusTimes = winningTimesPerPerson;

            activitySpinwinLuckyDrawTimesMapper.insertDrawTimes(param);
        }

        // 设置返回结果中的抽奖次数和剩余抽奖次数等相关信息
        result.setSurplusDrawTimes(surplusDrawTimes);
        result.setTurntableBackground(detail.getTurntableBackground());
        result.setSurplusTimes(surplusTimes);
        result.setPartakeNumIsLimit(detail.getPartakeNumIsLimit());

        // 返回查询结果
        return ResponseResult.success(result);
    }

    /**
     * 根据活动解释参数查询活动解释信息。
     *
     * @param param 活动解释参数数据传输对象，包含查询活动解释所需的具体条件。
     * @return 返回查询结果的响应结果对象，其中包含查询到的活动解释信息。
     */
    @Override
    public ResponseResult queryActivityExplain(ActivityExplainParamDTO param) {
        // 通过基础Mapper接口查询活动解释信息，返回查询结果。
        String graphicExplanation = this.baseMapper.selectActivityExplain(param);

        // 构建并返回查询成功的响应结果，其中包含查询到的活动解释信息。
        return ResponseResult.success(graphicExplanation);
    }

    /**
     * 更新抽奖记录
     * 根据传入的参数更新抽奖记录，并返回抽奖结果。
     *
     * @param param 活动解释参数数据传输对象，包含抽奖所需的具体条件。
     * @return 返回抽奖结果的响应结果对象。
     */
    @Override
    public ResponseResult updateLotteryDraw(ActivityExplainParamDTO param) {
        RedisLock redisLock = null;
        try (Jedis jedis = RedisUtil.getJedis()) {
            // 从参数字符串中解析出公共参数。
            // 解析用户信息
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
            param.setUserId(userInfo.getUserId());

            // 创建RedisLock实例，用于实现分布式锁。
            redisLock = new RedisLock(jedis, "user-updateLotteryDraw-" + userInfo.getUserId() + "-" + param.getActivityId(), 10000);

            // 尝试获取分布式锁，如果成功则执行订单创建逻辑。
            // 用户锁
            if (redisLock.lock()) {
                // 查询抽奖详情
                SpinWinDrawDetailVO detail = this.baseMapper.selectDrawDetail(param);

                // 检查抽奖详情是否存在
                if (detail == null) {
                    return ResponseResult.fail(0, "暂未有可用的活动");
                }

                // 判断是否超出参与人数限制
                // 参与人数
                Integer partakeNum = detail.getPartakeNum();
                // 参与人数限制
                Integer partakeNumLimit = detail.getPartakeNumLimit();
                // 总抽奖机会是否限制：1-不限，2-限制
                Integer totalSweepstakesIsLimit = detail.getTotalSweepstakesIsLimit();
                // 中奖次数
                Integer winningTimesPerPerson = detail.getWinningTimesPerPerson();
                // 总抽奖机会
                Integer totalSweepstakesLimit = detail.getTotalSweepstakesLimit();

                DrawTimesDetailsVO drawTimesDetails = activitySpinwinLuckyDrawTimesMapper.selectDrawTimes(param);

                // 初始化变量
                Integer surplusDrawTimes = drawTimesDetails != null ? drawTimesDetails.getSurplusDrawTimes() : totalSweepstakesLimit;
                Integer numSweepstakesToday = drawTimesDetails != null ? drawTimesDetails.getNumSweepstakesToday() + 1 : 1;
                Integer winningTimes = drawTimesDetails != null ? drawTimesDetails.getWinningTimes() : 0;
                Integer numberOfDailyLottery = drawTimesDetails != null ? drawTimesDetails.getNumberOfDailyLottery() : 0;

                // 检查参与人数上限
                if (totalSweepstakesIsLimit.equals(2) && partakeNum >= partakeNumLimit) {
                    return ResponseResult.fail(0, "活动人数已超上限");
                }

                // 检查抽奖次数是否已达上限
                if (totalSweepstakesIsLimit.equals(2) && surplusDrawTimes <= 0) {
                    return ResponseResult.fail(0, "抽奖次数已达上限");
                }

                // 检查今日抽奖次数是否已达上限
                if (winningTimesPerPerson < numSweepstakesToday) {
                    return ResponseResult.fail(0, "今日抽奖次数已达上限");
                }

                // 更新剩余抽奖次数
                surplusDrawTimes--;

                // 查询可用的礼品
                List<GiftDTO> gifts = this.baseMapper.selectActivityGift(param);
                if (CollectionUtils.isEmpty(gifts)) {
                    return ResponseResult.fail(0, "暂无可用礼品");
                }

                // 进行抽奖
                Integer orignalIndex = LotteryUtils.lottery(gifts);
                GiftDTO giftDTO = gifts.get(orignalIndex);
                Integer prizeId = giftDTO.getPrizeId();
                String prizeName = giftDTO.getPrizeName();
                param.setPrizeId(prizeId);

                // 更新抽奖记录中的中奖数量
                Integer drawCount = activitySpinwinPersonMapper.selectPrizeRecordCount(param);
                if (drawCount == 0) {
                    partakeNum++;
                }

                SpinwinLuckyDrawVO luckyDraw = new SpinwinLuckyDrawVO();
                // 处理中奖情况
                if (giftDTO.getIsWin().equals(1)) {
                    winningTimes++;
                    if (giftDTO.getGiftIsRedpacket().equals(1)) {
                        Map<String, String> paraMap = new HashMap<>();
                        paraMap.put("redpacketTId", giftDTO.getRedpacketTId().toString());
                        paraMap.put("redpacketId", "");
                        Integer redpacketId = grantRedpacket("spinwin",
                                giftDTO.getRedpacketTId(), paraMap);
                        luckyDraw.setRedpacketId(redpacketId);
                    }
                }

                // 创建并设置抽奖记录对象
                createLuckyDrawVO(userInfo, prizeId, luckyDraw, numSweepstakesToday, surplusDrawTimes, numberOfDailyLottery, winningTimes, partakeNum, param.getActivityId());
                activitySpinwinPersonMapper.insertLuckyDraw(luckyDraw);

                // 插入或更新抽奖记录
                if (drawTimesDetails == null) {
                    luckyDraw.setLuckyDrawTimes(totalSweepstakesIsLimit);
                    activitySpinwinLuckyDrawTimesMapper.insertDrawTimes2(luckyDraw);
                } else {
                    luckyDraw.setLuckyDrawTimes(drawTimesDetails.getLuckyDrawTimes() - 1);
                    activitySpinwinLuckyDrawTimesMapper.updateDrawTimes(luckyDraw);
                }

                // 更新奖品数量
                activitySpinwinPrizeMapper.updateGiveOutGiftNum(param);

                // 更新参与人数
                this.baseMapper.updateActivity(luckyDraw);

                // 构建抽奖结果返回信息
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("prizeId", prizeId);
                resultMap.put("prizeName", prizeName);
                return ResponseResult.success(resultMap);
            }
            return ResponseResult.fail(0, "请勿多次连续点击，稍后再试");
        } catch (Exception e) {
            // 记录异常日志
            log.error("抽奖过程中发生异常", e);
            throw new DefinedException(0, "系统繁忙，请稍后再试");
        } finally {
            // 释放分布式锁。
            if(redisLock != null) {
                redisLock.unLock();
            }
        }
    }

    private void createLuckyDrawVO(UserInfoVO userInfo, Integer prizeId, SpinwinLuckyDrawVO luckyDraw, Integer numSweepstakesToday, Integer surplusDrawTimes, Integer numberOfDailyLottery, Integer winningTimes, Integer partakeNum, Integer activityId) {
        luckyDraw.setActivityId(activityId);
        luckyDraw.setPrizeId(prizeId);
        luckyDraw.setUpdateDate(new Date());
        luckyDraw.setUserId(userInfo.getUserId());
        luckyDraw.setUserAvatar(userInfo.getUserAvatar());
        luckyDraw.setUserName(null == userInfo.getUserName() ? userInfo.getUserNickname() : userInfo.getUserName());
        luckyDraw.setNumSweepstakesToday(numSweepstakesToday);
        luckyDraw.setSurplusDrawTimes(surplusDrawTimes);
        luckyDraw.setNumberOfDailyLottery(numberOfDailyLottery);
        luckyDraw.setWinningTimes(winningTimes);
        luckyDraw.setPartakeNum(partakeNum);
    }


}




