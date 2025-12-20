package com.agricultura.sistema.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final String DB_FILENAME = "gestao.db";
    private static final String BACKUP_DIR = "backups";
    private static final int MAX_BACKUPS = 10;

    @EventListener(ApplicationReadyEvent.class)
    public void realizarBackupAoIniciar() {
        try {
            ensureBackupDirectoryExists();

            File sourceFile = new File(DB_FILENAME);
            if (!sourceFile.exists()) {
                logger.warn("Arquivo de banco de dados não encontrado para backup: {}", DB_FILENAME);
                return;
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupFilename = "backup_" + timestamp + ".db";
            Path targetPath = Paths.get(BACKUP_DIR, backupFilename);

            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            manageBackupRotation();

            // Re-counting files for the log message
            File backupDir = new File(BACKUP_DIR);
            int count = 0;
            if (backupDir.exists() && backupDir.isDirectory()) {
                File[] files = backupDir.listFiles();
                if (files != null) {
                    count = files.length;
                }
            }

            logger.info("Backup automático realizado: {}. Backups mantidos: {}", backupFilename, count);

        } catch (IOException e) {
            logger.error("Falha ao realizar backup automático", e);
        }
    }

    public void restaurarBackup(File arquivoBackup) {
        if (arquivoBackup == null || !arquivoBackup.exists()) {
            throw new IllegalArgumentException("Arquivo de backup inválido.");
        }

        try {
            Path dbPath = Paths.get(DB_FILENAME);
            // Copia o backup sobrescrevendo o banco atual
            Files.copy(arquivoBackup.toPath(), dbPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Restauração de backup realizada com sucesso a partir de: {}", arquivoBackup.getName());
        } catch (IOException e) {
            logger.error("Falha ao restaurar backup", e);
            throw new RuntimeException("Falha ao restaurar backup: " + e.getMessage(), e);
        }
    }

    private void ensureBackupDirectoryExists() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            if (backupDir.mkdirs()) {
                logger.info("Diretório de backups criado: {}", BACKUP_DIR);
            } else {
                logger.error("Falha ao criar diretório de backups: {}", BACKUP_DIR);
            }
        }
    }

    private void manageBackupRotation() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return;
        }

        File[] files = backupDir.listFiles();
        if (files == null || files.length <= MAX_BACKUPS) {
            return;
        }

        // Ordenar por data de modificação (mais antigos primeiro)
        // Nota: Nome do arquivo contém timestamp, mas lastModified deve ser suficiente
        // e seguro.
        // Se quiser garantir pelo nome, teria que fazer parsing, mas lastModified no
        // sistema de arquivos deve bater com a criação.
        // O prompt pede: "Ordene-os por data (mais antigos primeiro)"
        Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified))
                .limit(files.length - MAX_BACKUPS)
                .forEach(file -> {
                    if (file.delete()) {
                        logger.info("Backup antigo removido pela rotação: {}", file.getName());
                    } else {
                        logger.warn("Falha ao remover backup antigo: {}", file.getName());
                    }
                });
    }
}
