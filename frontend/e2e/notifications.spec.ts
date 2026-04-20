import { test, expect } from '@playwright/test';
import { loginAsManager, clearAuth } from './helpers';

// Helper to click the notification bell button
async function clickBell(page: import('@playwright/test').Page) {
  // The bell button is inside a relative-positioned div in the header
  // It's a button with no text (just a bell icon), inside header > div > div.relative
  const bellBtn = page.locator('header [class*="relative"] > button').first();
  await bellBtn.click();
  await page.waitForTimeout(1000);
}

test.describe('Notifications', () => {
  test.beforeEach(async ({ page }) => {
    await clearAuth(page);
    await loginAsManager(page);
  });

  test('bell icon visible in top bar', async ({ page }) => {
    // The notification bell is inside a relative div in the header
    const bellButton = page.locator('header [class*="relative"] > button');
    await expect(bellButton.first()).toBeVisible();
  });

  test('click bell opens notification dropdown panel', async ({ page }) => {
    await clickBell(page);
    // Panel has "Notifications" header
    await expect(page.locator('h3:has-text("Notifications")')).toBeVisible();
  });

  test('notification panel shows Clear All button', async ({ page }) => {
    await clickBell(page);
    await expect(page.locator('button:has-text("Clear All")')).toBeVisible();
  });

  test('notification panel shows View All link', async ({ page }) => {
    await clickBell(page);
    await expect(page.locator('button:has-text("View All")')).toBeVisible();
  });

  test('notification panel shows items or empty state', async ({ page }) => {
    await clickBell(page);
    await page.waitForTimeout(1000);
    // Either notification items are shown, or "No notifications" empty state
    const hasItems = await page.locator('[class*="border-b"][class*="cursor-pointer"]').count() > 0;
    const hasEmpty = await page.locator('text=No notifications').isVisible().catch(() => false);
    const hasLoading = await page.locator('[class*="animate-spin"]').isVisible().catch(() => false);
    expect(hasItems || hasEmpty || hasLoading).toBeTruthy();
  });

  test('notification panel has Show Cleared checkbox', async ({ page }) => {
    await clickBell(page);
    await expect(page.locator('text=Show Cleared')).toBeVisible();
    // Checkbox should be present
    const checkbox = page.locator('input[type="checkbox"]');
    await expect(checkbox).toBeVisible();
  });

  test('clicking outside closes notification panel', async ({ page }) => {
    await clickBell(page);
    await expect(page.locator('h3:has-text("Notifications")')).toBeVisible();
    // Click outside the panel on the main content area
    await page.click('main', { force: true });
    await page.waitForTimeout(500);
    await expect(page.locator('h3:has-text("Notifications")')).toHaveCount(0);
  });
});
