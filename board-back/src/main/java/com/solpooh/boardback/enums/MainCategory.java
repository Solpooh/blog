package com.solpooh.boardback.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MainCategory {
    FRONTEND("프론트엔드", "HTML, CSS, JavaScript, React, Vue, Angular, UI/UX"),
    BACKEND("백엔드", "Spring, Node.js, Django, FastAPI, REST API, GraphQL"),
    MOBILE("모바일", "Android, iOS, React Native, Flutter"),
    DATABASE("데이터베이스", "MySQL, MongoDB, Redis, PostgreSQL, SQL"),
    DEVOPS("데브옵스/인프라", "Docker, Kubernetes, CI/CD, AWS, GCP, Azure"),
    AI_ML("AI/머신러닝", "TensorFlow, PyTorch, LangChain, 머신러닝, 딥러닝, LLM"),
    GAME("게임 개발", "Unity, Unreal Engine, Godot"),
    LANGUAGE("프로그래밍 언어", "Java, Python, JavaScript, Go, Rust"),
    ALGORITHM("알고리즘/자료구조", "알고리즘, 자료구조, 코딩테스트"),
    SECURITY("보안", "웹 보안, 네트워크 보안, 해킹, 보안 취약점"),
    CAREER("커리어/취업", "취업, 이직, 면접, 개발자 커리어"),
    TUTORIAL("튜토리얼/강의", "입문 강좌, 초보자 튜토리얼, 강의"),
    NEWS("뉴스/트렌드", "기술 뉴스, 트렌드, 컨퍼런스, 업계 동향"),
    ETC("기타", "기타 개발 관련 콘텐츠");

    private final String displayName;
    private final String description;
}
