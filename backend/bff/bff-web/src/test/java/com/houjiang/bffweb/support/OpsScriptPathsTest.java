package com.houjiang.bffweb.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OpsScriptPathsTest {

    @Test
    void mapsWindowsDrivePathToLinuxRoot() {
        assertEquals(
                "/www/wwwroot/houcloud/jar/service-manage.ps1",
                OpsScriptPaths.mapWindowsProjectPathToLinux("D:/wwwroot/houcloud/jar/service-manage.ps1"));
        assertEquals(
                "/www/wwwroot/houcloud/jar/service-manage.ps1",
                OpsScriptPaths.mapWindowsProjectPathToLinux("D:\\wwwroot\\houcloud\\jar\\service-manage.ps1"));
    }

    @Test
    void mapsWindowsConfiguredPathToLinuxProjectRoot() {
        assumeTrue(!OpsScriptPaths.isWindows());
        String resolved = OpsScriptPaths.resolve(
                "D:/wwwroot/houcloud/jar/service-manage.ps1",
                "jar/service-manage.ps1");
        assertEquals("/www/wwwroot/houcloud/jar/service-manage.ps1", resolved);
    }

    @Test
    void usesLinuxFallbackWhenConfiguredPathEmptyOnLinux() {
        assumeTrue(!OpsScriptPaths.isWindows());
        assertEquals(
                "/www/wwwroot/houcloud/jar/restart-all.ps1",
                OpsScriptPaths.resolve("", "jar/restart-all.ps1"));
    }

    @Test
    void keepsLinuxAbsolutePathAsIs() {
        assumeTrue(!OpsScriptPaths.isWindows());
        assertEquals(
                "/www/wwwroot/houcloud/jar/service-manage.ps1",
                OpsScriptPaths.resolve(
                        "/www/wwwroot/houcloud/jar/service-manage.ps1",
                        "jar/service-manage.ps1"));
    }
}
