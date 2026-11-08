function pickDefaultProject(projects) {
  return projects.find((project) =>
    String(project?.domain || '').includes('houjit.com') ||
    String(project?.name || '').includes('厚匠企业官网')
  ) || projects[0] || null
}

export function normalizeProjectId(value) {
  if (Array.isArray(value)) value = value[0]
  if (value == null || String(value).trim() === '') return null
  const id = Number(value)
  return Number.isSafeInteger(id) && id > 0 ? id : null
}

export function createCachedProjectOption(projectId, projectName) {
  const id = normalizeProjectId(projectId)
  if (id == null) return null
  const name = String(projectName || '').trim() || `项目 ${id}`
  return { id, name, domain: '', label: name }
}

export function withProjectQuery(query, projectId) {
  const next = { ...(query || {}) }
  const id = normalizeProjectId(projectId)
  if (id == null) {
    delete next.projectId
  } else {
    next.projectId = String(id)
  }
  return next
}

export function shouldMountProjectView({ isBlankLayout, projectReady, projectId } = {}) {
  return !isBlankLayout && projectReady === true && normalizeProjectId(projectId) != null
}

export function resolveProjectLoadSuccess(projects, currentProjectId) {
  const list = Array.isArray(projects) ? projects : []
  if (!list.length) {
    return {
      projects: [],
      currentProject: null,
      showOnboarding: true,
      clearStoredProject: true,
      error: ''
    }
  }

  const currentProject = list.find(
    (project) => String(project.id) === String(currentProjectId)
  ) || pickDefaultProject(list)

  return {
    projects: list,
    currentProject,
    showOnboarding: false,
    clearStoredProject: false,
    error: ''
  }
}

export function resolveProjectLoadFailure(previous, error) {
  return {
    projects: previous?.projects || [],
    currentProjectId: previous?.currentProjectId ?? null,
    showOnboarding: false,
    error: '项目加载失败，请重试',
    cause: error
  }
}

export function isLatestProjectRequest(requestId, latestRequestId) {
  return requestId === latestRequestId
}
