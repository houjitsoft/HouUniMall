<template>
    <div ref="home" class="wrap">
        <div class="index-head clearfix">
            <div class="fl" style="margin-left: 10px">
                <img src="../../assets/images/join/index-ad.png" alt="" style="
            width: 392px;
            height: auto;
            margin: 50px auto;
            padding: 0px;
            position: absolute;
          " />
                <div class="fl" style="margin-top: 150px">
                    <img src="../../assets/images/join/biaoqian.png" alt="" style="
              margin-left: 5px;
              margin-bottom: -3px;
              width: 18px;
              height: 18px;
            " />
                    <span class="biaoqian"> 优先推荐 </span>
                    <img src="../../assets/images/join/biaoqian.png" alt="" style="
              margin-left: 60px;
              margin-bottom: -3px;
              width: 18px;
              height: 18px;
            " />
                    <span class="biaoqian"> 运营推广 </span>
                    <img src="../../assets/images/join/biaoqian.png" alt="" style="
              margin-left: 60px;
              margin-bottom: -3px;
              width: 18px;
              height: 18px;
            " />
                    <span class="biaoqian"> 数据总览 </span>
                </div>
            </div>
            <img src="../../assets/images/join/settleImg.png" alt="" class="fr"
                style="width: 390px; height: auto; margin-right: 30px" />
        </div>
        <div class="moduleshangjia">
            <div class="shangjiabt">
                <span class="liucheng"> 商家入驻流程 </span>
            </div>
            <div class="group fl">
                <div class="boxgroup fl">
                    <div class="groupjustify">
                        <div class="textwrapper fl">
                            <span class="textone13">01</span>
                        </div>
                        <div class="groupend fl"></div>
                        <span class="text_14">入驻须知</span>
                    </div>
                    <div class="groupjustify">
                        <div class="textwrapper fl">
                            <span class="textone13">02</span>
                        </div>
                        <div class="groupend fl"></div>
                        <span class="text_14">商家信息提交</span>
                    </div>
                    <div class="groupjustify">
                        <div class="textwrapper fl">
                            <span class="textone13">03</span>
                        </div>
                        <div class="groupend fl"></div>
                        <span class="text_14">平台审核资质</span>
                    </div>
                    <div class="groupjustify">
                        <div class="textwrapper fl">
                            <span class="textone13">04</span>
                        </div>
                        <div class="groupend fl"></div>
                        <span class="text_14">商业费用</span>
                    </div>
                    <div class="groupjustify">
                        <div class="textwrapper fl">
                            <span class="textone13">05</span>
                        </div>
                        <div class="groupend fl"></div>
                        <span class="text_14">开通</span>
                    </div>
                    <div class="group_32">
                        <div class="box_9"></div>
                    </div>
                </div>
                <div class="gerengr fl">
                    <div class="gerentp">
                        <img src="../../assets/images/join/ruzhuqiye.png" />
                        <h3 class="wenanh3">企业店铺</h3>
                        <h4 class="wenanh4">使用企业经营者进行认证</h4>
                        <div class="module-content marginbutton">
                            <el-button type="danger" round @click="openshop">立即开店</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="zzcl">
            <div class="shangjiabt">
                <span class="liucheng"> 资质材料 </span>
            </div>
            <div class="zzcldianpu">
                <div class="dianpuzhuti fl">店铺主体</div>
                <div class="zhutizz fl">主体资质</div>
                <div class="xiangxishuoming fl">详细说明</div>
            </div>
            <div class="zzclqiye">
                <div class="dianpuztgr fl">企业</div>
                <div class="zhutizzgr fl">统一社会信用代码证</div>
                <div class="xiangxishuominggr fl">
                    <p class="neirong">
                        *<span class="juli">需提供营业执照类型为“公司/企业/个体工商户”营业执照原件扫描件或照片</span>
                    </p>
                    <p class="neirongtwo">
                        *<span class="juli">须露出证件四角，请勿遮挡或模糊，保持信息清晰可见</span>
                    </p>
                    <p class="neirongtwo">
                        *<span class="juli">确保未在企业经营异常名录中且所售商品在营业执照经营范围内</span>
                    </p>
                    <p class="neirongtwo">
                        *<span class="juli">若营业执照的公司名称为星号或空白等，不支持入驻，须先前往工商局添加公司名称</span>
                    </p>
                </div>
            </div>
        </div>
        <div class="cjwt">
            <div class="shangjiabt">
                <span class="liucheng"> 常见问题 </span>
            </div>
            <div class="cjwtfx">
                <div class="xuhao fl">
                    <span class="xhwz">01</span>
                </div>
                <div class="textdianpu fl">
                    <p class="texttfive">店铺入驻资费？</p>
                    <span class="texttsix">不同的店铺分类对应的租金不同，平台只收取店铺租金费用后续所有功能均免费使用。</span>
                </div>
            </div>
            <div class="cjwtfx">
                <div class="xuhao fl">
                    <span class="xhwz">02</span>
                </div>
                <div class="textdianpu fl">
                    <p class="texttfive">主营分类怎么选？</p>
                    <span class="texttsix">主营分类即店铺经营的商品分类，在店铺入驻过程中需要确认好店铺的经营分类，入驻后，也可联系平台进行调整。</span>
                </div>
            </div>
            <div class="cjwtfx">
                <div class="xuhao fl">
                    <span class="xhwz">03</span>
                </div>
                <div class="textdianpu fl">
                    <p class="texttfive">同一个账号可以开多家线上店铺吗？</p>
                    <span class="texttsix">不可以，目前只支持一个手机号开通一家线上店铺。</span>
                </div>
            </div>
            <div class="cjwtfx">
                <div class="xuhao fl">
                    <span class="xhwz">04</span>
                </div>
                <div class="textdianpu fl">
                    <p class="texttfive">发现手机号码已经认证店铺怎么办？</p>
                    <span class="texttsix">联系平台客服进行处理。</span>
                </div>
            </div>
        </div>
        <div class="lxwm">
            <div class="shangjiabt dingbujianju">
                <span class="liucheng"> 联系我们 </span>
            </div>
            <div class="lxkf">
                <div class="bugoulj fl">
                    <span class="lianxikefu fl">不够了解，联系客服</span>
                    <div class="tupainqt fl"></div>
                </div>
                <div class="lianxifs fl">
                    <div class="imagetextturee">
                        <div class="box fl"></div>
                        <span class="textgrouponewer">电话：{{ setting_phone }}</span>
                    </div>
                </div>
                <div class="lianxifs fl">
                    <div class="imagetextturee">
                        <div class="blocktow fl"></div>
                        <span class="textgrouponewer">邮箱：{{ setting_email }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
    queryPhoneAndMail,
    joinagree,
} from '@/api/join'
import { useAppConfig } from '@/composables/useAppConfig'

const router = useRouter()
const route = useRoute()
const { baseUrl, safeOpenUrl } = useAppConfig()

const data = ref({})
const setting_email = ref('')
const setting_phone = ref('')
const shopType = ref('')
const joinId = ref('')
const indexro = new URL('/bcenter/#/login?redirect=%2Fhome', baseUrl).href

onMounted(() => {
    shopType.value = route.query.shopType
    joinId.value = route.query.joinId
    fetchData()
})

function fetchData() {
    queryPhoneAndMail().then((res) => {
        const settings = Array.isArray(res.data) ? res.data : []
        setting_email.value = settings.find((item) => item?.configKey === 'setting_email')?.configValue || ''
        setting_phone.value = settings.find((item) => item?.configKey === 'setting_phone')?.configValue || ''
    })
    joinagree({}).then((res) => {
        if (res.code == 1) {
            data.value = res.data
            joinId.value = res.data.oldJoinId == null ? res.data.newJoinId : res.data.oldJoinId
            if (res.data.shopBaseExist == 1) {
                safeOpenUrl(indexro)
                return
            }
            if (res.data.isExist == 1) {
                if (res.data.shopType == 1 || res.data.shopType == 3) {
                    router.push({
                        path: '/join/step3',
                        query: { joinId: joinId.value, shopType: res.data.shopType },
                    })
                }
            }
        }
    })
}

function openshop() {
    const origin = route.query.origin || 0
    router.push({
        path: '/join/step1',
        query: { shopType: 1, joinId: joinId.value,origin },
    })
}
</script>

<style>
.moduleshangjia {
    width: 1200px;
    height: 700px;
    background: #ffffff;
    box-shadow: 0px 2px 14px 0px rgba(70, 22, 22, 0.1);
    border-radius: 24px;
}

.module-shangjia {
    width: 1200px;
    height: 660px;
    background: #ffffff;
    box-shadow: 0px 2px 14px 0px rgba(70, 22, 22, 0.1);
    border-radius: 24px;
}

.shangjiabt {
    height: 100px;
    margin-left: 30px;
    line-height: 100px;
}

.liucheng {
    width: 108px;
    height: 25px;
    font-size: 18px;
    font-weight: 500;
    color: #333333;
    line-height: 25px;
}

.shangjialc {
    height: 35px;
}

.wenan {
    margin-left: 66px;
    width: 141px;
    height: 35px;
    background: #fff4f4;
    border-radius: 18px;
    text-align: center;
}

.wenantwo {
    width: 141px;
    height: 35px;
    background: #fff4f4;
    border-radius: 18px;
    margin-left: 20px;
    text-align: center;
}

.tupian {
    margin-left: 20px;
    line-height: 40px;
}

.module-rzxz {
    width: 141px;
    height: 35px;
    font-size: 14px;
    font-weight: 500;
    color: #db2c2c;
    line-height: 35px;
}

.kaiqidianpu {
    height: 376px;
    margin-top: 50px;
}

.quanbugerenone {
    width: 556px;
    height: 450px;
    margin-left: 32px;
    background: #fff8f4;
    border-radius: 24px;
}

.quanbuqiyeone {
    width: 556px;
    height: 450px;
    margin-left: 24px;
    background: #fff4f7;
    border-radius: 24px;
}

.gerentp {
    margin-top: 69px;
    text-align: center;
}

.wenanh3 {
    font-size: 16px;
    color: #333333;
}

.wenanh4 {
    font-size: 14px;
    font-weight: 400;
    color: #333333;
    margin-top: 24px;
}

.marginbutton {
    margin-top: 40px;
}

.zzcl {
    width: 1200px;
    background: #ffffff;
    box-shadow: 0px 2px 14px 0px rgba(70, 22, 22, 0.1);
    border-radius: 24px;
    margin-top: 40px;
}

.zzcldianpu {
    width: 1136px;
    height: 39px;
    background: #f7f8fa;
    margin-left: 30px;
    box-shadow: 0px 1px 0px 0px #ebedf0;
}

.zzclgeren {
    width: 1136px;
    height: 135px;
    background: #ffffff;
    margin-left: 30px;
    box-shadow: inset 0px -1px 0px 0px #ebedf0;
}

.zzclqiye {
    width: 1136px;
    height: 135px;
    background: #ffffff;
    margin-left: 30px;
    box-shadow: inset 0px -1px 0px 0px #ebedf0;
}

.dianpuzhuti {
    height: 39px;
    line-height: 39px;
    margin-left: 60px;
}

.zhutizz {
    height: 39px;
    line-height: 39px;
    margin-left: 187px;
}

.xiangxishuoming {
    height: 39px;
    line-height: 39px;
    margin-left: 370px;
}

.dianpuztgr {
    height: 110px;
    line-height: 110px;
    margin-left: 60px;
    font-size: 16px;
    font-weight: 400;
    color: #db2c2c;
}

.zhutizzgr {
    height: 110px;
    width: 150px;
    line-height: 110px;
    margin-left: 187px;
}

.xiangxishuominggr {
    width: 514px;
    height: 59px;
    font-size: 12px;
    font-weight: 400;
    color: #db2c2c;
    line-height: 13px;
    margin-top: 30px;
    margin-left: 160px;
}

.juli {
    margin-left: 5px;
    color: #333333;
}

.neirongtwo {
    margin-top: 10px;
}

.cjwt {
    width: 1200px;
    height: 818px;
    background: #ffffff;
    box-shadow: 0px 2px 14px 0px rgba(70, 22, 22, 0.1);
    border-radius: 24px;
    margin-top: 50px;
}

.dianpuqubie {
    width: 1137px;
    height: 130px;
    background: #f2f5fc;
    border-radius: 24px;
    margin-left: 32px;
}

.cjwtfx {
    width: 1137px;
    height: 107px;
    background: #f2f5fc;
    border-radius: 24px;
    margin-left: 32px;
    margin-top: 24px;
}

.xuhao {
    width: 44px;
    height: 71px;
    background: linear-gradient(180deg, #5d96ff 0%, #0055fc 100%);
    border-radius: 0px 0px 30px 30px;
    margin-left: 43px;
}

.xhwz {
    height: 71px;
    line-height: 71px;
    margin-left: 12px;
    color: aliceblue;
}

.textdianpu {
    width: 1003px;
    height: 91px;
    margin: 26px 23px 0 24px;
}

.texttfive {
    height: 25px;
    font-size: 18px;
    font-weight: 500;
    color: #333333;
    line-height: 25px;
}

.texttsix {
    width: 1003px;
    height: 58px;
    overflow-wrap: break-word;
    color: rgba(51, 51, 51, 1);
    font-size: 14px;
    text-align: left;
    line-height: 26px;
    margin-top: 8px;
}

.lxwm {
    height: 256px;
    background: url(../../assets/images/join/lxwmbeijin.png) -14px -12px no-repeat;
    background-size: 1228px 284px;
    width: 1200px;
    margin-bottom: 200px;
}

.lxkf {
    width: 1112px;
    height: 76px;
    margin: 70px 0 61px 36px;
}

.bugoulj {
    width: 282px;
    height: 25px;
    margin-top: 26px;
}

.lianxikefu {
    background-image: linear-gradient(180deg,
            rgba(255, 142, 20, 1) 0,
            rgba(219, 44, 44, 1) 100%);
    width: 218px;
    height: 25px;
    color: rgba(51, 51, 51, 1);
    font-size: 18px;
    text-align: left;
    line-height: 25px;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.tupainqt {
    width: 53px;
    height: 20px;
    background: url(../../assets/images/join/zhixiang.png) 100% no-repeat;
    background-size: 100% 100%;
    margin-top: 3px;
}

.lianxifs {
    background-color: rgba(153, 191, 255, 0.1);
    border-radius: 10px;
    width: 380px;
    height: 76px;
    line-height: 76px;
    margin-left: 25px;
}

.imagetextturee {
    width: 345px;
    height: 25px;
    margin-left: 20px;
}

.box {
    margin-top: 27px;
    margin-right: 10px;
    width: 24px;
    height: 24px;
    background: url(../../assets/images/join/shouji.png) 100% no-repeat;
    background-size: 100% 100%;
}

.textgrouponewer {
    width: 216px;
    height: 25px;
    color: rgba(51, 51, 51, 1);
    font-size: 18px;
    text-align: left;
    line-height: 25px;
}

.blocktow {
    width: 25px;
    height: 25px;
    margin-top: 27px;
    margin-right: 10px;
    background: url(../../assets/images/join/youxiang.png) 100% no-repeat;
    background-size: 100% 100%;
}

.dingbujianju {
    margin-top: 50px;
}

.boxgroup {
    border-radius: 24px 0px 0px 24px;
    height: 517px;
    border: 1px solid rgba(227, 239, 255, 1);
    width: 575px;
    position: relative;
}

.groupjustify {
    height: 44px;
    margin: 53px 0 0 101px;
}

.textwrapper {
    background-color: rgba(255, 255, 255, 1);
    border-radius: 50%;
    height: 48px;
    border: 2px solid rgba(219, 44, 44, 1);
    width: 48px;
    margin: -2px 0 0 -2px;
}

.textone13 {
    width: 19px;
    height: 25px;
    color: rgba(219, 44, 44, 1);
    font-size: 18px;
    text-align: right;
    line-height: 47px;
    margin: 11px 0 0 14px;
}

.groupend {
    background-color: rgba(219, 44, 44, 1);
    width: 121px;
    height: 1px;
    margin: 22px 0 0 -2px;
}

.text_14 {
    width: 79px;
    height: 25px;
    color: rgba(219, 44, 44, 1);
    font-size: 18px;
    text-align: left;
    line-height: 46px;
    margin: 10px 0 0 8px;
}

.group_32 {
    width: 274px;
    height: 359px;
    margin: -395px 0 0px 124px;
}

.box_9 {
    width: 2px;
    height: 346px;
    background-size: 100% 100%;
}

.gerengr {
    width: 556px;
    height: 517px;
    background: #f4f8ff;
    border-radius: 0px 24px 24px 0px;
    border: 1px solid #e3efff;
}

.group {
    width: 1136px;
    height: 517px;
    margin: 24px 0 34px 32px;
}

.biaoqian {
    font-size: 16px;
}
</style>
