<template>
    <div class="goods-lists">
        <el-card class="!border-none" shadow="never">
            <el-alert
                type="warning"
                title="温馨提示：1、商家发布商品需要平台审核，审核通过后，可以调整上下架状态；2、编辑未审核通过商品，可重新提交审核；审核通过后，重新编辑商品无需再次审核。"
                :closable="false"
                show-icon
            />

            <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
                <el-form-item label="商品名称">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.name"
                        placeholder="请输入名称"
                        clearable
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="平台分类">
                    <el-cascader
                        class="w-[280px]"
                        v-model="queryParams.category_id"
                        :options="shopCategoryList"
                        :props="props"
                        clearable
                        filterable
                    />
                </el-form-item>
                <el-form-item label="商品分类" prop="shop_category_id">
                    <el-select class="w-[280px]" v-model="queryParams.shop_category_id">
                        <el-option
                            v-for="(itme, index) in otherList"
                            :key="itme.id"
                            :label="itme.name"
                            :value="itme.id"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                    <ai-agent-panel
                        class="ml-2"
                        code="stock_alert"
                        label="AI 补货建议"
                        :payload="buildStockPayload"
                    />
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
            <!-- 选项卡 -->
            <el-tabs v-model="queryParams.type" @tab-change="changeTabs">
                <template v-for="(tabItem, tabIndex) in tabsParams.TabsEnumMap" :key="tabIndex">
                    <el-tab-pane
                        :label="`${tabItem.label}(${pager.extend[tabItem.type] || 0})`"
                        :name="tabItem.name"
                    />
                </template>
            </el-tabs>
            <el-button type="primary">
                <router-link
                    :to="{
                        path: getRoutePath('mall.Goods/add:edit')
                    }"
                >
                    + 发布商品
                </router-link>
            </el-button>

            <!-- 表格 -->
            <el-table size="large" v-loading="pager.loading" :data="pager.lists" class="mt-4">
                <el-table-column label="商品货号" prop="code" width="140" show-overflow-tooltip />
                <el-table-column label="商品信息" min-width="300">
                    <template #default="{ row }">
                        <div class="flex">
                            <el-image
                                fit="cover"
                                :src="row.image"
                                class="flex-none w-[58px] h-[58px]"
                            />
                            <div class="ml-4 overflow-hidden">
                                <el-tooltip effect="dark" :content="row.name" placement="top">
                                    <div class="text-base line-2 mb-[3px]">
                                        {{ row.name }}
                                    </div>
                                </el-tooltip>
                                <el-tag checked v-if="row.sku_type">多规格</el-tag>
                                <el-tag checked class="ml-[8px]" type="danger" v-if="row.ms"
                                    >秒杀</el-tag
                                >
                                <el-tag checked class="ml-[8px]" type="success" v-if="row.pt"
                                    >拼团</el-tag
                                >
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="价格" prop="price" min-width="160">
                    <template #default="{ row }"> ¥{{ row.price_text }} </template>
                </el-table-column>
                <el-table-column label="库存" prop="total_stock" min-width="90" />
                <el-table-column label="销量" prop="sale_num" min-width="90" />
                <el-table-column label="排序" prop="sort" min-width="90" />
                <el-table-column label="销售状态" min-width="90">
                    <template #default="{ row }">
                        <el-tag v-if="row?.status == 1" type="success">销售中</el-tag>
                        <el-tag type="info" v-else>仓库中</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="create_time" min-width="180" />
                <el-table-column label="操作" width="220" fixed="right">
                    <template #default="{ row }">
                        <el-button v-perms="['user.user/detail']" type="primary" link>
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.Goods/add:edit'),
                                    query: {
                                        id: row.id,
                                        read_only: 1
                                    }
                                }"
                            >
                                详情
                            </router-link>
                        </el-button>

                        <el-button
                            v-perms="['article.article/edit', 'article.article/add:edit']"
                            type="primary"
                            link
                        >
                            <router-link
                                :to="{
                                    path: getRoutePath('mall.Goods/add:edit'),
                                    query: {
                                        id: row.id
                                    }
                                }"
                            >
                                编辑
                            </router-link>
                        </el-button>

                        <el-button
                            v-perms="['article.article/delete']"
                            type="primary"
                            link
                            @click="handleStatus(row.status === 1 ? 0 : 1, row.id)"
                        >
                            {{ row.status === 0 ? '上架' : '下架' }}
                        </el-button>

                        <el-button
                            v-perms="['article.article/delete']"
                            type="danger"
                            link
                            @click="handleDelete(row.id)"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script lang="ts" setup name="GoodsLists">
import AiAgentPanel from '@/components/ai-agent-panel/index.vue'
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import { getGoodsLists, apiGoodsStatus, apiGoodsDel } from '@/api/goods/goods'
import { getGoodsCategoryOtherLists, getShopCategoryLists } from '@/api/goods/category'
import type { GoodsList } from '@/api/goods/goods'
import feedback from '@/utils/feedback'

// 分类组件配置数据
const props = reactive({
    multiple: false,
    checkStrictly: true,
    label: 'name',
    value: 'id',
    children: 'sons',
    emitPath: false
})

// Tab类型
enum TabsEnum {
    COYUNT_SALE = 'count_sale',
    COUNT_AUDIT = 'count_audit',
    COUNT_AUDIT_REFUSE = 'count_audit_refuse',
    COUNT_STOCK_WARNING = 'count_stock_warning',
    NOT_SALE = 'count_not_sale'
}

const tabsParams = reactive({
    TabsEnumMap: [
        {
            label: '销售中商品',
            name: 'sale',
            type: TabsEnum.COYUNT_SALE
        },
        {
            label: '待审核商品',
            name: 'audit',
            type: TabsEnum.COUNT_AUDIT
        },
        {
            label: '审核未通过商品',
            name: 'audit_refuse',
            type: TabsEnum.COUNT_AUDIT_REFUSE
        },
        {
            label: '库存预警',
            name: 'stock_warning',
            type: TabsEnum.COUNT_STOCK_WARNING
        },
        {
            label: '仓库中商品',
            name: 'not_sale',
            type: TabsEnum.NOT_SALE
        }
    ]
})

const queryParams = reactive<GoodsList>({
    name: '',
    category_id: '',
    shop_category_id: '',
    type: 'sale'
})

// 商品分类
const otherList = ref([])
// 平台分类
const shopCategoryList = ref([])

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getGoodsLists,
    params: queryParams
})

const changeTabs = (name: string) => {
    queryParams.type = name
    getLists()
}

const handleStatus = async (status: number, id: number) => {
    try {
        await feedback.confirm(`确定要${status ? '上架吗' : '下架吗'}？`)
        await apiGoodsStatus({ id, status })
        getLists()
    } catch (error) {
        console.log('上下架商品失败=>', error)
    }
}

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm('确定要删除？')
        await apiGoodsDel({ id })
        getLists()
    } catch (error) {
        console.log('删除商品失败=>', error)
    }
}

const getOtherList = async () => {
    try {
        const data = await getGoodsCategoryOtherLists()
        otherList.value = data
    } catch (error) {
        console.log(error)
    }
}

const getShopCateLists = async () => {
    try {
        const lists: any = await getShopCategoryLists()
        shopCategoryList.value = lists
    } catch (error) {
        console.log(error)
    }
}

onActivated(() => {
    getLists()
})

getLists()
getOtherList()
getShopCateLists()

/**
 * 库存与补货预警：只提交本页已有的库存数据，
 * 销量/在途/采购周期列表页没有，明确标注「未提供」交给模型判断，不在这里编造。
 */
const buildStockPayload = () => {
    const rows = (pager.lists || []).filter((r: any) => r && r.name)
    if (!rows.length) return ''
    const lines = rows.slice(0, 30).map((r: any, i: number) => {
        const stock = r.total_stock ?? r.stock ?? '未提供'
        const warn = r.stock_warning ?? r.warning_stock ?? '未设置'
        return `${i + 1}. ${r.name}｜当前库存：${stock}｜预警值：${warn}`
    })
    return `请评估以下商品的库存健康度并给补货建议（销量、在途、采购周期未提供，请按库存与预警值判断并说明依据）：\n${lines.join('\n')}`
}
</script>
