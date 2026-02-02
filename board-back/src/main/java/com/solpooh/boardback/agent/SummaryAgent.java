package com.solpooh.boardback.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.solpooh.boardback.dto.common.TranscriptAnalysisResult;
import com.solpooh.boardback.enums.MainCategory;
import com.solpooh.boardback.enums.SubCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryAgent {
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    private static final String SYSTEM_PROMPT = "" +
            "너는 영상 자막을 분석해서 영상 내용을 요약하는 AI야. " +
            "마크다운 형식(#, *, ** 등)을 사용하지 말고 일반 텍스트로 작성해줘. " +
            "다음 형식으로 요약해줘:\n\n" +
            "1. 영상 주제와 목적\n" +
            "[주제와 목적 설명]\n\n" +
            "2. 핵심 내용\n" +
            "- [첫 번째 핵심 내용]\n" +
            "- [두 번째 핵심 내용]\n" +
            "- [추가 핵심 내용들...]\n\n" +
            "3. 요점 정리\n" +
            "[최종 요약]";

    private static final String COMBINED_SYSTEM_PROMPT = """
            너는 개발자 대상 YouTube 영상의 자막을 분석하는 AI다.
            자막 내용을 기반으로 1) 영상 요약, 2) 카테고리 분류를 동시에 수행해줘.

            [요약 작성 규칙]
            마크다운 형식(#, *, ** 등)을 사용하지 말고 일반 텍스트로 작성해줘.
            추상적인 표현 대신 기술 중심으로 작성하고, 
            다음 형식으로 요약해줘:

            1. 영상 주제와 목적
            이 영상이 다루는 기술/개념과,
            왜 이 주제가 중요한지 한 번에 설명하라.

            2. 핵심 내용
            - 개념 정의: 영상에서 설명하는 핵심 개념은 무엇인가
            - 등장 배경/문제: 기존 방식의 한계 또는 문제점
            - 비교 또는 특징: 대안 기술과의 차이점 또는 장단점
            - 실무 포인트: 실제 개발/운영에서 고려해야 할 점

            3. 요점 정리
            이 영상을 통해 개발자가 반드시 기억해야 할 핵심 한 가지를 정리하라.

            [카테고리 분류 규칙]
            1. 대분류(mainCategory) 1개 필수 선택
            2. 소분류(subCategory)는 해당되는 경우만 선택 (없으면 "NONE")
            3. 여러 기술이 섞여있으면 "가장 핵심적인" 기술 선택
            4. 영어 대문자로만 반환 (예: "FRONTEND", "REACT")

            [대분류]
            - FRONTEND: HTML, CSS, JavaScript, React, Vue, Angular, UI/UX
            - BACKEND: Spring, Node.js, Django, FastAPI, REST API, GraphQL
            - MOBILE: Android, iOS, React Native, Flutter 앱 개발
            - DATABASE: MySQL, MongoDB, Redis, PostgreSQL, SQL
            - DEVOPS: Docker, Kubernetes, CI/CD, AWS, GCP, Azure
            - AI_ML: TensorFlow, PyTorch, LangChain, 머신러닝, 딥러닝, LLM
            - GAME: Unity, Unreal Engine, Godot, 게임 개발
            - LANGUAGE: Java, Python, JavaScript, Go, Rust 등 언어 자체
            - ALGORITHM: 알고리즘, 자료구조, 코딩테스트, LeetCode, 백준
            - SECURITY: 웹 보안, 네트워크 보안, 해킹, 보안 취약점
            - CAREER: 취업, 이직, 면접, 코딩테스트 준비, 개발자 커리어
            - TUTORIAL: 입문 강좌, 초보자 튜토리얼, 강의
            - NEWS: 기술 뉴스, 트렌드, 컨퍼런스, 업계 동향
            - ETC: 위 카테고리에 명확히 속하지 않는 경우

            [소분류] (해당되는 경우만)
            FRONTEND: HTML, CSS, SASS, REACT, VUE, ANGULAR, SVELTE, NEXT_JS, NUXT, JQUERY,
                      BOOTSTRAP, TAILWIND, REDUX, MOBX, WEBPACK, VITE
            BACKEND: SPRING, SPRING_CLOUD, JPA, NODE_JS, EXPRESS, NEST_JS, DJANGO, FLASK, FASTAPI,
                     LARAVEL, SYMFONY, GIN, RAILS, ASP_NET, DOTNET_CORE,
                     REST_API, GRAPHQL, GRPC, WEBSOCKET, KAFKA, RABBITMQ
            MOBILE: ANDROID, IOS, REACT_NATIVE, FLUTTER, IONIC, XAMARIN
            DATABASE: MYSQL, POSTGRESQL, MARIADB, MONGODB, REDIS, CASSANDRA, ELASTICSEARCH, SOLR,
                      FIREBASE, SUPABASE, ORACLE, MSSQL, SQLITE, DYNAMODB, NEO4J
            DEVOPS: DOCKER, KUBERNETES, AWS, GCP, AZURE, GITHUB_ACTIONS, GITLAB_CI, JENKINS,
                    TERRAFORM, ANSIBLE, NGINX, APACHE, TOMCAT, PROMETHEUS, GRAFANA, ELK_STACK
            AI_ML: TENSORFLOW, PYTORCH, KERAS, SCIKIT_LEARN, XGBOOST, LANGCHAIN, OPENAI, CHATGPT,
                   CLAUDE, GEMINI, HUGGINGFACE, OLLAMA, LLAMA, YOLO, OPENCV, STABLE_DIFFUSION
            GAME: UNITY, UNREAL, GODOT, COCOS2D, PHASER, BLENDER, MAYA, THREEJS
            LANGUAGE: JAVASCRIPT, TYPESCRIPT, PYTHON, JAVA, KOTLIN, GOLANG, RUST, C, CPP, CSHARP,
                      SWIFT, RUBY, PHP, DART, SCALA, ELIXIR, HASKELL, LUA, R
            ALGORITHM: LEETCODE, BAEKJOON, PROGRAMMERS, CODEFORCES, ATCODER, HACKERRANK
            기타: NONE

            [응답 형식 - JSON만 반환]
            {
              "summary": "1. 영상 주제와 목적\\n[주제와 목적 설명]\\n\\n2. 핵심 내용\\n- [첫 번째 핵심 내용]\\n- [두 번째 핵심 내용]\\n- [추가 핵심 내용들...]\\n\\n3. 요점 정리\\n[최종 요약]",
              "mainCategory": "BACKEND",
              "subCategory": "SPRING"
            }

            주의: 반드시 JSON 형식으로만 응답하고, 마크다운 코드 블록은 사용하지 마.
            """;

    /**
     * 기존 메서드 (하위 호환성 유지)
     */
    public String summarizeVideo(String transcript) {
        var params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage(SYSTEM_PROMPT)
                .addUserMessage(transcript)
                .temperature(0.3)
                .build();

        var completion = openAIClient.chat().completions().create(params);

        return completion.choices().stream()
                .findFirst()
                .map(choice -> choice.message().content().orElse(""))
                .orElse("");
    }

    /**
     * 요약 + 카테고리 분류 동시 수행 (신규)
     */
    public TranscriptAnalysisResult summarizeAndCategorize(String transcript) {
        try {
            // 1. Chat Completion 파라미터 생성
            var params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4O_MINI)
                    .addSystemMessage(COMBINED_SYSTEM_PROMPT)
                    .addUserMessage(transcript)
                    .temperature(0.3)
                    .build();

            // 2. Chat Completion 호출
            var completion = openAIClient.chat().completions().create(params);

            // 3. 응답 처리
            String content = completion.choices().stream()
                    .findFirst()
                    .map(choice -> choice.message().content().orElse(""))
                    .orElse("");

            // 4. JSON 파싱
            return parseResponse(content);

        } catch (Exception e) {
            log.error("Transcript 분석 실패", e);
            // 실패 시 요약만 반환 (카테고리는 ETC-NONE)
            return TranscriptAnalysisResult.ofSummaryOnly("요약 생성 실패");
        }
    }

    /**
     * OpenAI 응답 파싱
     */
    private TranscriptAnalysisResult parseResponse(String content) {
        try {
            // JSON 추출 (마크다운 코드 블록 제거)
            String jsonContent = content.trim();
            if (jsonContent.startsWith("```json")) {
                jsonContent = jsonContent.substring(7);
            }
            if (jsonContent.startsWith("```")) {
                jsonContent = jsonContent.substring(3);
            }
            if (jsonContent.endsWith("```")) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 3);
            }
            jsonContent = jsonContent.trim();

            JsonNode node = objectMapper.readTree(jsonContent);

            String summary = node.get("summary").asText();
            String mainCategoryStr = node.get("mainCategory").asText();
            String subCategoryStr = node.has("subCategory") ? node.get("subCategory").asText() : "NONE";

            MainCategory mainCategory = MainCategory.valueOf(mainCategoryStr);
            SubCategory subCategory = SubCategory.fromString(subCategoryStr);

            return TranscriptAnalysisResult.builder()
                    .summary(summary)
                    .mainCategory(mainCategory)
                    .subCategory(subCategory)
                    .build();

        } catch (Exception e) {
            log.error("응답 파싱 실패: content={}", content, e);
            return TranscriptAnalysisResult.ofSummaryOnly(content);
        }
    }
}
