---
name: "Package and module conventions"
description: "Root package naming, per-module sub-packages, module independence, unit tests in solutions, reference module, and root README updates"
type: project
---

# Package and module conventions

Root package for the entire workshop:
`io.temporal.workshops.springboot`.

Each module uses a distinct sub-package under
the root:
`io.temporal.workshops.springboot.<short-module>`.

Examples:
- `io.temporal.workshops.springboot.testing`
- `io.temporal.workshops.springboot.priority`
- `io.temporal.workshops.springboot.saga`

Each module must be fully independent: no
shared parent POM, no cross-module dependencies,
no imports between modules. Every module is
self-contained with its own build and
dependencies.

The **testing** module is the reference template
when creating new modules — both for code
structure and documentation layout.

Each module's `solution/` must include unit
tests that verify the code works correctly.

When a new module is created, the root
`README.md` must be updated to include a link
to the new module's `README.md`.

**Why:** keeps modules cleanly isolated so they
can be used in any order during a workshop;
the testing module is well-structured and serves
as the canonical example; the root README is the
single entry point for navigating modules.

**How to apply:** when scaffolding a new exercise
module, copy the testing module's directory
layout, pom.xml shape, README structure, and
package hierarchy, then rename the sub-package
to match the new module's short name. Do not
reference or depend on any other module. Include
unit tests in the solution to validate the
implementation. After creating the module, add
a link to its README in the root `README.md`
modules list.
