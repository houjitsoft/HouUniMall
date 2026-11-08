<template>
    <section class="plus-hero-shell" :style="heroStyle">
        <header class="plus-hero-shell__topbar" role="banner">
            <div class="plus-hero-shell__topbar-inner">
                <NuxtLink to="/member/plus" class="plus-hero-shell__brand">{{ $t('PLUS会员') }}</NuxtLink>
                <span class="plus-hero-shell__slogan">{{ $t('更好 / 更省 / 更优') }}</span>
            </div>
        </header>
        <slot />
    </section>
</template>

<script setup lang="ts">
import { IMAGE_URL } from '~/utils/externalUrl'

const heroStyle = {
    '--plus-hero-shell-bg': `url(${IMAGE_URL('plusImg/no2.png')})`
} as Record<string, string>
</script>

<style scoped lang="scss">
.plus-hero-shell {
    position: relative;
    width: 100%;
}

.plus-hero-shell::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 567px;
    background-image: var(--plus-hero-shell-bg);
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    z-index: 0;
}

.plus-hero-shell__topbar {
    position: relative;
    z-index: 2;
    width: 100%;
    height: 70px;
    background: rgba(27, 33, 48, 0.92);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(232, 192, 120, 0.12);
    box-sizing: border-box;
}

.plus-hero-shell__topbar-inner {
    max-width: 1200px;
    height: 100%;
    margin: 0 auto;
    padding: 0 clamp(16px, 4vw, 28px);
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;
}

.plus-hero-shell__brand {
    font-size: 22px;
    font-weight: 800;
    color: #ffd3a2;
    letter-spacing: 0.08em;
    text-decoration: none;
}

.plus-hero-shell__slogan {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.78);
    letter-spacing: 0.12em;
}
</style>
