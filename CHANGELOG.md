# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Two libraries are released from this repository, each with its own version:

  - Java `io.noticeable:kickmail`, published to GitHub Packages (Maven).
  - npm `@noticeable-app/kickmail`, published to GitHub Packages (npm).

The published deny list at https://kickmail.pages.dev/denylist.txt is not versioned:
it is rebuilt every 6 hours and on every push to `main`. Changes to the repo-owned
entries in `lists/` are recorded under "Deny list".

## [Unreleased]

### Deny list
- Added `lists/denylist.txt` and `lists/allowlist.txt`, merged on top of the upstream
  sources. The allow list has the highest priority, then our deny list, then upstream.
- Blocked `kya2.com` (alias of the bccto.cc temporary mailbox service).

### Java (next: 2.1.1)
- Upgraded jmail to 2.2.2 (validation fixes and faster address parsing), Gradle to 9.7.1,
  shadow plugin to 9.6.1.

### npm (no release planned)
- Development dependencies only: TypeScript 6.0.3, jest 30.5.1, typescript-eslint 8.69.0.
  Build output is unchanged. Resolved all Dependabot alerts.

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
