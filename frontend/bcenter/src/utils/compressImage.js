/**
 * 使用 canvas 压缩图片，默认输出 JPEG
 * - GIF：不压缩
 * - PNG < 2MB：不压缩（Canvas 对 PNG 几乎无效，常更大）
 * - PNG ≥ 2MB：压缩并转为 JPEG
 * @param {File} file 原始图片文件
 * @param {number} quality 压缩质量，范围 0~1，默认 0.7
 * @returns {Promise<File>} 压缩后的文件；压缩后更大则返回原文件
 */
export function compressImage(file, quality = 0.7) {
  if (!file || !(file instanceof File)) {
    return Promise.reject(new Error('文件无效'))
  }

  const beforeType = file.type || 'unknown'
  const beforeSize = file.size

  const inputType = normalizeMimeType(file)
  // GIF 不压缩；小 PNG Canvas 压不动，直接保留原图；非图片原样返回
  if (
    !inputType ||
    inputType === 'image/gif' ||
    (inputType === 'image/png' && file.size / 1024 / 1024 < 2)
  ) {
    logCompressResult(file.name, beforeType, beforeSize, file.type, file.size, '跳过压缩')
    return Promise.resolve(file)
  }

  const outputType = 'image/jpeg'
  const q = Math.min(1, Math.max(0.1, Number(quality) || 0.7))

  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        canvas.width = img.width
        canvas.height = img.height
        const ctx = canvas.getContext('2d')
        if (!ctx) {
          logCompressResult(file.name, beforeType, beforeSize, file.type, file.size, 'canvas不可用')
          resolve(file)
          return
        }
        // 转 JPEG 时补白底，避免透明区域变黑
        ctx.fillStyle = '#ffffff'
        ctx.fillRect(0, 0, canvas.width, canvas.height)
        ctx.drawImage(img, 0, 0)

        canvas.toBlob(
          (blob) => {
            if (!blob) {
              logCompressResult(file.name, beforeType, beforeSize, file.type, file.size, 'toBlob失败')
              resolve(file)
              return
            }
            // 压缩后体积未变小则保留原图
            if (blob.size >= file.size) {
              logCompressResult(
                file.name,
                beforeType,
                beforeSize,
                file.type,
                file.size,
                '压缩后更大，保留原图'
              )
              resolve(file)
              return
            }
            const name = file.name.replace(/\.\w+$/i, '.jpg')
            const compressed = new File([blob], name, {
              type: outputType,
              lastModified: Date.now(),
            })
            logCompressResult(
              file.name,
              beforeType,
              beforeSize,
              compressed.type,
              compressed.size,
              '压缩成功'
            )
            resolve(compressed)
          },
          outputType,
          q
        )
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = e.target.result
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

function formatSize(size) {
  return `${(size / 1024).toFixed(2)} KB`
}

function logCompressResult(name, beforeType, beforeSize, afterType, afterSize, tip) {
  console.log(
    `[图片压缩] ${name} | 前: ${beforeType}, ${formatSize(beforeSize)} → 后: ${afterType || 'unknown'}, ${formatSize(afterSize)} | ${tip}`
  )
}

function normalizeMimeType(file) {
  const type = (file.type || '').toLowerCase()
  if (type === 'image/jpg') return 'image/jpeg'
  if (
    type === 'image/jpeg' ||
    type === 'image/png' ||
    type === 'image/gif' ||
    type === 'image/bmp'
  ) {
    return type
  }
  const ext = (file.name || '').split('.').pop()?.toLowerCase()
  if (ext === 'jpg' || ext === 'jpeg') return 'image/jpeg'
  if (ext === 'png') return 'image/png'
  if (ext === 'gif') return 'image/gif'
  if (ext === 'bmp') return 'image/bmp'
  return ''
}
