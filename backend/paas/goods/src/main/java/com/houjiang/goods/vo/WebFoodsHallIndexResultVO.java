package com.houjiang.goods.vo;

import com.houjiang.commons.vo.base.WabSystemConfigHallVO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class WebFoodsHallIndexResultVO {

	private List<WabSystemConfigHallVO> slides;

	private List<WebIndexSpecialtysHallVO> recommendHall;

	private List<WebIndexSpecialtysHallVO> allHall;


}
