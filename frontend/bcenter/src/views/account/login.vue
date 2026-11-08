<template>
    <div class="flex flex-col login">
        <div class="login-stage">
            <div class="login-panel">
                <!-- 品牌侧：只在桌面端出现 -->
                <aside class="login-brand">
                    <div class="login-brand__glow"></div>
                    <div class="login-brand__top">
                        <image-contain
                            v-if="brandLogo"
                            class="login-brand__logo"
                            :width="44"
                            :height="44"
                            :src="brandLogo"
                        />
                        <span v-else class="login-brand__logo login-brand__logo--fallback">
                            U
                        </span>
                        <div class="login-brand__name">{{ webName || 'UniMall' }}</div>
                    </div>
                    <div class="login-brand__body">
                        <h1 class="login-brand__title">{{ config.shop_login_title || '商家中心' }}</h1>
                        <p class="login-brand__desc">
                            商品、订单、营销与数据的一体化经营台，让每一天的经营决策都有据可依。
                        </p>
                        <ul class="login-brand__list">
                            <li v-for="item in brandPoints" :key="item">
                                <icon name="el-icon-Select" :size="13" />
                                <span>{{ item }}</span>
                            </li>
                        </ul>
                    </div>
                    <div class="login-brand__foot">BCENTER · 商家经营控制台</div>
                </aside>

                <!-- 表单侧 -->
                <div class="login-form">
                    <div class="login-form__head">
                        <div class="login-form__title">欢迎回来</div>
                        <div class="login-form__sub">请使用商家账号登录以继续</div>
                    </div>
                    <el-form ref="formRef" :model="formData" size="large" :rules="rules">
                        <el-form-item prop="username">
                            <el-input
                                v-model="formData.username"
                                placeholder="请输入账号"
                                @keyup.enter="handleEnter"
                            >
                                <template #prepend>
                                    <icon name="el-icon-User" />
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="password">
                            <el-input
                                ref="passwordRef"
                                v-model="formData.password"
                                show-password
                                placeholder="请输入密码"
                                @keyup.enter="handleLogin"
                            >
                                <template #prepend>
                                    <icon name="el-icon-Lock" />
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-form>
                    <div class="login-form__extra">
                        <el-checkbox v-model="remAccount" label="记住账号"></el-checkbox>
                        <!-- 商家入驻入口（2026-09-24 由 tcenter 迁入本端） -->
                        <el-link type="primary" :underline="false" @click="gotoJoin">
                            还没有店铺？立即入驻
                        </el-link>
                    </div>
                    <el-button
                        class="login-form__submit"
                        type="primary"
                        size="large"
                        :loading="isLock"
                        @click="lockLogin"
                    >
                        登录
                    </el-button>
                </div>
            </div>
        </div>
        <layout-footer />
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import type { InputInstance, FormInstance } from 'element-plus'
import LayoutFooter from '@/layout/components/footer.vue'
import useAppStore from '@/stores/modules/app'
import useUserStore from '@/stores/modules/user'
import cache from '@/utils/cache'
import { ACCOUNT_KEY } from '@/enums/cacheEnums'
import { PageEnum } from '@/enums/pageEnum'
import { useLockFn } from '@/hooks/useLockFn'
import { resolveBrandAsset } from '@/utils/brandAsset'
const passwordRef = shallowRef<InputInstance>()
const formRef = shallowRef<FormInstance>()
const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const remAccount = ref(false)
const config = computed(() => appStore.config)
const webName = computed(() => config.value.web_name ?? '')
const brandLogo = computed(() => resolveBrandAsset(config.value.shop_admin_logo))
const brandPoints = ['全渠道订单与售后闭环', '营销活动与会员资产沉淀', '经营数据实时看板']
const formData = reactive({
    username: '',
    password: ''
})
const rules = {
    username: [
        {
            required: true,
            message: '请输入账号',
            trigger: ['blur']
        }
    ],
    password: [
        {
            required: true,
            message: '请输入密码',
            trigger: ['blur']
        }
    ]
}
// 回车按键监听
const handleEnter = () => {
    if (!formData.password) {
        return passwordRef.value?.focus()
    }
    handleLogin()
}
// 登录处理
const handleLogin = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    // 记住账号，缓存
    cache.set(ACCOUNT_KEY, {
        remember: remAccount.value,
        username: remAccount.value ? formData.username : ''
    })
    await userStore.login(formData)
    const {
        query: { redirect }
    } = route
    const path = typeof redirect === 'string' ? redirect : PageEnum.INDEX
    router.push(path)
}
const { isLock, lockFn: lockLogin } = useLockFn(handleLogin)

// 商家入驻入口（2026-09-24 由 tcenter 迁入本端 /join）
const gotoJoin = () => {
    router.push('/join')
}

onMounted(() => {
    const value = cache.get(ACCOUNT_KEY)
    if (value?.remember) {
        remAccount.value = value.remember
        formData.username = value.username
    }
})
</script>

<style lang="scss" scoped>
.login {
    position: relative;
    min-height: 100vh;
    background-color: var(--neutral-50);
    background-image:
        radial-gradient(1100px 520px at 12% -10%, rgba(239, 79, 85, 0.12), transparent 60%),
        radial-gradient(900px 460px at 96% 8%, rgba(35, 39, 46, 0.08), transparent 62%),
        linear-gradient(180deg, var(--neutral-25) 0%, var(--neutral-75) 100%);
    overflow: auto;
}

.login-stage {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 20px 28px;
}

.login-panel {
    display: flex;
    width: 100%;
    max-width: 940px;
    border-radius: var(--radius-xl);
    overflow: hidden;
    background-color: var(--neutral-0);
    box-shadow: var(--shadow-lg);
}

/* ── 品牌侧 ─────────────────────────────────────────────────────────────── */
.login-brand {
    position: relative;
    flex: none;
    width: 46%;
    padding: 34px 34px 28px;
    display: none;
    flex-direction: column;
    color: #ffffff;
    background: linear-gradient(158deg, var(--shell-900) 0%, var(--shell-700) 58%, var(--brand-600) 100%);

    &__glow {
        position: absolute;
        right: -60px;
        top: -60px;
        width: 220px;
        height: 220px;
        border-radius: 50%;
        background: radial-gradient(circle, rgba(239, 79, 85, 0.38), transparent 68%);
        pointer-events: none;
    }

    &__top {
        display: flex;
        align-items: center;
        gap: 12px;
    }
    &__logo {
        border-radius: var(--radius-md);
        overflow: hidden;
        flex: none;
        background-color: rgba(255, 255, 255, 0.12);

        &--fallback {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 44px;
            height: 44px;
            background: var(--brand-500);
            color: #ffffff;
            font-size: 20px;
            font-weight: 700;
        }
    }
    &__name {
        font-size: var(--el-font-size-large);
        font-weight: 600;
        letter-spacing: 0.02em;
    }

    &__body {
        position: relative;
        margin-top: 44px;
        flex: 1;
    }
    &__title {
        margin: 0;
        font-size: 30px;
        line-height: 1.25;
        font-weight: 600;
        letter-spacing: -0.02em;
        color: #ffffff;
    }
    &__desc {
        margin: 14px 0 0;
        font-size: var(--el-font-size-base);
        line-height: 1.75;
        color: rgba(255, 255, 255, 0.72);
    }
    &__list {
        margin: 26px 0 0;
        padding: 0;
        list-style: none;
        li {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 12px;
            font-size: var(--el-font-size-small);
            color: rgba(255, 255, 255, 0.86);
        }
    }

    &__foot {
        position: relative;
        font-size: var(--el-font-size-extra-small);
        letter-spacing: 0.16em;
        color: rgba(255, 255, 255, 0.42);
    }
}

/* ── 表单侧 ─────────────────────────────────────────────────────────────── */
.login-form {
    flex: 1;
    min-width: 0;
    padding: 44px 40px 40px;
    display: flex;
    flex-direction: column;
    justify-content: center;

    &__head {
        margin-bottom: 28px;
    }
    &__title {
        font-size: 24px;
        font-weight: 600;
        letter-spacing: -0.01em;
        color: var(--el-text-color-primary);
    }
    &__sub {
        margin-top: 8px;
        font-size: var(--el-font-size-small);
        color: var(--el-text-color-secondary);
    }

    &__extra {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        margin-bottom: 20px;
    }

    &__submit {
        width: 100%;
        height: 44px;
        font-size: var(--el-font-size-medium);
        border-radius: var(--radius-md);
    }

    :deep(.el-input__wrapper) {
        border-radius: var(--radius-md);
    }
    :deep(.el-input-group__prepend) {
        border-radius: var(--radius-md) 0 0 var(--radius-md);
        color: var(--el-text-color-secondary);
    }
}

@media (min-width: 768px) {
    .login-brand {
        display: flex;
    }
    .login-form {
        padding: 44px 48px 40px;
    }
}

@media (max-width: 767px) {
    .login-stage {
        padding: 24px 16px 20px;
        align-items: flex-start;
    }
    .login-panel {
        max-width: 420px;
    }
    .login-form {
        padding: 32px 24px 28px;
    }
    .login-form__extra {
        align-items: flex-start;
        flex-direction: column;
    }
}
</style>
