package sub.fwb.testing;

public class QueryMaker {

	public static void main(String[] args) {

		String[] searchWords = { "imbs" };
		String[] searchPhrases = {  };

		String query = "(";
		
		for (String word : searchWords) {
			query += exactMatch(word, "lemma", "neblem");
			//query += starRight(word, "lemma", "neblem");
			//query += starsBothSides(word, "lemma", "neblem");

			query += exactMatch(word, "artikel_text");
			query += starsBothSides(word, "artikel_text");
		}
		for (String phrase : searchPhrases) {
			query += "lemma:\"" + phrase + "\" neblem:\"" + phrase + "\" ";
			query += "artikel_text:\"" + phrase + "\" ";
		}
		
		query += ") ";

		for (String word : searchWords) {
			query += "AND artikel_text:*" + word + "* ";
		}
		for (String phrase : searchPhrases) {
			query += "AND artikel_text:\"" + phrase + "\" ";
		}

		System.out.println(query);
		System.out.println();
		System.out.println(query.replaceAll("\"", "\\\\\""));
	}

	private static String exactMatch(String word, String... fields) {
		String result = "";
		for (String field : fields) {
			result += field + ":" + word + " ";
		}
		return result;
	}
	private static String starRight(String word, String... fields) {
		String result = "";
		for (String field : fields) {
			result += field + ":" + word + "* ";
		}
		return result;
	}
	private static String starsBothSides(String word, String... fields) {
		String result = "";
		for (String field : fields) {
			result += field + ":*" + word + "* ";
		}
		return result;
	}
	
}
