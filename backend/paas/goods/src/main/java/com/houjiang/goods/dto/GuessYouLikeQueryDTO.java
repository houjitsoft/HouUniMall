package com.houjiang.goods.dto;


import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GuessYouLikeQueryDTO extends PageUserTokenBeanDTO {

	/**
	 * 分类id
	 */
	private List<Integer> catIds;

	/**
	 * 1特产推荐;2新品推荐;3为你推荐;4蜂狂特价;5新人专区;6平台活动专区;7:9.9包邮专区;8:品牌生活;9:好货溯源;10:猜你喜欢;11:时令果蔬;12:节日钜惠;13:限时折扣;14:超级拼团;15:发现好店
	 */
	private Integer opType;

	/**
	 *商品类型
	 */
	private Integer noneType;

	/**
	 * 商品名
	 */
	private String commonName;

	/**
	 *
	 */
	private String tableName;


}
