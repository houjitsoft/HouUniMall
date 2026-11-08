
// 秒杀列表参数
export interface SeckillListsQueryType {
    /**
     * 商品名称
     */
    goods_name: string
    /**
     * 审核状态 0待提交 10待审核 20审核通过 30审核失败
     */
    audit_status: string | number
    /**
     * 活动状态 0未开始 1进行中 2已结束
     */
    status: string | number
    /**
     * 开始时间
     */
    start_time: string
    /**
     * 结束时间
     */
    end_time: string
}

// 秒杀编辑/添加参数
export interface SeckillFormType {
    /**
     * 秒杀ID
     */
    id?: string;
    /**
     * 结束时间
     */
    end_time: string;
    /**
     * 商品id
     */
    goods_id: number | string;
    /**
     * 商品列表信息
     */
    ms_sku_list: SeckillSkuList[];
    /**
     * 下单限制 0不限制
     */
    order_limit: number;
    /**
     * 开始时间
     */
    start_time: string;
    /**
     * 审核状态
     */
    audit_status_text: string
    /**
     * 审核说明
     */
    audit_content: string
}

export interface SeckillSkuList {
    /**
     * 商品sku id
     */
    goods_sku_id: number;
    /**
     * 秒杀价格
     */
    price: number;
    /**
     * 秒杀总库存
     */
    total_stock: number;
}