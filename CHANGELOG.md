# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Two libraries are released from this repository, each with its own version:

  - Java `io.noticeable:kickmail`, published to GitHub Packages (Maven).
  - npm `@noticeable-app/kickmail`, published to npmjs.com and GitHub Packages (npm).

Releases are tagged `java-<version>` and `npm-<version>`.

The published deny list at https://kickmail.pages.dev/denylist.txt is not versioned:
it is rebuilt every 6 hours and on every push to `main`. Changes to the repo-owned
entries in `lists/` are recorded under "Deny list".

## Java 2.2.0, npm 1.3.0 - 2026-11-13

### Added
- A deny list entry also blocks its subdomains: `example.com` blocks `x@mail.example.com`.
  Account farms sign up with random subdomains of a domain they own, which exact matching let
  through one by one.
- Published list format: `=domain` blocks a domain as written only (entries that are public
  suffixes, such as `ddns.net`, whose subdomains belong to unrelated people), `!domain` keeps an
  allow-list entry allowed under a blocked parent. Older clients read both as domains no address
  has and keep matching the plain entries exactly.
- Java `DomainList` (parsing and matching) and `PublicSuffixes` (used by the build); npm
  `parseDenyList` and `isDomainDenied`.

### Repository
- `AggregateLists` reads the Public Suffix List and writes the `=` and `!` lines;
  `lists/denylist.txt` accepts `=domain` for a domain to block without its subdomains.
- README explains subdomain matching and the `=` / `!` lines.

### Changed
- Java: dnsjava 3.7.1, Gradle 9.9.1.
- npm (development only): jest 30.5.2, ts-jest 29.4.15, prettier 3.10.1, typescript-eslint 8.72.2,
  ESLint 10.14.0 with `@eslint/js` 10.0.1 as direct dependencies; removed the unused
  `eslint-plugin-import` (it held ESLint at 9); `overrides` for glob 14.0.6 and minimatch 11.0.8.
  `npm audit` leaves only low findings from deepmerge (used by jest, no fixed version).
  TypeScript stays on 6.0.3: ts-jest and typescript-eslint do not accept TypeScript 7 yet.

## Java 2.1.1 - 2026-09-03

### Changed
- Upgraded jmail to 2.2.2 (validation fixes and faster address parsing), Gradle to 9.7.1,
  shadow plugin to 9.6.1.

### Deny list
- Added `lists/denylist.txt` and `lists/allowlist.txt`, merged on top of the upstream
  sources. The allow list has the highest priority, then our deny list, then upstream.
- Blocked `kya2.com` (alias of the bccto.cc temporary mailbox service).

### Repository
- `AggregateLists` accepts `[listsDir] [outputFile]` arguments and prints a summary.
- The deploy workflow also runs on push to `main` when `lists/`, Java sources or the
  build change, and builds with the Gradle wrapper.
- Issue templates to request blocking or unblocking a domain.
- README documents the lists, their priorities and the reporting workflow.

## Java 2.1.0 - 2026-08-28, npm 1.2.0 - 2026-08-30

### Fixed
- Disposable matching is case-insensitive: the domain part is lower-cased before lookup
  and the address is split on the last `@`.

### Changed
- Java: MX lookups use a resolver with a 3 second timeout; deny list downloads use
  10 second connect and read timeouts.
- Java: upgraded dnsjava, jmail, JUnit 6. npm: replaced the eslint/prettier setup with
  typescript-eslint flat config.

## Java 2.0.0 - 2025-11-08

### Changed
- **Breaking:** requires Java 25 (toolchain), previously Java 21.
- Upgraded jmail to 2.1.0, JUnit to 6.0.1, shadow plugin to 9.2.2, Gradle wrapper to 9.

## Java 1.0.0 - 2024-01-11, npm 1.1.0 - 2024-01-11

### Changed
- Java: requires Java 21, previously 17. Upgraded dnsjava, jmail 1.6.2, shadow plugin 8.
- npm: switched from yarn to npm scripts, upgraded to TypeScript 5 and cross-fetch 4.

## npm 1.0.2 - 2022-09-04
### Fixed
- Wrong condition in the disposable check.

## npm 1.0.1 - 2022-09-04
### Added
- `init()` is exported.

## npm 1.0.0 - 2022-09-04
### Added
- Tests for the TypeScript implementation.

## Java 0.2.0 / npm 0.2.0 - 2022-08-19
### Added
- Initial release: `isValid`, `isDisposable`, Java `hasMxRecord` and `shouldKick`,
  deny list aggregated from disposable-email-domains, fakefilter and mailchecker and
  deployed to Cloudflare Pages.
