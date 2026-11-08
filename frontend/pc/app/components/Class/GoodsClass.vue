<template>
    <div class="goods-class-wrapper">
        <!-- 一级分类选择 -->
        <div class="custom-select" :style="selectStyle">
            <div class="select-trigger" @click="toggleDropdown('level1')">
                <span class="select-value">{{ $t(selectedLevel1Name || '') || $t('请选择一级分类') }}</span>
                <span class="select-arrow" :class="{ 'open': dropdownState.level1 }">▼</span>
            </div>
            <ul class="select-dropdown" v-show="dropdownState.level1">
                <li class="select-option" :class="{ 'selected': selectedLevel1Id === null }"
                    @click="selectLevel1(null, '')">
                    {{ $t('请选择一级分类') }}
                </li>
                <li class="select-option" v-for="item in level1List" :key="item.catId"
                    :class="{ 'selected': selectedLevel1Id === item.catId }"
                    @click="selectLevel1(item.catId, item.catName)">
                    {{ $t(item.catName || '') }}
                </li>
            </ul>
        </div>

        <!-- 二级分类选择 -->
        <div class="custom-select" :style="selectStyle" v-if="selectedLevel1Id !== null">
            <div class="select-trigger" @click="toggleDropdown('level2')">
                <span class="select-value">{{ $t(selectedLevel2Name || '') || $t('请选择二级分类') }}</span>
                <span class="select-arrow" :class="{ 'open': dropdownState.level2 }">▼</span>
            </div>
            <ul class="select-dropdown" v-show="dropdownState.level2">
                <li class="select-option" :class="{ 'selected': selectedLevel2Id === null }"
                    @click="selectLevel2(null, '')">
                    {{ $t('请选择二级分类') }}
                </li>
                <li class="select-option" v-for="item in level2List" :key="item.catId"
                    :class="{ 'selected': selectedLevel2Id === item.catId }"
                    @click="selectLevel2(item.catId, item.catName)">
                    {{ $t(item.catName || '') }}
                </li>
            </ul>
        </div>

        <!-- 三级分类选择 -->
        <div class="custom-select" :style="selectStyle" v-if="selectedLevel2Id !== null">
            <div class="select-trigger" @click="toggleDropdown('level3')">
                <span class="select-value">{{ $t(selectedLevel3Name || '') || $t('请选择三级分类') }}</span>
                <span class="select-arrow" :class="{ 'open': dropdownState.level3 }">▼</span>
            </div>
            <ul class="select-dropdown" v-show="dropdownState.level3">
                <li class="select-option" :class="{ 'selected': selectedLevel3Id === null }"
                    @click="selectLevel3(null, '')">
                    {{ $t('请选择三级分类') }}
                </li>
                <li class="select-option" v-for="item in level3List" :key="item.catId"
                    :class="{ 'selected': selectedLevel3Id === item.catId }"
                    @click="selectLevel3(item.catId, item.catName)">
                    {{ $t(item.catName || '') }}
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { queryGoodsAll } from '~/services/goods.service'
import type { GoodsCategoryItem } from '~/types/goods'

// 定义组件属性
const props = defineProps({
    modelValue: {
        type: Array,
        default: () => []
    },
    width: {
        type: [Number, String],
        default: 'auto'
    },
    height: {
        type: [Number, String],
        default: 'auto'
    },
    // 返回格式：'id' 返回ID数组，'name' 返回名称数组
    returnType: {
        type: String,
        default: 'id',
        validator: (value: string) => ['id', 'name'].includes(value)
    }
})

// 定义组件事件
const emit = defineEmits(['update:modelValue', 'change'])

// 计算属性，用于设置 select 的样式
const selectStyle = computed(() => {
    const style: any = {}

    if (props.width) {
        style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
    }

    if (props.height) {
        style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
    }

    return style
})

// 响应式数据
const level1List = ref<GoodsCategoryItem[]>([])
const level2List = ref<GoodsCategoryItem[]>([])
const level3List = ref<GoodsCategoryItem[]>([])
const dataLoaded = ref(false)

const selectedLevel1Id = ref<number | null>(null)
const selectedLevel2Id = ref<number | null>(null)
const selectedLevel3Id = ref<number | null>(null)

const selectedLevel1Name = ref('')
const selectedLevel2Name = ref('')
const selectedLevel3Name = ref('')

// 下拉框显示状态
const dropdownState = ref({
    level1: false,
    level2: false,
    level3: false
})

// 初始化选中值
const initSelectedValues = () => {
    if (!props.modelValue || !Array.isArray(props.modelValue) || !dataLoaded.value) {
        return
    }

    const value = props.modelValue
    const returnType = props.returnType

    // 根据 returnType 判断是ID还是名称
    if (returnType === 'id') {
        // ID模式：查找对应的分类
        if (value.length > 0 && value[0]) {
            const level1 = level1List.value.find(item => item.catId === Number(value[0]))
            if (level1) {
                selectedLevel1Id.value = level1.catId
                selectedLevel1Name.value = level1.catName
                level2List.value = level1.childGoodsCatList || []

                if (value.length > 1 && value[1]) {
                    const level2 = level1.childGoodsCatList?.find(item => item.catId === Number(value[1]))
                    if (level2) {
                        selectedLevel2Id.value = level2.catId
                        selectedLevel2Name.value = level2.catName
                        level3List.value = level2.childGoodsCatList || []

                        if (value.length > 2 && value[2]) {
                            const level3 = level2.childGoodsCatList?.find(item => item.catId === Number(value[2]))
                            if (level3) {
                                selectedLevel3Id.value = level3.catId
                                selectedLevel3Name.value = level3.catName
                            }
                        }
                    }
                }
            }
        }
    } else {
        // 名称模式：根据名称查找
        if (value.length > 0 && value[0]) {
            const level1 = level1List.value.find(item => item.catName === value[0])
            if (level1) {
                selectedLevel1Id.value = level1.catId
                selectedLevel1Name.value = level1.catName
                level2List.value = level1.childGoodsCatList || []

                if (value.length > 1 && value[1]) {
                    const level2 = level1.childGoodsCatList?.find(item => item.catName === value[1])
                    if (level2) {
                        selectedLevel2Id.value = level2.catId
                        selectedLevel2Name.value = level2.catName
                        level3List.value = level2.childGoodsCatList || []

                        if (value.length > 2 && value[2]) {
                            const level3 = level2.childGoodsCatList?.find(item => item.catName === value[2])
                            if (level3) {
                                selectedLevel3Id.value = level3.catId
                                selectedLevel3Name.value = level3.catName
                            }
                        }
                    }
                }
            }
        }
    }
}

// 加载分类数据
const loadCategoryData = async () => {
    try {
        const response = await queryGoodsAll({ catParentId: 0 })
        if (response?.code === 1 && response.data) {
            level1List.value = response.data
            dataLoaded.value = true
            // 数据加载完成后，初始化选中值
            initSelectedValues()
        }
    } catch (error) {
        console.error('加载商品分类数据失败:', error)
        dataLoaded.value = true
        initSelectedValues()
    }
}

// 统一的下拉框切换函数
const toggleDropdown = (type: 'level1' | 'level2' | 'level3') => {
    dropdownState.value[type] = !dropdownState.value[type]
    // 关闭其他下拉框
    Object.keys(dropdownState.value).forEach(key => {
        if (key !== type) {
            dropdownState.value[key as keyof typeof dropdownState.value] = false
        }
    })
}

// 选择一级分类
const selectLevel1 = (id: number | null, name: string) => {
    selectedLevel1Id.value = id
    selectedLevel1Name.value = name
    dropdownState.value.level1 = false

    // 重置二级和三级分类
    selectedLevel2Id.value = null
    selectedLevel2Name.value = ''
    selectedLevel3Id.value = null
    selectedLevel3Name.value = ''
    level2List.value = []
    level3List.value = []

    if (id !== null) {
        const level1 = level1List.value.find(item => item.catId === id)
        level2List.value = level1?.childGoodsCatList || []
    }
    updateModelValue()
}

// 选择二级分类
const selectLevel2 = (id: number | null, name: string) => {
    selectedLevel2Id.value = id
    selectedLevel2Name.value = name
    dropdownState.value.level2 = false

    // 重置三级分类
    selectedLevel3Id.value = null
    selectedLevel3Name.value = ''
    level3List.value = []

    if (id !== null && selectedLevel1Id.value !== null) {
        const level1 = level1List.value.find(item => item.catId === selectedLevel1Id.value)
        const level2 = level1?.childGoodsCatList?.find(item => item.catId === id)
        level3List.value = level2?.childGoodsCatList || []
    }
    updateModelValue()
}

// 选择三级分类
const selectLevel3 = (id: number | null, name: string) => {
    selectedLevel3Id.value = id
    selectedLevel3Name.value = name
    dropdownState.value.level3 = false
    updateModelValue()
}

// 点击外部关闭下拉框
const handleClickOutside = (event: MouseEvent) => {
    const target = event.target as HTMLElement
    if (!target.closest('.custom-select')) {
        dropdownState.value = { level1: false, level2: false, level3: false }
    }
}

// 监听属性变化，更新组件内部状态
watch(() => props.modelValue, (value) => {
    if (dataLoaded.value && value && Array.isArray(value) && value.length > 0) {
        nextTick(() => {
            initSelectedValues()
        })
    }
}, { immediate: true, deep: true })

// 更新modelValue
const updateModelValue = () => {
    // 根据 returnType 返回ID或名称数组
    const values: (number | string)[] = props.returnType === 'id' ? [
        ...(selectedLevel1Id.value !== null ? [selectedLevel1Id.value] : []),
        ...(selectedLevel2Id.value !== null ? [selectedLevel2Id.value] : []),
        ...(selectedLevel3Id.value !== null ? [selectedLevel3Id.value] : [])
    ] : [
        ...(selectedLevel1Name.value ? [selectedLevel1Name.value] : []),
        ...(selectedLevel2Name.value ? [selectedLevel2Name.value] : []),
        ...(selectedLevel3Name.value ? [selectedLevel3Name.value] : [])
    ]

    emit('update:modelValue', values)
    emit('change', {
        level1: {
            id: selectedLevel1Id.value,
            name: selectedLevel1Name.value
        },
        level2: {
            id: selectedLevel2Id.value,
            name: selectedLevel2Name.value
        },
        level3: {
            id: selectedLevel3Id.value,
            name: selectedLevel3Name.value
        }
    })
}

// 组件挂载时加载数据
onMounted(() => {
    loadCategoryData()
    document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.goods-class-wrapper {
    display: flex;
    gap: 8px;
    position: relative;

    .custom-select {
        flex: 1;
        min-width: 0;
        position: relative;
        z-index: 1;

        .select-trigger {
            width: 100%;
            padding: 8px 32px 8px 12px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            font-size: 14px;
            color: #333;
            background-color: #fff;
            cursor: pointer;
            transition: all 0.3s ease;
            box-sizing: border-box;
            display: flex;
            align-items: center;
            justify-content: space-between;
            min-height: 36px;

            .select-value {
                flex: 1;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .select-arrow {
                position: absolute;
                right: 12px;
                font-size: 12px;
                color: #999;
                transition: transform 0.3s ease;
                pointer-events: none;

                &.open {
                    transform: rotate(180deg);
                }
            }

            &:hover {
                border-color: #999;
            }

            &:focus {
                outline: none;
                border-color: var(--color);
                box-shadow: 0 0 0 2px color-mix(in srgb, var(--color) 20%, transparent);
            }
        }

        .select-dropdown {
            position: absolute;
            top: calc(100% + 4px);
            left: 0;
            right: 0;
            max-height: 300px;
            overflow-y: auto;
            background-color: #fff;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
            margin: 0;
            padding: 4px 0;
            list-style: none;
            z-index: 1000;

            .select-option {
                padding: 10px 12px;
                font-size: 14px;
                color: #333;
                cursor: pointer;
                transition: all 0.2s ease;
                line-height: 1.5;

                &:hover {
                    background-color: color-mix(in srgb, var(--color) 10%, white);
                    color: var(--color);
                }

                &.selected {
                    background-color: color-mix(in srgb, var(--color) 8%, white);
                    color: var(--color);
                    font-weight: 500;
                }
            }
        }
    }
}
</style>
