package com.swjtu.trans.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swjtu.lang.Lang;
import com.swjtu.trans.AbstractTranslator;
import com.swjtu.util.Util;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public final class IcibaTranslator extends AbstractTranslator {
    private static final String url = "http://fy.iciba.com/ajax.php?a=fy";

    public IcibaTranslator(){
        super(url);
    }

    @Override
    public void setLangSupport() {
        langMap.put(Lang.ZH, "zh");
        langMap.put(Lang.EN, "en");
        langMap.put(Lang.JP, "ja");
        langMap.put(Lang.KOR, "ko");
        langMap.put(Lang.FRA, "fr");
        langMap.put(Lang.DE, "de");
    }

    @Override
    public void setFormData(Lang from, Lang to, String text) {
        formData.put("f", langMap.get(from));
        formData.put("t", langMap.get(to));
        formData.put("w", text);
    }

    @Override
    public String query() throws Exception {
        HttpPost request = new HttpPost(Util.getUrlWithQueryString(url, formData));
        CloseableHttpResponse response = httpClient.execute(request);

        HttpEntity entity = response.getEntity();

        String result = EntityUtils.toString(entity, "utf-8");

        EntityUtils.consume(entity);
        response.getEntity().getContent().close();
        response.close();

        return result;
    }

    @Override
    public String parses(String text) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(text).path("content").findPath("out").toString();
    }
}
