<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-page-header content="用户详情" @back="$router.back()" />
        </el-card>
        <el-card class="mt-4 !border-none" header="基本资料" shadow="never">
            <el-form ref="formRef" class="ls-form" :model="formData" label-width="120px">
                <div class="py-5 pl-20 mb-10 bg-page">
                    <div class="mb-3 text-tx-regular">用户头像</div>
                    <el-avatar :src="formData.avatar" :size="58" />
                </div>
                <el-form-item label="用户编号："> {{ formData.sn }} </el-form-item>
                <el-form-item label="用户昵称：">
                    {{ formData.nickname }}
                </el-form-item>
                <el-form-item label="真实姓名：">
                    {{ formData.real_name || '-' }}
                </el-form-item>
                <el-form-item label="性别：">
                    {{ formData.sex }}
                </el-form-item>
                <el-form-item label="联系电话：">
                    {{ formData.mobile || '-' }}
                </el-form-item>
                <el-form-item label="注册来源："> {{ formData.channel_text }} </el-form-item>
                <el-form-item label="注册时间："> {{ formData.create_time }} </el-form-item>
                <el-form-item label="最近登录时间："> {{ formData.login_time }} </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script lang="ts" setup name="consumerDetail">
import type { FormInstance } from 'element-plus'
import { getUserDetail, userEdit } from '@/api/consumer'
import { isEmpty } from '@/utils/util'

const route = useRoute()
const formData = reactive({
    avatar: '',
    create_time: '',
    login_time: '',
    mobile: '',
    nickname: '',
    real_name: 0,
    sex: 0,
    channel_text: '',
    sn: ''
})

const formRef = shallowRef<FormInstance>()

const getDetails = async () => {
    const data = await getUserDetail({
        id: route.query.id
    })
    Object.keys(formData).forEach((key) => {
        //@ts-ignore
        formData[key] = data[key]
    })
}

const handleEdit = async (value: string, field: string) => {
    if (isEmpty(value)) return
    await userEdit({
        id: route.query.id,
        field,
        value
    })
    getDetails()
}

getDetails()
</script>
