<template>
    <button type="button" class="live-panel" @click="emit('select')">
        <div class="live-panel__head">
            <h3 class="live-panel__title">{{ title || fallbackTitle }}</h3>
            <span class="live-panel__badge">LIVE</span>
        </div>
        <div
            class="live-panel__cover"
            :style="coverImage ? { backgroundImage: `url(${coverImage})` } : undefined"
        >
            <div v-if="!coverImage" class="live-panel__placeholder">{{ $t('暂无直播') }}</div>
            <div v-else class="live-panel__mask">
                <span class="live-panel__play">▶</span>
            </div>
        </div>
    </button>
</template>

<script setup lang="ts">
withDefaults(
    defineProps<{
        title?: string
        fallbackTitle?: string
        coverImage?: string
    }>(),
    {
        title: '',
        fallbackTitle: '精选直播',
        coverImage: '',
    }
)

const emit = defineEmits<{
    select: []
}>()
</script>

<style lang="scss" scoped>
.live-panel {
    width: 100%;
    height: 100%;
    min-width: 0;
    padding: 10px 8px 12px;
    box-sizing: border-box;
    border-radius: var(--home-card-radius, 8px);
    border: 1px solid var(--home-border, #e2e8ed);
    background: var(--home-surface, #fff);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    cursor: pointer;
    text-align: left;
}

.live-panel__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
    flex-shrink: 0;
}

.live-panel__title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: var(--home-text, #202a33);
    line-height: 20px;
}

.live-panel__badge {
    flex-shrink: 0;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.04em;
    color: #fff;
    background: var(--color);
    border-radius: 2px;
    padding: 2px 7px;
    line-height: 14px;
}

.live-panel__cover {
    flex: 1;
    min-height: 0;
    border-radius: 6px;
    border: 1px solid var(--home-border, #e2e8ed);
    background: var(--home-surface-soft, #f7f9fb) center / cover no-repeat;
    position: relative;
    overflow: hidden;
}

.live-panel__placeholder {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #bbb;
    font-size: 12px;
}

.live-panel__mask {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(180deg, transparent 40%, rgba(0, 0, 0, 0.35));
}

.live-panel__play {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #17324d;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    padding-left: 2px;
    box-shadow: none;
}
</style>
