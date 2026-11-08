<template>
    <div class="upload-images">
        <div class="upload-area" @click="triggerFileUpload" :class="{ 'has-file': modelValue, 'uploading': uploading }">
            <input ref="fileInputRef" type="file" :accept="accept" @change="handleFileUpload" class="file-input" />
            <div v-if="!modelValue && !uploading" class="upload-placeholder">
                <i class="upload-icon">☁️</i>
                <div class="upload-text">{{ $t(placeholder) }}</div>
            </div>
            <div v-else-if="uploading" class="upload-loading">
                <div class="loading-spinner"></div>
                <div class="loading-text">{{ $t('上传中...') }}</div>
            </div>
            <div v-else class="upload-preview">
                <img :src="modelValue" :alt="alt" class="preview-image" />
                <button type="button" class="remove-file" @click.stop="handleRemove">×</button>
            </div>
        </div>
        <div v-if="hint" class="field-hint">{{ $t(hint) }}</div>
        <div v-if="error" class="field-error">{{ $t(error) }}</div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadImageToOSS } from '~/services/thirdParty.service'
import { compressImage } from '~/utils/compressImage'
import type { UploadImagesProps } from '~/types/component'

type Props = UploadImagesProps

const props = withDefaults(defineProps<Props>(), {
    accept: 'image/png,image/jpg,image/jpeg',
    placeholder: '点击上传图片',
    alt: '预览图片',
    maxSize: 5 * 1024 * 1024, // 默认5MB
    streamName: undefined,
    compressQuality: 0.7
})

const emit = defineEmits<{
    'update:modelValue': [value: string]
    'upload-success': [url: string]
    'upload-error': [error: string]
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const error = ref<string | null>(null)

const triggerFileUpload = () => {
    if (uploading.value) return
    fileInputRef.value?.click()
}

const validateFileType = (file: File): string | null => {
    const allowedTypes = props.accept.split(',').map(type => type.trim())
    if (!allowedTypes.includes(file.type)) {
        const formatNames = allowedTypes
            .map(type => type.split('/')[1])
            .filter(Boolean)
            .join('、')
        return `只支持${formatNames}格式`
    }
    return null
}

const validateFileSize = (file: File): string | null => {
    if (file.size > props.maxSize) {
        const maxSizeMB = (props.maxSize / 1024 / 1024).toFixed(0)
        return `文件大小不能超过${maxSizeMB}MB`
    }
    return null
}

const convertToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = (e) => {
            const result = e.target?.result
            if (!result || typeof result !== 'string') {
                reject(new Error('文件读取失败'))
                return
            }
            // 移除 data:image/xxx;base64, 前缀，只保留base64字符串
            const base64Str = result.includes(',') ? result.split(',')[1] : result
            if (!base64Str) {
                reject(new Error('文件读取失败：无法提取base64数据'))
                return
            }
            resolve(base64Str)
        }
        reader.onerror = () => reject(new Error('文件读取失败'))
        reader.readAsDataURL(file)
    })
}

const handleFileUpload = async (event: Event) => {
    const target = event.target as HTMLInputElement
    const file = target.files?.[0]
    if (!file) return

    // 重置错误状态
    error.value = null

    // 先校验类型，大小在压缩后再比
    const typeError = validateFileType(file)
    if (typeError) {
        error.value = typeError
        emit('upload-error', typeError)
        if (fileInputRef.value) {
            fileInputRef.value.value = ''
        }
        return
    }

    uploading.value = true

    try {
        const uploadFile = await compressImage(file, props.compressQuality)
        const sizeError = validateFileSize(uploadFile)
        if (sizeError) {
            error.value = sizeError
            emit('upload-error', sizeError)
            return
        }

        const base64Str = await convertToBase64(uploadFile)
        const streamName = props.streamName || uploadFile.name

        const response = await uploadImageToOSS({
            base64Str,
            streamName
        })

        if (response.code === 1 && response.data) {
            emit('update:modelValue', response.data)
            emit('upload-success', response.data)
            error.value = null
        } else {
            throw new Error(response.message || '上传失败')
        }
    } catch (err: any) {
        const errorMsg = err.message || '上传失败，请重试'
        error.value = errorMsg
        emit('upload-error', errorMsg)
    } finally {
        uploading.value = false
        // 清空input，允许重复选择同一文件
        if (fileInputRef.value) {
            fileInputRef.value.value = ''
        }
    }
}

const handleRemove = () => {
    emit('update:modelValue', '')
    error.value = null
}
</script>

<style lang="scss" scoped>
.upload-images {
    width: 100%;
}

.upload-area {
    border: 2px dashed #e0e0e0;
    border-radius: 8px;
    padding: 40px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    background-color: #fff;

    &:hover:not(.uploading) {
        border-color: var(--color, #4caf50);
        background-color: #f9f9f9;
    }

    &.has-file {
        border-style: solid;
        padding: 10px;
    }

    &.uploading {
        cursor: not-allowed;
        opacity: 0.7;
    }

    .file-input {
        display: none;
    }

    .upload-placeholder {
        .upload-icon {
            font-size: 48px;
            display: block;
            margin-bottom: 12px;
        }

        .upload-text {
            font-size: 14px;
            color: #999;
        }
    }

    .upload-loading {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 20px;

        .loading-spinner {
            width: 40px;
            height: 40px;
            border: 3px solid #f3f3f3;
            border-top: 3px solid var(--color, #4caf50);
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-bottom: 12px;
        }

        .loading-text {
            font-size: 14px;
            color: #666;
        }
    }

    .upload-preview {
        position: relative;
        display: inline-block;

        .preview-image {
            max-width: 100%;
            max-height: 200px;
            border-radius: 4px;
            display: block;
        }

        .remove-file {
            position: absolute;
            top: -10px;
            right: -10px;
            width: 24px;
            height: 24px;
            border-radius: 50%;
            background-color: var(--color, #f44336);
            color: white;
            border: none;
            font-size: 18px;
            line-height: 1;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s ease;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

            &:hover {
                background-color: #d32f2f;
                transform: scale(1.1);
            }
        }
    }
}

.field-hint,
.field-error {
    margin-top: 6px;
    font-size: 12px;
    line-height: 1.5;
}

.field-hint {
    color: #999;
}

.field-error {
    color: #f44336;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}
</style>
