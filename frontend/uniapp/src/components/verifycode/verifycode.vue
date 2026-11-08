<template>

	<view class="slider-verify-container" :style="themeStyle">

		<view class="verify-header">

			<text class="verify-title">{{ getTransNew('安全验证') }}</text>

			<view class="refresh-btn"

				:class="{ 'refresh-btn--disabled': isRefreshing || isMoving || verifySuccess || isVerifying }"

				@click="handleRefresh">

				<text class="icon-refresh" :class="{ loading: isRefreshing }">↻</text>

				<text class="refresh-text">{{ getTransNew('刷新') }}</text>

			</view>

		</view>



		<view class="verify-box" :class="boxStatusClass" :style="boxStyle">

			<view v-if="imageLoading" class="verify-state">

				<view class="verify-spinner" />

				<text>{{ getTransNew('加载中...') }}</text>

			</view>



			<template v-else-if="bgImage">

				<!-- scaleToFill：坐标与 CAPTCHA_CONFIG 线性映射，aspectFill 会裁剪导致拼图错位 -->
				<image class="verify-bg" :src="bgImage" mode="scaleToFill" />



				<view class="gap-mask" :style="gapStyle" />



				<view v-if="!verifySuccess" class="puzzle-block" :class="{ 'puzzle-block--shake': isFail }"

					:style="puzzleStyle">

					<view class="puzzle-img-wrapper" :style="puzzleImgWrapperStyle">

						<image class="puzzle-img" :src="bgImage" mode="scaleToFill" />

					</view>

				</view>

			</template>



			<view v-else class="verify-state verify-state--error">

				<text>{{ getTransNew('图片加载失败') }}</text>

				<view class="retry-btn" @click="handleRefresh">{{ getTransNew('点击重试') }}</view>

			</view>

		</view>



		<view class="slider-track" :class="trackStatusClass" :style="trackStyle" @touchstart="handleTouchStart"

			@touchmove.stop="handleTouchMove" @touchend="handleTouchEnd" @touchcancel="handleTouchEnd">

			<view class="slide-progress" :style="{ width: `${sliderLeft + SLIDER_BTN_SIZE}rpx` }" />

			<view v-if="!verifySuccess" class="slider-btn" :class="{ 'slider-btn--disabled': isVerifying }"

				:style="sliderBtnStyle">

				<up-icon size="20" color="#fff" name="arrow-right-double" />

			</view>

			<text v-if="!verifySuccess" class="slider-tips" :class="tipStatusClass">{{ tipText }}</text>

		</view>

	</view>

</template>



<script setup>

import { ref, computed, onMounted, getCurrentInstance } from 'vue'

import { publicVariable } from '@/utils/publicVariable.js'

import { generate, verifyGenerateCaptcha } from '@/api/general/login.js'



const BOX_WIDTH = 620

const BOX_HEIGHT = 360

const SLIDER_BTN_SIZE = 70



const CAPTCHA_CONFIG = {

	width: 320,

	height: 200,

	blockSize: 60,

	tolerance: 5

}



const PUZZLE_SIZE = Math.round(CAPTCHA_CONFIG.blockSize * BOX_WIDTH / CAPTCHA_CONFIG.width)



const props = defineProps({

	/** local: 本地图片校验；server: 接口验证 */

	verifyMode: {

		type: String,

		default: 'local',

		validator: (value) => ['local', 'server'].includes(value)

	}

})



const emit = defineEmits(['verifySuccess', 'verifyFail'])



const instance = getCurrentInstance()

const t = (key) => (instance?.proxy?.getTransNew ? instance.proxy.getTransNew(key) : key)



const isServerMode = computed(() => props.verifyMode === 'server')



const themeStyle = computed(() => ({

	'--webColor': publicVariable.webColor || '#000'

}))



const imagePool = computed(() => [

	`${publicVariable.imgUrl}verify1.png`,

	`${publicVariable.imgUrl}verify2.jpeg`

])



const toRpxX = (px) => px * BOX_WIDTH / CAPTCHA_CONFIG.width

const toRpxY = (px) => px * BOX_HEIGHT / CAPTCHA_CONFIG.height

const toPxX = (rpx) => rpx * CAPTCHA_CONFIG.width / BOX_WIDTH



const boxStyle = computed(() => ({

	width: `${BOX_WIDTH}rpx`,

	height: `${BOX_HEIGHT}rpx`

}))



const trackStyle = computed(() => ({

	width: `${BOX_WIDTH}rpx`

}))



const gapStyle = computed(() => ({

	left: `${toRpxX(gapLeftPx.value)}rpx`,

	top: `${toRpxY(gapTopPx.value)}rpx`,

	width: `${PUZZLE_SIZE}rpx`,

	height: `${PUZZLE_SIZE}rpx`

}))



const puzzleStyle = computed(() => ({

	left: `${puzzleLeft.value}rpx`,

	top: `${toRpxY(gapTopPx.value)}rpx`,

	width: `${PUZZLE_SIZE}rpx`,

	height: `${PUZZLE_SIZE}rpx`,

	transition: isMoving.value ? 'none' : 'left 0.2s ease'

}))



const puzzleImgWrapperStyle = computed(() => ({

	left: `${-toRpxX(gapLeftPx.value)}rpx`,

	top: `${-toRpxY(gapTopPx.value)}rpx`,

	width: `${BOX_WIDTH}rpx`,

	height: `${BOX_HEIGHT}rpx`

}))



const sliderBtnStyle = computed(() => ({

	left: `${sliderLeft.value}rpx`,

	transition: isMoving.value ? 'none' : 'left 0.2s ease'

}))



const gapLeftPx = ref(0)

const gapTopPx = ref(0)

const sliderLeft = ref(0)

const puzzleLeft = ref(0)

const startX = ref(0)

const imgToken = ref('')



const isMoving = ref(false)

const isVerifying = ref(false)

const verifySuccess = ref(false)

const isRefreshing = ref(false)

const isFail = ref(false)

const imageLoading = ref(true)

const bgImage = ref('')

const tipText = ref('')



const boxStatusClass = computed(() => ({

	'verify-box--fail': isFail.value

}))



const trackStatusClass = computed(() => ({

	'slider-track--fail': isFail.value

}))



const tipStatusClass = computed(() => ({

	'slider-tips--fail': isFail.value

}))



const setTip = (key) => {

	const tips = {

		default: t('请拖动滑块完成验证'),

		success: t('验证成功'),

		fail: t('验证失败，请重试'),

		verifying: t('验证中...')

	}

	tipText.value = tips[key] || tips.default

}



const formatBase64Image = (image) => {
	if (!image) return ''
	const raw = String(image).replace(/\s/g, '')
	if (raw.startsWith('data:')) return raw
	return `data:image/png;base64,${raw}`
}

const parseBase64Payload = (imageUrl) => {
	if (!imageUrl) return null
	if (imageUrl.startsWith('data:')) {
		const matched = imageUrl.match(/^data:image\/([\w+]+);base64,(.+)$/i)
		if (!matched) return null
		const mime = matched[1].toLowerCase()
		const ext = mime === 'jpeg' ? 'jpg' : mime.replace('+', '')
		return { ext, data: matched[2] }
	}
	return { ext: 'png', data: imageUrl.replace(/\s/g, '') }
}

/** 小程序对 dataURL 支持不稳定，写入本地临时文件再展示 */
const resolveDisplayImageSrc = (imageUrl) => {
	return new Promise((resolve, reject) => {
		// #ifdef MP-WEIXIN
		const payload = parseBase64Payload(imageUrl)
		if (!payload) {
			resolve(imageUrl)
			return
		}
		try {
			const fs = uni.getFileSystemManager()
			const filePath = `${wx.env.USER_DATA_PATH}/slide_captcha_${Date.now()}.${payload.ext}`
			fs.writeFile({
				filePath,
				data: payload.data,
				encoding: 'base64',
				success: () => resolve(filePath),
				fail: (err) => reject(err)
			})
		} catch (error) {
			reject(error)
		}
		// #endif
		// #ifndef MP-WEIXIN
		resolve(imageUrl)
		// #endif
	})
}

const initGapPositionPx = () => {
	const { width, height, blockSize } = CAPTCHA_CONFIG
	const minX = blockSize * 1.5
	const maxX = width - blockSize * 1.5
	const minY = 20
	const maxY = height - blockSize - 20

	gapLeftPx.value = Math.floor(Math.random() * (maxX - minX + 1)) + minX
	gapTopPx.value = Math.floor(Math.random() * (maxY - minY + 1)) + minY
}

const resetSlider = () => {
	sliderLeft.value = 0
	puzzleLeft.value = 0
	isMoving.value = false
	isFail.value = false
}

const preloadImage = (src) => {
	return new Promise((resolve, reject) => {
		if (!src) {
			reject(new Error('empty image src'))
			return
		}
		// dataURL 在小程序 getImageInfo 经常失败，跳过预加载
		if (src.startsWith('data:')) {
			resolve({ path: src, width: CAPTCHA_CONFIG.width, height: CAPTCHA_CONFIG.height })
			return
		}
		uni.getImageInfo({
			src,
			success: resolve,
			fail: reject
		})
	})
}

const refreshFromServer = async () => {
	const res = await generate()
	if (res.code !== 1 || !res.data) {
		bgImage.value = ''
		imgToken.value = ''
		return
	}

	const { bgImage: bg, imgToken: token, targetX, targetY } = res.data
	const imageUrl = formatBase64Image(bg)
	let displaySrc = imageUrl
	try {
		displaySrc = await resolveDisplayImageSrc(imageUrl)
		await preloadImage(displaySrc)
	} catch (error) {
		console.warn('验证码图片预处理失败，尝试直接展示', error)
		displaySrc = imageUrl
	}

	bgImage.value = displaySrc
	imgToken.value = token
	gapLeftPx.value = Number(targetX / targetY)
	gapTopPx.value = targetY
}



const refreshFromLocal = async () => {

	if (!imagePool.value.length) {

		bgImage.value = ''

		return

	}



	const randomIndex = Math.floor(Math.random() * imagePool.value.length)

	const imageUrl = imagePool.value[randomIndex]



	await preloadImage(imageUrl)

	bgImage.value = imageUrl

	imgToken.value = ''

	initGapPositionPx()

}



const refreshCaptcha = async () => {

	if (isRefreshing.value || verifySuccess.value || isVerifying.value) return



	isRefreshing.value = true

	imageLoading.value = true

	verifySuccess.value = false

	resetSlider()

	setTip('default')

	imgToken.value = ''

	gapTopPx.value = 0

	bgImage.value = ''



	try {

		if (isServerMode.value) {

			await refreshFromServer()

		} else {

			await refreshFromLocal()

		}

	} catch {

		bgImage.value = ''

		imgToken.value = ''

	} finally {

		imageLoading.value = false

		isRefreshing.value = false

	}

}



const handleRefresh = () => {

	if (isRefreshing.value || isMoving.value || verifySuccess.value || isVerifying.value) return

	refreshCaptcha()

}



const getPxToRpx = () => 750 / uni.getSystemInfoSync().windowWidth



const handleTouchStart = (e) => {

	if (verifySuccess.value || isRefreshing.value || imageLoading.value || isVerifying.value) return

	isMoving.value = true

	isFail.value = false

	const touch = e.touches[0]

	startX.value = touch?.clientX ?? touch?.x ?? 0

}



const handleTouchMove = (e) => {

	if (verifySuccess.value || isRefreshing.value || isVerifying.value || !isMoving.value) return

	e.preventDefault?.()



	const touch = e.touches[0]

	const moveX = touch?.clientX ?? touch?.x ?? 0

	let diffX = (moveX - startX.value) * getPxToRpx()



	const maxDiffX = BOX_WIDTH - PUZZLE_SIZE

	diffX = Math.max(0, Math.min(diffX, maxDiffX))



	sliderLeft.value = diffX

	puzzleLeft.value = diffX

}



const handleVerifySuccess = () => {

	verifySuccess.value = true

	isFail.value = false

	const gapRpx = toRpxX(gapLeftPx.value)

	sliderLeft.value = gapRpx

	puzzleLeft.value = gapRpx

	setTip('success')

	emit('verifySuccess', imgToken.value)

}



const handleVerifyFail = () => {

	isFail.value = true

	setTip('fail')

	emit('verifyFail')



	uni.showToast({

		title: t('验证失败，请重试'),

		icon: 'none',

		duration: 1500

	})



	setTimeout(async () => {

		resetSlider()

		setTip('default')

		isFail.value = false

		if (isServerMode.value) {

			await refreshCaptcha()

		} else {

			initGapPositionPx()

		}

	}, 800)

}



const verifyCaptchaLocal = () => {

	const diff = Math.abs(toPxX(puzzleLeft.value) - gapLeftPx.value)



	if (diff <= CAPTCHA_CONFIG.tolerance) {

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

		const res = await verifyGenerateCaptcha({

			imgToken: imgToken.value,

			targetX: Math.round(toPxX(puzzleLeft.value)),

			targetY: Math.round(gapTopPx.value)

		})



		if (res.code === 1 && res.data === true) {

			handleVerifySuccess()

			return

		}



		handleVerifyFail()

	} catch {

		handleVerifyFail()

	} finally {

		isVerifying.value = false

	}

}



const handleTouchEnd = () => {

	if (verifySuccess.value || isRefreshing.value || isVerifying.value) return

	isMoving.value = false



	if (isServerMode.value) {

		verifyCaptchaServer()

		return

	}



	verifyCaptchaLocal()

}



onMounted(() => {

	setTip('default')

	refreshCaptcha()

})

</script>



<style lang="scss" scoped>

.slider-verify-container {

	display: flex;

	flex-direction: column;

	background-color: #fff;

	border-radius: 16rpx;

	padding: 20rpx 24rpx 32rpx;

	width: 650rpx;

	box-shadow: none;

}



.verify-header {

	display: flex;

	justify-content: space-between;

	align-items: center;

	padding: 8rpx 0 16rpx;

}



.verify-title {

	font-size: 30rpx;

	color: #333;

	font-weight: 600;

}



.refresh-btn {

	display: flex;

	align-items: center;

	gap: 8rpx;

	font-size: 24rpx;

	color: #666;



	&--disabled {

		opacity: 0.5;

	}

}



.icon-refresh {

	font-size: 24rpx;

}



.loading {

	animation: verify-rotate 0.75s linear infinite;

}



.refresh-text {

	user-select: none;

}



.verify-box {

	position: relative;

	width: 100%;

	height: 360rpx;

	border-radius: 8rpx;

	overflow: hidden;

	background-color: #f5f7fa;



	&--fail {

		animation: verify-shake 0.4s ease;

	}

}



.verify-bg {

	display: block;

	width: 100%;

	height: 100%;

}



.gap-mask {

	position: absolute;

	background-color: rgba(0, 0, 0, 0.55);

	border: 1px solid rgba(255, 255, 255, 0.5);

	border-radius: 0 29rpx 29rpx 29rpx;

	box-sizing: border-box;

}



.puzzle-block {

	position: absolute;

	border-radius: 0 29rpx 29rpx 29rpx;

	overflow: hidden;

	z-index: 10;

	box-shadow: none;

	border: 1px solid rgba(255, 255, 255, 0.5);



	&--shake {

		animation: verify-shake 0.4s ease;

	}

}



.puzzle-img-wrapper {

	position: absolute;

	overflow: hidden;

}



.puzzle-img {

	display: block;

	width: 100%;

	height: 100%;

}



.verify-state {

	position: absolute;

	inset: 0;

	z-index: 20;

	display: flex;

	flex-direction: column;

	align-items: center;

	justify-content: center;

	gap: 16rpx;

	background: #f5f7fa;

	font-size: 26rpx;

	color: #999;



	&--error {

		color: #666;

	}

}



.verify-spinner {

	width: 48rpx;

	height: 48rpx;

	border: 4rpx solid #e8eaed;

	border-top-color: var(--webColor);

	border-radius: 50%;

	animation: verify-rotate 0.75s linear infinite;

}



.retry-btn {

	padding: 10rpx 28rpx;

	border: 1px solid var(--webColor);

	border-radius: 8rpx;

	color: var(--webColor);

	font-size: 24rpx;

}



.slider-track {

	position: relative;

	height: 70rpx;

	background-color: #f5f7fa;

	border: 1px solid #e8eaed;

	border-radius: 35rpx;

	margin-top: 20rpx;

	box-sizing: border-box;

	display: flex;

	align-items: center;

	justify-content: center;

	overflow: hidden;



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

	background: rgba(0, 0, 0, 0.06);

	border-radius: 35rpx 0 0 35rpx;

	pointer-events: none;

}



.slider-btn {

	position: absolute;

	top: 0;

	left: 0;

	width: 70rpx;

	height: 70rpx;

	background-color: var(--webColor);

	border-radius: 50%;

	display: flex;

	align-items: center;

	justify-content: center;

	box-shadow: none;

	box-sizing: border-box;

	z-index: 5;



	&--disabled {

		opacity: 0.6;

	}

}



.slider-tips {

	font-size: 24rpx;

	color: #999;

	user-select: none;



	&--fail {

		color: #ff4d4f;

	}

}



@keyframes verify-rotate {

	to {

		transform: rotate(360deg);

	}

}



@keyframes verify-shake {

	0%,

	100% {

		transform: translateX(0);

	}



	20%,

	60% {

		transform: translateX(-8rpx);

	}



	40%,

	80% {

		transform: translateX(8rpx);

	}

}

</style>

