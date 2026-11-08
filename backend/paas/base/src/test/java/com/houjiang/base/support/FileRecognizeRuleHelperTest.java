package com.houjiang.base.support;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRecognizeRuleHelperTest {

    @Test
    void doesNotTreatStorageTimestampAsAmountOrTitle() {
        Map<String, Object> result = FileRecognizeRuleHelper.recognize(
                "1784095933005_94987942937900.jpg", new byte[]{1, 2, 3}, null);

        String summary = String.valueOf(result.get("summary"));
        assertFalse(summary.contains("1784095933005"));
        assertFalse(summary.contains("金额:"));
    }

    @Test
    void keepsReasonableAmountInFilename() {
        Map<String, Object> result = FileRecognizeRuleHelper.recognize(
                "技术服务费22000.00元.jpg", new byte[]{1, 2, 3}, null);

        String summary = String.valueOf(result.get("summary"));
        assertTrue(summary.contains("金额: 22000.00"));
    }
}
