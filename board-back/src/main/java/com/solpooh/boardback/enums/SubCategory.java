package com.solpooh.boardback.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubCategory {
    // ===== FRONTEND (기본 기술 + 프레임워크 + 도구) =====
    // 기본 웹 기술
    HTML("HTML", MainCategory.FRONTEND),
    CSS("CSS", MainCategory.FRONTEND),
    SASS("Sass/SCSS", MainCategory.FRONTEND),
    LESS("Less", MainCategory.FRONTEND),

    // JavaScript 프레임워크/라이브러리
    REACT("React", MainCategory.FRONTEND),
    VUE("Vue.js", MainCategory.FRONTEND),
    ANGULAR("Angular", MainCategory.FRONTEND),
    SVELTE("Svelte", MainCategory.FRONTEND),
    NEXT_JS("Next.js", MainCategory.FRONTEND),
    NUXT("Nuxt.js", MainCategory.FRONTEND),
    JQUERY("jQuery", MainCategory.FRONTEND),

    // UI 프레임워크/라이브러리
    BOOTSTRAP("Bootstrap", MainCategory.FRONTEND),
    TAILWIND("Tailwind CSS", MainCategory.FRONTEND),
    MATERIAL_UI("Material UI", MainCategory.FRONTEND),
    ANT_DESIGN("Ant Design", MainCategory.FRONTEND),
    CHAKRA_UI("Chakra UI", MainCategory.FRONTEND),

    // 상태 관리
    REDUX("Redux", MainCategory.FRONTEND),
    MOBX("MobX", MainCategory.FRONTEND),
    ZUSTAND("Zustand", MainCategory.FRONTEND),
    RECOIL("Recoil", MainCategory.FRONTEND),

    // 빌드 도구
    WEBPACK("Webpack", MainCategory.FRONTEND),
    VITE("Vite", MainCategory.FRONTEND),
    PARCEL("Parcel", MainCategory.FRONTEND),
    ROLLUP("Rollup", MainCategory.FRONTEND),

    // ===== BACKEND (프레임워크 + API + 메시징) =====
    // Java/Kotlin
    SPRING("Spring/Spring Boot", MainCategory.BACKEND),
    SPRING_CLOUD("Spring Cloud", MainCategory.BACKEND),
    JPA("JPA/Hibernate", MainCategory.BACKEND),

    // Node.js
    NODE_JS("Node.js", MainCategory.BACKEND),
    EXPRESS("Express", MainCategory.BACKEND),
    NEST_JS("NestJS", MainCategory.BACKEND),
    KOA("Koa", MainCategory.BACKEND),

    // Python
    DJANGO("Django", MainCategory.BACKEND),
    FLASK("Flask", MainCategory.BACKEND),
    FASTAPI("FastAPI", MainCategory.BACKEND),

    // PHP
    LARAVEL("Laravel", MainCategory.BACKEND),
    SYMFONY("Symfony", MainCategory.BACKEND),
    CODEIGNITER("CodeIgniter", MainCategory.BACKEND),

    // Go
    GIN("Gin", MainCategory.BACKEND),
    ECHO("Echo", MainCategory.BACKEND),
    FIBER("Fiber", MainCategory.BACKEND),

    // Ruby
    RAILS("Ruby on Rails", MainCategory.BACKEND),
    SINATRA("Sinatra", MainCategory.BACKEND),

    // .NET
    ASP_NET("ASP.NET", MainCategory.BACKEND),
    DOTNET_CORE(".NET Core", MainCategory.BACKEND),

    // API/통신
    REST_API("REST API", MainCategory.BACKEND),
    GRAPHQL("GraphQL", MainCategory.BACKEND),
    GRPC("gRPC", MainCategory.BACKEND),
    WEBSOCKET("WebSocket", MainCategory.BACKEND),
    SSE("Server-Sent Events", MainCategory.BACKEND),

    // 메시징/스트리밍
    KAFKA("Kafka", MainCategory.BACKEND),
    RABBITMQ("RabbitMQ", MainCategory.BACKEND),
    ACTIVEMQ("ActiveMQ", MainCategory.BACKEND),
    NATS("NATS", MainCategory.BACKEND),

    // ===== MOBILE =====
    ANDROID("Android", MainCategory.MOBILE),
    IOS("iOS/Swift", MainCategory.MOBILE),
    REACT_NATIVE("React Native", MainCategory.MOBILE),
    FLUTTER("Flutter", MainCategory.MOBILE),
    IONIC("Ionic", MainCategory.MOBILE),
    XAMARIN("Xamarin", MainCategory.MOBILE),
    CORDOVA("Cordova", MainCategory.MOBILE),

    // ===== DATABASE (관계형 + NoSQL + 검색엔진) =====
    // 관계형 DB
    MYSQL("MySQL", MainCategory.DATABASE),
    POSTGRESQL("PostgreSQL", MainCategory.DATABASE),
    MARIADB("MariaDB", MainCategory.DATABASE),
    ORACLE("Oracle", MainCategory.DATABASE),
    MSSQL("MS SQL Server", MainCategory.DATABASE),
    SQLITE("SQLite", MainCategory.DATABASE),

    // NoSQL
    MONGODB("MongoDB", MainCategory.DATABASE),
    REDIS("Redis", MainCategory.DATABASE),
    CASSANDRA("Cassandra", MainCategory.DATABASE),
    DYNAMODB("DynamoDB", MainCategory.DATABASE),
    COUCHDB("CouchDB", MainCategory.DATABASE),
    NEO4J("Neo4j", MainCategory.DATABASE),

    // 검색 엔진
    ELASTICSEARCH("Elasticsearch", MainCategory.DATABASE),
    SOLR("Solr", MainCategory.DATABASE),

    // BaaS/클라우드 DB
    FIREBASE("Firebase", MainCategory.DATABASE),
    SUPABASE("Supabase", MainCategory.DATABASE),

    // 시계열 DB
    INFLUXDB("InfluxDB", MainCategory.DATABASE),
    TIMESCALEDB("TimescaleDB", MainCategory.DATABASE),

    // ===== DEVOPS (CI/CD + 컨테이너 + 클라우드 + 웹서버) =====
    // 컨테이너/오케스트레이션
    DOCKER("Docker", MainCategory.DEVOPS),
    KUBERNETES("Kubernetes", MainCategory.DEVOPS),
    HELM("Helm", MainCategory.DEVOPS),

    // 클라우드
    AWS("AWS", MainCategory.DEVOPS),
    GCP("Google Cloud", MainCategory.DEVOPS),
    AZURE("Azure", MainCategory.DEVOPS),
    NAVER_CLOUD("Naver Cloud", MainCategory.DEVOPS),

    // CI/CD
    GITHUB_ACTIONS("GitHub Actions", MainCategory.DEVOPS),
    GITLAB_CI("GitLab CI", MainCategory.DEVOPS),
    JENKINS("Jenkins", MainCategory.DEVOPS),
    CIRCLECI("CircleCI", MainCategory.DEVOPS),
    TRAVIS_CI("Travis CI", MainCategory.DEVOPS),
    ARGOCD("ArgoCD", MainCategory.DEVOPS),

    // IaC (Infrastructure as Code)
    TERRAFORM("Terraform", MainCategory.DEVOPS),
    ANSIBLE("Ansible", MainCategory.DEVOPS),
    CLOUDFORMATION("CloudFormation", MainCategory.DEVOPS),
    PULUMI("Pulumi", MainCategory.DEVOPS),

    // 웹서버/프록시
    NGINX("Nginx", MainCategory.DEVOPS),
    APACHE("Apache", MainCategory.DEVOPS),
    TOMCAT("Tomcat", MainCategory.DEVOPS),
    HAPROXY("HAProxy", MainCategory.DEVOPS),

    // 모니터링
    PROMETHEUS("Prometheus", MainCategory.DEVOPS),
    GRAFANA("Grafana", MainCategory.DEVOPS),
    DATADOG("Datadog", MainCategory.DEVOPS),
    ELK_STACK("ELK Stack", MainCategory.DEVOPS),

    // ===== AI/ML (프레임워크 + LLM + 도구) =====
    // 딥러닝 프레임워크
    TENSORFLOW("TensorFlow", MainCategory.AI_ML),
    PYTORCH("PyTorch", MainCategory.AI_ML),
    KERAS("Keras", MainCategory.AI_ML),
    JAX("JAX", MainCategory.AI_ML),

    // 머신러닝 라이브러리
    SCIKIT_LEARN("Scikit-learn", MainCategory.AI_ML),
    XGBOOST("XGBoost", MainCategory.AI_ML),
    LIGHTGBM("LightGBM", MainCategory.AI_ML),

    // LLM/GenAI
    OPENAI("OpenAI API", MainCategory.AI_ML),
    LANGCHAIN("LangChain", MainCategory.AI_ML),
    HUGGINGFACE("Hugging Face", MainCategory.AI_ML),
    OLLAMA("Ollama", MainCategory.AI_ML),
    CHATGPT("ChatGPT", MainCategory.AI_ML),
    CLAUDE("Claude", MainCategory.AI_ML),
    GEMINI("Gemini", MainCategory.AI_ML),
    LLAMA("LLaMA", MainCategory.AI_ML),

    // Computer Vision
    OPENCV("OpenCV", MainCategory.AI_ML),
    YOLO("YOLO", MainCategory.AI_ML),
    STABLE_DIFFUSION("Stable Diffusion", MainCategory.AI_ML),
    MEDIAPIPE("MediaPipe", MainCategory.AI_ML),

    // MLOps
    MLFLOW("MLflow", MainCategory.AI_ML),
    KUBEFLOW("Kubeflow", MainCategory.AI_ML),

    // ===== GAME (게임 엔진 + 3D 도구) =====
    // 게임 엔진
    UNITY("Unity", MainCategory.GAME),
    UNREAL("Unreal Engine", MainCategory.GAME),
    GODOT("Godot", MainCategory.GAME),
    COCOS2D("Cocos2d", MainCategory.GAME),
    PHASER("Phaser", MainCategory.GAME),
    GAMEMAKER("GameMaker", MainCategory.GAME),
    CRYENGINE("CryEngine", MainCategory.GAME),

    // 3D 모델링/애니메이션
    BLENDER("Blender", MainCategory.GAME),
    MAYA("Maya", MainCategory.GAME),
    THREEJS("Three.js", MainCategory.GAME),

    // ===== LANGUAGE (프로그래밍 언어) =====
    JAVASCRIPT("JavaScript", MainCategory.LANGUAGE),
    TYPESCRIPT("TypeScript", MainCategory.LANGUAGE),
    PYTHON("Python", MainCategory.LANGUAGE),
    JAVA("Java", MainCategory.LANGUAGE),
    KOTLIN("Kotlin", MainCategory.LANGUAGE),
    GOLANG("Go", MainCategory.LANGUAGE),
    RUST("Rust", MainCategory.LANGUAGE),
    C("C", MainCategory.LANGUAGE),
    CPP("C++", MainCategory.LANGUAGE),
    CSHARP("C#", MainCategory.LANGUAGE),
    SWIFT("Swift", MainCategory.LANGUAGE),
    RUBY("Ruby", MainCategory.LANGUAGE),
    PHP("PHP", MainCategory.LANGUAGE),
    DART("Dart", MainCategory.LANGUAGE),
    SCALA("Scala", MainCategory.LANGUAGE),
    ELIXIR("Elixir", MainCategory.LANGUAGE),
    HASKELL("Haskell", MainCategory.LANGUAGE),
    LUA("Lua", MainCategory.LANGUAGE),
    R("R", MainCategory.LANGUAGE),
    MATLAB("MATLAB", MainCategory.LANGUAGE),
    ASSEMBLY("Assembly", MainCategory.LANGUAGE),

    // ===== ALGORITHM (코딩테스트 플랫폼) =====
    LEETCODE("LeetCode", MainCategory.ALGORITHM),
    BAEKJOON("백준", MainCategory.ALGORITHM),
    PROGRAMMERS("프로그래머스", MainCategory.ALGORITHM),
    CODEFORCES("Codeforces", MainCategory.ALGORITHM),
    ATCODER("AtCoder", MainCategory.ALGORITHM),
    HACKERRANK("HackerRank", MainCategory.ALGORITHM),

    // 소분류 없음
    NONE("없음", null);

    private final String displayName;
    private final MainCategory mainCategory;

    public static SubCategory fromString(String value) {
        if (value == null || value.isEmpty()) {
            return NONE;
        }
        try {
            return SubCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
