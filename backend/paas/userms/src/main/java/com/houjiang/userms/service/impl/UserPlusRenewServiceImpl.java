package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.userms.entity.UserPlusRenewEntity;
import com.houjiang.userms.mapper.UserPlusRenewMapper;
import com.houjiang.userms.service.UserPlusRenewService;
import org.springframework.stereotype.Service;

/**
 * 用户Plus续费服务实现类
 * 该类继承了ServiceImpl，实现了UserPlusRenewService接口，
 * 主要用于处理用户Plus续费的相关业务逻辑。
 * @author Amos
 */
@Service
public class UserPlusRenewServiceImpl extends ServiceImpl<UserPlusRenewMapper, UserPlusRenewEntity>
    implements UserPlusRenewService{

}
