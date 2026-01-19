package com.solpooh.boardback.fetcher;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
// 외부 프로세스 실행
public class TranscriptFetcher {
    public Path fetchTranscriptJson(String videoId) throws IOException, InterruptedException {
        Path outputDir = Paths.get("transcript").toAbsolutePath();
        Files.createDirectories(outputDir);

        String outputTemplate = "/downloads/%(id)s.%(ext)s";
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", outputDir + ":/downloads",
                "ghcr.io/jauderho/yt-dlp:latest",
                "--skip-download",
                "--write-auto-sub",
                "--sub-lang", "ko",
                "--sub-format", "json3",
                "-o", outputTemplate,
                "https://www.youtube.com/watch?v=" + videoId
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // stdout 소비 (deadlock 방지)
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("yt-dlp transcript fetch failed");
        }

        return outputDir.resolve(videoId + ".ko.json3");
    }
}
