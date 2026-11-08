<template>
    <div class="decoration-pages min-w-[1100px]">
        <!-- <el-card shadow="never" class="!border-none flex-1 flex" :body-style="{ flex: 1 }"> -->
        <div class="!border-none flex-1 flex" :body-style="{ flex: 1 }">
            <div class="flex items-start h-full justify-between w-full">
                <Menu v-model="activeMenu" :menus="menus" />
                <preview v-model="selectWidgetIndex" :pageData="getPageData" />
                <attr-setting class="w-[560px]" :widget="getSelectWidget" />
            </div>
        </div>
        <!-- </el-card> -->
        <footer-btns class="mt-4" :fixed="false" v-perms="['decorate:pages:save']">
            <el-button type="primary" @click="setData">保存</el-button>
        </footer-btns>
    </div>
</template>
<script lang="ts" setup name="decorationPages">
import Menu from '../component/pages/menu.vue'
import Preview from '../component/pages/preview.vue'
import AttrSetting from '../component/pages/attr-setting.vue'
import widgets from '../component/widgets'
import { getDecoratePages, setDecoratePages } from '@/api/decoration'
import { getNonDuplicateID } from '@/utils/util'
enum pagesTypeEnum {
    HOME = '0',
    SERVICE = '1'
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

const menus: Record<
    string,
    {
        id: number
        name: string
        content: any[]
    }
> = reactive({
    [pagesTypeEnum.HOME]: {
        id: 0,
        type: 0,
        name: '首页装修',
        content: generatePageData(['shop-info', 'recommend-goods', 'shop-cate-goods'])
    },
    [pagesTypeEnum.SERVICE]: {
        id: 1,
        type: 1,
        name: '客服设置',
        content: generatePageData(['customer-service'])
    }
})

const activeMenu = ref('0')
const selectWidgetIndex = ref(-1)
const getPageData = computed(() => {
    return menus[activeMenu.value]?.content ?? []
})
const getSelectWidget = computed(() => {
    return menus[activeMenu.value]?.content[selectWidgetIndex.value] ?? ''
})

const getData = async () => {
    const data = await getDecoratePages({ type: activeMenu.value })
    menus[String(data.type)].content = data.content
}

const setData = async () => {
    await setDecoratePages({
        ...menus[activeMenu.value],
        data: JSON.stringify(menus[activeMenu.value].content)
    })
    getData()
}

watch(
    activeMenu,
    () => {
        selectWidgetIndex.value = getPageData.value.findIndex((item) => !item.disabled)
        getData()
    },
    {
        immediate: true
    }
)
</script>
<style lang="scss" scoped>
.decoration-pages {
    min-height: calc(100vh - var(--navbar-height) - 80px);
    @apply flex flex-col;
}
</style>
