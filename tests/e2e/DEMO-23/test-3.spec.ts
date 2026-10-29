import { test, expect } from '@playwright/test';

test('The system should not allow any changes to the mode at the story level when no code_mode is provided', async ({ page }) => {
  await page.goto('/stories/dl-14'); // Assuming this route leads to the DL-14 control screen
  const modeInput = page.locator('role:button[name="Change Mode"]');
  await expect(modeInput).toBeDisabled();
});