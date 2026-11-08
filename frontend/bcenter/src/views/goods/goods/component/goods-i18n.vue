<template>
    <div class="goods-i18n-panel p-2">
        <el-alert
            v-if="!goodsId"
            type="info"
            :closable="false"
            show-icon
            title="可先按语言填写译文，点击页面底部“保存”后将随商品一起保存。"
            class="mb-4"
        />
        <el-alert
            v-else
            type="info"
            :closable="false"
            show-icon
            title="按语言页签维护商品名称、卖点和商品详情，页面底部“保存”会同时保存商品与译文。"
            class="mb-4"
        />

        <div class="mb-4 flex flex-wrap items-center justify-between gap-3" v-if="!readOnly">
            <span class="text-sm text-gray-500">当前语言：{{ activeLanguageLabel }}</span>
            <div class="flex gap-2">
                <el-tooltip
                    :disabled="!!goodsId"
                    content="新商品首次保存后即可使用一键翻译"
                    placement="top"
                >
                    <span>
                        <el-button
                            type="primary"
                            :loading="translating"
                            :disabled="!goodsId"
                            @click="handleAutoTranslate"
                        >
                            一键翻译当前语言
                        </el-button>
                    </span>
                </el-tooltip>
                <el-button @click="loadList" :disabled="!goodsId">刷新译文</el-button>
            </div>
        </div>

        <el-tabs v-model="activeLang" type="card" class="language-tabs">
            <el-tab-pane
                v-for="l in langOptions"
                :key="l.code"
                :label="l.label"
                :name="l.code"
                lazy
            >
                <div class="language-form">
                    <el-form-item label="状态">
                        <el-tag v-if="formMap[l.code].status === 'published'" type="success">
                            已发布
                        </el-tag>
                        <el-tag v-else-if="formMap[l.code].status === 'stale'" type="warning">
                            待重译
                        </el-tag>
                        <el-tag v-else-if="hasLanguageContent(formMap[l.code])" type="info">
                            待保存
                        </el-tag>
                        <el-tag v-else type="info">未填写</el-tag>
                        <span class="ml-2 text-gray-400 text-sm" v-if="formMap[l.code].source">
                            来源：{{ sourceLabel(formMap[l.code].source) }}
                        </span>
                    </el-form-item>

                    <el-form-item label="商品名称">
                        <div class="field-width">
                            <el-input
                                v-model="formMap[l.code].commonName"
                                :disabled="readOnly"
                                maxlength="500"
                                show-word-limit
                                placeholder="请输入该语言的商品名称"
                            />
                        </div>
                    </el-form-item>

                    <el-form-item label="卖点/简介">
                        <div class="field-width">
                            <el-input
                                v-model="formMap[l.code].promotionTips"
                                type="textarea"
                                :rows="3"
                                :disabled="readOnly"
                                maxlength="500"
                                show-word-limit
                                placeholder="请输入该语言的卖点或简介"
                            />
                        </div>
                    </el-form-item>

                    <el-form-item label="商品详情">
                        <div class="detail-editor">
                            <editor
                                v-if="!readOnly"
                                v-model="formMap[l.code].commonBody"
                                :height="420"
                                :width="720"
                            />
                            <el-scrollbar v-else class="detail-preview">
                                <div v-html="formMap[l.code].commonBody"></div>
                            </el-scrollbar>
                        </div>
                    </el-form-item>
                </div>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script lang="ts" setup>
import { autoTranslateGoodsI18n, getGoodsI18nList, saveGoodsI18n } from '@/api/goods/i18n'
import feedback from '@/utils/feedback'
import { normalizeGoodsI18nId, shouldReloadGoodsI18n } from './goodsI18nId.mjs'

const props = defineProps<{
    goodsId?: number | string
    readOnly?: boolean
}>()

const langOptions = [
    { code: 'en', label: 'English' },
    { code: 'ja', label: '日本語' },
    { code: 'ko', label: '한국어' },
    { code: 'fr', label: 'Français' },
    { code: 'de', label: 'Deutsch' },
    { code: 'es', label: 'Español' }
]

type LangForm = {
    langCode: string
    commonName: string
    promotionTips: string
    commonBody: string
    specNameJson: string
    specValueJson: string
    source: string
    status: string
}

const createEmptyForm = (langCode: string): LangForm => ({
    langCode,
    commonName: '',
    promotionTips: '',
    commonBody: '',
    specNameJson: '',
    specValueJson: '',
    source: '',
    status: ''
})

const formMap = reactive<Record<string, LangForm>>({})
langOptions.forEach((l) => {
    formMap[l.code] = createEmptyForm(l.code)
})

const activeLang = ref('en')
const translating = ref(false)
const readOnly = computed(() => !!props.readOnly)
const goodsId = computed(() => props.goodsId)
const activeLanguageLabel = computed(
    () => langOptions.find((item) => item.code === activeLang.value)?.label || activeLang.value
)

function hasLanguageContent(form: LangForm) {
    return Boolean(
        form.commonName ||
            form.promotionTips ||
            form.commonBody ||
            form.specNameJson ||
            form.specValueJson
    )
}

function sourceLabel(source: string) {
    if (source === 'ai') return '自动翻译'
    if (source === 'corrected') return '人工校正'
    if (source === 'manual') return '人工填写'
    return source
}

function resetForms() {
    langOptions.forEach((l) => {
        Object.assign(formMap[l.code], createEmptyForm(l.code))
    })
}

async function loadList() {
    if (!goodsId.value) return
    try {
        const data: any = await getGoodsI18nList(goodsId.value)
        const rows = Array.isArray(data) ? data : data?.data || []
        resetForms()
        for (const row of rows) {
            const code = String(row.langCode || row.lang_code || '').toLowerCase()
            if (!formMap[code]) continue
            formMap[code].commonName = row.commonName || row.common_name || ''
            formMap[code].promotionTips = row.promotionTips || row.promotion_tips || ''
            formMap[code].commonBody = row.commonBody || row.common_body || ''
            formMap[code].specNameJson = row.specNameJson || row.spec_name_json || ''
            formMap[code].specValueJson = row.specValueJson || row.spec_value_json || ''
            formMap[code].source = row.source || ''
            formMap[code].status = row.status || ''
        }
    } catch (e: any) {
        feedback.msgError(e?.message || '加载多语言失败')
    }
}

function buildSaveItems() {
    return langOptions
        .map((l) => {
            const form = formMap[l.code]
            if (!hasLanguageContent(form)) return null
            return {
                langCode: l.code,
                commonName: form.commonName,
                promotionTips: form.promotionTips,
                commonBody: form.commonBody,
                specNameJson: form.specNameJson,
                specValueJson: form.specValueJson,
                source: form.source === 'ai' ? 'corrected' : form.source || 'manual',
                status: 'published'
            }
        })
        .filter(Boolean)
}

async function saveForGoods(targetGoodsId: number | string) {
    const items = buildSaveItems()
    if (!items.length) return false
    await saveGoodsI18n(targetGoodsId, { items })
    return true
}

async function handleAutoTranslate() {
    if (!goodsId.value) return
    translating.value = true
    try {
        await autoTranslateGoodsI18n(goodsId.value, {
            langCodes: [activeLang.value],
            publish: true
        })
        feedback.msgSuccess(`${activeLanguageLabel.value} 翻译完成`)
        await loadList()
    } catch (e: any) {
        feedback.msgError(e?.message || '一键翻译失败')
    } finally {
        translating.value = false
    }
}

watch(
    () => props.goodsId,
    (id, previousId) => {
        if (shouldReloadGoodsI18n(id, previousId)) {
            void loadList()
        } else if (!normalizeGoodsI18nId(id) && normalizeGoodsI18nId(previousId)) {
            resetForms()
        }
    },
    { immediate: true }
)

defineExpose({ saveForGoods })
</script>

<style scoped>
.language-tabs {
    min-height: 520px;
}

.language-form {
    padding: 20px 8px 8px;
}

.field-width,
.detail-editor {
    width: min(100%, 720px);
}

.detail-preview {
    min-height: 320px;
    max-height: 520px;
    padding: 12px;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
}
</style>
