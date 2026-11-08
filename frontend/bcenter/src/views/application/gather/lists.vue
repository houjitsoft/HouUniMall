<template>
    <el-card class="!border-none" shadow="never">
        <el-alert
            type="warning"
            title="温馨提示：1、目前采集商品支持淘宝、天猫、京东、1688。2、在采集过程中，请不要进行任何操作。3、每行一个商品链接，每次最多可添加20个商品链接。"
            :closable="false"
            show-icon
        />
    </el-card>
    <el-card class="!border-none mt-2" shadow="never">
        <el-form ref="formRef" :model="formData" label-width="120px" :rules="rules" class="mt-2">
            <el-form-item label="采集金币" prop="brand">
                <span> {{ pageData.gather_money }} </span>
                <el-button type="primary" link class="ml-5" @click="handlecharge">充值</el-button>
            </el-form-item>
            <el-form-item label="商品链接" prop="links">
                <el-input
                    v-model="formData.links"
                    :rows="10"
                    type="textarea"
                    placeholder="请输入"
                />
            </el-form-item>
            <el-form-item label="商品类型" prop="brand">
                <el-radio-group v-model="formData.type">
                    <el-radio :label="0">实物商品</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="平台分类" prop="category_id">
                <el-select v-model="formData.category_id">
                    <el-option
                        v-for="item in CategoryLists"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                    ></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="商品分类" prop="shop_category_id">
                <el-select v-model="formData.shop_category_id">
                    <el-option
                        v-for="item in OtherLists"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                    ></el-option>
                </el-select>
                <router-link
                    :to="{
                        path: getRoutePath('mall.GoodsCategory/lists')
                    }"
                >
                    <el-button type="primary" link class="ml-5">新增分类</el-button>
                </router-link>
                <el-button type="primary" link @click="handleRefresh">刷新</el-button>
            </el-form-item>
        </el-form>
    </el-card>
    <footer-btns>
        <el-button type="primary" @click="handleSave">采集</el-button>
    </footer-btns>
    <ServerPop
        ref="serverPopRef"
        @close="showPop = false"
        @confirm="getData"
        v-if="showPop"
    ></ServerPop>
    <schedulePop
        ref="schedulePopRef"
        @close="showschedulePop = false"
        v-if="showschedulePop"
    ></schedulePop>
</template>
<script lang="ts" setup>
import { reactive, ref, nextTick, shallowRef } from 'vue'
import { getShopConfig } from '@/api/setting/shop'
import ServerPop from './serverPop.vue'
import schedulePop from './schedulePop.vue'
import { getShopCategoryLists, getGoodsCategoryOtherLists } from '@/api/goods/category'
import { getRoutePath } from '@/router'
import { apiGatherAdd } from '@/api/application/gather'

const showPop = ref(false)
const showschedulePop = ref(false)
const serverPopRef = shallowRef()
const schedulePopRef = shallowRef()
const CategoryLists = ref<any>([])
const OtherLists = ref<any>([])
const pageData = ref<any>({})
const formData = reactive({
    links: '',
    category_id: '',
    shop_category_id: '',
    type: 0
})
const rules = reactive<any>({
    links: [{ required: true, message: '请输入', trigger: 'blur' }],
    category_id: [{ required: true, message: '请输入', trigger: 'blur' }],
    shop_category_id: [{ required: true, message: '请输入', trigger: 'blur' }]
})
const getData = async () => {
    pageData.value = await getShopConfig()
    CategoryLists.value = await getShopCategoryLists()
    OtherLists.value = await getGoodsCategoryOtherLists()
}
const handleRefresh = async () => {
    await getData()
}
const handleSave = async () => {
    // 批量添加商品链接
    let arr = formData.links.split(/[(\r\n\s)\r\n\s]+/) // \n代表换行，\r代表回车，可以加个\s代表空格
    arr.forEach((item, idx) => {
        // 删除空项
        if (!item) {
            arr.splice(idx, 1)
        }
    })
    arr = Array.from(new Set(arr)) // 去重
    const { id } = await apiGatherAdd({ ...formData, links: arr })
    showschedulePop.value = true
    await nextTick()
    await schedulePopRef.value.open(id)
}
const handlecharge = async () => {
    showPop.value = true
    await nextTick()
    await serverPopRef.value.open()
}
getData()
</script>
