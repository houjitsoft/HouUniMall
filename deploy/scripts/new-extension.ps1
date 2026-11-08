#requires -Version 5.1
<#
.SYNOPSIS
    生成一个二开模块，或往已有模块追加一个扩展点实现。

.DESCRIPTION
    按《二开开发手册》的约定，用 deploy/scripts/templates/extension 下的模板生成：
      Custom* 实现、Default* 契约对照实现、契约测试抽象基类、Default/Custom 两个测试用例、
      自动配置 Bean；模块不存在时一并生成 pom.xml、交付描述与 AutoConfiguration.imports，
      并登记到 backend/extensions/pom.xml。

    生成后必须补齐两处 TODO：
      1) 契约测试抽象基类里按扩展点语义补不变量断言（before* 不篡改入参 / after* 结果不越界）；
      2) Custom* 实现里的业务规则。
    然后跑：
      mvn -f backend/extensions/pom.xml -DforkCount=0 clean test
      powershell -File deploy/scripts/verify-extensions.ps1

.PARAMETER Module
    业务域模块名，目录为 custom-{Module}（如 goods / order / payment）。

.PARAMETER Point
    扩展点名，不含 Extension 后缀（如 StockLock / PriceCalc）。

.PARAMETER Package
    扩展点接口所在包，如 com.houjiang.plugin.api.extension.goods。

.PARAMETER Site
    可选站点/客户标识，生成 custom-{Module}-{Site}，用于多客户交付。

.PARAMETER Mode
    APPEND（默认，追加钩子）或 REPLACE（接管官方实现，仅 owner=custom 可用）。

.PARAMETER Description
    二开实现的一句话描述，写入 @HouExtension.description。

.EXAMPLE
    powershell -File deploy/scripts/new-extension.ps1 -Module order -Point CartCalc -Package com.houjiang.plugin.api.extension.order
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$Module,
    [Parameter(Mandatory)][string]$Point,
    [Parameter(Mandatory)][string]$Package,
    [string]$Site,
    [ValidateSet('APPEND', 'REPLACE')][string]$Mode = 'APPEND',
    [string]$Description
)

$ErrorActionPreference = 'Stop'

$repoRoot    = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$extRoot     = Join-Path $repoRoot 'backend/extensions'
$tplDir      = Join-Path $PSScriptRoot 'templates/extension'
$moduleName  = if ($Site) { 'custom-' + $Module + '-' + $Site } else { 'custom-' + $Module }
$moduleDir   = Join-Path $extRoot $moduleName
$extPackage  = 'com.houjiang.ext.' + $Module
$pkgDir      = 'com/houjiang/ext/' + $Module
$enc         = New-Object System.Text.UTF8Encoding($false)

if ([string]::IsNullOrWhiteSpace($Description)) { $Description = '二开：' + $Point + ' 定制' }
if (-not (Test-Path -LiteralPath $tplDir)) { throw '未找到模板目录：' + $tplDir }
if (-not (Test-Path -LiteralPath $extRoot)) { throw '未找到二开根目录：' + $extRoot }

$moduleTitle = (Get-Culture).TextInfo.ToTitleCase($Module)
$configName  = 'Custom' + $moduleTitle + 'ExtensionsAutoConfiguration'
$modeAnno    = if ($Mode -eq 'REPLACE') { "`r`n        mode = ExtensionMode.REPLACE," } else { '' }
$modeImport  = if ($Mode -eq 'REPLACE') { "`r`nimport com.houjiang.plugin.sdk.extension.ExtensionMode;" } else { '' }

$isNewModule = -not (Test-Path -LiteralPath $moduleDir)
$action = if ($isNewModule) { '新建' } else { '追加' }
Write-Host ('模块：' + $moduleName + ' [' + $action + ']   扩展点：' + $Package + '.' + $Point + 'Extension   模式：' + $Mode)

function Write-TextFile {
    param([string]$Path, [string]$Content)
    $dir = Split-Path -Parent $Path
    if (-not (Test-Path -LiteralPath $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    [IO.File]::WriteAllText($Path, $Content, $enc)
    $rel = $Path
    if ($rel.StartsWith($repoRoot)) { $rel = $rel.Substring($repoRoot.Length).TrimStart('\', '/') }
    Write-Host ('  + ' + $rel)
}

function Expand-Template {
    param([string]$Name)
    $path = Join-Path $tplDir $Name
    if (-not (Test-Path -LiteralPath $path)) { throw '缺少模板：' + $path }
    $text = [IO.File]::ReadAllText($path)
    $text = $text.Replace('{EXTPACKAGE}', $extPackage)
    $text = $text.Replace('{PACKAGE}', $Package)
    $text = $text.Replace('{POINT}', $Point)
    $text = $text.Replace('{MODULE}', $Module)
    $text = $text.Replace('{CODE}', $moduleName)
    $text = $text.Replace('{CONFIGNAME}', $configName)
    $text = $text.Replace('{DESCRIPTION}', $Description)
    $text = $text.Replace('{MODE}', $modeAnno)
    $text = $text.Replace('{MODEIMPORT}', $modeImport)
    return $text
}

$mainJava = Join-Path $moduleDir ('src/main/java/' + $pkgDir)
$testJava = Join-Path $moduleDir ('src/test/java/' + $pkgDir)

Write-TextFile (Join-Path $mainJava ('Custom' + $Point + 'Extension.java')) (Expand-Template 'CustomExtension.java.tpl')
Write-TextFile (Join-Path $mainJava ('Default' + $Point + 'Extension.java')) (Expand-Template 'DefaultExtension.java.tpl')
Write-TextFile (Join-Path $testJava ($Point + 'ExtensionContractTest.java')) (Expand-Template 'ContractTest.java.tpl')
Write-TextFile (Join-Path $testJava ('Default' + $Point + 'ExtensionTest.java')) (Expand-Template 'DefaultTest.java.tpl')
Write-TextFile (Join-Path $testJava ('Custom' + $Point + 'ExtensionTest.java')) (Expand-Template 'CustomTest.java.tpl')

if ($isNewModule) {
    Write-TextFile (Join-Path $mainJava ($configName + '.java')) (Expand-Template 'AutoConfiguration.java.tpl')
    Write-TextFile (Join-Path $moduleDir 'src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports') (Expand-Template 'AutoConfiguration.imports.tpl')
    Write-TextFile (Join-Path $moduleDir 'src/main/resources/META-INF/houcloud-extension.properties') (Expand-Template 'houcloud-extension.properties.tpl')
    Write-TextFile (Join-Path $moduleDir 'pom.xml') (Expand-Template 'pom.xml.tpl')
} else {
    $configPath = Join-Path $mainJava ($configName + '.java')
    if (-not (Test-Path -LiteralPath $configPath)) {
        throw '已存在模块但找不到自动配置类，请手工确认模块结构：' + $configPath
    }
    $configText = [IO.File]::ReadAllText($configPath)
    $beanMethod = 'custom' + $Point + 'Extension()'
    if ($configText.Contains($beanMethod)) {
        Write-Host ('  = 自动配置已存在 ' + $beanMethod + '，跳过')
    } else {
        $beanLines = @(
            ''
            '    @Bean'
            ('    public Custom' + $Point + 'Extension custom' + $Point + 'Extension() {')
            ('        return new Custom' + $Point + 'Extension();')
            '    }'
            '}'
        )
        $bean = ($beanLines -join "`r`n") + "`r`n"
        $idx = $configText.LastIndexOf('}')
        $configText = $configText.Substring(0, $idx).TrimEnd() + "`r`n" + $bean
        [IO.File]::WriteAllText($configPath, $configText, $enc)
        Write-Host ('  ~ 已向 ' + $configName + ' 追加 Bean')
    }
}

if ($isNewModule) {
    $agg = Join-Path $extRoot 'pom.xml'
    $aggText = [IO.File]::ReadAllText($agg)
    $needle = '<module>' + $moduleName + '</module>'
    if (-not $aggText.Contains($needle)) {
        $anchor = '        <module>custom-' + $Module + '</module>'
        $line = '        ' + $needle
        if ($aggText.Contains($anchor)) {
            $aggText = $aggText.Replace($anchor, $anchor + "`r`n" + $line)
        } else {
            $aggText = $aggText.Replace('    </modules>', $line + "`r`n    </modules>")
        }
        [IO.File]::WriteAllText($agg, $aggText, $enc)
        Write-Host '  ~ 已登记到 backend/extensions/pom.xml'
    }
}

Write-Host ''
Write-Host '生成完成。下一步：'
Write-Host '  1) 补齐生成的 TODO（契约断言 + Custom* 业务规则）'
Write-Host '  2) mvn -f backend/extensions/pom.xml -DforkCount=0 clean test'
Write-Host '  3) powershell -File deploy/scripts/verify-extensions.ps1'
