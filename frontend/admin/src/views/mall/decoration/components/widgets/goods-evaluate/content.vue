<template>
    <div class="p-2">
        <div class="bg-white p-2 rounded-lg">
            <div class="flex justify-between items-center">
                <div class="font-medium">{{ content.title }}</div>
                <div class="form-tips">97%好评率</div>
            </div>
            <template v-for="item in content.show_conmment" :key="item">
                <div class="mt-4">
                    <div class="flex items-center">
                        <img src="./images/avatar.png" />
                        <div class="ml-2">阿巴阿巴桑桑</div>
                    </div>
                    <div class="mt-2">
                        收到货后迫不及待打开看了，质量真的很好，手感细腻嫩滑，做工非常精细，完全没有异味，材质摸起来...
                    </div>
                    <div class="mt-2 flex">
                        <img src="./images/goods.png" />
                        <img src="./images/goods.png" class="ml-2" />
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    }
})
</script>

<style lang="scss" scoped></style>
