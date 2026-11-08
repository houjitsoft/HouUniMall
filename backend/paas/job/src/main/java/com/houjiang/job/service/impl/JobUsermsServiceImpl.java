package com.houjiang.job.service.impl;

import com.houjiang.job.feign.UserServiceClient;
import com.houjiang.job.service.JobUsermsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName JobUsermsServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/9/4 14:28
 * @Version 1.0
 * @author Amos
 */

@Service
public class JobUsermsServiceImpl implements JobUsermsService {
    @Autowired
    private UserServiceClient userServiceClient;
    /**
     * 实现了对用户服务的更新操作。
     * 调用 userServiceClient 的 updateplus 方法来执行更新操作。
     */
    @Override
    public void updateplus() {
        userServiceClient.updateplus();
    }

    /**
     * 实现了用户的红包发放功能。
     * 调用 userServiceClient 的 redgrant 方法来执行红包发放操作。
     */
    @Override
    public void redgrant() {
        userServiceClient.redgrant();
    }

    /**
     * 实现了对用户限额的更新功能。
     * 调用 userServiceClient 的 updatelimit 方法来更新用户的限额。
     */
    @Override
    public void updatelimit() {
        userServiceClient.updatelimit();
    }
}
