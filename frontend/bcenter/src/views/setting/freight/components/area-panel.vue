<template>
    <el-scrollbar height="400px" class="p-4 mt-4 border border-[#e5e5e5] rounded-md">
        <el-tree
            ref="treeRef"
            :data="data"
            node-key="id"
            :props="{
                label: 'name',
                children: 'sub',
            }"
            :filter-node-method="filterNode"
        >
            <template #default="{ node, data }">
                <div class="flex justify-between w-full">
                    <span>{{ node.label }}</span>

                    <el-button 
                        v-if="isSelect"
                        type="primary" 
                        :link="true"
                        @click.stop="onSelect(node, data)"
                    > 选择 </el-button>

                    <el-button 
                        v-else
                        type="primary" 
                        :link="true"
                        @click.stop="onCancel(data)"
                    > 取消 </el-button>
                </div>
            </template>    
        </el-tree>
    </el-scrollbar>
</template>

<script lang="ts" setup>
import type { PropType } from "vue"
import type { Tree } from "./type"
import { ElTree } from 'element-plus'
import useAreaEffect from "./useAreaEffect"

const props = defineProps({
    data: {
        type: Array as PropType<any>
    },
    isSelect: {
        type: Boolean
    },
    filterText: {
        type: String
    }
})

const treeRef = shallowRef<InstanceType<typeof ElTree>>();

const { filterNode, onSelect, onCancel } = useAreaEffect({
    refs: treeRef,
    props: props
});
</script>