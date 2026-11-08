package com.houjiang.commons.vo.goods;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsBatchCommonVO extends UserTokenBeanDTO {

	/** 分类id，前台传入 */
	private Integer catId;
	/** 分类id，前台传入 */
	private String catIds;
	/** 分类名称，前台传入 */
	private String catName;
	/** 类型id,用于获取属性及规格 */
	private Integer typeId;
	/** 售卖区域id,和hou_transport_area中的id对应 */
	private Integer transportAreaId;
	/** 运费模板 */
	private Integer transportTemplate;
	/** 归属店铺分类id */
	private String shopCatId;

	/** 批量导入的图片 */
	private List<ManageBatchImageVO> batchImageList;

	/** 文件路径 */
	private String csvPath;
	private String key;


}
