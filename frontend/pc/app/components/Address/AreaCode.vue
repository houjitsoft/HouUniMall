<template>
    <div class="area-code-overlay" @click="handleClose">
        <div class="area-code-container" @click.stop>
            <!-- 头部 -->
            <div class="header">
                <span class="title">{{ $t('选择国家/地区') }}</span>
                <i class="icon-close" @click="handleClose"></i>
            </div>

            <!-- 搜索框 -->
            <div class="search-box">
                <i class="icon-search"></i>
                <input v-model="searchText" type="text" :placeholder="$t('搜索您的国家和地区')" class="search-input"
                    @input="handleSearch" />
            </div>

            <!-- 字母索引 -->
            <div class="letter-index">
                <span v-for="letter in letterList" :key="letter" class="letter-item"
                    :class="{ active: activeLetter === letter }" @click="handleLetterClick(letter)">
                    {{ letter }}
                </span>
            </div>

            <!-- 国家和地区列表 -->
            <div class="area-list" ref="areaListContainer">
                <!-- 加载动画 -->
                <Loading v-if="loading" :text="$t('加载中...')" compact />

                <!-- 内容区域（加载完成后显示） -->
                <template v-else>
                    <!-- 搜索结果 -->
                    <template v-if="searchText">
                        <div v-if="searchResults.length > 0" class="area-group">
                            <div class="group-title">{{ $t('搜索结果') }}</div>
                            <div v-for="area in searchResults" :key="`${area.code}-${area.name}`" class="area-item"
                                @click="handleAreaClick(area)">
                                <span class="area-name">{{ $t(area.name || '') }}</span>
                                <span class="area-code">{{ area.code }}</span>
                            </div>
                        </div>
                        <div v-else class="no-result">
                            {{ $t('未找到相关国家或地区') }}
                        </div>
                    </template>

                    <!-- 国家列表（无搜索时显示） -->
                    <template v-else>
                        <!-- 按字母筛选的国家列表 -->
                        <template v-if="activeLetter">
                            <div v-if="filteredAreasByLetter.length > 0" class="area-group">
                                <div class="group-title">{{ activeLetter }}</div>
                                <div class="common-areas-scroll">
                                    <div v-for="area in filteredAreasByLetter" :key="`${area.code}-${area.name}`"
                                        class="area-item" @click="handleAreaClick(area)">
                                        <span class="area-name">{{ $t(area.name || '') }}</span>
                                        <span class="area-code">{{ area.code }}</span>
                                    </div>
                                </div>
                            </div>
                            <div v-else class="no-result">
                                {{ $t('该字母下无国家或地区') }}
                            </div>
                        </template>

                        <!-- 无字母筛选时：显示常用国家 + 所有国家按字母分组 -->
                        <template v-else>
                            <!-- 常用国家 -->
                            <div v-if="commonAreasGroup.length > 0" class="area-group">
                                <div class="group-title">{{ $t('常用国家/地区') }}</div>
                                <div class="common-areas-scroll">
                                    <div v-for="area in commonAreasGroup" :key="`common-${area.code}-${area.name}`"
                                        class="area-item" @click="handleAreaClick(area)">
                                        <span class="area-name">{{ $t(area.name || '') }}</span>
                                        <span class="area-code">{{ area.code }}</span>
                                    </div>
                                </div>
                            </div>

                            <!-- 所有国家按字母分组显示 -->
                            <template v-for="letter in Object.keys(groupedAreas).sort()" :key="letter">
                                <div class="area-group">
                                    <div class="group-title">{{ letter }}</div>
                                    <div class="common-areas-scroll">
                                        <div v-for="area in groupedAreas[letter]" :key="`${area.code}-${area.name}`"
                                            class="area-item" @click="handleAreaClick(area)">
                                            <span class="area-name">{{ $t(area.name || '') }}</span>
                                            <span class="area-code">{{ area.code }}</span>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </template>
                    </template>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import { queryCountryAndCurrency } from '~/services/base.service'
import type { Area, CountryEntity } from '~/types/address'

// 定义事件
const emit = defineEmits<{
    (e: 'select', area: Area): void
    (e: 'close'): void
}>()

// 国家列表
const commonAreas = ref<Area[]>([])
const allAreas = ref<Area[]>([])
const dataLoaded = ref(false)
const loading = ref(false)

// 字母索引 - 包含所有国家涉及到的字母
const letterList = computed(() => {
    const letters = new Set<string>()
    allAreas.value.forEach(area => {
        if (area.pinyin) {
            letters.add(area.pinyin.toUpperCase())
        }
    })
    return Array.from(letters).sort()
})

// 按字母分组的国家列表
const groupedAreas = computed(() => {
    const groups: Record<string, Area[]> = {}
    allAreas.value.forEach(area => {
        const letter = area.pinyin?.toUpperCase() || 'OTHER'
        if (!groups[letter]) {
            groups[letter] = []
        }
        groups[letter].push(area)
    })
    // 对每个分组内的国家按名称排序
    Object.keys(groups).forEach(letter => {
        if (groups[letter]) {
            groups[letter]!.sort((a, b) => a.name.localeCompare(b.name))
        }
    })
    return groups
})

// 获取常用国家
const commonAreasGroup = computed(() => {
    return commonAreas.value
})

// 搜索文本
const searchText = ref<string>('')
// 激活的字母
const activeLetter = ref<string>('')
// 地区列表容器
const areaListContainer = ref<HTMLElement | null>(null)

// 筛选后的国家列表（根据选中的字母）
const filteredAreasByLetter = computed(() => {
    if (!activeLetter.value) {
        return []
    }
    return groupedAreas.value[activeLetter.value] || []
})

// 搜索结果
const searchResults = computed(() => {
    if (!searchText.value) return []

    const query = searchText.value.toLowerCase()
    return allAreas.value.filter(area =>
        area.name.toLowerCase().includes(query) ||
        area.code.toLowerCase().includes(query) ||
        area.pinyin.toLowerCase().includes(query)
    ).sort((a, b) => a.name.localeCompare(b.name))
})

// 是否显示常用国家（已移除，改用模板中的 v-if）

// 处理字母点击
const handleLetterClick = (letter: string) => {
    if (activeLetter.value === letter) {
        activeLetter.value = '' // 再次点击同一字母取消筛选
    } else {
        activeLetter.value = letter // 点击其他字母进行筛选
    }
}

// 处理搜索
const handleSearch = () => {
    activeLetter.value = ''
}

// 处理地区点击
const handleAreaClick = (area: Area) => {
    // 触发父组件事件，传递选中的地区信息
    emit('select', area)
}

// 处理关闭
const handleClose = () => {
    emit('close')
}

// 加载国家数据
const loadCountryData = async () => {
    loading.value = true
    try {
        const response = await queryCountryAndCurrency({})
        if (response?.data?.baseCountryEntities) {
            const countries = response.data.baseCountryEntities as CountryEntity[]

            // 转换为组件需要的数据格式
            const areas: Area[] = countries
                .map(country => ({
                    name: country.countryName,
                    code: country.internationalDialingCode,
                    pinyin: country.countryInitial || getFirstLetter(country.countryName),
                    countryId: Number(country.id) || undefined
                }))

            allAreas.value = areas

            // 筛选常用国家（countryCommonUse 为 1 或 '1' 表示常用）
            commonAreas.value = areas.filter(area => {
                const country = countries.find(c =>
                    c.countryName === area.name &&
                    c.internationalDialingCode === area.code
                )
                if (!country) return false
                const commonUse = String(country.countryCommonUse)
                return commonUse === '1' || commonUse === 'true'
            })

            // 如果没有常用国家，至少显示前12个
            if (commonAreas.value.length === 0 && areas.length > 0) {
                commonAreas.value = areas.slice(0, 12)
            }

            dataLoaded.value = true
        }
    } catch (error) {
        console.error('加载国家数据失败:', error)
        dataLoaded.value = true
        // 如果接口失败，使用默认数据
        if (commonAreas.value.length === 0) {
            commonAreas.value = [
                { name: '中国', code: '+86', pinyin: 'Z', countryId: 1 },
                { name: '美国', code: '+1', pinyin: 'M' },
                { name: '英国', code: '+44', pinyin: 'Y' },
                { name: '澳大利亚', code: '+61', pinyin: 'A' },
                { name: '新加坡', code: '+65', pinyin: 'X' },
                { name: '日本', code: '+81', pinyin: 'R' },
                { name: '韩国', code: '+82', pinyin: 'H' },
                { name: '德国', code: '+49', pinyin: 'D' },
                { name: '法国', code: '+33', pinyin: 'F' },
                { name: '意大利', code: '+39', pinyin: 'Y' },
                { name: '西班牙', code: '+34', pinyin: 'X' },
                { name: '加拿大', code: '+1', pinyin: 'J' }
            ]
            allAreas.value = commonAreas.value
        }
    } finally {
        loading.value = false
    }
}

// 获取中文首字母（如果接口没有提供 countryInitial）
const getFirstLetter = (name: string): string => {
    // 简单的首字母提取，可以根据需要优化
    if (!name) return ''
    const firstChar = name.charAt(0)
    // 如果是中文，可以集成拼音库，这里先用简单处理
    // 如果是英文，直接返回大写首字母
    if (/[a-zA-Z]/.test(firstChar)) {
        return firstChar.toUpperCase()
    }
    // 中文或其他字符，返回第一个字符（实际项目中可以使用拼音库）
    return firstChar.toUpperCase()
}

onMounted(() => {
    loadCountryData()
})
</script>

<style lang="scss" scoped>
.area-code-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.area-code-container {
    width: 400px;
    max-width: 90%;
    max-height: 80%;
    background: #fff;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #e8e8e8;

    .title {
        font-size: 18px;
        font-weight: 500;
        color: #333;
    }

    .icon-close {
        width: 24px;
        height: 24px;
        cursor: pointer;
        color: #999;
        transition: color 0.3s;

        &:hover {
            color: #666;
        }

        &::before {
            content: '×';
            font-size: 24px;
            line-height: 24px;
        }
    }
}

.area-code-container .content {
    flex: 1;
    max-height: calc(80vh - 100px);
    overflow-y: auto;
    padding: 10px 0;
}

.search-box {
    position: relative;
    margin: 0 15px 15px;

    .icon-search {
        position: absolute;
        left: 12px;
        top: 50%;
        transform: translateY(-50%);
        color: #999;
        font-size: 16px;
    }

    .search-input {
        width: 100%;
        height: 40px;
        border: 1px solid #e8e8e8;
        border-radius: 4px;
        padding: 0 12px 0 40px;
        font-size: 14px;
        outline: none;
        transition: border-color 0.3s;

        &:focus {
            border-color: var(--color);
        }

        &::placeholder {
            color: #ccc;
        }
    }
}

.letter-index {
    display: flex;
    flex-wrap: wrap;
    margin: 0 15px 15px;
    gap: 8px;

    .letter-item {
        padding: 4px 8px;
        font-size: 14px;
        color: #666;
        cursor: pointer;
        border-radius: 4px;
        transition: all 0.3s;

        &:hover {
            background: #f0f0f0;
            color: var(--color);
        }

        &.active {
            background: var(--color);
            color: #fff;
        }
    }
}

.area-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 15px 15px;
    max-height: calc(80vh - 200px);

    .area-group {
        margin-bottom: 15px;

        .group-title {
            padding: 8px 15px;
            font-size: 14px;
            font-weight: 500;
            color: #333;
            background: #f5f5f5;
            border-left: 3px solid var(--color);
        }

        .common-areas-scroll {
            max-height: 250px;
            overflow-y: auto;

            &::-webkit-scrollbar {
                width: 4px;
            }

            &::-webkit-scrollbar-track {
                background: #f5f5f5;
                border-radius: 2px;
            }

            &::-webkit-scrollbar-thumb {
                background: #ccc;
                border-radius: 2px;

                &:hover {
                    background: #999;
                }
            }
        }

        .area-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 15px;
            font-size: 14px;
            color: #333;
            cursor: pointer;
            transition: background 0.3s;

            &:hover {
                background: #f0f0f0;
            }

            .area-name {
                flex: 1;
                margin-right: 10px;
            }

            .area-code {
                color: #666;
                font-weight: 500;
            }
        }
    }

    .no-result {
        text-align: center;
        padding: 20px;
        color: #999;
        font-size: 14px;
    }

}

// 滚动条样式
.area-code-container::-webkit-scrollbar {
    width: 6px;
}

.area-code-container::-webkit-scrollbar-track {
    background: #f5f5f5;
    border-radius: 3px;
}

.area-code-container::-webkit-scrollbar-thumb {
    background: #ccc;
    border-radius: 3px;

    &:hover {
        background: #999;
    }
}
</style>
