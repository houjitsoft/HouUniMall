package com.houjiang.plugin.artifact.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.plugin.artifact.util.HttpRanges.RangeSpec;
import org.junit.jupiter.api.Test;

/**
 * HTTP Range 解析单测：断点续传的正确性全靠它，解析错一位就会装上坏包。
 *
 * @author Amos
 * @since M2
 */
class HttpRangesTest {

    @Test
    void parsesExplicitRange() {
        RangeSpec spec = HttpRanges.parse("bytes=0-1023");
        assertEquals(0L, spec.resolveStart(4096));
        assertEquals(1023L, spec.resolveEnd(4096));
        assertTrue(spec.satisfiable(4096));
    }

    @Test
    void parsesOpenEndedRange() {
        RangeSpec spec = HttpRanges.parse("bytes=1024-");
        assertEquals(1024L, spec.resolveStart(4096));
        assertEquals(4095L, spec.resolveEnd(4096));
    }

    @Test
    void parsesSuffixRange() {
        RangeSpec spec = HttpRanges.parse("bytes=-512");
        assertEquals(3584L, spec.resolveStart(4096));
        assertEquals(4095L, spec.resolveEnd(4096));
    }

    @Test
    void suffixRangeClampsWhenLongerThanFile() {
        RangeSpec spec = HttpRanges.parse("bytes=-9999");
        assertEquals(0L, spec.resolveStart(100));
        assertEquals(99L, spec.resolveEnd(100));
    }

    @Test
    void endIsClampedToTotalSize() {
        RangeSpec spec = HttpRanges.parse("bytes=0-99999");
        assertEquals(4095L, spec.resolveEnd(4096));
    }

    @Test
    void missingHeaderYieldsNull() {
        assertNull(HttpRanges.parse(null));
        assertNull(HttpRanges.parse(""));
        assertNull(HttpRanges.parse("   "));
    }

    @Test
    void rejectsNonBytesUnit() {
        assertThrows(IllegalArgumentException.class, () -> HttpRanges.parse("items=0-10"));
    }

    @Test
    void rejectsMultiRange() {
        assertThrows(IllegalArgumentException.class, () -> HttpRanges.parse("bytes=0-99,200-299"));
    }

    @Test
    void rejectsMalformedRange() {
        assertThrows(IllegalArgumentException.class, () -> HttpRanges.parse("bytes=abc-def"));
        assertThrows(IllegalArgumentException.class, () -> HttpRanges.parse("bytes=10"));
        assertThrows(IllegalArgumentException.class, () -> HttpRanges.parse("bytes=-"));
        assertThrows(IllegalArgumentException.class, () -> HttpRanges.parse("bytes=9-5"));
    }

    @Test
    void unsatisfiableWhenStartBeyondTotal() {
        RangeSpec spec = HttpRanges.parse("bytes=5000-6000");
        assertFalse(spec.satisfiable(4096), "起点超出制品长度必须判 416");
        assertFalse(spec.satisfiable(0));
    }
}
