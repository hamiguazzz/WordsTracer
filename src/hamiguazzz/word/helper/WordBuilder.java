package hamiguazzz.word.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swjtu.lang.Lang;
import com.swjtu.querier.Querier;
import hamiguazzz.word.Word;
import hamiguazzz.word.trans.WordTranslator;

import java.io.IOException;
import java.util.ArrayList;

public final class WordBuilder {

	public static Word buildWordFromNet(String word) throws IOException {

		//region Get
		String s = getDataFromNet(word);

		ObjectMapper mapper = new ObjectMapper();
		var root = mapper.readTree(s);
		//endregion

//		//region test_usage
//		String s;
//		try (var is = new FileInputStream("pure_test.json")) {
//			s = new String(is.readAllBytes());
//		}
//		ObjectMapper mapper = new ObjectMapper();
//		var root = mapper.readTree(s);
//		//endregion

		//region Base
		WordBase base = new WordBase();
		base.word = word;
		base.frequency = root.path("dict_result").path("collins").path("frequence").asInt(0);
		base.pronunciation_am = root.path("dict_result").path("simple_means").findPath("symbols").findPath("ph_am").asText();
		base.pronunciation_en = root.path("dict_result").path("simple_means").findPath("symbols").findPath("ph_en").asText();
		var col = root.path("dict_result").path("simple_means").path("tags").withArray("core");
		var list = new ArrayList<String>(col.size());
		col.forEach(e -> list.add(e.asText()));
		base.tags = list.toArray(new String[0]);
		//endregion

		//region Exchanges
		WordExchange exchange = new WordExchange();
		var ex = root.path("dict_result").path("simple_means").path("exchange");
		exchange.word_done = toArray(ex.path("word_done").toString());
		exchange.word_pl = toArray(ex.path("word_pl").toString());
		exchange.word_er = toArray(ex.path("word_er").toString());
		exchange.word_est = toArray(ex.path("word_est").toString());
		exchange.word_ing = toArray(ex.path("word_ing").toString());
		exchange.word_past = toArray(ex.path("word_past").toString());
		exchange.word_third = toArray(ex.path("word_third").toString());
		//endregion

		//region Means
		var par = root.path("dict_result").path("simple_means").path("symbols").findPath("parts");
		var ml = new ArrayList<WordMeaning>();
		par.forEach(e -> {
			var m = new WordMeaning();
			m.part = e.path("part").asText();
			m.means = toArray(e.path("means").toString());
			ml.add(m);
		});
		WordMeaning[] means = ml.toArray(new WordMeaning[0]);
		//endregion

		//region Means_en
		var grs = root.path("dict_result").path("edict").findPath("item");
		var mle = new ArrayList<WordMeaningEn>();
		for (var gr : grs) {
			var ps = gr.path("pos").asText();
			var gg = gr.findPath("tr_group");
			for (var g : gg) {
				var em = new WordMeaningEn();
				em.part = ps;
				em.means = toArray(g.path("tr").toString());
				em.examples = toArray(g.path("example").toString());
				em.similar_words = toArray(g.path("similar_word").toString());
				mle.add(em);
			}
		}
		WordMeaningEn[] means_en = mle.toArray(new WordMeaningEn[0]);
		//endregion

		return new Word(base, exchange, means, means_en);
	}

	private static String getDataFromNet(String word) {
		Querier<WordTranslator> querierTrans = new Querier<>();
		querierTrans.setParams(Lang.EN, Lang.ZH, word);
		querierTrans.attach(new WordTranslator());
		return querierTrans.execute().get(0);
	}

	private static String[] toArray(String s) {
		if (s == null || s.equals("") || s.equals("\"\"")) {
			return new String[]{};
		}
		var re = s.split(",");
		if (re.length <= 1) {
			return new String[]{deal(s, 2, 2)};
		}
		re[0] = deal(re[0], 2, 1);
		for (int i = 1; i < re.length - 1; i++) {
			re[i] = deal(re[i], 1, 1);
		}
		re[re.length - 1] = deal(re[re.length - 1], 1, 2);
		return re;
	}

	private static String deal(String s, int left, int right) {
		if (s == null || s.equals("") || s.length() < left + right) return s;
		return new String(s.toCharArray(), left, s.length() - left - right);
	}

}
