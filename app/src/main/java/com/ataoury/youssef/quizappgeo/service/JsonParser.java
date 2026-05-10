package com.ataoury.youssef.quizappgeo.service;

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
            String questionText = obj.has("question") ? obj.get("question").getAsString() : "";

            List<String> choices = new ArrayList<>();
            if (obj.has("choices") && obj.get("choices").isJsonArray()) {
                for (JsonElement choiceItem : obj.getAsJsonArray("choices")) {
                    choices.add(choiceItem.getAsString());
                }
            }

            int correctIndex = obj.has("correctIndex") ? obj.get("correctIndex").getAsInt() : 0;
            Question question = new Question(questionText, choices, correctIndex);
            questions.add(question);
        }

        return questions;
    }
}
