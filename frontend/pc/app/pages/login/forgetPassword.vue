<template>
    <main class="auth-redirect">
        <p>正在前往统一会员中心找回密码…</p>
        <a :href="target">立即前往</a>
    </main>
</template>

<script setup lang="ts">
import { useStaticUcenterAuthRedirect } from '~/composables/useStaticUcenterAuthRedirect'

definePageMeta({ layout: 'full-screen' })
const target = useStaticUcenterAuthRedirect('forgot-password')
</script>

<style scoped>
.auth-redirect { min-height: 60vh; display: grid; place-content: center; gap: 12px; text-align: center; color: #475569; }
.auth-redirect a { color: #2056d7; }
</style>
