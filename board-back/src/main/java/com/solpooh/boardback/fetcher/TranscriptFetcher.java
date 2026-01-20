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

@Slf4j
@Component
// 외부 프로세스 실행
public class TranscriptFetcher {

    @Value("${youtube.cookies.path:#{null}}")
    private String cookiesPath;

    public Path fetchTranscriptJson(String videoId) throws IOException, InterruptedException {
        Path outputDir = Paths.get("transcript").toAbsolutePath();
        Files.createDirectories(outputDir);

        String outputTemplate = "/downloads/%(id)s.%(ext)s";

        // Docker 명령어 구성
        List<String> command = new ArrayList<>(List.of(
                "docker", "run", "--rm",
                "-v", outputDir + ":/downloads"
        ));

        // 쿠키 파일이 설정되어 있으면 볼륨 마운트 추가
        if (cookiesPath != null && !cookiesPath.isBlank()) {
            Path cookiesFile = Paths.get(cookiesPath).toAbsolutePath();
            if (Files.exists(cookiesFile)) {
                command.add("-v");
                command.add(cookiesFile + ":/cookies.txt");
                log.info("YouTube 쿠키 파일 사용: {}", cookiesFile);
            } else {
                log.warn("쿠키 파일이 존재하지 않음: {}", cookiesFile);
            }
        }

        // yt-dlp 이미지 및 옵션
        command.add("ghcr.io/jauderho/yt-dlp:latest");
        command.add("--skip-download");
        command.add("--write-auto-sub");
        command.add("--sub-lang");
        command.add("ko");
        command.add("--sub-format");
        command.add("json3");

        // 쿠키 파일 옵션 추가
        if (cookiesPath != null && !cookiesPath.isBlank() && Files.exists(Paths.get(cookiesPath))) {
            command.add("--cookies");
            command.add("/cookies.txt");
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

        int exitCode = process.waitFor();
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
