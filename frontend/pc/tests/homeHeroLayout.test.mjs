import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { test } from 'node:test'

const source = readFileSync(new URL('../app/components/home/BannerBox.vue', import.meta.url), 'utf8')

test('首页首屏四个促销模块必须保持在同一网格行内', () => {
    const stageBottom = source.match(/<div class="stage-bottom">([\s\S]*?)<\/div>\s*<\/div>/)?.[1] || ''
    const moduleCount = (stageBottom.match(/<Banner(?:Promo|Live)Panel\b/g) || []).length

    assert.equal(moduleCount, 4)
    assert.match(source, /\.stage-bottom\s*\{[\s\S]*?grid-template-columns:\s*repeat\(4,/)
    assert.match(source, /class="subsidy-panel"[^>]*:cols="4"/)
})
