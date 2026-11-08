<template>
    <ul class="rail-menu" :data-depth="depth">
        <template v-for="node in nodes" :key="node.fullPath">
            <!-- 有下级 -->
            <li v-if="node.children.length" class="rail-menu__group">
                <!-- 顶层分组：菜单项样式（图标 + 标题 + 箭头） -->
                <button
                    v-if="isTop"
                    type="button"
                    class="rail-menu__item rail-menu__item--top"
                    :class="{ 'is-open': isOpen(node.fullPath), 'is-branch-active': containsActive(node) }"
                    @click="toggle(node.fullPath)"
                >
                    <span class="icon-slot">
                        <icon v-if="node.icon" :name="node.icon" :size="16" />
                        <span v-else class="dot" />
                    </span>
                    <span class="label">{{ node.title }}</span>
                    <icon class="caret" name="el-icon-ArrowRight" :size="12" />
                </button>

                <!-- 下级分组：小标题样式 -->
                <button
                    v-else
                    type="button"
                    class="rail-menu__caption"
                    :class="{ 'is-open': isOpen(node.fullPath) }"
                    @click="toggle(node.fullPath)"
                >
                    <icon class="caret" name="el-icon-ArrowRight" :size="11" />
                    <span class="label">{{ node.title }}</span>
                    <span class="count">{{ collectLeaves([node]).length }}</span>
                </button>

                <menu-tree
                    v-show="isOpen(node.fullPath)"
                    :nodes="node.children"
                    :depth="depth + 1"
                />
            </li>

            <!-- 叶子节点：直接跳转 -->
            <li v-else class="rail-menu__node">
                <app-link
                    :to="node.link"
                    class="rail-menu__link"
                    :class="{ 'is-active': isActive(node), 'rail-menu__link--top': isTop }"
                    @click="$emit('navigate')"
                >
                    <span class="icon-slot">
                        <icon v-if="node.icon" :name="node.icon" :size="16" />
                        <span v-else class="dot" />
                    </span>
                    <span class="label">{{ node.title }}</span>
                </app-link>
            </li>
        </template>
    </ul>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import useSettingStore from '@/stores/modules/setting'
import { useShellMenu, type ShellNode } from '../../composables/useShellMenu'

const props = withDefaults(
    defineProps<{
        nodes: ShellNode[]
        depth?: number
    }>(),
    { depth: 0 }
)

defineEmits(['navigate'])

const isTop = computed(() => props.depth === 0)
const { highlightPath, isActive, collectLeaves } = useShellMenu()
const settingStore = useSettingStore()

const containsActive = (node: ShellNode): boolean =>
    node.fullPath === highlightPath.value || node.children.some(containsActive)

const defaultOpen = () => props.nodes.filter(containsActive).map((node) => node.fullPath)
const openKeys = ref<string[]>(defaultOpen())

watch(highlightPath, () => {
    openKeys.value = defaultOpen()
})

const isOpen = (key: string) => openKeys.value.includes(key)

const toggle = (key: string) => {
    if (isOpen(key)) {
        openKeys.value = openKeys.value.filter((item) => item !== key)
        return
    }
    openKeys.value = settingStore.isUniqueOpened ? [key] : [...openKeys.value, key]
}
</script>

<style lang="scss" scoped>
.rail-menu {
    list-style: none;
    margin: 0;
    padding: 0;

    /* ── 顶层菜单项（一级） ─────────────────────────────────────────────── */
    &__item {
        display: flex;
        align-items: center;
        gap: 10px;
        width: 100%;
        height: 42px;
        margin: 2px 0;
        padding: 0 12px 0 10px;
        border: none;
        border-radius: var(--radius-md);
        background: none;
        cursor: pointer;
        color: var(--rail-text-color);
        font-size: var(--el-font-size-base);
        font-family: inherit;
        text-align: left;
        transition:
            background-color var(--duration-base) var(--ease-out),
            color var(--duration-base) var(--ease-out);

        .label {
            flex: 1;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }

        .caret {
            flex: none;
            color: var(--rail-caption-color);
            transition: transform var(--duration-base) var(--ease-out);
        }
        &.is-open .caret {
            transform: rotate(90deg);
        }

        &:hover {
            background-color: var(--rail-hover-bg);
            color: var(--rail-text-hover);
        }

        /* 分组下有激活页面时，标题保持强调色 */
        &.is-branch-active {
            color: var(--rail-branch-active);
            font-weight: 600;
        }
    }

    /* ── 下级分组小标题 ─────────────────────────────────────────────────── */
    &__caption {
        display: flex;
        align-items: center;
        gap: 6px;
        width: 100%;
        height: 32px;
        margin-top: 10px;
        padding: 0 12px 0 8px;
        border: none;
        background: none;
        cursor: pointer;
        color: var(--rail-caption-color);
        font-size: var(--el-font-size-extra-small);
        font-weight: 600;
        letter-spacing: 0.08em;
        text-align: left;
        transition: color var(--duration-base) var(--ease-out);

        .caret {
            transition: transform var(--duration-base) var(--ease-out);
        }
        &.is-open .caret {
            transform: rotate(90deg);
        }
        .label {
            flex: 1;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
        .count {
            font-size: 11px;
            font-weight: 500;
            letter-spacing: 0;
            opacity: 0.6;
        }
        &:hover {
            color: var(--rail-text-hover);
        }
    }

    &__group > .rail-menu[data-depth='1'] {
        padding-left: 6px;
    }

    /* ── 叶子链接 ───────────────────────────────────────────────────────── */
    &__link {
        display: flex;
        align-items: center;
        gap: 10px;
        position: relative;
        height: 38px;
        margin: 2px 0;
        padding: 0 12px 0 10px;
        border-radius: var(--radius-md);
        color: var(--rail-text-color);
        font-size: var(--el-font-size-base);
        text-decoration: none;
        transition:
            background-color var(--duration-base) var(--ease-out),
            color var(--duration-base) var(--ease-out);

        .icon-slot {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 22px;
            height: 22px;
            flex: none;
            border-radius: var(--radius-sm);
            background-color: var(--rail-icon-bg);
            color: var(--rail-icon-color);
            transition:
                background-color var(--duration-base) var(--ease-out),
                color var(--duration-base) var(--ease-out);

            .dot {
                width: 5px;
                height: 5px;
                border-radius: 50%;
                background-color: currentColor;
                opacity: 0.55;
            }
        }

        .label {
            flex: 1;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }

        &:hover {
            background-color: var(--rail-hover-bg);
            color: var(--rail-text-hover);
        }

        &.is-active {
            background-color: var(--rail-active-bg);
            color: var(--rail-active-color);
            font-weight: 600;

            .icon-slot {
                background-color: var(--rail-active-icon-bg);
                color: var(--rail-active-icon-color);
            }

            &::before {
                content: '';
                position: absolute;
                left: 0;
                top: 50%;
                transform: translateY(-50%);
                width: 3px;
                height: 16px;
                border-radius: 0 2px 2px 0;
                background-color: var(--rail-active-bar);
            }
        }
    }
}
</style>
