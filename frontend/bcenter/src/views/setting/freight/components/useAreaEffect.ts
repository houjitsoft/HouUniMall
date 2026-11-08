import { reactive, ref, nextTick, onMounted } from "vue";
import type { Tree, Select } from "./type"


const selected = reactive<Select>({
    // ids: [],
    map_ids: new Map()
});
const filterText = ref<string>('');

const useAreaEffect = (options: Record<string, any>) => {
    const { refs, props } = options;
    const treeRef = refs;
    const select = props.isSelect;

    if (select) {
        watch(filterText, (v1) => {
            if (!select) return
            filterArea(v1);
        })
    } else {
        // 初始化时默认渲染一次已选择试图
        watch(() => props.data, () => {
            if (!select) filterArea();
        }, { deep: true });
    }

    // 已选择的ID变化哦
    onMounted(() => {
        watch(() => selected.map_ids, () => {
            filterArea()
        }, { deep: true, immediate: true })
    })

    const filterArea = async (value: string | any = null) => {
        await nextTick();
        treeRef?.value?.filter(value);
    }

    // 筛选方法
    const filterNode = (value: string, data: Tree) => {
        if (!select) {
            return selected.map_ids.has(data.id)
        }
        if (isSelectAll(data)) {
            return false
        }
        if (!value && select) {
            return true
        }
        return (data.name.indexOf(value) !== -1);
    }

    const isSelectAll = (data: any) => {
        if (!data.sub) {
            return selected.map_ids.has(data.id)
        }
        return data.sub.every((item: any) => isSelectAll(item))
    }

    const onSelect = (node: any, data: Tree) => {
        editNode(data, true);
    }

    const onCancel = (data: Tree) => {
        editNode(data, false);
    }

    const editNode = (data: Tree, select: boolean) => {
        const subLen = data?.sub?.length ?? 0;
        if (select) {
            selected.map_ids.set(data.id, data.id);
        } else {
            selected.map_ids.delete(data.id);
        }
        for (let i = 0; i < subLen; i++) {
            const element = data.sub[i];
            editNode(element, select);
        }
    }

    return {
        filterArea,
        filterNode,
        onSelect,
        onCancel
    }
}

// 将地址数据进行转换成为地址ID数组
const transformAreaIdsToJson = (data: any, ids: number[] = [], names: string[] = []) => {
    const dataLen = data.length;
    for (let i = 0; i < dataLen; i++) {
        const item = data[i];
        const subLen = item?.sub?.length ?? 0;
        if (subLen) {
            const cityArr = item.sub.filter((fitem: any) => {
                return selected.map_ids.has(fitem.id);
            })
            if (cityArr.length == item.sub.length) {
                ids.push(item.id);
                names.push(item.name);
            } else {
                transformAreaIdsToJson(item.sub, ids, names);
            }
        } else {
            if (selected.map_ids.has(item.id)) {
                ids.push(item.id);
                names.push(item.name);
            }
        }
    }
    return { ids, names };
}

// 将ids转换成为地址数据
const transformAreaIdsToParse = (data: any, ids: number[] = [], result_ids: number[] = [], isChildren: boolean = false) => {
    const dataLen = data.length;
    for (let i = 0; i < dataLen; i++) {
        const item = data[i];
        const subLen = item?.sub?.length ?? 0;
        if (ids.includes(item.id)) {
            result_ids.push(item.id);
            transformAreaIdsToParse(item.sub, ids, result_ids, true);
        } else if (isChildren) {
            result_ids.push(item.id);
            if (subLen) {
                transformAreaIdsToParse(item.sub, ids, result_ids, true);
            }
        }
    }
    return result_ids;
}

export {
    selected,
    filterText,
    transformAreaIdsToJson,
    transformAreaIdsToParse
}

export default useAreaEffect;