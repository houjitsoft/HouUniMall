<template>
    <Teleport to="body">
        <!-- 领取成功：全屏遮罩 + 居中图；点遮罩关闭；点图跳转用户中心红包券 -->
        <div v-if="showSuccessOverlay" class="home-platform-redpacket__mask" role="dialog" aria-modal="true"
            @click="handleMaskClick">
            <img class="home-platform-redpacket__success-img" :src="getRedpacketSuccessUrl" alt=""
                @click.stop="handleSuccessImageClick" />
        </div>
    </Teleport>

    <div v-if="visibleEntry" class="home-platform-redpacket" role="button" tabindex="0"
        :class="{ 'home-platform-redpacket--busy': claiming }" @click="handleRedpacketClick"
        @keydown.enter.prevent="handleRedpacketClick">
        <button type="button" class="home-platform-redpacket__close" :aria-label="$t('关闭') || ''"
            @click.stop="handleClose">
            ×
        </button>
        <img class="home-platform-redpacket__img" :src="redpacketImgUrl" alt="" />
    </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '~/stores/modules/user'
import { useMallStore } from '~/stores/modules/mall'
import { redpacketBaseAllAvailable, redpacketInsertRedAll } from '~/services/activity.service'
import { IMAGE_URL, UCENTER_URL } from '~/utils/externalUrl'
import { Toast } from '~/composables/useToast'

const userStore = useUserStore()
const mallStore = useMallStore()
const { isLogin } = storeToRefs(userStore)
const { homePlatformRedpacketClosed, homePlatformRedpacketEntryVisible } = storeToRefs(mallStore)

const claiming = ref(false)
const showSuccessOverlay = ref(false)
const redpacketImgUrl = IMAGE_URL('pcImg/redpacket.png')
const getRedpacketSuccessUrl = IMAGE_URL('pcImg/getRedpacket.png')
const visibleEntry = computed(
    () => homePlatformRedpacketEntryVisible.value && !homePlatformRedpacketClosed.value
)

/**
 * 已登录时请求平台可领取红包接口，成功则展示入口。
 * @returns Promise<void>
 */
async function fetchAvailableRedpacket(): Promise<void> {
    if (!isLogin.value) {
        mallStore.setHomePlatformRedpacketEntryVisible(false)
        return
    }
    try {
        const res = await redpacketBaseAllAvailable({ platForm: 1 })
        mallStore.setHomePlatformRedpacketEntryVisible(res?.code === 1)
    } catch {
        mallStore.setHomePlatformRedpacketEntryVisible(false)
    }
}

/**
 * 点击红包：调用领取接口；code === 1 时展示成功图；data === 0 时隐藏浮标。
 * @returns Promise<void>
 */
async function handleRedpacketClick(): Promise<void> {
    if (claiming.value) return
    claiming.value = true
    try {
        const res = await redpacketInsertRedAll({ platForm: 1 })
        if (res?.code === 1) {
            showSuccessOverlay.value = true
            if (Number(res.data) === 0) {
                mallStore.setHomePlatformRedpacketEntryVisible(false)
            }
        } else {
            Toast.error(res?.message || '领取失败')
        }
    } catch {
        Toast.error('领取失败，请稍后重试')
    } finally {
        claiming.value = false
    }
}

/**
 * 点击遮罩层关闭成功提示图。
 * @returns void
 */
function handleMaskClick(): void {
    showSuccessOverlay.value = false
}

/**
 * 点击成功图：关闭遮罩并跳转用户中心红包券页。
 * @returns void
 */
function handleSuccessImageClick(): void {
    showSuccessOverlay.value = false
    UCENTER_URL('/property/redPacket')
}

/**
 * 关闭平台红包入口，并记录到状态管理中。
 * @returns void
 */
function handleClose(): void {
    mallStore.setHomePlatformRedpacketClosed(true)
}

watch(isLogin, () => {
    void fetchAvailableRedpacket()
}, { immediate: true })
</script>

<style scoped lang="scss">
.home-platform-redpacket {
    position: fixed;
    right: 30px;
    top: 150px;
    z-index: 50;
    cursor: pointer;
    line-height: 0;
    transition: transform 0.22s ease, filter 0.22s ease;
    filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.12));
    animation: redpacket-float 2.2s ease-in-out infinite;

    &--busy {
        cursor: wait;
        opacity: 0.85;
    }

    &:hover:not(.home-platform-redpacket--busy) {
        transform: scale(1.1) translateY(-2px);
        filter: drop-shadow(0 8px 20px rgba(228, 57, 60, 0.28));
    }

    &:focus-visible {
        outline: 2px solid var(--color, #e4393c);
        outline-offset: 4px;
        border-radius: 8px;
    }

    &__img {
        display: block;
        width: 187px;
        height: auto;
        max-height: 229px;
        object-fit: contain;
        user-select: none;
        pointer-events: none;
    }

    &__close {
        position: absolute;
        top: -22px;
        right: -12px;
        width: 24px;
        height: 24px;
        border: none;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.55);
        color: #fff;
        font-size: 16px;
        line-height: 24px;
        text-align: center;
        cursor: pointer;
        z-index: 2;
        pointer-events: auto;
        transition: background-color 0.2s ease, transform 0.2s ease;

        &:hover {
            background: rgba(0, 0, 0, 0.75);
            transform: scale(1.08);
        }
    }

    &__mask {
        position: fixed;
        inset: 0;
        z-index: 10050;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 24px;
        background: rgba(0, 0, 0, 0.55);
        cursor: pointer;
        box-sizing: border-box;
    }

    &__success-img {
        display: block;
        max-width: min(520px, 90vw);
        max-height: min(70vh, 520px);
        width: auto;
        height: auto;
        object-fit: contain;
        cursor: pointer;
        pointer-events: auto;
        user-select: none;
    }
}

@keyframes redpacket-float {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-4px);
    }
}
</style>
