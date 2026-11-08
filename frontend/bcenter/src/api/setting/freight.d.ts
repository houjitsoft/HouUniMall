
export interface FreightModal {
    /**
     * 配送内容
     */
    content: Content[];
    /**
     * 名称
     */
    name: string;
    /**
     * 备注
     */
    remark?: string;
    /**
     * 计费方式 0计件 1计重 2计体积
     */
    type: number;
}

export interface Content {
    /**
     * 地区id
     */
    area_ids: string[];
    /**
     * 地区名称
     */
    area_names: string[] | string;
    /**
     * 续 件/ kg/立方  数量
     */
    continue_num: number;
    /**
     * 续 件/ kg/立方  价格
     */
    continue_price: number;
    /**
     * 首 件/ kg/立方  数量
     */
    first_num: number;
    /**
     * 首 件/ kg/立方  价格
     */
    first_price: number;
}