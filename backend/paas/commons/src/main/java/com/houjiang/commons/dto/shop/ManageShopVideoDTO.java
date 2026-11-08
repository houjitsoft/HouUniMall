package com.houjiang.commons.dto.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ManageShopVideoDTO extends PageUserTokenBeanDTO {

	private Integer videoId;

	private Integer albumId;

	private String shopId;

	private String videoUrl;

	private String uploadPath;

	private String uploadType;

	private String uploadName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate;



}
