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

			query += exactMatch(word, "article_fulltext");
			query += starsBothSides(word, "article_fulltext");
		}
		for (String phrase : searchPhrases) {
			query += "lemma:\"" + phrase + "\" neblem:\"" + phrase + "\" ";
			query += "article_fulltext:\"" + phrase + "\" ";
		}
		
		query += ") ";

		for (String word : searchWords) {
			query += "AND article_fulltext:*" + word + "* ";
		}
		for (String phrase : searchPhrases) {
			query += "AND article_fulltext:\"" + phrase + "\" ";
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
