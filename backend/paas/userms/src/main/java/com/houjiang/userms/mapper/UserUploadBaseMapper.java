package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.shop.ManageShopVideoDTO;
import com.houjiang.userms.dto.ManageImageNameUpdateParamDTO;
import com.houjiang.userms.dto.ManageImageSpaceQueryDTO;
import com.houjiang.userms.dto.ManageImageUploadParamDTO;
import com.houjiang.userms.entity.UserUploadBaseEntity;
import com.houjiang.userms.vo.ManageUploadBaseQueryResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName UserUploadBaseMapper
 * @Description 用户附件表-图片、视频
 * @Author Amos
 * @Date 2023/6/29 9:14
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserUploadBaseMapper extends BaseMapper<UserUploadBaseEntity> {


    /**
     * 图片空间查询
     * @param param
     * @return
     */
    IPage<ManageUploadBaseQueryResultVO> selectByAlbumId(IPage<ManageUploadBaseQueryResultVO> page , @Param("param") ManageImageSpaceQueryDTO param);

    /**
     * 查询条数
     * @param param
     * @return
     */
    Integer selectByAlbumIdCount2(@Param("param") ManageImageSpaceQueryDTO param);

    /**
     * 删除图片
     * @param uploadIds
     */
    void deleteImageBatch(@Param("uploadIds")List<Integer> uploadIds);

    /**
     * 更改图片名称
     * @param param
     * @return
     */
    void updateImageName(@Param("param") ManageImageNameUpdateParamDTO param);


    /**
     * 上传
     * @param param
     */
    void insertImage(@Param("param") ManageImageUploadParamDTO param);

    /**
     * 修改
     * @param param
     */
    void uploadImage(@Param("param")ManageImageUploadParamDTO param);

    /**
     * 查询店铺图片空间
     * @param shopId
     * @return
     */
    Integer selectShopImageSpaceCount(@Param("shopId") Integer shopId,@Param("type") String type);

    /**
     * 插入视频
     * @param param
     */
    void insertVideo(ManageShopVideoDTO param);

    /**
     * 更新视频
     * @param param
     */
    void updateVideo(ManageShopVideoDTO param);

    /**
     * 查询店铺空间图片
     */
    List<ManageShopVideoDTO> selectImageSpaceList(ManageShopVideoDTO param);
}
