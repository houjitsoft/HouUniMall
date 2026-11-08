export enum LinkTypeEnum {
    'SHOP_PAGES' = 'shop',
    'GOODS_LISTS' = 'goods',
    'CUSTOM_LINK' = 'custom'
}

export interface Link {
    path: string
    name?: string
    type: string
    query?: Record<string, any>
}
