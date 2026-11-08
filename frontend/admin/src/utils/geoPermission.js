const PERMISSIONS_KEY = 'geo_permissions'

export function setPermissions(list) {
  if (Array.isArray(list)) {
    localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(list))
  }
}

export function getPermissions() {
  try {
    const raw = localStorage.getItem(PERMISSIONS_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

export function clearPermissions() {
  localStorage.removeItem(PERMISSIONS_KEY)
}

export function hasPermission(permission) {
  if (!permission) return true
  const perms = getPermissions()
  return perms.includes(permission)
}
