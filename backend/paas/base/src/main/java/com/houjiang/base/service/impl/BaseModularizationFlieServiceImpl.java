package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.ModularizationFlieDTO;
import com.houjiang.base.entity.BaseModularizationFlieEntity;
import com.houjiang.base.feign.ThirdPartyServiceClient;
import com.houjiang.base.mapper.BaseModularizationFlieMapper;
import com.houjiang.base.service.BaseModularizationFlieService;
import com.houjiang.base.vo.ModularizationFlieVO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseModularizationFlieServiceImpl
 * @Description  组件化  图片 视频空间
 * @Author Amos
 * @Date 2023-07-04 13:17:22
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseModularizationFlieServiceImpl extends ServiceImpl<BaseModularizationFlieMapper, BaseModularizationFlieEntity>
        implements BaseModularizationFlieService {

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    /**
     * 根据文件信息查询模块化文件。
     * @param param 查询参数，包含模块化文件的详细信息。
     * @return 返回查询结果，包含分页信息和模块化文件列表。
     */
    @Override
    public ResponseResult queryYfModularizationFlie(ModularizationFlieDTO param) {
       Map map = null;
       try {
           map = ParseMapUtils.beanToMap(param);
       } catch (IllegalAccessException e) {
           e.printStackTrace();
       }
       IPage<ModularizationFlieVO> page = this.baseMapper.queryYfModularizationFlie(new Query<ModularizationFlieVO>().getPage(map), param);
       return  ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 上传视频URL。
     * @param ossUploadUpdate 包含视频相关信息的参数对象。
     * @return 返回上传结果，成功或失败。
     */
    @Override
    public ResponseResult insertVideoUrl(OssUploadUpdateParamDTO ossUploadUpdate) {
        ModularizationFlieVO houModularizationFlie = new ModularizationFlieVO();
        houModularizationFlie.setFileName(ossUploadUpdate.getStreamName());
        ossUploadUpdate.setStreamName(".png");
        ResponseResult resp = thirdPartyServiceClient.uploadToOssBase64(ossUploadUpdate);
        //将数据写入数据库
        //获取视频封面url
        houModularizationFlie.setFileCoverUrl(String.valueOf(resp.getData()));
        //获取视频url
        houModularizationFlie.setFileUrl(ossUploadUpdate.getFileUrl());
        houModularizationFlie.setFileType(2);
        Integer integer = this.baseMapper.manageUploadVideo(houModularizationFlie);
        if(integer > 0){
            return ResponseResult.success();
        }else{
            return ResponseResult.fail();
        }
    }

    /**
     * 上传图片。
     * @param modularizationFlieDTO 包含图片上传信息的参数对象。
     * @return 返回上传结果，成功或失败。
     */
    @Override
    public ResponseResult uploadImage(ModularizationFlieDTO modularizationFlieDTO) {
        List<OssUploadUpdateParamDTO> paramList = modularizationFlieDTO.getParamList();
        ModularizationFlieVO param = new ModularizationFlieVO();
        if (!CollectionUtils.isEmpty(paramList)){
            for (OssUploadUpdateParamDTO file : paramList) {
                //文件名加后缀
                String fileName = file.getStreamName();
                param.setFileName(fileName);
                String str1 = fileName.substring(0, fileName.indexOf("."));
                String str2 = fileName.substring(str1.length() + 1);
                file.setStreamName("." + str2);
                ResponseResult resp = thirdPartyServiceClient.uploadToOssBase64(file);
                param.setFileType(1);
                param.setFileUrl((String) resp.getData());
                this.baseMapper.manageUpload(param);
            }
        }
        return ResponseResult.success();
    }

    /**
     * 删除模块化文件。
     * @param param 包含待删除文件ID的参数对象。
     * @return 返回删除结果，成功或失败。
     */
    @Override
    public ResponseResult deleteYfModularizationFlie(Map<String, Object> param) {
        List<Integer> ids = (List<Integer>) param.get("fileIds");
        if(null != ids && ids.size() > 0 ){
          this.baseMapper.deleteBatchIds(ids);
        }
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    @Override
    public ResponseResult registerStorageFile(Map<String, Object> param) {
        String fileName = param.get("fileName") != null ? String.valueOf(param.get("fileName")).trim() : "";
        String fileUrl = param.get("fileUrl") != null ? String.valueOf(param.get("fileUrl")).trim() : "";
        if (fileName.isEmpty() || fileUrl.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "文件名和访问地址不能为空");
        }
        int fileType = 1;
        if (param.get("fileType") != null) {
            fileType = Integer.parseInt(String.valueOf(param.get("fileType")));
        }
        ModularizationFlieVO vo = new ModularizationFlieVO();
        vo.setFileName(fileName);
        vo.setFileUrl(fileUrl);
        vo.setFileType(fileType);
        this.baseMapper.manageUpload(vo);
        return ResponseResult.success();
    }

    /**
     * 根据条件查询模块化文件列表。
     * @param param 查询参数，包含模块化文件的查询条件。
     * @return 返回查询结果，包含分页信息和模块化文件列表。
     */
    @Override
    public ResponseResult queryYfModularizationFlieShop(ModularizationFlieDTO param) {
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (userInfoVO == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        param.setShopId(userInfoVO.getShopId());
        //分页
        //将对象转换为map
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //分页查询数据
        IPage<ModularizationFlieVO> page = this.baseMapper.queryYfModularizationFlieShop(new Query<ModularizationFlieVO>().getPage(data),param);
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 上传店铺图片
     * @param param 包含上传文件信息和用户token的DTO
     * @return 返回上传结果
     */
    @Override
    public ResponseResult uploadShopImg(ModularizationFlieDTO param) {
        // 通过token获取用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 若用户信息为空，则返回token无效的结果
        if (userInfoVO == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 获取上传文件列表
        List<OssUploadUpdateParamDTO> paramList = param.getParamList();
        ModularizationFlieVO modularizationFlie = new ModularizationFlieVO();
        // 获取OSS配置信息
        // 遍历文件列表，逐个上传
        if (!CollectionUtils.isEmpty(paramList)){
            for (OssUploadUpdateParamDTO file : paramList) {
                // 设置文件名
                //文件名加后缀
                String fileName = file.getStreamName();
                modularizationFlie.setFileName(fileName);
                // 设置OSS终端点
                // 处理文件名，用于后续操作
                String str1 = fileName.substring(0, fileName.indexOf("."));
                String str2 = fileName.substring(str1.length() + 1);
                file.setStreamName("." + str2);
                // 调用第三方服务上传文件到OSS
                ResponseResult resp = thirdPartyServiceClient.uploadToOssBase64(file);
                // 设置文件类型
                modularizationFlie.setFileType(1);
                // 设置文件URL
                modularizationFlie.setFileUrl((String) resp.getData());
                // 设置店铺ID
                modularizationFlie.setShopId(Integer.valueOf(userInfoVO.getShopId()));
                // 将文件信息插入数据库
                this.baseMapper.manageUploadShop(modularizationFlie);
            }
        }
        // 文件上传成功，返回成功结果
        return ResponseResult.success();
    }

    /**
     * 上传店铺视频
     * @param ossUploadUpdate 包含上传视频信息和用户token的DTO
     * @return 返回上传结果
     */
    @Override
    public ResponseResult uploadShopVideo(OssUploadUpdateParamDTO ossUploadUpdate) {
        // 通过token获取用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(ossUploadUpdate.getToken());
        // 若用户信息为空，则返回token无效的结果
        if (userInfoVO == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        ModularizationFlieVO houModularizationFlie = new ModularizationFlieVO();
        // 设置文件名
        houModularizationFlie.setFileName(ossUploadUpdate.getStreamName());
        // 设置视频封面文件名
        ossUploadUpdate.setStreamName(".png");
        // 调用第三方服务上传视频到OSS
        ResponseResult resp = thirdPartyServiceClient.uploadToOssBase64(ossUploadUpdate);
        // 设置视频封面URL
        //将数据写入数据库
        //获取视频封面url
        houModularizationFlie.setFileCoverUrl(String.valueOf(resp.getData()));
        // 设置视频URL
        //获取视频url
        houModularizationFlie.setFileUrl(ossUploadUpdate.getFileUrl());
        // 设置文件类型为视频
        houModularizationFlie.setFileType(2);
        // 将视频信息插入数据库
        Integer integer = this.baseMapper.indexShopVideoUrl(houModularizationFlie,userInfoVO.getShopId());
        // 若插入成功，则返回成功结果，否则返回失败结果
        if(integer > 0){
            return ResponseResult.success();
        }else{
            return ResponseResult.fail();
        }
    }
}
