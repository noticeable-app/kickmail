import {describe, expect, test} from '@jest/globals';
import {DENY_LIST, isDisposable, isValid, refresh} from '../../main/typescript';

describe('kickmail', () => {
    test('DENY_LIST is empty if not initialized', async () => {
        expect(DENY_LIST.size).toBe(0);
    });

    test('DENY_LIST is not empty after initialization', async () => {
        await refresh();
        expect(DENY_LIST.size).toBeGreaterThan(0);
    });

    test('DENY_LIST contains disposable email domain', async () => {
        expect(DENY_LIST.has('sharklasers.com')).toBe(true);
    });

    test('disposable email is detected', async () => {
        expect(isDisposable('qgm6ca+3avb6y3wziqbs@sharklasers.com')).toBe(true);
    });

    test('non disposable email pass detection', async () => {
        expect(isDisposable('john.doe@acme.com')).toBe(false);
    });

    test('non disposable email pass detection', async () => {
        expect(isDisposable('estelle+test@gmail.com')).toBe(false);
    });

    test('valid email address pass verification', async () => {
        expect(isValid('john.doe@acme.com')).toBe(true);
    });

    test('invalid email address does not pass verification', async () => {
        expect(isValid('john.doe@@acme.com')).toBe(false);
    });
});