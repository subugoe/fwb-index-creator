package sub.fwb;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IndexerHtmlTest {

	private OutputStream outputBaos;
	private static Xslt xslt;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		xslt = new Xslt("src/main/resources/fwb-indexer.xslt");
	}

	@Before
	public void beforeEachTest() throws Exception {
		outputBaos = new ByteArrayOutputStream();
	}

	@After
	public void afterEachTest() {
		// System.out.println(outputBaos.toString());
	}

	@Test
	public void shouldMakeHtmlField() throws Exception {
		xslt.transform("src/test/resources/html_articleField.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='article_html']", result);
	}

	@Test
	public void shouldMakeEmptyArticle() throws Exception {
		xslt.transform("src/test/resources/html_articleField.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("/div[@class='article']", html);
	}

	@Test
	public void shouldInsertLemma() throws Exception {
		xslt.transform("src/test/resources/html_lemma.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("testlemma", "//div[@class='lemma']", html);
	}

	@Test
	public void shouldInsertArticleHead() throws Exception {
		xslt.transform("src/test/resources/html_articleHead.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("article head", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldInsertNeblem() throws Exception {
		xslt.transform("src/test/resources/html_neblem.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("neblem1, neblem2,", "//span[@class='neblem']", html);
	}

	private String extractHtmlField(String s) {
		Pattern pattern = Pattern.compile("CDATA\\[(.*?)]]");
		Matcher matcher = pattern.matcher(s.replaceAll("\\n", " "));
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

}
