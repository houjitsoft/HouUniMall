<template>
    <div class="filter-section">
        <div class="filter-items">
            <!-- 分类筛选 -->
            <div v-if="lineSaleCategories.length > 0" class="filter-item category-filter-item">
                <div class="filter-label">{{ $t('分类') }}</div>
                <div class="filter-values">
                    <div class="filter-values-content">
                        <!-- 未展开模式：显示分类列表（网格布局） -->
                        <template v-if="!showCategoryMore">
                            <span v-for="cat in displayCategories" :key="cat.catId || 0" class="filter-value"
                                :class="{ active: cat.catId && selectedCatIds.includes(cat.catId) }"
                                @click="cat.catId && handleCategoryClick(cat.catId)">
                                {{ $t(cat.catName || '') }}
                            </span>
                        </template>

                        <!-- 展开模式：左侧显示分类标题，右侧显示子分类 -->
                        <template v-else>
                            <div class="category-expanded-container">
                                <template v-for="cat in lineSaleCategories" :key="cat.catId || 0">
                                    <!-- 左侧分类标题 -->
                                    <div class="category-title-item"
                                        :class="{ active: cat.catId && selectedCatIds.includes(cat.catId) }"
                                        @click="cat.catId && handleCategoryClick(cat.catId)">
                                        {{ $t(cat.catName || '') }}
                                    </div>

                                    <!-- 右侧子分类（与左侧分类一一对应） -->
                                    <div class="category-sub-group">
                                        <span v-for="subCat in cat.secordNameList || []" :key="subCat.catId || 0"
                                            class="filter-value"
                                            :class="{ active: subCat.catId && selectedCatIds.includes(subCat.catId) }"
                                            @click="subCat.catId && handleCategoryClick(subCat.catId)">
                                            {{ $t(subCat.catName || '') }}
                                        </span>
                                    </div>
                                </template>
                            </div>
                        </template>
                    </div>

                    <!-- 右侧按钮区域 -->
                    <div class="filter-actions">
                        <!-- 未展开模式：显示"更多"按钮 -->
                        <template v-if="!showCategoryMore">
                            <button v-if="lineSaleCategories.length > displayCategories.length"
                                class="filter-action-btn filter-action-btn--expand" @click="showCategoryMore = true">
                                <UIcon name="i-heroicons-chevron-down" class="btn-icon" />
                                {{ $t('更多') }}
                            </button>
                        </template>

                        <!-- 展开模式：显示"收起"按钮 -->
                        <template v-else>
                            <button class="filter-action-btn filter-action-btn--collapse"
                                @click="showCategoryMore = false">
                                <UIcon name="i-heroicons-chevron-up" class="btn-icon" />
                                {{ $t('收起') }}
                            </button>
                        </template>
                    </div>
                </div>
            </div>

            <!-- 品牌筛选 -->
            <div v-if="lineBrands.length > 0" class="filter-item">
                <div class="filter-label">{{ $t('品牌') }}</div>
                <div class="filter-values">
                    <div class="filter-values-content">
                        <!-- 未展开模式：显示部分品牌 -->
                        <template v-if="!showBrandMore && !brandMultiSelect">
                            <span v-for="brand in displayBrands" :key="brand.brandId || 0" class="filter-value"
                                :class="{ active: brand.brandId && selectedBrandIds.includes(brand.brandId) }"
                                @click="brand.brandId && handleBrandClick(brand.brandId)">
                                {{ $t(brand.brandName || '') }}
                            </span>
                        </template>

                        <!-- 展开模式：显示所有品牌，支持字母筛选 -->
                        <template v-else-if="showBrandMore && !brandMultiSelect">
                            <!-- 字母筛选栏（支持鼠标滑动） -->
                            <div class="brand-alphabet-filter" @mousedown="handleAlphabetMouseDown"
                                @mousemove="handleAlphabetMouseMove" @mouseup="handleAlphabetMouseUp"
                                @mouseleave="handleAlphabetMouseUp">
                                <span class="alphabet-item" :class="{ active: selectedBrandLetter === 'all' }"
                                    @click="handleBrandLetterFilter('all')"
                                    @mouseenter="isAlphabetDragging ? handleBrandLetterFilter('all') : null">
                                    全部
                                </span>
                                <span v-for="letter in brandAlphabetList" :key="letter" class="alphabet-item"
                                    :class="{ active: selectedBrandLetter === letter }"
                                    @click="handleBrandLetterFilter(letter)"
                                    @mouseenter="isAlphabetDragging ? handleBrandLetterFilter(letter) : null">
                                    {{ letter }}
                                </span>
                            </div>

                            <!-- 品牌列表 -->
                            <div class="brand-list-container">
                                <span v-for="brand in filteredBrands" :key="brand.brandId || 0" class="filter-value"
                                    :class="{ active: brand.brandId && selectedBrandIds.includes(brand.brandId) }"
                                    @click="brand.brandId && handleBrandClick(brand.brandId)">
                                    {{ $t(brand.brandName || '') }}
                                </span>
                            </div>
                        </template>

                        <!-- 多选模式：显示所有品牌，支持字母筛选 -->
                        <template v-else>
                            <!-- 字母筛选栏（支持鼠标滑动） -->
                            <div class="brand-alphabet-filter" @mousedown="handleAlphabetMouseDown"
                                @mousemove="handleAlphabetMouseMove" @mouseup="handleAlphabetMouseUp"
                                @mouseleave="handleAlphabetMouseUp">
                                <span class="alphabet-item" :class="{ active: selectedBrandLetter === 'all' }"
                                    @click="handleBrandLetterFilter('all')"
                                    @mouseenter="isAlphabetDragging ? handleBrandLetterFilter('all') : null">
                                    全部
                                </span>
                                <span v-for="letter in brandAlphabetList" :key="letter" class="alphabet-item"
                                    :class="{ active: selectedBrandLetter === letter }"
                                    @click="handleBrandLetterFilter(letter)"
                                    @mouseenter="isAlphabetDragging ? handleBrandLetterFilter(letter) : null">
                                    {{ letter }}
                                </span>
                            </div>

                            <!-- 品牌列表（带复选框） -->
                            <div class="brand-list-container">
                                <label v-for="brand in filteredBrands" :key="brand.brandId || 0"
                                    class="filter-value filter-value--checkbox"
                                    :class="{ 'is-selected': brand.brandId && tempSelectedBrandIds.includes(brand.brandId) }">
                                    <input type="checkbox"
                                        :checked="brand.brandId ? tempSelectedBrandIds.includes(brand.brandId) : false"
                                        @change.stop="brand.brandId && handleBrandCheckboxClick(brand.brandId)" />
                                    <span class="checkbox-label">{{ $t(brand.brandName || '') }}</span>
                                </label>
                            </div>

                            <!-- 已选择品牌显示 -->
                            <div v-if="tempSelectedBrandIds.length > 0" class="brand-selected-list">
                                <div class="selected-label">{{ $t('已选择') }}:</div>
                                <div class="selected-items">
                                    <span v-for="brandId in tempSelectedBrandIds" :key="brandId" class="selected-item">
                                        {{ getBrandNameById(brandId) }}
                                    </span>
                                </div>
                            </div>

                            <!-- 确认/取消按钮 -->
                            <div class="brand-action-buttons">
                                <button class="action-btn action-btn--confirm" @click="handleBrandConfirm">
                                    {{ $t('确认') }}
                                </button>
                                <button class="action-btn action-btn--cancel" @click="handleBrandCancel">
                                    {{ $t('取消') }}
                                </button>
                            </div>
                        </template>
                    </div>

                    <!-- 右侧按钮区域 -->
                    <div class="filter-actions">
                        <!-- 未展开模式：显示"更多"和"多选" -->
                        <template v-if="!showBrandMore && !brandMultiSelect">
                            <button v-if="lineBrands.length > displayBrands.length"
                                class="filter-action-btn filter-action-btn--expand" @click="handleBrandExpand">
                                <UIcon name="i-heroicons-chevron-down" class="btn-icon" />
                                {{ $t('更多') }}
                            </button>
                            <button v-if="lineBrands.length >= 2" class="filter-action-btn filter-action-btn--multi"
                                @click="toggleBrandMultiSelect">
                                <UIcon name="i-heroicons-check-circle" class="btn-icon" />
                                {{ $t('多选') }}
                            </button>
                        </template>

                        <!-- 展开模式：显示"收起"和"多选" -->
                        <template v-else-if="showBrandMore && !brandMultiSelect">
                            <button class="filter-action-btn filter-action-btn--collapse" @click="handleBrandExpand">
                                <UIcon name="i-heroicons-chevron-up" class="btn-icon" />
                                {{ $t('收起') }}
                            </button>
                            <button class="filter-action-btn filter-action-btn--multi" @click="toggleBrandMultiSelect">
                                <UIcon name="i-heroicons-check-circle" class="btn-icon" />
                                {{ $t('多选') }}
                            </button>
                        </template>

                        <!-- 多选模式：显示"收起" -->
                        <template v-else>
                            <button class="filter-action-btn filter-action-btn--collapse" @click="handleBrandCancel">
                                <UIcon name="i-heroicons-chevron-up" class="btn-icon" />
                                {{ $t('收起') }}
                            </button>
                        </template>
                    </div>
                </div>
            </div>

            <!-- 属性筛选（只保留多选按钮） -->
            <div v-for="(attrGroup, index) in displayAttrGroups" :key="index" class="filter-item">
                <div class="filter-label">{{ attrGroup.groupName || '' }}</div>
                <div class="filter-values">
                    <div class="filter-values-content">
                        <!-- 普通模式：显示所有属性 -->
                        <template v-if="!attrMultiSelectMap[attrGroup.groupName || '']">
                            <span v-for="attr in attrGroup.goodsAttributeList" :key="attr.id || 0" class="filter-value"
                                :class="{ active: attrGroup.groupName && attr.id && isAttrSelected(attrGroup.groupName, attr.id) }"
                                @click="attrGroup.groupName && attr.id && attr.attributeValueName && handleAttrClick(attrGroup.groupName, attr.id, attr.attributeValueName)">
                                {{ attr.attributeValueName }}
                            </span>
                        </template>

                        <!-- 多选模式：显示所有属性，带复选框 -->
                        <template v-else>
                            <label v-for="attr in attrGroup.goodsAttributeList" :key="attr.id || 0"
                                class="filter-value filter-value--checkbox"
                                :class="{ 'is-selected': attrGroup.groupName && attr.id && getTempAttrSelected(attrGroup.groupName, attr.id) }">
                                <input type="checkbox"
                                    :checked="attrGroup.groupName && attr.id ? getTempAttrSelected(attrGroup.groupName, attr.id) : false"
                                    @change.stop="attrGroup.groupName && attr.id && handleAttrCheckboxClick(attrGroup.groupName, attr.id)" />
                                <span class="checkbox-label">{{ attr.attributeValueName }}</span>
                            </label>

                            <!-- 已选择属性显示 -->
                            <div v-if="getTempAttrSelectedCount(attrGroup.groupName) > 0" class="attr-selected-list">
                                <div class="selected-label">已选择：</div>
                                <div class="selected-items">
                                    <span v-for="attrId in tempSelectedAttrIds[attrGroup.groupName || ''] || []"
                                        :key="attrId" class="selected-item">
                                        {{ getAttrNameById(attrGroup.groupName || '', attrId) }}
                                    </span>
                                </div>
                            </div>

                            <!-- 确认/取消按钮 -->
                            <div class="attr-action-buttons">
                                <button class="action-btn action-btn--confirm"
                                    @click="attrGroup.groupName && handleAttrConfirm(attrGroup.groupName)">
                                    {{ $t('确认') }}
                                </button>
                                <button class="action-btn action-btn--cancel"
                                    @click="attrGroup.groupName && handleAttrCancel(attrGroup.groupName)">
                                    {{ $t('取消') }}
                                </button>
                            </div>
                        </template>
                    </div>

                    <!-- 右侧按钮区域 -->
                    <div class="filter-actions">
                        <!-- 普通模式：显示"多选"按钮 -->
                        <template v-if="!attrMultiSelectMap[attrGroup.groupName || '']">
                            <button v-if="(attrGroup.goodsAttributeList?.length || 0) > 5"
                                class="filter-action-btn filter-action-btn--multi"
                                @click="attrGroup.groupName && handleAttrMultiSelectChange(attrGroup.groupName, true)">
                                <UIcon name="i-heroicons-check-circle" class="btn-icon" />
                                {{ $t('多选') }}
                            </button>
                        </template>

                        <!-- 多选模式：显示"收起"按钮 -->
                        <template v-else>
                            <button class="filter-action-btn filter-action-btn--collapse"
                                @click="attrGroup.groupName && handleAttrMultiSelectChange(attrGroup.groupName, false)">
                                <UIcon name="i-heroicons-chevron-up" class="btn-icon" />
                                {{ $t('收起') }}
                            </button>
                        </template>
                    </div>
                </div>
            </div>

            <!-- 更多属性筛选（鼠标移入展示所有子集） -->
            <div v-if="moreAttrGroups.length > 0" class="filter-item filter-item--more">
                <div class="filter-label">{{ $t('更多') }}</div>
                <div class="filter-values">
                    <div class="filter-values-content">
                        <div v-for="(attrGroup, index) in moreAttrGroups" :key="index" class="filter-attr-name-wrapper"
                            :class="{ 'is-active': hoveredMoreAttrGroup === attrGroup.groupName }"
                            @mouseenter="attrGroup.groupName && handleMoreAttrMouseEnter(attrGroup.groupName)"
                            @mouseleave="attrGroup.groupName && handleMoreAttrMouseLeave(attrGroup.groupName)">
                            <span class="filter-value filter-attr-name">
                                {{ $t(attrGroup.groupName || '') }}
                                <UIcon name="i-heroicons-chevron-down" class="attr-dropdown-icon" />
                            </span>

                            <!-- 鼠标移入时显示的悬浮面板 -->
                            <transition name="fade-slide">
                                <div v-if="attrGroup.groupName && hoveredMoreAttrGroup === attrGroup.groupName"
                                    class="attr-hover-panel"
                                    @mouseenter.stop="attrGroup.groupName && handleMoreAttrPanelEnter(attrGroup.groupName)"
                                    @mouseleave.stop="attrGroup.groupName && handleMoreAttrMouseLeave(attrGroup.groupName)">
                                    <!-- 面板头部 -->
                                    <div class="attr-hover-header">
                                        <div class="attr-hover-title">{{ $t(attrGroup.groupName || '') }}</div>
                                        <button v-if="(attrGroup.goodsAttributeList?.length || 0) > 5"
                                            class="attr-multi-select-btn"
                                            @click.stop="attrGroup.groupName && handleAttrMultiSelectChange(attrGroup.groupName, !attrMultiSelectMap[attrGroup.groupName || ''])">
                                            {{ attrMultiSelectMap[attrGroup.groupName || ''] ? $t('收起') : $t('多选') }}
                                        </button>
                                    </div>

                                    <!-- 普通模式：显示所有属性值 -->
                                    <template v-if="!attrMultiSelectMap[attrGroup.groupName || '']">
                                        <div class="attr-hover-content">
                                            <span v-for="attr in attrGroup.goodsAttributeList" :key="attr.id || 0"
                                                class="attr-hover-item"
                                                :class="{ 'is-selected': attr.id && isAttrSelected(attrGroup.groupName || '', attr.id) }"
                                                @click.stop="attr.id && attr.attributeValueName && handleAttrClick(attrGroup.groupName || '', attr.id, attr.attributeValueName)">
                                                {{ attr.attributeValueName }}
                                            </span>
                                        </div>
                                    </template>

                                    <!-- 多选模式：显示所有属性值，带复选框 -->
                                    <template v-else>
                                        <div class="attr-hover-content">
                                            <label v-for="attr in attrGroup.goodsAttributeList" :key="attr.id || 0"
                                                class="attr-hover-item attr-hover-item--checkbox"
                                                :class="{ 'is-selected': attrGroup.groupName && attr.id && getTempAttrSelected(attrGroup.groupName, attr.id) }">
                                                <input type="checkbox"
                                                    :checked="attrGroup.groupName && attr.id ? getTempAttrSelected(attrGroup.groupName, attr.id) : false"
                                                    @change.stop="attrGroup.groupName && attr.id && handleAttrCheckboxClick(attrGroup.groupName, attr.id)" />
                                                <span class="attr-checkbox-label">{{ attr.attributeValueName }}</span>
                                            </label>
                                        </div>

                                        <!-- 已选择属性显示 -->
                                        <div v-if="getTempAttrSelectedCount(attrGroup.groupName) > 0"
                                            class="attr-selected-list">
                                            <div class="selected-label">已选择：</div>
                                            <div class="selected-items">
                                                <span
                                                    v-for="attrId in tempSelectedAttrIds[attrGroup.groupName || ''] || []"
                                                    :key="attrId" class="selected-item">
                                                    {{ getAttrNameById(attrGroup.groupName || '', attrId) }}
                                                </span>
                                            </div>
                                        </div>

                                        <!-- 确认/取消按钮 -->
                                        <div class="attr-action-buttons">
                                            <button class="action-btn action-btn--confirm"
                                                @click.stop="attrGroup.groupName && handleAttrConfirm(attrGroup.groupName)">
                                                {{ $t('确认') }}
                                            </button>
                                            <button class="action-btn action-btn--cancel"
                                                @click.stop="attrGroup.groupName && handleAttrCancel(attrGroup.groupName)">
                                                {{ $t('取消') }}
                                            </button>
                                        </div>
                                    </template>
                                </div>
                            </transition>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import type { GoodsCatResultVO, GoodsBrandVO, GoodsAttributeGroupVO } from '~/types/goods'

/**
 * 组件 Props
 */
interface Props {
    lineSaleCategories: GoodsCatResultVO[]
    lineBrands: GoodsBrandVO[]
    attrs: GoodsAttributeGroupVO[]
    selectedCatIds: number[]
    selectedBrandIds: number[]
    selectedAttrs: Record<string, number[]>
    brandMultiSelect: boolean
    attrMultiSelectMap: Record<string, boolean>
}

const props = defineProps<Props>()

/**
 * 组件事件
 */
const emit = defineEmits<{
    categoryChange: [catId: number]
    brandChange: [brandId: number]
    brandMultiChange: [brandIds: number[]]
    attrChange: [groupName: string, attrId: number, attrValueName: string]
    brandMultiSelectChange: [value: boolean]
    attrMultiSelectChange: [groupName: string, value: boolean]
}>()

/**
 * 处理属性多选变化
 * @param groupName 属性组名称
 * @param value 是否多选
 */
const handleAttrMultiSelectChange = (groupName: string, value: boolean): void => {
    if (value) {
        // 进入多选模式，初始化临时选择列表
        if (!tempSelectedAttrIds.value[groupName]) {
            tempSelectedAttrIds.value[groupName] = [...(props.selectedAttrs[groupName] || [])]
        }
    }
    emit('attrMultiSelectChange', groupName, value)
}

/**
 * 处理属性复选框点击（多选模式）
 * @param groupName 属性组名称
 * @param attrId 属性ID
 */
const handleAttrCheckboxClick = (groupName: string, attrId: number): void => {
    if (!tempSelectedAttrIds.value[groupName]) {
        tempSelectedAttrIds.value[groupName] = []
    }
    const index = tempSelectedAttrIds.value[groupName].indexOf(attrId)
    if (index > -1) {
        tempSelectedAttrIds.value[groupName].splice(index, 1)
    } else {
        tempSelectedAttrIds.value[groupName].push(attrId)
    }
}

/**
 * 获取临时属性选择状态
 * @param groupName 属性组名称
 * @param attrId 属性ID
 * @returns 是否选中
 */
const getTempAttrSelected = (groupName: string, attrId: number): boolean => {
    return tempSelectedAttrIds.value[groupName]?.includes(attrId) || false
}

/**
 * 获取临时属性选择数量
 * @param groupName 属性组名称
 * @returns 选择数量
 */
const getTempAttrSelectedCount = (groupName: string | undefined): number => {
    if (!groupName) return 0
    return tempSelectedAttrIds.value[groupName]?.length || 0
}

/**
 * 根据ID获取属性名称
 * @param groupName 属性组名称
 * @param attrId 属性ID
 * @returns 属性名称
 */
const getAttrNameById = (groupName: string, attrId: number): string => {
    const attrGroup = props.attrs.find(g => g.groupName === groupName)
    if (attrGroup?.goodsAttributeList) {
        const attr = attrGroup.goodsAttributeList.find(a => a.id === attrId)
        return attr?.attributeValueName || ''
    }
    return ''
}

/**
 * 处理属性确认（多选模式）
 * @param groupName 属性组名称
 */
const handleAttrConfirm = (groupName: string): void => {
    // 将临时选择应用到实际选择
    const selectedIds = tempSelectedAttrIds.value[groupName] || []
    const attrGroup = props.attrs.find(g => g.groupName === groupName)

    if (attrGroup && attrGroup.goodsAttributeList) {
        const currentSelected = props.selectedAttrs[groupName] || []

        // 找出需要添加的属性
        selectedIds.forEach(attrId => {
            if (!currentSelected.includes(attrId)) {
                const attr = attrGroup.goodsAttributeList?.find(a => a.id === attrId)
                if (attr && attr.attributeValueName) {
                    emit('attrChange', groupName, attrId, attr.attributeValueName)
                }
            }
        })

        // 找出需要移除的属性
        currentSelected.forEach(attrId => {
            if (!selectedIds.includes(attrId)) {
                const attr = attrGroup.goodsAttributeList?.find(a => a.id === attrId)
                if (attr && attr.attributeValueName) {
                    emit('attrChange', groupName, attrId, attr.attributeValueName)
                }
            }
        })
    }

    // 取消多选模式
    emit('attrMultiSelectChange', groupName, false)
    tempSelectedAttrIds.value[groupName] = []
}

/**
 * 处理属性取消（多选模式）
 * @param groupName 属性组名称
 */
const handleAttrCancel = (groupName: string): void => {
    emit('attrMultiSelectChange', groupName, false)
    tempSelectedAttrIds.value[groupName] = []
}

// 显示控制
const showCategoryMore = ref(false)
const showBrandMore = ref(false)
const hoveredMoreAttrGroup = ref<string>('') // 鼠标悬停的更多属性组
const moreAttrLeaveTimer = ref<ReturnType<typeof setTimeout> | null>(null) // 延迟隐藏定时器

// 品牌字母筛选
const selectedBrandLetter = ref<string>('all')
const brandAlphabetList = ref<string[]>([])
const tempSelectedBrandIds = ref<number[]>([]) // 多选模式下的临时选择
const isAlphabetDragging = ref(false) // 是否正在拖动字母筛选

// 属性多选临时选择（每个属性组一个）
const tempSelectedAttrIds = ref<Record<string, number[]>>({}) // { groupName: [attrId1, attrId2] }

// 多选模式（使用 props，通过事件通知父组件）
const brandMultiSelect = computed(() => props.brandMultiSelect)
const attrMultiSelectMap = computed(() => props.attrMultiSelectMap)

/**
 * 显示的分类（未展开时显示更多，约20个左右）
 */
const displayCategories = computed(() => {
    // 未展开时显示更多分类，约20个左右
    return props.lineSaleCategories.slice(0, 20)
})

/**
 * 显示的品牌（未展开时显示更多品牌，约20个左右）
 */
const displayBrands = computed(() => {
    // 未展开时显示更多品牌，约20个左右，让用户可以查看更多
    return props.lineBrands.slice(0, 20)
})

/**
 * 更多品牌
 */
const moreBrands = computed(() => {
    return props.lineBrands.slice(3)
})

/**
 * 初始化品牌字母列表
 * 从品牌列表中提取首字母，用于字母筛选
 */
const initBrandAlphabet = (): void => {
    const letters = new Set<string>()
    props.lineBrands.forEach(brand => {
        if (brand.brandKey && brand.brandKey.length > 0) {
            const firstLetter = brand.brandKey.charAt(0).toUpperCase()
            if (/[A-Z]/.test(firstLetter)) {
                letters.add(firstLetter)
            }
        }
    })
    brandAlphabetList.value = Array.from(letters).sort()
}

/**
 * 根据字母筛选品牌
 * 根据选中的字母筛选品牌列表
 * @returns 筛选后的品牌列表
 */
const filteredBrands = computed(() => {
    let brands = props.lineBrands

    // 如果处于展开或多选模式，显示所有品牌
    if (showBrandMore.value || brandMultiSelect) {
        brands = props.lineBrands
    }
    // 未展开模式不在这里处理，由 displayBrands 处理

    // 字母筛选（仅在展开或多选模式下生效）
    if ((showBrandMore.value || brandMultiSelect) && selectedBrandLetter.value !== 'all') {
        brands = brands.filter(brand => {
            let firstLetter = ''
            // 优先使用 brandKey
            if (brand.brandKey && brand.brandKey.length > 0) {
                firstLetter = brand.brandKey.charAt(0).toUpperCase()
            }
            // 如果没有 brandKey，从品牌名称提取首字母
            else if (brand.brandName && brand.brandName.length > 0) {
                const name = brand.brandName.trim()
                // 如果首字符是英文，直接使用
                if (/[A-Za-z]/.test(name.charAt(0))) {
                    firstLetter = name.charAt(0).toUpperCase()
                }
                // 如果是中文，尝试从括号中提取英文（如 "博世 (BOSCH)"）
                else {
                    const match = name.match(/\(([A-Za-z]+)\)/)
                    if (match && match[1]) {
                        firstLetter = match[1].charAt(0).toUpperCase()
                    }
                }
            }

            return firstLetter === selectedBrandLetter.value
        })
    }

    return brands
})

/**
 * 根据ID获取品牌名称
 * @param brandId 品牌ID
 * @returns 品牌名称
 */
const getBrandNameById = (brandId: number): string => {
    const brand = props.lineBrands.find(b => b.brandId === brandId)
    return brand?.brandName || ''
}

/**
 * 显示的属性组（前三个）
 */
const displayAttrGroups = computed(() => {
    return props.attrs.slice(0, 3)
})

/**
 * 更多属性组
 */
const moreAttrGroups = computed(() => {
    return props.attrs.slice(3)
})

/**
 * 处理分类点击
 * @param catId 分类ID
 */
const handleCategoryClick = (catId: number): void => {
    emit('categoryChange', catId)
}

/**
 * 处理品牌点击
 * @param brandId 品牌ID
 */
const handleBrandClick = (brandId: number): void => {
    emit('brandChange', brandId)
}

/**
 * 处理属性点击
 * @param groupName 属性组名称
 * @param attrId 属性ID
 * @param attrValueName 属性值名称
 */
const handleAttrClick = (groupName: string, attrId: number, attrValueName: string): void => {
    emit('attrChange', groupName, attrId, attrValueName)
}

/**
 * 检查属性是否选中
 * @param groupName 属性组名称
 * @param attrId 属性ID
 * @returns 是否选中
 */
const isAttrSelected = (groupName: string, attrId: number): boolean => {
    return props.selectedAttrs[groupName]?.includes(attrId) || false
}

/**
 * 处理更多属性鼠标移入
 */
const handleMoreAttrMouseEnter = (groupName: string) => {
    // 清除之前的定时器
    if (moreAttrLeaveTimer.value) {
        clearTimeout(moreAttrLeaveTimer.value)
        moreAttrLeaveTimer.value = null
    }
    hoveredMoreAttrGroup.value = groupName
}

/**
 * 处理更多属性面板鼠标移入（保持显示）
 */
const handleMoreAttrPanelEnter = (groupName: string) => {
    // 清除之前的定时器
    if (moreAttrLeaveTimer.value) {
        clearTimeout(moreAttrLeaveTimer.value)
        moreAttrLeaveTimer.value = null
    }
    hoveredMoreAttrGroup.value = groupName
}

/**
 * 处理更多属性鼠标移出
 */
const handleMoreAttrMouseLeave = (groupName: string) => {
    // 使用延迟，确保鼠标能够移动到面板上
    if (moreAttrLeaveTimer.value) {
        clearTimeout(moreAttrLeaveTimer.value)
    }
    moreAttrLeaveTimer.value = setTimeout(() => {
        if (hoveredMoreAttrGroup.value === groupName) {
            hoveredMoreAttrGroup.value = ''
        }
        moreAttrLeaveTimer.value = null
    }, 150)
}

/**
 * 处理品牌展开
 */
const handleBrandExpand = (): void => {
    showBrandMore.value = !showBrandMore.value
    if (showBrandMore.value) {
        initBrandAlphabet()
        selectedBrandLetter.value = 'all'
    }
}

/**
 * 处理品牌字母筛选
 * @param letter 字母
 */
const handleBrandLetterFilter = (letter: string): void => {
    selectedBrandLetter.value = letter
}

/**
 * 处理字母筛选栏鼠标按下
 */
const handleAlphabetMouseDown = (): void => {
    isAlphabetDragging.value = true
}

/**
 * 处理字母筛选栏鼠标移动
 */
const handleAlphabetMouseMove = (): void => {
    // 移动时保持拖动状态
}

/**
 * 处理字母筛选栏鼠标释放
 */
const handleAlphabetMouseUp = (): void => {
    isAlphabetDragging.value = false
}

/**
 * 切换品牌多选模式
 */
const toggleBrandMultiSelect = (): void => {
    emit('brandMultiSelectChange', true)
    initBrandAlphabet()
    selectedBrandLetter.value = 'all'
    // 初始化临时选择列表
    tempSelectedBrandIds.value = [...props.selectedBrandIds]
}

/**
 * 处理品牌复选框点击（多选模式）
 * @param brandId 品牌ID
 */
const handleBrandCheckboxClick = (brandId: number): void => {
    const index = tempSelectedBrandIds.value.indexOf(brandId)
    if (index > -1) {
        tempSelectedBrandIds.value.splice(index, 1)
    } else {
        tempSelectedBrandIds.value.push(brandId)
    }
}

/**
 * 处理品牌确认（多选模式）
 */
const handleBrandConfirm = (): void => {
    // 一次性设置所有选中的品牌
    emit('brandMultiChange', [...tempSelectedBrandIds.value])

    // 取消多选模式
    emit('brandMultiSelectChange', false)
    tempSelectedBrandIds.value = []
    selectedBrandLetter.value = 'all'
}

/**
 * 处理品牌取消（多选模式）
 */
const handleBrandCancel = (): void => {
    emit('brandMultiSelectChange', false)
    tempSelectedBrandIds.value = []
    selectedBrandLetter.value = 'all'
}



// 监听品牌列表变化，初始化字母列表
watch(() => props.lineBrands, () => {
    if (props.lineBrands.length > 0) {
        initBrandAlphabet()
    }
}, { immediate: true })

// 组件挂载时初始化
onMounted(() => {
    if (props.lineBrands.length > 0) {
        initBrandAlphabet()
    }
})
</script>

<style lang="scss" scoped>
.filter-section {
    width: 100%;
    box-sizing: border-box;
    font-size: 12px;
    margin-top: 0;
    background: color-mix(in srgb, var(--color) 5%, white);
    border-bottom: 1px solid #fff;
    border-radius: 0 0 5px 5px;
    padding: 10px 0 0;
    overflow: visible;

    .filter-result {
        font-size: 14px;
        color: #333;
        margin-bottom: 12px;
        font-weight: 500;
        padding: 0 15px;
    }

    .filter-items {
        .filter-item {
            width: 100%;
            min-height: 40px;
            border-bottom: 1px solid color-mix(in srgb, var(--color) 5%, white);
            display: flex;
            padding: 0;
            transition: background-color 0.2s;
            position: relative;
            overflow: visible;

            &:hover {
                background-color: color-mix(in srgb, var(--color) 2%, white);
            }

            &:last-child {
                border-bottom: none;
            }

            .filter-label {
                width: 80px;
                min-width: 80px;
                line-height: 53px;
                font-size: 12px;
                font-weight: bold;
                text-align: center;
                padding: 0;
                color: #333;
                flex-shrink: 0;
            }

            .filter-values {
                flex: 1;
                min-width: 0;
                min-height: 40px;
                background-color: #fff;
                display: flex;
                flex-wrap: wrap;
                justify-content: flex-start;
                position: relative;
                padding: 10px;
                gap: 8px;
                align-items: flex-start;
                overflow: visible;

                .filter-values-content {
                    flex: 1;
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;
                    align-items: flex-start;
                    overflow: visible;
                    position: relative;

                    .filter-value {
                        display: inline-flex;
                        align-items: center;
                        padding: 4px 12px;
                        font-size: 12px;
                        color: #333;
                        background-color: transparent;
                        border: 1px solid transparent;
                        border-radius: 4px;
                        cursor: pointer;
                        transition: all 0.2s ease;
                        white-space: nowrap;
                        margin: 0;
                        user-select: none;

                        &:hover {
                            color: var(--color);
                            border-color: var(--color);
                            background-color: transparent;
                        }

                        &.active {
                            color: var(--color);
                            background-color: transparent;
                            border-color: var(--color);
                            font-weight: 500;
                        }

                        &.filter-attr-name {
                            display: inline-flex;
                            align-items: center;
                            gap: 6px;
                            white-space: nowrap;
                            margin: 0;

                            .attr-dropdown-icon {
                                width: 12px;
                                height: 12px;
                                transition: transform 0.2s ease-in-out;
                                color: inherit;
                            }

                            &.active .attr-dropdown-icon {
                                transform: rotate(180deg);
                            }
                        }

                        // 复选框模式
                        &--checkbox {
                            display: flex;
                            align-items: center;
                            gap: 6px;
                            padding: 4px 12px;
                            margin: 0;
                            cursor: pointer;
                            background-color: transparent;
                            border: 1px solid transparent;

                            input[type="checkbox"] {
                                width: 16px;
                                height: 16px;
                                margin: 0;
                                cursor: pointer;
                                accent-color: var(--color);
                                flex-shrink: 0;
                            }

                            .checkbox-label {
                                flex: 1;
                                cursor: pointer;
                                user-select: none;
                            }

                            &:hover {
                                background-color: transparent;
                                border-color: var(--color);
                                color: #333;

                                &.is-selected {
                                    background-color: transparent;
                                    color: var(--color);
                                }
                            }

                            &.is-selected {
                                color: var(--color);
                                background-color: transparent;
                                border-color: var(--color);
                                font-weight: 500;

                                .checkbox-label {
                                    color: var(--color);
                                }
                            }
                        }
                    }

                    /* 品牌字母筛选栏 */
                    .brand-alphabet-filter {
                        width: 100%;
                        display: flex;
                        flex-wrap: wrap;
                        gap: 6px;
                        margin-bottom: 12px;
                        padding: 0 15px;
                        user-select: none;

                        .alphabet-item {
                            min-width: 32px;
                            height: 28px;
                            line-height: 28px;
                            text-align: center;
                            padding: 0 10px;
                            font-size: 12px;
                            font-weight: 500;
                            color: #666;
                            cursor: pointer;
                            border: 1px solid #e8e8e8;
                            border-radius: 4px;
                            background-color: #f8f8f8;
                            transition: all 0.2s ease;

                            &:hover {
                                border-color: var(--color);
                                color: var(--color);
                                background-color: color-mix(in srgb, var(--color) 5%, white);
                                transform: translateY(-1px);
                                box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 10%, transparent);
                            }

                            &.active {
                                border-color: var(--color);
                                color: #fff;
                                background-color: var(--color);
                                font-weight: 600;
                                box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 20%, transparent);
                            }
                        }
                    }

                    /* 品牌列表容器 */
                    .brand-list-container {
                        width: 100%;
                        display: flex;
                        flex-wrap: wrap;
                        gap: 8px;
                        padding: 0 15px;
                        align-items: flex-start;
                    }

                    /* 已选择品牌/属性列表 */
                    .brand-selected-list,
                    .attr-selected-list {
                        width: 100%;
                        padding: 12px 15px;
                        border-top: 1px solid #f0f0f0;
                        margin-top: 12px;
                        background-color: #fafafa;

                        .selected-label {
                            font-size: 12px;
                            color: #666;
                            margin-bottom: 8px;
                            font-weight: 500;
                        }

                        .selected-items {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 6px;
                        }

                        .selected-brand-item,
                        .selected-item {
                            display: inline-block;
                            padding: 4px 10px;
                            font-size: 12px;
                            color: var(--color);
                            background-color: color-mix(in srgb, var(--color) 5%, white);
                            border: 1px solid color-mix(in srgb, var(--color) 10%, white);
                            border-radius: 4px;
                            transition: all 0.2s ease;

                            &:hover {
                                background-color: color-mix(in srgb, var(--color) 10%, white);
                                border-color: var(--color);
                            }
                        }
                    }

                    /* 品牌/属性操作按钮 */
                    .brand-action-buttons,
                    .attr-action-buttons {
                        width: 100%;
                        display: flex;
                        justify-content: flex-end;
                        gap: 10px;
                        padding: 12px 15px;
                        border-top: 1px solid #f0f0f0;
                        margin-top: 12px;
                        background-color: #fafafa;

                        .action-btn {
                            padding: 6px 20px;
                            height: 32px;
                            border-radius: 4px;
                            font-size: 12px;
                            font-weight: 500;
                            cursor: pointer;
                            transition: all 0.2s ease;
                            white-space: nowrap;
                            border: 1px solid transparent;
                            user-select: none;

                            &--confirm {
                                background-color: var(--color);
                                color: #fff;
                                border-color: var(--color);

                                &:hover {
                                    background-color: color-mix(in srgb, var(--color) 90%, black);
                                    border-color: color-mix(in srgb, var(--color) 90%, black);
                                    transform: translateY(-1px);
                                    box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
                                }

                                &:active {
                                    transform: translateY(0);
                                }
                            }

                            &--cancel {
                                background-color: #fff;
                                color: #666;
                                border-color: #e5e5e5;

                                &:hover {
                                    color: var(--color);
                                    border-color: var(--color);
                                    background-color: color-mix(in srgb, var(--color) 5%, white);
                                }

                                &:active {
                                    transform: scale(0.98);
                                }
                            }
                        }
                    }
                }

                /* 右侧操作按钮区域 */
                .filter-actions {
                    display: flex;
                    flex-direction: column;
                    gap: 8px;
                    padding-right: 15px;
                    align-items: flex-end;
                    flex-shrink: 0;
                    justify-content: flex-start;
                    padding-top: 10px;

                    .filter-action-btn {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        gap: 4px;
                        min-width: 60px;
                        height: 28px;
                        padding: 0 12px;
                        background-color: #fff;
                        border: 1px solid #e5e5e5;
                        color: #666;
                        border-radius: 4px;
                        font-size: 12px;
                        font-weight: 500;
                        cursor: pointer;
                        transition: all 0.2s ease;
                        white-space: nowrap;
                        user-select: none;

                        .btn-icon {
                            width: 14px;
                            height: 14px;
                            flex-shrink: 0;
                            transition: transform 0.2s ease;
                        }

                        &:hover {
                            border-color: var(--color);
                            color: var(--color);
                            background-color: color-mix(in srgb, var(--color) 5%, white);
                            transform: translateY(-1px);
                            box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 10%, transparent);
                        }

                        &:active {
                            transform: translateY(0);
                            box-shadow: none;
                        }

                        // 展开按钮
                        &--expand {
                            &:hover {
                                .btn-icon {
                                    transform: translateY(1px);
                                }
                            }
                        }

                        // 收起按钮
                        &--collapse {
                            &:hover {
                                .btn-icon {
                                    transform: translateY(-1px);
                                }
                            }
                        }

                        // 多选按钮
                        &--multi {
                            &:hover {
                                background-color: color-mix(in srgb, var(--color) 8%, white);
                            }
                        }
                    }
                }
            }

            /* 分类筛选展开样式 */
            .category-filter-item {
                .filter-values-content {
                    width: 100%;
                }

                .category-expanded-container {
                    width: 100%;
                    display: grid;
                    grid-template-columns: 150px 1fr;
                    gap: 20px;
                    padding: 10px 0;
                    min-height: 200px;
                    align-items: start;

                    /* 左侧分类标题 */
                    .category-title-item {
                        font-size: 13px;
                        color: #333;
                        padding: 10px 0;
                        cursor: pointer;
                        transition: all 0.2s ease;
                        text-align: left;
                        font-weight: 500;
                        min-height: 40px;
                        display: flex;
                        align-items: center;
                        line-height: 1.5;
                        box-sizing: border-box;
                        border-right: 1px solid #f0f0f0;
                        padding-right: 15px;
                        border-radius: 4px;
                        margin-bottom: 4px;

                        &:hover {
                            color: var(--color);
                            background-color: color-mix(in srgb, var(--color) 5%, white);
                            padding-left: 8px;
                        }

                        &.active {
                            color: var(--color);
                            font-weight: 600;
                            background-color: color-mix(in srgb, var(--color) 8%, white);
                            padding-left: 8px;
                        }
                    }

                    /* 右侧子分类 */
                    .category-sub-group {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 6px;
                        align-items: flex-start;
                        min-height: 40px;
                        padding: 8px 0;
                        align-content: flex-start;
                        line-height: 1.5;
                        box-sizing: border-box;
                        padding-left: 10px;

                        .filter-value {
                            margin: 0;
                        }
                    }
                }
            }

            .filter-more-panel {
                width: calc(100% - 90px);
                margin-left: 80px;
                margin-top: 0;
                display: flex;
                flex-direction: column;
                gap: 0;
                padding: 10px;
                border-top: none;
                background-color: #fff;

                /* 品牌字母筛选栏（展开模式） */
                .brand-alphabet-filter {
                    width: 100%;
                    display: flex;
                    flex-wrap: wrap;
                    gap: 6px;
                    margin-bottom: 12px;
                    padding: 0 5px;
                    user-select: none;

                    .alphabet-item {
                        min-width: 32px;
                        height: 28px;
                        line-height: 28px;
                        text-align: center;
                        padding: 0 10px;
                        font-size: 12px;
                        font-weight: 500;
                        color: #666;
                        cursor: pointer;
                        border: 1px solid #e8e8e8;
                        border-radius: 4px;
                        background-color: #f8f8f8;
                        transition: all 0.2s ease;

                        &:hover {
                            border-color: var(--color);
                            color: var(--color);
                            background-color: color-mix(in srgb, var(--color) 5%, white);
                            transform: translateY(-1px);
                            box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 10%, transparent);
                        }

                        &.active {
                            border-color: var(--color);
                            color: #fff;
                            background-color: var(--color);
                            font-weight: 600;
                            box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 20%, transparent);
                        }
                    }
                }

                /* 品牌列表容器（展开模式） */
                .brand-list-container {
                    width: 100%;
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;
                    padding: 0 5px;
                    align-items: flex-start;

                    .filter-value {
                        margin: 0;
                    }
                }
            }
        }
    }

    /* 属性悬浮面板样式 */
    .attr-hover-panel {
        position: absolute;
        top: 100%;
        left: 0;
        z-index: 1000;
        background: #fff;
        border: 1px solid #e5e5e5;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        min-width: 300px;
        max-width: 600px;
        max-height: 400px;
        overflow-y: auto;
        padding: 10px;
        margin-top: 5px;
        pointer-events: auto;

        .attr-hover-header {
            display: flex;
            justify-content: flex-end;
            margin-bottom: 10px;
            padding-bottom: 10px;
            border-bottom: 1px solid #f0f0f0;

            .attr-multi-select-btn {
                width: 45px;
                height: 20px;
                background-color: #fff;
                border: 1px solid #e5e5e5;
                color: #666;
                border-radius: 2px;
                font-size: 12px;
                padding: 0;
                cursor: pointer;
                transition: all 0.3s;

                &:hover {
                    border: 1px solid var(--color);
                    color: var(--color);
                    background-color: #fff;
                }
            }
        }

    }

    /* 更多属性筛选样式 */
    .filter-item--more {
        .filter-attr-name-wrapper {
            position: relative;
            display: inline-block;
            margin: 0;

            &.is-active {
                .filter-attr-name {
                    color: var(--color);
                    border-color: var(--color);
                }

                .attr-dropdown-icon {
                    transform: rotate(180deg);
                }
            }

            .filter-attr-name {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 4px 12px;
                font-size: 12px;
                color: #333;
                background-color: transparent;
                border: 1px solid transparent;
                border-radius: 4px;
                transition: all 0.2s ease;
                cursor: pointer;
                white-space: nowrap;
                margin: 0;

                &:hover {
                    color: var(--color);
                    border-color: var(--color);
                    background-color: transparent;
                }

                &.active {
                    color: var(--color);
                    border-color: var(--color);
                    background-color: transparent;
                    font-weight: 500;
                }

                .attr-dropdown-icon {
                    width: 12px;
                    height: 12px;
                    transition: transform 0.2s ease;
                    color: inherit;
                }
            }
        }

        .attr-hover-panel {
            position: absolute;
            top: calc(100% + 8px);
            left: 0;
            z-index: 10000;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 8px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
            min-width: 320px;
            max-width: 600px;
            max-height: 480px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            pointer-events: auto;

            // 面板头部
            .attr-hover-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 12px 16px;
                border-bottom: 1px solid #f0f0f0;
                background-color: #fafafa;
                flex-shrink: 0;

                .attr-hover-title {
                    font-size: 13px;
                    font-weight: 600;
                    color: #333;
                }

                .attr-multi-select-btn {
                    padding: 4px 12px;
                    height: 24px;
                    background-color: #fff;
                    border: 1px solid #e5e5e5;
                    color: #666;
                    border-radius: 4px;
                    font-size: 12px;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    white-space: nowrap;

                    &:hover {
                        border-color: var(--color);
                        color: var(--color);
                        background-color: color-mix(in srgb, var(--color) 5%, white);
                    }

                    &:active {
                        transform: scale(0.98);
                    }
                }
            }

            // 内容区域
            .attr-hover-content {
                flex: 1;
                display: flex;
                flex-wrap: wrap;
                gap: 6px;
                padding: 12px 16px;
                overflow-y: auto;
                overflow-x: visible;
                align-items: flex-start;
                max-height: 360px;

                // 自定义滚动条
                &::-webkit-scrollbar {
                    width: 6px;
                }

                &::-webkit-scrollbar-track {
                    background: #f5f5f5;
                    border-radius: 3px;
                }

                &::-webkit-scrollbar-thumb {
                    background: #d0d0d0;
                    border-radius: 3px;

                    &:hover {
                        background: #b0b0b0;
                    }
                }

                // 属性项样式
                .attr-hover-item {
                    display: inline-flex;
                    align-items: center;
                    padding: 4px 12px;
                    font-size: 12px;
                    color: #333;
                    background-color: transparent;
                    border: 1px solid transparent;
                    border-radius: 4px;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    white-space: nowrap;
                    user-select: none;

                    &:hover {
                        color: var(--color);
                        border-color: var(--color);
                        background-color: transparent;
                    }

                    &.is-selected {
                        color: var(--color);
                        background-color: transparent;
                        border-color: var(--color);
                        font-weight: 500;
                    }

                    // 复选框模式
                    &--checkbox {
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        padding: 4px 12px;
                        margin: 0;
                        background-color: transparent;
                        border: 1px solid transparent;

                        input[type="checkbox"] {
                            width: 16px;
                            height: 16px;
                            margin: 0;
                            cursor: pointer;
                            accent-color: var(--color);
                        }

                        .attr-checkbox-label {
                            flex: 1;
                            cursor: pointer;
                        }

                        &:hover {
                            background-color: transparent;
                            border-color: var(--color);
                            color: #333;

                            &.is-selected {
                                background-color: transparent;
                                color: var(--color);
                            }
                        }

                        &.is-selected {
                            background-color: transparent;
                            border-color: var(--color);
                            color: var(--color);

                            .attr-checkbox-label {
                                color: var(--color);
                            }
                        }
                    }
                }
            }

            // 已选择列表
            .attr-selected-list {
                padding: 12px 16px;
                border-top: 1px solid #f0f0f0;
                background-color: #fafafa;
                flex-shrink: 0;

                .selected-label {
                    font-size: 12px;
                    color: #666;
                    margin-bottom: 8px;
                    font-weight: 500;
                }

                .selected-items {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 6px;

                    .selected-item {
                        display: inline-block;
                        padding: 4px 10px;
                        font-size: 12px;
                        color: var(--color);
                        background-color: color-mix(in srgb, var(--color) 5%, white);
                        border: 1px solid color-mix(in srgb, var(--color) 10%, white);
                        border-radius: 4px;
                    }
                }
            }

            // 操作按钮
            .attr-action-buttons {
                display: flex;
                justify-content: flex-end;
                gap: 10px;
                padding: 12px 16px;
                border-top: 1px solid #f0f0f0;
                background-color: #fafafa;
                flex-shrink: 0;

                .action-btn {
                    padding: 6px 20px;
                    height: 32px;
                    border-radius: 4px;
                    font-size: 12px;
                    font-weight: 500;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    white-space: nowrap;
                    border: 1px solid transparent;

                    &--confirm {
                        background-color: var(--color);
                        color: #fff;
                        border-color: var(--color);

                        &:hover {
                            background-color: color-mix(in srgb, var(--color) 90%, black);
                            border-color: color-mix(in srgb, var(--color) 90%, black);
                            transform: translateY(-1px);
                            box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
                        }

                        &:active {
                            transform: translateY(0);
                        }
                    }

                    &--cancel {
                        background-color: #fff;
                        color: #666;
                        border-color: #e5e5e5;

                        &:hover {
                            color: var(--color);
                            border-color: var(--color);
                            background-color: color-mix(in srgb, var(--color) 5%, white);
                        }

                        &:active {
                            transform: scale(0.98);
                        }
                    }
                }
            }
        }
    }

    // 过渡动画
    .fade-slide-enter-active,
    .fade-slide-leave-active {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .fade-slide-enter-from {
        opacity: 0;
        transform: translateY(-8px) scale(0.95);
    }

    .fade-slide-leave-to {
        opacity: 0;
        transform: translateY(-8px) scale(0.95);
    }

}
</style>