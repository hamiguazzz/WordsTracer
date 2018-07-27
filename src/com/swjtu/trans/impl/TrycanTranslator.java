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

public final class TrycanTranslator extends AbstractTranslator {
    private static final String url = "http://fanyi.trycan.com/Transfer.do";

    public TrycanTranslator(){
        super(url);
    }

    @Override
    public void setLangSupport() {
        langMap.put(Lang.ZH, "zh");
        langMap.put(Lang.EN, "en");
    }

    @Override
    public void setFormData(Lang from, Lang to, String text) {
        formData.put("word_from", langMap.get(from));
        formData.put("word_to", langMap.get(to));
        formData.put("word_src", text);
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
        return mapper.readTree(text).path("data").findPath("dst").toString();
    }
}
