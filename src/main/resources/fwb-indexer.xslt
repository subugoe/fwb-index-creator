<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0">

  <xsl:output method="xml" indent="yes" />
  <xsl:strip-space elements="*" />

  <xsl:template match="/">
    <add>
      <doc>
        <field name="type">artikel</field>
        <xsl:apply-templates select="//teiHeader//sourceDesc/bibl" />
        <xsl:apply-templates select="//body/entry" />
      </doc>
      <xsl:apply-templates select="//body//sense" />
    </add>
  </xsl:template>

  <xsl:template match="bibl[@type='printedSource']">
    <field name="printedSource">
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
    <xsl:variable name="lemma" select="form[@type='lemma']/orth" />
    <field name="lemma">
      <xsl:value-of select="$lemma" />
    </field>
    <field name="lemma_normalized">
      <xsl:choose>
        <xsl:when test="ends-with($lemma, ',')">
          <xsl:value-of select="substring-before($lemma, ',')" />
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
  </xsl:template>

  <xsl:template match="sense">
    <doc>
      <field name="type">bedeutung</field>
      <field name="definition_fulltext">
        <xsl:value-of select="def" />
      </field>
    </doc>
  </xsl:template>

</xsl:stylesheet>