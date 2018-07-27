package com.swjtu.tts.impl;

import com.swjtu.lang.Lang;
import com.swjtu.tts.AbstractTTS;

public final class BaiduTTS extends AbstractTTS {
    private final static String url = "http://fanyi.baidu.com/gettts";
    private final static int spd = 3;

    public BaiduTTS() {
        super(url);
        setLangSupport();
    }

    @Override
    public void setLangSupport() {
        langMap.put(Lang.ZH, "zh");
        langMap.put(Lang.EN, "en");
        langMap.put(Lang.JP, "jp");
        langMap.put(Lang.KOR, "kor");
        langMap.put(Lang.FRA, "fra");
        langMap.put(Lang.RU, "ru");
        langMap.put(Lang.DE, "de");
        langMap.put(Lang.TH, "th");
    }

    @Override
    public void setFormData(Lang source, String text) {
        formData.put("lan", langMap.get(source));
        formData.put("text", text);
        formData.put("spd", String.valueOf(spd));
        formData.put("source", "web");
    }
}
