# DEMO-91: Update and delete a user account

As a registered user, I want to update my name or password and delete my account so that I can keep my details current and remove them when I no longer need the service.
A name or password change should be validated the same way as registration, and a new password must be stored only as a bcrypt hash. Changing the email must not be allowed to duplicate another user's email. Deleting an account should remove the user and return a clear response if the user doesn't exist. No response may contain the password or its hash.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- As a registered user, I can update my name.
- As a registered user, I can update my password.
- As a registered user, I can delete my account.

## Non-functional requirements

- The system should validate the new name and password in the same way as during registration.
- The new password must be stored only as a bcrypt hash.
- Changing the email is not allowed to duplicate another user's email.
- Deleting an account should return a clear response if the user doesn't exist.
- No response may contain the password or its hash.

## Acceptance criteria

- A user can successfully update their name and the change is reflected in the database.
- A user can successfully update their password, which is stored as a bcrypt hash in the database.
- A user cannot update their email to one that already exists in the system.
- A user can delete their account and it is removed from the database.
- Attempting to delete a non-existent user returns a clear error message.
- Updating a name trims spaces at the start and end, and only allows letters (any language), spaces, hyphen, apostrophe and period; 2-100 characters long.
- Updating a password requires it to be 8-64 characters long, with at least one uppercase letter, one lowercase letter, one digit, and one special character.
- Password update fails if the new password contains the user's email or name, or is the same as the current password.
- Password update requires the current password for verification.
- Validation failure returns a 400 status code with all messages listed.
- Incorrect current password during password change returns a 401 status code with the message 'Invalid email or password'.

## Business rules

- User details must be kept current by allowing updates to name and password.
- Users should have the option to remove their accounts when no longer needed.
- Password security is maintained by storing only bcrypt hashes of passwords.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `src/test/java/com/my/proj/resource/Criterion1_UpdateNameIsStoredTest.java`
- `src/test/java/com/my/proj/resource/Criterion2_UpdatePasswordStoredAsBcryptTest.java`
- `src/test/java/com/my/proj/resource/Criterion3_UpdateEmailToExistingIsRefusedTest.java`
- `src/test/java/com/my/proj/resource/Criterion4_DeleteAccountRemovesUserTest.java`
- `src/test/java/com/my/proj/resource/Criterion5_DeleteMissingUserClearErrorTest.java`
- `src/test/java/com/my/proj/resource/Criterion6_NameUpdateRulesTest.java`
- `src/test/java/com/my/proj/resource/Criterion7_PasswordStrengthRulesTest.java`
- `src/test/java/com/my/proj/resource/Criterion8_PasswordNotEmailNameOrCurrentTest.java`
- `src/test/java/com/my/proj/resource/Criterion9_PasswordUpdateNeedsCurrentPasswordTest.java`
- `src/test/java/com/my/proj/resource/Criterion10_ValidationListsEveryMessageTest.java`
- `src/test/java/com/my/proj/resource/Criterion11_WrongCurrentPasswordGives401Test.java`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /users`, `PATCH /users/email/{email}`, `PUT /users/email/{email}/password`, `GET /users/email/{email}`, `DELETE /users/email/{email}`

## Planned subtasks

- [other] Implement account deletion functionality in UserService (DEMO-122)
- [other] Write unit tests for name and password updates (DEMO-126)
- [other] Implement password update functionality in UserService (DEMO-121)
- [other] Update UserResource to handle account deletion (DEMO-125)
- [other] Update UserResource to handle name updates (DEMO-123)
- [other] Write unit tests for account deletion (DEMO-127)
- [other] Implement name update functionality in UserService (DEMO-120)
- [other] Update UserResource to handle password updates (DEMO-124)


Clarifications from the product owner (authoritative; follow these):
- What specific validation rules should be applied to name and password updates? → Name
- Required. Spaces at the start and end are trimmed.
- 2–100 characters.
- Letters (any language), spaces, hyphen, apostrophe and period only. No digits, symbols or HTML.

Password
- 8–64 characters (bcrypt ignores anything past 72 bytes).
- At least 1 uppercase letter, 1 lowercase letter, 1 digit and 1 special character.
- Can't contain the user's email or name, and can't be the same as the current password.
- The current password is required to change it.
- Stored only as a bcrypt hash and never returned or logged.

Errors
- Validation failure: 400 with all messages listed.
- Wrong current password: 401 with the generic message "Invalid email or password".

Open questions for the product owner (not answered yet). Do not invent an answer. Before writing code that depends on one, stop and ask the developer.
- Should there be any restrictions on how often a user can update their details?