package com.houjiang.goods.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class WapGoodsDetailQueryDTO {
	/**
	 *商品id
	 */
	private Integer goodsId;
	/**
	 *
	 */
	private String token;
	/**
	 *商品id
	 */
	private Integer commonId;
	/**
	 *场次时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date sessionTime;



}
