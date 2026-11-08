<template>
    <div class="load" :class="{ 'load--compact': compact }">
        <div class="load__spinner">
            <div class="load__spinner-ring"></div>
            <div class="load__spinner-ring"></div>
            <div class="load__spinner-ring"></div>
        </div>
        <p v-if="text" class="load__text">{{ text }}</p>
    </div>
</template>

<script setup lang="ts">
withDefaults(
    defineProps<{
        /** 加载提示文案，传空字符串则不显示 */
        text?: string
        /** 紧凑模式，用于弹窗、表格等小区域 */
        compact?: boolean
    }>(),
    { text: '正在加载...', compact: false }
)
</script>

<style lang="scss" scoped>
.load {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 500px;
    padding: 60px 20px;
    background-color: #fafafa;
    border-radius: 8px;
    transition: all 0.3s ease;
    animation: load-fade-in 0.3s ease-in-out;

    @media (max-width: 768px) {
        min-height: 400px;
        padding: 40px 15px;
    }

    &--compact {
        min-height: 150px;
        padding: 40px 20px;

        @media (max-width: 768px) {
            min-height: 120px;
            padding: 30px 15px;
        }

        .load__spinner {
            width: 40px;
            height: 40px;

            @media (max-width: 768px) {
                width: 32px;
                height: 32px;
            }
        }

        .load__text {
            margin-top: 16px;
            font-size: 14px;

            @media (max-width: 768px) {
                margin-top: 12px;
                font-size: 13px;
            }
        }
    }

    &__spinner {
        position: relative;
        width: 64px;
        height: 64px;
        display: flex;
        align-items: center;
        justify-content: center;

        @media (max-width: 768px) {
            width: 48px;
            height: 48px;
        }
    }

    &__spinner-ring {
        position: absolute;
        width: 100%;
        height: 100%;
        border: 4px solid transparent;
        border-top-color: var(--color);
        border-radius: 50%;
        animation: load-spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
        box-sizing: border-box;

        @media (max-width: 768px) {
            border-width: 3px;
        }

        &:nth-child(1) {
            animation-delay: -0.45s;
            border-top-color: var(--color);
            opacity: 1;
        }

        &:nth-child(2) {
            animation-delay: -0.3s;
            border-top-color: color-mix(in srgb, var(--color) 70%, transparent);
            opacity: 0.8;
            width: 80%;
            height: 80%;
            top: 10%;
            left: 10%;
        }

        &:nth-child(3) {
            animation-delay: -0.15s;
            border-top-color: color-mix(in srgb, var(--color) 50%, transparent);
            opacity: 0.6;
            width: 60%;
            height: 60%;
            top: 20%;
            left: 20%;
        }
    }

    &__text {
        margin-top: 24px;
        font-size: 15px;
        color: #666;
        font-weight: 500;
        letter-spacing: 0.5px;
        animation: load-pulse 1.5s ease-in-out infinite;
        user-select: none;

        @media (max-width: 768px) {
            font-size: 14px;
            margin-top: 20px;
        }
    }
}

@keyframes load-fade-in {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes load-spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

@keyframes load-pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.6;
    }
}
</style>
