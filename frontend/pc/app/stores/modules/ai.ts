import { defineStore } from 'pinia'

export const useAiStore = defineStore('ai', {
    state: () => ({
        /** 当前选中的会话 ID，新建对话时为 null */
        currentConversationId: null as number | null
    }),

    getters: {
        getCurrentConversationId: (state) => state.currentConversationId
    },

    actions: {
        setCurrentConversationId(conversationId: number | null) {
            this.currentConversationId = conversationId
        },

        clearCurrentConversationId() {
            this.currentConversationId = null
        }
    },

    persist: {
        key: 'ai-assistant-state',
        storage: typeof window !== 'undefined' ? window.localStorage : undefined,
        paths: ['currentConversationId']
    } as any
})
