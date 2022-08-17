const EMAIL_REGEXP =
    /^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$/;

let DENY_LIST = new Set();

async function loadDenyList(): Promise<Set<string>> {
    const denyList = await (await fetch('https://kickmail.pages.dev/denylist.txt')).text();
    const domains = denyList.split('\n');
    const domainSet = new Set(domains);
    for (const domain of domains) {
        domainSet.add(domain);
    }
    return domainSet;
}

async function refresh() {
    DENY_LIST = await loadDenyList();
}

/**
 * Checks if an email is disposable or not.
 * @param email The email to check. It is assumed to be valid. You can check email validity with {#isValid()}.
 * @returns True if the email is disposable, false otherwise.
 */
function isDisposable(email: string): boolean {
    return !DENY_LIST.has(email.split('@')[1]);
}

/**
 * Checks if an email is valid or not.
 * @param email The email to check.
 * @returns True if the email is valid, false otherwise.
 */
function isValid(email: string): boolean {
    return EMAIL_REGEXP.test(email);
}

export { isDisposable, isValid, refresh };
