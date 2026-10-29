
import { test, expect } from '@playwright/test';

test('User can see login form on the page', async ({ page }) => {
  await page.goto('/login');
  const loginForm = await page.locator('text=Login to your account');
  expect(loginForm).toBeVisible();
});
