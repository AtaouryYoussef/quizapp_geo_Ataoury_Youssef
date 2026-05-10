# ARCHITECTURE.md — quizapp_geo_Ataoury_Youssef

> Fichier de contexte architecture pour GitHub Copilot.
> Ce fichier décrit l'intégralité de la structure, des conventions et des responsabilités du projet.

---

## 1. Vue d'ensemble du projet

**Nom du projet** : `quizapp_geo_Ataoury_Youssef`
**Plateforme** : Android (API 24+ / Android 7.0 minimum)
**Langage** : Java (pas Kotlin)
**IDE** : Android Studio
**Architecture** : MVVM (Model-View-ViewModel) + Repository Pattern
**Backend** : Firebase (Auth + Firestore)
**IA** : API Gemini (Google AI Studio — plan gratuit)
**Localisation** : GPS Android via FusedLocationProviderClient + Geocoder

---

## 2. Architecture globale : MVVM + Repository

```
┌─────────────────────────────────────────────────────┐
│                     UI LAYER                        │
│  Activities / Fragments → observent les LiveData    │
│  Ne contiennent AUCUNE logique métier               │
└────────────────────┬────────────────────────────────┘
                     │ observe / appelle
┌────────────────────▼────────────────────────────────┐
│                 VIEWMODEL LAYER                     │
│  Gère l'état UI, appelle les Repositories          │
│  Survit aux rotations d'écran (lifecycle-aware)    │
└────────────────────┬────────────────────────────────┘
                     │ appelle
┌────────────────────▼────────────────────────────────┐
│               REPOSITORY LAYER                      │
│  Source unique de vérité — orchestre les données   │
│  Combine Firebase + API IA + GPS                   │
└──────┬──────────────────────┬───────────────────────┘
       │                      │
┌──────▼──────┐     ┌─────────▼──────────────┐
│  Firebase   │     │     AI / Location       │
│  Auth       │     │  GeminiApiService       │
│  Firestore  │     │  LocationService        │
└─────────────┘     └────────────────────────┘
```

---

## 3. Structure des packages Java

```
com.ataoury.youssef.quizappgeo/
│
├── ui/
│   ├── auth/
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   └── AuthViewModel.java
│   │
│   ├── quiz/
│   │   ├── QuizActivity.java
│   │   ├── QuizViewModel.java
│   │   └── ResultActivity.java
│   │
│   ├── history/
│   │   ├── HistoryActivity.java
│   │   ├── HistoryViewModel.java
│   │   └── HistoryAdapter.java       ← RecyclerView adapter
│   │
│   └── home/
│       ├── HomeActivity.java
│       └── HomeViewModel.java
│
├── repository/
│   ├── AuthRepository.java           ← Firebase Auth logic
│   ├── QuizRepository.java           ← Firestore quiz save/load
│   └── AiRepository.java             ← Appels API Gemini
│
├── model/
│   ├── User.java                     ← Modèle utilisateur
│   ├── Question.java                 ← Une question du quiz
│   ├── QuizSession.java              ← Session complète du quiz
│   └── QuizResult.java               ← Résultat final
│
├── service/
│   ├── GeminiApiService.java         ← Client HTTP Gemini (OkHttp/Retrofit)
│   └── LocationService.java          ← FusedLocationProviderClient wrapper
│
├── utils/
│   ├── Constants.java                ← Constantes globales (clés, URLs, etc.)
│   ├── JsonParser.java               ← Parse la réponse JSON de l'IA
│   ├── NetworkUtils.java             ← Vérifie la connectivité
│   └── SessionManager.java           ← Gestion session SharedPreferences
│
└── QuizApplication.java              ← Application class (init Firebase, etc.)
```

---

## 4. Structure des fichiers XML (res/)

```
res/
├── layout/
│   ├── activity_login.xml
│   ├── activity_register.xml
│   ├── activity_home.xml
│   ├── activity_quiz.xml
│   ├── activity_result.xml
│   ├── activity_history.xml
│   └── item_history.xml             ← Item du RecyclerView historique
│
├── values/
│   ├── strings.xml                  ← Toutes les chaînes en français/anglais
│   ├── colors.xml                   ← Palette couleurs de l'app
│   ├── themes.xml                   ← Thème Material Design 3
│   └── dimens.xml                   ← Marges, tailles de police
│
├── drawable/
│   ├── bg_gradient.xml              ← Fond dégradé
│   ├── btn_primary.xml              ← Bouton principal stylisé
│   └── ic_*.xml                     ← Icônes vectorielles
│
└── navigation/
    └── nav_graph.xml                ← Navigation component (optionnel)
```

---

## 5. Flux de données principal

```
[App Start]
    │
    ▼
[HomeActivity]
    │── vérifie session Firebase (AuthRepository)
    │── si non connecté → LoginActivity
    │── si connecté → demande permissions GPS
    │
    ▼
[LocationService.getCurrentCity()]
    │── FusedLocationProviderClient → lat/lng
    │── Geocoder → nom de la ville
    │
    ▼
[AiRepository.generateQuiz(cityName)]
    │── GeminiApiService → POST /v1beta/models/gemini-pro:generateContent
    │── Prompt structuré → réponse JSON
    │── JsonParser → List<Question>
    │
    ▼
[QuizActivity]
    │── affiche 10 questions une par une
    │── enregistre réponses utilisateur
    │── calcule le score
    │
    ▼
[ResultActivity]
    │── affiche score final
    │── QuizRepository.saveSession() → Firestore
    │
    ▼
[HistoryActivity]
    │── QuizRepository.getUserHistory() → Firestore
    │── affiche RecyclerView des sessions passées
```

---

## 6. Modèles de données (Java + Firestore)

### Question.java
```java
public class Question {
    private String questionText;
    private List<String> choices;      // 4 choix
    private int correctAnswerIndex;    // 0-3
    private String selectedAnswer;     // réponse choisie par l'user
}
```

### QuizSession.java (= document Firestore)
```java
public class QuizSession {
    private String sessionId;          // auto-généré
    private String userId;             // Firebase UID
    private String cityName;
    private int score;                 // sur 10
    private long timestamp;            // System.currentTimeMillis()
    private List<Question> questions;  // les 10 questions + réponses
}
```

### User.java
```java
public class User {
    private String uid;
    private String email;
    private String displayName;
    private long createdAt;
}
```

---

## 7. Structure Firestore

```
Firestore
├── users/
│   └── {userId}/
│       ├── email: "..."
│       ├── displayName: "..."
│       └── createdAt: timestamp
│
└── quizSessions/
    └── {sessionId}/
        ├── userId: "..."
        ├── cityName: "Marrakech"
        ├── score: 7
        ├── timestamp: 1234567890
        └── questions: [ { questionText, choices, correctAnswerIndex, selectedAnswer }, ... ]
```

---

## 8. Prompt Gemini — Format attendu

**Prompt envoyé à Gemini :**
```
Tu es un expert en tourisme et culture mondiale.
Génère exactement 10 questions de quiz sur la ville de {CITY_NAME}.
Les questions doivent couvrir : tourisme, histoire, culture, monuments, spécialités locales, géographie.
Retourne UNIQUEMENT un tableau JSON valide, sans texte supplémentaire, dans ce format exact :
[
  {
    "question": "Quelle est la principale attraction touristique de {CITY_NAME} ?",
    "choices": ["Option A", "Option B", "Option C", "Option D"],
    "correctIndex": 0
  },
  ...
]
```

---

## 9. Dépendances Gradle (build.gradle app)

```groovy
dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.android.gms:play-services-auth:21.0.0'

    // Location
    implementation 'com.google.android.gms:play-services-location:21.2.0'

    // HTTP Client (Gemini API)
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

    // ViewModel + LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'

    // UI
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'

    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

---

## 10. Conventions de code

| Élément | Convention |
|---|---|
| Classes | PascalCase → `QuizViewModel` |
| Méthodes | camelCase → `generateQuiz()` |
| Variables | camelCase → `cityName` |
| Constantes | UPPER_SNAKE → `GEMINI_API_URL` |
| Layouts XML | snake_case → `activity_quiz.xml` |
| IDs XML | camelCase → `@+id/btnStart` |
| Commentaires | Français ou Anglais (cohérent) |

---

## 11. Règles importantes pour Copilot

1. **Toujours utiliser MVVM** — aucune logique métier dans les Activities
2. **LiveData** pour tous les états UI observés
3. **Repository** comme seule source d'accès aux données externes
4. **Callbacks Firebase** gérés dans les Repositories, pas dans les ViewModels
5. **Permissions runtime** demandées avant tout accès GPS ou Réseau
6. **Clé API Gemini** stockée dans `local.properties` (jamais dans le code source)
7. **Gestion des erreurs** : toujours exposer un LiveData d'erreur dans les ViewModels
8. **SharedPreferences** via `SessionManager` uniquement (pas d'accès direct)
9. **Pas de logique dans les Adapters RecyclerView** — juste du binding
10. **Toutes les opérations réseau/Firebase** en arrière-plan (thread non-UI)
