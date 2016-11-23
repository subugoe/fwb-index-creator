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

	@After
	public void afterEachTest() {
		 System.out.println(outputBaos.toString());
	}

	@Test
	public void shouldMakeWbgForListItems() throws Exception {
		xslt.transform("src/test/resources/sense_wbgList.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("3", "count(//field[@name='wbg'])", result);
		assertXpathEvaluatesTo("3", "count(//field[@name='wbg_text'])", result);
	}

	@Test
	public void shouldIgnoreDefinitionNumbersIfNotPresent() throws Exception {
		xslt.transform("src/test/resources/defNumbers_wrong.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("0", "count(//field[@name='def_number'])", result);
		assertXpathEvaluatesTo("1", "count(//field[@name='def_text'])", result);
	}

	@Test
	public void shouldCreateDefinitionNumbers() throws Exception {
		xslt.transform("src/test/resources/defNumbers.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("1.-4.", "//field[@name='def_number'][1]", result);
		assertXpathEvaluatesTo("5.", "//field[@name='def_number'][2]", result);
	}

	@Test
	public void shouldCreateSortKey() throws Exception {
		xslt.transform("src/test/resources/sortkey.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("abc", "//field[@name='sortkey']", result);
	}

	@Test
	public void shouldCreateRegionAndDate() throws Exception {
		xslt.transform("src/test/resources/dateAndRegion.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("Region", "//field[@name='region']", result);
		assertXpathEvaluatesTo("1500", "//field[@name='datum']", result);
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

	@Test
	public void shouldTransformOneSense() throws Exception {
		xslt.transform("src/test/resources/oneSense.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("Definition.", "//field[@name='def_text']", result);
	}

	@Test
	public void shouldTransformTwoSenses() throws Exception {
		xslt.transform("src/test/resources/twoSenses.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("Definition one.", "//field[@name='def_text'][1]", result);
		assertXpathEvaluatesTo("Definition two.", "//field[@name='def_text'][2]", result);
		assertXpathEvaluatesTo(
				"<div id=\"sense1\" class=\"definition\"><!--start sense1--><div class=\"sense-number\">1. </div>Definition one.<!--end sense1--></div>",
				"//field[@name='def'][1]", result);
		assertXpathEvaluatesTo(
				"<div id=\"sense2\" class=\"definition\"><!--start sense2--><div class=\"sense-number\">2. </div>Definition two.<!--end sense2--></div>",
				"//field[@name='def'][2]", result);
	}

	@Test
	public void shouldFindRelatedArticlesInSense() throws Exception {
		xslt.transform("src/test/resources/relatedArticles.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("2", "count(//field[@name='bdv'])", result);
		assertXpathEvaluatesTo("lemma1, lemma2", "//field[@name='bdv_text']", result);
		assertXpathEvaluatesTo("<div class=\"highlight-boundary\"><!--start bdv2--><div class=\"italic\"><a href=\"related_id_2\">lemma2</a></div><!--end bdv2--></div>", "//field[@name='bdv'][2]", result);
	}

	@Test
	public void shouldTransformCitations() throws Exception {
		xslt.transform("src/test/resources/citations.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("source_11", "//field[@name='definition_source_id']", result);
		assertXpathEvaluatesTo("source_22", "//field[@name='definition_source_instance']", result);
		assertXpathEvaluatesTo(
				"<div class=\"quote\" id=\"quote1\"><!--start quote1-->A quote.<!--end quote1--></div>",
				"//field[@name='zitat']", result);
		assertXpathEvaluatesTo("A quote.", "//field[@name='zitat_text']", result);
	}

	@Test
	public void shouldAddNeblems() throws Exception {
		xslt.transform("src/test/resources/neblem.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("2", "count(//field[@name='neblem'])", result);
		assertXpathEvaluatesTo("<div class=\"neblem\"><!--start neblem1-->neblem1, <!--end neblem1--></div> ",
				"//field[@name='neblem'][1]", result);
		assertXpathEvaluatesTo("<div class=\"neblem\"><!--start neblem2-->neblem2,<!--end neblem2--></div> ",
				"//field[@name='neblem'][2]", result);
		assertXpathEvaluatesTo("neblem1,  neblem2, some text ",
				"//field[@name='artikel_text']", result);
	}

	@Test
	public void shouldAddNeblemTextsFromTwoAreas() throws Exception {
		xslt.transform("src/test/resources/neblemAreas.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("neblem1, neblem2, neblemarea2", "//field[@name='neblem_text'][1]", result);
		assertXpathEvaluatesTo("1", "count(//field[@name='neblem_text'])", result);
	}

	@Test
	public void shouldAddNeblemsFromTwoAreas() throws Exception {
		xslt.transform("src/test/resources/neblemAreas.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("<div class=\"neblem\"><!--start neblem1-->neblem1, neblem2,<!--end neblem1--></div> ",
				"//field[@name='neblem'][1]", result);
		assertXpathEvaluatesTo("<div class=\"neblem\"><!--start neblem2-->neblemarea2<!--end neblem2--></div> ",
				"//field[@name='neblem'][2]", result);
		assertXpathEvaluatesTo("2", "count(//field[@name='neblem'])", result);
	}

	@Test
	public void shouldAddSubvoce() throws Exception {
		xslt.transform("src/test/resources/sense_subvoce.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("Vgl. ferner s. v. sub1, 3.", "//field[@name='subvoce_text']", result);
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

		assertXpathEvaluatesTo("ggs1, ggs2", "//field[@name='ggs_text']", result);
		assertXpathEvaluatesTo("2", "count(//field[@name='ggs'])", result);
		assertXpathEvaluatesTo("<div class=\"highlight-boundary\"><!--start ggs2--><div class=\"italic\"><a href=\"ggs2.s.*\">ggs2</a></div><!--end ggs2--></div>", "//field[@name='ggs'][2]", result);
	}

	@Test
	public void shouldAddSayingAsPhraseme() throws Exception {
		xslt.transform("src/test/resources/sense_saying.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("This is a saying.", "//field[@name='phras_text']", result);
	}

	@Test
	public void shouldAddBothSayingAndPhraseme() throws Exception {
		xslt.transform("src/test/resources/sense_sayingAndPhraseme.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("This is a phraseme.", "//field[@name='phras_text'][1]", result);
		assertXpathEvaluatesTo("This is a saying.", "//field[@name='phras_text'][2]", result);
	}

	@Test
	public void shouldAddRelatedReference() throws Exception {
		xslt.transform("src/test/resources/sense_relatedReference.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("some name 1, 41–43 (s. v. lemma).", "//field[@name='zursache_text']", result);
	}

	@Test
	public void shouldAddSyntagma() throws Exception {
		xslt.transform("src/test/resources/sense_syntagma.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("this is a syntagma", "//field[@name='synt_text']", result);
	}

	@Test
	public void shouldAddTwoSymptomValues() throws Exception {
		xslt.transform("src/test/resources/sense_symptomValuesTwo.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("2", "count(//field[@name='stw'])", result);
		assertXpathEvaluatesTo("<div class=\"stw\"><!--start stw1-->This is a symptom value.<!--end stw1--></div>",
				"//field[@name='stw'][1]", result);
		assertXpathEvaluatesTo("<div class=\"stw\"><!--start stw2-->Second symptom value.<!--end stw2--></div>",
				"//field[@name='stw'][2]", result);
	}

	@Test
	public void shouldAddSymptomValue() throws Exception {
		xslt.transform("src/test/resources/sense_symptomValue.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathExists("//field[@name='stw']", result);
	}

	@Test
	public void shouldAddWordFormation() throws Exception {
		xslt.transform("src/test/resources/sense_wordFormation.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("formation, bla", "//field[@name='wbg_text']", result);
		assertXpathEvaluatesTo("2", "count(//field[@name='wbg'])", result);
		assertXpathEvaluatesTo("<div class=\"highlight-boundary\"><div class=\"italic\"><!--start wbg1--><div class=\"higher-and-smaller\">2</div>formation<!--end wbg1--></div></div>", "//field[@name='wbg'][1]", result);
		assertXpathEvaluatesTo("<div class=\"highlight-boundary\"><!--start wbg2--><div class=\"italic\"><a href=\"bla.h1.0m\">bla</a></div><!--end wbg2--></div>", "//field[@name='wbg'][2]", result);
	}

	@Test
	public void shouldAddWordReference() throws Exception {
		xslt.transform("src/test/resources/sense_wordFormation.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("vgl. lemma.", "//field[@name='wbv_text']", result);
	}

	@Test
	public void shouldTakeFulltextOfWholeArticle() throws Exception {
		xslt.transform("src/test/resources/fulltext.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("Müller," + " " + "2: A quote." + " " + "Ärmel." + " ", "//field[@name='artikel_text']",
				result);
	}

	@Test
	public void shouldInsertSpacesIntoFulltext() throws Exception {
		xslt.transform("src/test/resources/fulltext_spaces.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo(
				"test_lemma, myneblem, die. definition. Name, 346, 35 (reg., M. 14. Jh.) A quote. before space after space. line break. ",
				"//field[@name='artikel_text']", result);
	}

	@Test
	public void shouldRemoveSpecialSpaceInLemma() throws Exception {
		xslt.transform("src/test/resources/lemmaWithSpecialSpace.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("test_lemma", "//field[@name='lemma']", result);
	}

	@Test
	public void shouldRemovePeriodInLemma() throws Exception {
		xslt.transform("src/test/resources/lemmaWithPeriod.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("test_lemma", "//field[@name='lemma']", result);
	}

	@Test
	public void shouldCreateHomonym() throws Exception {
		xslt.transform("src/test/resources/homonym.xml", outputBaos);
		String result = outputBaos.toString();

		assertXpathEvaluatesTo("2", "//field[@name='homonym']", result);
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
