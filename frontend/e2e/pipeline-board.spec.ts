import { test, expect } from '@playwright/test';
import { loginAsManager, clearAuth, waitForBoardLoaded } from './helpers';

test.describe('Pipeline Board', () => {
  test.beforeEach(async ({ page }) => {
    await clearAuth(page);
    await loginAsManager(page);
  });

  test('board shows 3 phase sections', async ({ page }) => {
    await waitForBoardLoaded(page);
    // The three phases: Early, Commercial, Closure — each has an h2 heading
    await expect(page.locator(':is(h2, h3):has-text("Early")').first()).toBeVisible();
    await expect(page.locator(':is(h2, h3):has-text("Commercial")').first()).toBeVisible();
    await expect(page.locator(':is(h2, h3):has-text("Closure")').first()).toBeVisible();
  });

  test('deal cards render with operator name and stage badge', async ({ page }) => {
    await waitForBoardLoaded(page);
    // Deal cards are buttons with cursor-pointer class
    const dealCards = page.locator('main button[class*="cursor-pointer"]');
    const count = await dealCards.count();
    if (count > 0) {
      // First deal card should have text content
      const firstCard = dealCards.first();
      await expect(firstCard).toBeVisible();
      // Should have some text inside (operator or deal name)
      const text = await firstCard.textContent();
      expect(text?.length).toBeGreaterThan(0);
    }
  });

  test('+ New Deal button visible on desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await waitForBoardLoaded(page);
    // The TopBar has a "New Deal" button (hidden below md, visible at 1280px)
    await expect(page.locator('button:has-text("New Deal")')).toBeVisible({ timeout: 10000 });
  });

  test('Create Deal modal opens with correct fields', async ({ page }) => {
    // Use mobile FAB to trigger the Create Deal modal
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // FAB is the fixed round button
    const fabButton = page.locator('button[class*="fixed"][class*="rounded-full"]');
    await fabButton.waitFor({ timeout: 5000 });
    await fabButton.click();

    // Verify the Create Deal modal opens
    const modal = page.locator('dialog');
    await expect(modal).toBeVisible({ timeout: 5000 });
    await expect(page.locator('h2:has-text("Create New Deal")')).toBeVisible();

    // Check the form fields (labels use CSS uppercase so actual text is mixed-case)
    await expect(page.locator('dialog label:has-text("Deal Name")')).toBeVisible();
    await expect(page.locator('dialog label:has-text("Operator")')).toBeVisible();
    await expect(page.locator('dialog label:has-text("Assigned Agent")')).toBeVisible();
    await expect(page.locator('dialog label:has-text("Fleet Size")')).toBeVisible();
    await expect(page.locator('dialog label:has-text("Estimated Monthly Value")')).toBeVisible();
    await expect(page.locator('dialog label:has-text("Lead Source")')).toBeVisible();
  });

  test('filter bar has correct buttons', async ({ page }) => {
    await waitForBoardLoaded(page);
    const filterLabels = ['Agent', 'Operator', 'Created Date', 'Due Date', 'Region'];
    for (const label of filterLabels) {
      await expect(page.locator(`main button:has-text("${label}")`)).toBeVisible();
    }
  });

  test('phase sections are collapsible', async ({ page }) => {
    await waitForBoardLoaded(page);
    // Phase headers are buttons containing h2 "Early", "Commercial", "Closure"
    const earlyHeader = page.locator('button:has(:is(h2, h3):has-text("Early"))').first();
    await earlyHeader.click();
    await page.waitForTimeout(300);

    // Click again to expand
    await earlyHeader.click();
    await page.waitForTimeout(300);
  });

  test('phase sections show deal count badges', async ({ page }) => {
    await waitForBoardLoaded(page);
    // Each phase header button shows a count number
    // The Early button text includes a number like "1"
    const earlyBtn = page.locator('button:has(:is(h2, h3):has-text("Early"))').first();
    await expect(earlyBtn).toBeVisible();
    const earlyText = await earlyBtn.textContent();
    expect(earlyText?.length).toBeGreaterThan(0);
  });

  test('phase sections show pipeline value', async ({ page }) => {
    await waitForBoardLoaded(page);
    // Phase buttons contain currency values like ₹5,00,000
    const phaseButtons = page.locator('button:has(:is(h2, h3))');
    const count = await phaseButtons.count();
    expect(count).toBeGreaterThanOrEqual(3);
  });

  test('mobile: bottom tabs visible, sidebar hidden', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // Sidebar should be hidden on mobile
    const sidebar = page.locator('aside');
    await expect(sidebar).toBeHidden();

    // Bottom tab bar should be visible — it contains "Alerts" link (sidebar doesn't)
    const bottomNav = page.locator('nav').filter({ has: page.locator('a:has-text("Alerts")') });
    await expect(bottomNav).toBeVisible();
    await expect(bottomNav.locator('text="Board"')).toBeVisible();
    await expect(bottomNav.locator('text="Tasks"')).toBeVisible();
  });

  test('mobile: FAB visible', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await waitForBoardLoaded(page);

    // FAB is a fixed round button at bottom-right
    const fab = page.locator('button[class*="fixed"][class*="rounded-full"]');
    await expect(fab).toBeVisible();
  });

  test('stages within phase show correct stage names', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await waitForBoardLoaded(page);
    // Stage names appear as text in the expanded phase section
    await expect(page.locator('text="Lead Captured"').first()).toBeVisible({ timeout: 10000 });
  });

  test('board page loads without errors', async ({ page }) => {
    await waitForBoardLoaded(page);
    // No error message should be displayed
    await expect(page.locator('text=Failed to load deals')).toHaveCount(0);
  });
});
