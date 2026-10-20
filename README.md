# Trivial Trivia

[![CI](https://github.com/Powan55/Trivial-Trivia/actions/workflows/ci.yml/badge.svg)](https://github.com/Powan55/Trivial-Trivia/actions/workflows/ci.yml)

<img src="src/main/resources/static/images/Trivial-Trivia.jpg" alt="" width="140" align="right">

A multiple-choice trivia game. Spring Boot serves it, JSP renders it, and you can swap the
question set for one of your own.

**[Play the demo](https://powan55.github.io/Trivial-Trivia/)** is the game loop running in your
browser, with nothing to wait for. The same page links to
**[the full app](https://trivial-trivia.onrender.com/)**, which is this repository actually
running: accounts, a score history, and an import that persists. It is a free instance, so it
sleeps when nobody is using it and the first request afterwards pays for waking it. It needs a
database pointed at it, and an account there lasts exactly as long as that database does.

It started as a university project for two people and it shows in the git history: 25 pull
requests, a design document, and a set of sequence diagrams drawn before any of it was written.
Version 1.0.0 is the point where all of it actually works.

## What it does

- Ten questions a round, ten points for each correct answer. A wrong answer costs nothing but is
  counted.
- Accounts, with BCrypt-hashed passwords. You can also play as a guest.
- A score history per player. Guests do not get one, because there is nothing to key it to.
- Import and export the question set as CSV, JSON or XML. Anyone can export; importing replaces
  the set everyone plays, so it takes an administrator.

## Running it

Java 17 and Maven.

```bash
mvn spring-boot:run
```

Then open <http://localhost:8080>.

Data lives in `~/.trivial-trivia/`, which locally means an H2 database file. The questions arrive
from a migration that runs at startup, so there is nothing to seed and nothing to copy. If you
have a `~/.trivial-trivia/` left over from 1.0, the accounts and scores in its CSV files are read
into the database the first time 2.0 starts. Point the directory somewhere else if you like:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--app.data.dir=/tmp/trivia
```

Nothing is written inside the project or into the packaged artifact.

## Tests

```bash
mvn -B clean verify
```

JUnit 5 tests cover the game, format adapters, validators, persistence and HTTP routes. Some boot
a real servlet container to check rendered JSPs. The build requires at least 85% line coverage
and 80% branch coverage. The JaCoCo report is written to `target/site/jacoco/index.html`.

Two of them are there specifically to stop old bugs coming back:
`PlayControllerTest.sessionsKeepQuestionsAndScoresSeparate` (the game state used to be a singleton) and
`FormatAdapterTest.theXmlParserDoesNotResolveExternalEntities`.

## How it is put together

| Package | What is in it |
| --- | --- |
| `Controller` | The routes: home, sign in, accounts, the game loop, scores, question import and export |
| `Service` | `RoundService`: persisted round progress, ownership checks and scoring |
| `Game` | `Question` |
| `Authentication` | Sign-in, registration, roles, the sign-in throttle, and who is playing right now. The filter chain itself is `SecurityConfig`, in the root package next to `Main` |
| `Persistence` | The repositories, and the one-time read of a 1.0 data directory |
| `Database` | `RowFormat`, with a CSV, a JSON and an XML implementation behind it |

Three of the patterns from the original design survived into the finished thing:

- **Adapter.** `RowFormat` with `CSVAdapter`, `JSONAdapter` and `XMLAdapter` behind it, and
  `RowFormats` picking one by file extension. They used to be the datastore. They are the import
  and export layer now, which is the job they were always good at, and they read and write streams
  instead of files so an export never has to touch a disk.
- **Proxy.** `SecurityContextPlayer` stands in for whoever is playing. Still a proxy; what it
  stands in front of is now Spring Security's context rather than a mutable session field, and a
  guest is an anonymous authentication instead of a null.
- **MVC.** Controllers, JSP views, and the model in between.

The Command layer from Sprint 1 did not survive: `Action.execute()` takes no arguments and returns
nothing, which cannot express an HTTP request, and Spring's handler methods are already the
dispatch it was trying to be.

The original design documents and sequence diagrams are under [`SprintOne/`](SprintOne). The
[domain model](SprintOne/Diagrams/Domain%20Model%20TrivalTrivia%20.jpg) and
[user authentication](SprintOne/Diagrams/User%20authentication.jpg) diagrams still describe what
the code does. The [database one](SprintOne/Diagrams/Database.jpg) does not, and it is more
interesting for that: it put a `SQLAdapter` behind the same interface as the CSV one, which is a
reasonable guess and not where this ended up. SQL got repositories of its own, and the adapters
kept the format job.

## The demo, and why it is only half the app

<https://powan55.github.io/Trivial-Trivia/> is a static page. GitHub Pages serves files and does
not run a server-side process, so what is deployed there is the game loop rewritten in about a
hundred lines of JavaScript, under [`site/`](site). It scores the same way `RoundService` does: ten
points for a correct answer, nothing subtracted for a wrong one, answers compared ignoring case.

Everything that needs a server is missing from it — registering, signing in, a saved score
history, and an import that persists. That is what the hosted instance is for.

`questions.json` is generated from `QuestionData.csv` when the demo deploys rather than committed,
and a test holds that CSV in step with the migration the app seeds from, so there is still one
question set and adding to it adds to both. Preview the demo locally with:

```bash
python scripts/questions-to-json.py src/main/resources/Data/QuestionData.csv site/questions.json
cp src/main/resources/static/images/Trivial-Trivia.jpg site/
python -m http.server 8081 --directory site
```

## Deploying the real thing

<https://trivial-trivia.onrender.com/> is the app itself, built from the `Dockerfile` here and
redeployed by Render on every push to `main`; `render.yaml` is the whole configuration. The free
instance shapes what it can be: it sleeps after fifteen idle minutes and the next request pays for
waking it, and it has no disk, so the database cannot live in the container. `DATABASE_URL` points
at one outside it, and the app refuses to start under the production profile without it rather
than falling back to something that forgets every account on the next restart.

The JSPs are compiled by the build rather than by Jasper on the first request for each page: at a
tenth of a CPU that was over a minute per page, and the `Dockerfile` hands the compiled classes to
Jasper as its scratch directory so it uses them. Locally nothing changes — `mvn spring-boot:run`
still compiles a JSP on demand, so an edited view still reloads.

The same container runs locally:

```bash
docker build -t trivial-trivia . && docker run --rm -p 8080:8080 trivial-trivia
```

Importing a question set needs an administrator, and there is no way to sign up as one. Register
the account through the site first, then set `APP_ADMIN_USERNAME` to that username and restart: the
role is written to the database on the next boot. Once it has taken, the variable can be unset
again. Pointing it at a username nobody has registered stops the app from starting, on purpose —
left running, the next person to claim that name would come back as an administrator. Taking the
role away is one `update users set role = 'ROLE_USER'` statement; nothing in the app demotes anyone.

Packaging is `war`, so `mvn package` also gives you `target/trivial-trivia-1.0.0.war` for any
Tomcat 10.1 or later. This used to run on Azure; that subscription was disabled and the workflow
that deployed there was removed rather than left to fail on every push.

## Known limits

- A finished round stores its score and not the individual answers, so there is no way to look
  back at which questions you got wrong.
- Sessions are in-memory, so a restart signs everyone out.
- There is no password reset.
- Sign-in throttling is a map in memory: it counts per instance and a restart forgives everyone.
  It locks the account rather than whoever is guessing, so anyone who knows a username can keep
  its owner out.
- The hosted instance is public and shared. Anyone can register on it, though only an
  administrator can replace the question set everyone else plays.

## License

[MIT](LICENSE).
