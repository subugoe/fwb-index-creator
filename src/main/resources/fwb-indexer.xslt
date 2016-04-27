<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0" xmlns:fwb="http://sub.fwb.de"
  exclude-result-prefixes="fwb">

  <xsl:output method="xml" indent="yes" />
  <xsl:strip-space elements="*" />

  <xsl:param name="previousArticleId" />
  <xsl:param name="currentArticleId" />
  <xsl:param name="nextArticleId" />

  <xsl:param name="previousLemma" />
  <xsl:param name="nextLemma" />

  <xsl:param name="wordTypes" />

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
      <xsl:variable name="wordTypeId" select="fwb:getWordTypeId(@xml:id)" />
      <xsl:variable name="typeValueWithTail" select="substring-after($wordTypes, concat($wordTypeId, ':'))" />
      <xsl:value-of select="substring-before($typeValueWithTail, '###')" />
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

  <xsl:function name="fwb:getWordTypeId">
    <xsl:param name="internalArticleId" />
    <xsl:if test="$internalArticleId != ''">
      <xsl:analyze-string select="$internalArticleId" regex="\.(\d[a-z]+)">
        <xsl:matching-substring>
          <xsl:sequence select="regex-group(1)" />
        </xsl:matching-substring>
      </xsl:analyze-string>
    </xsl:if>
  </xsl:function>

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
        <xsl:text>Phraseme: </xsl:text>
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
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </span>
  </xsl:template>

  <xsl:template match="hi[@rendition='hoch']" mode="html_fulltext">
    <span class="higher-and-smaller">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template match="hi[@rendition='rect']" mode="html_fulltext">
    <span class="rect">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
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
    <xsl:choose>
      <xsl:when test="contains(@target, '#') and number(.)">
        <xsl:variable name="linkStart" select="concat(substring-before(@target, '#'), '#')" />
        <xsl:variable name="linkEnd" select="concat('sense', text())" />
        <xsl:variable name="link" select="concat($linkStart, $linkEnd)" />
        <a href="{$link}">
          <xsl:value-of select="." />
        </a>
      </xsl:when>
      <xsl:when test="matches(@target, '_s\d+$') and number(.)">
        <xsl:variable name="link" select="replace(@target, '_s\d+$', concat('#sense', text()))" />
        <a href="{$link}">
          <xsl:value-of select="." />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{@target}">
          <xsl:value-of select="." />
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="sense" mode="html_fulltext">
    <div class="sense">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="def" mode="html_fulltext">
    <xsl:variable name="senseNumber" select="count(preceding::sense) + 1" />
    <xsl:variable name="senseAnchor" select="concat('sense', $senseNumber)" />
    <div id="{$senseAnchor}" class="definition">
      <xsl:if test="count(//sense) gt 1">
        <span class="sense-number">
          <xsl:value-of select="$senseNumber" />
          <xsl:text>. </xsl:text>
        </span>
      </xsl:if>
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
        <xsl:text>Bedeutungsverwandt: </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='synt']" mode="html_fulltext">
    <div class="synt">
      <span class="synt-begin">
        <xsl:text>Syntagmen: </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbg']" mode="html_fulltext">
    <div class="wbg">
      <span class="wbg-begin">
        <xsl:text>Wortbildungen: </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='cit']" mode="html_fulltext">
    <div class="citations">
      <span class="citations-begin">
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
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='sv']" mode="html_fulltext">
    <div class="subvoce">
      <span class="subvoce-begin">
        <xsl:text>‒ </xsl:text>
      </span>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="cit" mode="html_fulltext">
    <div class="citation">
      <xsl:apply-templates select="*" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="bibl" mode="html_fulltext">
    <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    <xsl:if test="following-sibling::quote">
      <xsl:text>: </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bibl/name[@n]" mode="html_fulltext">
    <xsl:variable name="currentCitationId">
      <xsl_text>source_</xsl_text>
      <xsl:value-of select="@n" />
    </xsl:variable>
    <a class="name citation-source_link" href="/source/{$currentCitationId}">
      <xsl:value-of select="." />
    </a>
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
    <xsl:variable name="currentSenseId" select="count(preceding-sibling::sense) + 1" />
    <doc>
      <field name="type">bedeutung</field>
      <field name="id">
        <xsl:value-of select="$currentArticleId" />
        <xsl:text>_</xsl:text>
        <xsl:value-of select="$currentSenseId" />
      </field>
      <field name="ref_id">
        <xsl:value-of select="$currentArticleId" />
      </field>
      <field name="sense_number">
        <xsl:value-of select="$currentSenseId" />
      </field>
      <field name="definition_fulltext">
        <xsl:value-of select="def" />
      </field>
      <xsl:apply-templates select="dictScrap[@rend='bdv']/ref" />
      <xsl:apply-templates select=".//cit" />
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

  <xsl:template match="cit[quote]">
    <field name="definition_source_id">
      <xsl:text>source_</xsl:text>
      <xsl:value-of select="./bibl/name/@n" />
    </field>
    <field name="definition_source_citation">
      <xsl:value-of select="quote" />
    </field>
  </xsl:template>

  <xsl:template match="cit[not(quote)]">
    <field name="definition_source_instance">
      <xsl:text>source_</xsl:text>
      <xsl:value-of select="./bibl/name/@n" />
    </field>
  </xsl:template>

  <xsl:function name="fwb:print-html-with-spans">
    <xsl:param name="left" />
    <xsl:param name="right" />
    <xsl:if test="$right!=''">
      <div>
        <span class="column-left">
          <xsl:value-of select="$left" />
        </span>
        <span class="column-right">
          <xsl:value-of select="$right" />
        </span>
      </div>
    </xsl:if>
  </xsl:function>

  <xsl:function name="fwb:print-html-with-link">
    <xsl:param name="left" />
    <xsl:param name="right" />
    <xsl:if test="$right!=''">
      <div>
        <span class="column-left">
          <xsl:value-of select="$left" />
        </span>
        <span class="column-right">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$right" />
            </xsl:attribute>
            <xsl:value-of select="$right" />
          </a>
        </span>
      </div>
    </xsl:if>
  </xsl:function>

</xsl:stylesheet>