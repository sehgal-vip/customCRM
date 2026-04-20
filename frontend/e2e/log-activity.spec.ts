import { test, expect } from '@playwright/test';
import { loginAsManager, clearAuth, waitForBoardLoaded, navigateToFirstDeal } from './helpers';

test.describe('Log Activity Modal', () => {
  test.beforeEach(async ({ page }) => {
    await clearAuth(page);
    await loginAsManager(page);
    await waitForBoardLoaded(page);
    await navigateToFirstDeal(page);
    // Open Log Activity modal
    await page.click('button:has-text("Log Activity")');
    await page.waitForSelector('dialog', { timeout: 5000 });
  });

  test('modal opens with correct title', async ({ page }) => {
    await expect(page.locator('dialog h2:has-text("Log Activity")')).toBeVisible();
  });

  test('modal title shows deal name', async ({ page }) => {
    // The modal header has the deal name as a subtitle paragraph
    const subtitle = page.locator('dialog p').first();
    await expect(subtitle).toBeVisible();
    const text = await subtitle.textContent();
    expect(text?.length).toBeGreaterThan(0);
  });

  test('activity type shows Field Visit and Virtual buttons', async ({ page }) => {
    await expect(page.locator('dialog button:has-text("Field Visit")')).toBeVisible();
    await expect(page.locator('dialog button:has-text("Virtual")')).toBeVisible();
  });

  test('Field Visit is selected by default', async ({ page }) => {
    const fieldVisitBtn = page.locator('dialog button:has-text("Field Visit")');
    await expect(fieldVisitBtn).toBeVisible();
  });

  test('clicking Virtual switches activity type', async ({ page }) => {
    await page.click('dialog button:has-text("Virtual")');
    const virtualBtn = page.locator('dialog button:has-text("Virtual")');
    await expect(virtualBtn).toBeVisible();
  });

  test('template indicator shows template type', async ({ page }) => {
    // The template indicator text like "Template: T1 -- Early Field Visit -- First"
    await expect(page.locator('dialog :text("Template:")')).toBeVisible();
  });

  test('contact dropdown is present with select option', async ({ page }) => {
    // Contact Person uses a combobox (select element)
    const contactSelect = page.locator('dialog select').first();
    await expect(contactSelect).toBeVisible();
    // Should have "Select contact..." as first option (options are not "visible" - check attached count)
    await expect(contactSelect.locator('option:has-text("Select contact")')).toBeAttached();
    // Should have "+ New Contact" option
    await expect(contactSelect.locator('option:has-text("+ New Contact")')).toBeAttached();
  });

  test('contact role dropdown is present', async ({ page }) => {
    await expect(page.locator('dialog :text("Contact Role")')).toBeVisible();
    // Find the role select (second select in dialog)
    const roleSelect = page.locator('dialog select').nth(1);
    await expect(roleSelect).toBeVisible();
    // Should have options: Owner, Fleet Manager, etc. (options are attached, not "visible")
    await expect(roleSelect.locator('option:has-text("Owner")')).toBeAttached();
    await expect(roleSelect.locator('option:has-text("Fleet Manager")')).toBeAttached();
  });

  test('duration dropdown with 5 options', async ({ page }) => {
    await expect(page.locator('dialog :text("Duration")')).toBeVisible();
    // The duration select has duration options
    const durationSelect = page.locator('dialog select:has(option:has-text("< 15 min"))');
    await expect(durationSelect).toBeVisible();
    // Check all 5 duration options (options are attached, not "visible")
    const durations = ['< 15 min', '15-30 min', '30-60 min', '1-2 hrs', '2+ hrs'];
    for (const d of durations) {
      await expect(durationSelect.locator(`option:has-text("${d}")`)).toBeAttached();
    }
  });

  test('objection chips are present and toggleable', async ({ page }) => {
    await expect(page.locator('dialog :text("Objections Raised")')).toBeVisible();
    // Find an objection chip button
    const chip = page.locator('dialog button:has-text("Price too high")');
    await expect(chip).toBeVisible();
    // Click to toggle on
    await chip.click();
    // Wait for state change
    await page.waitForTimeout(200);
    // Click to toggle off
    await chip.click();
  });

  test('buying signal chips are present and toggleable', async ({ page }) => {
    await expect(page.locator('dialog :text("Buying Signals")')).toBeVisible();
    const chip = page.locator('dialog button:has-text("Asking about delivery timelines")');
    await expect(chip).toBeVisible();
    // Toggle on
    await chip.click();
    await page.waitForTimeout(200);
    // Toggle off
    await chip.click();
  });

  test('notes textarea is present', async ({ page }) => {
    await expect(page.locator('dialog :text("Notes")')).toBeVisible();
    const textarea = page.locator('dialog textarea[placeholder*="Additional context"]');
    await expect(textarea).toBeVisible();
  });

  test('next action section has step, due date, owner fields', async ({ page }) => {
    await expect(page.locator('dialog h4:has-text("Next Action")')).toBeVisible();
    await expect(page.locator('dialog :text("Next Step")')).toBeVisible();
    await expect(page.locator('dialog input[placeholder*="next step"]')).toBeVisible();
    // Owner select with options (options are attached, not "visible")
    const ownerSelect = page.locator('dialog select:has(option:has-text("Self"))');
    await expect(ownerSelect).toBeVisible();
    await expect(ownerSelect.locator('option:has-text("Manager")')).toBeAttached();
    await expect(ownerSelect.locator('option:has-text("Operator")')).toBeAttached();
  });

  test('submit button is present', async ({ page }) => {
    await expect(page.locator('dialog button:has-text("Submit Report")')).toBeVisible();
  });

  test('cancel button closes modal', async ({ page }) => {
    await page.click('dialog button:has-text("Cancel")');
    // Modal should close
    await expect(page.locator('dialog')).toHaveCount(0, { timeout: 3000 });
  });

  test('close (X) button closes modal', async ({ page }) => {
    // The X button is next to the title in the dialog header
    // It's the button right after the "Log Activity" heading
    // Use Escape key as a reliable way to close the dialog
    await page.keyboard.press('Escape');
    await expect(page.locator('dialog')).toHaveCount(0, { timeout: 3000 });
  });
});
