package com.ataoury.youssef.quizappgeo.service;

import android.text.TextUtils;

import com.ataoury.youssef.quizappgeo.model.Question;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JsonParser {

    private JsonParser() {
    }

    public static List<Question> parseQuestions(String rawJsonArray) {
        List<Question> questions = new ArrayList<>();
        JsonElement root = com.google.gson.JsonParser.parseString(rawJsonArray);

        if (!root.isJsonArray()) {
            throw new IllegalArgumentException("Le JSON des questions n'est pas un tableau.");
        }

        JsonArray jsonQuestions = root.getAsJsonArray();
        for (JsonElement item : jsonQuestions) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject obj = item.getAsJsonObject();
            String questionText = getString(obj, "question", "questionText", "title");

            List<String> choices = new ArrayList<>();
            JsonArray choicesArray = getArray(obj, "choices", "options", "answers");
            if (choicesArray != null) {
                for (JsonElement choiceItem : choicesArray) {
                    choices.add(choiceItem.getAsString());
                }
            }

            if (TextUtils.isEmpty(questionText) || choices.size() < 2) {
                continue;
            }

            while (choices.size() > 4) {
                choices.remove(choices.size() - 1);
            }

            int correctIndex = getCorrectIndex(obj, choices);
            if (correctIndex < 0 || correctIndex >= choices.size()) {
                correctIndex = 0;
            }

            Question question = new Question(questionText, choices, correctIndex);
            questions.add(question);
        }

        return questions;
    }

    private static String getString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        }
        return "";
    }

    private static JsonArray getArray(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && obj.get(key).isJsonArray()) {
                return obj.getAsJsonArray(key);
            }
        }
        return null;
    }

    private static int getCorrectIndex(JsonObject obj, List<String> choices) {
        if (obj.has("correctIndex")) {
            return obj.get("correctIndex").getAsInt();
        }
        if (obj.has("correctAnswerIndex")) {
            return obj.get("correctAnswerIndex").getAsInt();
        }

        String answerText = getString(obj, "correctAnswer", "answer", "correct");
        if (!TextUtils.isEmpty(answerText)) {
            for (int i = 0; i < choices.size(); i++) {
                if (answerText.equalsIgnoreCase(choices.get(i))) {
                    return i;
                }
            }
        }

        return 0;
    }
}
