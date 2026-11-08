<template>
    <div class="decoration-pages min-w-[1100px]">
        <div class="flex flex-1 h-full justify-between">
            <el-card
                shadow="never"
                class="!border-none flex scroll-view-content"
                :body-style="{ 'padding-right': 0 }"
            >
                <componentSelect
                    @addwidgetName="addwidgetName"
                    @update:title="updatetitle"
                    @resetdragIndex="resetdragIndex"
                    :title="title"
                />
            </el-card>

            <preview
                class="flex-1 scroll-view-content"
                ref="previewRef"
                v-model="selectWidgetIndex"
                @delwidget="delwidget"
                @copywidget="copywidget"
                @addwidget="addwidget"
                @updatePageData="updatePageData"
                :pageData="getPageData"
                :pageMeta="getPageMeta"
            />

            <attr-setting
                class="w-[460px] scroll-view-content"
                :widget="getSelectWidget"
                @update:content="updateContent"
            />
        </div>
        <footer-btns class="mt-4" :fixed="false" v-perms="['decorate:pages:save']">
            <el-button type="primary" @click="setData">保存</el-button>
        </footer-btns>
    </div>
</template>
<script lang="ts" setup name="decorationPages">
import componentSelect from '../component/pages/component-select.vue'
import Preview from '../component/pages/preview.vue'
import AttrSetting from '../component/pages/attr-setting.vue'
import widgets from '../component/widgets'
import { getDecoratePages, setDecoratePages } from '@/api/decoration'
import { getNonDuplicateID } from '@/utils/util'
import { addPages } from '@/api/decoration'
const route = useRoute()
const router = useRouter()
enum pagesTypeEnum {
    HOME = '1'
}
const previewRef = shallowRef()
const resetdragIndex = () => {
    previewRef.value.resetdragIndex()
}
const generatePageData = (widgetNames: string[]) => {
    return widgetNames.map((widgetName) => {
        const options = {
            id: getNonDuplicateID(),
            ...(widgets[widgetName]?.options() || {})
        }
        return options
    })
}
const title = ref('默认页面')
const widgetName = ref('')
const widgetIndex = ref('')
const addwidgetName = (Name: string) => {
    widgetName.value = Name
}
const updatetitle = (value: string) => {
    title.value = value
}
const addwidget = (Index: any, position: string) => {
    widgetIndex.value = Index
    if (Index >= 0) {
        if (position == 'bottom') {
            Index++
        }
        menus[activeMenu.value].pageData.splice(Index, 0, ...generatePageData([widgetName.value]))
    }
    if (Index == -1) {
        menus[activeMenu.value].pageData.push(...generatePageData([widgetName.value]))
        Index = menus[activeMenu.value].pageData.length - 1
    }
}
const delwidget = (index: number) => {
    menus[activeMenu.value].pageData.splice(index, 1)
}
const copywidget = (index: any, componentName: any) => {
    menus[activeMenu.value].pageData.splice(index, 0, ...generatePageData([componentName]))
}
const updatePageData = (value: any) => {
    menus[activeMenu.value].pageData = [...value]
}

const menus: Record<
    string,
    {
        id: number | string
        type: string
        name: string
        pageMeta?: any
        pageData: any[]
    }
> = reactive({
    [pagesTypeEnum.HOME]: {
        id: '',
        type: '',
        name: '',
        pageMeta: generatePageData(['page-meta']),
        pageData: generatePageData([])
    }
})

const activeMenu = ref('1')
const selectWidgetIndex = ref(-1)
const getPageData = computed(() => {
    return menus[activeMenu.value]?.pageData ?? []
})
const getSelectWidget = computed(() => {
    if (selectWidgetIndex.value === -1) {
        return menus[activeMenu.value]?.pageMeta[0] ?? ''
    } else {
        return menus[activeMenu.value]?.pageData[selectWidgetIndex.value] ?? ''
    }
})

const getData = async () => {
    if (route.query.id) {
        const data = await getDecoratePages({ id: route.query.id })
        menus[pagesTypeEnum.HOME].pageMeta = data.content[0]
        menus[pagesTypeEnum.HOME].pageData = data.content.slice(1)
        menus[pagesTypeEnum.HOME].id = data.id
        menus[pagesTypeEnum.HOME].type = data.type
        title.value = data.name
    }
}
const getPageMeta = computed(() => {
    return menus[activeMenu.value]?.pageMeta ?? null
})
const setData = async () => {
    if (route.query.id) {
        await setDecoratePages({
            id: menus[pagesTypeEnum.HOME].id,
            type: menus[pagesTypeEnum.HOME].type,
            name: title.value,
            content: JSON.stringify([
                menus[pagesTypeEnum.HOME].pageMeta,
                ...menus[activeMenu.value].pageData
            ])
        })
        getData()
    } else {
        const id = await addPages({
            type: 0,
            name: title.value,
            content: JSON.stringify([
                menus[pagesTypeEnum.HOME].pageMeta,
                ...menus[activeMenu.value].pageData
            ])
        })
        // 新增成功后更新本地状态并跳转到编辑页面
        menus[pagesTypeEnum.HOME].id = id
        menus[pagesTypeEnum.HOME].type = '0'
        router.replace({ path: route.path, query: { id } })
    }
}
const updateContent = (content: any) => {
    if (menus[activeMenu.value]?.pageData) {
        menus[activeMenu.value].pageData[selectWidgetIndex.value].content = content
    }
}
getData()
</script>
<style lang="scss" scoped>
$scroll-height: calc(100vh - var(--navbar-height) - 74px);
.decoration-pages {
    height: $scroll-height;
    @apply flex flex-col;
    .scroll-view-content {
        height: calc($scroll-height - 60px);
    }
}
</style>
