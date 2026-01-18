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
                                "첫 번째로는 영상 주제와 목적에 대해 설명해줘. " +
                        "두 번째로는 핵심 내용을 가시성있게 요약해줘." +
                                "마지막으로 최종 요점을 정리해줘.")

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
