package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class CommonIsXianUpdateDTO {
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
	 */
	private Integer commonIsXian;

	/**
	 * 商品id集合
	 */
	private List<Integer> commonIdList;

	
	
}
