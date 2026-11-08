/**
 * OSS objects configured with forced-download response headers are blocked by
 * Chromium when embedded as images. Keep configurable images for normal URLs,
 * while falling back to bundled assets for the affected legacy OSS bucket.
 */
export function canDisplayConfiguredImage(url) {
  if (!url || typeof url !== 'string') return false

  try {
    const parsed = new URL(url, window.location.origin)
    return parsed.hostname !== ''
  } catch {
    return false
  }
}
