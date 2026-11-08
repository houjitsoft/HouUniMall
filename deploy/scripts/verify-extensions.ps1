[CmdletBinding()]
param(
    [string]$Root
)

if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
}

$extensionRoot = Join-Path $Root 'backend/extensions'
$errors = [System.Collections.Generic.List[string]]::new()
$customExtensionCount = 0

if (-not (Test-Path -LiteralPath $extensionRoot -PathType Container)) {
    $errors.Add("Missing extension root: $extensionRoot")
} else {
    # Every build-time customization is a site-scoped, versioned delivery unit.
    # The descriptor is copied into the JAR and gives release tooling one
    # stable source of provenance without importing host implementation code.
    $moduleDirs = Get-ChildItem -LiteralPath $extensionRoot -Directory |
        Where-Object { $_.Name -like 'custom-*' }
    foreach ($module in $moduleDirs) {
        $descriptor = Join-Path $module.FullName 'src/main/resources/META-INF/houcloud-extension.properties'
        if (-not (Test-Path -LiteralPath $descriptor -PathType Leaf)) {
            $errors.Add("Missing build-time extension descriptor: $descriptor")
            continue
        }
        $properties = @{}
        foreach ($line in (Get-Content -LiteralPath $descriptor)) {
            if ($line -match '^\s*([A-Za-z][A-Za-z0-9]*)\s*=\s*(.*?)\s*$') {
                $properties[$matches[1]] = $matches[2]
            }
        }
        $expectedCode = $module.Name
        if ($properties['code'] -ne $expectedCode) {
            $errors.Add("Extension descriptor code must match module directory ($expectedCode): $descriptor")
        }
        if ($properties['version'] -notmatch '^\d+\.\d+\.\d+$') {
            $errors.Add("Extension descriptor version must be semantic X.Y.Z: $descriptor")
        }
        if ($properties['hostVersion'] -notmatch '^(?:\d+\.){1,2}(?:\d+|x)$') {
            $errors.Add("Extension descriptor hostVersion must be a compatible release (for example 1.x): $descriptor")
        }
        if ($properties['scope'] -ne 'site') {
            $errors.Add("Build-time extensions must declare scope=site: $descriptor")
        }
        if ($properties['contracts'] -notmatch '(^|,)plugin-api(,|$)') {
            $errors.Add("Extension descriptor must declare plugin-api contract: $descriptor")
        }

        $imports = Join-Path $module.FullName 'src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports'
        if (-not (Test-Path -LiteralPath $imports -PathType Leaf)) {
            $errors.Add("Build-time extension must expose explicit AutoConfiguration.imports: $imports")
        } elseif (-not (Get-Content -LiteralPath $imports | Where-Object { $_.Trim() -and -not $_.Trim().StartsWith('#') })) {
            $errors.Add("AutoConfiguration.imports must list at least one configuration class: $imports")
        }
    }

    $sourceFiles = Get-ChildItem -LiteralPath $extensionRoot -Recurse -File -Filter '*.java' |
        Where-Object { $_.FullName -match '\\src\\main\\java\\' }
    foreach ($file in $sourceFiles) {
        $content = Get-Content -LiteralPath $file.FullName -Raw
        $customExtensionCount += ([regex]::Matches($content, '(?m)^\s*@HouExtension\b')).Count
        if ($content -match '(?m)^\s*@Primary\b') {
            $errors.Add("@Primary is not allowed in build-time extensions: $($file.FullName)")
        }
        if ($content -match '(?m)^\s*import\s+com\.houjiang\.(auth|base|goods|job|merchant|order|payment|permission|supplier|thirdparty|userms|aicode|mall)(?:\.|;)') {
            $errors.Add("Official implementation package imported by extension: $($file.FullName)")
        }
        if ($content -match '(?m)^\s*import\s+com\.houjiang\.(?:plugin\.engine|plugin\.runtime)\b') {
            $errors.Add("Runtime implementation package imported by extension: $($file.FullName)")
        }
        if ($content -match '(?m)^\s*@(?:ComponentScan|SpringBootApplication)\b') {
            $errors.Add("Build-time extensions must not scan or own the host application: $($file.FullName)")
        }
        if ($content -match '(?m)\breturn\s+new\s+Default[A-Za-z0-9_]+\s*\(') {
            $errors.Add("Auto-configuration must not register Default* reference implementations: $($file.FullName)")
        }

        $matches = [regex]::Matches($content, '(?ms)^\s*@HouExtension\s*\((.*?)\)')
        foreach ($match in $matches) {
            $annotation = $match.Groups[1].Value
            $ownerCustom = $annotation -match 'owner\s*=\s*"custom"'
            $ownerThirdparty = $annotation -match 'owner\s*=\s*"thirdparty"'
            $modeReplace = $annotation -match 'mode\s*=\s*ExtensionMode\.REPLACE'
            if ($ownerCustom -and
                $annotation -notmatch 'code\s*=\s*"[a-z0-9][a-z0-9._-]*"') {
                $errors.Add("Custom extension must declare a stable code: $($file.FullName)")
            }
            if ($ownerThirdparty) {
                $errors.Add("Build-time extensions cannot use owner=thirdparty; ship a signed runtime plugin instead: $($file.FullName)")
            }
            if ($modeReplace -and -not $ownerCustom) {
                $errors.Add("ExtensionMode.REPLACE is reserved for owner=custom: $($file.FullName)")
            }
        }
    }

    # 五件套约定（设计 §1.2）：配置禁热更新（禁 @RefreshScope）；出站禁 Feign（须放 client/）
    foreach ($file in $sourceFiles) {
        $content = Get-Content -LiteralPath $file.FullName -Raw
        if ($content -match '(?m)^\s*@RefreshScope\b') {
            $errors.Add("@RefreshScope is forbidden in build-time extensions (configuration is not hot-reloadable): $($file.FullName)")
        }
        if ($content -match '(?m)^\s*@FeignClient\b') {
            $errors.Add("@FeignClient is forbidden in build-time extensions (put outbound clients under client/): $($file.FullName)")
        }
    }

    # 五件套约定：plugin-api 是契约面，禁出站客户端（@FeignClient）
    $pluginApiRoot = Join-Path $root 'backend/contracts/plugin-api'
    if (Test-Path -LiteralPath $pluginApiRoot -PathType Container) {
        $apiSources = Get-ChildItem -LiteralPath $pluginApiRoot -Recurse -File -Filter '*.java' |
            Where-Object { $_.FullName -match '\\src\\main\\java\\' }
        foreach ($file in $apiSources) {
            $content = Get-Content -LiteralPath $file.FullName -Raw
            if ($content -match '(?m)^\s*@FeignClient\b') {
                $errors.Add("@FeignClient is forbidden in plugin-api (contract surface must stay outbound-free): $($file.FullName)")
            }
        }
    }

    # 五件套约定：二开不注册微服务、不占端口（资源中不得声明 server.port / server:）
    $resourceFiles = Get-ChildItem -LiteralPath $extensionRoot -Recurse -File |
        Where-Object { $_.FullName -match '\\src\\main\\resources\\' -and (
            $_.Name.EndsWith('.yml') -or $_.Name.EndsWith('.yaml') -or $_.Name.EndsWith('.properties')) }
    foreach ($file in $resourceFiles) {
        $content = Get-Content -LiteralPath $file.FullName -Raw
        if ($content -match '(?m)^\s*server\.port\s*[:=]' -or $content -match '(?m)^\s*server\s*:\s*$') {
            $errors.Add("Build-time extensions must not declare a standalone server port: $($file.FullName)")
        }
    }

    $pomFiles = Get-ChildItem -LiteralPath $extensionRoot -Recurse -File -Filter 'pom.xml'
    foreach ($pom in $pomFiles) {
        $content = Get-Content -LiteralPath $pom.FullName -Raw
        if ($content -match '(?s)<artifactId>\s*(auth|base|goods|job|merchant|order|payment|permission|supplier|thirdparty|userms|aicode|mall|core-api|goods-api|order-api|payment-api|permission-api)\s*</artifactId>') {
            $errors.Add("Official implementation dependency is not allowed: $($pom.FullName)")
        }
    }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Output "Build-time extension architecture checks passed ($customExtensionCount annotated source file(s))."
