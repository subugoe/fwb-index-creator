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
		xslt.transform("src/test/resources/html/articleField.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='article_html']", result);
	}

	@Test
	public void shouldMakeEmptyArticle() throws Exception {
		xslt.transform("src/test/resources/html/articleField.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("/div[@class='article']", html);
	}

	@Test
	public void shouldInsertLemma() throws Exception {
		xslt.transform("src/test/resources/html/lemma.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("testlemma", "//div[@class='lemma']", html);
	}

	@Test
	public void shouldInsertArticleHead() throws Exception {
		xslt.transform("src/test/resources/html/articleHead.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("article head", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldInsertNeblem() throws Exception {
		xslt.transform("src/test/resources/html/neblem.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("neblem1, neblem2,", "//span[@class='neblem']", html);
	}

	@Test
	public void shouldInsertPhras() throws Exception {
		xslt.transform("src/test/resources/html/phras.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='phras']", html);
		assertXpathExists("//span[@class='phras-begin']", html);
	}

	@Test
	public void shouldInsertGgs() throws Exception {
		xslt.transform("src/test/resources/html/ggs.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='ggs']", html);
		assertXpathExists("//span[@class='ggs-begin']", html);
	}

	@Test
	public void shouldInsertHighlightings() throws Exception {
		xslt.transform("src/test/resources/html/highlightings.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("italic", "//span[@class='italic']", html);
		assertXpathEvaluatesTo("hoch", "//span[@class='higher-and-smaller']", html);
		assertXpathEvaluatesTo("rect", "//span[@class='rect']", html);
	}

	@Test
	public void shouldTransformLinebreak() throws Exception {
		xslt.transform("src/test/resources/html/linebreak.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo(" / ", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldTransformGrammar() throws Exception {
		xslt.transform("src/test/resources/html/grammar.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Art", "//span[@class='type-of-word']", html);
		assertXpathEvaluatesTo("-Ø", "//span[@class='flex']", html);
	}

	@Test
	public void shouldTransformReference() throws Exception {
		xslt.transform("src/test/resources/html/reference.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("reference", "//a/@href", html);
		assertXpathEvaluatesTo("click here", "//a", html);
	}

	@Test
	public void shouldInsertSenseWithDefinition() throws Exception {
		xslt.transform("src/test/resources/html/sense.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='sense']", html);
		assertXpathEvaluatesTo("my definition", "//div[@class='definition']", html);
	}

	@Test
	public void shouldInsertSenseWithWbv() throws Exception {
		xslt.transform("src/test/resources/html/senseWithWbv.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Wortbildungsverweis", "//div[@class='definition']/span[@class='wbv']", html);
	}

	@Test
	public void shouldInsertStw() throws Exception {
		xslt.transform("src/test/resources/html/stw.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathEvaluatesTo("Stw", "//div[@class='stw']", html);
	}

	@Test
	public void shouldInsertAcronyms() throws Exception {
		xslt.transform("src/test/resources/html/withBeginnings.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString());

		assertXpathExists("//div[@class='bdv']", html);
		assertXpathExists("//span[@class='bdv-begin']", html);
		assertXpathExists("//div[@class='synt']", html);
		assertXpathExists("//span[@class='synt-begin']", html);
		assertXpathExists("//div[@class='wbg']", html);
		assertXpathExists("//span[@class='wbg-begin']", html);
		assertXpathExists("//div[@class='dict-ref']", html);
		assertXpathExists("//span[@class='dict-ref-begin']", html);
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
