# DEMO-2: Story B: audit log export

Demo story for numbering tests

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- The system must provide an option to export audit logs.
- Exported audit logs should include all relevant details such as timestamp, user actions, and changes made.

## Non-functional requirements

- The export process should not impact the performance of the system negatively.
- Audit log exports should be secure and accessible only to authorized personnel.

## Acceptance criteria

- Users with appropriate permissions can initiate an audit log export.
- Exported files are in a readable format (e.g., CSV, PDF).
- The exported data matches the current state of the audit logs.

## Business rules

- Only users with specific roles or permissions can access and export audit logs.
- Audit logs must be retained for at least one year as per compliance regulations.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/api/DEMO-2/test-1.txt`
- `tests/api/DEMO-2/test-2.txt`
- `tests/api/DEMO-2/test-3.txt`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /api/audit-logs/export`

## Planned subtasks

- [AuditLogService] Implement audit log export functionality (DEMO-36)
- [ExportService] Create export service for different formats (DEMO-37)
- [AuditLogController] Add endpoint for audit log export (DEMO-38)
- [UserPermissionsModule] Implement permission checks for audit log export (DEMO-39)
- [AuditLogControllerTest] Write tests for audit log export endpoint (DEMO-40)
- [AuditLogServiceTest] Write tests for audit log preparation logic (DEMO-41)
- [ExportServiceTest] Write tests for export services (DEMO-42)
- [configuration] Configure export settings (DEMO-43)
- [other] Document audit log export feature (DEMO-44)
