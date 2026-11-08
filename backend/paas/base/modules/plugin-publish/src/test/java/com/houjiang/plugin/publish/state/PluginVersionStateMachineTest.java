package com.houjiang.plugin.publish.state;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.commons.constant.PluginDistErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link PluginVersionStateMachine} 单测：合法跃迁全通过，非法跃迁一律被拒。
 *
 * @author Amos
 * @since M1
 */
class PluginVersionStateMachineTest {

    @Test
    @DisplayName("合法跃迁全部放行")
    void legalTransitionsAllowed() {
        assertDoesNotThrow(() -> PluginVersionStateMachine.assertTransition(
                PluginVersionStatus.DRAFT, PluginVersionStatus.PENDING));
        assertDoesNotThrow(() -> PluginVersionStateMachine.assertTransition(
                PluginVersionStatus.PENDING, PluginVersionStatus.PUBLISHED));
        assertDoesNotThrow(() -> PluginVersionStateMachine.assertTransition(
                PluginVersionStatus.PENDING, PluginVersionStatus.REJECTED));
        assertDoesNotThrow(() -> PluginVersionStateMachine.assertTransition(
                PluginVersionStatus.REJECTED, PluginVersionStatus.PENDING));
        assertDoesNotThrow(() -> PluginVersionStateMachine.assertTransition(
                PluginVersionStatus.PUBLISHED, PluginVersionStatus.OFFLINE));
        assertDoesNotThrow(() -> PluginVersionStateMachine.assertTransition(
                PluginVersionStatus.OFFLINE, PluginVersionStatus.PUBLISHED));
    }

    @Test
    @DisplayName("draft 不能直接跳 published（必须先提交审核）")
    void draftToPublishedRejected() {
        PluginVersionStateMachine.PluginStateTransitionException e = assertThrows(
                PluginVersionStateMachine.PluginStateTransitionException.class,
                () -> PluginVersionStateMachine.assertTransition(
                        PluginVersionStatus.DRAFT, PluginVersionStatus.PUBLISHED));
        assertEquals(PluginDistErrorCode.VERSION_CONFLICT, e.getCode());
    }

    @Test
    @DisplayName("published 不能回退到 draft（已发布版本不可回退编辑）")
    void publishedToDraftRejected() {
        assertFalse(PluginVersionStateMachine.canTransition(
                PluginVersionStatus.PUBLISHED, PluginVersionStatus.DRAFT));
        assertThrows(PluginVersionStateMachine.PluginStateTransitionException.class,
                () -> PluginVersionStateMachine.assertTransition(
                        PluginVersionStatus.PUBLISHED, PluginVersionStatus.DRAFT));
    }

    @Test
    @DisplayName("其它非法跃迁同样被拒")
    void otherIllegalTransitionsRejected() {
        assertFalse(PluginVersionStateMachine.canTransition(
                PluginVersionStatus.OFFLINE, PluginVersionStatus.PENDING));
        assertFalse(PluginVersionStateMachine.canTransition(
                PluginVersionStatus.REJECTED, PluginVersionStatus.PUBLISHED));
        assertFalse(PluginVersionStateMachine.canTransition(
                PluginVersionStatus.PUBLISHED, PluginVersionStatus.REJECTED));
        assertFalse(PluginVersionStateMachine.canTransition(null, PluginVersionStatus.PENDING));
        assertFalse(PluginVersionStateMachine.canTransition(PluginVersionStatus.DRAFT, null));
    }

    @Test
    @DisplayName("published / offline 是不可变终态：不可删除、不可被同版本覆盖")
    void immutableStatuses() {
        assertTrue(PluginVersionStateMachine.isImmutable(PluginVersionStatus.PUBLISHED));
        assertTrue(PluginVersionStateMachine.isImmutable(PluginVersionStatus.OFFLINE));
        assertFalse(PluginVersionStateMachine.isImmutable(PluginVersionStatus.DRAFT));
        assertFalse(PluginVersionStateMachine.isImmutable(PluginVersionStatus.PENDING));
        assertFalse(PluginVersionStateMachine.isImmutable(PluginVersionStatus.REJECTED));
    }

    @Test
    @DisplayName("只有 draft 允许覆盖制品")
    void artifactOverwritePolicy() {
        assertTrue(PluginVersionStateMachine.allowsArtifactOverwrite(PluginVersionStatus.DRAFT));
        assertFalse(PluginVersionStateMachine.allowsArtifactOverwrite(PluginVersionStatus.PENDING));
        assertFalse(PluginVersionStateMachine.allowsArtifactOverwrite(PluginVersionStatus.REJECTED));
        assertFalse(PluginVersionStateMachine.allowsArtifactOverwrite(PluginVersionStatus.PUBLISHED));
        assertFalse(PluginVersionStateMachine.allowsArtifactOverwrite(PluginVersionStatus.OFFLINE));
    }

    @Test
    @DisplayName("状态取值与现有代码固化语义一致（published=1，商品 published=2）")
    void statusCodesAlignedWithExistingCode() {
        assertEquals(1, PluginVersionStatus.PUBLISHED.getCode());
        assertEquals(0, PluginVersionStatus.DRAFT.getCode());
        assertEquals(3, PluginVersionStatus.PENDING.getCode());
        assertEquals(4, PluginVersionStatus.REJECTED.getCode());
        assertEquals(5, PluginVersionStatus.OFFLINE.getCode());
        assertEquals(2, PluginProductStatus.PUBLISHED.getCode());
        assertEquals(PluginVersionStatus.PUBLISHED, PluginVersionStatus.fromCode(1));
    }
}
