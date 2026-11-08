package com.houjiang.goods.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName ManageCommonIdsDTO
 * @Description commonid删除商品
 * @Author Amos
 * @Date 2023/6/26 11:08
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageCommonIdsDTO extends UserTokenBeanDTO{

    /**
     * 商品id集合
     */
    private List<Integer> commonIds;

    /**
     * 商品推荐 1推荐 2 不推荐
     */
    private Integer commonIsRecommend;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 秒杀Id
     */
    private Integer seckillId;

    /**
     * 是否是版本查询
     */
    private Integer isVersion;

    public ManageCommonIdsDTO(Integer commonId, int isVersion, String token) {
        this.commonId = commonId;
        this.isVersion = isVersion;
        this.setToken(token);
    }
}
