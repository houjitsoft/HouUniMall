import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'

const sourceUrl = new URL('../src/api/set/switch.ts', import.meta.url)

test('public switch query never forwards merchant authorization', async () => {
    const source = await readFile(sourceUrl, 'utf8')
    assert.match(
        source,
        /querySysSwitch[\s\S]*?urlPrefix:\s*['"]web['"][\s\S]*?withToken:\s*false/
    )
})
