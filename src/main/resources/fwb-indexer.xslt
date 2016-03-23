<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0">

  <xsl:output method="xml" indent="yes" />
  <xsl:strip-space elements="*" />

  <xsl:param name="previousArticleId" />
  <xsl:param name="currentArticleId" />
  <xsl:param name="nextArticleId" />

  <xsl:param name="previousLemma" />
  <xsl:param name="nextLemma" />


  <xsl:template match="/">
    <add>
      <doc>
        <field name="type">artikel</field>
        <field name="id">
          <xsl:value-of select="$currentArticleId" />
        </field>
        <field name="article_previous_id">
          <xsl:value-of select="$previousArticleId" />
        </field>
        <field name="article_previous_lemma">
          <xsl:value-of select="$previousLemma" />
        </field>
        <field name="article_next_id">
          <xsl:value-of select="$nextArticleId" />
        </field>
        <field name="article_next_lemma">
          <xsl:value-of select="$nextLemma" />
        </field>
        <xsl:apply-templates select="//teiHeader//sourceDesc/bibl" />
        <xsl:apply-templates select="//body/entry" />
        <xsl:apply-templates select="//body/entry" mode="html_fulltext" />
      </doc>
      <xsl:apply-templates select="//body//sense" />
    </add>
  </xsl:template>

  <xsl:template match="bibl[@type='printedSource']">
    <field name="printed_source">
      <xsl:value-of select="title" />
    </field>
    <field name="volume">
      <xsl:value-of select="biblScope[@type='vol']" />
    </field>
    <field name="col">
      <xsl:value-of select="biblScope[@type='col']" />
    </field>
  </xsl:template>

  <xsl:template match="entry">
    <field name="internal_id">
      <xsl:value-of select="@xml:id" />
    </field>
    <xsl:variable name="lemma" select="normalize-space(form[@type='lemma']/orth)" />
    <field name="lemma">
      <xsl:value-of select="$lemma" />
    </field>
    <field name="lemma_normalized">
      <xsl:choose>
        <xsl:when test="ends-with($lemma, ',')">
          <xsl:value-of select="normalize-space(substring-before($lemma, ','))" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$lemma" />
        </xsl:otherwise>
      </xsl:choose>
    </field>
    <field name="type_of_word">
      <xsl:value-of select="dictScrap[@rend='artkopf']/gram[@type='wortart'][1]" />
    </field>
    <xsl:variable name="variants" select="dictScrap[@rend='artkopf']/hi[@rendition='it'][1]" />
    <xsl:variable name="variants_tokenized" select="tokenize($variants, ',\s*')" />
    <xsl:for-each select="$variants_tokenized">
      <field name="notation_variant">
        <xsl:value-of select="." />
      </field>
    </xsl:for-each>
    <field name="is_reference">
      <xsl:value-of select="not(sense)" />
    </field>
  </xsl:template>

  <xsl:template match="entry" mode="html_fulltext">
    <field name="article_html">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <div class="article">
        <xsl:apply-templates select="*" mode="html_fulltext" />
      </div>
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
  </xsl:template>

  <xsl:template match="*" mode="html_fulltext">
    <missing>
      <xsl:value-of select="local-name()" />
      <xsl:text>, </xsl:text>
      <xsl:value-of select="@rend" />
      <xsl:text>: </xsl:text>
      <xsl:value-of select="text()" />
    </missing>
  </xsl:template>

  <xsl:template match="form[@type='lemma']" mode="html_fulltext">
    <div class="lemma">
      <xsl:value-of select="orth" />
    </div>
  </xsl:template>

  <xsl:template match="form[@type='neblem']" mode="html_fulltext">
    <span class="neblem">
      <xsl:value-of select="orth" />
    </span>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='artkopf']" mode="html_fulltext">
    <div class="article-head">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='phras']" mode="html_fulltext">
    <div class="phras">
      <span class="phras-begin">
        <xsl:text>Phras.: </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ggs']" mode="html_fulltext">
    <div class="ggs">
      <span class="ggs-begin">
        <xsl:text>Ggs.: </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="text()" mode="html_fulltext">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template match="hi[@rendition='it']" mode="html_fulltext">
    <span class="italic">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="hi[@rendition='hoch']" mode="html_fulltext">
    <span class="higher-and-smaller">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="hi[@rendition='rect']" mode="html_fulltext">
    <span class="rect">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="lb" mode="html_fulltext">
    <xsl:text> / </xsl:text>
  </xsl:template>

  <xsl:template match="gram[@type='wortart']" mode="html_fulltext">
    <span class="type-of-word">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="gram[@type='flex']" mode="html_fulltext">
    <span class="flex">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </span>
  </xsl:template>

  <xsl:template match="oRef" mode="html_fulltext">
    <xsl:text>-</xsl:text>
  </xsl:template>

  <xsl:template match="ref" mode="html_fulltext">
    <a href="{@target}">
      <xsl:value-of select="." />
    </a>
  </xsl:template>

  <xsl:template match="sense" mode="html_fulltext">
    <div class="sense">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="def" mode="html_fulltext">
    <div class="definition">
      <xsl:apply-templates select="text()|*" mode="html_fulltext" />
      <xsl:apply-templates select="following-sibling::dictScrap[@rend='wbv']"
        mode="html_fulltext_once" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbv']" mode="html_fulltext_once">
    <span class="wbv">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </span>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbv']" mode="html_fulltext">
  </xsl:template>

  <xsl:template match="dictScrap[@rend='stw']" mode="html_fulltext">
    <div class="stw">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bdv']" mode="html_fulltext">
    <div class="bdv">
      <span class="bdv-begin">
        <xsl:text>Bdv.: </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='synt']" mode="html_fulltext">
    <div class="synt">
      <span class="synt-begin">
        <xsl:text>Synt. </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbg']" mode="html_fulltext">
    <div class="wbg">
      <span class="wbg-begin">
        <xsl:text>Wbg. </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='cit']" mode="html_fulltext">
    <div class="cites">
      <span class="cites-begin">
        <xsl:text>Quellenzitate: </xsl:text>
      </span>
      <xsl:apply-templates select="*" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ref']" mode="html_fulltext">
    <div class="dict-ref">
      <span class="dict-ref-begin">
        <xsl:text>Zur Sache: </xsl:text>
      </span>
      <xsl:apply-templates select="*" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='sv']" mode="html_fulltext">
    <div class="subvoce">
      <xsl:text>‒</xsl:text>
      <xsl:apply-templates select="*" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="cit" mode="html_fulltext">
    <div class="cite">
      <xsl:apply-templates select="*" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="bibl" mode="html_fulltext">
    <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    <xsl:if test="following-sibling::quote">
      <xsl:text>: </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="name" mode="html_fulltext">
    <span class="name">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="citedRange" mode="html_fulltext">
    <span class="cited-range">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="region|date" mode="html_fulltext">
    <span class="{local-name()}">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="quote" mode="html_fulltext">
    <span class="quote">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </span>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bls']" mode="html_fulltext">
    <div class="bls">
      <span class="bls-begin">
        <xsl:text>Belegstellenangaben: </xsl:text>
      </span>
      <xsl:apply-templates select="*" mode="html_fulltext" />
    </div>
  </xsl:template>


  <xsl:template match="sense">
    <doc>
      <field name="type">bedeutung</field>
      <field name="id">
        <xsl:value-of select="$currentArticleId" />
        <xsl:text>_</xsl:text>
        <xsl:value-of select="count(preceding-sibling::sense) + 1" />
      </field>
      <field name="ref_id">
        <xsl:value-of select="$currentArticleId" />
      </field>
      <field name="definition_fulltext">
        <xsl:value-of select="def" />
      </field>
      <xsl:apply-templates select="dictScrap[@rend='bdv']/ref" />
    </doc>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bdv']/ref">
    <xsl:if test="not(number(.))">
      <field name="article_related_id">
        <xsl:value-of select="@target" />
      </field>
      <field name="article_related_lemma">
        <xsl:value-of select="." />
      </field>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>