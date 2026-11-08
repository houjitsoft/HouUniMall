<template>
    <div class="qualification-page">
        <div class="qualification-container">
            <!-- 顶部：店铺信息 -->
            <div class="store-header">
                <div class="store-header-left">
                    <div class="store-logo">
                        <img :src="shopInfo.shopLogo || shopInfo.wapShopLogo" :alt="shopInfo.shopName" />
                    </div>
                    <div class="store-info">
                        <h2 class="store-name">{{ $t(shopInfo.shopName || '') }}</h2>
                        <div class="store-name-label">{{ $t('店铺名称') }}: {{ $t(shopInfo.shopName || '') }}</div>
                    </div>
                </div>
            </div>

            <!-- 资质信息卡片 -->
            <div class="qualification-card">
                <div class="qualification-title">{{ $t('网店经营者资质信息公示') }}</div>
                <div class="qualification-subtitle">{{ $t('网店经营资质信息') }}</div>
                <div class="qualification-description">
                    {{ $t('根据国家相关政策要求对网店经营者资质信息公示如下') }}:
                </div>

                <!-- 验证码区域 -->
                <div class="verification-section" v-if="!qualificationInfo">
                    <div class="verification-label">{{ $t('验证码') }}:</div>
                    <div class="verification-input-group">
                        <input v-model="verificationCode" type="text" class="verification-input"
                            :placeholder="$t('请输入验证码')" maxlength="10" />
                        <div class="verification-image-wrapper" @click="handleRefreshVerificationCode">
                            <img v-if="verificationImage" :src="verificationImage" :alt="$t('验证码')"
                                class="verification-image" />
                            <div v-else class="verification-image-placeholder">
                                <span>{{ $t('点击刷新') }}</span>
                            </div>
                        </div>
                    </div>
                    <button class="confirm-btn" @click="handleConfirm" :disabled="!verificationCode || loading">
                        {{ loading ? $t('处理中...') : $t('确定') }}
                    </button>
                </div>

                <!-- 资质信息展示区域 -->
                <div class="qualification-info-section" v-if="qualificationInfo">
                    <div class="info-row">
                        <div class="info-label">{{ $t('店铺名称') }}:</div>
                        <div class="info-value">{{ $t(qualificationInfo.shopName || '') || '-' }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">{{ $t('用户名称') }}:</div>
                        <div class="info-value">{{ $t(qualificationInfo.userName || '') || '-' }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">{{ $t('店铺所在地') }}:</div>
                        <div class="info-value">{{ $t(qualificationInfo.completeShopAddress || '') ||
                            $t(qualificationInfo.shopLocation || '') || '-' }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">{{ $t('公司名称') }}:</div>
                        <div class="info-value">{{ $t(qualificationInfo.enterpriseName || '') || '-' }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">{{ $t('税号') }}:</div>
                        <div class="info-value">{{ qualificationInfo.taxId || '-' }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">{{ $t('营业执照所在地') }}:</div>
                        <div class="info-value">{{ $t(qualificationInfo.completeBlAddress || '') ||
                            $t(qualificationInfo.blLocation || '')
                            || '-' }}</div>
                    </div>
                    <div class="info-row"
                        v-if="qualificationInfo.validateStartTime || qualificationInfo.validateEndTime">
                        <div class="info-label">{{ $t('证件有效期') }}:</div>
                        <div class="info-value">
                            <span v-if="qualificationInfo.validateLongTerm === 1">{{ $t('长期有效') }}</span>
                            <span v-else>
                                {{ qualificationInfo.validateStartTime || '' }} {{ $t('至') }} {{
                                    qualificationInfo.validateEndTime
                                || '' }}
                            </span>
                        </div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">{{ $t('企业电话') }}:</div>
                        <div class="info-value">{{ qualificationInfo.enterprisePhone || '-' }}</div>
                    </div>
                    <div class="info-row" v-if="qualificationInfo.employeeNum">
                        <div class="info-label">{{ $t('企业员工') }}:</div>
                        <div class="info-value">{{ qualificationInfo.employeeNum }}{{ $t('人') }}</div>
                    </div>
                    <div class="info-row" v-if="qualificationInfo.registerMoney">
                        <div class="info-label">{{ $t('注册资金') }}:</div>
                        <div class="info-value">{{ formatPriceWithSymbolSafe(qualificationInfo.registerMoney) }}</div>
                    </div>
                    <div class="info-row" v-if="qualificationInfo.legalPersonName">
                        <div class="info-label">{{ $t('法人姓名') }}:</div>
                        <div class="info-value">{{ qualificationInfo.legalPersonName }}</div>
                    </div>
                    <div class="info-row" v-if="qualificationInfo.legalPersonMobile">
                        <div class="info-label">{{ $t('法人手机号') }}:</div>
                        <div class="info-value">{{ qualificationInfo.legalPersonMobile }}</div>
                    </div>
                    <div class="info-row" v-if="qualificationInfo.selfDeclaration">
                        <div class="info-label">{{ $t('自我声明') }}:</div>
                        <div class="info-value">{{ qualificationInfo.selfDeclaration }}</div>
                    </div>

                    <!-- 营业执照图片 -->
                    <div class="info-row"
                        v-if="qualificationInfo.businessLicencePicBase64 || qualificationInfo.businessLicencePic">
                        <div class="info-label">{{ $t('营业执照') }}:</div>
                        <div class="info-value">
                            <img :src="getImageUrl(qualificationInfo.businessLicencePicBase64 || qualificationInfo.businessLicencePic)"
                                alt="营业执照" class="qualification-image"
                                @click="previewImage(getImageUrl(qualificationInfo.businessLicencePicBase64 || qualificationInfo.businessLicencePic))" />
                        </div>
                    </div>

                    <!-- 生产许可证图片 -->
                    <div class="info-row" v-if="qualificationInfo.manufacturePermitPic">
                        <div class="info-label">{{ $t('生产许可证') }}:</div>
                        <div class="info-value">
                            <img :src="qualificationInfo.manufacturePermitPic" :alt="$t('生产许可证')"
                                class="qualification-image"
                                @click="previewImage(qualificationInfo.manufacturePermitPic)" />
                        </div>
                    </div>

                    <!-- 开户许可证图片 -->
                    <div class="info-row" v-if="qualificationInfo.openPermitPic">
                        <div class="info-label">{{ $t('开户许可证') }}:</div>
                        <div class="info-value">
                            <img :src="qualificationInfo.openPermitPic" :alt="$t('开户许可证')" class="qualification-image"
                                @click="previewImage(qualificationInfo.openPermitPic)" />
                        </div>
                    </div>

                    <!-- 法人身份证正面照 -->
                    <div class="info-row" v-if="qualificationInfo.idCardFront">
                        <div class="info-label">{{ $t('法人身份证正面') }}:</div>
                        <div class="info-value">
                            <img :src="qualificationInfo.idCardFront" :alt="$t('法人身份证正面')" class="qualification-image"
                                @click="previewImage(qualificationInfo.idCardFront)" />
                        </div>
                    </div>

                    <!-- 法人身份证反面照 -->
                    <div class="info-row" v-if="qualificationInfo.idCardBack">
                        <div class="info-label">{{ $t('法人身份证反面') }}:</div>
                        <div class="info-value">
                            <img :src="qualificationInfo.idCardBack" :alt="$t('法人身份证反面')" class="qualification-image"
                                @click="previewImage(qualificationInfo.idCardBack)" />
                        </div>
                    </div>

                    <div class="back-btn-wrapper">
                        <button class="back-btn" @click="handleBack">{{ $t('返回重新查询') }}</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getShopIndexInfo, queryShopQualificationsCode, queryShopQualifications } from '~/services/business.service'
import type { WebShopIndexVO, WebShopBaseIndexVO, ShopQualificationsInfo } from '~/types/store'
import { Toast } from '~/composables/useToast'
import { useIsLogin } from '~/composables/useAuth'

// 获取路由参数
const route = useRoute()
const shopId = computed(() => {
    const id = route.query.id as string
    return id ? Number(id) : 0
})

// 响应式数据
const shopInfo = ref<Pick<WebShopBaseIndexVO, 'shopId' | 'shopName' | 'shopLogo' | 'wapShopLogo'>>({
    shopId: shopId.value,
    shopName: '',
    shopLogo: '',
    wapShopLogo: ''
})

const verificationCode = ref('')
const verificationImage = ref('')
const loading = ref(false)
const qualificationInfo = ref<ShopQualificationsInfo | null>(null)

// 获取店铺信息
const fetchShopInfo = async () => {
    if (!shopId.value) return

    try {
        const response = await getShopIndexInfo(shopId.value)
        if (response.code === 1 && response.data) {
            if (response.data.shopBase) {
                shopInfo.value = {
                    shopId: response.data.shopBase.shopId || shopId.value,
                    shopName: response.data.shopBase.shopName || '',
                    shopLogo: response.data.shopBase.shopLogo || '',
                    wapShopLogo: response.data.shopBase.wapShopLogo || response.data.shopBase.shopLogo || ''
                }
            }
        }
    } catch (error) {
        console.error('获取店铺信息失败:', error)
    }
}

// 获取验证码
const fetchVerificationCode = async () => {
    if (!shopId.value) return

    try {
        const response = await queryShopQualificationsCode(shopId.value)
        if (response.code === 1 && response.data) {
            // base64数据可以直接显示，如果返回的数据不包含前缀，需要添加
            const base64Data = response.data
            if (base64Data.startsWith('data:image')) {
                verificationImage.value = base64Data
            } else {
                verificationImage.value = `data:image/png;base64,${base64Data}`
            }
        }
    } catch (error) {
        console.error('获取验证码失败:', error)
        verificationImage.value = ''
    }
}

// 刷新验证码
const refreshVerificationCode = () => {
    verificationCode.value = ''
    fetchVerificationCode()
}

// 点击验证码图片刷新（需登录）
const handleRefreshVerificationCode = () => {
    if (!useIsLogin(route.fullPath)) {
        return
    }
    refreshVerificationCode()
}

// 处理确定按钮点击
const handleConfirm = async () => {
    if (!useIsLogin(route.fullPath)) {
        return
    }

    if (!verificationCode.value) {
        Toast.warning('请输入验证码')
        return
    }

    if (!shopId.value) {
        Toast.error('店铺ID不存在')
        return
    }

    loading.value = true
    try {
        const response = await queryShopQualifications({
            code: verificationCode.value,
            shopId: shopId.value
        })

        if (response.code === 1 && response.data) {
            qualificationInfo.value = response.data
            Toast.success('查询成功')
        } else {
            Toast.error(response.message || '查询失败，请检查验证码是否正确')
            // 验证码错误时刷新验证码
            refreshVerificationCode()
        }
    } catch (error: any) {
        console.error('查询资质信息失败:', error)
        Toast.error(error?.message || '查询失败，请稍后重试')
        // 出错时刷新验证码
        refreshVerificationCode()
    } finally {
        loading.value = false
    }
}

// 返回重新查询
const handleBack = () => {
    qualificationInfo.value = null
    verificationCode.value = ''
    fetchVerificationCode()
}

// 格式化金额
const formatMoney = (money: number): string => {
    if (!money) return '0.00'
    return money.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 获取图片URL（处理base64）
const getImageUrl = (url: string | undefined): string => {
    if (!url) return ''
    if (url.startsWith('data:image') || url.startsWith('http')) {
        return url
    }
    return `data:image/png;base64,${url}`
}

// 预览图片
const previewImage = (url: string) => {
    if (!url) return
    window.open(url, '_blank')
}

// 页面加载时获取数据
onMounted(async () => {
    await Promise.all([
        fetchShopInfo(),
        fetchVerificationCode()
    ])
})
</script>

<style lang="scss" scoped>
.qualification-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0 40px;
}

.qualification-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.store-header {
    display: flex;
    align-items: center;
    padding: 20px 24px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    margin-bottom: 20px;

    .store-header-left {
        display: flex;
        align-items: center;
        gap: 16px;
        flex: 1;

        .store-logo {
            width: 80px;
            height: 80px;
            border-radius: 10px;
            overflow: hidden;
            background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
            flex-shrink: 0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .store-info {
            flex: 1;
            min-width: 0;

            .store-name {
                font-size: 20px;
                font-weight: 600;
                color: #1a1a1a;
                margin: 0 0 8px 0;
                line-height: 1.4;
            }

            .store-name-label {
                font-size: 14px;
                color: #666;
            }
        }
    }
}

.qualification-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

    .qualification-title {
        font-size: 24px;
        font-weight: 600;
        color: #1a1a1a;
        margin-bottom: 16px;
        text-align: center;
    }

    .qualification-subtitle {
        font-size: 18px;
        font-weight: 500;
        color: #333;
        margin-bottom: 20px;
        text-align: center;
    }

    .qualification-description {
        font-size: 14px;
        color: #666;
        margin-bottom: 40px;
        text-align: center;
        line-height: 1.6;
    }

    .verification-section {
        max-width: 500px;
        margin: 0 auto;

        .verification-label {
            font-size: 14px;
            color: #333;
            margin-bottom: 12px;
        }

        .verification-input-group {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 20px;

            .verification-input {
                flex: 1;
                height: 40px;
                padding: 0 12px;
                border: 1px solid #e0e0e0;
                border-radius: 8px;
                font-size: 14px;
                color: #333;
                transition: all 0.3s ease;

                &:focus {
                    outline: none;
                    border-color: var(--color, #ff6b35);
                    box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.1);
                }

                &::placeholder {
                    color: #999;
                }
            }

            .verification-image-wrapper {
                width: 120px;
                height: 40px;
                border: 1px solid #e0e0e0;
                border-radius: 8px;
                overflow: hidden;
                cursor: pointer;
                background-color: #f8f9fa;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.3s ease;

                &:hover {
                    border-color: var(--color, #ff6b35);
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                }

                .verification-image {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                }

                .verification-image-placeholder {
                    font-size: 12px;
                    color: #999;
                    text-align: center;
                }
            }
        }

        .confirm-btn {
            width: 100%;
            height: 44px;
            background: linear-gradient(135deg, #ff6b35 0%, #ff8c5a 100%);
            border: none;
            border-radius: 8px;
            color: #ffffff;
            font-size: 16px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);

            &:hover:not(:disabled) {
                background: linear-gradient(135deg, #ff8c5a 0%, #ff6b35 100%);
                transform: translateY(-2px);
                box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
            }

            &:active:not(:disabled) {
                transform: translateY(0);
            }

            &:disabled {
                opacity: 0.6;
                cursor: not-allowed;
                transform: none;
            }
        }
    }

    .qualification-info-section {
        margin-top: 40px;

        .info-row {
            display: flex;
            padding: 16px 0;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
                border-bottom: none;
            }

            .info-label {
                width: 150px;
                font-size: 14px;
                color: #666;
                font-weight: 500;
                flex-shrink: 0;
            }

            .info-value {
                flex: 1;
                font-size: 14px;
                color: #333;
                word-break: break-all;
                line-height: 1.6;

                .qualification-image {
                    max-width: 300px;
                    max-height: 200px;
                    border: 1px solid #e0e0e0;
                    border-radius: 8px;
                    cursor: pointer;
                    transition: all 0.3s ease;

                    &:hover {
                        border-color: var(--color, #ff6b35);
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                        transform: scale(1.02);
                    }
                }
            }
        }

        .back-btn-wrapper {
            margin-top: 30px;
            text-align: center;

            .back-btn {
                padding: 12px 32px;
                background: #ffffff;
                border: 1px solid #e0e0e0;
                border-radius: 8px;
                color: #333;
                font-size: 14px;
                font-weight: 500;
                cursor: pointer;
                transition: all 0.3s ease;

                &:hover {
                    border-color: var(--color, #ff6b35);
                    color: var(--color, #ff6b35);
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                }
            }
        }
    }
}

/* 响应式 */
@media (max-width: 768px) {
    .qualification-container {
        padding: 0 16px;
    }

    .store-header {
        padding: 16px;

        .store-header-left {
            .store-logo {
                width: 60px;
                height: 60px;
            }

            .store-info {
                .store-name {
                    font-size: 18px;
                }

                .store-name-label {
                    font-size: 12px;
                }
            }
        }
    }

    .qualification-card {
        padding: 24px 16px;

        .qualification-title {
            font-size: 20px;
        }

        .qualification-subtitle {
            font-size: 16px;
        }

        .qualification-description {
            font-size: 13px;
            margin-bottom: 30px;
        }

        .verification-section {
            .verification-input-group {
                flex-direction: column;
                align-items: stretch;

                .verification-input {
                    width: 100%;
                }

                .verification-image-wrapper {
                    width: 100%;
                    height: 50px;
                }
            }
        }

        .qualification-info-section {
            .info-row {
                flex-direction: column;
                gap: 8px;

                .info-label {
                    width: 100%;
                }

                .info-value {
                    .qualification-image {
                        max-width: 100%;
                    }
                }
            }
        }
    }
}
</style>
