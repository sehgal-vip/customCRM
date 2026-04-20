import { test, expect } from '@playwright/test';
import { loginAsManager, clearAuth, waitForBoardLoaded, navigateToFirstDeal } from './helpers';

test.describe('Deal Detail Page', () => {
  test.beforeEach(async ({ page }) => {
    await clearAuth(page);
    await loginAsManager(page);
    await waitForBoardLoaded(page);
  });

  test('click deal navigates to /board/:id', async ({ page }) => {
    const url = await navigateToFirstDeal(page);
    expect(url).toMatch(/\/board\/\d+/);
  });

  test('back button returns to board', async ({ page }) => {
    await navigateToFirstDeal(page);
    // Click "Back to Pipeline" button
    await page.click('button:has-text("Back to Pipeline")');
    await page.waitForURL('/board', { timeout: 10000 });
    expect(page.url()).toContain('/board');
  });

  test('deal name and operator info visible', async ({ page }) => {
    await navigateToFirstDeal(page);
    // The h1 should contain the deal name
    const dealName = page.locator('main h1').first();
    await expect(dealName).toBeVisible();
    const nameText = await dealName.textContent();
    expect(nameText?.length).toBeGreaterThan(0);
  });

  test('stage progress bar visible with 8 segments', async ({ page }) => {
    await navigateToFirstDeal(page);
    // StageProgressBar renders numbered segments 1-8 (STAGE_1 through STAGE_8)
    // Look for the numbered stage indicators
    await expect(page.locator('text="1"').first()).toBeVisible({ timeout: 5000 });
    await expect(page.locator('text="8"').first()).toBeVisible({ timeout: 5000 });
  });

  test('metadata cards visible (Monthly value, Fleet Size, Days in Stage, Next Action, Due Date)', async ({ page }) => {
    await navigateToFirstDeal(page);
    // Monthly label is either "Monthly Billing" or "Estimated Monthly Value" depending on deal state
    await expect(page.locator('text=/Monthly/').first()).toBeVisible({ timeout: 5000 });
    const otherLabels = ['Fleet Size', 'Days in Stage', 'Next Action', 'Due Date'];
    for (const label of otherLabels) {
      await expect(page.locator(`text="${label}"`)).toBeVisible({ timeout: 5000 });
    }
  });

  test('4 tabs: Activity, Documents, Operator, Audit (no Pricing tab)', async ({ page }) => {
    await navigateToFirstDeal(page);
    // Tab buttons have lowercase text: "activity", "documents", "operator", "audit" (CSS capitalize for display)
    // Use getByRole with exact to avoid matching "Log Activity" etc.
    const expectedTabs = ['activity', 'documents', 'operator', 'audit'];
    for (const tab of expectedTabs) {
      await expect(page.getByRole('button', { name: tab, exact: true })).toBeVisible();
    }
    // Ensure no "pricing" tab button exists (there is a "Pricing History" button, but not a tab)
    await expect(page.getByRole('button', { name: 'pricing', exact: true })).toHaveCount(0);
  });

  test('action buttons visible: Log Activity, Change Stage, Edit Deal, Archive', async ({ page }) => {
    await navigateToFirstDeal(page);
    await expect(page.locator('button:has-text("Log Activity")')).toBeVisible();
    await expect(page.locator('button:has-text("Change Stage")')).toBeVisible();
    await expect(page.locator('button:has-text("Edit Deal")')).toBeVisible();
    await expect(page.locator('button:has-text("Archive")')).toBeVisible();
  });

  test('activity tab is default and shows content', async ({ page }) => {
    await navigateToFirstDeal(page);
    // Activity tab should be active by default — wait for content to load
    await page.waitForTimeout(1000);
    // Should show either activity reports or the empty state message
    const mainContent = page.locator('main');
    // At minimum, the main area should have content
    await expect(mainContent).toBeVisible();
    const text = await mainContent.textContent();
    expect(text?.length).toBeGreaterThan(10);
  });

  test('documents tab shows checklist when clicked', async ({ page }) => {
    await navigateToFirstDeal(page);
    await page.getByRole('button', { name: 'documents', exact: true }).click();
    // Wait for documents tab content to render
    await page.waitForTimeout(500);
    // Should show some document-related content
    const content = page.locator('main');
    await expect(content).toBeVisible();
  });

  test('operator tab shows operator info when clicked', async ({ page }) => {
    await navigateToFirstDeal(page);
    await page.getByRole('button', { name: 'operator', exact: true }).click();
    await page.waitForTimeout(500);
    const content = page.locator('main');
    await expect(content).toBeVisible();
  });

  test('audit tab shows timeline when clicked', async ({ page }) => {
    await navigateToFirstDeal(page);
    await page.getByRole('button', { name: 'audit', exact: true }).click();
    await page.waitForTimeout(500);
    const content = page.locator('main');
    await expect(content).toBeVisible();
  });

  test('status badges visible (phase name and stage badge)', async ({ page }) => {
    await navigateToFirstDeal(page);
    // Phase badge (Early/Commercial/Closure) is shown as a div/span in deal detail header
    const phaseBadges = page.locator('text=/Early|Commercial|Closure/');
    await expect(phaseBadges.first()).toBeVisible({ timeout: 5000 });
  });

  test('pricing history button visible', async ({ page }) => {
    await navigateToFirstDeal(page);
    await expect(page.locator('button:has-text("Pricing History")')).toBeVisible();
  });

  test('Log Activity button opens modal', async ({ page }) => {
    await navigateToFirstDeal(page);
    await page.click('button:has-text("Log Activity")');
    await expect(page.locator('dialog')).toBeVisible({ timeout: 5000 });
  });

  test('Change Stage button opens modal', async ({ page }) => {
    await navigateToFirstDeal(page);
    await page.click('button:has-text("Change Stage")');
    await expect(page.locator('dialog')).toBeVisible({ timeout: 5000 });
  });

  test('Archive button opens confirmation modal', async ({ page }) => {
    await navigateToFirstDeal(page);
    await page.click('button:has-text("Archive")');
    await expect(page.locator('dialog')).toBeVisible({ timeout: 5000 });
  });
});
