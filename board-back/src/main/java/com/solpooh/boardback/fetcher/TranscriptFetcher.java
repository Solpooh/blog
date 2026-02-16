package com.solpooh.boardback.fetcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
// 외부 프로세스 실행
public class TranscriptFetcher {

    @Value("${youtube.cookies.path:#{null}}")
    private String cookiesPath;

    public Path fetchTranscriptJson(String videoId) throws IOException, InterruptedException {
        Path outputDir = Paths.get("transcript").toAbsolutePath();
        Files.createDirectories(outputDir);

        String outputTemplate = outputDir + "/%(id)s.%(ext)s";

        // yt-dlp 명령어 직접 실행 (Docker 불필요)
        List<String> command = new ArrayList<>(List.of(
                "yt-dlp",
                "--skip-download",
                "--write-auto-sub",
                "--sub-lang", "ko",
                "--sub-format", "json3",
                "--js-runtimes", "node"
        ));

        // 쿠키 파일이 설정되어 있으면 옵션 추가
        if (cookiesPath != null && !cookiesPath.isBlank()) {
            Path cookiesFile = Paths.get(cookiesPath).toAbsolutePath();
            if (Files.exists(cookiesFile)) {
                command.add("--cookies");
                command.add(cookiesFile.toString());
                log.info("YouTube 쿠키 파일 사용: {}", cookiesFile);
            } else {
                log.warn("쿠키 파일이 존재하지 않음: {}", cookiesFile);
            }
        }

        command.add("-o");
        command.add(outputTemplate);
        command.add("https://www.youtube.com/watch?v=" + videoId);

        // 실행 명령어 전체 로깅
        log.info("yt-dlp 실행 명령어: {}", String.join(" ", command));
        log.debug("출력 디렉토리: {}", outputDir);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // stdout/stderr 수집 및 로깅
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("yt-dlp output: {}", line);
            }
        }

        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            String errorMsg = String.format(
                    "yt-dlp timed out after 60s (videoId=%s)%nOutput:%n%s",
                    videoId, output.toString()
            );
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String errorMsg = String.format(
                    "yt-dlp transcript fetch failed (exitCode=%d, videoId=%s)%nOutput:%n%s",
                    exitCode, videoId, output.toString()
            );
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        Path resultPath = outputDir.resolve(videoId + ".ko.json3");
        log.info("yt-dlp 자막 다운로드 성공 - videoId: {}, path: {}", videoId, resultPath);
        return resultPath;
    }
}
