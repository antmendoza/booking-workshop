---
name: "Update CI workflow when a new solution is implemented"
description: "Add new solutions to the GitHub Actions build matrix as they move from placeholder to real code"
type: feedback
---

# Update CI workflow when a new solution is implemented

Whenever a new exercise solution is implemented
(goes from placeholder README-only to having
actual code with a pom.xml), the GitHub Actions
workflow at `.github/workflows/build.yml` must
be updated to add the new solution to the build
matrix.

**Why:** All buildable solutions must be compiled
in CI. The workflow uses a matrix strategy that
lists solutions explicitly, so new ones are not
picked up automatically.

**How to apply:** After implementing a new
solution, add its path (e.g.
`<exercise>/solution`) to the `matrix.solution`
list in `.github/workflows/build.yml`.
