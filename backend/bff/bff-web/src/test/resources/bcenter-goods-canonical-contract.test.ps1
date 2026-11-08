$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..\..\..')).Path
$controller = Join-Path $repoRoot 'backend\bff\bff-web\src\main\java\com\houjiang\bffweb\controller\BcenterMallGoodsController.java'
$goodsService = Join-Path $repoRoot 'backend\paas\goods\src\main\java\com\houjiang\goods\mall\service\impl\MallGoodsServiceImpl.java'

if (-not (Test-Path -LiteralPath $controller)) {
    throw 'Bcenter canonical goods controller is missing'
}

$source = Get-Content -Raw -LiteralPath $controller
$serviceSource = Get-Content -Raw -LiteralPath $goodsService

$requiredSnippets = @(
    '@RequestMapping("/web/merchant")',
    '@GetMapping("/mall.Goods/lists")',
    '@GetMapping("/mall.Goods/detail")',
    '@PostMapping("/mall.Goods/add")',
    '@PostMapping("/mall.Goods/edit")',
    '@GetMapping("/mall.Goods/i18n/{commonId}")',
    '@PostMapping("/mall.Goods/i18n/{commonId}/save")',
    '@PostMapping("/mall.Goods/i18n/{commonId}/auto-translate")',
    'goodsServiceFeign.mallGoodsSet',
    'params.put("shopId", merchant.getShopId())',
    'assertOwnedByShop',
    'moveAlias(result, "category_ids", "categoryIds")',
    'result.put("imageList", list)',
    'result.put("skuList", normalized)',
    'item.put("sku_value_arr", jsonValue',
    'item.put("sons", toBcenterCategories(children))',
    'currentMerchant("mall.Goods/add:edit")',
    'assertOwnedByShop(commonId, merchant.getShopId())',
    'saveBody.put("commonId", commonId)'
)

foreach ($snippet in $requiredSnippets) {
    if (-not $source.Contains($snippet)) {
        throw "Missing canonical goods contract: $snippet"
    }
}

if (-not $serviceSource.Contains('.eq(MallGoods::getShopCategoryId, s.getShopCategoryId())')) {
    throw 'Canonical goods list does not filter merchant shop category'
}

Write-Output 'bcenter canonical goods contract: PASS'
