# Working on this repo

## Branches

`main` is the only long-lived branch and it is protected by CI, not by convention — every push
and every pull request runs the build and the tests. Work on a short-lived branch off `main`,
open a pull request, merge it when the checks are green. Don't push to `main` directly.

Branch names follow what the change is: `fix/`, `feat/`, `refactor/`, `chore/`, `docs/`,
`security/`, `build/`.

## Before you push

```bash
mvn -B clean verify
```

Green means: it compiles, all the tests pass, and coverage is above the floor in `pom.xml`. CI
runs the same command, so a red build locally is a red build there.

If you touched a JSP, that is not enough. A JSP compiles when it is first requested, so a syntax
error in a view does not fail the build — it fails whoever opens the page. Start the app and load
what you changed:

```bash
mvn spring-boot:run
```

## Tests

Every behaviour change comes with a test. Not for its own sake — for the ones that have already
come back once:

- Anything touching per-player state needs a two-session test. The game state used to be a
  singleton and two people playing at once shared one score.
- Anything reading a file a user supplied needs a malformed-input test. Returning empty beats
  throwing on the page.
- Anything printing a user's own words back at them needs to go through `<c:out>`.

`@TempDir` for anything that writes. The suite must not touch `~/.trivial-trivia/`; surefire
points `app.data.dir` at `target/test-data` so it doesn't.

## Commits and pull requests

Short. A commit subject says what changed; a body only explains a *why* the diff doesn't show.
A pull request is a couple of sentences and the issue number — the diff is right there, so don't
narrate it.

Reference issues as `Closes #NN` so they close on merge.

## Data files

`src/main/resources/Data/` holds shipped seed data and nothing else. Real user data belongs in
the data directory at runtime. This repo carried three cleartext passwords in
`Data/loginData.txt` for two years, so: no credentials, not even fake-looking ones.
