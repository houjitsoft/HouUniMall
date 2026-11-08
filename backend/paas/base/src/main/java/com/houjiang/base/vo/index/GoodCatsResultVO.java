package com.houjiang.base.vo.index;

import com.houjiang.commons.vo.goods.GoodsCatVO;
import lombok.Data;

import java.util.List;
/**
 * @ClassName GoodCatsResultVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodCatsResultVO {

	private Integer secordCatId;

	private String secordCatName;

	private String catPic;

	private List<GoodsCatVO> gdsCats;



}
