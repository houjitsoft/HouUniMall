package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.dto.ModularizationFlieDTO;
import com.houjiang.base.entity.BaseModularizationFlieEntity;
import com.houjiang.base.vo.ModularizationFlieVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseModularizationFlieMapper
 * @Description  组件化  图片 视频空间
 * @Author Amos
 * @Date 2023-07-04 13:17:22
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseModularizationFlieMapper extends BaseMapper<BaseModularizationFlieEntity> {

    /**
     * 根据类型，和名称模糊查询 图片库
     **/
    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    IPage<ModularizationFlieVO> queryYfModularizationFlie(IPage<ModularizationFlieVO> page, @Param("param") ModularizationFlieDTO param);

    /**
     * 视频url 视频封面 写入数据库
     **/
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer manageUploadVideo(@Param("file") ModularizationFlieVO file);

    /**
     * 添加图片
     * @param file
     * @return
     */
    Integer manageUpload(@Param("file") ModularizationFlieVO file);

    /**
     * 店铺装修查询图片
     * @param param
     * @return
     */
    IPage<ModularizationFlieVO> queryYfModularizationFlieShop(IPage<ModularizationFlieVO> page, @Param("param") ModularizationFlieDTO param);

    /**
     * 店铺添加图片
     * @param file
     * @return
     */
    Integer manageUploadShop(@Param("file") ModularizationFlieVO file);


    /**
     * 根据店铺ID和模块化文件信息来查询店铺视频URL的索引
     *
     * @param houModularizationFlie 模块化文件信息对象，包含了文件的相关信息
     * @param shopId 店铺的唯一标识符，用于区分不同的店铺
     * @return 返回店铺视频URL的索引，如果找不到则返回null
     */
    Integer indexShopVideoUrl(@Param("houModularizationFlie")ModularizationFlieVO houModularizationFlie, @Param("shopId")String shopId);
}
