import { test, expect } from '@playwright/test';

test.describe('Página de Login', () => {
  test('deve carregar a página de login corretamente e validar elementos visuais', async ({ page }) => {
    // Acessar a página inicial (que deve redirecionar ou abrir direto o login se configurado, 
    // mas vamos direto para /login)
    await page.goto('/login');

    // Verifica se o título da página ou logo TaskFlow está presente
    await expect(page.getByRole('heading', { name: /TaskFlow/i })).toBeVisible();
    await expect(page.getByText('Acesse seu workspace')).toBeVisible();

    // Verifica a presença dos campos de e-mail e senha
    const emailInput = page.getByLabel('Email Corporativo');
    await expect(emailInput).toBeVisible();
    await expect(emailInput).toBeEnabled();

    const passwordInput = page.getByLabel('Senha');
    await expect(passwordInput).toBeVisible();
    await expect(passwordInput).toBeEnabled();

    // Verifica o botão de envio
    const loginButton = page.getByRole('button', { name: /Acessar Workspace/i });
    await expect(loginButton).toBeVisible();
    await expect(loginButton).toBeEnabled();
  });
});
