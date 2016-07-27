package sub.fwb;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class IndexerTest {

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

	@Test
	public void shouldTransformPrintedSource() throws Exception {
		xslt.transform("src/test/resources/printedSource.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("artikel", "//field[@name='type']", result);
		assertXpathEvaluatesTo("Some printed source", "//field[@name='printed_source']", result);
		assertXpathEvaluatesTo("08", "//field[@name='volume']", result);
		assertXpathEvaluatesTo("1", "//field[@name='col']", result);
	}

	@Test
	public void shouldFindArticleEntries() throws Exception {
		xslt.transform("src/test/resources/articleEntries.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("some.id", "//field[@name='internal_id']", result);
		assertXpathEvaluatesTo("test_lemma", "//field[@name='lemma']", result);
	}

	@Test
	public void shouldTransformLemmaWithoutComma() throws Exception {
		xslt.transform("src/test/resources/lemmaWithoutComma.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("test_lemma", "//field[@name='lemma']", result);
	}

	@Test
	public void shouldNormalizeLemmaWithSpace() throws Exception {
		xslt.transform("src/test/resources/lemmaWithSpace.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("test_lemma", "//field[@name='lemma']", result);
	}

	@Test
	public void shouldRecognizeRefArticle() throws Exception {
		xslt.transform("src/test/resources/articleRef.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("true", "//field[@name='is_reference']", result);
	}

	@Test
	public void shouldRecognizeNormalArticle() throws Exception {
		xslt.transform("src/test/resources/articleNotRef.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("false", "//field[@name='is_reference']", result);
	}

	@Test
	public void shouldSetArticleId() throws Exception {
		xslt.setParameter("currentArticleId", "123");
		xslt.transform("src/test/resources/articleId.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("123", "//field[@name='id']", result);
	}

	@Ignore
	@Test
	public void shouldSetPreviousAndNext() throws Exception {
		xslt.setParameter("previousArticleId", "prev_id");
		xslt.setParameter("nextArticleId", "next_id");
		xslt.setParameter("previousLemma", "prev_lemma");
		xslt.setParameter("nextLemma", "next_lemma");

		xslt.transform("src/test/resources/articleId.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("prev_id", "//field[@name='article_previous_id']", result);
		assertXpathEvaluatesTo("next_id", "//field[@name='article_next_id']", result);
		assertXpathEvaluatesTo("prev_lemma", "//field[@name='article_previous_lemma']", result);
		assertXpathEvaluatesTo("next_lemma", "//field[@name='article_next_lemma']", result);
	}

	@Ignore
	@Test
	public void shouldSetSenseIdAndNumber() throws Exception {
		xslt.setParameter("currentArticleId", "123");
		xslt.transform("src/test/resources/senseId.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("123_1", "//doc/doc/field[@name='id']", result);
		assertXpathEvaluatesTo("1", "//doc/doc/field[@name='sense_number']", result);
	}

	@Ignore
	@Test
	public void shouldSetArticleIdInSense() throws Exception {
		xslt.setParameter("currentArticleId", "123");
		xslt.transform("src/test/resources/senseId.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("123", "//doc/doc/field[@name='ref_id']", result);
	}

	@Ignore
	@Test
	public void shouldTransformOneSense() throws Exception {
		xslt.transform("src/test/resources/oneSense.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("1", "count(//field[text()='bedeutung'])", result);
		assertXpathExists("//field[text()='Definition.']", result);
	}

	@Ignore
	@Test
	public void shouldTransformTwoSenses() throws Exception {
		xslt.transform("src/test/resources/twoSenses.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("2", "count(//field[text()='bedeutung'])", result);
		assertXpathExists("//field[text()='Definition one.']", result);
		assertXpathExists("//field[text()='Definition two.']", result);
	}

	@Test
	public void shouldFindRelatedArticlesInSense() throws Exception {
		xslt.transform("src/test/resources/relatedArticles.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("2", "count(//field[@name='bdv_id'])", result);
		assertXpathEvaluatesTo("2", "count(//field[@name='bdv'])", result);
		assertXpathEvaluatesTo("related_id_1", "//field[@name='bdv_id'][1]", result);
		assertXpathEvaluatesTo("lemma2", "//field[@name='bdv'][2]", result);
	}

	@Test
	public void shouldTransformCitations() throws Exception {
		xslt.transform("src/test/resources/citations.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("source_11", "//field[@name='definition_source_id']", result);
		assertXpathEvaluatesTo("source_22", "//field[@name='definition_source_instance']", result);
	}

	@Test
	public void shouldAddNeblems() throws Exception {
		xslt.transform("src/test/resources/neblem.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("neblem1", "//field[@name='neblem'][1]", result);
		assertXpathEvaluatesTo("neblem2", "//field[@name='neblem'][2]", result);
		assertXpathEvaluatesTo("2", "count(//field[@name='neblem'])", result);
		assertXpathEvaluatesTo("1", "//field[@name='neblem'][2]/@boost", result);
	}

	@Test
	public void shouldAddNeblemsFromTwoAreas() throws Exception {
		xslt.transform("src/test/resources/neblemAreas.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("neblem1", "//field[@name='neblem'][1]", result);
		assertXpathEvaluatesTo("neblem2", "//field[@name='neblem'][2]", result);
		assertXpathEvaluatesTo("neblem3", "//field[@name='neblem'][3]", result);
		assertXpathEvaluatesTo("3", "count(//field[@name='neblem'])", result);
		assertXpathEvaluatesTo("1", "//field[@name='neblem'][2]/@boost", result);
		assertXpathEvaluatesTo("1", "//field[@name='neblem'][3]/@boost", result);
	}

	@Test
	public void shouldAddSubvoce() throws Exception {
		xslt.transform("src/test/resources/sense_subvoce.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("sub1", "//field[@name='subvoce'][1]", result);
		assertXpathEvaluatesTo("", "//field[@name='subvoce'][2]", result);
	}

	@Test
	public void shouldAddPhraseme() throws Exception {
		xslt.transform("src/test/resources/sense_phraseme.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='phras']", result);
	}

	@Test
	public void shouldAddGgs() throws Exception {
		xslt.transform("src/test/resources/sense_ggs.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("ggs1", "//field[@name='ggs'][1]", result);
		assertXpathEvaluatesTo("", "//field[@name='ggs'][2]", result);
	}

	@Test
	public void shouldAddSayingAsPhraseme() throws Exception {
		xslt.transform("src/test/resources/sense_saying.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("This is a saying.", "//field[@name='phras']", result);
	}

	@Test
	public void shouldAddBothSayingAndPhraseme() throws Exception {
		xslt.transform("src/test/resources/sense_sayingAndPhraseme.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("This is a saying. This is a phraseme.", "//field[@name='phras']", result);
	}

	@Test
	public void shouldAddRelatedReference() throws Exception {
		xslt.transform("src/test/resources/sense_relatedReference.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='zursache']", result);
	}

	@Test
	public void shouldAddSyntagma() throws Exception {
		xslt.transform("src/test/resources/sense_syntagma.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='synt']", result);
	}

	@Test
	public void shouldAddSymptomValue() throws Exception {
		xslt.transform("src/test/resources/sense_symptomValue.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='swt']", result);
	}

	@Test
	public void shouldAddWordFormation() throws Exception {
		xslt.transform("src/test/resources/sense_wordFormation.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='wbg']", result);
	}

	@Test
	public void shouldAddWordReference() throws Exception {
		xslt.transform("src/test/resources/sense_wordFormation.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='wbv']", result);
	}

	@Test
	public void shouldTakeFulltextOfWholeArticle() throws Exception {
		xslt.transform("src/test/resources/fulltext.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("Müller," + " " + "2: A quote." + " " + "Ärmel." + " ",
				"//field[@name='artikel_text']", result);
	}

	@Test
	public void shouldInsertSpacesIntoFulltext() throws Exception {
		xslt.transform("src/test/resources/fulltext_spaces.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("mylemma, die. definition. Name, 346, 35 (reg., M. 14. Jh.) A quote. before space after space. line break. ",
				"//field[@name='artikel_text']", result);
	}

	@After
	public void afterEachTest() {
		// System.out.println(outputBaos.toString());
	}

	@Test
	public void should() throws Exception {
		// xslt.transform("/home/dennis/temp/in_tei/i/imb/imbis.imbis.s.0m.xml",
		// outputBaos);

	}

	@Test
	public void shouldRef() throws Exception {
		// File f = new File("/home/dennis/temp/wortarten.txt");
		// WordTypesGenerator gen = new WordTypesGenerator();
		//
		// String result = gen.prepareForXslt(f);
		// xslt.setParameter("wordTypes", result);
		//
		// xslt.transform("/home/dennis/temp/i/i/it.it.s.9ref.xml", outputBaos);

	}

	@Test
	public void shouldMakeTestFile() throws Exception {
		// xslt.setParameter("currentArticleId", "testId");
		// xslt.transform("/home/dennis/temp/test.xml",
		// outputBaos);

	}

}
