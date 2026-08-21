# Changelog

## v1.0.0 — 2026-08-20

The first release where the whole thing works end to end. Before this, sign-in could not succeed
and an account did not survive a restart.

### Added

- Registration, sign-in, a guest path and sign-out, with server-side validation (#55, #68)
- A score history per signed-in player, written when a round finishes (#29)
- Import and export the question set as CSV, JSON or XML (#5, #3)
- A landing page — `/` used to be a 404 when running standalone
- 111 JUnit 5 tests and a CI workflow that runs on every push and pull request, with a JaCoCo
  coverage floor (#70)
- MIT license

### Fixed

- Sign-in could never succeed: the user file was read from a classpath path that does not
  exist (#58)
- Account creation wrote to a directory that is not there, and reported success anyway (#59)
- Any username passed the uniqueness check, because the list it checked never loaded (#60)
- Two people playing at once shared one score and one question cursor (#61)
- The current user was held in a `static` field, so everyone signed in was the same person (#62)
- Loading the questions threw when the file could not be read (#63)
- `@Autowired` on a `final` field that the constructor already assigned (#64)
- A wrong answer subtracted ten points, and the wrong counter stopped once the score hit zero (#65)
- The README linked a demo that returns 403 (#71)

### Security

- Passwords are BCrypt instead of a single round of SHA-256 (#66)
- Three cleartext passwords and their hashes removed from the repository (#67). They remain in the
  git history; treat them as burned.
- Everything the server prints on a page goes through `<c:out>`
- The XML parser has DTDs and external entities switched off — it reads uploaded files
- Uploads are validated and written under a name the server chose, never the uploaded one
- Spring Boot 3.2.4 to 3.5.16, and the hand-pinned `spring-*` and `tomcat-embed-core` versions
  removed so the starter parent manages them

### Removed

- The Scanner-driven console driver, unreachable since the app became a web app in #22 (#69)
- The `Action` command layer it dispatched through — Spring's handler methods already are that
- The Azure deploy workflow: the subscription behind it is disabled, so every run failed
- `.idea/`, the `.iml` files and a Qodana config no workflow ever ran (#72)
