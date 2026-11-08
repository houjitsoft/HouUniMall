package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.dto.PiecingTogetherConfigDTO;
import com.houjiang.activity.dto.WebPinTuanParamDTO;
import com.houjiang.activity.entity.ActivityPiecingTogetherConfigEntity;
import com.houjiang.activity.feign.BaseServiceClient;
import com.houjiang.activity.mapper.ActivityPiecingTogetherConfigMapper;
import com.houjiang.activity.mapper.ActivityPiecingTogetherMapper;
import com.houjiang.activity.service.ActivityPiecingTogetherConfigService;
import com.houjiang.activity.vo.GoodsPinTuanResultVO;
import com.houjiang.activity.vo.PinTuanConfigVO;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class ActivityPiecingTogetherConfigServiceImpl extends ServiceImpl<ActivityPiecingTogetherConfigMapper, ActivityPiecingTogetherConfigEntity>
    implements ActivityPiecingTogetherConfigService{

    @Autowired
    private ActivityPiecingTogetherMapper activityPiecingTogetherMapper;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private RedisCache redisCache;

    /**
     * 删除拼团配置模型
     * 该方法用于删除特定的拼团配置信息。通过传入的参数，定位到特定的拼团配置项，并尝试进行删除操作。
     * 如果删除成功，返回操作成功的响应结果；如果删除失败，返回操作失败的响应结果。
     *
     * @param param PiecingTogetherConfigDTO 类型的参数，包含需要删除的拼团配置的ID。
     * @return ResponseResult 类型的返回结果，表示操作的成功与否。
     */
    @Override
    public ResponseResult h5ConfigModelDelete(PiecingTogetherConfigDTO param) {
        try {
            // 使用LambdaQueryWrapper构建查询条件，定位到配置类型为2且ID与参数ID相同的拼团配置项
            // 设置为模块2  1 代表模块1 （不可新增 删除）  2 代表 模块 2（可新增 删除）
            int temp = this.baseMapper.delete(new LambdaQueryWrapper<ActivityPiecingTogetherConfigEntity>()
                    .eq(ActivityPiecingTogetherConfigEntity::getConfigType, 2)
                    .eq(ActivityPiecingTogetherConfigEntity::getId, param.getId()));

            // 判断删除操作影响的行数，如果为1，则表示删除成功
            if (temp == 1) {
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
            // 如果删除操作影响的行数不为1，则表示删除失败
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        } catch (Exception e) {
            // 捕获删除过程中可能出现的异常，打印异常信息，并返回操作失败的响应结果，其中包含异常信息
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }
    }


    /**
     * 新增拼团配置信息
     *
     * @param param 拼团配置的参数对象，包含配置的相关信息
     * @return 返回操作结果，成功或失败
     *
     * 此方法主要用于新增拼团配置模型。通过设置配置类型为2，然后调用基础Mapper接口
     * 实现拼团配置信息的添加。如果添加操作成功，返回操作成功的结果；如果添加操作失败，
     * 返回操作失败的结果。如果操作过程中出现异常，将返回失败结果并附带异常信息。
     */
    @Override
    public ResponseResult h5ConfigModelAdd(PiecingTogetherConfigDTO param) {
        try {
            // 设置拼团配置的类型为2，表示模块2，该模块支持新增和删除操作
            // 设置为模块2 1 代表模块1 （不可新增 删除） 2 代表 模块 2（可新增 删除）
            param.setConfigType(2);
            // 调用基础Mapper接口的新增方法，尝试添加拼团配置信息
            Integer temp = this.baseMapper.h5ConfigModelAdd(param);
            // 判断添加操作是否成功，如果返回值为1，则表示成功
            if(temp == 1){
                // 返回操作成功的结果
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
            // 如果添加操作失败，返回操作失败的结果
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        } catch (Exception e) {
            // 捕获操作过程中可能出现的异常，打印异常信息，并返回操作失败的结果附带异常信息
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }

    }


    /**
     * 更新拼团配置信息。
     * 该方法用于根据传入的拼团配置参数更新数据库中的相关配置信息。
     * 如果更新成功，则返回操作成功的响应结果；如果更新失败或出现异常，则返回操作失败的响应结果。
     *
     * @param param 拼团配置参数对象，包含需要更新的配置信息。
     * @return 返回操作结果的响应对象。如果操作成功，则包含成功代码和消息；如果操作失败，则包含失败代码和消息。
     */
    @Override
    public ResponseResult updateH5Config(PiecingTogetherConfigDTO param) {
        try {
            // 调用BaseMapper的updateH5Config方法，尝试更新拼团配置信息。
            Integer temp = this.baseMapper.updateH5Config(param);
            // 判断更新操作是否成功，如果返回值为1，表示更新成功。
            if(temp == 1){
                // 更新成功，返回操作成功的响应结果。
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
            // 更新失败，返回操作失败的响应结果。
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        } catch (Exception e) {
            // 更新过程中出现异常，打印异常堆栈跟踪信息，并返回操作失败的响应结果，其中包含异常消息。
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }
    }


    /**
     * 查询拼团配置信息
     * 该方法用于根据传入的参数查询拼团活动的配置信息，并解析配置数据中相关的商品信息。
     * 如果token不存在于Redis中，则认为token无效，返回相应的错误结果。
     * 如果配置信息存在，则遍历配置信息，查询并解析每个拼团活动的商品信息，并将解析后的商品信息添加到配置信息中。
     *
     * @param param 查询参数，包含token信息
     * @return 查询结果，包含拼团配置信息列表
     */
    @Override
    public ResponseResult queryH5Config(Map<String, Object> param) {
        try {
            // 检查token是否存在于Redis中
            boolean isExistToken = redisCache.hasKey(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(param.get("token")));
            if(!isExistToken) {
                // 如果token不存在，返回token无效的错误结果
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }

            // 查询所有的拼团配置信息
            List<ActivityPiecingTogetherConfigEntity> pinTuanConfigs = this.baseMapper.selectList(new LambdaQueryWrapper<ActivityPiecingTogetherConfigEntity>());
            if(null != pinTuanConfigs && 0 < pinTuanConfigs.size()){
                for (ActivityPiecingTogetherConfigEntity pinTuanConfig : pinTuanConfigs) {
                    // 用于存放解析后的商品信息
                    // 用于存放h5页面商品信息
                    List<GoodsPinTuanResultVO> goodsPinTuanResults = new ArrayList<>();
                    // 将配置数据解析为字符串列表
                    List<String> list = JsonUtils.string2Obj(pinTuanConfig.getConfigData(), List.class, String.class);
                    if(null != list){
                        for (String commonid : list) {
                            // 根据商品的commonid查询商品的详细信息
                            // 查询拼团商品信息
                            GoodsPinTuanResultVO goodsPinTuanResult = activityPiecingTogetherMapper.queryPinTunGoods(commonid);
                            if(null != goodsPinTuanResult){
                                // 将查询到的商品信息添加到结果列表中
                                goodsPinTuanResults.add(goodsPinTuanResult);
                            }
                        }
                        // 将解析后的商品信息列表设置到拼团配置信息中
                        pinTuanConfig.setGoodsPinTuanResultList(goodsPinTuanResults);
                    }
                }
            }
            // 返回查询成功的结果，包含拼团配置信息列表
            return ResponseResult.success(pinTuanConfigs);
        } catch (Exception e) {
            // 捕获查询过程中可能出现的异常，打印异常信息，并返回失败的结果
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }
    }


    /**
     * 查询H5拼团首页信息
     * 该方法用于根据传入的参数查询拼团首页的配置信息以及相关商品信息，并结合其他服务查询轮播图信息，最终返回拼团首页的数据结构。
     *
     * @param param 查询参数，包含店铺ID等信息
     * @return 包含拼团配置信息、商品信息和轮播图信息的响应结果
     */
    @Override
    public ResponseResult queryH5PinTuanIndex(WebPinTuanParamDTO param) {
        // 获取店铺ID
        Integer shopId = param.getShopId();
        // 初始化用于存放查询结果的Map
        Map<String,Object> map = new HashMap<>();
        try {
            // 查询平台的H5拼团配置信息
            //查询平台h5页面配置
            List<PinTuanConfigVO> pinTuanConfigs = this.baseMapper.queryH5Config();
            // 遍历配置信息，处理每个配置项
            if(null != pinTuanConfigs && !pinTuanConfigs.isEmpty()){
                for (PinTuanConfigVO pinTuanConfig : pinTuanConfigs) {
                    // 初始化用于存放商品拼团信息的列表
                    //用于存放h5页面商品信息
                    List<GoodsPinTuanResultVO> goodsPinTuanResults = new ArrayList<>();
                    // 将配置数据转换为字符串列表
                    List<String> list = JsonUtils.string2Obj(pinTuanConfig.getConfigData(), List.class, String.class);
                    // 遍历商品ID列表，查询每个商品的拼团信息
                    if(null != list){
                        for (String commonid : list) {
                            // 根据商品ID查询拼团商品信息
                            //查询拼团商品信息
                            GoodsPinTuanResultVO goodsPinTuanResult = this.baseMapper.queryPinTunGoods(commonid);
                            // 根据是否有店铺ID和商品的店铺ID是否匹配，决定是否添加到结果列表中
                            if (shopId == null ){
                                if(null != goodsPinTuanResult && goodsPinTuanResult.getGoodsType() != 3){
                                    goodsPinTuanResults.add(goodsPinTuanResult);
                                }
                            }else{
                                if(null != goodsPinTuanResult && goodsPinTuanResult.getShopId().equals(shopId)){
                                    goodsPinTuanResults.add(goodsPinTuanResult);
                                }
                            }
                        }
                        // 将商品拼团信息列表设置到配置项中
                        pinTuanConfig.setGoodsPinTuanResultList(goodsPinTuanResults);
                    }
                }
            }
            // 将拼团配置信息列表放入查询结果中
            map.put("pinTuanConfigs",pinTuanConfigs);
            // 查询轮播图信息
            // 查询轮播图
            NavSortLogoDTO gdbNavSortLogoDTO = new NavSortLogoDTO();
            gdbNavSortLogoDTO.setConfigType("group_slide_picture_h5");
            ResponseResult resp1 = baseServiceClient.quertNavLogo(gdbNavSortLogoDTO);
            // 将轮播图信息列表放入查询结果中
            List<NavSortLogoDTO> list = (List<NavSortLogoDTO>) resp1.getData();
            map.put("images",list);
        } catch (Exception e) {
            // 异常处理，打印堆栈信息
            e.printStackTrace();
        }
        // 返回查询结果
        return ResponseResult.success(map);
    }


    /**
     * 查询拼团商品的详细信息。
     * 该方法用于根据传入的参数查询拼团商品的列表，包括分页信息和商品的详细拼团信息。
     *
     * @param param 查询参数，包含分页信息和查询条件。
     * @return 返回查询结果，包括拼团商品列表和总数。
     */
    @Override
    public ResponseResult queryPinTuanGoodsAll(WebPinTuanParamDTO param) {
        // 初始化结果映射表
        Map<String,Object> resMap = new HashMap<>();
        try {
            // 将查询参数对象转换为Map格式，以便后续处理
            //将对象转换为map
            Map data = ParseMapUtils.beanToMap(param);

            // 初始化分页对象
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            //分页查询数据
            Page<GoodsPinTuanResultVO> pageObj = new Page<>(page, limit);

            // 调用Mapper查询拼团商品信息
            IPage<GoodsPinTuanResultVO> resultVOIPage = this.baseMapper.queryPinTuanGoodsAll(pageObj,param);

            // 将查询结果和总数添加到结果映射表中
            resMap.put("goodsPinTuanResults",resultVOIPage.getRecords());
            resMap.put("count",resultVOIPage.getTotal());

            // 返回查询结果
            return  ResponseResult.success(resMap);
        } catch (Exception e) {
            // 捕获异常，打印堆栈跟踪
            e.printStackTrace();
        }
        // 如果出现异常，返回空的结果映射表
        return ResponseResult.success(resMap);
    }

}




