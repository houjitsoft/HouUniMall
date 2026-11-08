export function clampFloatingPosition(position, viewport, element, margin = 12) {
  const maxX = Math.max(margin, viewport.width - element.width - margin)
  const maxY = Math.max(margin, viewport.height - element.height - margin)

  return {
    x: Math.min(Math.max(position.x, margin), maxX),
    y: Math.min(Math.max(position.y, margin), maxY)
  }
}

export function clampPositionAfterResize(position, viewport, resizedElement, margin = 12) {
  return clampFloatingPosition(position, viewport, resizedElement, margin)
}

export function createDefaultFloatingPosition(viewport, element, margin = 12) {
  return clampFloatingPosition(
    {
      x: viewport.width - element.width - margin,
      y: viewport.height - element.height - margin
    },
    viewport,
    element,
    margin
  )
}

export function isPersistedUserPosition(position) {
  return position?.userMoved === true
    && Number.isFinite(position.x)
    && Number.isFinite(position.y)
}

export function getPanelPlacement(element, viewportWidth) {
  return element.x + element.width / 2 < viewportWidth / 2 ? 'right' : 'left'
}

function clampPanelAxis(value, viewportSize, panelSize, margin) {
  if (panelSize + margin * 2 > viewportSize) {
    return Math.max(0, (viewportSize - panelSize) / 2)
  }
  return Math.min(Math.max(value, margin), viewportSize - panelSize - margin)
}

export function calculatePanelPosition(
  button,
  panel,
  viewport,
  preferredPlacement,
  gap = 12,
  margin = 12
) {
  const candidates = {
    left: button.x - panel.width - gap,
    right: button.x + button.width + gap
  }
  const alternatePlacement = preferredPlacement === 'left' ? 'right' : 'left'
  const fits = (x) => x >= margin && x + panel.width <= viewport.width - margin
  const placement = fits(candidates[preferredPlacement])
    ? preferredPlacement
    : fits(candidates[alternatePlacement])
      ? alternatePlacement
      : preferredPlacement

  return {
    x: clampPanelAxis(candidates[placement], viewport.width, panel.width, margin),
    y: clampPanelAxis(
      button.y + button.height / 2 - panel.height / 2,
      viewport.height,
      panel.height,
      margin
    ),
    placement
  }
}
