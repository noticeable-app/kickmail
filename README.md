# kickmail

A TypeScript and Java library to filter out disposable email addresses based on domain name.

Deny list is aggregated from multiple sources every 6 hours and hosted by Cloudflare:

  - https://kickmail.pages.dev/denylist.txt

## Maintaining our own entries

Two files in `lists/` are merged on top of the upstream sources:

  - `lists/denylist.txt`: domains to block that upstream does not know yet.
  - `lists/allowlist.txt`: domains to never block, even when an upstream source lists them (false positives).

One domain per line, `#` starts a comment. The allow list wins over everything else.
Pushing a change to `main` rebuilds and redeploys the published list right away; clients
pick it up on their next `refresh()`.

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

# Acknowledgments

The following sources are currently in use:

  - https://github.com/disposable-email-domains/disposable-email-domains
  - https://github.com/7c/fakefilter
  - https://github.com/FGRibreau/mailchecker

Contributions are welcome!