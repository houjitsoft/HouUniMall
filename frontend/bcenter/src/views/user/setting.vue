<template>
    <div class="user-setting">
        <el-card class="!border-none" shadow="never">
            <el-form ref="formRef" :model="formData" label-width="100px">
                <el-form-item label="头像"><material-picker v-model="formData.face" :limit="1" /></el-form-item>
                <el-form-item label="统一账号"><el-input v-model="formData.login_account" class="w-80" disabled /></el-form-item>
                <el-form-item label="名称"><el-input v-model="formData.name" class="w-80" /></el-form-item>
                <el-form-item label="当前密码"><el-input v-model.trim="formData.update_current_password" class="w-80" type="password" show-password /></el-form-item>
                <el-form-item label="新密码"><el-input v-model.trim="formData.update_new_password" class="w-80" type="password" show-password /></el-form-item>
                <el-form-item label="确认密码"><el-input v-model.trim="formData.update_confirm_password" class="w-80" type="password" show-password /></el-form-item>
            </el-form>
        </el-card>
        <footer-btns><el-button type="primary" @click="handleSubmit">保存</el-button></footer-btns>
    </div>
</template>

<script setup lang="ts" name="userSetting">
import { setUserInfo, getShopUserInfo } from '@/api/user'
import useUserStore from '@/stores/modules/user'
import feedback from '@/utils/feedback'
const userStore = useUserStore()
const formData = reactive<any>({ face: '', login_account: '', name: '', update_current_password: '', update_new_password: '', update_confirm_password: '' })
const getUser = async () => Object.assign(formData, await getShopUserInfo())
const handleSubmit = async () => {
    const hasPassword = formData.update_current_password || formData.update_new_password || formData.update_confirm_password
    if (hasPassword && (!formData.update_current_password || !formData.update_new_password || !formData.update_confirm_password)) return feedback.msgError('修改密码请完整填写三项')
    if (hasPassword && formData.update_new_password !== formData.update_confirm_password) return feedback.msgError('两次输入的密码不一致')
    await setUserInfo(formData)
    userStore.getUserInfo()
}
getUser()
</script>
