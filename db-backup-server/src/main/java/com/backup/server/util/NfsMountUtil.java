package com.backup.server.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NfsMountUtil {

    public static boolean mount(String server, String remotePath, String localMountPoint, String options) {
        try {
            File mountDir = new File(localMountPoint);
            if (!mountDir.exists()) {
                boolean created = mountDir.mkdirs();
                if (!created) {
                    log.error("Failed to create mount directory: {}", localMountPoint);
                    return false;
                }
            }

            String mountCmd;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                mountCmd = String.format("mount -o %s %s:%s %s",
                        options != null ? options : " anon",
                        server, remotePath, localMountPoint);
            } else {
                String opts = options != null && !options.isEmpty() ? "-o " + options : "";
                mountCmd = String.format("mount -t nfs %s %s:%s %s", opts, server, remotePath, localMountPoint);
            }

            log.info("Executing mount command: {}", mountCmd);
            Process process = Runtime.getRuntime().exec(mountCmd);
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("Mount command timeout");
                return false;
            }

            if (process.exitValue() != 0) {
                String error = readStream(process.getErrorStream());
                log.error("Mount failed: {}", error);
                return false;
            }

            log.info("NFS mounted successfully: {} -> {}", server + ":" + remotePath, localMountPoint);
            return true;
        } catch (Exception e) {
            log.error("Mount exception", e);
            return false;
        }
    }

    public static boolean unmount(String localMountPoint) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String umountCmd = os.contains("win") ? "umount " + localMountPoint : "umount " + localMountPoint;

            log.info("Executing unmount command: {}", umountCmd);
            Process process = Runtime.getRuntime().exec(umountCmd);
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;
        } catch (Exception e) {
            log.error("Unmount exception", e);
            return false;
        }
    }

    public static boolean isMounted(String localMountPoint) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("mount");
            } else {
                process = Runtime.getRuntime().exec("mountpoint -q " + localMountPoint);
            }
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            log.error("Check mount status exception", e);
            return false;
        }
    }

    private static String readStream(java.io.InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception ignored) {
        }
        return sb.toString();
    }
}
