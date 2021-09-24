package com.max.def.translationapp.Models;

public class ApiResponse
{
    private String translatedText;

    public ApiResponse() {
    }

    public ApiResponse(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
