<template>
    <div class="upload-files">
        <div class="file-list">
            <div v-for="(file, index) in fileList" :key="index" class="file-item">
                <div class="file-info">
                    <span class="file-icon">{{ getFileIcon(file.type) }}</span>
                    <span class="file-name" :title="file.name">{{ $t(file.name || '') }}</span>
                </div>
                <button type="button" class="remove-btn" @click="handleRemove(index)" :disabled="uploading">
                    ×
                </button>
            </div>
        </div>

        <div v-if="fileList.length < maxCount" class="upload-area" :class="{ 'uploading': uploading }"
            @click="triggerFileUpload">
            <input ref="fileInputRef" type="file" :accept="accept" :multiple="multiple" class="file-input"
                @change="handleFileUpload" />
            <div v-if="!uploading" class="upload-placeholder">
                <i class="upload-icon">📎</i>
                <div class="upload-text">{{ $t(placeholder) }}</div>
            </div>
            <div v-else class="upload-loading">
                <div class="loading-spinner"></div>
                <div class="loading-text">{{ $t('上传中...') }}</div>
            </div>
        </div>

        <div v-if="hint" class="field-hint">{{ $t(hint) }}</div>
        <div v-if="error" class="field-error">{{ $t(error) }}</div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { uploadFileToOSS } from '~/services/thirdParty.service'
import { Toast } from '~/composables/useToast'
import type { FileItem } from '~/types/component'

interface Props {
    modelValue: string[] // 文件URL数组
    accept?: string // 接受的文件类型
    placeholder?: string
    hint?: string
    maxCount?: number // 最大上传数量
    maxSize?: number // 最大文件大小（字节）
    multiple?: boolean // 是否支持多选
}

const props = withDefaults(defineProps<Props>(), {
    accept: '*',
    placeholder: '点击上传文件',
    maxCount: 3,
    maxSize: 10 * 1024 * 1024, // 默认10MB
    multiple: true
})

const emit = defineEmits<{
    'update:modelValue': [value: string[]]
    'upload-success': [urls: string[]]
    'upload-error': [error: string]
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const error = ref<string | null>(null)
const fileList = ref<FileItem[]>([])

// 监听 modelValue 变化，更新文件列表
watch(
    () => props.modelValue,
    (urls) => {
        if (urls && urls.length > 0) {
            // 从URL推断文件名和类型
            fileList.value = urls.map(url => ({
                name: getFileNameFromUrl(url),
                url: url,
                type: getFileTypeFromUrl(url)
            }))
        } else {
            fileList.value = []
        }
    },
    { immediate: true }
)

/**
 * 从URL获取文件名
 */
const getFileNameFromUrl = (url: string): string => {
    try {
        const urlObj = new URL(url)
        const pathname = urlObj.pathname
        const fileName = pathname.split('/').pop() || '文件'
        return decodeURIComponent(fileName)
    } catch {
        return '文件'
    }
}

/**
 * 从URL获取文件类型
 */
const getFileTypeFromUrl = (url: string): string => {
    const extension = url.split('.').pop()?.toLowerCase() || ''
    if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(extension)) {
        return 'image'
    } else if (['pdf'].includes(extension)) {
        return 'pdf'
    } else if (['doc', 'docx'].includes(extension)) {
        return 'word'
    } else {
        return 'file'
    }
}

/**
 * 获取文件图标
 */
const getFileIcon = (type: string): string => {
    const iconMap: Record<string, string> = {
        image: '🖼️',
        pdf: '📄',
        word: '📝',
        file: '📎'
    }
    return iconMap[type] || '📎'
}

/**
 * 验证文件
 */
const validateFile = (file: File): string | null => {
    // 验证文件大小
    if (file.size > props.maxSize) {
        const maxSizeMB = (props.maxSize / (1024 * 1024)).toFixed(0)
        return `文件大小不能超过${maxSizeMB}MB`
    }

    // 验证文件类型
    if (props.accept !== '*') {
        const allowedTypes = props.accept.split(',').map(type => type.trim())
        const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()
        const fileType = file.type

        const isAllowed = allowedTypes.some(type => {
            if (type.startsWith('.')) {
                return fileExtension === type.toLowerCase()
            } else {
                return fileType === type
            }
        })

        if (!isAllowed) {
            return `不支持的文件格式，请选择：${props.accept}`
        }
    }

    return null
}

/**
 * 触发文件选择
 */
const triggerFileUpload = () => {
    if (uploading.value) return
    if (fileList.value.length >= props.maxCount) {
        Toast.warning(`最多只能上传${props.maxCount}个文件`)
        return
    }
    fileInputRef.value?.click()
}

/**
 * 处理文件上传
 */
const handleFileUpload = async (event: Event) => {
    const target = event.target as HTMLInputElement
    const files = target.files
    if (!files || files.length === 0) return

    // 重置错误状态
    error.value = null

    // 检查文件数量
    const remainingSlots = props.maxCount - fileList.value.length
    if (files.length > remainingSlots) {
        Toast.warning(`最多只能上传${remainingSlots}个文件`)
        if (fileInputRef.value) {
            fileInputRef.value.value = ''
        }
        return
    }

    // 验证所有文件
    const filesArray = Array.from(files)
    for (const file of filesArray) {
        const validationError = validateFile(file)
        if (validationError) {
            error.value = validationError
            emit('upload-error', validationError)
            if (fileInputRef.value) {
                fileInputRef.value.value = ''
            }
            return
        }
    }

    uploading.value = true

    try {
        const uploadPromises = filesArray.map(file => uploadSingleFile(file))
        const urls = await Promise.all(uploadPromises)

        // 过滤掉失败的上传（返回null的）
        const successUrls = urls.filter((url): url is string => url !== null)

        if (successUrls.length > 0) {
            const newUrls = [...props.modelValue, ...successUrls]
            emit('update:modelValue', newUrls)
            emit('upload-success', successUrls)
            error.value = null
            // 如果部分文件上传成功，显示成功提示
            if (successUrls.length < filesArray.length) {
                Toast.warning(`成功上传 ${successUrls.length}/${filesArray.length} 个文件`)
            }
        } else {
            // 所有文件都失败时才显示错误
            const errorMsg = '所有文件上传失败'
            error.value = errorMsg
            emit('upload-error', errorMsg)
            Toast.error(errorMsg)
        }
    } catch (err: any) {
        const errorMsg = err.message || '上传失败，请重试'
        error.value = errorMsg
        emit('upload-error', errorMsg)
        Toast.error(errorMsg)
    } finally {
        uploading.value = false
        // 清空input，允许重复选择同一文件
        if (fileInputRef.value) {
            fileInputRef.value.value = ''
        }
    }
}

/**
 * 上传单个文件
 */
const uploadSingleFile = async (file: File): Promise<string | null> => {
    try {
        const formData = new FormData()
        formData.append('file', file)

        const response = await uploadFileToOSS(formData)

        // 检查响应结构，支持多种可能的响应格式
        if (response.code === 1) {
            // 如果 data 是对象且包含 url 属性
            if (response.data && typeof response.data === 'object' && 'url' in response.data) {
                return (response.data as { url: string }).url
            }
            // 如果 data 直接是字符串（URL）
            if (typeof response.data === 'string') {
                return response.data
            }
            // 如果 data 是对象但没有 url，尝试直接使用 data
            if (response.data) {
                console.warn('上传响应结构异常:', response.data)
                throw new Error('上传响应格式不正确')
            }
        }

        throw new Error(response.message || '上传失败')
    } catch (err: any) {
        console.error('文件上传失败:', err)
        Toast.error(`${file.name} 上传失败：${err.message || '未知错误'}`)
        return null
    }
}

/**
 * 删除文件
 */
const handleRemove = (index: number) => {
    if (uploading.value) return

    const newUrls = [...props.modelValue]
    newUrls.splice(index, 1)
    emit('update:modelValue', newUrls)
}
</script>

<style lang="scss" scoped>
.upload-files {
    width: 100%;
}

.file-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;
}

.file-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    background: #fafafa;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover {
        background: #f5f5f5;
        border-color: #d9d9d9;
    }

    .file-info {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;
        min-width: 0;

        .file-icon {
            font-size: 18px;
            flex-shrink: 0;
        }

        .file-name {
            font-size: 14px;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }

    .remove-btn {
        width: 24px;
        height: 24px;
        border: none;
        background: transparent;
        color: #999;
        font-size: 20px;
        line-height: 1;
        cursor: pointer;
        transition: all 0.2s ease;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 4px;

        &:hover:not(:disabled) {
            background: #ff4d4f;
            color: #fff;
        }

        &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
    }
}

.upload-area {
    border: 2px dashed #e8e8e8;
    border-radius: 8px;
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #fafafa;
    position: relative;

    &:hover:not(.uploading) {
        border-color: var(--color-primary, #ff4d4f);
        background: #fff;
    }

    &.uploading {
        cursor: not-allowed;
        opacity: 0.7;
    }

    .file-input {
        display: none;
    }

    .upload-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        .upload-icon {
            font-size: 32px;
        }

        .upload-text {
            font-size: 14px;
            color: #666;
        }
    }

    .upload-loading {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        .loading-spinner {
            width: 24px;
            height: 24px;
            border: 3px solid #f3f3f3;
            border-top: 3px solid var(--color-primary, #ff4d4f);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        .loading-text {
            font-size: 14px;
            color: #666;
        }
    }
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.field-hint {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 6px;
}

.field-error {
    font-size: 12px;
    color: #ff4d4f;
    margin-top: 6px;
}
</style>
