# kickmail

A TypeScript and Java library to filter out disposable email addresses based on domain name.

Deny list is aggregated from multiple sources every day and hosted by Cloudflare:

  - https://kickmail.pages.dev/denylist.txt

The following sources are currently in use:

  - https://github.com/disposable-email-domains/disposable-email-domains
  - https://github.com/7c/fakefilter
  - https://github.com/FGRibreau/mailchecker

Contributions are welcome!