import { defineStore } from 'pinia'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    permissions: [],
    roles: [],
    userPermissions: []
  }),
  
  getters: {
    hasPermission: (state) => (permission) => {
      return state.userPermissions.includes(permission)
    },
    hasRole: (state) => (role) => {
      return state.roles.includes(role)
    }
  },
  
  actions: {
    setPermissions(permissions) {
      this.permissions = permissions
    },
    setRoles(roles) {
      this.roles = roles
    },
    setUserPermissions(permissions) {
      this.userPermissions = permissions
    },
    clearPermissions() {
      this.permissions = []
      this.roles = []
      this.userPermissions = []
    }
  }
})

