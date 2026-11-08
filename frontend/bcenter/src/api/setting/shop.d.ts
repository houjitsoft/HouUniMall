export interface ShopModal {
    /**
     * 商家名称
     */
    name?: string;
    /**
     * 商家类型
     */
    type_text?: string;
    /**
     * 主营类目
     */
    shop_category: ShopCateModal
    /**
     * 商家评分
     */
    sale_orders?: string;
    /**
     * 营业执照
     */
    business_license?: string;
    /**
     * 联系人
     */
    contact: string;
    /**
     * 店铺描述
     */
    desc?: string;
    /**
     * logo
     */
    logo: string;
    /**
     * 联系电话 数组格式
     */
    mobile: number[];
    /**
     * 其他执照 数组格式
     */
    other_license?: string[];
}

interface ShopCateModal {
    /**
     * 分类ID
     */
    id?: string
    /**
     * 分类名称
     */
    name: string
}