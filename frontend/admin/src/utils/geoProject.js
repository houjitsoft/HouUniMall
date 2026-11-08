const STORAGE_KEY = 'geo_current_project_id'
const STORAGE_NAME_KEY = 'geo_current_project_name'

export function getCurrentProjectId() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw == null || raw === '') return null
  const id = Number(raw)
  return Number.isFinite(id) && id > 0 ? id : null
}

export function getCurrentProjectName() {
  return localStorage.getItem(STORAGE_NAME_KEY) || ''
}

export function setCurrentProject(project) {
  if (!project?.id) return
  const previousProjectId = getCurrentProjectId()
  localStorage.setItem(STORAGE_KEY, String(project.id))
  localStorage.setItem(STORAGE_NAME_KEY, project.name || project.domain || String(project.id))
  if (String(previousProjectId) !== String(project.id)) {
    window.dispatchEvent(new CustomEvent('geo-project-change', { detail: project }))
  }
}

export function clearCurrentProject() {
  localStorage.removeItem(STORAGE_KEY)
  localStorage.removeItem(STORAGE_NAME_KEY)
  window.dispatchEvent(new CustomEvent('geo-project-change', { detail: null }))
}

/** 首次进入时优先选中 houjit.com 项目 */
export function pickDefaultProject(projects = []) {
  if (!projects.length) return null
  const houjit = projects.find((p) =>
    String(p.domain || '').includes('houjit.com') ||
    String(p.name || '').includes('厚匠企业官网')
  )
  return houjit || projects[0]
}
