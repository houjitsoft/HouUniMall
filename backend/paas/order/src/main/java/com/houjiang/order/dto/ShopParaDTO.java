package com.houjiang.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 店铺 入参
 * 
 * @author Amos
 *
 */
@Data
public class ShopParaDTO {
	/**
	 * 描述分数
	 */
	private Integer describeScore;

	/**
	 * 服务分数
	 */
	private Integer serviceScore;

	/**
	 * 配送分数
	 */
	private Integer deliveryScore;

	/**
	 * 开始时间，格式为"yyyy-MM-dd HH:mm:ss"，时区为GMT+8
	 * 使用JsonFormat注解进行序列化和反序列化，保证在JSON字符串和Java对象之间正确转换
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	/**
	 * 结束时间，格式为"yyyy-MM-dd HH:mm:ss"，时区为GMT+8
	 * 使用JsonFormat注解进行序列化和反序列化，保证在JSON字符串和Java对象之间正确转换
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

	/**
	 * 页码，用于分页查询
	 */
	private Integer page;

	/**
	 * 每页数据个数，用于分页查询
	 */
	private Integer limit;

}
