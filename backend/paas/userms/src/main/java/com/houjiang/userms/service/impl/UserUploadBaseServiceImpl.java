package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.shop.ManageShopVideoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.RedisLock;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.userms.dto.ManageBatchDeleteImageDTO;
import com.houjiang.userms.dto.ManageImageNameUpdateParamDTO;
import com.houjiang.userms.dto.ManageImageSpaceQueryDTO;
import com.houjiang.userms.dto.ManageImageUploadParamDTO;
import com.houjiang.userms.entity.UserUploadBaseEntity;
import com.houjiang.userms.feign.MerchantServiceClient;
import com.houjiang.userms.mapper.UserUploadBaseMapper;
import com.houjiang.userms.service.UserUploadBaseService;
import com.houjiang.userms.vo.ManageUploadBaseQueryResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserUploadBaseServiceImpl
 * @Description 用户附件表-图片、视频
 * @Author Amos
 * @Date 2023/6/29 9:14
 * @Version 1.0
 * @author Amos
 */
@Service
public class UserUploadBaseServiceImpl extends ServiceImpl<UserUploadBaseMapper, UserUploadBaseEntity> implements UserUploadBaseService {

    @Resource
    private UserUploadBaseMapper userUploadBaseMapper;

    @Resource
    private MerchantServiceClient merchantServiceClient;

    /**
     * 根据传入的参数查询图片空间信息。
     *
     * @param param 查询图片空间的参数DTO，包含查询条件和令牌信息。
     * @return 返回查询结果的ResponseResult对象。
     * @throws IllegalAccessException 如果用户权限不足，抛出此异常。
     */
    @Override
    public ResponseResult queryImageSpace(ManageImageSpaceQueryDTO param) throws IllegalAccessException {
        // 判断用户权限是否足够
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            // 如果权限足够，设置店铺ID并查询数据
            param.setShopId(userPermissionsResult.getShopId());
            // 将查询参数转换为Map对象
            //查询上传的附件信息
            Map data= ParseMapUtils.beanToMap(param);
            // 根据相册ID查询上传的图片信息
            IPage<ManageUploadBaseQueryResultVO> page=this.baseMapper.selectByAlbumId(new Query<ManageUploadBaseQueryResultVO>().getPage(data), param);
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 如果权限不足，返回失败结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
    }

    /**
     * 删除图片接口
     *
     * @param param 包含删除图片所需信息的DTO（数据传输对象）
     * @return 返回操作结果，成功或失败及原因
     *
     * 本方法首先判断用户是否有权限执行删除操作，然后根据传入的参数删除单张或多张图片。
     * 如果用户无权限，将返回失败结果；如果删除成功，将返回成功结果。
     */
    @Override
    public ResponseResult deleteImage(ManageBatchDeleteImageDTO param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            // 如果传入了单个图片ID，则删除该图片
            if(null!=param.getUploadId()){
                this.baseMapper.deleteById(param.getUploadId());
            }
            // 如果传入了图片ID列表，则批量删除这些图片
            if(null!=param.getUploadIds()){
                this.baseMapper.deleteImageBatch(param.getUploadIds());
            }
        } else {
            // 如果用户无权限，则返回失败结果，并说明权限不足
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 如果删除成功，则返回成功结果
        return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
    }

    /**
     * 更新图片名称。
     *
     * @param param 包含更新后的图片名称和用于权限验证的token。
     * @return 如果更新成功，返回成功的响应结果；如果权限不足，返回失败的响应结果。
     * @Transactional 注解确保该方法执行的过程中，数据库操作作为单个事务处理。
     */
    @Override
    @Transactional
    public ResponseResult updateImageName(ManageImageNameUpdateParamDTO param) {
        // 判断用户权限是否足够
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            // 权限足够，更新图片名称
            userUploadBaseMapper.updateImageName(param);
        } else {
            // 权限不足，返回失败结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 更新成功，返回成功结果
        return ResponseResult.success();
    }


    /**
     * 上传图片的方法。
     *
     * @param param 包含上传图片相关信息的参数对象。
     * @return 返回操作结果，成功或失败及原因。
     */
    @Override
    @Transactional
    public ResponseResult uploadImage(ManageImageUploadParamDTO param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            RedisLock redisLock = null;
            try (Jedis jedis = RedisUtil.getJedis()) {
                // 创建Redis锁，用于并发控制
                redisLock = new RedisLock(jedis, "shop-uploadImage-" + userPermissionsResult.getShopId(), 10000);

                // 尝试获取锁
                // 用户锁
                if (redisLock.lock()) {
                    // 查询用户剩余的图片上传空间
                    ResponseResult result = merchantServiceClient.queryAddImageSpaceLimit(new UserTokenBeanDTO(param.getToken()));
                    // 如果查询失败，则直接返回查询结果
                    if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                        return result;
                    }

                    // 设置用户和店铺ID，以及上传时间
                    param.setUserId(userPermissionsResult.getUserId());
                    param.setShopId(userPermissionsResult.getShopId());
                    param.setUploadTime((int) System.currentTimeMillis());

                    // 如果未设置上传大小，则默认为0
                    if (param.getUploadSize() == null) {
                        param.setUploadSize(0);
                    }

                    List<Map<String, String>> path = param.getUploadPath();
                    // 如果未设置上传ID，表示是新增图片上传记录
                    if (param.getUploadId() == null) {
                        for (Map<String, String> map : path) {
                            // 设置图片的URL和名称
                            // 设置图片名
                            if (param.getUploadPath() != null) {
                                param.setPath(map.get("url"));
                                param.setUploadName(map.get("name"));
                            }
                            userUploadBaseMapper.insertImage(param);
                        }
                    } else {
                        // 如果设置了上传ID，表示是更新已存在的图片上传记录
                        // 设置图片的URL和名称
                        if (path != null && !path.isEmpty()) {
                            param.setPath(path.get(0).get("url"));
                            param.setUploadName(path.get(0).get("name"));
                        }
                        userUploadBaseMapper.uploadImage(param);
                    }
                }
            } catch (InterruptedException e) {
                // 处理异常，打印堆栈信息，并返回失败结果
                e.printStackTrace();
                return ResponseResult.fail(e.getMessage());
            } finally {
                // 释放Redis锁
                if (redisLock != null) {
                    redisLock.unLock();
                }
            }
        } else {
            // 权限不足，返回失败结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 操作成功，返回成功结果
        return ResponseResult.success();
    }

    @Override
    public Integer selectShopImageSpaceCount(Integer shopId, String type) {
        Integer result=this.baseMapper.selectShopImageSpaceCount(shopId,type);
        return  result;
    }

    @Override
    public void insertVideo(ManageShopVideoDTO param) {
        this.baseMapper.insertVideo(param);
    }

    @Override
    public void updateVideo(ManageShopVideoDTO param) {
        this.baseMapper.updateVideo(param);
    }

    /**
     * 查询店铺空间图片
     */
    @Override
    public List<ManageShopVideoDTO> selectImageSpaceList(ManageShopVideoDTO param) {
        return baseMapper.selectImageSpaceList(param);
    }
}
