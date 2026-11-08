/**
 * 使用 canvas 压缩图片，默认输出 JPEG
 * - GIF：不压缩
 * - PNG < 2MB：不压缩，原样上传
 * - PNG ≥ 2MB：压缩并转为 JPEG
 * - JPEG / BMP 等：压缩为 JPEG
 * @param file 原始图片文件
 * @param quality 压缩质量，范围 0~1，默认 0.7
 * @returns 压缩后的文件；跳过或压缩后更大则返回原文件
 */
export function compressImage(file: File, quality = 0.7): Promise<File> {
  if (!file || !(file instanceof File)) {
    return Promise.reject(new Error('文件无效'))
  }

  const beforeType = file.type || 'unknown'
  const beforeSize = file.size

  const inputType = normalizeMimeType(file)
  // GIF 不压缩；小 PNG 不压缩；非图片原样返回
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
      img.src = e.target?.result as string
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

function formatSize(size: number) {
  return `${(size / 1024).toFixed(2)} KB`
}

function logCompressResult(
  name: string,
  beforeType: string,
  beforeSize: number,
  afterType: string | undefined,
  afterSize: number,
  tip: string
) {
  console.log(
    `[图片压缩] ${name} | 前: ${beforeType}, ${formatSize(beforeSize)} → 后: ${afterType || 'unknown'}, ${formatSize(afterSize)} | ${tip}`
  )
}

function normalizeMimeType(file: File): string {
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
