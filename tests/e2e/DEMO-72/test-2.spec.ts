import { test, expect } from '@playwright/test';

test('If the project flag is on, the effective mode should follow the project flag setting regardless of the install default when no code_mode is specified on the story', async ({ page }) => {
  await page.goto('/stories/dl-14'); // Assuming this route leads to the DL-14 control screen
  const effectiveMode = await page.locator('role:heading[level=2]').filter({ hasText: 'Effective Mode' }).nextSibling().textContent();
  expect(effectiveMode).toBe('project-flag-mode'); // Replace 'project-flag-mode' with the actual expected mode when project flag is on
});