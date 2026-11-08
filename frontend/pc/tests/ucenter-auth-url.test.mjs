import test from 'node:test'
import assert from 'node:assert/strict'

import { ucenterAuthUrl, ucenterAuthUrlFromSearch } from '../app/utils/ucenterAuthUrl.ts'

test('maps legacy consumer authentication routes to ucenter', () => {
    assert.equal(ucenterAuthUrl('login'), '/ucenter/#/login')
    assert.equal(ucenterAuthUrl('register'), '/ucenter/#/register')
    assert.equal(ucenterAuthUrl('forgot-password'), '/ucenter/#/forgot-password')
})

test('preserves one encoded same-origin return target', () => {
    assert.equal(
        ucenterAuthUrl('login', '/product/7?sku=8'),
        '/ucenter/#/login?redirect=%2Fproduct%2F7%3Fsku%3D8'
    )
})

test('reads the return target from a static-page browser query', () => {
    assert.equal(
        ucenterAuthUrlFromSearch('login', '?redirect=%2Fproduct%2F7'),
        '/ucenter/#/login?redirect=%2Fproduct%2F7'
    )
    assert.equal(
        ucenterAuthUrlFromSearch('login', '?redirect=https%3A%2F%2Fevil.example%2Fsteal'),
        '/ucenter/#/login'
    )
})

test('rejects external, protocol-relative, and authentication-loop targets', () => {
    assert.equal(ucenterAuthUrl('login', 'https://evil.example/steal'), '/ucenter/#/login')
    assert.equal(ucenterAuthUrl('login', '//evil.example/steal'), '/ucenter/#/login')
    assert.equal(ucenterAuthUrl('login', '/\\evil.example/steal'), '/ucenter/#/login')
    assert.equal(ucenterAuthUrl('login', '%2F%5Cevil.example%2Fsteal'), '/ucenter/#/login')
    assert.equal(ucenterAuthUrl('login', '/login/register'), '/ucenter/#/login')
    assert.equal(ucenterAuthUrl('login', '/ucenter/#/forgot-password'), '/ucenter/#/login')
})

test('rejects unknown authentication kinds', () => {
    assert.throws(() => ucenterAuthUrl('admin'), /Unsupported consumer auth route/)
})
