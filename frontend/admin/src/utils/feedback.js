import { ElMessage } from 'element-plus'

export const msgError = (message) => {
    ElMessage.error(message)
}

export const msgSuccess = (message) => {
    ElMessage.success(message)
}

export default {
    msgError,
    msgSuccess
}
