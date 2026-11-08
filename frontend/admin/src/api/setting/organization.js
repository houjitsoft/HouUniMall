/**
 * 组织管理API接口
 * 使用主服务的统一API服务层
 */

// 导入主服务的统一API服务
import request from '@/utils/request'

/** commons 分页：{ code, data: { records, total } } → 同时挂载 records/total 便于页面读取 */
function normalizePageResponse(response) {
  if (!response || (response.code !== 1 && response.code !== 200)) {
    return response
  }
  const pageData = response.data || {}
  if (pageData.records != null) {
    return {
      ...response,
      records: pageData.records,
      total: pageData.total ?? 0,
      current: pageData.current,
      size: pageData.size,
      pages: pageData.pages
    }
  }
  return response
}

/**
 * 组织管理API接口
 */
export const organizationService = {
  // 分页查询组织信息
  getOrganizationPage: async (params) => {
    try {
      const response = await request.post('/web/base/organization/queryOrganizationPage', params)
      return normalizePageResponse(response)
    } catch (error) {
      console.error('分页查询组织信息失败:', error)
      throw error
    }
  },

  // 查询组织树形结构
  getOrganizationTree: async (params) => {
    try {
      const response = await request.post('/web/base/organization/queryOrganizationTree', params)
      return response
    } catch (error) {
      console.error('查询组织树形结构失败:', error)
      throw error
    }
  },

  // 根据ID查询组织信息
  getOrganizationById: async (orgId) => {
    try {
      const response = await request.get('/web/base/organization/queryOrganizationById', {
        params: { orgId }
      })
      return response
    } catch (error) {
      console.error('根据ID查询组织信息失败:', error)
      throw error
    }
  },

  // 新增组织
  addOrganization: async (data) => {
    try {
      const response = await request.post('/web/base/organization/addOrganization', data)
      return response
    } catch (error) {
      console.error('新增组织失败:', error)
      throw error
    }
  },

  // 更新组织信息
  updateOrganization: async (data) => {
    try {
      const response = await request.post('/web/base/organization/updateOrganization', data)
      return response
    } catch (error) {
      console.error('更新组织信息失败:', error)
      throw error
    }
  },

  // 删除组织
  deleteOrganization: async (orgId) => {
    try {
      const response = await request.post('/web/base/organization/deleteOrganization', null, {
        params: { orgId }
      })
      return response
    } catch (error) {
      console.error('删除组织失败:', error)
      throw error
    }
  },

  // 批量删除组织
  batchDeleteOrganization: async (orgIds) => {
    try {
      const response = await request.post('/web/base/organization/batchDeleteOrganization', orgIds)
      return response
    } catch (error) {
      console.error('批量删除组织失败:', error)
      throw error
    }
  },

  // 启用/禁用组织
  updateOrganizationStatus: async (orgId, status) => {
    try {
      const response = await request.post('/web/base/organization/updateOrganizationStatus', null, {
        params: { orgId, status }
      })
      return response
    } catch (error) {
      console.error('启用/禁用组织失败:', error)
      throw error
    }
  },

  // 检查组织名称是否存在
  checkOrgNameExists: async (orgName, orgId) => {
    try {
      const response = await request.get('/web/base/organization/checkOrgNameExists', {
        params: { orgName, orgId }
      })
      return response
    } catch (error) {
      console.error('检查组织名称是否存在失败:', error)
      throw error
    }
  },

  // 提交公司登记申请（通过userms服务，会自动创建组织到hou_base_organization表并绑定用户）
  submitCompanyRegistration: async (companyData) => {
    try {
      // 使用userms服务的接口，会自动创建组织并绑定用户
      const response = await request.post('/web/user/company/register', companyData)
      return response
    } catch (error) {
      console.error('提交公司登记申请失败:', error)
      throw error
    }
  },

  // 检查用户是否已经提交过公司登记申请
  checkUserCompanyRegistration: async (userId) => {
    try {
      const response = await request.get('/web/base/organization/checkUserRegistration', {
        params: { userId }
      })
      return response
    } catch (error) {
      console.error('检查用户是否已经提交过公司登记申请失败:', error)
      throw error
    }
  },

  // 创建用户组织关联关系
  createUserOrganization: async (userOrgData) => {
    try {
      // 通过userms服务创建用户组织关联关系
      const response = await request.post('/web/user/organization/createUserOrg', userOrgData)
      return response
    } catch (error) {
      console.error('创建用户组织关联关系失败:', error)
      throw error
    }
  }
}

/**
 * 部门管理API接口
 */
export const departmentService = {
  // 分页查询部门信息
  getDepartmentPage: async (params) => {
    try {
      const response = await request.get('/web/oa/hr/department/page', {
        params: {
          page: params.current || 1,
          size: params.size || 10,
          departmentName: params.departmentName,
          departmentCode: params.departmentCode,
          parentId: params.parentId
        }
      })
      return normalizePageResponse(response)
    } catch (error) {
      console.error('分页查询部门信息失败:', error)
      throw error
    }
  },

  // 查询部门树形结构
  getDepartmentTree: async () => {
    try {
      const response = await request.get('/web/oa/hr/department/tree')
      return response
    } catch (error) {
      console.error('查询部门树形结构失败:', error)
      throw error
    }
  },

  // 根据ID查询部门信息
  getDepartmentById: async (deptId) => {
    try {
      const response = await request.get(`/web/oa/hr/department/${deptId}`)
      return response
    } catch (error) {
      console.error('根据ID查询部门信息失败:', error)
      throw error
    }
  },

  // 新增部门
  addDepartment: async (data) => {
    try {
      const response = await request.post('/web/oa/hr/department', data)
      return response
    } catch (error) {
      console.error('新增部门失败:', error)
      throw error
    }
  },

  // 更新部门信息
  updateDepartment: async (deptId, data) => {
    try {
      const response = await request.post(`/web/oa/hr/department/${deptId}`, data)
      return response
    } catch (error) {
      console.error('更新部门信息失败:', error)
      throw error
    }
  },

  // 删除部门
  deleteDepartment: async (deptId) => {
    try {
      const response = await request.delete(`/web/oa/hr/department/${deptId}`)
      return response
    } catch (error) {
      console.error('删除部门失败:', error)
      throw error
    }
  },

  // 从企业微信同步部门
  syncFromWecom: async () => {
    try {
      const response = await request.post('/web/oa/hr/department/sync-wecom')
      return response
    } catch (error) {
      console.error('企微同步部门失败:', error)
      throw error
    }
  },

  // 获取当前登录用户的部门列表
  getDeptListByCurrentUser: async () => {
    try {
      const response = await request.get('/web/user/dept/list')
      return response
    } catch (error) {
      console.error('获取当前登录用户的部门列表失败:', error)
      throw error
    }
  },

  // 根据组织ID获取部门列表
  getDeptListByOrgId: async (orgId) => {
    try {
      const response = await request.get(`/web/user/dept/list/${orgId}`)
      return response
    } catch (error) {
      console.error('根据组织ID获取部门列表失败:', error)
      throw error
    }
  },

  // 获取当前登录用户的部门树结构
  getDeptTreeByCurrentUser: async () => {
    try {
      const response = await request.get('/web/user/dept/tree')
      return response
    } catch (error) {
      console.error('获取当前登录用户的部门树结构失败:', error)
      throw error
    }
  },

  // 根据组织ID获取部门树结构
  getDeptTreeByOrgId: async (orgId) => {
    try {
      const response = await request.get(`/web/user/dept/tree/${orgId}`)
      return response
    } catch (error) {
      console.error('根据组织ID获取部门树结构失败:', error)
      throw error
    }
  },

  // 根据部门ID获取部门详细信息
  getDeptDetailById: async (deptId) => {
    try {
      const response = await request.get(`/web/user/dept/detail/${deptId}`)
      return response
    } catch (error) {
      console.error('根据部门ID获取部门详细信息失败:', error)
      throw error
    }
  }
}

/**
 * 岗位管理API接口
 */
export const positionService = {
  // 分页查询岗位信息
  getPositionPage: async (params) => {
    try {
      const response = await request.get('/web/oa/hr/position/page', {
        params: {
          page: params.current || 1,
          size: params.size || 10,
          name: params.name
        }
      })
      return normalizePageResponse(response)
    } catch (error) {
      console.error('分页查询岗位信息失败:', error)
      throw error
    }
  },

  // 根据ID查询岗位信息
  getPositionById: async (positionId) => {
    try {
      const response = await request.get(`/web/oa/hr/position/${positionId}`)
      return response
    } catch (error) {
      console.error('根据ID查询岗位信息失败:', error)
      throw error
    }
  },

  // 新增岗位
  addPosition: async (data) => {
    try {
      const response = await request.post('/web/oa/hr/position', data)
      return response
    } catch (error) {
      console.error('新增岗位失败:', error)
      throw error
    }
  },

  // 更新岗位信息
  updatePosition: async (positionId, data) => {
    try {
      const response = await request.post(`/web/oa/hr/position/${positionId}`, data)
      return response
    } catch (error) {
      console.error('更新岗位信息失败:', error)
      throw error
    }
  },

  // 删除岗位
  deletePosition: async (positionId) => {
    try {
      const response = await request.delete(`/web/oa/hr/position/${positionId}`)
      return response
    } catch (error) {
      console.error('删除岗位失败:', error)
      throw error
    }
  },

  // 从企业微信同步岗位（员工职务）
  syncFromWecom: async () => {
    try {
      const response = await request.post('/web/oa/hr/position/sync-wecom')
      return response
    } catch (error) {
      console.error('企微同步岗位失败:', error)
      throw error
    }
  }
}

// 为了保持向后兼容，单独导出函数
export const {
  getOrganizationPage,
  getOrganizationTree,
  getOrganizationById,
  addOrganization,
  updateOrganization,
  deleteOrganization,
  batchDeleteOrganization,
  updateOrganizationStatus,
  checkOrgNameExists,
  submitCompanyRegistration,
  checkUserCompanyRegistration,
  createUserOrganization
} = organizationService

export const {
  getDepartmentPage,
  getDepartmentTree,
  getDepartmentById,
  addDepartment,
  updateDepartment,
  deleteDepartment,
  getDeptListByCurrentUser,
  getDeptListByOrgId,
  getDeptTreeByCurrentUser,
  getDeptTreeByOrgId,
  getDeptDetailById
} = departmentService

export const {
  getPositionPage,
  getPositionById,
  addPosition,
  updatePosition,
  deletePosition
} = positionService

// 为了保持向后兼容，导出统一的API实例
export const organizationApi = organizationService
export const departmentApi = departmentService
export const positionApi = positionService

// 导出服务实例
export default {
  organizationService,
  departmentService,
  positionService,
  organizationApi,
  departmentApi,
  positionApi
}
