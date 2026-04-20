import { test, expect } from '@playwright/test';
import { loginAsManager, loginAsAgent, clearAuth } from './helpers';

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    await clearAuth(page);
  });

  test('login page shows all 6 users', async ({ page }) => {
    await page.goto('/login');
    // Wait for user buttons to load from API
    await page.waitForSelector('button:has-text("MANAGER")', { timeout: 10000 });

    // Each user is rendered as a button with a role badge
    const userButtons = page.locator('button').filter({ has: page.locator('text=/MANAGER|AGENT/') });
    const count = await userButtons.count();
    expect(count).toBe(6);
  });

  test('login page shows TurnoCRM title and Dev Mode badge', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('h1:has-text("TurnoCRM")')).toBeVisible();
    await expect(page.locator('text=Dev Mode')).toBeVisible();
    await expect(page.locator('text=Electric Bus Leasing CRM')).toBeVisible();
    await expect(page.locator('text=Restricted to Turno employees')).toBeVisible();
  });

  test('clicking user logs in and redirects to /board', async ({ page }) => {
    await loginAsManager(page);
    expect(page.url()).toContain('/board');
    // Top bar should show Pipeline Board
    await expect(page.locator('header h1:has-text("Pipeline Board")')).toBeVisible();
  });

  test('authenticated layout shows sidebar on desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsManager(page);
    // Sidebar has the TurnoCRM logo and nav links
    const sidebar = page.locator('aside');
    await expect(sidebar).toBeVisible();
    await expect(sidebar.locator('text=TurnoCRM')).toBeVisible();
    // Use getByRole with exact to avoid "Board" matching "Dashboard"
    await expect(sidebar.getByRole('link', { name: 'Board', exact: true })).toBeVisible();
    await expect(sidebar.getByRole('link', { name: 'Tasks', exact: true })).toBeVisible();
    await expect(sidebar.getByRole('link', { name: 'Operators', exact: true })).toBeVisible();
  });

  test('logout clears session and redirects to /login', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsManager(page);
    // Click logout button in sidebar (has title="Logout")
    const logoutBtn = page.locator('aside button[title="Logout"]');
    await logoutBtn.waitFor({ timeout: 10000 });
    await logoutBtn.click();
    // Should redirect to login
    await page.waitForURL('/login', { timeout: 10000 });
    await expect(page.locator('h1:has-text("TurnoCRM")')).toBeVisible();
  });

  test('unauthenticated user redirected to /login', async ({ page }) => {
    // Go directly to /board without logging in
    await page.goto('/board');
    await page.waitForURL('/login', { timeout: 10000 });
    await expect(page.locator('h1:has-text("TurnoCRM")')).toBeVisible();
  });

  test('manager sees Admin nav link; agent does not', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });

    // Login as manager
    await loginAsManager(page);
    await expect(page.locator('aside')).toBeVisible();
    await expect(page.locator('aside a:has-text("Admin Settings")')).toBeVisible();

    // Logout and login as agent
    await page.evaluate(() => {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    });
    await loginAsAgent(page);
    await expect(page.locator('aside')).toBeVisible();
    await expect(page.locator('aside a:has-text("Admin Settings")')).toHaveCount(0);
  });
});
