import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'
import ts from 'typescript'

test('Nuxt production configuration has no TypeScript syntax diagnostics', async () => {
    const source = await readFile(new URL('../../nuxt.config.ts', import.meta.url), 'utf8')
    const result = ts.transpileModule(source, {
        compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
        reportDiagnostics: true
    })
    const syntaxErrors = (result.diagnostics || []).filter(item => item.category === ts.DiagnosticCategory.Error)

    assert.deepEqual(syntaxErrors.map(item => ts.flattenDiagnosticMessageText(item.messageText, '\n')), [])
})
