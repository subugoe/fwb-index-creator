package sub.fwb.testing;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SolrTester {
	private static SolrState solr;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		String solrUrl = System.getProperty("SOLR_URL_FOR_TESTS", "http://localhost:8983/solr/fwb");
		SolrClient solrServerClient = new HttpSolrClient(solrUrl);
		solr = new SolrState(solrServerClient);
	}

	@After
	public void afterEach() throws Exception {
		if (System.getProperty("SOLR_URL_FOR_TESTS") != null) {
			solr.printQueryString();
		} else {
			solr.printResults();
		}
	}

	@Ignore
	@Test
	public void complexPhrase() throws Exception {

		// solr.ask("{!complexphrase inOrder=true}\"imbi* ward\" {!complexphrase
		// inOrder=true}+artikel_text:\"imbi* ward\"");

		// assertEquals(24, results());
		// assertEquals("abziehen", lemma(1));
		// assertEquals("abrechen", lemma(2));
		// assertEquals("abschlagen", lemma(3));
	}

	@Test
	public void es() throws Exception {

		solr.askByQuery("artikel_text:es");

		// String s = "";
		// for (int i = 1; i <= results(); i++) {
		// s += lemma(i) + "\n";
		// }
		// FileUtils.writeStringToFile(new
		// java.io.File("/home/dennis/html.txt"), s);

		assertEquals(4446, results());
	}

	// @Test
	public void generateAllFwbChars() throws Exception {
		Set<String> simpleChars = new TreeSet<>();
		Set<String> combiningChars = new HashSet<>();
		Map<String, Long> combCharsMap = new HashMap<>();
		Map<String, Long> simpleCharsMap = new HashMap<>();
		String[][] extraParams = { { "hl.fragsize", "1" }, { "rows", "1" } };
		String knownCharsInIndex = "\\-\\|()\\[\\]\\\\⁽⁾a-zA-Z0-9_äöüß";
		String knownCharsInHtml = "\\p{Z}\\-\\|()\\[\\]\\\\⁽⁾a-zA-Z0-9_äöüß<>\\/‒\\&\"\\s′`″”∣%«»‛\\$⅓⅙⅔·⅕#˄˚{}¼¾©@‚°=½§…℔*₰¶⸗˺˹„“+–?!;›‹\\.,’·‘:";

		String q = "artikel:/.*[^" + knownCharsInIndex + "].*/";
		solr.askByQuery(extraParams, q, "/selecthl");

		while (results() > 0) {
			String hlText = solr.getHighlightings().get(solr.id(1)).get("artikel").get(0);
			String newChars = hlText.replaceAll("[" + knownCharsInHtml + "]", " ");
			for (int i = 0; i < newChars.length(); i++) {
				Character currentChar = newChars.charAt(i);
				if (!currentChar.equals(" ") && Character.getType(currentChar) == Character.NON_SPACING_MARK) {
					String charAndCombining = "" + currentChar;
					combiningChars.add(charAndCombining);
					knownCharsInIndex += charAndCombining;
					knownCharsInHtml += charAndCombining;
				} else if (!currentChar.toString().equals(" ")) {
					simpleChars.add("" + currentChar);
					knownCharsInIndex += currentChar;
					knownCharsInHtml += currentChar;
				}
			}
			String query = "-lemma:(leib ban abziehen ausgehen) artikel:/.*[^" + knownCharsInIndex + "].*/";
			solr.askByQuery(extraParams, query, "/selecthl");
		}
		for (String comb : combiningChars) {
			for (char ch = 'a'; ch <= 'z'; ch++) {
				String query = "artikel:*" + ch + comb + "*";
				solr.askByQuery(extraParams, query, "/select");
				if (results() > 0) {
					combCharsMap.put("" + ch + comb, results());
				}
			}
		}
		for (String simple : simpleChars) {
			String query = "artikel:*" + simple + "*";
			solr.askByQuery(extraParams, query, "/select");
			if (results() > 0) {
				simpleCharsMap.put(simple, results());
			}
		}
		Map<String, Long> sortedSimple = sortByValue(simpleCharsMap);
		System.out.println("Einfache: (" + sortedSimple.size() + ")");
		for (Map.Entry<String, Long> entry : sortedSimple.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue() + " - " + getUnicode(entry.getKey()));
		}
		System.out.println();
		Map<String, Long> sortedComb = sortByValue(combCharsMap);
		System.out.println("Mit combining: (" + sortedComb.size() + ")");
		for (Map.Entry<String, Long> entry : sortedComb.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue() + " - " + getUnicode(entry.getKey()));
		}
	}

	private String getUnicode(String s) {
		String unicode = "";
		for (int i = 0; i < s.length(); i++) {
			String hexCode = Integer.toHexString(Character.codePointAt(s, i));
			int fillCount = 4 - hexCode.length();
			String zeroes = "";
			for (int j = 0; j < fillCount; j++) {
				zeroes += "0";
			}
			unicode += "U+" + zeroes + hexCode.toUpperCase() + " ";
		}
		return unicode;
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@Test
	public void negatedQueryShouldCoverAllTerms() throws Exception {

		solr.askByQuery(
				"artikel:/.*[^\\|()\\[\\]\\-⁽⁾a-z0-9äöüßoͤúv́aͤñÿu͂Øůaͧuͥóoͮàïêŷǔıͤēëâôeͣîûwͦýãæáéòõœv̈èu̇ŭāōùēīíūėm̃Γͤŭẽũśŏǒǎǔẅẹìǹăṣẏẙẹσĕĩẃåg̮ńỹěçṅȳňṡćęъčẘịǧḥṁạṙľu֔b].*/");

		assertEquals(0, results());
	}

	@Test
	public void dollarSignInKindeln() throws Exception {
		String[][] extraparams = { { "hl.q", "kindeln" } };
		solr.askByQuery(extraparams, "internal_id:kindeln.s.3v", "/article-hl");
		// This used to lead to an exception in Matcher class
		assertEquals(1, results());
	}

	@Test
	public void maxClauseCountOver1024() throws Exception {

		solr.askByQuery("artikel_text:*e*", "/selecthl");

		assertEquals(40578, results());
	}

	@Test
	public void dashLach() throws Exception {

		solr.askByQuery("-lach", "/search");

		assertEquals(5, results());
		assertEquals("-lach", lemma(1));
	}

	@Test
	public void imbs() throws Exception {

		solr.ask("imbs");

		assertEquals(29, results());
		assertEquals("imbs", lemma(1));
		assertEquals("imbis", lemma(2));
		assertBestResultsContainWordPart("imbs");
	}

	@Test
	public void imbis() throws Exception {

		solr.ask("imbis");

		assertEquals(34, results());
		assertEquals("imbis", lemma(1));
		assertBestResultsContainWordPart("imbis");
	}

	@Test
	public void gericht() throws Exception {

		solr.ask("gericht");

		assertEquals(1953, results());
		assertEquals("landgericht", lemma(1));
		assertBestResultsContainWordPart("gericht");
	}

	@Test
	public void phrase() throws Exception {

		solr.ask("abziehen", "\"Ziesemer, Gr.\"");

		assertEquals(24, results());
		assertEquals("abziehen", lemma(1));
		assertBestResultsContainWordPart("abziehen");
	}

	@Test
	public void essen() throws Exception {

		solr.ask("essen");

		assertEquals(5431, results());
		assertEquals("geniessen", lemma(1));
		assertBestResultsContainWordPart("essen");
	}

	@Test
	public void imbisBergman() throws Exception {

		solr.ask("imbis", "bergman");

		assertEquals(1, results());
		assertEquals("geben", lemma(1));
	}

	@Test
	public void bergleuteBergman() throws Exception {

		solr.ask("bergleute", "bergman");

		assertEquals(5, results());
		assertEquals("bergman", lemma(1));
		assertEquals("bergleute", lemma(2));
		assertEquals("berg", lemma(3));
	}

	@Test
	public void leben() throws Exception {

		solr.ask("leben");

		assertEquals(1648, results());
		assertEquals("leben", lemma(1));
		assertEquals("leben", lemma(2));
		assertBestResultsContainWordPart("leben");
	}

	@Test
	public void christ() throws Exception {

		solr.ask("christ");

		assertEquals(2082, results());
		assertEquals("christ", lemma(1));
		assertBestResultsContainWordPart("christ");
	}

	private String lemma(int resultNumber) {
		return solr.lemma(resultNumber);
	}

	private long results() {
		return solr.results();
	}

	private void assertBestResultsContainWordPart(String wordPart) throws Exception {
		int numLemmas = solr.askForNumberOfLemmas(wordPart);
		for (int i = 1; i <= numLemmas; i++) {
			String currentLemma = solr.lemma(i).toLowerCase();
			assertThat(currentLemma, containsString(wordPart));
		}
	}

}
