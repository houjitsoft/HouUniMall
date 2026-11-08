package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.PlusEquityDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import com.houjiang.userms.entity.UserPlusEquityEntity;
import com.houjiang.userms.feign.ActivityServiceClient;
import com.houjiang.userms.mapper.UserPlusEquityMapper;
import com.houjiang.userms.mapper.UserPlusInfoMapper;
import com.houjiang.userms.plugin.RedpacketQueryGateway;
import com.houjiang.userms.service.UserPlusEquityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户权益服务实现类
 * @author Amos
 */
@Service
public class UserPlusEquityServiceImpl extends ServiceImpl<UserPlusEquityMapper, UserPlusEquityEntity>
    implements UserPlusEquityService{

    @Autowired
    /** 用于用户权益相关信息的查询和操作 */
    private UserPlusInfoMapper userPlusInfoMapper;
    @Autowired
    /** 用于调用活动服务的相关接口 */
    private ActivityServiceClient activityServiceClient;
    @Autowired
    /** 红包查询只读扩展点网关（插件优先，无插件回落官方 Feign） */
    private RedpacketQueryGateway redpacketQueryGateway;

    /**
     * 根据权益ID列表查询权益信息
     * @param id 权益ID列表
     * @return 权益信息列表
     */
    @Override
    public ResponseResult selectPlusEquitys(List<Integer> id) {
        // 使用LambdaQueryWrapper构造查询条件，查询权益状态为1且权益ID在指定列表中的权益信息
        List<UserPlusEquityEntity> list=this.baseMapper.selectList(new LambdaQueryWrapper<UserPlusEquityEntity>()
                                        .eq(UserPlusEquityEntity::getEquityStatus,1)
                                        .in(UserPlusEquityEntity::getEquityId,id));
        return ResponseResult.success(list);
    }

    /**
     * 插入权益信息
     * @param dto 权益信息DTO
     * @return 插入结果
     */
    @Override
    public ResponseResult insertPlusEquity(PlusEquityDTO dto) {
        // 从Redis中获取用户信息，验证Token的有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + dto.getToken());
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 插入权益信息，返回影响的行数
        Integer integer = this.baseMapper.insertPlusEquity(dto);
        if (integer > 0){
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 删除权益信息
     * @param dto 权益信息DTO
     * @return 删除结果
     */
    @Override
    public ResponseResult deletePlusEquity(PlusEquityDTO dto) {
        // 从Redis中获取用户信息，验证Token的有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + dto.getToken());
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 构建包含所有已设置权益类型ID的列表
        ArrayList<Object> list = new ArrayList<>();
        List<String> selectthalitypes = this.baseMapper.selectthalitypes();
        for (String selectthalitype : selectthalitypes) {
            String[] split = selectthalitype.split(",");
            List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
            list.addAll(appIdList);
        }
        // 检查待删除的权益类型是否已被设置，如果已被设置，则不允许删除
        boolean contains = list.contains(dto.getEquityId());
        if (contains){
            return ResponseResult.fail(BizCodeEnume.PLUS_ERROR);
        }
        // 将权益信息备份到临时表
        UserPlusEquityEntity plusEquityEntity =this.baseMapper.selectById(dto.getEquityId());
        Integer integer1 =this.baseMapper.insertplusequityTemp(plusEquityEntity);

        // 删除权益信息，返回影响的行数
        Integer integer = this.baseMapper.deleteById(plusEquityEntity.getEquityId());
        if (integer > 0){
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 更新权益信息
     * @param dto 权益信息DTO
     * @return 更新结果
     */
    @Override
    public ResponseResult updatePlusEquity(PlusEquityDTO dto) {
        // 从Redis中获取用户信息，验证Token的有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + dto.getToken());
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 如果权益状态设置为0，检查该权益类型是否已被设置，如果已被设置，则不允许更新状态为0
        if (dto.getEquityStatus() == 0) {
            ArrayList<Object> list = new ArrayList<>();
            List<String> selectthalitypes = this.baseMapper.selectthalitypes();
            for (String selectthalitype : selectthalitypes) {
                String[] split = selectthalitype.split(",");
                List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                list.addAll(appIdList);
            }
            boolean contains = list.contains(dto.getEquityId());
            if (contains) {
                return ResponseResult.fail(BizCodeEnume.PLUS_ERROR2);
            }
        }
        // 更新权益信息，返回影响的行数
        Integer integer = this.baseMapper.update(null,new LambdaUpdateWrapper<UserPlusEquityEntity>()
                                                .set(dto.getEquityStatus()!=null,UserPlusEquityEntity::getEquityStatus,dto.getEquityStatus())
                                                .eq(UserPlusEquityEntity::getEquityId,dto.getEquityId()));
        if (integer > 0){
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 根据条件查询权益信息
     * @param map 查询条件
     * @return 权益信息列表
     */
    @Override
    public ResponseResult selectPlusEquity(Map<String, Object> map) {
        // 从Redis中获取用户信息，验证Token的有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token").toString());
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 分页查询权益信息
        IPage<UserPlusEquityEntity> page=this.baseMapper.selectPlusEquityByPage(new Query<UserPlusEquityEntity>().getPage(map),map);
        if (page != null && page.getRecords().size() > 0){
            for (UserPlusEquityEntity plusEquityEntity : page.getRecords()) {
                // 根据权益类型处理权益值，如红包类型则查询红包模板信息，代金券类型则转换为实际金额
                if (plusEquityEntity.getEquityType() == 2 || plusEquityEntity.getEquityType() == 3){
                    String equityValue = plusEquityEntity.getEquityValue();
                    String[] split = equityValue.split(",");
                    List<Integer> redIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                    // 调用活动服务查询红包模板信息
                    Map<String, Object> actMap = new HashMap<>();
                    actMap.put("id",redIdList);
                    List<RedTemplateEntityVO> result = activityServiceClient.selectredtemplateId(redIdList);
                    // 只读扩展点：二开插件优先，无插件回落官方 Feign 结果。
                    result = redpacketQueryGateway.redTemplatesByIds(null, redIdList, result);
                    plusEquityEntity.setRedList(result);
                }
                if (plusEquityEntity.getEquityType() == 1){
                    BigDecimal multiply = new BigDecimal(plusEquityEntity.getEquityValue()).multiply(new BigDecimal(10));
                    plusEquityEntity.setEquityValue(multiply.toString());
                }
            }
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public List<PlusEquityEntityVO> selectplusequitys(List<Integer> id) {
        List<PlusEquityEntityVO> selectplusequitys = this.baseMapper.selectplusequitys(id);
        return selectplusequitys;
    }
}
