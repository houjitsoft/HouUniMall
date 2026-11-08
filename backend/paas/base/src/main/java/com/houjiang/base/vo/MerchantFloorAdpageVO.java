package com.houjiang.base.vo;

import com.houjiang.base.dto.PlatformLeftAdpageNavDTO;
import com.houjiang.base.dto.PlatformRightAdpageNavDTO;
import com.houjiang.base.dto.WebFloorAdpageNameDTO;
import com.houjiang.commons.vo.goods.GoodsIdsVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName GdbMerchantFloorAdpage
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:50
 * @Version 1.0
 * @author Amos
 */
@Data
public class MerchantFloorAdpageVO {

    /**
     * 楼层id
     */
    private Integer floorAdpageId;

    /**
     * 1启用2不启用
     */
    private Integer floorStatus;

    /**
     * 楼层名称
     */
    private String floorAdpageName;

    /**
     * 楼层信息
     */
    private WebFloorAdpageNameDTO floorAdpageNameNew;

    /**
     * 左侧轮播
     */
    private String leftAdpageBanner;

    /**
     *货品id查询返回集合
     */
    private List<GoodsIdsQueryListVO> adpageBannerList;

    /**
     * 左侧楼层导航
     */
    private String leftAdpageNav;

    /**
     * 左侧分类集合
     */
    private List<PlatformLeftAdpageNavDTO> leftAdpageNavList;

    /**
     * 右侧推荐
     */
    private String rightHotRecommend;

    /**
     * 商品查询返回集合
     */
    private List<GoodsIdsVO> rightRecGoodsList;

    /**
     * 右侧导航
     */
    private PlatformRightAdpageNavDTO rightRec;

    /**
     * 右侧楼层导航
     */
    private String rightAdpageNav;

    /**
     * 右侧分类集合
     */
    private List<WebFloorAdpageNameVO> rightAdpageNavList;

    /**
     * 货品id查询返回集合
     */
    private List<GoodsIdsQueryVO> goodsList;

    /**
     * 楼层默认排序
     */
    private Integer floorAdpageOrder;

    /**
     * 页面类型  5单楼层 6多楼层 1 天天特价 2 特色专题  3 为你推荐 4家居馆  (1、2、3、4 状态在gdb_pc_index_config表）
     */
    private Integer pageType;
}
