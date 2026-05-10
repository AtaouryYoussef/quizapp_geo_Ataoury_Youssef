# SKILL.md — quizapp_geo_Ataoury_Youssef

> Ce fichier est le guide de compétences pour GitHub Copilot.
> Il contient tous les prompts optimisés, les règles de génération de code,
> et les bonnes pratiques pour ce projet Android Java.

---

## Identité du projet

- **App** : Quiz géolocalisé — génération de questions par IA selon la ville GPS
- **Développeur** : Ataoury Youssef
- **Stack** : Android Java, Firebase Auth, Firestore, API Gemini, GPS Android
- **Architecture** : MVVM + Repository Pattern
- **Langue de l'interface** : Français
- **Lire aussi** : `ARCHITECTURE.md` pour la structure complète

---

## Règles de génération Copilot

Quand tu génères du code pour ce projet, respecte TOUJOURS ces règles :

```
1. Utilise Java (jamais Kotlin)
2. Architecture MVVM — les Activities n'ont pas de logique métier
3. LiveData<T> pour exposer les états depuis ViewModel
4. Repository pattern pour accéder à Firebase et à l'API Gemini
5. Gère toujours les erreurs avec un LiveData<String> errorMessage
6. Utilise ExecutorService ou Firebase callbacks pour les opérations async
7. Ne stocke jamais la clé API Gemini dans le code source
8. Respecte le naming défini dans ARCHITECTURE.md
9. Commente le code en français
10. Génère toujours le layout XML correspondant à chaque Activity
```

---

## Prompts optimisés par fonctionnalité

### PROMPT 1 — Authentification Firebase (LoginActivity)

```
Génère pour un projet Android Java avec architecture MVVM :
- LoginActivity.java : Activity qui observe AuthViewModel, affiche email/password + bouton Google Sign-In
- AuthViewModel.java : ViewModel qui expose LiveData<FirebaseUser> currentUser et LiveData<String> errorMessage
- AuthRepository.java : Repository qui encapsule Firebase Auth (signInWithEmailAndPassword + Google Sign-In)
- activity_login.xml : Layout Material Design 3 avec TextInputLayout email/password, bouton de connexion, bouton Google, lien vers inscription
Règles : MVVM strict, aucune logique Firebase dans l'Activity, gestion des erreurs via LiveData.
Package : com.ataoury.youssef.quizappgeo
```

---

### PROMPT 2 — Inscription Firebase (RegisterActivity)

```
Génère pour un projet Android Java MVVM :
- RegisterActivity.java : Activity qui observe AuthViewModel pour l'inscription
- Dans AuthRepository.java, ajoute : createUserWithEmailAndPassword() + saveUserToFirestore()
- activity_register.xml : Layout avec champs nom, email, mot de passe, confirmation mot de passe
- Après inscription réussie → créer le document utilisateur dans Firestore (collection "users")
Règles : validation des champs côté UI (email valide, passwords identiques, longueur min), erreurs via LiveData.
Package : com.ataoury.youssef.quizappgeo
```

---

### PROMPT 3 — Service de Géolocalisation

```
based on skill.md and architecture.md 
Génère pour Android Java :
- LocationService.java : classe utilitaire qui utilise FusedLocationProviderClient
  - méthode getCurrentLocation(Context ctx, LocationCallback callback)
  - méthode getCityFromLocation(Context ctx, double lat, double lng) → String cityName via Geocoder
- Gère les cas : permission refusée, GPS désactivé, ville non trouvée (fallback sur coordonnées)
- Permissions à vérifier : ACCESS_FINE_LOCATION et ACCESS_COARSE_LOCATION
Package : com.ataoury.youssef.quizappgeo.service
```

---

### PROMPT 4 — Appel API Gemini pour générer le quiz

```
based on skill.md and architecture.md 
Génère pour Android Java avec OkHttp :
- GeminiApiService.java : client HTTP qui appelle l'API REST Gemini (POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent)
  - méthode generateQuizQuestions(String cityName, GeminiCallback callback)
  - Construit le prompt suivant : "Génère 10 questions de quiz en JSON sur la ville de {cityName}. Format : [{question, choices:[4 options], correctIndex}]. Retourne UNIQUEMENT le JSON."
  - Parse la réponse pour extraire le tableau JSON
- AiRepository.java : Repository qui appelle GeminiApiService et retourne LiveData<List<Question>>
- JsonParser.java : utilitaire qui parse le JSON Gemini en List<Question>
- La clé API est lue depuis BuildConfig.GEMINI_API_KEY (défini dans local.properties)
Package : com.ataoury.youssef.quizappgeo.service
```

---

### PROMPT 5 — HomeActivity (écran principal)

```
based on skill.md and architecture.md 
Génère pour Android Java MVVM :
- HomeActivity.java : écran d'accueil après connexion
  - affiche le nom de l'utilisateur connecté
  - bouton "Commencer le Quiz" → demande permission GPS → détecte la ville → lance QuizActivity
  - bouton "Mon Historique" → lance HistoryActivity
  - bouton "Déconnexion" → Firebase signOut → redirige vers LoginActivity
- HomeViewModel.java : expose LiveData<String> cityName, LiveData<FirebaseUser> currentUser
- activity_home.xml : layout Material Design avec CardView, icônes, animations
Package : com.ataoury.youssef.quizappgeo.ui.home
```

---

### PROMPT 6 — QuizActivity et QuizViewModel

```
based on skill.md and architecture.md 
Génère pour Android Java MVVM :
- QuizActivity.java :
  - reçoit cityName en Intent extra
  - observe QuizViewModel : LiveData<Question> currentQuestion, LiveData<Integer> questionIndex, LiveData<Boolean> quizFinished
  - affiche la question + 4 boutons de réponse
  - marque la bonne/mauvaise réponse visuellement (vert/rouge)
  - passe à la question suivante après 1 seconde
  - à la fin → lance ResultActivity avec le score
- QuizViewModel.java :
  - charge les questions via AiRepository
  - méthodes : loadQuestions(cityName), answerQuestion(int selectedIndex), nextQuestion()
  - expose LiveData<List<Question>> questions, LiveData<Integer> score, LiveData<Boolean> isLoading
- activity_quiz.xml : TextView question, 4 MaterialButton réponses, ProgressBar, indicateur question X/10
Package : com.ataoury.youssef.quizappgeo.ui.quiz
```

---

### PROMPT 7 — ResultActivity

```
based on skill.md and architecture.md 
Génère pour Android Java :
- ResultActivity.java :
  - reçoit score (int) et cityName (String) en Intent extras
  - affiche le score sur 10 avec un message dynamique (Excellent/Bien/À améliorer)
  - bouton "Rejouer" → retourne à HomeActivity
  - bouton "Voir l'historique" → lance HistoryActivity
  - sauvegarde automatiquement la session dans Firestore via QuizRepository.saveSession()
- activity_result.xml : écran de résultat avec animation (score animé), CardView, boutons
Package : com.ataoury.youssef.quizappgeo.ui.quiz
```

---

### PROMPT 8 — Historique des quiz (Firestore)

```
based on skill.md and architecture.md 
Génère pour Android Java MVVM :
- QuizRepository.java :
  - saveSession(QuizSession session) → sauvegarde dans Firestore collection "quizSessions"
  - getUserHistory(String userId) → retourne LiveData<List<QuizSession>> des sessions de l'user triées par date
- HistoryActivity.java : affiche RecyclerView des sessions avec date, ville, score
- HistoryViewModel.java : expose LiveData<List<QuizSession>> history
- HistoryAdapter.java : RecyclerView.Adapter<HistoryViewHolder> avec item_history.xml
- item_history.xml : CardView avec ville, score X/10, date formatée
Package : com.ataoury.youssef.quizappgeo.ui.history
```

---

### PROMPT 9 — AndroidManifest.xml complet

```
based on skill.md and architecture.md 
Génère le AndroidManifest.xml complet pour ce projet Android Java avec :
- Permissions : INTERNET, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_NETWORK_STATE
- Activities déclarées : LoginActivity (LAUNCHER), RegisterActivity, HomeActivity, QuizActivity, ResultActivity, HistoryActivity
- Meta-data pour Firebase et Google Sign-In
- Application class : QuizApplication
- Orientation : portrait forcé pour toutes les activities
- Theme Material Design 3
```

---

### PROMPT 10 — QuizApplication.java

```
based on skill.md and architecture.md 
Génère QuizApplication.java pour Android Java :
- extends Application
- initialise Firebase dans onCreate() : FirebaseApp.initializeApp(this)
- configure un gestionnaire d'erreurs global
- méthode statique getInstance() pour accès global
Package : com.ataoury.youssef.quizappgeo
```

---

### PROMPT 11 — Constants.java

```
based on skill.md and architecture.md 
Génère Constants.java pour Android Java :
- interface ou classe finale avec toutes les constantes du projet :
  - GEMINI_API_BASE_URL
  - GEMINI_MODEL_NAME
  - FIRESTORE_COLLECTION_USERS
  - FIRESTORE_COLLECTION_SESSIONS
  - EXTRA_CITY_NAME (Intent key)
  - EXTRA_SCORE (Intent key)
  - PREFS_NAME (SharedPreferences)
  - KEY_USER_ID (SharedPreferences)
  - QUIZ_QUESTIONS_COUNT = 10
Package : com.ataoury.youssef.quizappgeo.utils
```

---

### PROMPT 12 — SessionManager.java

```
based on skill.md and architecture.md 
Génère SessionManager.java pour Android Java :
- Singleton qui gère SharedPreferences
- méthodes : saveUserId(String uid), getUserId(), clearSession(), isLoggedIn()
- utilisé pour persister la session entre les ouvertures de l'app
Package : com.ataoury.youssef.quizappgeo.utils
```

---

## Guide de configuration Firebase

### Étape 1 — Créer le projet Firebase

1. Aller sur https://console.firebase.google.com
2. Créer un projet nommé `quizapp-geo-ataoury`
3. Désactiver Google Analytics (optionnel)
4. Ajouter une app Android :
   - Package name : `com.ataoury.youssef.quizappgeo`
   - Télécharger `google-services.json`
   - Placer dans `app/` (pas à la racine du projet)

### Étape 2 — Activer Firebase Authentication

1. Console Firebase → Authentication → Commencer
2. Activer **Email/Password**
3. Activer **Google** (fournir un email support)

### Étape 3 — Créer la base Firestore

1. Console Firebase → Firestore Database → Créer une base de données
2. Choisir **Mode test** (pour le développement)
3. Région : `europe-west` (proche du Maroc)
4. Collections créées automatiquement : `users`, `quizSessions`

### Étape 4 — Règles de sécurité Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Un utilisateur ne peut lire/écrire que ses propres données
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /quizSessions/{sessionId} {
      allow read, write: if request.auth != null && 
                           request.auth.uid == resource.data.userId;
      allow create: if request.auth != null;
    }
  }
}
```

### Étape 5 — Clé API Gemini (Google AI Studio)

1. Aller sur https://aistudio.google.com/app/apikey
2. Créer une clé API gratuite
3. Dans `local.properties` (jamais commité sur Git) :
   ```
   GEMINI_API_KEY=ta_clé_ici
   ```
4. Dans `build.gradle` (app level) :
   ```groovy
   android {
       defaultConfig {
           buildConfigField "String", "GEMINI_API_KEY", "\"${localProperties['GEMINI_API_KEY']}\""
       }
   }
   ```
5. Dans `.gitignore`, vérifier que `local.properties` est bien listé

---

## Checklist de développement par étape

| # | Étape | Fichiers clés | Statut |
|---|---|---|---|
| 1 | Setup projet + dépendances | `build.gradle`, `google-services.json` | ⬜ |
| 2 | Firebase Auth — Inscription | `RegisterActivity`, `AuthViewModel`, `AuthRepository` | ⬜ |
| 3 | Firebase Auth — Connexion | `LoginActivity`, `SessionManager` | ⬜ |
| 4 | GPS + Détection ville | `LocationService`, `HomeActivity` | ⬜ |
| 5 | API Gemini — Génération quiz | `GeminiApiService`, `AiRepository`, `JsonParser` | ⬜ |
| 6 | Logique quiz | `QuizActivity`, `QuizViewModel` | ⬜ |
| 7 | Résultats | `ResultActivity`, `QuizRepository.saveSession()` | ⬜ |
| 8 | Historique | `HistoryActivity`, `HistoryViewModel`, `HistoryAdapter` | ⬜ |
| 9 | Tests + polish UI | Tous les layouts XML | ⬜ |

---

## Bonnes pratiques — Rappels critiques

- **LiveData** : toujours exposer via `MutableLiveData` privé + `LiveData` public dans le ViewModel
- **Threads** : Firebase callbacks arrivent sur le thread principal — OK pour update LiveData
- **OkHttp / Gemini** : toujours appeler sur un `ExecutorService` ou thread background
- **Permissions GPS** : utiliser `ActivityResultLauncher` (pas `onRequestPermissionsResult` déprécié)
- **Google Sign-In** : utiliser `ActivityResultLauncher` avec `GoogleSignInClient`
- **Firestore offline** : activé par défaut — les données sont cachées localement
- **Gestion erreurs réseau** : toujours vérifier `NetworkUtils.isConnected()` avant appel API
- **Null safety** : vérifier `FirebaseAuth.getInstance().getCurrentUser() != null` avant tout accès user
