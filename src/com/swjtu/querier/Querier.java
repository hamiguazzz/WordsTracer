package com.swjtu.querier;

import com.swjtu.http.AbstractHttpAttribute;
import com.swjtu.lang.Lang;

import java.util.ArrayList;
import java.util.List;

public final class Querier<T extends AbstractHttpAttribute> {
    private Lang from;
    private Lang to;
    private String text;
    private List<T> collection;

    public Querier() {
        collection = new ArrayList<>();
    }

    public List<String> execute() {
        List<String> result = new ArrayList<>();

        for (T element : collection) {
            if (element.getClass().getName().contains("Translator")) {
                result.add(element.run(from, to, text));
            } else if (element.getClass().getName().contains("TTS")) {
                result.add(element.run(from, text));
            }
        }
        return result;
    }

    public void setParams(Lang from, Lang to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
    }

    public void setParams(Lang source, String text) {
        this.from = source;
        this.text = text;
    }

    public void attach(T element){
        collection.add(element);
    }

    public void detach(T element) {
        collection.remove(element);
    }
}
