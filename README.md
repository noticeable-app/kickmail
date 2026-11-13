# kickmail

A TypeScript and Java library to filter out disposable email addresses based on domain name.

Deny list is aggregated from multiple sources every 6 hours and hosted by Cloudflare:

  - https://kickmail.pages.dev/denylist.txt

## How the deny list is built

The published list is the union of the upstream sources below, corrected by two
files kept in this repository under `lists/`:

  - `lists/denylist.txt`: domains we block that upstream does not know yet
    (for example alias domains of a temporary mailbox provider).
  - `lists/allowlist.txt`: domains we never block, even when an upstream source
    lists them (false positives such as a customer's real mail domain).

Format: one domain per line, lower-case, `#` starts a comment, blank lines ignored. A plain
domain also covers its subdomains; `lists/denylist.txt` accepts `=domain` for a domain to block
without its subdomains (see [Subdomains and the `=` / `!` exceptions](#subdomains-and-the----exceptions)).

### Priorities

Sources are applied in this order, highest priority first:

  1. `lists/allowlist.txt`: a domain listed here is always removed from the published list.
  2. `lists/denylist.txt`: a domain listed here is always added.
  3. Upstream sources: everything else comes from the three community lists.

So a domain in both our allow and deny lists is allowed, and a domain in our allow
list is never re-blocked when an upstream source picks it up later.

### Subdomains and the `=` / `!` exceptions

A plain entry blocks the domain **and every subdomain of it**: `example.com` blocks
`x@example.com`, `x@mail.example.com` and `x@a.b.example.com`. Account farms sign up with random
subdomains of a domain they own (`x@aas.farm.com`, `x@zz.farm.com`...), so list the parent
domain only, never its subdomains one by one.

Two kinds of lines change that. Each line of the published list is one of:

| Line | Meaning | Example | Blocks | Does not block |
|---|---|---|---|---|
| `domain` | the domain and all its subdomains | `farm.com` | `x@farm.com`, `x@aas.farm.com` | |
| `=domain` | the domain as written only, not its subdomains | `=ddns.net` | `x@ddns.net` | `x@home.ddns.net` |
| `!domain` | never blocked, nor its subdomains, even under a blocked parent | `!good.farm.com` | | `x@good.farm.com`, `x@mail.good.farm.com` |

How a client decides: it walks up from the domain of the address (`a.b.farm.com`, then
`b.farm.com`, then `farm.com`) and the first line met decides: a `!` line allows, a plain line
blocks, a `=` line blocks only when it is the address's own domain. A bare top-level domain
(`com`) is never looked up.

Where the exceptions come from:

  - `=domain` is written by the build for every entry that is a public suffix
    (https://publicsuffix.org, ICANN and private sections, such as `ddns.net`, `it.com`,
    `cloudns.cc`): their subdomains are registered by unrelated people, so blocking them would
    block whole services. You can also write `=domain` yourself in `lists/denylist.txt` for a
    domain that is not on the Public Suffix List but whose subdomains belong to other people.
  - `!domain` is written by the build for every entry of `lists/allowlist.txt` that has a
    blocked parent. In `lists/allowlist.txt` you write the plain domain (`good.farm.com`),
    never the `!`.

Compatibility: subdomain matching and the `=` / `!` lines need Java 2.2.0 or npm 1.3.0 and
later. Older clients compare the domain of the address to each line exactly: plain entries still
block their own domain, and they read `=` and `!` lines as domains that no address has (so they
no longer block the few public suffix entries).

### Workflow

  1. Edit `lists/denylist.txt` or `lists/allowlist.txt` and push to `main`.
  2. The `Build and Deploy Lists` workflow rebuilds and redeploys
     https://kickmail.pages.dev/denylist.txt within minutes. The same workflow
     also runs every 6 hours to pick up upstream changes.
  3. Clients see the change on their next `refresh()` (or restart). No library
     release is needed.

### Reporting an entry to add or remove

Open an issue with one of the templates, they ask for the domain(s) and the evidence needed
to decide:

  - **Block a domain**: a disposable or abusive domain missing from the list.
    Goes to `lists/denylist.txt` once accepted.
  - **Unblock a domain**: a legitimate domain wrongly blocked (false positive).
    Goes to `lists/allowlist.txt` once accepted.

Check https://kickmail.pages.dev/denylist.txt first to see whether the domain is currently
blocked. Maintainers can also skip the issue and edit the files directly in a pull request.

To rebuild locally:

```bash
./gradlew shadowJar
java -cp build/libs/kickmail-all.jar io.noticeable.kickmail.AggregateLists lists dist/denylist.txt
```

# Usage Examples

## TypeScript

This example shows how to block registrations that make use of disposable email addresses with Firebase Authentication:

```typescript
import * as functions from 'firebase-functions';

import {init, isDisposable, isValid} from '@noticeable-app/kickmail';

const kickmailInitPromise = init();

module.exports =
    functions.runWith({memory: '512MB', timeoutSeconds: 10})
        .auth.user().beforeCreate(async (user, context) => {

            await kickmailInitPromise;

            if (!isValid(user.email)) {
                throw new functions.auth.HttpsError('invalid-argument', 'Invalid email address.');
            }

            if (isDisposable(user.email)) {
                throw new functions.auth.HttpsError('invalid-argument', 'Disposable email addresses are not allowed.');
            }
        }
    );
```

## Java

```java
import io.noticeable.kickmail.KickMail;

final KickMail kickMail = new KickMail();

if (!kickMail.isValid(email)) {
    throw new InvalidArgumentException("Invalid email address: " + email);
}

if (kickMail.isDisposable(email)) {
    throw new InvalidArgumentException("Disposable email addresses are not allowed: " + email);
}

if (!kickMail.hasMxRecord(email)) {
   throw new InvalidArgumentException("Invalid email address (no MX record): " + email);
}
```

# Changelog

See [CHANGELOG.md](CHANGELOG.md). Add an entry under `Unreleased` with every change;
move it under a version heading when releasing.

# Acknowledgments

The following sources are currently in use:

  - https://github.com/disposable-email-domains/disposable-email-domains
  - https://github.com/7c/fakefilter
  - https://github.com/FGRibreau/mailchecker

Contributions are welcome!