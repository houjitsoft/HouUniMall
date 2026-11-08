<template>
    <div class="area-select-wrapper">
        <!-- 省份选择 -->
        <div class="custom-select" :style="selectStyle">
            <div class="select-trigger" @click="toggleDropdown('province')">
                <span class="select-value">{{ $t(selectedProvinceName || '') || $t('请选择') }}</span>
                <span class="select-arrow" :class="{ 'open': dropdownState.province }">▼</span>
            </div>
            <ul class="select-dropdown" v-show="dropdownState.province">
                <li class="select-option" :class="{ 'selected': selectedProvinceId === '' }"
                    @click="selectProvince('', '')">
                    {{ $t('请选择') }}
                </li>
                <li class="select-option" v-for="province in provinceList" :key="province.districtId"
                    :class="{ 'selected': selectedProvinceId === province.districtId }"
                    @click="selectProvince(province.districtId, province.districtName)">
                    {{ $t(province.districtName || '') }}
                </li>
            </ul>
        </div>

        <!-- 城市选择 -->
        <div class="custom-select" :style="selectStyle" v-if="Number(showArea) > 1">
            <div class="select-trigger" @click="toggleDropdown('city')">
                <span class="select-value">{{ $t(selectedCityName || '') || $t('请选择') }}</span>
                <span class="select-arrow" :class="{ 'open': dropdownState.city }">▼</span>
            </div>
            <ul class="select-dropdown" v-show="dropdownState.city">
                <li class="select-option" :class="{ 'selected': selectedCityId === '' }" @click="selectCity('', '')">
                    {{ $t('请选择') }}
                </li>
                <li class="select-option" v-for="city in cityList" :key="city.districtId"
                    :class="{ 'selected': selectedCityId === city.districtId }"
                    @click="selectCity(city.districtId, city.districtName)">
                    {{ $t(city.districtName || '') }}
                </li>
            </ul>
        </div>

        <!-- 区县选择 -->
        <div class="custom-select" :style="selectStyle" v-if="Number(showArea) === 3">
            <div class="select-trigger" @click="toggleDropdown('district')">
                <span class="select-value">{{ $t(selectedDistrictName || '') || $t('请选择') }}</span>
                <span class="select-arrow" :class="{ 'open': dropdownState.district }">▼</span>
            </div>
            <ul class="select-dropdown" v-show="dropdownState.district">
                <li class="select-option" :class="{ 'selected': selectedDistrictId === '' }"
                    @click="selectDistrict('', '')">
                    {{ $t('请选择') }}
                </li>
                <li class="select-option" v-for="district in districtList" :key="district.districtId"
                    :class="{ 'selected': selectedDistrictId === district.districtId }"
                    @click="selectDistrict(district.districtId, district.districtName)">
                    {{ $t(district.districtName || '') }}
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { queryDistrictAll } from '~/services/base.service'
import type { DistrictItem } from '~/types/address'

// 定义组件属性
const props = defineProps({
    modelValue: {
        type: Array,
        default: () => []
    },
    showArea: {
        type: [Number, String],
        default: 3
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
        default: 'name',
        validator: (value: string) => ['id', 'name'].includes(value)
    }
})

// 定义组件事件
const emit = defineEmits(['update:modelValue'])

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
const provinceList = ref<DistrictItem[]>([])
const cityList = ref<DistrictItem[]>([])
const districtList = ref<DistrictItem[]>([])
const dataLoaded = ref(false)

const selectedProvinceId = ref('')
const selectedCityId = ref('')
const selectedDistrictId = ref('')

const selectedProvinceName = ref('')
const selectedCityName = ref('')
const selectedDistrictName = ref('')

// 下拉框显示状态 - 使用对象统一管理
const dropdownState = ref({
    province: false,
    city: false,
    district: false
})

// 初始化选中值
const initSelectedValues = () => {
    if (!props.modelValue || !Array.isArray(props.modelValue) || !dataLoaded.value) {
        return
    }

    const value = props.modelValue
    const showAreaNum = Number(props.showArea)

    if (showAreaNum >= 1 && value[0]) {
        // 根据名称或ID查找省份（支持字符串和数字ID的比较）
        const province = provinceList.value.find(p =>
            p.districtName === value[0] ||
            String(p.districtId) === String(value[0]) ||
            p.districtId === value[0]
        )
        if (province) {
            selectedProvinceId.value = province.districtId
            selectedProvinceName.value = province.districtName
            cityList.value = province.childDistrict || []

            if (showAreaNum >= 2 && value[1]) {
                // 支持字符串和数字ID的比较
                const city = province.childDistrict?.find(c =>
                    c.districtName === value[1] ||
                    String(c.districtId) === String(value[1]) ||
                    c.districtId === value[1]
                )
                if (city) {
                    selectedCityId.value = city.districtId
                    selectedCityName.value = city.districtName
                    districtList.value = city.childDistrict || []

                    if (showAreaNum === 3 && value[2]) {
                        // 支持字符串和数字ID的比较
                        const district = city.childDistrict?.find(d =>
                            d.districtName === value[2] ||
                            String(d.districtId) === String(value[2]) ||
                            d.districtId === value[2]
                        )
                        if (district) {
                            selectedDistrictId.value = district.districtId
                            selectedDistrictName.value = district.districtName
                        }
                    }
                }
            }
        }
    }
}

// 加载地区数据
const loadDistrictData = async () => {
    try {
        const response = await queryDistrictAll({})
        if (response?.data) {
            provinceList.value = response.data
            dataLoaded.value = true
            // 数据加载完成后，初始化选中值
            initSelectedValues()
        }
    } catch (error) {
        console.error('加载地区数据失败:', error)
        dataLoaded.value = true
        // 即使加载失败，也要尝试初始化（可能使用默认值）
        initSelectedValues()
    }
}

// 统一的下拉框切换函数
const toggleDropdown = (type: 'province' | 'city' | 'district') => {
    dropdownState.value[type] = !dropdownState.value[type]
    // 关闭其他下拉框
    Object.keys(dropdownState.value).forEach(key => {
        if (key !== type) {
            dropdownState.value[key as keyof typeof dropdownState.value] = false
        }
    })
}

// 选择省份
const selectProvince = (id: string, name: string) => {
    selectedProvinceId.value = id
    selectedProvinceName.value = name
    dropdownState.value.province = false

    // 重置城市和区县
    selectedCityId.value = ''
    selectedCityName.value = ''
    selectedDistrictId.value = ''
    selectedDistrictName.value = ''
    cityList.value = []
    districtList.value = []

    if (id) {
        const province = provinceList.value.find(p => p.districtId === id)
        cityList.value = province?.childDistrict || []
    }
    updateModelValue()
}

// 选择城市
const selectCity = (id: string, name: string) => {
    selectedCityId.value = id
    selectedCityName.value = name
    dropdownState.value.city = false

    // 重置区县
    selectedDistrictId.value = ''
    selectedDistrictName.value = ''
    districtList.value = []

    if (id && selectedProvinceId.value) {
        const province = provinceList.value.find(p => p.districtId === selectedProvinceId.value)
        const city = province?.childDistrict?.find(c => c.districtId === id)
        districtList.value = city?.childDistrict || []
    }
    updateModelValue()
}

// 选择区县
const selectDistrict = (id: string, name: string) => {
    selectedDistrictId.value = id
    selectedDistrictName.value = name
    dropdownState.value.district = false
    updateModelValue()
}

// 点击外部关闭下拉框
const handleClickOutside = (event: MouseEvent) => {
    const target = event.target as HTMLElement
    if (!target.closest('.custom-select')) {
        dropdownState.value = { province: false, city: false, district: false }
    }
}

// 监听属性变化，更新组件内部状态
watch(() => props.modelValue, (value) => {
    if (dataLoaded.value && value && Array.isArray(value) && value.length > 0) {
        // 使用 nextTick 确保数据已经更新
        nextTick(() => {
            initSelectedValues()
        })
    }
}, { immediate: true, deep: true })

// 更新modelValue
const updateModelValue = () => {
    const showAreaNum = Number(props.showArea)
    // 根据 returnType 返回ID或名称数组
    const values: string[] = props.returnType === 'id' ? [
        selectedProvinceId.value,
        ...(showAreaNum >= 2 ? [selectedCityId.value] : []),
        ...(showAreaNum === 3 ? [selectedDistrictId.value] : [])
    ] : [
        selectedProvinceName.value,
        ...(showAreaNum >= 2 ? [selectedCityName.value] : []),
        ...(showAreaNum === 3 ? [selectedDistrictName.value] : [])
    ]
    emit('update:modelValue', values)
}

// 组件挂载时加载数据
onMounted(() => {
    loadDistrictData()
    document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.area-select-wrapper {
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
                border-color: var(--color, #0a8cf0);
                box-shadow: 0 0 0 2px color-mix(in srgb, var(--color, #0a8cf0) 20%, transparent);
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

                // Hover效果 - 使用var(--color)
                &:hover {
                    background-color: rgba(10, 140, 240, 0.1);
                    background-color: color-mix(in srgb, var(--color, #0a8cf0) 10%, white);
                    color: var(--color, #0a8cf0);
                }

                // 选中状态
                &.selected {
                    background-color: rgba(10, 140, 240, 0.08);
                    background-color: color-mix(in srgb, var(--color, #0a8cf0) 8%, white);
                    color: var(--color, #0a8cf0);
                    font-weight: 500;
                }
            }
        }
    }
}
</style>
