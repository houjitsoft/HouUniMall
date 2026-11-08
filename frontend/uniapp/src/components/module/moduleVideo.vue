<template>
	<view class="moduleVideo" :style="{paddingTop:propsobj.padding,paddingBottom:propsobj.padding}">
		<swiper class="swiper video-swiper" :class="[propsobj.radius=='2'?'radius':'']" :autoplay="false" :current="currentInnerIndex" @change="handleSwiperChange">  
			<swiper-item v-for="(item,index) in propsobj.items" :key="item.fileUrl || index">
				<video 
					@loadedmetadata="loadVideo(index)"
					@play="handleVideoPlay(index)" 
					@ended="handleVideoEnded"
					@click="handleVideoClick"
					class="wp100" 
					:id="`video_${propsobj.componentId}_${index}`"
					:src="item.fileUrl"  
					:poster="item.fileCoverUrl" 
					controls="true"
				></video>
			</swiper-item>
		</swiper>    
	</view>
</template>

<script>
	export default {
		name: 'moduleVideo',
		props: ['propsobj'],
		data() {
			return {
				token: uni.getStorageSync('token'),
				videoContextList: [], // 当前组件实例的视频上下文列表
				currentInnerIndex: -1 ,// 当前组件实例内正在播放的视频索引
				isUserInteracted: false // 标记用户是否首次主动交互（解锁 App 端自动播放）
			}
		},
		// 保留 Vue 原生 mounted 生命周期，不使用 onLoad/onReady
		mounted() {
			// 1. 初始化全局视频状态（小程序端全局挂载，无隔离，在 mounted 中初始化）
			if (!uni.$videoGlobalState) {
				uni.$videoGlobalState = {
					currentPlay: null,
					isGlobalInteracted: false // 全局交互标记
				};
			}
			// 2. 绑定跨模块视频播放事件（Vue 生命周期中绑定，小程序端兼容）
			// 先移除重复监听（避免组件重复挂载时监听叠加）
			uni.$off('videoPlayGlobal', this.handleCrossModuleVideoPlay);
			uni.$on('videoPlayGlobal', this.handleCrossModuleVideoPlay);

			// 3. 原有视频上下文初始化逻辑，保留 500ms 延迟
			setTimeout(() => {
			    this.initVideoContext();
			    if (this.propsobj.items && this.propsobj.items.length > 0) {
			      this.currentInnerIndex = 0;
			    }
			  }, 500)
		},
		// 保留 Vue 原生 beforeDestroy 生命周期，替代 onUnload，移除监听
		beforeDestroy() {
			// 组件销毁时移除全局事件监听，避免内存泄漏（小程序端必须手动移除）
			uni.$off('videoPlayGlobal', this.handleCrossModuleVideoPlay);
		},
		// 监听视频列表变化，重新初始化上下文
		watch: {
			'propsobj.items': {
				deep: true,
				handler() {
					this.initVideoContext();
					this.currentInnerIndex = -1;
				}
			}
		},
		methods: {
			// 新增：跨模块视频播放监听，自动暂停当前模块所有视频（核心解决小程序多模块同时播放）
			handleCrossModuleVideoPlay(playInfo) {
				const { componentId } = this.propsobj;
				// 仅暂停其他模块的视频（通过 componentId 区分）
				if (playInfo && playInfo.componentId !== componentId) {
					// 1. 上下文列表暂停（快速暂停）
					this.videoContextList.forEach((context) => {
						if (context && typeof context.pause === 'function') {
							context.pause();
						}
					});
					// 2. ID 兜底暂停（防止上下文失效，小程序端必备）
					const { items } = this.propsobj;
					if (items && items.length > 0) {
						items.forEach((item, index) => {
							const videoId = `video_${componentId}_${index}`;
							// 必须传入 this，精准定位当前组件内的视频（小程序端关键）
							const videoContext = uni.createVideoContext(videoId, this);
							if (videoContext && typeof videoContext.pause === 'function') {
								videoContext.pause();
							}
						});
					}
					// 重置当前模块的播放索引
					this.currentInnerIndex = -1;
				}
			},
			// 初始化当前组件实例的所有视频上下文
			initVideoContext() {
			  const { items, componentId } = this.propsobj;
			  if (!items || items.length === 0 || !componentId) return;
			  this.videoContextList = [];
			  items.forEach((item, index) => {
			    const videoId = `video_${componentId}_${index}`;
			    // 逐个延迟初始化，避免小程序端并发获取上下文失效
			    setTimeout(() => {
			      const videoContext = uni.createVideoContext(videoId, this);
			      this.videoContextList[index] = videoContext;
			    }, 100 * index);
			  });
			},
			// 
			handleVideoClick(e) {
			  if (!uni.$videoGlobalState.isGlobalInteracted) {
			    uni.$videoGlobalState.isGlobalInteracted = true;
			    this.isUserInteracted = true; // 同步组件内标记
			    // 原生交互触发兜底
			    const videoId = e.target.id || e.currentTarget.id;
			    if (videoId) {
			      const videoContext = uni.createVideoContext(videoId, this);
			      if (videoContext) {
			        videoContext.pause();
			      }
			    }
			  }
			},
			// 封装 App 端兼容的视频播放方法（带授权判断，替换全局状态来源）
			safeVideoPlay(videoId) {
			  // 从 uni.$videoGlobalState 获取全局授权状态，小程序端无隔离
			  const hasPermission = uni.$videoGlobalState.isGlobalInteracted || this.isUserInteracted;
			  if (!hasPermission) {
			    return;
			  }
			
			  // 重试机制：最多重试 2 次，解决临时组件未就绪问题
			  const playVideo = (retryCount = 0) => {
			    const videoContext = uni.createVideoContext(videoId, this);
			    if (!videoContext) {
			      if (retryCount < 2) {
			        setTimeout(() => playVideo(retryCount + 1), 200);
			      }
			      return;
			    }
			
			    try {
			      videoContext.play();
			      console.log('视频播放成功，ID：', videoId);
			    } catch (e) {
			      // 捕获播放异常，重试一次
			      if (retryCount < 2) {
			        setTimeout(() => playVideo(retryCount + 1), 200);
			      } else {
			        console.error('视频播放失败，ID：', videoId, '错误：', e);
			      }
			    }
			  };
			
			  // 延迟 500ms 后执行播放，带重试
			  setTimeout(() => {
			    playVideo(0);
			  }, 500);
			},
			// 封装 App 端兼容的视频暂停方法
			safeVideoPause(videoId) {
			    const videoContext = uni.createVideoContext(videoId, this);
			    if (videoContext && typeof videoContext.pause === 'function') {
			      setTimeout(() => {
			        videoContext.pause();
			      }, 100);
			    }
			},
			// 视频元数据加载完成（兜底：确保上下文初始化）
			loadVideo(index) {
				const { componentId } = this.propsobj;
				if (!componentId) return;
				const videoId = `video_${componentId}_${index}`;
				// 避免重复初始化
				if (!this.videoContextList[index]) {
					const videoContext = uni.createVideoContext(videoId, this);
					this.videoContextList[index] = videoContext;
				}
			},
			// 核心：内外双层互斥播放逻辑（修改：触发全局事件，解决小程序跨模块问题）
			handleVideoPlay(playIndex) {
			  const { componentId, items } = this.propsobj;
			  if (!componentId || !items) return;
			
			  // 1. 当前组件内互斥：暂停所有非当前视频（原有逻辑不变，兼容所有端）
			  items.forEach((item, index) => {
			    if (index !== playIndex) {
			      const videoId = `video_${componentId}_${index}`;
			      const videoContext = uni.createVideoContext(videoId, this);
			      if (videoContext && typeof videoContext.pause === 'function') {
			        videoContext.pause();
			      }
			    }
			  });
			
			  // 2. 触发全局事件，通知所有其他模块暂停视频（小程序端跨模块互斥核心）
			  const playInfo = { componentId, videoIndex: playIndex };
			  uni.$emit('videoPlayGlobal', playInfo);
			
			  // 3. 更新全局状态
			  uni.$videoGlobalState.currentPlay = playInfo;
			},
			// 逻辑：手动切换swiper时 - 暂停原视频，播放新视频（原有逻辑不变）
			handleSwiperChange(e) {
			  const { componentId, items } = this.propsobj;
			  if (!componentId || !items || items.length === 0) return;
			
			  const newVideoIndex = e.detail.current;
			  const oldVideoIndex = this.currentInnerIndex;
			
			  if (newVideoIndex === oldVideoIndex) return;
			
			  // 1. 双重暂停原视频（使用兼容方法）
			  if (oldVideoIndex !== -1) {
			    // 方式1：上下文列表暂停
			    if (this.videoContextList[oldVideoIndex]) {
			      this.videoContextList[oldVideoIndex].pause();
			    }
			    // 方式2：ID 精准暂停（兜底）
			    const oldVideoId = `video_${componentId}_${oldVideoIndex}`;
			    this.safeVideoPause(oldVideoId);
			  }
			
			  // 2. 优先更新逻辑索引，强制数据驱动 Swiper 切换
			  this.currentInnerIndex = newVideoIndex;
			
			  // 3. 等待视图完全渲染，播放新视频
			  this.$nextTick(() => {
			    setTimeout(() => {
			      // 执行互斥逻辑
			      this.handleVideoPlay(newVideoIndex);
			      // 用兼容方法播放新视频
			      const newVideoId = `video_${componentId}_${newVideoIndex}`;
			      this.safeVideoPlay(newVideoId);
			    }, 200);
			  });
			},
			// 逻辑2：视频播放完毕后 - 自动切换swiper，再播放新视频（原有逻辑不变）
			handleVideoEnded() {
			  const { componentId, items } = this.propsobj;
			  if (!componentId || !items || items.length === 0 || this.currentInnerIndex === -1) return;
			
			  const videoCount = items.length;
			  const nextVideoIndex = (this.currentInnerIndex + 1) % videoCount;
			  const oldVideoId = `video_${componentId}_${this.currentInnerIndex}`;
			  const newVideoId = `video_${componentId}_${nextVideoIndex}`;
			
			  // 1. 暂停原视频（兼容方法）
			  this.safeVideoPause(oldVideoId);
			
			  // 2. 数据驱动 Swiper 切换（确保视图更新）
			  this.currentInnerIndex = nextVideoIndex;
			
			  // 3. 双重延迟 + 双重播放触发（App 端核心：等待原生组件完全就绪）
			  this.$nextTick(() => {
			    // 第一层延迟：等待 Swiper 视图渲染完成
			    setTimeout(() => {
			      // 先执行互斥逻辑
			      this.handleVideoPlay(nextVideoIndex);
			
			      // 方式1：使用兼容的 safeVideoPlay 播放（优先）
			      this.safeVideoPlay(newVideoId);
			
			      // 方式2：直接通过视频索引获取组件，强制触发播放（兜底）
			      setTimeout(() => {
			        const newVideoContext = uni.createVideoContext(newVideoId, this);
			        if (newVideoContext && typeof newVideoContext.play === 'function') {
			          newVideoContext.play();
			          console.log('新视频兜底播放触发，视频ID：', newVideoId);
			        }
			      }, 300);
			
			    }, 400);
			  });
			}
		}
	}
</script>

<style lang="scss">
	.moduleVideo .swiper{
		margin-left:20rpx;
		margin-right:20rpx;
		height:200px;
		position: relative;
		z-index: 1;
	}
	.moduleVideo .swiper video{
		height:200px;
		position: relative;
		z-index: 1;
	}
	.radius {
	    border-radius: 20rpx;
	    overflow: hidden;
	}
	.uni-video-cover-duration{
		display:none !important;
	}
</style>