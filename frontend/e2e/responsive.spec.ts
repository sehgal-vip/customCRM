import { test, expect } from '@playwright/test';
import { loginAsManager, clearAuth, waitForBoardLoaded } from './helpers';

test.describe('Responsive Design', () => {
  test.beforeEach(async ({ page }) => {
    await clearAuth(page);
    await loginAsManager(page);
  });

  test('desktop (1280px): sidebar visible', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await waitForBoardLoaded(page);

    const sidebar = page.locator('aside');
    await expect(sidebar).toBeVisible();
    // Sidebar should show nav items — use getByRole with exact to avoid "Board" matching "Dashboard"
    await expect(sidebar.getByRole('link', { name: 'Board', exact: true })).toBeVisible();
    await expect(sidebar.getByRole('link', { name: 'Tasks', exact: true })).toBeVisible();
    await expect(sidebar.getByRole('link', { name: 'Operators', exact: true })).toBeVisible();
    await expect(sidebar.getByRole('link', { name: 'Dashboard', exact: true })).toBeVisible();
  });

  test('desktop (1280px): bottom tab bar hidden', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await waitForBoardLoaded(page);

    // Bottom tab bar has class md:hidden, so should be hidden on desktop
    // The bottom nav is the only <nav> that contains an "Alerts" link
    const bottomNav = page.locator('nav').filter({ has: page.locator('a:has-text("Alerts")') });
    await expect(bottomNav).toBeHidden();
  });

  test('desktop (1280px): New Deal button visible in top bar', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await waitForBoardLoaded(page);

    await expect(page.locator('button:has-text("New Deal")')).toBeVisible({ timeout: 10000 });
  });

  test('mobile (375px): sidebar hidden', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // Sidebar has class "hidden md:flex" so hidden on mobile
    const sidebar = page.locator('aside');
    await expect(sidebar).toBeHidden();
  });

  test('mobile (375px): bottom tabs visible', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // Bottom tabs nav contains Board, Tasks, Alerts, More links
    const bottomNav = page.locator('nav').filter({ has: page.locator('a:has-text("Alerts")') });
    await expect(bottomNav).toBeVisible();
    await expect(bottomNav.locator('text="Board"')).toBeVisible();
    await expect(bottomNav.locator('text="Tasks"')).toBeVisible();
    await expect(bottomNav.locator('text="Alerts"')).toBeVisible();
    await expect(bottomNav.locator('text="More"')).toBeVisible();
  });

  test('mobile (375px): FAB visible on pipeline board', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // FAB: fixed round button at bottom right
    const fab = page.locator('button[class*="fixed"][class*="rounded-full"]');
    await expect(fab).toBeVisible();
  });

  test('mobile (375px): search hidden as full bar, icon may exist', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // The GlobalSearch wrapper has "hidden md:block" so should be hidden on mobile
    // The search textbox should not be visible
    const searchInput = page.locator('header input[placeholder*="Search"]');
    // On mobile the search container is hidden, so the input won't be attached or visible
    const count = await searchInput.count();
    if (count > 0) {
      await expect(searchInput).toBeHidden();
    }
    // Test passes either way — input is either hidden or not in DOM
  });

  test('mobile (375px): New Deal button hidden in top bar', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // The "New Deal" button in top bar has class "hidden md:inline-flex"
    const newDealBtn = page.locator('header button:has-text("New Deal")');
    await expect(newDealBtn).toBeHidden();
  });
});
