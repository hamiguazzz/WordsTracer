package com.swjtu.tts.impl;

import com.swjtu.lang.Lang;
import com.swjtu.tts.AbstractTTS;

public final class YoudaoTTS extends AbstractTTS {
    private final static String url = "http://tts.youdao.com/fanyivoice";

    public YoudaoTTS() {
        super(url);
        setLangSupport();
    }

    @Override
    public void setLangSupport() {
        langMap.put(Lang.EN, "eng");
        langMap.put(Lang.JP, "jap");
        langMap.put(Lang.FRA, "fr");
        langMap.put(Lang.KOR, "ko");
    }

    @Override
    public void setFormData(Lang source, String text) {
        formData.put("word", text);
        formData.put("le", langMap.get(source));
        formData.put("keyfrom", "speaker-target");
    }
}
