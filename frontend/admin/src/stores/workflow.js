import { defineStore } from 'pinia'

export const useWorkflowStore = defineStore('workflow', {
  state: () => ({
    processList: [],
    currentProcess: null,
    taskList: [],
    currentTask: null
  }),
  
  getters: {
    getProcessById: (state) => (id) => {
      return state.processList.find(process => process.id === id)
    },
    getTaskById: (state) => (id) => {
      return state.taskList.find(task => task.id === id)
    }
  },
  
  actions: {
    setProcessList(list) {
      this.processList = list
    },
    setCurrentProcess(process) {
      this.currentProcess = process
    },
    setTaskList(list) {
      this.taskList = list
    },
    setCurrentTask(task) {
      this.currentTask = task
    },
    clearWorkflowData() {
      this.processList = []
      this.currentProcess = null
      this.taskList = []
      this.currentTask = null
    }
  }
})

