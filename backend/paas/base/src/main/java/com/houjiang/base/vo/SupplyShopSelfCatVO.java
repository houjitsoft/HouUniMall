package com.houjiang.base.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class SupplyShopSelfCatVO {

	private Integer shopGoodsCatId;

	private String shopGoodsCatName;

	private Integer dealerStatus;

	private List<SupplyShopSelfCatVO> childShopSelfCatList;


}
