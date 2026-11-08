<template>
  <div>
    <div class="wrap">
      <div ref="step1" class="settle-cont-box">
        <h3>商家店铺入驻流程</h3>
        <el-steps :active="4" align-center style="margin-top: 50px">
          <el-step title="入驻须知" :icon="ElIconEdit"></el-step>
          <el-step title="商家基本信息填写" :icon="ElIconUpload"></el-step>
          <el-step title="缴纳店铺租金" :icon="ElIconPostcard"></el-step>
          <el-step title="入驻成功" :icon="ElIconPicture"></el-step>
        </el-steps>
        <div class="">
          <div class="imagetextserve">
            <div class="boxsever"></div>
            <div class="textgroupserve">
              <span class="textserve block">恭喜您开店成功</span>
              <span class="textserveyy"
                >您可以在平台售卖商品，请先至店铺后台完成店铺基本设置~</span
              >
              <div class="textwrapperonetow">
                <span class="textonetow" @click="shangjiahoutai"
                  >进入商家后台</span
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  Edit as ElIconEdit,
  Upload as ElIconUpload,
  Picture as ElIconPicture,
  Postcard as ElIconPostcard,
} from '@element-plus/icons-vue'
import { useAppConfig } from '@/composables/useAppConfig'

const { baseUrl, safeOpenUrl } = useAppConfig()

function shangjiahoutai() {
  safeOpenUrl(baseUrl + '/bcenter/origin/login', { source: 'web' })
}
</script>

<style>
.imagetextserve {
  width: 500px;
  height: 500px;
  margin-left: 30%;
  margin-top: 90px;
}
.boxsever {
  width: 270px;
  height: 176px;
  background: url(../../assets/images/join/kaidianchenhgong.png) 100% no-repeat;
  background-size: 100% 100%;
  margin-left: 115px;
}
.textgroupserve {
  width: 500px;
  height: 69px;
  margin-top: 20px;
}
.textserve {
  width: 168px;
  height: 33px;
  color: rgba(51, 51, 51, 1);
  font-size: 24px;
  text-align: center;
  line-height: 33px;
  margin-left: 166px;
}
.textserveyy {
  width: 500px;
  height: 24px;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  text-align: center;
  line-height: 24px;
  margin-top: 12px;
  margin-left: 70px;
}
.textwrapperonetow {
  background-color: rgba(219, 44, 44, 1);
  border-radius: 4px;
  height: 40px;
  width: 162px;
  margin-top: 60px;
  margin-left: 170px;
  cursor: pointer;
}
.textonetow {
  width: 84px;
  height: 40px;
  color: rgba(255, 255, 255, 1);
  font-size: 14px;
  text-align: center;
  line-height: 40px;
  margin: 10px 0 0 39px;
}
</style>
