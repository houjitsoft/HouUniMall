export interface WithDrawModal {
    /**
     * id
     */
    id?: string;
    /**
     * 银行名称
     */
    name: string;
    /**
     * 支行名称
     */
    branch?: string;
    /**
     * 开户名
     */
    username: string;
    /**
     * 卡号
     */
    card_number: string;
    /**
     * 1默认 0不默认
     */
    is_default?: number;
}