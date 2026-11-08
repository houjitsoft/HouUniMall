package com.houjiang.permission.support;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuAncestorSupportTest {

    @Test
    void expandAddsAncestorsInMemoryWithoutMissingParents() {
        Map<Integer, Integer> pidOf = Map.of(
            18, 0,
            10002, 18,
            100021, 10002
        );
        List<Integer> expanded = MenuAncestorSupport.expandMenuIdsWithAncestorsInMemory(
            List.of(100021), pidOf, Set.of());
        assertEquals(List.of(100021, 10002, 18), expanded);
    }

    @Test
    void expandSkipsDisabledNodesAndStopsClimb() {
        Map<Integer, Integer> pidOf = Map.of(
            18, 0,
            10002, 18,
            100021, 10002
        );
        List<Integer> expanded = MenuAncestorSupport.expandMenuIdsWithAncestorsInMemory(
            List.of(100021), pidOf, Set.of(10002));
        assertEquals(List.of(100021), expanded);
        assertTrue(!expanded.contains(18));
    }

    @Test
    void expandDedupsSharedAncestorsAcrossManyLeaves() {
        Map<Integer, Integer> pidOf = Map.of(
            18, 0,
            264, 18,
            10002, 18
        );
        List<Integer> expanded = MenuAncestorSupport.expandMenuIdsWithAncestorsInMemory(
            List.of(264, 10002), pidOf, Set.of());
        assertEquals(3, expanded.size());
        assertTrue(expanded.containsAll(List.of(264, 10002, 18)));
    }
}
