package com.swjtu.tts.impl;

import com.swjtu.lang.Lang;
import com.swjtu.tts.AbstractTTS;

public final class TencentTTS extends AbstractTTS {
    private final static String url = "http://audiodetect.browser.qq.com:8080/tts";

    public TencentTTS() {
        super(url);
        setLangSupport();
    }

    @Override
    public void setLangSupport() {
        langMap.put(Lang.ZH, "0");
        langMap.put(Lang.EN, "1");
        langMap.put(Lang.JP, "2");
        langMap.put(Lang.KOR, "3");
    }

    @Override
    public void setFormData(Lang source, String text) {
        formData.put("platform", "PC_Website");
        formData.put("language", langMap.get(source));
        formData.put("text", text);
        formData.put("guid", "d4480e20-1644-4a47-a98d-787cfa244fd2");
    }
}