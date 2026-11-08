package com.houjiang.plugin.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 任务枚举语义测试。
 *
 * <p>这三个枚举是整条安装/升级链路的状态机地基：并发互斥（{@code isActive}）、
 * 断点续做（{@code getOrder}）、「失败要不要回滚」（{@code requiresRollbackOnFailure}）
 * 全部依赖它们的语义，语义错了编排就会在故障场景下做错动作，因此必须钉死。
 *
 * @author Amos
 * @since M3
 */
class UpdateEnumsTest {

    @Test
    void 终态判定与活动态判定相反() {
        assertTrue(UpdateTaskState.SUCCESS.isTerminal());
        assertTrue(UpdateTaskState.FAILED.isTerminal());
        assertTrue(UpdateTaskState.ROLLED_BACK.isTerminal());
        assertFalse(UpdateTaskState.PENDING.isTerminal());
        assertFalse(UpdateTaskState.RUNNING.isTerminal());

        assertTrue(UpdateTaskState.PENDING.isActive());
        assertTrue(UpdateTaskState.RUNNING.isActive());
        assertFalse(UpdateTaskState.SUCCESS.isActive());
        assertFalse(UpdateTaskState.FAILED.isActive());
    }

    @Test
    void 状态按入库值解析且大小写不敏感() {
        assertEquals(UpdateTaskState.RUNNING, UpdateTaskState.fromCode("RUNNING"));
        assertEquals(UpdateTaskState.ROLLED_BACK, UpdateTaskState.fromCode(" rolled_back "));
        assertEquals("PENDING", UpdateTaskState.PENDING.getCode());
        assertEquals("等待中", UpdateTaskState.PENDING.getDescription());
    }

    @Test
    void 状态空值或未知值抛异常() {
        assertThrows(IllegalArgumentException.class, () -> UpdateTaskState.fromCode(null));
        assertThrows(IllegalArgumentException.class, () -> UpdateTaskState.fromCode("  "));
        assertThrows(IllegalArgumentException.class, () -> UpdateTaskState.fromCode("UNKNOWN"));
    }

    @Test
    void 操作类型解析() {
        assertEquals(UpdateOpType.INSTALL, UpdateOpType.fromCode("install"));
        assertEquals(UpdateOpType.UPGRADE, UpdateOpType.fromCode("UPGRADE"));
        assertEquals(UpdateOpType.ROLLBACK, UpdateOpType.fromCode(" rollback "));
        assertEquals("安装", UpdateOpType.INSTALL.getDescription());
        assertThrows(IllegalArgumentException.class, () -> UpdateOpType.fromCode("DELETE"));
    }

    @Test
    void 备份之前的阶段失败不需要回滚() {
        assertFalse(UpdateStep.DOWNLOAD.requiresRollbackOnFailure());
        assertFalse(UpdateStep.VERIFY.requiresRollbackOnFailure());
        assertFalse(UpdateStep.COMPAT.requiresRollbackOnFailure());
    }

    @Test
    void 备份及之后的阶段失败必须回滚() {
        assertTrue(UpdateStep.BACKUP.requiresRollbackOnFailure());
        assertTrue(UpdateStep.STOP.requiresRollbackOnFailure());
        assertTrue(UpdateStep.SCHEMA.requiresRollbackOnFailure());
        assertTrue(UpdateStep.START.requiresRollbackOnFailure());
        assertTrue(UpdateStep.HEALTH.requiresRollbackOnFailure());
    }

    @Test
    void 阶段序号即流水线顺序且可按入库值解析() {
        assertEquals(0, UpdateStep.DOWNLOAD.getOrder());
        assertEquals(UpdateStep.VERIFY.getOrder() + 1, UpdateStep.COMPAT.getOrder());
        assertEquals(UpdateStep.COMPAT.getOrder() + 1, UpdateStep.BACKUP.getOrder());
        assertEquals(UpdateStep.SCHEMA.getOrder() + 1, UpdateStep.START.getOrder());
        assertEquals(UpdateStep.START.getOrder() + 1, UpdateStep.HEALTH.getOrder());
        assertEquals(UpdateStep.HEALTH, UpdateStep.fromCode(" health "));
        assertEquals("执行数据迁移", UpdateStep.SCHEMA.getDescription());
        assertThrows(IllegalArgumentException.class, () -> UpdateStep.fromCode("ROLLBACK"));
    }
}
