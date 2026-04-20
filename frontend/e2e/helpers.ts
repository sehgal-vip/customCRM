import { Page } from '@playwright/test';

/**
 * Login as Vipul Sehgal (Manager role).
 * Clears localStorage first to ensure a clean session.
 */
export async function loginAsManager(page: Page) {
  await page.goto('/login');
  // Wait for user list to load from API
  await page.waitForSelector('button:has-text("MANAGER")', { timeout: 10000 });
  await page.click('button:has-text("Vipul Sehgal")');
  await page.waitForURL('/board', { timeout: 15000 });
  // Ensure the board page is rendered
  await page.waitForSelector('header', { timeout: 10000 });
}

/**
 * Login as Rahul Sharma (Agent role).
 */
export async function loginAsAgent(page: Page) {
  await page.goto('/login');
  await page.waitForSelector('button:has-text("AGENT")', { timeout: 10000 });
  await page.click('button:has-text("Rahul Sharma")');
  await page.waitForURL('/board', { timeout: 15000 });
  await page.waitForSelector('header', { timeout: 10000 });
}

/**
 * Logout by clearing localStorage and navigating to login.
 */
export async function logout(page: Page) {
  await page.evaluate(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  });
  await page.goto('/login');
  await page.waitForURL('/login', { timeout: 10000 });
}

/**
 * Clear auth state before a test — run this in beforeEach to ensure clean slate.
 */
export async function clearAuth(page: Page) {
  await page.goto('/login');
  await page.evaluate(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  });
}

/**
 * Navigate to the first deal on the board and return the deal detail URL.
 * Deal cards are <button> elements with cursor-pointer inside the board.
 */
export async function navigateToFirstDeal(page: Page): Promise<string> {
  // Deal cards are buttons with cursor=pointer containing deal info (operator name, deal name, etc.)
  // They live inside main and have paragraph children with deal names
  const firstDeal = page.locator('main button[class*="cursor-pointer"]').first();
  await firstDeal.waitFor({ timeout: 15000 });
  await firstDeal.click();
  await page.waitForURL(/\/board\/\d+/, { timeout: 10000 });
  return page.url();
}

/**
 * Wait for the pipeline board to finish loading deals.
 * Works at any viewport size by waiting for deal cards OR visible phase headings.
 */
export async function waitForBoardLoaded(page: Page) {
  // Wait for either a visible deal card or a visible phase heading
  await page.locator('main button[class*="cursor-pointer"], main :text("Early")').first().waitFor({ state: 'attached', timeout: 15000 });
  // Small delay for rendering to settle
  await page.waitForTimeout(500);
}
