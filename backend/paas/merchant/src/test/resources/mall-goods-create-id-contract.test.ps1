$ErrorActionPreference = 'Stop'

$controllerPath = Join-Path $PSScriptRoot '..\..\main\java\com\houjiang\merchant\controller\mall\MallGoodsController.java'
$servicePath = Join-Path $PSScriptRoot '..\..\main\java\com\houjiang\merchant\service\mall\IMallGoodsService.java'
$implementationPath = Join-Path $PSScriptRoot '..\..\main\java\com\houjiang\merchant\service\impl\mall\MallGoodsServiceImpl.java'

$controller = Get-Content -LiteralPath $controllerPath -Raw -Encoding UTF8
$service = Get-Content -LiteralPath $servicePath -Raw -Encoding UTF8
$implementation = Get-Content -LiteralPath $implementationPath -Raw -Encoding UTF8

if ($controller -notmatch 'AjaxResult<Integer>\s+add') {
    throw 'mall goods add endpoint must return the created goods id'
}
if ($service -notmatch 'Integer\s+add\(MallGoodsCreateValidate') {
    throw 'mall goods service interface must return the created goods id'
}
if ($implementation -notmatch 'return\s+model\.getId\(\)') {
    throw 'mall goods add implementation must return model.getId()'
}

Write-Output 'mall goods create id contract: PASS'
