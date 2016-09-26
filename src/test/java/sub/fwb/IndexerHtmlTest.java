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

	@After
	public void afterEachTest() {
		// System.out.println(outputBaos.toString());
	}

	@Test
	public void shouldIgnoreEmptyRegion() throws Exception {
		xslt.transform("src/test/resources/html/emptyRegion.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("0", "count(//div[@class='region'])", html);
	}

	@Test
	public void shouldMakeHtmlField() throws Exception {
		xslt.transform("src/test/resources/html/articleField.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='artikel']", result);
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

		assertXpathEvaluatesTo("neblem1, neblem2,", "//div[@class='neblem']", html);
	}

	@Test
	public void shouldInsertPhras() throws Exception {
		xslt.transform("src/test/resources/html/phras.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='phras']", html);
		assertXpathExists("//div[@class='phras-begin']", html);
	}

	@Test
	public void shouldInsertGgs() throws Exception {
		xslt.transform("src/test/resources/html/ggs.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='ggs']", html);
		assertXpathExists("//div[@class='ggs-begin']", html);
	}

	@Test
	public void shouldInsertRa() throws Exception {
		xslt.transform("src/test/resources/html/ra.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='redensart']", html);
		assertXpathExists("//div[@class='redensart-begin']", html);
	}

	@Test
	public void shouldInsertHighlightings() throws Exception {
		xslt.transform("src/test/resources/html/highlightings.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("italic", "//div[@class='italic']", html);
		assertXpathEvaluatesTo("hoch", "//div[@class='higher-and-smaller']", html);
		assertXpathEvaluatesTo("tief", "//div[@class='deep']", html);
		assertXpathEvaluatesTo("rect", "//div[@class='rect']", html);
		assertXpathEvaluatesTo("sc", "//div[@class='small-capitals']", html);
		assertXpathEvaluatesTo("bold", "//div[@class='bold']", html);
		assertXpathEvaluatesTo("wide", "//div[@class='wide']", html);
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

		assertXpathEvaluatesTo("Art", "//div[@class='type-of-word']", html);
		assertXpathEvaluatesTo("-Ø", "//div[@class='flex']", html);
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
	public void shouldIgnoreSemanticsOfSenseWithBedzif() throws Exception {
		xslt.transform("src/test/resources/html/senseWithBedzif.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("", "//div[@class='sense']", html);
	}

	@Test
	public void shouldInsertNumbersIfSeveralSenses() throws Exception {
		xslt.transform("src/test/resources/html/twoSenses.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='sense']", html);
		assertXpathExists("//div[@class='sense-number' and text()='1. ']", html);
		assertXpathExists("//div[@class='sense-number' and text()='2. ']", html);
	}

	@Test
	public void shouldInsertSenseWithWbv() throws Exception {
		xslt.transform("src/test/resources/html/senseWithWbv.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("Wortbildungsverweis", "//div[@class='definition']/div[@class='wbv']", html);
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
		assertXpathExists("//div[@class='bdv-begin']", html);
		assertXpathExists("//div[@class='synt']", html);
		assertXpathExists("//div[@class='synt-begin']", html);
		assertXpathExists("//div[@class='wbg']", html);
		assertXpathExists("//div[@class='wbg-begin']", html);
		assertXpathExists("//div[@class='dict-ref']", html);
		assertXpathExists("//div[@class='dict-ref-begin']", html);
		assertXpathExists("//div[@class='subvoce']", html);
		assertXpathExists("//div[@class='subvoce-begin']", html);
	}

	@Test
	public void shouldTransformCitation() throws Exception {
		xslt.transform("src/test/resources/html/cite.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='citations']", html);
		assertXpathExists("//div[@class='citations-begin']", html);
		assertXpathExists("//div[@class='citation']", html);
		assertXpathEvaluatesTo("Name", "//a[@class='name citation-source_link']", html);
		assertXpathEvaluatesTo("13, 20 ", "//div[@class='cited-range']", html);
		assertXpathEvaluatesTo("Region", "//div[@class='region']", html);
		assertXpathEvaluatesTo("1599", "//div[@class='date']", html);
		assertXpathExists("//div[@class='quote' and @id='quote1']", html);
		assertXpathEvaluatesTo("Miller", "//a[@href='/source/source_xyu']", html);
	}

	@Test
	public void shouldMakeHtmlQuote() throws Exception {
		xslt.transform("src/test/resources/html/cite.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='quote' and @id='quote1']", html);
		assertXpathEvaluatesTo("Miller", "//a[@href='/source/source_xyu']", html);
	}

	@Test
	public void shouldMakeLinkInDefinition() throws Exception {
		xslt.transform("src/test/resources/html/definitionWithName.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("Meier", "//a[@class='name citation-source_link']", html);
	}

	@Test
	public void shouldTransformBls() throws Exception {
		xslt.transform("src/test/resources/html/bls.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathExists("//div[@class='bls']", html);
		assertXpathExists("//div[@class='bls-begin']", html);
		assertXpathExists("//div[@class='citation']", html);
	}

	@Test
	public void shouldCreateAnchors() throws Exception {
		xslt.transform("src/test/resources/html/anchors.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("sense1", "//div[@class='definition']/@id", html);
		assertXpathEvaluatesTo("mylemma#sense2", "//a[1]/@href", html);
		assertXpathEvaluatesTo("#sense12", "//a[2]/@href", html);
	}

	@Test
	public void shouldDecideIfLinkIsItalic() throws Exception {
		xslt.transform("src/test/resources/html/refsItalicOrNot.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("lemma", "//div[@class='italic']/a", html);
		assertXpathEvaluatesTo("1", "//div[@class='article-head']/a", html);
	}

	@Test
	public void shouldCreateHomonymForLemma() throws Exception {
		xslt.transform("src/test/resources/homonym.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("2", "//div[@class='homonym']", html);
	}

	@Test
	public void shouldNotCreateHomonymForLemma() throws Exception {
		xslt.transform("src/test/resources/html/withoutHomonym.xml", outputBaos);
		String html = extractHtmlField(outputBaos.toString(), 1);

		assertXpathEvaluatesTo("", "//div[@class='homonym']", html);
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
