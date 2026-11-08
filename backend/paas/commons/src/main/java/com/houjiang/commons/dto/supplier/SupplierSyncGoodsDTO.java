package com.houjiang.commons.dto.supplier;

import com.houjiang.commons.dto.goods.SynchronousGoodsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSyncGoodsDTO {

    private List<SynchronousGoodsDTO> goodsList;

    private String token;

}
