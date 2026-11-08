<template>
    <el-form-item label="营业状态">
        <div>
            <el-radio-group v-model="modelValue.business_status">
                <el-radio :label="1">营业中</el-radio>
                <el-radio :label="0">暂停营业</el-radio>
            </el-radio-group>

            <div class="form-tips">商家暂停营业后，则不能对外提供服务</div>
        </div>
    </el-form-item>

    <el-form-item label="商家地址">
        <div>
            <map-select
                class="w-[320px]"
                v-model:province="modelValue.province_id"
                v-model:city="modelValue.city_id"
                v-model:district="modelValue.district_id"
            />
            <el-input
                v-model="modelValue.address_info"
                type="textarea"
                class="w-[320px] mt-2"
                placeholder="请输入详细地址"
            ></el-input>
        </div>
    </el-form-item>
    <el-form-item label="地图定位">
        <div>
            <div>
                <el-input v-model="modelValue.lng" class="w-[180px]">
                    <template #append>经度 </template>
                </el-input>
                <el-input v-model="modelValue.lat" class="w-[180px]">
                    <template #append>纬度 </template>
                </el-input>
                <el-button type="primary" class="ml-2" @click="onMapSearch">搜索地图</el-button>
            </div>
            <div id="container" class="map-container mt-2 w-[600px]"></div>
        </div>
    </el-form-item>

    <el-form-item label="退货联系人" prop="contact">
        <div class="w-80">
            <el-input
                v-model="modelValue.return_contact"
                placeholder="请输入联系人名称"
                clearable
            />
        </div>
    </el-form-item>
    <el-form-item label="联系电话" prop="mobile">
        <div class="w-80">
            <el-input v-model="modelValue.return_mobile" placeholder="请输入联系电话" clearable />
        </div>
    </el-form-item>
    <el-form-item label="退货地址" prop="contact">
        <div class="w-80">
            <area-select
                class="w-[320px]"
                v-model:province="modelValue.return_province_id"
                v-model:city="modelValue.return_city_id"
                v-model:district="modelValue.return_district_id"
            />
            <el-input
                class="mt-2"
                type="textarea"
                v-model="modelValue.return_address_info"
                placeholder="请输入详细地址"
                clearable
                :autosize="{ minRows: 3, maxRows: 5 }"
            />
            <div class="form-tips">会员退货时显示在退货信息填写页面</div>
        </div>
    </el-form-item>
</template>

<script lang="ts" setup>
import { importCDN } from '@/utils/importCDN'
import iconMarker from '@/assets/images/icon_marker.png'
import { apiMapRegionSearch } from '@/api/app'
import feedback from '@/utils/feedback'

const props = withDefaults(
    defineProps<{
        modelValue?: any
    }>(),
    {
        modelValue: {}
    }
)
let map: any = null
let TMap: any = null
let markerLayer: any = null
//导入腾讯地图api
const loadTencentMapScript = () => {
    const TENTCENT_MAP_API = `https://map.qq.com/api/gljs?v=1.exp&key=A34BZ-FT5K6-DTPSC-E6RVP-JOHHV-WQB27`
    importCDN(TENTCENT_MAP_API).then(() => initTencentMap())
}
//初始化地图
const initTencentMap = () => {
    TMap = (window as any).TMap // TMap地图实例
    const LatLng = TMap.LatLng // 用于创建经纬度坐标实例
    const center = new LatLng(props.modelValue.lat, props.modelValue.lng) //设置中心点坐标

    // 销毁上一次地图
    new TMap.Map('container', {
        center
    }).destroy()

    //初始化地图
    map = new TMap.Map('container', {
        center: center
    })

    //创建并初始化MultiMarker:表示地图上的多个标注点，可自定义标注的图标。,

    markerLayer = new TMap.MultiMarker({
        map: map, //指定地图容器
        //样式定义
        styles: {
            //创建一个styleId为"myStyle"的样式（styles的子属性名即为styleId）
            myStyle: new TMap.MarkerStyle({
                width: 34, // 点标记样式宽度（像素）
                height: 34, // 点标记样式高度（像素
                src: iconMarker, //图片路径
                //焦点在图片中的像素位置，一般大头针类似形式的图片以针尖位置做为焦点，圆形点以圆心位置为焦点
                anchor: { x: 17, y: 34 }
            })
        },
        //点标记数据数组
        geometries: [
            {
                id: '1', //点标记唯一标识，后续如果有删除、修改位置等操作，都需要此id
                styleId: 'myStyle', //指定样式id
                position: center //点标记坐标位置
            }
        ]
    })

    //监听地图点击事件
    map.on('click', (event: any) => {
        markerLayer.updateGeometries([
            {
                styleId: 'myStyle',
                id: '1',
                position: new TMap.LatLng(event.latLng.lat, event.latLng.lng)
            }
        ])
        //赋值给value
        props.modelValue.lat = event.latLng.lat
        props.modelValue.lng = event.latLng.lng
    })
}
//搜索地址
const onMapSearch = async () => {
    try {
        const { data }: any = await apiMapRegionSearch({
            tencent_keyword: encodeURI(props.modelValue.address_info),
            tencent_boundary: `region(${
                props.modelValue.district_id ?? props.modelValue.city_id
            }, 0)`,
            tencent_key: 'A34BZ-FT5K6-DTPSC-E6RVP-JOHHV-WQB27'
        })
        if (!data.length) {
            return feedback.msgError(`没有找到“${props.modelValue.address_info}”的相关地点`)
        }
        markerLayer.setGeometries([])
        // 添加自己位置的标点
        markerLayer.add([
            {
                id: '1',
                styleId: 'me',
                position: new TMap.LatLng(props.modelValue.lat, props.modelValue.lng)
            }
        ])
        const geometries: any = []
        data.forEach((item: any) => {
            const { lat, lng } = item.location
            geometries.push({
                id: item.id,
                styleId: 'marker',
                position: new TMap.LatLng(lat, lng),
                properties: {
                    title: item.title
                }
            })
        })
        // 更改地图中心点
        const { lat, lng } = data[0].location
        map?.setCenter({ lat, lng })
        // 添加标点
        markerLayer.add(geometries)
    } catch (error) {
        console.error(error)
    }
}
const addMobile = () => {
    props.modelValue.mobile.push('')
}

const removeMobile = (index: number) => {
    if (props.modelValue.mobile.length <= 1) return
    props.modelValue.mobile.splice(index, 1)
}
onMounted(async () => {
    loadTencentMapScript()
})
</script>

<style>
.el__rate .el-rate__item {
    line-height: 10px;
}
</style>
