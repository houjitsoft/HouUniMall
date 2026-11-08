package com.houjiang.job.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName QuWxGoodsListVO
 * @Description 微信
 * @Author Amos
 * @Date 2023/5/17 13:37
 * @Version 1.0
 * @author Amos
 */
@Data
public class QuWxGoodsListVO {

    /**
     * 错误code
     */
    private Integer errcode;

    /**
     * 总量
     */
    private Integer total;

    /**
     * 微信商品
     */
    private List<QuWxGoodsVO> goods;
}
