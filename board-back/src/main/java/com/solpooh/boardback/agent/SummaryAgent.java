package com.solpooh.boardback.agent;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SummaryAgent {
    private final OpenAIClient openAIClient;

    public String summarize(String transcript) {
        // 1. Chat Completion 파라미터 생성
        var params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage(
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
                        "[최종 요약]")

                .addUserMessage(transcript)
                .temperature(0.3)
                .build();

        // 2. Chat Completion 호출
        var completion = openAIClient.chat().completions().create(params);

        // 3. 응답 처리
        return completion.choices().stream() // 여러 choice를 가질 수 있음
                .findFirst()
                .map(choice -> choice.message().content().orElse(""))
                .orElse("");
    }
}
