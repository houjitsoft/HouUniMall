package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.dto.GdbMerchantUploadAlbumDTO;
import com.houjiang.userms.entity.UserUploadAlbumEntity;
import com.houjiang.userms.vo.ManageQueryGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName UserUploadAlbumMapper
 * @Description 用户相册
 * @Author Amos
 * @Date 2023/6/28 16:48
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserUploadAlbumMapper extends BaseMapper<UserUploadAlbumEntity> {

    /**
     * 查询图片分组
     *
     * @param map
     * @return
     */
    List<ManageQueryGroupVO> selectByShopIdAndType(@Param("param") Map<String, Object> map);


    /**
     * 插入
     * @param record
     * @return
     */
    int insertSelective(GdbMerchantUploadAlbumDTO record);

    /**
     * 修改
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(GdbMerchantUploadAlbumDTO record);

    /**
     * 删除
     * @param albumId
     * @return
     */
    int deleteByPrimaryKey(Integer albumId);
}
