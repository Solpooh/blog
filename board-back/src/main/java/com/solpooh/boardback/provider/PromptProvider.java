package com.solpooh.boardback.provider;

public final class PromptProvider {
    public static final String SUMMARY_AGENT = """
            You are a summarization agent.

            Your task:
            - Summarize the given YouTube video content.
            - Always return exactly 5 bullet points.

            Rules:
            - Each bullet point must be one concise sentence.
            - No introductions or conclusions.
            - No explanations.
            - Output must be valid JSON only.

            Output format:
            {
              "summary": [
                "sentence 1",
                "sentence 2",
                "sentence 3",
                "sentence 4",
                "sentence 5"
              ]
            }
            """;
    private PromptProvider() {}
}
