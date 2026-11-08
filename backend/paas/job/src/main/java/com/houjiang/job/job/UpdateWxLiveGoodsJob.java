package com.houjiang.job.job;

import com.houjiang.job.mapper.WxLiveHostMapper;
import com.houjiang.job.utils.WxLiveUtil;
import com.houjiang.job.vo.QuWxGoodsListVO;
import com.houjiang.job.vo.QuWxGoodsVO;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @ClassName UpdateWxLiveGoodsJob
 * @Description 更新小程序直播商品状态
 * @Author Amos
 * @Date 2023/5/17 11:45
 * @Version 1.0
 * @author Amos
 */
public class UpdateWxLiveGoodsJob implements BaseJob {

    /**
     * 自动注入微信直播工具类，用于调用微信直播的相关接口。
     */
    @Autowired
    private WxLiveUtil wxLiveUtil;

    /**
     * 自动注入微信直播主持人Mapper，用于更新直播商品的审核状态。
     */
    @Autowired
    private WxLiveHostMapper wxLiveHostMapper;

    /**
     * 日志记录器，用于记录任务执行的日志信息。
     */
    private static final Logger logger = LoggerFactory.getLogger(UpdateWxLiveGoodsJob.class);

    /**
     * 定时任务的执行方法。
     * 该方法会分别查询已审核和未审核的微信直播商品列表，并更新它们在系统中的审核状态。
     *
     * @param jobExecutionContext 任务执行的上下文环境，包含任务执行所需的信息。
     * @throws JobExecutionException 如果任务执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            // 查询已审核的微信直播商品列表
            QuWxGoodsListVO quWxGoodsListResult = wxLiveUtil.queryWxLiveGoodsList("0", "2");
            List<QuWxGoodsVO> goodIds = quWxGoodsListResult.getGoods();
            // 更新已审核商品的审核状态为“微信审核通过”
            Integer integer = wxLiveHostMapper.upLiveGoodsiIsverify(1, goodIds, "微信审核通过");
            logger.info("小程序已审核列表更新完成");

            // 查询未审核的微信直播商品列表
            QuWxGoodsListVO goodsListResult = wxLiveUtil.queryWxLiveGoodsList("0", "3");
            List<QuWxGoodsVO> goods = goodsListResult.getGoods();
            // 更新未审核商品的审核状态为“微信审核不通过”
            Integer count = wxLiveHostMapper.upLiveGoodsiIsverify(3, goods, "微信审核不通过");
            logger.info("小程序审核不通过列表更新完成");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

