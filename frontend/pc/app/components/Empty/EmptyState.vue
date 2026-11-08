<template>
    <div class="empty-state" :class="{ 'empty-state-compact': compact }">
        <div class="empty-content">
            <div class="empty-icon">
                <svg :width="iconSize" :height="iconSize" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                        d="M20 16H44C45.1046 16 46 16.8954 46 18V46C46 47.1046 45.1046 48 44 48H20C18.8954 48 18 47.1046 18 46V18C18 16.8954 18.8954 16 20 16Z"
                        stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                    <path d="M24 24H40M24 32H40M24 40H36" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                        stroke-linejoin="round" />
                    <circle cx="28" cy="28" r="2" fill="currentColor" />
                    <circle cx="36" cy="28" r="2" fill="currentColor" />
                </svg>
            </div>
            <div class="empty-text">{{ text }}</div>
            <div v-if="description" class="empty-desc">{{ description }}</div>
            <NuxtLink v-if="actionTo" class="empty-action" :to="actionTo">{{ actionText }}</NuxtLink>
        </div>
    </div>
</template>

<script setup lang="ts">
interface Props {
    text?: string
    description?: string
    iconSize?: number
    compact?: boolean
    actionText?: string
    actionTo?: string
}

withDefaults(defineProps<Props>(), {
    text: '暂无数据',
    description: '',
    iconSize: 64,
    compact: false,
    actionText: '去逛逛',
    actionTo: ''
})
</script>

<style lang="scss" scoped>
.empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 48px 20px;
    background: var(--bg-card, #fff);
    border-radius: var(--border-radius, 4px);
    border: 1px solid var(--border-light, #eee);
    min-height: 240px;

    &.empty-state-compact {
        padding: 32px 16px;
        min-height: 160px;
    }

    .empty-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        text-align: center;

        .empty-icon {
            color: #d0d0d0;

            svg {
                display: block;
            }
        }

        .empty-text {
            color: var(--text-regular, #5d6872);
            font-size: var(--font-size-base, 14px);
            font-weight: 500;
        }

        .empty-desc {
            color: var(--text-secondary, #8f8f8f);
            font-size: var(--font-size-xs, 12px);
            line-height: 1.5;
            max-width: 300px;
        }

        .empty-action {
            margin-top: 4px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            height: 36px;
            padding: 0 16px;
            font-size: 13px;
            color: #fff;
            background: var(--color);
            border-radius: var(--border-radius, 4px);
            text-decoration: none;

            &:hover {
                background: var(--color-primary-hover, var(--color));
            }
        }
    }
}

@media (max-width: 640px) {
    .empty-state {
        padding: 32px 16px;
        min-height: 180px;
    }
}
</style>
