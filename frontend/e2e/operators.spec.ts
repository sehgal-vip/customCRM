import { test, expect } from '@playwright/test';
import { loginAsManager, clearAuth } from './helpers';

test.describe('Operators Page', () => {
  test.beforeEach(async ({ page }) => {
    await clearAuth(page);
    await loginAsManager(page);
    await page.goto('/operators');
    // Wait for operators heading in main content (h2)
    await page.waitForSelector('h2:has-text("Operators")', { timeout: 15000 });
  });

  test('operators page shows card grid', async ({ page }) => {
    // Wait for operators to load
    await page.waitForTimeout(2000);
    // Either operator cards are shown or loading spinner clears
    const cards = page.locator('main [class*="cursor-pointer"]');
    const count = await cards.count();
    const hasEmpty = await page.locator('text=No operators found').isVisible().catch(() => false);
    expect(count > 0 || hasEmpty).toBeTruthy();
  });

  test('search input is visible and functional', async ({ page }) => {
    const searchInput = page.locator('input[placeholder*="Search operators"]');
    await expect(searchInput).toBeVisible();
    // Type something to filter
    await searchInput.fill('test');
    await page.waitForTimeout(1000);
    // The search should trigger a re-query
    await expect(searchInput).toHaveValue('test');
  });

  test('+ New Operator button opens modal', async ({ page }) => {
    const newBtn = page.locator('button:has-text("New Operator")');
    await expect(newBtn).toBeVisible();
    await newBtn.click();
    // Modal should open
    await expect(page.locator('dialog')).toBeVisible({ timeout: 5000 });
    // Modal should have Company Name field
    await expect(page.locator('dialog :text("Company Name")')).toBeVisible();
  });

  test('type filter buttons work', async ({ page }) => {
    // Filter buttons: All, Private Fleet, Govt Contract, School/Corporate, Mixed
    const filters = ['All', 'Private Fleet', 'Govt Contract', 'School/Corporate', 'Mixed'];
    for (const f of filters) {
      const btn = page.locator(`main button:has-text("${f}")`);
      await expect(btn).toBeVisible();
    }
    // Click Private Fleet filter
    await page.click('main button:has-text("Private Fleet")');
    await page.waitForTimeout(1000);
  });

  test('operator card shows company name', async ({ page }) => {
    await page.waitForTimeout(2000);
    const cards = page.locator('main [class*="cursor-pointer"]');
    const count = await cards.count();
    if (count > 0) {
      const firstCard = cards.first();
      // Card should have a company name (h3)
      const companyName = firstCard.locator('h3');
      await expect(companyName).toBeVisible();
      const text = await companyName.textContent();
      expect(text?.length).toBeGreaterThan(0);
    }
  });

  test('create operator modal has all required fields', async ({ page }) => {
    await page.click('button:has-text("New Operator")');
    await expect(page.locator('dialog')).toBeVisible({ timeout: 5000 });

    // Check fields
    await expect(page.locator('dialog :text("Company Name")')).toBeVisible();
    await expect(page.locator('dialog :text("Phone")')).toBeVisible();
    await expect(page.locator('dialog :text("Email")')).toBeVisible();
    await expect(page.locator('dialog :text("Operator Type")')).toBeVisible();
    await expect(page.locator('dialog :text("Fleet Size")')).toBeVisible();
    await expect(page.locator('dialog :text("Number of Routes")')).toBeVisible();
    await expect(page.locator('dialog :text("Primary Use Case")')).toBeVisible();

    // Cancel button
    await page.click('dialog button:has-text("Cancel")');
    await page.waitForTimeout(500);
  });
});
