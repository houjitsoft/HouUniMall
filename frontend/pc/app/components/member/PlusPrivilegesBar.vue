<template>
    <div class="plus-privileges-bar" :class="{ 'plus-privileges-bar--no-title': !showTitle }" :style="barMergedStyle">
        <h3 v-if="showTitle" class="plus-privileges-title">
            <slot name="title">{{ $t(title || '') }}</slot>
        </h3>
        <div class="plus-privileges-icons">
            <div v-for="(eq, idx) in displayEquities" :key="eq.equityId ?? idx" class="plus-privilege-item">
                <img :src="privilegeIconSrc(eq)" alt="" class="plus-privilege-icon">
                <span class="plus-privilege-label">{{ $t(eq.equityName || '会员特权') }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { Comment, Fragment, Text, computed, useSlots, type CSSProperties, type VNode } from 'vue'
import type { PlusEquity } from '~/types/plus'

const slots = useSlots()

/**
 * 判断插槽渲染结果是否包含可见内容（排除注释、空白文本、空 Fragment）。
 * @param nodes 插槽 vnode 数组
 * @returns 是否有可见内容
 */
function vnodesHaveContent(nodes: unknown): boolean {
    if (!Array.isArray(nodes) || nodes.length === 0) return false
    for (const n of nodes as VNode[]) {
        if (n == null) continue
        if (n.type === Comment) continue
        if (n.type === Text) {
            const raw =
                typeof n.children === 'string'
                    ? n.children
                    : Array.isArray(n.children)
                        ? (n.children as string[]).join('')
                        : ''
            if (String(raw).trim() !== '') return true
            continue
        }
        if (n.type === Fragment) {
            if (vnodesHaveContent(n.children)) return true
            continue
        }
        return true
    }
    return false
}

const props = withDefaults(
    defineProps<{
        /** 标题文案；与 #title 均未提供时不渲染标题区 */
        title?: string
        /** 权益条背景等样式（如 `{ backgroundImage: url(...) }`），可与默认样式合并 */
        backgroundStyle?: CSSProperties
        /** 要展示的权益列表（建议父级已按业务截取条数，如前 5 项） */
        equities: PlusEquity[]
        /** 最多展示图标数量，多于该数量时截断 */
        maxIcons?: number
    }>(),
    {
        title: '',
        backgroundStyle: undefined,
        maxIcons: 5
    }
)

/** 非空 title 文案，或 #title 插槽渲染出可见内容时才显示标题区（避免仅因插槽函数存在而误判） */
const showTitle = computed(() => {
    const t = props.title != null ? String(props.title).trim() : ''
    if (t !== '') return true
    const render = slots.title
    if (typeof render !== 'function') return false
    return vnodesHaveContent(render())
})

const barMergedStyle = computed(() => {
    const base: CSSProperties = {
        backgroundSize: '100% 100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat'
    }
    return { ...base, ...(props.backgroundStyle ?? {}) }
})

const displayEquities = computed(() => {
    const list = props.equities ?? []
    const n = props.maxIcons
    if (typeof n === 'number' && n > 0) return list.slice(0, n)
    return list
})

function equityTypeNum(eq: PlusEquity): number {
    const num = Number(eq.equityType)
    return Number.isFinite(num) ? num : Number.NaN
}

const PRIVILEGE_ICON_BY_TYPE: Record<number, string> = {
    1: 'plusImg/quanyi.png',
    2: 'plusImg/wumenkan.png',
    3: 'plusImg/redbaoquan.png',
    4: 'plusImg/jifennum.png',
    5: 'plusImg/gouwujifen.png'
}

/**
 * 根据权益类型返回概览区图标地址。
 * @param eq 单条权益
 * @returns 完整图片 URL
 */
function privilegeIconSrc(eq: PlusEquity): string {
    const n = equityTypeNum(eq)
    const file: string =
        (n >= 1 && n <= 5 && PRIVILEGE_ICON_BY_TYPE[n]) || PRIVILEGE_ICON_BY_TYPE[1] || 'plusImg/quanyi.png'
    return IMAGE_URL(file)
}
</script>

<style scoped lang="scss">
.plus-privileges-bar {
    position: relative;
    display: flex;
    flex-direction: column;
    width: 100%;
    height: auto;
    min-height: clamp(220px, 36vw, 280px);
    background: transparent;
    border-radius: var(--plus-radius-lg);
    padding: 0;
    margin-top: 24px;
    margin-bottom: 24px;
    border: 1px solid rgba(255, 255, 255, 0.06);
    overflow: hidden;
}

.plus-privileges-title {
    margin: 0;
    flex: 0 0 auto;
    min-height: 96px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: clamp(16px, 2.5vw, 28px) clamp(18px, 3vw, 36px);
    font-size: 32px;
    font-weight: 800;
    background: linear-gradient(180deg, #f0dbc5 0%, #cc9b7b 100%);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    text-align: center;
    letter-spacing: 0.06em;
    position: relative;
}

.plus-privileges-title::after {
    content: '';
    position: absolute;
    left: clamp(18px, 3vw, 36px);
    right: clamp(18px, 3vw, 36px);
    bottom: 0;
    height: 1px;
    background: linear-gradient(270deg, rgba(84, 97, 133, 1), rgba(143, 157, 190, 1));
}

.plus-privileges-icons {
    flex: 1 1 auto;
    display: flex;
    flex-wrap: wrap;
    justify-content: space-around;
    align-items: center;
    align-content: center;
    gap: 18px 28px;
    padding: clamp(8px, 1.4vw, 14px) clamp(18px, 3vw, 36px) clamp(12px, 1.8vw, 20px);
    box-sizing: border-box;
    min-height: 0;
}

.plus-privileges-bar--no-title .plus-privileges-icons {
    padding-top: clamp(12px, 2vw, 22px);
    padding-bottom: clamp(12px, 2vw, 22px);
}

.plus-privilege-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 180px;
    text-align: center;
}

.plus-privilege-icon {
    width: 82px;
    height: 82px;
    margin: 0 auto 10px;
    object-fit: contain;
    display: block;
    pointer-events: none;
}

.plus-privilege-label {
    max-width: 180px;
    font-size: 24px;
    line-height: 1.2;
    color: rgba(232, 220, 190, 0.88);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

@media (max-width: 900px) {
    .plus-privileges-bar {
        min-height: 0;
    }
}
</style>
