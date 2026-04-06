# Phase 01: Исправление критических ошибок - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Fix compilation errors by correcting file naming and class names. Goal: `mvn compile` succeeds. No new features — only fixes to make existing code build.

</domain>

<decisions>
## Implementation Decisions

### File Naming Fix
- **D-01:** Rename `ProcessData.Java` → `ProcessData.java` (fix extension case)

### Class Naming Strategy
- **D-02:** Rename `Controller.java` → `Task2Controller.java` (class and file)
- **D-03:** Rename `Dispatcher.java` → `Task2Dispatcher.java` (class and file)
- **D-04:** Update all references to use new class names

### the agent's Discretion
- Order of fixes (can be done in any sequence)
- Whether to also fix `SelectTaskMenu.Java` if it exists with wrong case

</decisions>

<canonical_refs>
## Canonical References

### Project Requirements
- `.planning/REQUIREMENTS.md` — FR-001: Компилируемость проекта
- `.planning/codebase/CONCERNS.md` §1.1-1.2 — Details of compilation errors
- `.planning/codebase/STRUCTURE.md` — File structure and class locations

</canonical_refs>

<code_context>
## Existing Code Insights

### Files to modify
- `L4S4-Maven/src/main/java/Task2/ProcessData.Java` → rename to `.java`
- `L4S4-Maven/src/main/java/Task2/Controller.java` → rename to `Task2Controller.java`
- `L4S4-Maven/src/main/java/Task2/Dispatcher.java` → rename to `Task2Dispatcher.java`
- `L4S4-Maven/src/main/java/Task2/Task2ConsoleDemo.java` → update class references
- `L4S4-Maven/src/main/java/GUI/Task2/ConnectionFrame.java` → update if needed

### Integration Points
- Maven compile will validate all changes
- No runtime testing required in this phase

</code_context>

<specifics>
## Specific Ideas

- User chose to rename classes to more descriptive names (Task2Controller, Task2Dispatcher)
- This matches the original intent of the code (references already use Task2 prefix)

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 01-fix-critical-errors*
*Context gathered: 2026-04-06*
