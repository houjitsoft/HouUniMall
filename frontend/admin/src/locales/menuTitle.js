function lookupMenuTitle(entry, messages) {
  if (entry?.path && messages?.menu?.paths?.[entry.path]) {
    return messages.menu.paths[entry.path]
  }
  if (entry?.perms && messages?.menu?.permissions?.[entry.perms]) {
    return messages.menu.permissions[entry.perms]
  }

  const index = entry?.index || entry?.menuIndex
  if (index && messages?.menu?.groups?.[index]) {
    return messages.menu.groups[index]
  }
  return ''
}

export function resolveMenuTitle(entry, messages, fallbackMessages) {
  return String(entry?.title || entry?.name || '').trim()
    || lookupMenuTitle(entry, messages)
    || lookupMenuTitle(entry, fallbackMessages)
}
