package com.swjtu.http;

import com.swjtu.lang.Lang;

/**
 * HttpParams is an interface containing two functions to set up
 * HTTP request parameters.
 */
public interface HttpParams {
    /**
     * Set the request parameters that will be sent to the server.
     * @param source source language
     * @param text the content to be convert to speech
     */
    void setFormData(Lang source, String text);

    /**
     * Set the request parameters that will be sent to the server.
     * @param from source language
     * @param to target language
     * @param text the content to be translated
     */
    public void setFormData(Lang from, Lang to, String text);
}
