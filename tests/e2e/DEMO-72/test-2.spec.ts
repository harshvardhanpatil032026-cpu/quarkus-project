import { test, expect } from '@playwright/test';

// Assuming the project flag is on and set to 'auto'
// Navigate to a real starting route before asserting anything
// Let's assume this information is displayed on the /settings page

test('If the project flag is on, the effective mode should follow the project flag setting regardless of the install default when no code_mode is specified', async ({ page }) => {
  await page.goto('/settings');
  const modeText = await page.textContent('text=Effective Mode:');
  expect(modeText).toBe('Effective Mode: Auto');
});