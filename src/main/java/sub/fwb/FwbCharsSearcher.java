package sub.fwb;

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

import sub.fwb.testing.SolrState;

public class FwbCharsSearcher {

	public void generateAllFwbChars() {
		String solrUrl = System.getProperty("SOLR_URL_FOR_TESTS", "http://localhost:8983/solr/fwb");
		SolrClient solrServerClient = new HttpSolrClient(solrUrl);
		SolrState solr = new SolrState(solrServerClient);

		Set<String> simpleChars = new TreeSet<>();
		Set<String> combiningChars = new HashSet<>();
		Map<String, Long> combCharsMap = new HashMap<>();
		Map<String, Long> simpleCharsMap = new HashMap<>();
		String[][] extraParams = { { "hl", "on" }, { "hl.fragsize", "1" }, { "rows", "1" } };
		String[][] extraParamsNoHl = { { "rows", "1" } };
		String knownCharsInIndex = "\\-\\|()\\[\\]\\\\⁽⁾a-zA-Z0-9_äöüß";
		String knownCharsInHtml = "\\p{Z}\\-\\|()\\[\\]\\\\⁽⁾a-zA-Z0-9_äöüß<>\\/‒\\&\"\\s′`″”∣%«»‛\\$⅓⅙⅔·⅕#˄˚{}¼¾©@‚°=½§…℔*₰¶⸗˺˹„“+–?!;›‹\\.,’·‘:'";

		String q = "artikel:/.*[^" + knownCharsInIndex + "].*/";
		solr.select(extraParams, q);

		while (solr.results() > 0) {
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
			solr.select(extraParams, query);
		}
		for (String comb : combiningChars) {
			for (char ch = 'a'; ch <= 'z'; ch++) {
				String query = "artikel:*" + ch + comb + "*";
				solr.select(extraParamsNoHl, query);
				if (solr.results() > 0) {
					combCharsMap.put("" + ch + comb, solr.results());
				}
			}
		}
		for (String simple : simpleChars) {
			String query = "artikel:*" + simple + "*";
			solr.select(extraParamsNoHl, query);
			if (solr.results() > 0) {
				simpleCharsMap.put(simple, solr.results());
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

}
