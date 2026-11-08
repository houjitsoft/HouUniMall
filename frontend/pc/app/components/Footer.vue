<template>
    <footer class="footer">
        <!-- 如果数据正在加载，显示加载状态 -->
        <div v-if="loading" class="loading">{{ $t('加载中...') }}</div>
        <!-- 优先使用后台帮助栏目；未配置时展示商城基础服务导航 -->
        <div v-else class="footer-content">
            <div v-for="section in displayHelpInfo" :key="section.articleGroupId" class="footer-section">
                <h3>{{ $t(section.articleGroupTitle || '') }}</h3>
                <ul>
                    <li v-for="item in section.articletitle2" :key="item.articleId">
                        <NuxtLink
                            :to="item.articleUrl || '/article'"
                            :external="String(item.articleUrl || '').startsWith('/ucenter/#/')"
                            @click="handleArticleClick(section.articleGroupId, item.articleId)">
                            {{ $t(item.articleTitle)  }}
                        </NuxtLink>
                    </li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            <p class="footer-version">
                <span>{{ $t(copyrightInfo || 'UniMall商城') }}</span>
                <span>{{ $t('版本号') }}：V{{ appVersion }}</span>
            </p>
            <p class="footer-records">
                <span>{{ $t('服务热线') }}: {{ displayPhoneInfo }}</span>
				<a class="icp-info" href="https://beian.miit.gov.cn" rel="noreferrer" target="_blank"><img class="ics-style" :src="IMAGE_URL('beian.png')" alt="">{{ displayIcpInfo }}</a>
                <a
                    v-if="cybersecurityFilingNumber"
                    class="icp-info"
                    :href="`http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=${cybersecurityRecordCode}`"
                    target="_blank"
                    rel="noopener noreferrer"
                ><img class="ics-style" :src="IMAGE_URL('wangan.png')" alt="">{{ cybersecurityFilingNumber }}</a>
            </p>
        </div>
    </footer>
</template>

<script setup lang="ts">
// 底部组件逻辑
import { ref, computed, onMounted } from 'vue';
import { getHelpInfo, selectByConfigKey } from '~/services/base.service';
import { useArticleStore } from '~/stores';
import { ucenterAuthUrl } from '~/utils/ucenterAuthUrl';

const { applyBackendTheme } = useTheme()
const articleStore = useArticleStore()
const runtimeConfig = useRuntimeConfig()

const DEFAULT_HELP_INFO = [
    {
        articleGroupId: 'default-onboarding',
        articleGroupTitle: '用户入门',
        articletitle2: [
            { articleId: 'register', articleTitle: '免费注册', articleUrl: ucenterAuthUrl('register') },
            { articleId: 'promotion', articleTitle: '促销活动', articleUrl: '/activity/discount' },
            { articleId: 'shopping', articleTitle: '购物流程', articleUrl: '/article' }
        ]
    },
    {
        articleGroupId: 'default-payment',
        articleGroupTitle: '支付方式',
        articletitle2: [
            { articleId: 'online-payment', articleTitle: '在线支付', articleUrl: '/article' },
            { articleId: 'cash-on-delivery', articleTitle: '货到付款', articleUrl: '/article' },
            { articleId: 'company-transfer', articleTitle: '公司转账', articleUrl: '/article' }
        ]
    },
    {
        articleGroupId: 'default-about',
        articleGroupTitle: '关于我们',
        articletitle2: [
            { articleId: 'about', articleTitle: '关于我们', articleUrl: '/article' },
            { articleId: 'agreement', articleTitle: '服务协议', articleUrl: '/agreement/service' },
            { articleId: 'invoice', articleTitle: '开票介绍', articleUrl: '/article' }
        ]
    },
    {
        articleGroupId: 'default-merchant',
        articleGroupTitle: '商家服务',
        articletitle2: [
            { articleId: 'merchant-entry', articleTitle: '商家入驻', articleUrl: '/article' },
            { articleId: 'platform-rules', articleTitle: '平台规则', articleUrl: '/article' },
            { articleId: 'rule-standards', articleTitle: '规则标准', articleUrl: '/article' }
        ]
    },
    {
        articleGroupId: 'default-after-sales',
        articleGroupTitle: '售后服务',
        articletitle2: [
            { articleId: 'after-sales-policy', articleTitle: '售后政策', articleUrl: '/article' },
            { articleId: 'refund', articleTitle: '退款说明', articleUrl: '/article' },
            { articleId: 'invoice-protection', articleTitle: '发票保障', articleUrl: '/article' }
        ]
    },
    {
        articleGroupId: 'default-help',
        articleGroupTitle: '帮助中心',
        articletitle2: [
            { articleId: 'customer-service', articleTitle: '联系客服', articleUrl: '/article' },
            { articleId: 'returns', articleTitle: '退换细则', articleUrl: '/article' },
            { articleId: 'faq', articleTitle: '常见问题', articleUrl: '/article' }
        ]
    }
];

const helpInfo = ref<any[]>([]);
const icpInfo = ref<string>('');
const cybersecurityFilingNumber = ref<string>('');
const phoneInfo = ref<string>('');
const copyrightInfo = ref<string>('');
const loading = ref(false);
const displayHelpInfo = computed(() => helpInfo.value.length > 0 ? helpInfo.value : DEFAULT_HELP_INFO);
const displayPhoneInfo = computed(() => phoneInfo.value || '400-858-1598');
const displayIcpInfo = computed(() => icpInfo.value || '沪ICP备11025711号-38');
const appVersion = computed(() => String(runtimeConfig.public.appVersion || '1.0.0').replace(/^v/i, ''));

// 公网安备号中间为纯数字，截取后作为 recordcode 跳转参数
const cybersecurityRecordCode = computed(() => {
    const match = cybersecurityFilingNumber.value.match(/\d+/);
    return match ? match[0] : '';
});

const handleArticleClick = (articleGroupId: number | string, articleId: number | string) => {
    if (typeof articleGroupId !== 'number' || typeof articleId !== 'number') return
    articleStore.setArticleGroupId(articleGroupId)
    articleStore.setArticleId(articleId)
}

const applyThemeFromBackend = (color: string) => {
    applyBackendTheme({
        primary: color,
        background: '#ffffff',
        text: '#303133'
    })
}

// 调用接口获取帮助信息
const fetchHelpInfo = async () => {
    loading.value = true;
    try {
        const response = await getHelpInfo();
        // 假设接口返回的数据结构符合我们的预期
        if (response.code === 1 && response.data) {
            helpInfo.value = response.data.articleGroups || [];
        }

        if (response.code === 1 && response.data?.webColor) {
            applyThemeFromBackend(response.data.webColor)
        }
    } catch (error) {
        console.error('获取帮助信息失败:', error);
    } finally {
        loading.value = false;
    }
};

// 调用接口获取备案号信息
const fetchIcpInfo = async () => {
    try {
        const response = await selectByConfigKey('setting_records');
        // 假设接口返回的数据结构符合我们的预期
        if (response.code === 1 && response.data) {
            icpInfo.value = response.data.configValue;
        }
    } catch (error) {
        console.error('获取备案号信息失败:', error);
    }
};

// 调用接口获取网安备案号信息
const fetchCybersecurityFilingNumber = async () => {
    try {
        const response = await selectByConfigKey('cybersecurity_filing_number');
        if (response.code === 1 && response.data) {
            cybersecurityFilingNumber.value = response.data.configValue;
        }
    } catch (error) {
        console.error('获取网安备案号信息失败:', error);
    }
};

// 调用接口获取服务热线信息
const fetchPhoneInfo = async () => {
    try {
        const response = await selectByConfigKey('setting_phone');
        // 假设接口返回的数据结构符合我们的预期
        if (response.code === 1 && response.data) {
            phoneInfo.value = response.data.configValue;
        }
    } catch (error) {
        console.error('获取服务热线信息失败:', error);
    }
};

// 调用接口获取版权信息（版本号）
const fetchCopyrightInfo = async () => {
    try {
        const response = await selectByConfigKey('setting_copyright');
        // 假设接口返回的数据结构符合我们的预期
        if (response.code === 1 && response.data) {
            copyrightInfo.value = response.data.configValue;
        }
    } catch (error) {
        console.error('获取版权信息失败:', error);
    }
};

// 导航 Logo 配置由 Header 组件统一拉取，此处不再重复请求

onMounted(() => {
    fetchHelpInfo();
    fetchIcpInfo();
    fetchCybersecurityFilingNumber();
    fetchPhoneInfo();
    fetchCopyrightInfo();
});
</script>

<style lang="scss" scoped>
.footer {
    background: #fff;
    color: #171717;
    border-top: 18px solid #f6f7f9;

    .loading {
        text-align: center;
        min-height: 178px;
        padding: 72px 0;
        color: #737373;
        font-size: 14px;
    }

    .footer-content {
        max-width: 1160px;
        margin: 0 auto;
        padding: 34px 20px 26px;
        display: grid;
        grid-template-columns: repeat(6, 1fr);
        gap: 20px;

        .footer-section {
            min-width: 0;
            text-align: center;

            h3 {
                margin: 0 0 10px;
                padding: 0 8px 11px;
                color: #171717;
                border-bottom: 1px solid #dedede;
                font-size: 16px;
                font-weight: 700;
                line-height: 1.4;
            }

            ul {
                list-style: none;
                padding: 0;
                margin: 0;

                li {
                    min-height: 31px;

                    a {
                        display: inline-flex;
                        align-items: center;
                        min-height: 31px;
                        color: #222;
                        text-decoration: none;
                        transition: color 0.2s ease;
                        font-size: 14px;
                        line-height: 1.5;

                        &:hover {
                            color: var(--color-primary, #4a5dff);
                        }

                        &:focus-visible {
                            border-radius: 2px;
                            outline: 2px solid var(--color-primary, #4a5dff);
                            outline-offset: 3px;
                        }
                    }
                }
            }
        }
    }

    .footer-bottom {
        text-align: center;
        padding: 22px 20px 30px;
        border-top: 1px solid #e1e1e1;
        color: #171717;
        font-size: 14px;

        p {
            margin: 0;
            font-size: 14px;
            line-height: 1.8;
        }

        .footer-version,
        .footer-records {
            display: flex;
            align-items: center;
            justify-content: center;
            flex-wrap: wrap;
            gap: 0 10px;
        }

        .footer-version {
            margin-bottom: 5px;
        }

        .icp-info {
            display: inline-flex;
            align-items: center;
            color: #171717;
            text-decoration: none;
            white-space: nowrap;

            &:hover {
                color: var(--color-primary, #4a5dff);
            }

            &:focus-visible {
                border-radius: 2px;
                outline: 2px solid var(--color-primary, #4a5dff);
                outline-offset: 3px;
            }
        }
    }
}
.ics-style{
	margin-right: 5px;
	width: 16px;
	height: 16px;
    object-fit: contain;
}
// 响应式设计
@media (max-width: 960px) {
    .footer {
        .footer-content {
            grid-template-columns: repeat(3, 1fr);
            gap: 28px 24px;
        }
    }
}

@media (max-width: 600px) {
    .footer {
        border-top-width: 12px;

        .footer-content {
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 24px 16px;
            padding: 28px 16px 24px;
        }

        .footer-bottom {
            padding: 20px 16px 26px;

            p {
                font-size: 13px;
            }

            .footer-version,
            .footer-records {
                gap: 2px 8px;
            }

            .footer-records {
                flex-direction: column;
                gap: 2px;
            }
        }
    }
}
</style>
