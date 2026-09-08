# AI Developer Instructions

You are the autonomous developer working on this repository.

## Project

Build a production-quality distributed rate limiter in Java.

The system should eventually support:

- Token bucket rate limiting
- Redis-backed distributed state
- Multiple application instances
- Concurrent requests
- HTTP middleware
- Configuration
- Unit tests
- Integration tests
- Benchmarks
- Docker
- Observability
- Logging
- Failure handling
- Documentation

## Core Rules

1. Make one meaningful improvement per work session.
2. Never modify unrelated parts of the project.
3. Do not remove working functionality without a strong reason.
4. Every new feature must have appropriate tests.
5. Existing tests must continue to pass.
6. Run formatting before committing.
7. Run linting when configured.
8. Run the complete test suite before committing.
9. Do not introduce unnecessary dependencies.
10. Do not hard-code secrets.
11. Never commit credentials, API keys, passwords, or tokens.
12. Prefer simple designs over unnecessary abstractions.
13. Document important architectural decisions.
14. Do not claim a task is complete unless it has been verified.

## Git Rules

Create focused commits.

Commit messages should explain the change.

Do not rewrite previous commits.

Do not force-push.

Never push directly to main unless explicitly instructed.

Prefer:

feature/<description>


## Daily Process

At the beginning of every session:

1. Inspect the repository.
2. Read AGENTS.md.
3. Read PROJECT.md.
4. Inspect recent git history.
5. Inspect the current TODO/backlog.
6. Determine the highest-priority unfinished task.
7. Implement the task.
8. Write or update tests.
9. Run tests.
10. Fix failures.
11. Update documentation if necessary.
12. Commit the completed work.
13. Update the task status.

## Quality

Correctness is more important than lines of code.

Never create unnecessary code simply to increase the number of changed lines.

A small correct change is better than a large meaningless change.