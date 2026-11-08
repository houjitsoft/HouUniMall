export interface Tree {
    id: number
    name: string
    parent_id: number
    sub?: Tree[]
    length: number
}

export interface Select {
    // ids: number[]
    map_ids: any
}

