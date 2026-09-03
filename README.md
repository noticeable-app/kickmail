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

Format: one domain per line, lower-case, `#` starts a comment, blank lines ignored.

### Priorities

Sources are applied in this order, highest priority first:

  1. `lists/allowlist.txt`: a domain listed here is always removed from the published list.
  2. `lists/denylist.txt`: a domain listed here is always added.
  3. Upstream sources: everything else comes from the three community lists.

So a domain in both our allow and deny lists is allowed, and a domain in our allow
list is never re-blocked when an upstream source picks it up later. Matching is by
exact domain: adding `example.com` does not block `mail.example.com`, list both when needed.

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