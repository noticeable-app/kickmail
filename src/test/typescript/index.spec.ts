import {describe, expect, test} from '@jest/globals';
import {DENY_LIST, isDisposable, isDomainDenied, isValid, parseDenyList, refresh} from '../../main/typescript';

describe('deny list format', () => {
    const lists = parseDenyList('# comment\nRehearsalk.com\n=ddns.net\ncorp-farm.com\n!good.corp-farm.com\n\n');

    test('an entry denies itself and every subdomain', () => {
        expect(isDomainDenied(lists, 'rehearsalk.com')).toBe(true);
        expect(isDomainDenied(lists, 'aas.rehearsalk.com')).toBe(true);
        expect(isDomainDenied(lists, 'a.b.rehearsalk.com')).toBe(true);
    });

    test('a domain that only ends like an entry is not denied', () => {
        expect(isDomainDenied(lists, 'notrehearsalk.com')).toBe(false);
    });

    test('an exact only entry does not deny its subdomains', () => {
        expect(isDomainDenied(lists, 'ddns.net')).toBe(true);
        expect(isDomainDenied(lists, 'someone.ddns.net')).toBe(false);
    });

    test('an exception under a denied parent stays allowed with its subdomains', () => {
        expect(isDomainDenied(lists, 'bad.corp-farm.com')).toBe(true);
        expect(isDomainDenied(lists, 'good.corp-farm.com')).toBe(false);
        expect(isDomainDenied(lists, 'mail.good.corp-farm.com')).toBe(false);
    });

    test('bare top-level domains are never looked up', () => {
        expect(isDomainDenied(parseDenyList('com\n'), 'acme.com')).toBe(false);
    });
});

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

    test('disposable email on a subdomain of a denied domain is detected', async () => {
        expect(isDisposable('qgm6ca@random123.sharklasers.com')).toBe(true);
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