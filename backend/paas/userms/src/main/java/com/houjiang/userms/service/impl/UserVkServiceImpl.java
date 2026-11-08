package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.user.UserVkVO;
import com.houjiang.userms.dto.VkAddUserDTO;
import com.houjiang.userms.dto.VkGiftOrderDTO;
import com.houjiang.userms.entity.*;
import com.houjiang.userms.feign.MerchantServiceClient;
import com.houjiang.userms.mapper.*;
import com.houjiang.userms.service.UserVkService;
import com.houjiang.userms.vo.VkCommissionVO;
import com.houjiang.userms.vo.VkCustomerInfoVO;
import com.houjiang.userms.vo.VkTgUserListVO;
import com.houjiang.userms.vo.VkUserListVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @implSpec 店铺微客会员表
 * @author Amos
 * @date  2023-06-05 11:00:52
 * @version  1.0
 **/
@Service
public class UserVkServiceImpl extends ServiceImpl<UserVkMapper, UserVkEntity>
        implements UserVkService {

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserVkSetMapper userVkSetMapper;

    @Autowired
    private UserInviterMapper userInviterMapper;

    @Autowired
    private UserInfoDetailMapper userInfoDetailMapper;

    /**
     * 根据用户ID查询并更新用户邀请者信息
     * 此方法主要功能是查询用户邀请者的信息，并根据查询结果更新UserInviterEntity对象的状态和相关信息
     * 它首先尝试查询UserInviterEntity，如果查询到结果，则更新其VK标志和其他信息
     * 如果没有查询到UserInviterEntity，则创建一个新的UserInviterEntity对象，并设置其VK标志为false
     * 不管是否查询到UserInviterEntity，都会查询UserInfoEntity，并用查询到的信息更新UserInviterEntity对象的用户名和手机号
     *
     * @param paraMap 包含用户ID的参数映射，用户ID键名为"userId"
     * @return 返回更新后的UserInviterEntity对象
     */
    @Override
    public UserInviterEntity queryVKUserInfo(Map<String, String> paraMap) {
        // 根据用户ID查询UserInviterEntity
        UserInviterEntity entity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                .eq(UserInviterEntity::getUserId, paraMap.get("userId")));
        // 如果查询到结果，更新entity的相关信息
        if (entity != null) {
            entity.setUserVkFlag(true);
            // 更新一级邀请者的VK标志和信息
            if (!StringUtils.isEmpty(entity.getOneInviter())) {
                UserVkEntity oneEntity = baseMapper.selectOne(new LambdaQueryWrapper<UserVkEntity>()
                        .eq(UserVkEntity::getUserId, entity.getOneInviter()));
                entity.setOneVkFlag(oneEntity != null);
                if (oneEntity != null) {
                    entity.setOneUserName(oneEntity.getUserName());
                    entity.setOneUserMobile(oneEntity.getMobile());
                } else {
                    entity.setOneUserName("");
                    entity.setOneUserMobile("");
                }
            } else {
                entity.setOneVkFlag(false);
                entity.setOneUserName("");
                entity.setOneUserMobile("");
            }
            // 更新二级邀请者的VK标志和信息
            if (!StringUtils.isEmpty(entity.getTwoInviter())) {
                UserVkEntity twoEntity = baseMapper.selectOne(new LambdaQueryWrapper<UserVkEntity>()
                        .eq(UserVkEntity::getUserId, entity.getTwoInviter()));
                entity.setTwoVkFlag(twoEntity != null);
                if (twoEntity != null) {
                    entity.setTwoUserName(twoEntity.getUserName());
                    entity.setTwoUserMobile(twoEntity.getMobile());
                } else {
                    entity.setTwoUserName("");
                    entity.setTwoUserMobile("");
                }
            } else {
                entity.setTwoVkFlag(false);
                entity.setTwoUserName("");
                entity.setTwoUserMobile("");
            }
        } else {
            // 如果没有查询到UserInviterEntity，创建一个新的实体，设置VK标志为false
            entity = new UserInviterEntity();
            entity.setUserVkFlag(false);
            entity.setUserId(paraMap.get("userId"));
        }
        // 根据用户ID查询UserInfoEntity，用于更新UserInviterEntity的用户名和手机号
        UserInfoEntity ui = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                .eq(UserInfoEntity::getUserId, paraMap.get("userId")));
        entity.setUserName(ui.getUserName());
        entity.setUserMobile(ui.getUserMobile());
        // 返回更新后的UserInviterEntity对象
        return entity;
    }


    /**
     * 根据传入的参数获取用户信息。
     * <p>
     * 该方法通过解析token获取用户信息，并进一步查询该用户的微客信息（如果存在）。
     * 主要用于处理用户信息查询请求，提供给前端用于展示或进一步处理。
     *
     * @param paraMap 包含请求参数的映射表，其中必须包含token参数。
     * @return 返回包含用户信息的ResponseResult对象，如果查询成功，则用户信息包含在成功结果中。
     */
    @Override
    public ResponseResult getAskUserInfo(Map<String, String> paraMap) {
        // 从参数映射中获取token
        String token = paraMap.get("token");
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 根据用户ID查询该用户的微客信息 查询出我的微客信息，拿到我的邀请人ID
        paraMap.put("userId", userInfo.getUserId());
        UserInviterEntity entity = queryVKUserInfo(paraMap);
        if (entity != null && !StringUtils.isBlank(entity.getOneInviter())) {
            UserInfoDetaileEntity detail = userInfoDetailMapper.selectOne(new LambdaQueryWrapper<UserInfoDetaileEntity>()
                    .eq(UserInfoDetaileEntity::getUserId, entity.getOneInviter()));
            entity.setOneUserAvatar(detail.getUserAvatar());
        }
        // 构建成功响应结果，并返回用户信息
        return ResponseResult.success(entity);
    }

    /**
     * 添加微客用户
     * 该方法负责将用户信息添加到微客系统中，并处理相关的绑定逻辑
     * 使用了事务注解确保在发生异常时可以回滚
     *
     * @param param 包含待添加用户信息的DTO对象
     * @return 响应结果对象，包含操作是否成功的状态码和消息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseResult addVkUser(VkAddUserDTO param) {
        // 查询微客设置
        UserVkSetEntity result = userVkSetMapper.selectOne(new LambdaQueryWrapper<>());
        // 微客推广是否开启
        if (result.getVkState() != 1) {
            return ResponseResult.fail(BizCodeEnume.VK_CLOSE);
        }
        // 查询是否绑定微客
        UserVkEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<UserVkEntity>().eq(UserVkEntity::getUserId, param.getUserId()));
        if (entity != null) {
            return ResponseResult.fail(BizCodeEnume.USER_IS_VK);
        }
        // 判断分享人是不是微客
        if (!StringUtils.isEmpty(param.getShareUserId())) {
            UserVkEntity vkEntity = baseMapper.selectOne(new LambdaQueryWrapper<UserVkEntity>().eq(UserVkEntity::getUserId, param.getShareUserId()));
            if (vkEntity == null) {
                return ResponseResult.fail(BizCodeEnume.INVITER_NO_VK_USER);
            }
        }
        // 根据微客设置和用户来源决定是否添加邀请关系
        if (result.getApplyType() == 1 || param.getSource() == 1) {
            // 添加绑定关系
            addUserInviter(param);
        }
        // 准备插入的用户实体
        entity = new UserVkEntity();
        BeanUtils.copyProperties(param, entity);
        entity.setTgNum(0);
        entity.setCreateTime(DateUtils.getNowDateToDf());
        // 插入微客用户数据
        int count = this.baseMapper.insert(entity);
        if (count > 0) {
            // 获取用户信息
            UserInfoVO userInfo = userInfoMapper.getUserInfoByUserId(param.getUserId());
            // 添加微客店铺基础信息
            ResponseResult rr = merchantServiceClient.insertVkShopBase(userInfo);
            if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR);
            }
            // 返回操作成功的结果
            return ResponseResult.success();
        } else {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR);
        }
    }


    /**
     * 添加用户邀请者
     *
     * @param param 包含用户ID和分享用户ID等信息的DTO
     * @return 操作结果，成功或失败
     */
    @Override
    public ResponseResult addUserInviter(VkAddUserDTO param) {
        // 检查用户邀请者是否已存在
        UserInviterEntity entity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                .eq(UserInviterEntity::getUserId, param.getUserId()));
        if (entity != null) {
            // 如果已存在邀请者，则返回失败
            return ResponseResult.fail(BizCodeEnume.INVITER_IS_EXIST);
        }

        // 创建新的用户邀请者实体
        UserInviterEntity userInviterEntity = new UserInviterEntity();
        userInviterEntity.setUserId(param.getUserId());
        userInviterEntity.setOneInviter("");
        userInviterEntity.setTwoInviter("");

        // 根据来源判断用户的上级关系
        if (param.getSource() == 1) {
            userInviterEntity.setIsSuperior(0);
        } else {
            // 如果分享用户ID不为空，查询分享用户的邀请者信息
            if (!StringUtils.isEmpty(param.getShareUserId())) {
                UserInviterEntity vkEntity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                        .eq(UserInviterEntity::getUserId, param.getShareUserId()));
                if (vkEntity == null) {
                    // 如果分享用户没有邀请者信息，则设置为空字符串
                    userInviterEntity.setOneInviter("");
                    userInviterEntity.setTwoInviter("");
                } else {
                    // 如果分享用户有邀请者信息，则设置为一级和二级邀请者
                    userInviterEntity.setOneInviter(param.getShareUserId());
                    userInviterEntity.setTwoInviter(vkEntity.getOneInviter());
                }
                // 设置用户为有上级，并记录创建时间
                userInviterEntity.setIsSuperior(1);
                userInviterEntity.setCreateTime(DateUtils.getNowDateToDf());

                // 更新邀请者和分享用户的TG数量
                List<String> userIdList = Arrays.asList(param.getShareUserId(), userInviterEntity.getOneInviter());
                addTgNum(userIdList);
            } else {
                // 如果分享用户ID为空，则设置用户为无上级
                userInviterEntity.setIsSuperior(0);
            }
        }

        // 插入用户邀请者信息到数据库
        userInviterMapper.insert(userInviterEntity);
        // 返回操作成功
        return ResponseResult.success();
    }

    /**
     * 同步增加用户ID列表对应的推广数量
     *
     * @param userIdList 用户ID列表，可能包含空或空字符串的元素。经过筛选后，只处理非空的用户ID。
     */
    private synchronized void addTgNum(List<String> userIdList) {
        // 筛选出非空的用户ID，避免处理无效或空的ID。
        userIdList = userIdList.stream().filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toList());
        // 调用底层Mapper接口，增加指定用户ID列表的推广数量
        if (!userIdList.isEmpty()) {
            baseMapper.addVkTgNumByList(userIdList);
        }
    }


    /**
     * 同步减少用户ID列表对应的推广数量。
     *
     * @param userIdList 用户ID列表，会首先筛选出非空的用户ID列表，以确保只有有效的ID被处理。
     */
    private synchronized void subTgNum(List<String> userIdList) {
        // 筛选出非空的用户ID，避免处理无效或空的ID。
        userIdList = userIdList.stream().filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toList());
        // 调用baseMapper的subVkTgNumByList方法，减少指定用户ID列表的推广数量。
        if (!userIdList.isEmpty()) {
            baseMapper.subVkTgNumByList(userIdList);
        }
    }

    /**
     * 将Vk用户添加到平台
     * 此方法首先检查用户是否已通过手机号码注册，如果已注册，则将Vk用户信息添加到系统中，否则返回用户不存在的错误
     * 使用了事务注解，当出现异常时回滚，确保数据的一致性
     *
     * @param param 包含需要添加到平台的Vk用户的手机号码和其他相关信息
     * @return 如果用户已存在，返回添加Vk信息的结果；如果用户不存在，返回失败的结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseResult addVkUserToPlatform(VkAddUserDTO param) {
        // 根据手机号码查询数据库，以检查用户是否已注册
        UserInfoEntity entity = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                .eq(UserInfoEntity::getUserMobile, param.getMobile())
                .isNull(UserInfoEntity::getShopId));
        // 如果查询到用户实体，则表示用户已注册，准备添加Vk用户信息
        if (entity != null) {
            VkAddUserDTO dto = new VkAddUserDTO();
            dto.setUserId(entity.getUserId());
            dto.setUserName(entity.getUserName());
            dto.setMobile(entity.getUserMobile());
            dto.setSource(1);
            // 添加微客信息
            return addVkUser(dto);
        }
        // 如果没有查询到用户实体，表示用户不存在，返回用户不存在的错误信息
        return ResponseResult.fail(BizCodeEnume.PLATFORM_NOT_REGISTERED);
    }


    /**
     * 更新微客用户的上级信息。
     *
     * @param param 包含用户ID和是否为上级等信息的DTO对象。
     * @return 返回操作结果，成功或失败。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseResult updateVkUserSuperior(VkAddUserDTO param) {
        // 根据用户ID查询用户邀请者信息
        UserInviterEntity userEntity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                .eq(UserInviterEntity::getUserId, param.getUserId()));

        // 判断是否设置为非上级且未填写手机号
        boolean flag = param.getIsSuperior() == 0 && StringUtils.isEmpty(param.getMobile());
        if (flag) {
            // 如果是，则清空用户的邀请人信息，并更新数据库
            List<String> subUserIdList = Arrays.asList(userEntity.getOneInviter(), userEntity.getTwoInviter());
            subTgNum(subUserIdList);
            UserInviterEntity userInviterEntity = new UserInviterEntity();
            userInviterEntity.setUserId(param.getUserId());
            userInviterEntity.setIsSuperior(param.getIsSuperior());
            userInviterEntity.setOneInviter("");
            userInviterEntity.setTwoInviter("");
            userInviterMapper.update(userInviterEntity, new LambdaUpdateWrapper<UserInviterEntity>()
                    .eq(UserInviterEntity::getUserId, param.getUserId()));
            return ResponseResult.success();
        } else {
            // 根据手机号查询用户信息
            UserInfoEntity entity = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                    .eq(UserInfoEntity::getUserMobile, param.getMobile())
                    .isNull(UserInfoEntity::getShopId));
            if (entity != null) {
                // 检查该用户是否已作为下级存在，是则返回错误信息
                if (userEntity.getOneInviter() != null && userEntity.getOneInviter().equals(entity.getUserId())) {
                    return ResponseResult.fail(BizCodeEnume.VK_USER_REPEAT);
                }
                UserInviterEntity mobileInviterEntity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                        .eq(UserInviterEntity::getUserId, entity.getUserId()));
                // 查询该用户是否已绑定微客
                UserVkEntity ckEntity = baseMapper.selectOne(new LambdaQueryWrapper<UserVkEntity>().eq(UserVkEntity::getUserId, entity.getUserId()));
                if (ckEntity == null) {
                    return ResponseResult.fail(BizCodeEnume.NO_VK_USER);
                }
                // 减少相应用户的推广人计数
                List<String> subUserIdList = Arrays.asList(userEntity.getOneInviter(), userEntity.getTwoInviter());
                subTgNum(subUserIdList);
                // 更新用户的邀请人信息
                UserInviterEntity userInviterEntity = new UserInviterEntity();
                userInviterEntity.setUserId(param.getUserId());
                userInviterEntity.setIsSuperior(param.getIsSuperior());
                if (param.getIsSuperior() == 1) {
                    // 如果设置为上级，则记录该用户的ID，并增加推广人计数
                    userInviterEntity.setOneInviter(entity.getUserId());
                    userInviterEntity.setTwoInviter(mobileInviterEntity.getOneInviter());
                    List<String> addUserIdList = Arrays.asList(entity.getUserId(), mobileInviterEntity.getOneInviter());
                    addTgNum(addUserIdList);
                }
                userInviterMapper.update(userInviterEntity, new LambdaUpdateWrapper<UserInviterEntity>()
                        .eq(UserInviterEntity::getUserId, param.getUserId()));
                return ResponseResult.success();
            }
            // 如果用户不存在，则返回错误信息
            return ResponseResult.fail(BizCodeEnume.PLATFORM_NOT_REGISTERED);
        }
    }


    @Override
    public void delUserVk(String userId) {
        UserInviterEntity userEntity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                .eq(UserInviterEntity::getUserId, userId));
        List<String> subUserIdList = Arrays.asList(userEntity.getOneInviter(), userEntity.getTwoInviter());
        subTgNum(subUserIdList);
        userInviterMapper.delete(new LambdaQueryWrapper<UserInviterEntity>().eq(UserInviterEntity::getUserId, userId));
    }

    /**
     * 根据参数查询VK用户列表，并包含用户的结算订单数量、一级和二级佣金总额以及商品数量。
     *
     * @param param 查询参数，包含分页信息和过滤条件。
     * @return 返回查询结果，包含分页信息和用户列表。
     */
    @Override
    public ResponseResult selectVkUserList(VkAddUserDTO param) {
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<VkUserListVO> pageObj = new Page<>(page, limit);
        // 执行查询，返回带分页信息的结果集
        IPage<VkUserListVO> result = baseMapper.selectVkUserList(pageObj, param);

        // 遍历查询结果，为每个用户补充结算订单数、佣金总额和商品数
        for (VkUserListVO vo : result.getRecords()) {
            // 根据用户ID查询用户的结算订单数、一级和二级佣金总额
            VkUserListVO vkUserListVO = baseMapper.selectVkUserOrder(vo.getUserId());
            // 补充数据到用户信息
            vo.setTotalSettlementOrderNum(vkUserListVO.getTotalSettlementOrderNum());
            vo.setTotalOneTgCommission(vkUserListVO.getTotalOneTgCommission());
            vo.setTotalTwoTgCommission(vkUserListVO.getTotalTwoTgCommission());
            // 根据用户ID查询用户的商品数量
            Integer count = baseMapper.selectVkOrderGoodsCount(vo.getUserId());
            // 补充商品数量到用户信息
            vo.setTotalTgGoodsNum(count);
        }

        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(result));
    }

    @Override
    public ResponseResult selectVkUserListExp(VkAddUserDTO param) {
        String[] title = {"微客昵称", "微客手机号", "是否存在上级", "直接邀请人", "间接邀请人", "累计推广用户数", "累计结算订单数", "累计推广商品数", "一级推广佣金", "二级级推广佣金", "分享佣金", "佣金总额", "已提现佣金", "未提现佣金", "成为微客时间"};
        List<Map<String, String>> list = Lists.newArrayList();
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<VkUserListVO> pageObj = new Page<>(page, limit);
        // 执行查询，返回带分页信息的结果集
        IPage<VkUserListVO> result = baseMapper.selectVkUserList(pageObj, param);

        // 遍历查询结果，为每个用户补充结算订单数、佣金总额和商品数
        for (VkUserListVO vo : result.getRecords()) {
            // 根据用户ID查询用户的结算订单数、一级和二级佣金总额
            VkUserListVO vkUserListVO = baseMapper.selectVkUserOrder(vo.getUserId());
            // 根据用户ID查询用户的商品数量
            Integer count = baseMapper.selectVkOrderGoodsCount(vo.getUserId());
            // 构建微客信息的Map，用于Excel数据填充
            HashMap<String, String> map = getStringStringHashMap(vo, vkUserListVO, count);

            list.add(map);
        }
        // 构建Excel数据结构
        Map<String, List<Map<String, String>>> map = Maps
                .newHashMap();
        map.put("微客管理用户列表.xls", list);
        String url = ExcelsUtil.createExcel(title, map, new int[]{0});
        // 返回处理后的查询结果
        return ResponseResult.success(url);
    }

    /**
     * 构建一个HashMap，包含微客用户的详细信息和推广统计数据
     *
     * @param vo 微客用户列表的视图对象，包含用户信息和部分统计数据
     * @param vkUserListVO 另一个微客用户列表的视图对象，包含更多统计数据
     * @param count 累计推广商品数
     * @return 返回一个HashMap，键为信息类别，值为对应的微客用户信息和统计数据
     */
    private static HashMap<String, String> getStringStringHashMap(VkUserListVO vo, VkUserListVO vkUserListVO, Integer count) {
        // 初始化HashMap来存储信息
        HashMap<String, String> map = Maps.newHashMap();
        // 存储微客昵称
        map.put("微客昵称", vo.getUserName());
        // 存储微客手机号
        map.put("微客手机号", vo.getUserMobile());
        // 根据vo.getIsSuperior()的值判断是否存在上级，存储相应信息
        map.put("是否存在上级", vo.getIsSuperior() == 0 ? "否" : "是");
        // 存储直接邀请人的信息
        map.put("直接邀请人", vo.getOneInviter());
        // 存储间接邀请人的信息
        map.put("间接邀请人", vo.getTwoInviter());
        // 存储累计推广用户数
        map.put("累计推广用户数", vo.getTotalTgUserNum() + "");
        // 存储累计结算订单数
        map.put("累计结算订单数", (vkUserListVO.getTotalSettlementOrderNum() != null ? vkUserListVO.getTotalSettlementOrderNum() : "0") + "");
        // 存储累计推广商品数
        map.put("累计推广商品数", (count != null ? count : "0") + "");
        // 存储一级推广佣金
        map.put("一级推广佣金", (vkUserListVO.getTotalOneTgCommission() != null ? vkUserListVO.getTotalOneTgCommission() : "0") + "");
        // 存储二级推广佣金
        map.put("二级级推广佣金", (vkUserListVO.getTotalTwoTgCommission() != null ? vkUserListVO.getTotalTwoTgCommission() : "0") + "");
        // 存储分享佣金
        map.put("分享佣金", (vo.getTotalShareCommission() != null ? vo.getTotalShareCommission() : "0") + "");
        // 存储佣金总额
        map.put("佣金总额", (vo.getTotalCommission() != null ? vo.getTotalCommission() : "0") + "");
        // 存储已提现佣金
        map.put("已提现佣金", (vo.getWithdrawnCommission() != null ? vo.getWithdrawnCommission() : "0") + "");
        // 存储未提现佣金
        map.put("未提现佣金", (vo.getPendingWithdrawnCommission() != null ? vo.getPendingWithdrawnCommission() : "0") + "");
        // 存储成为微客的时间
        map.put("成为微客时间", DateUtils.formateForSdfCn(vo.getCreateTime()));
        // 返回填充好的HashMap
        return map;
    }


    /**
     * 设置VK信息
     * 使用事务控制，确保在遇到异常时能够回滚
     * @param orderId 订单ID
     * @return 操作结果
     */
    @Override
    public ResponseResult setVKInfo(String orderId) {
        try {
            // 查询微客设置
            UserVkSetEntity result = userVkSetMapper.selectOne(new LambdaQueryWrapper<>());
            // 检查微客推广是否开启
            if (result.getVkState() != 1) {
                return ResponseResult.fail(BizCodeEnume.VK_CLOSE);
            }
            // 查询订单用户信息
            VkGiftOrderDTO orderDTO = baseMapper.queryUserInfoByOrderId(orderId);
            // 查询礼物订单数量
            Integer count = baseMapper.selectGiftOrderCount(orderDTO.getUserId());
            // 如果订单不是礼物订单，则根据微客推广设置处理
            if (count <= 1) {
                // 检查是否开启注册成功+购买礼包商品
                if (result.getApplyType() == 2) {
                    // 如果是礼物订单，则添加微客用户
                    if (orderDTO.getIsGiftOrder() == 1) {
                        VkAddUserDTO vkAddUserDTO = setVKUserInfo(orderDTO);
                        addVkUser(vkAddUserDTO);
                        // 查询用户邀请信息
                        UserInviterEntity entity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                                .eq(UserInviterEntity::getUserId, orderDTO.getUserId()));
                        // 如果邀请信息存在且一级邀请人不为空，则给一级邀请人添加固定奖励
                        if (entity != null && !StringUtils.isEmpty(entity.getOneInviter())) {
                            UserVkSetEntity vkSet = userVkSetMapper.selectOne(new LambdaQueryWrapper<>());
                            BigDecimal fixedRewards = vkSet.getFixedRewards();
                            baseMapper.addBalance(entity.getOneInviter(), fixedRewards);
                            baseMapper.addPayAssetDetails(14, 1, fixedRewards, "微客固定奖励", orderId, entity.getOneInviter(), 1, "微客固定奖励");
                        } else if (entity == null) {
                            addUserInviter(vkAddUserDTO);
                        }
                    }
                } else {
                    return ResponseResult.success();
                }
            } else {
                // 检查是否开启注册成功+购买礼包商品
                if (result.getApplyType() == 2) {
                    // 如果是礼物订单，则添加微客用户
                    if (orderDTO.getIsGiftOrder() == 1) {
                        VkAddUserDTO vkAddUserDTO = setVKUserInfo(orderDTO);
                        addVkUser(vkAddUserDTO);
                        // 查询用户邀请信息
                        UserInviterEntity entity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                                .eq(UserInviterEntity::getUserId, orderDTO.getUserId()));
                        if (entity == null) {
                            addUserInviter(vkAddUserDTO);
                        }
                    }
                } else {
                    return ResponseResult.success();
                }
            }
            return ResponseResult.success();
        }catch (Exception e) {
            // 如果发生异常，则回
            e.printStackTrace();
            return ResponseResult.fail();
        }
    }

    /**
     * 根据订单信息设置VK用户信息
     * <p>
     * 此方法接收一个订单数据传输对象（DTO），从中提取出必要的用户信息，
     * 并将其设置到一个新的VK用户DTO对象中此方法主要用于简化和标准化用户信息的处理过程
     *
     * @param orderDTO 包含订单信息的数据传输对象，包括用户ID、用户手机号、用户名等
     * @return 返回填充了用户信息的VkAddUserDTO对象
     */
    private VkAddUserDTO setVKUserInfo(VkGiftOrderDTO orderDTO) {
        // 创建一个新的VkAddUserDTO对象用于存储用户信息
        VkAddUserDTO vkAddUserDTO = new VkAddUserDTO();
        // 设置用户ID
        vkAddUserDTO.setUserId(orderDTO.getUserId());
        // 设置用户手机号
        vkAddUserDTO.setMobile(orderDTO.getUserMobile());
        // 设置用户名
        vkAddUserDTO.setUserName(orderDTO.getUserName());
        // 设置用户来源，此处的2代表具体的来源类型（例如VK平台）
        vkAddUserDTO.setSource(2);
        // 返回填充了用户信息的VkAddUserDTO对象
        return vkAddUserDTO;
    }


    /**
     * 根据指定的参数分页查询Vk平台的用户信息
     * 此方法主要负责调用底层数据访问层的方法来获取Vk用户的数据，并对每个用户的相关数据进行补充查询
     *
     * @param param 查询参数对象，包含页码和每页数量等分页信息
     * @return 包含查询结果的响应对象
     */
    @Override
    public ResponseResult selectVkTotalTgUser(VkAddUserDTO param) {
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 创建分页对象，初始化为请求的页码和每页数量
        Page<VkTgUserListVO> pageObj = new Page<>(page, limit);
        // 调用底层数据访问层的方法执行分页查询，并获取结果
        IPage<VkTgUserListVO> result = baseMapper.selectVkTotalTgUser(pageObj, param);

        // 遍历查询结果的每条记录，补充查询每个用户的分销订单数量和分销佣金总额
        for (VkTgUserListVO vo : result.getRecords()) {
            // 查询用户的分销订单数量，并处理可能的空值情况
            Integer fxOrderNum = baseMapper.selectFxOrderNum(vo.getUserId());
            vo.setFxOrderNum(fxOrderNum != null ? fxOrderNum : 0);

            // 查询用户的分销佣金总额，并处理可能的空值情况
            BigDecimal fxCommission = baseMapper.selectFxOrderAmount(vo.getUserId());
            vo.setFxCommissionNum(fxCommission != null ? fxCommission : BigDecimal.ZERO);
        }

        // 将分页查询结果包装成成功响应并返回
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 根据用户Token查询Vk分销信息
     *
     * @param param 包含用户Token的参数对象
     * @return 包含Vk分销信息的响应结果
     */
    @Override
    public ResponseResult selectVkDistributionInfo(VkAddUserDTO param) {
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (userInfo != null) {
            // 获取用户信息
            UserVkEntity userVk = baseMapper.selectOne(new LambdaQueryWrapper<UserVkEntity>()
                    .eq(UserVkEntity::getUserId, userInfo.getUserId()));
            if (userVk != null) {
                // 查询用户详情
                UserInfoDetaileEntity detail = userInfoDetailMapper.selectOne(new LambdaQueryWrapper<UserInfoDetaileEntity>()
                        .eq(UserInfoDetaileEntity::getUserId, userInfo.getUserId()));
                VkCommissionVO vkCommissionVO = new VkCommissionVO();
                // 设置用户基础信息
                vkCommissionVO.setUserId(userVk.getUserId());
                vkCommissionVO.setUserName(userVk.getUserName());
                vkCommissionVO.setUserAvatar(detail.getUserAvatar());
                // 获取可提现佣金
                vkCommissionVO.setCanWithdrawCommission(new BigDecimal(userVk.getPendingWithdrawnCommission()));
                // 获取累计提现佣金
                vkCommissionVO.setTotalWithdraw(new BigDecimal(userVk.getWithdrawnCommission()));
                // 获取累计结算佣金
                BigDecimal totalSettlement = new BigDecimal(userVk.getTotalShareCommission()).add(new BigDecimal(userVk.getTotalTgCommission())).setScale(2, RoundingMode.HALF_DOWN);
                vkCommissionVO.setTotalSettlement(totalSettlement);
                // 今日结算佣金
                BigDecimal todaySettlement = baseMapper.selectTodaySettlement(userInfo.getUserId());
                vkCommissionVO.setTodaySettlement(todaySettlement == null ? BigDecimal.ZERO : todaySettlement);
                // 今日预估佣金
                BigDecimal todayEstimatedIncome = baseMapper.selectTodayEstimatedIncome(userInfo.getUserId());
                vkCommissionVO.setTodayEstimatedIncome(todayEstimatedIncome == null ? BigDecimal.ZERO : todayEstimatedIncome);
                // 累计预估佣金
                BigDecimal totalEstimatedIncome = baseMapper.selectTotalEstimatedIncome(userInfo.getUserId());
                vkCommissionVO.setTotalEstimatedIncome(totalEstimatedIncome == null ? BigDecimal.ZERO : totalEstimatedIncome);
                return ResponseResult.success(vkCommissionVO);
            } else {
                return ResponseResult.fail(BizCodeEnume.USER_NOT_VK);
            }
        }
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    /**
     * 根据用户token查询相应的客户信息。
     *
     * @param param 包含用户token和其他查询参数的DTO。
     * @return 返回查询结果，如果用户不存在，则返回失败响应。
     */
    @Override
    public ResponseResult selectVkCustomerInfo(VkAddUserDTO param) {
        // 解析用户token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (userInfo != null) {
            // 设置查询参数中的用户ID，用于后续的查询过滤
            param.setUserId(userInfo.getUserId());
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            // 创建分页对象
            Page<VkCustomerInfoVO> pageObj = new Page<>(page, limit);
            // 执行查询
            IPage<VkCustomerInfoVO> result = baseMapper.selectVkCustomerInfo(pageObj, param);
            // 遍历查询结果，补充每个客户的订单信息
            for (VkCustomerInfoVO vo : result.getRecords()) {
                vo.setTgNum(vo.getTgNum() == null ? 0 : vo.getTgNum());
                // 根据客户ID和当前用户ID查询客户的订单信息
                VkCustomerInfoVO infoVO = baseMapper.selectVkOrderInfo(vo.getUserId(), userInfo.getUserId());
                // 更新客户信息，包括订单数量和订单金额
                if (infoVO != null) {
                    vo.setFxOrderNum(infoVO.getFxOrderNum());
                    vo.setFxMoney(infoVO.getFxMoney());
                } else {
                    vo.setFxOrderNum(0);
                    vo.setFxMoney(BigDecimal.ZERO);
                }
            }
            // 返回查询结果，包含分页信息
            return ResponseResult.success(PageUtils.getPage(result));
        }
        // 如果用户信息不存在，则返回失败响应
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    /**
     * 根据用户令牌查询微客用户的佣金信息。
     *
     * @param param 包含用户令牌的DTO对象。
     * @return 如果用户存在，返回佣金信息的成功响应；如果用户不存在，返回用户不存在的失败响应。
     */
    @Override
    public ResponseResult selectVkCommissionInfo(VkAddUserDTO param) {
        // 通过令牌解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (userInfo != null) {
            // 初始化返回结果的映射
            Map<String, Object> result = new HashMap<>();
            // 查询用户对应的微客佣金信息
            UserVkEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<UserVkEntity>()
                    .eq(UserVkEntity::getUserId, userInfo.getUserId()));
            // 将待提取佣金和冻结佣金添加到结果映射中
            // 可提现佣金
            result.put("pendingWithdrawnCommission", entity.getPendingWithdrawnCommission());
            // 冻结佣金
            result.put("freezeCommission", baseMapper.selectVkFreezeCommission(userInfo.getUserId()));
            // 返回查询成功的响应结果，包含佣金信息
            return ResponseResult.success(result);
        }
        // 如果用户信息不存在，返回用户不存在的失败响应
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    /**
     * 根据提供的VK（VKontakte）用户信息查询用户是否存在
     * 此方法主要用于验证用户是否已经在系统中被邀请或注册
     * 它通过解析用户提供的token来获取用户信息，并检查该用户是否已被邀请
     *
     * @param param 包含用户token的信息对象，用于解析用户信息
     * @return 如果用户存在，则返回成功响应结果，否则返回失败响应结果，表明用户不存在
     */
    @Override
    public ResponseResult queryUserIsVk(VkAddUserDTO param) {
        // 解析token获取用户信息
        UserInfoVO vo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 检查是否成功获取用户信息
        if(vo != null){
            // 根据用户ID查询用户邀请记录
            UserInviterEntity entity = userInviterMapper.selectOne(new LambdaQueryWrapper<UserInviterEntity>()
                    .eq(UserInviterEntity::getUserId, vo.getUserId()));
            // 如果查询到用户邀请记录，则表示用户已存在，返回成功响应
            return ResponseResult.success(entity != null);
        }
        // 如果未获取到用户信息，返回失败响应，表明用户不存在
        return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
    }

    
    /**
     * 同步VK（社交平台）用户的邀请人数到TG（另一个社交平台）系统中
     * 该方法首先从数据库中检索所有VK用户的信息，然后对每个用户查询他们在邀请系统中的邀请次数，
     * 最后更新数据库，将邀请次数同步到VK用户的记录中
     *
     * @return 返回一个响应结果对象，表示操作是否成功
     */
    @Override
    public ResponseResult syncVkTgNum() {
        // 查询并获取所有VK用户实体列表
        List<UserVkEntity> list = baseMapper.selectList(new LambdaQueryWrapper<>());

        // 遍历VK用户列表，处理每个用户的邀请次数同步
        for (UserVkEntity vkEntity : list) {
            // 查询该用户邀请的人数，包括一级和二级邀请
            Integer count = Math.toIntExact(userInviterMapper.selectCount(new LambdaQueryWrapper<UserInviterEntity>()
                    .eq(UserInviterEntity::getOneInviter, vkEntity.getUserId())
                    .or()
                    .eq(UserInviterEntity::getTwoInviter, vkEntity.getUserId())));

            // 更新VK用户的邀请人数
            baseMapper.update(null, new LambdaUpdateWrapper<UserVkEntity>()
                    .set(UserVkEntity::getTgNum, count)
                    .eq(UserVkEntity::getUserId, vkEntity.getUserId()));
        }

        // 返回操作成功的结果
        return ResponseResult.success();
    }

    @Override
    public UserVkVO queryUserAccount(String userId) {
        UserVkVO vo=this.baseMapper.queryUserAccount(userId);
        return vo;
    }

    @Override
    public int subUserAccount(Map<String, String> paraMap) {
        int result=this.baseMapper.subUserAccount(paraMap);
        return result;
    }
}
