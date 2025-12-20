package com.agricultura.sistema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AppLauncher {

    public static void main(String[] args) {
        // --- ZONA DE PERIGO (Antes do Spring) ---
        verificarERestaurarBackup();
        // ----------------------------------------

        // Inicia o sistema normalmente
        SistemaApplication.main(args);
    }

    private static void verificarERestaurarBackup() {
        File arquivoGatilho = new File("restaurar.db");
        File bancoOficial = new File("gestao.db");

        // Se existe um arquivo pedindo para ser restaurado
        if (arquivoGatilho.exists()) {
            System.out.println("🔄 RESTORE DETECTADO: Iniciando processo de restauração...");
            try {
                // 1. (Opcional) Fazer backup de segurança do atual antes de matar
                if (bancoOficial.exists()) {
                    File backupSeguranca = new File("gestao_old_" + System.currentTimeMillis() + ".db");
                    Files.move(bancoOficial.toPath(), backupSeguranca.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("   -> Banco atual salvo como: " + backupSeguranca.getName());
                }

                // 2. Promover o 'restaurar.db' para 'gestao.db'
                Files.move(arquivoGatilho.toPath(), bancoOficial.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ SUCESSO: Banco de dados restaurado!");

            } catch (IOException e) {
                System.err.println("❌ ERRO CRÍTICO AO RESTAURAR: " + e.getMessage());
                e.printStackTrace();
                // Aqui você poderia abrir um JOptionPane simples avisando o erro, se quiser
            }
        }
    }
}