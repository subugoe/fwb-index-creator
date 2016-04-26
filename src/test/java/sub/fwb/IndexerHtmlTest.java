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
		xslt.setParameter("sourcesListFile", "src/test/resources/sourcesList.xml");
	}

	@Before
	public void beforeEachTest() throws Exception {
		outputBaos = new ByteArrayOutputStream();
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
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("/div[@class='article']", html);
	}

	@Test
	public void shouldInsertLemma() throws Exception {
		xslt.transform("src/test/resources/html/lemma.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("testlemma", "//div[@class='lemma']", html);
	}

	@Test
	public void shouldInsertArticleHead() throws Exception {
		xslt.transform("src/test/resources/html/articleHead.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("article head", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldInsertNeblem() throws Exception {
		xslt.transform("src/test/resources/html/neblem.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("neblem1, neblem2,", "//span[@class='neblem']", html);
	}

	@Test
	public void shouldInsertPhras() throws Exception {
		xslt.transform("src/test/resources/html/phras.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='phras']", html);
		assertXpathExists("//span[@class='phras-begin']", html);
	}

	@Test
	public void shouldInsertGgs() throws Exception {
		xslt.transform("src/test/resources/html/ggs.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='ggs']", html);
		assertXpathExists("//span[@class='ggs-begin']", html);
	}

	@Test
	public void shouldInsertHighlightings() throws Exception {
		xslt.transform("src/test/resources/html/highlightings.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("italic", "//span[@class='italic']", html);
		assertXpathEvaluatesTo("hoch", "//span[@class='higher-and-smaller']", html);
		assertXpathEvaluatesTo("rect", "//span[@class='rect']", html);
	}

	@Test
	public void shouldTransformLinebreak() throws Exception {
		xslt.transform("src/test/resources/html/linebreak.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo(" / ", "//div[@class='article-head']", html);
	}

	@Test
	public void shouldTransformGrammar() throws Exception {
		xslt.transform("src/test/resources/html/grammar.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("Art", "//span[@class='type-of-word']", html);
		assertXpathEvaluatesTo("-Ø", "//span[@class='flex']", html);
	}

	@Test
	public void shouldTransformReference() throws Exception {
		xslt.transform("src/test/resources/html/reference.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("reference", "//a/@href", html);
		assertXpathEvaluatesTo("click here", "//a", html);
	}

	@Test
	public void shouldInsertSenseWithDefinition() throws Exception {
		xslt.transform("src/test/resources/html/sense.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='sense']", html);
		assertXpathEvaluatesTo("my definition", "//div[@class='definition']", html);
	}

	@Test
	public void shouldInsertNumbersIfSeveralSenses() throws Exception {
		xslt.transform("src/test/resources/html/twoSenses.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='sense']", html);
		assertXpathExists("//span[@class='sense-number' and text()='1. ']", html);
		assertXpathExists("//span[@class='sense-number' and text()='2. ']", html);
	}

	@Test
	public void shouldInsertSenseWithWbv() throws Exception {
		xslt.transform("src/test/resources/html/senseWithWbv.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("Wortbildungsverweis", "//div[@class='definition']/span[@class='wbv']", html);
	}

	@Test
	public void shouldInsertStw() throws Exception {
		xslt.transform("src/test/resources/html/stw.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("Stw", "//div[@class='stw']", html);
	}

	@Test
	public void shouldInsertAcronyms() throws Exception {
		xslt.transform("src/test/resources/html/withBeginnings.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='bdv']", html);
		assertXpathExists("//span[@class='bdv-begin']", html);
		assertXpathExists("//div[@class='synt']", html);
		assertXpathExists("//span[@class='synt-begin']", html);
		assertXpathExists("//div[@class='wbg']", html);
		assertXpathExists("//span[@class='wbg-begin']", html);
		assertXpathExists("//div[@class='dict-ref']", html);
		assertXpathExists("//span[@class='dict-ref-begin']", html);
		assertXpathExists("//div[@class='subvoce']", html);
		assertXpathExists("//span[@class='subvoce-begin']", html);
	}

	@Test
	public void shouldTransformCitation() throws Exception {
		xslt.transform("src/test/resources/html/cite.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='citations']", html);
		assertXpathExists("//span[@class='citations-begin']", html);
		assertXpathExists("//div[@class='citation']", html);
		assertXpathEvaluatesTo("Name", "//a[@class='name citation-source_link']", html);
		assertXpathEvaluatesTo("13, 20 ", "//span[@class='cited-range']", html);
		assertXpathEvaluatesTo("Region", "//span[@class='region']", html);
		assertXpathEvaluatesTo("1599", "//span[@class='date']", html);
		assertXpathExists("//span[@class='quote']", html);
		assertXpathEvaluatesTo("Miller", "//a[@href='/source/source_xyu']", html);
	}

	@Test
	public void shouldMakeLinkInDefinition() throws Exception {
		xslt.transform("src/test/resources/html/definitionWithName.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("", "//span", html);
		assertXpathEvaluatesTo("Meier", "//a[@class='name citation-source_link']", html);
	}

	@Test
	public void shouldTransformBls() throws Exception {
		xslt.transform("src/test/resources/html/bls.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='bls']", html);
		assertXpathExists("//span[@class='bls-begin']", html);
		assertXpathExists("//div[@class='citation']", html);
	}

	@Test
	public void shouldCreateAnchors() throws Exception {
		xslt.transform("src/test/resources/html/anchors.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("sense1", "//div[@class='definition']/@id", html);
		assertXpathEvaluatesTo("mylemma#sense2", "//a[1]/@href", html);
		assertXpathEvaluatesTo("mysecondlemma.s.1f#sense12", "//a[2]/@href", html);
	}

	@After
	public void afterEachTest() {
		// System.out.println(outputBaos.toString());
	}

	private String extractHtmlField(String s, int number) {
		Pattern pattern = Pattern.compile("CDATA\\[(.*?)]]");
		Matcher matcher = pattern.matcher(s.replaceAll("\\n", " "));
		String html = "";
		for (int i = 0; i < number; i++) {
			if (matcher.find()) {
				html = matcher.group(1);
			}
		}
		return html;
	}

}
