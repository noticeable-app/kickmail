const EMAIL_REGEXP =
    /^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$/;

/**
 * The published deny list, parsed. A line of the list is one of:
 *   - `example.com`: denied, and so is every subdomain of it (`mail.example.com`, `a.b.example.com`);
 *   - `=example.com`: denied as written only (public suffixes such as `ddns.net`, whose subdomains
 *     belong to unrelated people);
 *   - `!mail.example.com`: never denied, nor its subdomains, even under a denied parent.
 * Walking up from the domain of the address, the first exception or deny entry met wins.
 */
export interface DomainLists {
    denied: Set<string>;
    exactOnly: Set<string>;
    allowed: Set<string>;
}

/** The plain deny entries (each also denies its subdomains). */
export let DENY_LIST = new Set<string>();

let LISTS: DomainLists = {denied: DENY_LIST, exactOnly: new Set(), allowed: new Set()};

function parseDenyList(text: string): DomainLists {
    const lists: DomainLists = {denied: new Set(), exactOnly: new Set(), allowed: new Set()};
    for (const line of text.split('\n')) {
        const entry = line.trim().toLowerCase();
        if (entry === '' || entry.startsWith('#')) {
            continue;
        }
        if (entry.startsWith('!')) {
            lists.allowed.add(entry.slice(1));
        } else if (entry.startsWith('=')) {
            lists.exactOnly.add(entry.slice(1));
        } else {
            lists.denied.add(entry);
        }
    }
    return lists;
}

/** Whether a lower-cased mailbox domain is denied by the lists. A bare top-level domain is never looked up. */
function isDomainDenied(lists: DomainLists, domain: string): boolean {
    let current = domain;
    let asWritten = true;
    for (;;) {
        if (lists.allowed.has(current)) {
            return false;
        }
        if (lists.denied.has(current) || (asWritten && lists.exactOnly.has(current))) {
            return true;
        }
        const dot = current.indexOf('.');
        if (dot < 0) {
            return false;
        }
        current = current.slice(dot + 1);
        if (!current.includes('.')) {
            return false;
        }
        asWritten = false;
    }
}

async function loadDenyList(): Promise<DomainLists> {
    return parseDenyList(await (await fetch('https://kickmail.pages.dev/denylist.txt')).text());
}

async function init() {
    return refresh();
}

async function refresh() {
    LISTS = await loadDenyList();
    DENY_LIST = LISTS.denied;
    return DENY_LIST;
}

/**
 * Checks if an email is disposable or not: its domain, or a parent domain of it, is on the deny list.
 * @param email The email to check. It is assumed to be valid. You can check email validity with {#isValid()}.
 * @returns True if the email is disposable, false otherwise.
 */
function isDisposable(email: string): boolean {
    const at = email.lastIndexOf('@');
    if (at < 0 || at === email.length - 1) {
        return false;
    }
    // Mailbox domains are case-insensitive and the deny list is lower-case.
    return isDomainDenied(LISTS, email.slice(at + 1).toLowerCase());
}

/**
 * Checks if an email is valid or not.
 * @param email The email to check.
 * @returns True if the email is valid, false otherwise.
 */
function isValid(email: string): boolean {
    return EMAIL_REGEXP.test(email);
}

export { init, isDisposable, isDomainDenied, isValid, parseDenyList, refresh };
