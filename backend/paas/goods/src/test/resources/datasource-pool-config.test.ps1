$ErrorActionPreference = 'Stop'

$configPath = Join-Path $PSScriptRoot '..\..\main\resources\bootstrap.yml'
$content = Get-Content -LiteralPath $configPath -Raw -Encoding UTF8

$required = @(
    'max-lifetime: 120000',
    'keepalive-time: 30000',
    'validation-timeout: 3000',
    'connection-timeout: 10000'
)

foreach ($setting in $required) {
    if (-not $content.Contains($setting)) {
        throw "missing goods datasource pool setting: $setting"
    }
}

Write-Output 'goods datasource pool config: PASS'
