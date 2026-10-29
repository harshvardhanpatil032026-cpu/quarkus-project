import { test, expect } from '@playwright/test';

test('When no code_mode is specified on the story, the effective mode should be manual if the project flag is off', async ({ page }) => {
  await page.goto('/stories/dl-14'); // Assuming this route leads to the DL-14 control screen
  const effectiveMode = await page.locator('role:heading[level=2]').filter({ hasText: 'Effective Mode' }).nextSibling().textContent();
  expect(effectiveMode).toBe('manual');
});