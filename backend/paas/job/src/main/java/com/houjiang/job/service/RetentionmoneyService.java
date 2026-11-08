package com.houjiang.job.service;

import java.text.ParseException;

/**
 * @ClassName RetentionmoneyService
 * @Description
 * @Author Amos
 * @Date 2023/9/4 11:44
 * @Version 1.0
 * @author Amos
 */

public interface RetentionmoneyService {

    /**
     * 提醒商家处理失效定时任务
     */
    void merchantprocessing();

    /**
     * 商家超时未处理投诉单
     */
    void merchantprocessingouttime();

    /**
     *更新商家是否需要缴纳质保金 定时任务
     */
    void updateshopmoney();


    /**
     * 质保金 限权店铺
     */
    void restrictedstore();


    /**
     * 更新店铺质保金基础额度
     * @throws ParseException
     */
    void updatebasicquota() throws ParseException;
}
