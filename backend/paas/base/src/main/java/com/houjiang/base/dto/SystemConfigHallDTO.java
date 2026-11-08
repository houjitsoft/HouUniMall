package com.houjiang.base.dto;


import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class SystemConfigHallDTO extends UserTokenBeanDTO {

	/**
	 //1.普通店铺 2供应商店铺
/**
	 *
	 */
	private Integer shopType = 1;

	List<BaseSysConfigsEntity> systemConfigAddParam;



}
