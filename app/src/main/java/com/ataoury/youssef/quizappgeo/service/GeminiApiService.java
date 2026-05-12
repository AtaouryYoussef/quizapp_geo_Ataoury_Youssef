package com.ataoury.youssef.quizappgeo.service;

import androidx.annotation.NonNull;

import com.ataoury.youssef.quizappgeo.BuildConfig;
import com.ataoury.youssef.quizappgeo.model.Question;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiService {

    public interface GeminiCallback {
        void onSuccess(List<Question> questions);

        void onError(String message);
    }

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    public GeminiApiService() {
        this.okHttpClient = new OkHttpClient();
    }

    public void generateQuizQuestions(@NonNull String cityName, @NonNull GeminiCallback callback) {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("GEMINI_API_KEY est vide. Ajoutez-la dans local.properties.");
            return;
        }

        String prompt = "Tu es un generateur de quiz geo local. " +
                "Produit exactement 10 questions sur la ville de " + cityName + ". " +
                "Chaque question doit etre personnalisee a cette ville avec des references concretes (monuments, quartiers, transport, "
                +
                "equipe locale, economie locale, histoire locale, culture locale). " +
                "Interdit: formulations generiques reutilisables pour n'importe quelle ville, comme 'dans cette ville' sans element local. "
                +
                "Chaque question doit contenir au moins un nom propre local ou un fait numerique local plausible. " +
                "Retourne uniquement un JSON array valide de 10 objets avec exactement ces champs: " +
                "question (string), choices (array de 4 strings), correctIndex (int de 0 a 3). " +
                "Ne renvoie aucun markdown, aucune explication, aucun texte hors JSON.";

        RequestBody requestBody = buildRequestBody(prompt);
        Request request = new Request.Builder()
                .url(BASE_URL + "?key=" + apiKey)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Erreur reseau Gemini.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erreur API Gemini: " + response.code());
                    response.close();
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                response.close();

                try {
                    String jsonArrayText = extractJsonArrayTextFromResponse(responseBody);
                    List<Question> questions = JsonParser.parseQuestions(jsonArrayText);
                    if (questions.isEmpty()) {
                        callback.onError("Aucune question valide retournee par Gemini.");
                        return;
                    }
                    callback.onSuccess(questions);
                } catch (Exception e) {
                    callback.onError(e.getMessage() != null
                            ? e.getMessage()
                            : "Impossible de parser la reponse Gemini.");
                }
            }
        });
    }

    private RequestBody buildRequestBody(@NonNull String prompt) {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestJson = new JsonObject();
        requestJson.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.4);
        generationConfig.addProperty("responseMimeType", "application/json");

        JsonObject responseSchema = new JsonObject();
        responseSchema.addProperty("type", "ARRAY");

        JsonObject itemSchema = new JsonObject();
        itemSchema.addProperty("type", "OBJECT");

        JsonObject properties = new JsonObject();

        JsonObject questionProperty = new JsonObject();
        questionProperty.addProperty("type", "STRING");
        properties.add("question", questionProperty);

        JsonObject choicesProperty = new JsonObject();
        choicesProperty.addProperty("type", "ARRAY");
        JsonObject choiceItem = new JsonObject();
        choiceItem.addProperty("type", "STRING");
        choicesProperty.add("items", choiceItem);
        properties.add("choices", choicesProperty);

        JsonObject correctIndexProperty = new JsonObject();
        correctIndexProperty.addProperty("type", "INTEGER");
        properties.add("correctIndex", correctIndexProperty);

        itemSchema.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("question");
        required.add("choices");
        required.add("correctIndex");
        itemSchema.add("required", required);

        responseSchema.add("items", itemSchema);
        generationConfig.add("responseSchema", responseSchema);

        requestJson.add("generationConfig", generationConfig);

        return RequestBody.create(requestJson.toString(), JSON_MEDIA_TYPE);
    }

    private String extractJsonArrayTextFromResponse(@NonNull String responseBody) {
        JsonObject root = com.google.gson.JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.size() == 0) {
            throw new IllegalStateException("Reponse Gemini vide.");
        }

        JsonObject candidate = candidates.get(0).getAsJsonObject();
        JsonObject content = candidate.getAsJsonObject("content");
        if (content == null) {
            throw new IllegalStateException("Contenu Gemini introuvable.");
        }

        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.size() == 0) {
            throw new IllegalStateException("Parties du contenu Gemini introuvables.");
        }

        String rawText = parts.get(0).getAsJsonObject().get("text").getAsString();
        String cleaned = rawText.replace("```json", "").replace("```", "").trim();

        int firstArrayIndex = cleaned.indexOf('[');
        int lastArrayIndex = cleaned.lastIndexOf(']');
        if (firstArrayIndex == -1 || lastArrayIndex == -1 || firstArrayIndex >= lastArrayIndex) {
            throw new IllegalStateException("Tableau JSON des questions introuvable.");
        }

        return cleaned.substring(firstArrayIndex, lastArrayIndex + 1);
    }
}
