# DEMO-23: DL-14 control: no story-level mode override

No code_mode on the story, so it follows the project flag and the install default. Effective mode should be manual while the flag is off.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- The system should not allow story-level mode overrides when no code_mode is specified on the story.
- The effective mode of the story should be determined by the project flag and the install default settings.

## Non-functional requirements

- The solution should maintain consistency with existing system behavior regarding mode determination.
- The system should handle scenarios where both project flag and install default are not explicitly set for a mode.

## Acceptance criteria

- When no code_mode is specified on the story, the effective mode should be manual if the project flag is off.
- If the project flag is on, the effective mode should follow the project flag setting regardless of the install default when no code_mode is specified on the story.
- The system should not allow any changes to the mode at the story level when no code_mode is provided.

## Business rules

- In the absence of a story-level code_mode, the effective mode is determined by the project flag and install default settings.
- When the project flag is off, the default mode should be manual unless overridden by an explicit setting at higher levels (e.g., install default).

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them against your own local dev server as you work (`npx playwright test <path>`), not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/e2e/DEMO-23/test-1.spec.ts`
- `tests/e2e/DEMO-23/test-2.spec.ts`
- `tests/e2e/DEMO-23/test-3.spec.ts`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Pages the tests open: `/stories/dl-14`

## Planned subtasks

- [fullstack] Implement logic to determine effective mode based on project flag and install default settings (DEMO-24)
- [fullstack] Write tests for effective mode determination logic (DEMO-26)
- [other] Update documentation for effective mode determination logic (DEMO-28)
- [fullstack] Write tests to prevent story-level mode overrides (DEMO-27)
- [fullstack] Prevent story-level mode overrides when no code_mode is specified (DEMO-25)
