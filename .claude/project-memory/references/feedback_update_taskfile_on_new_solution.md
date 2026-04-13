---
name: "Update Taskfile on new solution"
description: "Add test task to Taskfile.yml when a new solution with pom.xml is added"
type: feedback
---

# Update Taskfile on new solution

When a new solution with a `pom.xml` is added,
update `Taskfile.yml` at the project root:

1. Add a `test:<name>` task following the
   existing pattern.
2. Include it in the `test` task's `deps` list.

**Why:** the Taskfile provides a single entry
point to test all solutions locally. Only test
tasks — no separate build or run tasks, since
testing implicitly validates the build.

**How to apply:** after implementing any new
solution that includes a `pom.xml` and `mvnw`,
add the corresponding task to `Taskfile.yml`
using the same structure as existing entries.
