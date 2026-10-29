import { test, expect } from '@playwright/test';

// Assuming the project flag is off and there's a page to check the mode setting
// Navigate to a real starting route before asserting anything
// Let's assume this information is displayed on the /settings page

test('When no code_mode is specified, the effective mode should be manual if the project flag is off', async ({ page }) => {
  await page.goto('/settings');
  const modeText = await page.textContent('text=Effective Mode:');
  expect(modeText).toBe('Effective Mode: Manual');
});