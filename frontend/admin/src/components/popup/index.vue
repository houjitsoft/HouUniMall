<template>
    <div class="dialog">
        <div class="dialog__trigger" @click="open">
            <!-- 触发弹窗 -->
            <slot name="trigger"></slot>
        </div>
        <el-dialog
            v-model="visible"
            :custom-class="customClass"
            :center="center"
            :append-to-body="true"
            :width="width"
            :close-on-click-modal="clickModalClose"
            @closed="close"
        >
            <!-- 弹窗内容 -->
            <template v-if="title" #header>{{ title }}</template>

            <!-- 自定义内容 -->
            <slot>{{ content }}</slot>
            <!-- 底部弹窗页脚 -->
            <template #footer>
                <div class="dialog-footer" :class="{ 'flex justify-center': footerCenter }">
                    <el-button v-if="cancelButtonText" @click="handleEvent('cancel')">
                        {{ cancelButtonText }}
                    </el-button>
                    <el-button
                        v-if="confirmButtonText"
                        type="primary"
                        @click="handleEvent('confirm')"
                    >
                        {{ confirmButtonText }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script>
// 普通 <script> 不会像 <script setup> 那样自动注入 Vue API，
// 必须显式 import；否则构建期不报错、运行到装修编辑器的属性面板时
// 抛 ReferenceError: defineComponent is not defined。
import { defineComponent, ref, nextTick, provide } from 'vue'

export default defineComponent({
    props: {
        title: {
            type: String,
            default: ''
        },
        content: {
            type: String,
            default: ''
        },
        confirmButtonText: {
            type: [String, Boolean],
            default: '确定'
        },
        cancelButtonText: {
            type: [String, Boolean],
            default: '取消'
        },
        width: {
            type: String,
            default: '400px'
        },
        disabled: {
            type: Boolean,
            default: false
        },
        async: {
            type: Boolean,
            default: false
        },
        clickModalClose: {
            type: Boolean,
            default: false
        },
        center: {
            type: Boolean,
            default: false
        },
        customClass: {
            type: String,
            default: ''
        },
        footerCenter: {
            type: Boolean,
            default: false
        }
    },
    emits: ['confirm', 'cancel', 'close', 'open'],
    setup(props, { emit }) {
        const visible = ref(false)

        const handleEvent = (type) => {
            emit(type)
            if (!props.async || type === 'cancel') {
                close()
            }
        }

        const close = () => {
            visible.value = false
            nextTick(() => {
                emit('close')
            })
        }
        const open = () => {
            if (props.disabled) {
                return
            }
            emit('open')
            visible.value = true
        }
        provide('visible', visible)
        return {
            visible,
            handleEvent,
            close,
            open
        }
    }
})
</script>

<style scoped lang="scss">
.dialog-body {
    white-space: pre-line;
}
</style>
