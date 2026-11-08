package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class DetailModelVO {

	private Integer shopId;

	private String shopName;

	private Integer catId;

	private String url;

	private Integer commonId;

	private Integer goodsId;

	private Integer noneType;

	private String noneName;

	public DetailModelVO() {
		super();
	}

	public DetailModelVO(Integer commonId) {
		super();
		this.commonId = commonId;
	}


}
