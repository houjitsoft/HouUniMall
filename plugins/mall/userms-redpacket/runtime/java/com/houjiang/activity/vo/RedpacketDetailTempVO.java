package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.excel.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class RedpacketDetailTempVO implements Serializable {
	public static final long serialVersionUID = 1L;
	/**
	 * 红包标题
	 */
	@ExcelCell(index = 1)
	private String redpacketTitle; 
	/**
	 *红包描述
	 */
	@ExcelCell(index = 2)
	private String redpacketDesc; 
	/**
	 *红包有效期开始时间
	 */
	@ExcelCell(index = 3)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketStartDate; 
	/**
	 *红包有效期结束时间
	 */
	@ExcelCell(index = 4)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketEndDate; 
	/**
	 *红包面额
	 */
	@ExcelCell(index = 5)
	private double redpacketPrice; 
	/**
	 *红包状态
	 */
	@ExcelCell(index = 6)
	private String redpacketState; 
	/**
	 *使用该红包的订单编号
	 */
	@ExcelCell(index = 7)
	private String redpacketOrderId; 
	/**
	 *红包所有者名称
	 */
	@ExcelCell(index = 8)
	private String redpacketOwnerName; 
	/**
	 *红包发放日期
	 */
	@ExcelCell(index = 9)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketActiveDate; 

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}


}
