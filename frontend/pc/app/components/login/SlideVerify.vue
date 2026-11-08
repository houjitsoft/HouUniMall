<template>
  <div class="slide-captcha-card" @click.stop>
    <div class="captcha-header">
      <h3 class="captcha-title">{{ $t('安全验证') }}</h3>
      <div class="captcha-actions">
        <button type="button" class="refresh-btn" :class="{ 'refresh-btn--loading': isRefreshing }"
          :disabled="isRefreshing || isVerified || isVerifying" @click="refreshCaptcha">
          <svg class="refresh-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 11a8 8 0 0 0-14.9-3.9L3 9m0 0V4m0 5h5M4 13a8 8 0 0 0 14.9 3.9L21 15m0 0v5m0-5h-5" /></svg>
          <span>{{ $t('刷新') }}</span>
        </button>
        <button type="button" class="close-btn" :aria-label="$t('关闭')" @click="emit('close')">
          <svg class="close-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18" /></svg>
        </button>
      </div>
    </div>

    <div class="captcha-body">
      <div class="captcha-image-container" :class="imageStatusClass"
        :style="{ width: `${config.width}px`, height: `${config.height}px` }">
        <Loading v-if="imageLoading" compact :text="$t('加载中...')" class="captcha-loading" />

        <template v-else-if="bgImageUrl">
          <div class="captcha-bg" :class="{
            'captcha-bg--server': effectiveServerMode,
            'captcha-bg--local': !effectiveServerMode
          }" :style="{
            width: `${config.width}px`,
            height: `${config.height}px`,
            backgroundImage: `url(${bgImageUrl})`
          }">
            <div class="captcha-gap-mask" :style="{
              left: `${targetX}px`,
              top: `${targetY}px`,
              width: `${config.blockSize}px`,
              height: `${config.blockSize}px`
            }" />
          </div>

          <div v-if="!isVerified" class="captcha-block" :class="{ 'captcha-block--shake': isFail }"
            :style="blockContainerStyle">
            <img v-if="blockImageUrl" :src="blockImageUrl" alt="" class="captcha-block__img" />
          </div>
        </template>

        <div v-else class="captcha-overlay captcha-overlay--error">
          <p>{{ $t('图片加载失败') }}</p>
          <button type="button" class="retry-btn" @click="refreshCaptcha">{{ $t('点击重试') }}</button>
        </div>

        <div v-if="isVerified" class="captcha-overlay captcha-overlay--success">
          <svg class="success-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9" /><path d="m8 12 2.5 2.5L16 9" /></svg>
          <p>{{ $t('验证成功') }}</p>
        </div>
      </div>

      <div class="slide-track" ref="trackRef" :class="trackStatusClass" :style="{ width: `${config.width}px` }">
        <div class="slide-progress" :style="{ width: `${sliderX + SLIDER_SIZE}px` }" />
        <div class="slide-btn"
          :class="{ 'slide-btn--active': isDragging, 'slide-btn--verified': isVerified, 'slide-btn--disabled': isVerifying }"
          :style="{ left: `${sliderX}px`, transition: isDragging ? 'none' : 'left 0.2s ease' }"
          @mousedown.prevent="onMouseDown" @touchstart.prevent="onTouchStart"
          :aria-disabled="isVerifying || imageLoading">
          <svg class="slide-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path v-if="isVerified" d="m5 12 4 4L19 6" />
            <path v-else d="m8 5 7 7-7 7M14 5l7 7-7 7" />
          </svg>
        </div>
        <span class="slide-tip" :class="tipStatusClass">{{ tipText }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { generateCaptcha, verifyGenerateCaptcha } from '~/services/authServer.service'

const { t } = useTranslate()

type VerifyMode = 'local' | 'server'

const SLIDER_SIZE = 48

interface CaptchaConfig {
  width: number
  height: number
  blockSize: number
  tolerance: number
}

const props = withDefaults(
  defineProps<{
    config?: Partial<CaptchaConfig>
    /** local: 本地图片校验；server: 接口验证2 */
    verifyMode?: VerifyMode
  }>(),
  {
    config: () => ({
      width: 320,
      height: 200,
      blockSize: 60,
      tolerance: 5
    }),
    verifyMode: 'server'
  }
)

const emit = defineEmits<{
  onSuccess: [ imgToken: string ]
  onFail: []
  close: []
}>()

const config = computed(() => ({
  width: 320,
  height: 200,
  blockSize: 60,
  tolerance: 5,
  ...props.config
}))

const trackRef = ref<HTMLDivElement | null>(null)
const bgImageUrl = ref('')
const blockImageUrl = ref('')
const imageLoading = ref(true)
const isRefreshing = ref(false)
const isVerifying = ref(false)
const imgToken = ref('')
// 服务器验证码接口在不同环境可能未部署或返回旧格式，使用本地兜底后改走本地校验。
const localFallback = ref(false)

const sliderX = ref(0)
const blockX = ref(0)
const blockY = ref(0)
const targetX = ref(0)
const targetY = ref(0)

const isDragging = ref(false)
const isVerified = ref(false)
const isFail = ref(false)
const tipText = ref('')

const isServerMode = computed(() => props.verifyMode === 'server')
const effectiveServerMode = computed(() => isServerMode.value && !localFallback.value)

const blockContainerStyle = computed(() => {
  const baseStyle: Record<string, string> = {
    left: `${blockX.value}px`,
    top: `${targetY.value}px`,
    width: `${config.value.blockSize}px`,
    height: `${config.value.blockSize}px`,
    transition: isDragging.value ? 'none' : 'left 0.2s ease'
  }

  // 裁切图不可用时，回退 background-position 方案
  if (!effectiveServerMode.value && !blockImageUrl.value) {
    baseStyle.backgroundImage = `url(${bgImageUrl.value})`
    baseStyle.backgroundPosition = `-${targetX.value}px -${targetY.value}px`
    baseStyle.backgroundSize = `${config.value.width}px ${config.value.height}px`
  }

  return baseStyle
})

const formatBase64Image = (bgImage: string) => {
  if (!bgImage) return ''
  if (bgImage.startsWith('data:')) return bgImage
  if (bgImage.startsWith('http://') || bgImage.startsWith('https://') || bgImage.startsWith('/')) return bgImage
  return `data:image/png;base64,${bgImage}`
}

const imageStatusClass = computed(() => ({
  'captcha-image-container--verified': isVerified.value,
  'captcha-image-container--fail': isFail.value
}))

const trackStatusClass = computed(() => ({
  'slide-track--verified': isVerified.value,
  'slide-track--fail': isFail.value
}))

const tipStatusClass = computed(() => ({
  'slide-tip--success': isVerified.value,
  'slide-tip--fail': isFail.value
}))

const setTip = (key: 'default' | 'success' | 'fail' | 'verifying') => {
  const tips = {
    default: t('请拖动滑块完成验证'),
    success: t('验证成功'),
    fail: t('验证失败，请重试'),
    verifying: t('验证中...')
  }
  tipText.value = tips[key]
}

const generateRandomPosition = () => {
  const { width, height, blockSize } = config.value
  const minX = blockSize * 1.5
  const maxX = width - blockSize * 1.5
  const minY = 20
  const maxY = height - blockSize - 20

  targetX.value = Math.floor(Math.random() * (maxX - minX + 1)) + minX
  targetY.value = Math.floor(Math.random() * (maxY - minY + 1)) + minY
  blockY.value = targetY.value
}

const isDataUrl = (url: string) => url.startsWith('data:')

const preloadImage = (url: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve()
    img.onerror = () => reject(new Error('image load failed'))
    img.src = url
  })
}

/** 跨域图片先转 base64，避免 canvas 污染 */
const resolveCropSourceUrl = async (imageUrl: string): Promise<string> => {
  if (isDataUrl(imageUrl)) return imageUrl

  try {
    const response = await fetch(imageUrl, { mode: 'cors', credentials: 'omit' })
    if (!response.ok) throw new Error('fetch image failed')
    const blob = await response.blob()
    return await new Promise<string>((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result as string)
      reader.onerror = () => reject(new Error('read image failed'))
      reader.readAsDataURL(blob)
    })
  } catch {
    return imageUrl
  }
}

const loadImageElement = (imageUrl: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    if (!isDataUrl(imageUrl)) {
      img.crossOrigin = 'anonymous'
    }
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error('image load failed'))
    img.src = imageUrl
  })
}

/** 根据 targetX/targetY 从背景图裁切拼图块 */
const cropBlockImage = async (imageUrl: string, gapX: number, gapY: number): Promise<string> => {
  const { blockSize, width, height } = config.value
  const sourceUrl = await resolveCropSourceUrl(imageUrl)
  const img = await loadImageElement(sourceUrl)

  const canvas = document.createElement('canvas')
  canvas.width = blockSize
  canvas.height = blockSize
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    throw new Error('canvas context failed')
  }

  const scaleX = img.naturalWidth / width
  const scaleY = img.naturalHeight / height
  const sourceX = Math.round(gapX * scaleX)
  const sourceY = Math.round(gapY * scaleY)
  const sourceW = Math.round(blockSize * scaleX)
  const sourceH = Math.round(blockSize * scaleY)

  ctx.drawImage(img, sourceX, sourceY, sourceW, sourceH, 0, 0, blockSize, blockSize)

  try {
    return canvas.toDataURL('image/png')
  } catch {
    throw new Error('canvas tainted')
  }
}

const resetSlider = () => {
  sliderX.value = 0
  blockX.value = 0
  isDragging.value = false
  isFail.value = false
}

const resetCaptcha = async () => {
  isVerified.value = false
  resetSlider()
  setTip('default')
  if (!effectiveServerMode.value && bgImageUrl.value) {
    await updateLocalBlockPosition()
  }
}

const refreshCaptchaFromServer = async () => {
  const res = await generateCaptcha()
  if (res.code !== 1 || !res.data) {
    bgImageUrl.value = ''
    blockImageUrl.value = ''
    imgToken.value = ''
    return
  }

  const { bgImage, imgToken: token, targetX: gapX, targetY: gapY } = res.data
  if (!bgImage || !token || gapX == null || gapY == null) {
    bgImageUrl.value = ''
    blockImageUrl.value = ''
    imgToken.value = ''
    return
  }
  const imageUrl = formatBase64Image(bgImage)
  const nextTargetX = Number(gapX)
  const nextTargetY = Number(gapY)
  if (!Number.isFinite(nextTargetX) || !Number.isFinite(nextTargetY)) {
    bgImageUrl.value = ''
    blockImageUrl.value = ''
    imgToken.value = ''
    return
  }

  await preloadImage(imageUrl)

  // 先提交完整的服务端状态。即使浏览器禁止 canvas 裁切（CORS、隐私策略等），
  // 也保留真实背景图，下面会切到 CSS background-position 兜底，而不是整张图替换成演示 SVG。
  imgToken.value = token
  targetX.value = nextTargetX
  targetY.value = nextTargetY
  blockY.value = nextTargetY
  bgImageUrl.value = imageUrl
  try {
    blockImageUrl.value = await cropBlockImage(imageUrl, nextTargetX, nextTargetY)
  } catch (error) {
    console.warn('滑动验证码拼图块裁切不可用，使用背景定位兜底:', error)
    blockImageUrl.value = ''
    // 保留服务端 token/答案，但本次在前端按 targetX 做本地校验。
    // effectiveServerMode=false 时 blockContainerStyle 会显示背景图裁切块。
    localFallback.value = true
  }
}

const updateLocalBlockPosition = async () => {
  generateRandomPosition()
  if (!bgImageUrl.value) {
    blockImageUrl.value = ''
    return
  }

  try {
    blockImageUrl.value = await cropBlockImage(bgImageUrl.value, targetX.value, targetY.value)
  } catch {
    blockImageUrl.value = ''
  }
}

const refreshCaptchaFromLocal = async () => {
  // 本地兜底使用内置 SVG，不再请求部署环境中可能不存在的 verify*.png/jpeg。
  // 这样即使 BFF/auth 尚未完成发布，验证码弹窗也始终有可渲染的背景图。
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${config.value.width}" height="${config.value.height}" viewBox="0 0 ${config.value.width} ${config.value.height}"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#eef3ff"/><stop offset="1" stop-color="#ffdfe2"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/><circle cx="70" cy="62" r="42" fill="#ff8a8a" opacity=".8"/><circle cx="255" cy="145" r="66" fill="#7ca7ff" opacity=".75"/><path d="M0 165 Q80 105 160 170 T320 135 V200 H0Z" fill="#7bd8c4" opacity=".8"/><text x="160" y="105" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#334155">UniMall 安全验证</text></svg>`
  bgImageUrl.value = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
  await updateLocalBlockPosition()
}

const refreshCaptcha = async () => {
  if (isRefreshing.value || isVerified.value || isVerifying.value) return

  isRefreshing.value = true
  // 每次刷新都重新尝试服务端验证码；只有本次请求失败时才进入本地兜底。
  localFallback.value = false
  imageLoading.value = true
  resetSlider()
  isVerified.value = false
  isFail.value = false
  setTip('default')
  imgToken.value = ''
  targetY.value = 0
  blockY.value = 0
  bgImageUrl.value = ''
  blockImageUrl.value = ''

  try {
    if (effectiveServerMode.value) {
      // 新接口未发布、网关返回 HTTP 404 或网络异常时，都切换到内置兜底。
      // 不能只判断业务响应，否则 ofetch 在 HTTP 404 时会直接抛异常，弹窗会一直空白。
      try {
        await refreshCaptchaFromServer()
      } catch (error) {
        console.warn('滑动验证码接口暂不可用，使用本地兜底:', error)
      }
      if (!bgImageUrl.value) {
        localFallback.value = true
        await refreshCaptchaFromLocal()
      }
    } else {
      await refreshCaptchaFromLocal()
    }
  } catch {
    bgImageUrl.value = ''
    blockImageUrl.value = ''
    imgToken.value = ''
  } finally {
    imageLoading.value = false
    isRefreshing.value = false
  }
}

const getMaxSliderX = () => {
  const trackWidth = trackRef.value?.getBoundingClientRect().width ?? config.value.width
  return Math.max(0, trackWidth - SLIDER_SIZE)
}

const updateSliderPosition = (clientX: number) => {
  if (!trackRef.value || isVerified.value) return

  const trackRect = trackRef.value.getBoundingClientRect()
  const x = clientX - trackRect.left - SLIDER_SIZE / 2
  const maxX = getMaxSliderX()
  sliderX.value = Math.max(0, Math.min(x, maxX))
  blockX.value = sliderX.value
}

const onMouseDown = () => {
  if (isVerified.value || imageLoading.value || isVerifying.value) return
  isDragging.value = true
  isFail.value = false
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

const onTouchStart = () => {
  if (isVerified.value || imageLoading.value || isVerifying.value) return
  isDragging.value = true
  isFail.value = false
  document.addEventListener('touchmove', onTouchMove, { passive: false })
  document.addEventListener('touchend', onTouchEnd)
}

const onMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return
  updateSliderPosition(e.clientX)
}

const onTouchMove = (e: TouchEvent) => {
  if (!isDragging.value) return
  e.preventDefault()
  const touch = e.touches[0]
  if (touch) updateSliderPosition(touch.clientX)
}

const endDrag = () => {
  if (!isDragging.value) return
  isDragging.value = false
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
  verifyCaptcha()
}

const onMouseUp = () => endDrag()
const onTouchEnd = () => endDrag()

const handleVerifySuccess = () => {
  isVerified.value = true
  isFail.value = false
  sliderX.value = blockX.value
  setTip('success')
  emit('onSuccess', imgToken.value)
}

const handleVerifyFail = async () => {
  isFail.value = true
  setTip('fail')
  emit('onFail')

  setTimeout(async () => {
    resetSlider()
    setTip('default')
    isFail.value = false
    if (effectiveServerMode.value) {
      await refreshCaptcha()
    } else {
      await updateLocalBlockPosition()
    }
  }, 800)
}

const verifyCaptchaLocal = () => {
  const diff = Math.abs(blockX.value - targetX.value)

  if (diff <= config.value.tolerance) {
    sliderX.value = targetX.value
    blockX.value = targetX.value
    handleVerifySuccess()
    return
  }

  handleVerifyFail()
}

const verifyCaptchaServer = async () => {
  if (!imgToken.value || isVerifying.value) return

  isVerifying.value = true
  setTip('verifying')

  try {
    const res = await verifyGenerateCaptcha(
      imgToken.value,
      Math.round(blockX.value),
      Math.round(targetY.value)
    )
    if (res.code === 1 && res.data === true) {
      handleVerifySuccess()
      return
    }
    await handleVerifyFail()
  } catch {
    await handleVerifyFail()
  } finally {
    isVerifying.value = false
  }
}

const verifyCaptcha = () => {
  if (effectiveServerMode.value && imgToken.value) {
    verifyCaptchaServer()
    return
  }
  verifyCaptchaLocal()
}

const cleanupListeners = () => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
}

onMounted(() => {
  setTip('default')
  refreshCaptcha()
})

onUnmounted(() => {
  cleanupListeners()
})
</script>

<style lang="scss" scoped>
.slide-captcha-card {
  width: calc(100% - 40px);
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  overflow: hidden;
  user-select: none;
}

.captcha-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.captcha-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.captcha-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s, background 0.2s;

  &:hover:not(:disabled) {
    color: var(--color);
    background: color-mix(in srgb, var(--color) 8%, white);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &--loading .refresh-icon {
    animation: captcha-spin 0.75s linear infinite;
  }
}

.refresh-icon {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #999;
  cursor: pointer;
  transition: color 0.2s, background 0.2s;

  &:hover {
    color: #333;
    background: #f5f5f5;
  }
}

.close-icon {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.captcha-body {
  padding: 20px;
}

.captcha-image-container {
  position: relative;
  margin: 0 auto;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  &--verified {
    box-shadow: 0 0 0 2px color-mix(in srgb, var(--color) 40%, transparent);
  }

  &--fail {
    animation: captcha-shake 0.4s ease;
  }
}

.captcha-bg {
  background-size: cover;
  background-position: center;
  position: relative;

  &--server,
  &--local {
    background-size: 100% 100%;
    background-position: left top;
  }
}

.captcha-block__img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
  border-radius: 0 15px 15px 15px;
}

.captcha-gap-mask {
  position: absolute;
  z-index: 5;
  background-color: rgba(0, 0, 0, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 0 15px 15px 15px;
  box-sizing: border-box;
}

.captcha-block {
  position: absolute;
  z-index: 10;
  border-radius: 0 15px 15px 15px;
  overflow: hidden;
  background-repeat: no-repeat;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.5);

  &--shake {
    animation: captcha-shake 0.4s ease;
  }
}

.captcha-overlay {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;

  p {
    margin: 0;
    font-size: 14px;
  }

  &--error {
    background: #f5f7fa;
    color: #666;
  }

  &--success {
    background: rgba(0, 0, 0, 0.45);
    color: #fff;
  }
}

.captcha-loading {
  position: absolute;
  inset: 0;
  z-index: 20;

  :deep(.load) {
    min-height: 100%;
    height: 100%;
    padding: 24px 16px;
    background: #f5f7fa;
    border-radius: 0;
    animation: none;
  }

  :deep(.load__text) {
    margin-top: 12px;
    animation: none;
  }
}

.retry-btn {
  padding: 6px 16px;
  border: 1px solid var(--color);
  border-radius: 4px;
  background: #fff;
  color: var(--color);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--color);
    color: #fff;
  }
}

.success-icon {
  width: 40px;
  height: 40px;
  color: #52c41a;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.slide-track {
  position: relative;
  height: 48px;
  margin: 16px auto 0;
  background: #f5f7fa;
  border: 1px solid #e8eaed;
  border-radius: 24px;
  overflow: hidden;
  transition: border-color 0.3s, background 0.3s;

  &--verified {
    border-color: color-mix(in srgb, var(--color) 35%, #e8eaed);
    background: color-mix(in srgb, var(--color) 8%, white);
  }

  &--fail {
    border-color: #ffccc7;
    background: #fff2f0;
  }
}

.slide-progress {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: color-mix(in srgb, var(--color) 18%, white);
  border-radius: 24px 0 0 24px;
  pointer-events: none;
  transition: width 0.05s linear;
}

.slide-btn {
  position: absolute;
  top: 0;
  left: 0;
  width: 48px;
  height: 48px;
  background: var(--color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: grab;
  box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 40%, transparent);
  z-index: 2;
  transition: background 0.2s, box-shadow 0.2s;

  &--active {
    cursor: grabbing;
    box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 50%, transparent);
  }

  &--verified {
    background: #52c41a;
    cursor: default;
    box-shadow: 0 2px 8px rgba(82, 196, 26, 0.35);
  }

  &--disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.slide-icon {
  width: 22px;
  height: 22px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.slide-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #999;
  font-size: 13px;
  pointer-events: none;
  white-space: nowrap;
  transition: color 0.3s;

  &--success {
    color: var(--color);
    font-weight: 500;
  }

  &--fail {
    color: #ff4d4f;
  }
}

@keyframes captcha-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes captcha-shake {

  0%,
  100% {
    transform: translateX(0);
  }

  20%,
  60% {
    transform: translateX(-6px);
  }

  40%,
  80% {
    transform: translateX(6px);
  }
}
</style>
