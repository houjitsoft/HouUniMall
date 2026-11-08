package com.houjiang.base.vo.index;


import com.houjiang.base.vo.GoodsIdsQueryVO;
import com.houjiang.commons.vo.goods.SecordNameListVO;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import lombok.Data;

import java.util.List;
/**
 * @ClassName GoodsListHeaderResultVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsListHeaderResultVO {

	/** 下级菜单 */
	private List<SecordNameListVO> nextCatIds;
	/** 推荐商品 */
	private List<GoodsIdsQueryVO> recommendGoodsList;
	private List<WebSystemConfigQueryVO> pcGoodsPage;
	private List<WebCatMenuVO> goodsCatsList;



}
