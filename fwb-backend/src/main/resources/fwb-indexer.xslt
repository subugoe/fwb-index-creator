<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0" xmlns:fwb="http://sub.fwb.de"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:saxon="http://saxon.sf.net/" exclude-result-prefixes="fwb saxon xs">

  <xsl:output method="xml" indent="yes" saxon:suppress-indentation="div a ul li h1 h2" />
  <xsl:strip-space elements="*" />

  <xsl:param name="currentArticleId" />
  <xsl:param name="wordTypes" />
  <xsl:param name="generalWordTypes" />
  <xsl:param name="subfacetWordTypes" />

  <xsl:template match="/">
    <add>
      <doc>
        <field name="type">artikel</field>
        <field name="id">
          <xsl:value-of select="$currentArticleId" />
        </field>
        <xsl:apply-templates select="//teiHeader//sourceDesc/bibl" />
        <xsl:apply-templates select="//teiHeader//notesStmt" />
        <xsl:apply-templates select="//body/entry" />
        <xsl:apply-templates select="//body/entry" mode="fulltext" />
        <xsl:apply-templates select="//body/entry" mode="html_fulltext" />
        <xsl:apply-templates select="//body//sense" />
      </doc>
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

  <xsl:template match="notesStmt">
    <xsl:for-each select="note[@type='orth.de_DE']">
      <field name="sufo">
        <xsl:value-of select="." />
      </field>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="entry">
    <xsl:variable name="internalId" select="@xml:id" />
    <field name="internal_id">
      <xsl:value-of select="$internalId" />
    </field>
    <field name="virtual_id">
      <xsl:value-of select="$internalId" />
    </field>
    <xsl:if test="$internalId != ''">
      <xsl:analyze-string select="$internalId" regex=".*(_[a-zäöüß]_).*">
        <xsl:matching-substring>
          <field name="virtual_id">
            <xsl:value-of select="substring-before(., regex-group(1))" />
            <xsl:value-of select="substring-after(., regex-group(1))" />
          </field>
          <field name="virtual_id">
            <xsl:value-of select="substring-before(., regex-group(1))" />
            <xsl:variable name="groupLength" select="string-length(regex-group(1))" />
            <xsl:value-of select="substring(regex-group(1), 2, $groupLength - 2)" />
            <xsl:value-of select="substring-after(., regex-group(1))" />
          </field>
        </xsl:matching-substring>
      </xsl:analyze-string>
    </xsl:if>
    <xsl:if test="@n">
      <field name="homonym">
        <xsl:value-of select="@n" />
      </field>
    </xsl:if>
    <xsl:if test="form[@type='lemma']/num">
      <field name="roman_number">
        <xsl:value-of select="form[@type='lemma']/num" />
      </field>
    </xsl:if>
    <xsl:variable name="lemma" select="normalize-space(replace(form[@type='lemma']/orth,'\p{Z}+', ' '))" />
    <field name="lemma">
      <xsl:choose>
        <xsl:when test="ends-with($lemma, ',')">
          <xsl:value-of select="normalize-space(substring-before($lemma, ','))" />
        </xsl:when>
        <xsl:when test="ends-with($lemma, '.')">
          <xsl:value-of select="normalize-space(substring-before($lemma, '.'))" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$lemma" />
        </xsl:otherwise>
      </xsl:choose>
    </field>
    <field name="sortkey">
      <xsl:value-of select="@sortKey" />
    </field>
    <xsl:variable name="wordTypeId" select="fwb:getWordTypeId(@xml:id)" />
    <field name="wortart">
      <xsl:variable name="typeValueWithTail" select="substring-after($wordTypes, concat($wordTypeId, ':'))" />
      <xsl:value-of select="substring-before($typeValueWithTail, '###')" />
    </field>
    <field name="wortart_allgemein">
      <xsl:variable name="typeValueWithTail" select="substring-after($generalWordTypes, concat($wordTypeId, ':'))" />
      <xsl:value-of select="substring-before($typeValueWithTail, '###')" />
    </field>
    <field name="wortart_facette">
      <xsl:variable name="typeValueWithTail" select="substring-after($generalWordTypes, concat($wordTypeId, ':'))" />
      <xsl:value-of select="substring-before($typeValueWithTail, '###')" />
    </field>
    <xsl:variable name="typeValueWithTail" select="substring-after($subfacetWordTypes, concat($wordTypeId, ':'))" />
    <xsl:variable name="typeValue" select="substring-before($typeValueWithTail, '###')" />
    <xsl:for-each select="tokenize($typeValue,',')">
      <field name="wortart_subfacette">
        <xsl:value-of select="." />
      </field>
    </xsl:for-each>
    <xsl:apply-templates select="dictScrap[@rend='artkopf']/re[@type='re.neblem']" />
    <field name="neblem_text">
      <xsl:value-of select="dictScrap[@rend='artkopf']/re[@type='re.neblem']" />
    </field>
    <xsl:apply-templates select="dictScrap[@rend='artkopf']/etym" />
    <field name="etym_text">
      <xsl:value-of select="dictScrap[@rend='artkopf']/etym" />
    </field>
    <field name="is_reference">
      <xsl:value-of select="not(sense)" />
    </field>
  </xsl:template>

  <xsl:template match="re[@type='re.neblem']">
    <field name="neblem">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
  </xsl:template>

  <xsl:template match="re[@type='re.ggs']">
    <field name="ggs">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="ggs_text">
      <xsl:value-of select=".//text()" />
    </field>
  </xsl:template>

  <xsl:template match="re[@type='re.bdv']">
    <field name="bdv">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="bdv_text">
      <xsl:value-of select=".//text()" />
    </field>
  </xsl:template>

  <xsl:template match="etym">
    <field name="etym">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
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

  <xsl:template match="entry" mode="fulltext">
    <field name="artikel_text">
      <xsl:apply-templates select="*" mode="fulltext" />
    </field>
  </xsl:template>

  <xsl:template match="hi[@rendition='hoch' and number(.)]" mode="fulltext">
    <xsl:value-of select="." />
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="text()" mode="fulltext">
    <xsl:value-of select="replace(., '\p{Z}+', ' ')" />
    <xsl:variable name="tag" select="local-name(parent::*)" />
    <xsl:variable name="followingText" select="parent::*/following-sibling::text()" />
    <xsl:variable name="tagsFollowedBySpace" select="($tag = 'num' or $tag = 're' or $tag = 'orth' or $tag = 'dictScrap' or $tag = 'def' or $tag = 'bibl' or $tag = 'quote') and not(following-sibling::*) and not(parent::re[@type='re.wbg']) and not(matches($followingText[1],'^[,;.]'))" />
    <xsl:variable name="gramWithoutSibling" select="$tag = 'gram' and not(parent::*/following-sibling::*)" />
    <xsl:variable name="afterLastCitedRange" select="$tag = 'citedRange' and not(parent::*/following-sibling::*) and not(parent::*/following-sibling::text())" />
    <xsl:variable name="beforeLineBreak" select="local-name(following-sibling::*[1]) = 'lb'" />
    <xsl:if test="$tagsFollowedBySpace or $afterLastCitedRange or $beforeLineBreak or $gramWithoutSibling">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="entry" mode="html_fulltext">
    <field name="artikel">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <div class="article">
        <xsl:apply-templates select="*" mode="html_fulltext" />
      </div>
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
  </xsl:template>

  <xsl:template match="*" mode="html_fulltext">
    <xsl:if test=".//text()">
      <xsl:message>
        <xsl:text>Unknown element &lt;</xsl:text>
        <xsl:value-of select="local-name()" />
        <xsl:if test="@rend">
          <xsl:text> rend="</xsl:text>
          <xsl:value-of select="@rend" />
          <xsl:text>"</xsl:text>
        </xsl:if>
        <xsl:if test="@type">
          <xsl:text> type="</xsl:text>
          <xsl:value-of select="@type" />
          <xsl:text>"</xsl:text>
        </xsl:if>
        <xsl:text>&gt; - first occurrence: </xsl:text>
      </xsl:message>
      <span class="unknown-element">
        <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      </span>
    </xsl:if>
  </xsl:template>

  <xsl:template match="form[@type='lemma']" mode="html_fulltext">
    <xsl:variable name="homonym" select="ancestor::entry/@n" />
    <div class="lemma">
      <xsl:if test="$homonym">
        <div class="homonym">
          <xsl:value-of select="$homonym" />
        </div>
      </xsl:if>
      <xsl:value-of select="orth" />
      <xsl:apply-templates select="num" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="num" mode="html_fulltext">
    <div class="roman-number">
      <xsl:text> </xsl:text>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="lang" mode="html_fulltext">
    <div class="language">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="desc" mode="html_fulltext">
    <div class="description">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="re[@type='re.neblem']" mode="html_fulltext">
    <xsl:variable name="neblemNr" select="count(preceding::re[@type='re.neblem']) + 1" />
    <xsl:variable name="neblemId" select="concat('neblem',$neblemNr)" />
    <div class="neblem">
      <xsl:comment>start <xsl:value-of select="$neblemId" /></xsl:comment>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$neblemId" /></xsl:comment>
    </div>
    <xsl:variable name="followingText" select="following-sibling::text()" />
    <xsl:if test="not(matches($followingText[1], '^[,;.]'))">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='artkopf']" mode="html_fulltext">
    <div class="article-head">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="etym" mode="html_fulltext">
    <xsl:variable name="etymNr" select="count(preceding::etym) + 1" />
    <xsl:variable name="etymId" select="concat('etym',$etymNr)" />
    <div class="etymology">
      <xsl:comment>start <xsl:value-of select="$etymId" /></xsl:comment>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$etymId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='phras']" mode="html_fulltext">
    <xsl:variable name="phrasNr" select="count(preceding::dictScrap[@rend='phras']) + 1" />
    <xsl:variable name="phrasId" select="concat('phras',$phrasNr)" />
    <div class="phras">
      <xsl:comment>start <xsl:value-of select="$phrasId" /></xsl:comment>
      <div class="phras-begin">
        <xsl:text>Phraseme: </xsl:text>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$phrasId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ggs']" mode="html_fulltext">
    <div class="ggs">
      <div class="ggs-begin">
        <xsl:text>Gegensätze: </xsl:text>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="re[@type='re.ggs']" mode="html_fulltext" >
    <xsl:apply-templates select="*|text()" mode="html_fulltext" />
  </xsl:template>

  <xsl:template match="re[@type='re.bdv']" mode="html_fulltext" >
    <xsl:apply-templates select="*|text()" mode="html_fulltext" />
  </xsl:template>

  <xsl:template match="re[@type='re.ggs']/ref[not(matches(@target, '_s\d+$') and number(.))]" mode="html_fulltext">
    <xsl:variable name="reggsNr" select="count(preceding::ref) + 1" />
    <xsl:variable name="reggsId" select="concat('reggs',$reggsNr)" />
    <div class="highlight-boundary">
      <xsl:comment>start <xsl:value-of select="$reggsId" /></xsl:comment>
      <xsl:choose>
        <xsl:when test="contains(@target, '#') and number(.)">
          <xsl:variable name="linkStart" select="concat(substring-before(@target, '#'), '#')" />
          <xsl:variable name="linkEnd" select="concat('sense', text())" />
          <xsl:variable name="link" select="concat($linkStart, $linkEnd)" />
          <a href="{$link}">
            <xsl:value-of select="." />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <div class="italic">
            <a href="{@target}">
              <xsl:value-of select="." />
            </a>
          </div>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:comment>end <xsl:value-of select="$reggsId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="re[@type='re.bdv']/ref[not(matches(@target, '_s\d+$') and number(.))]" mode="html_fulltext">
    <xsl:variable name="rebdvNr" select="count(preceding::ref) + 1" />
    <xsl:variable name="rebdvId" select="concat('rebdv',$rebdvNr)" />
    <div class="highlight-boundary">
      <xsl:comment>start <xsl:value-of select="$rebdvId" /></xsl:comment>
      <xsl:choose>
        <xsl:when test="contains(@target, '#') and number(.)">
          <xsl:variable name="linkStart" select="concat(substring-before(@target, '#'), '#')" />
          <xsl:variable name="linkEnd" select="concat('sense', text())" />
          <xsl:variable name="link" select="concat($linkStart, $linkEnd)" />
          <a href="{$link}">
            <xsl:value-of select="." />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <div class="italic">
            <a href="{@target}">
              <xsl:value-of select="." />
            </a>
          </div>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:comment>end <xsl:value-of select="$rebdvId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbg']" mode="html_fulltext">
    <div class="wbg">
      <div class="wbg-begin">
        <xsl:text>Wortbildungen: </xsl:text>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ggs']/ref[not(matches(@target, '_s\d+$') and number(.))]" mode="html_fulltext">
    <xsl:variable name="ggsNr" select="count(preceding::ref[not(matches(@target, '_s\d+$') and number(.))]) + 1" />
    <xsl:variable name="ggsId" select="concat('ggs',$ggsNr)" />
    <div class="highlight-boundary">
      <xsl:comment>start <xsl:value-of select="$ggsId" /></xsl:comment>
      <xsl:choose>
        <xsl:when test="contains(@target, '#') and number(.)">
          <xsl:variable name="linkStart" select="concat(substring-before(@target, '#'), '#')" />
          <xsl:variable name="linkEnd" select="concat('sense', text())" />
          <xsl:variable name="link" select="concat($linkStart, $linkEnd)" />
          <a href="{$link}">
            <xsl:value-of select="." />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <div class="italic">
            <a href="{@target}">
              <xsl:value-of select="." />
            </a>
          </div>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:comment>end <xsl:value-of select="$ggsId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bdv']/ref[not(matches(@target, '_s\d+$') and number(.))]" mode="html_fulltext">
    <xsl:variable name="bdvNr" select="count(preceding::ref[not(matches(@target, '_s\d+$') and number(.))]) + 1" />
    <xsl:variable name="bdvId" select="concat('bdv',$bdvNr)" />
    <div class="highlight-boundary">
      <xsl:comment>start <xsl:value-of select="$bdvId" /></xsl:comment>
      <xsl:choose>
        <xsl:when test="contains(@target, '#') and number(.)">
          <xsl:variable name="linkStart" select="concat(substring-before(@target, '#'), '#')" />
          <xsl:variable name="linkEnd" select="concat('sense', text())" />
          <xsl:variable name="link" select="concat($linkStart, $linkEnd)" />
          <a href="{$link}">
            <xsl:value-of select="." />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <div class="italic">
            <a href="{@target}">
              <xsl:value-of select="." />
            </a>
          </div>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:comment>end <xsl:value-of select="$bdvId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbg']/ref[not(matches(@target, '_s\d+$') and number(.))]" mode="html_fulltext">
    <xsl:variable name="wbgNr" select="count(preceding::ref[not(matches(@target, '_s\d+$') and number(.))]) + count(preceding::re[@type='re.wbg']) + 1" />
    <xsl:variable name="wbgId" select="concat('wbg',$wbgNr)" />
    <div class="highlight-boundary">
      <xsl:comment>start <xsl:value-of select="$wbgId" /></xsl:comment>
      <xsl:choose>
        <xsl:when test="contains(@target, '#') and number(.)">
          <xsl:variable name="linkStart" select="concat(substring-before(@target, '#'), '#')" />
          <xsl:variable name="linkEnd" select="concat('sense', text())" />
          <xsl:variable name="link" select="concat($linkStart, $linkEnd)" />
          <a href="{$link}">
            <xsl:value-of select="." />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <div class="italic">
            <a href="{@target}">
              <xsl:value-of select="." />
            </a>
          </div>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:comment>end <xsl:value-of select="$wbgId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="re[@type='re.wbg']" mode="html_fulltext">
    <xsl:variable name="wbgNr" select="count(preceding::ref[not(matches(@target, '_s\d+$') and number(.))]) + count(preceding::re[@type='re.wbg']) + 1" />
    <xsl:variable name="wbgId" select="concat('wbg',$wbgNr)" />
    <div class="highlight-boundary">
      <div class="italic">
        <xsl:comment>start <xsl:value-of select="$wbgId" /></xsl:comment>
        <xsl:apply-templates select="*|text()" mode="html_fulltext" />
        <xsl:comment>end <xsl:value-of select="$wbgId" /></xsl:comment>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ipLiPkt']" mode="html_fulltext">
    <xsl:apply-templates select="*|text()" mode="html_fulltext" />
  </xsl:template>

  <xsl:template match="dictScrap[@rend='meta']" mode="html_fulltext">
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ra']" mode="html_fulltext">
    <xsl:variable name="raNr" select="count(preceding::dictScrap[@rend='ra']) + 1" />
    <xsl:variable name="raId" select="concat('ra',$raNr)" />
    <div class="redensart">
      <xsl:comment>start <xsl:value-of select="$raId" /></xsl:comment>
      <div class="redensart-begin">
        <xsl:text>Redensart: </xsl:text>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$raId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="text()" mode="html_fulltext">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template match="hi[@rendition='it' and .//text()]" mode="html_fulltext">
    <div class="italic">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="hi[@rendition='hoch' and .//text()]" mode="html_fulltext">
    <div class="higher-and-smaller">
      <xsl:value-of select="." />
    </div>
  </xsl:template>

  <xsl:template match="hi[@rendition='tief' and .//text()]" mode="html_fulltext">
    <div class="deep">
      <xsl:value-of select="." />
    </div>
  </xsl:template>

  <xsl:template match="hi[@rendition='rect' and (.//text() or .//*)]" mode="html_fulltext">
    <div class="rect">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="hi[@rendition='sc' and .//text()]" mode="html_fulltext">
    <div class="small-capitals">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="hi[@rendition='b' and .//text()]" mode="html_fulltext">
    <div class="bold">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="hi[@rendition='wide' and .//text()]" mode="html_fulltext">
    <div class="wide">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="lb" mode="html_fulltext">
    <xsl:text> | </xsl:text>
  </xsl:template>

  <xsl:template match="gram[@type='wortart' and .//text()]" mode="html_fulltext">
    <div class="type-of-word">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="gram[@type='flex' and .//text()]" mode="html_fulltext">
    <div class="flex">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
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
        <xsl:variable name="link" select="concat('#sense', text())" />
        <a href="{$link}">
          <xsl:value-of select="." />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <div class="italic">
          <a href="{@target}">
            <xsl:value-of select="." />
          </a>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="sense[@rend='bedzif']" mode="html_fulltext">
    <xsl:apply-templates mode="html_fulltext" />
  </xsl:template>

  <xsl:template match="sense" mode="html_fulltext">
    <xsl:variable name="senseNumbers" as="xs:integer*">
      <xsl:choose>
        <xsl:when test="@rend and contains(@rend, '-')">
          <xsl:sequence select="xs:integer(substring-before(@rend, '-')) to xs:integer(substring-after(@rend, '-'))" />
        </xsl:when>
        <xsl:when test="@rend">
          <xsl:sequence select="xs:integer(@rend)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>1</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <div class="sense">
      <xsl:for-each select="$senseNumbers">
        <span id="sense{.}">
          <xsl:comment>jump target</xsl:comment>
        </span>
      </xsl:for-each>
      <xsl:for-each-group select="*" 
        group-adjacent="if (self::dictScrap[@rend='ipLiPkt']) then 1
         else if (self::dictScrap[@rend='BBlock' or @rend='cit' or @rend='bls' or @rend='sv']) then 2
         else 3">
        <xsl:choose>
          <xsl:when test="current-grouping-key() = 1">
            <div class="info-list-with-header">
              <xsl:variable name="pre-sib" select="preceding-sibling::*[1]" />
              <xsl:if test="$pre-sib[local-name() = 'dictScrap' and @rend = 'meta']">
                <h3>
                  <xsl:apply-templates select="$pre-sib/*|$pre-sib/text()" mode="html_fulltext" />
                </h3>
              </xsl:if>
              <ul class="info-list">
                <xsl:for-each select="current-group()">
                  <li>
                    <xsl:apply-templates select="." mode="html_fulltext" />
                  </li>
                </xsl:for-each>
              </ul>
            </div>
          </xsl:when>
          <xsl:when test="current-grouping-key() = 2">
            <section class="citations-block">
              <xsl:for-each-group select="current-group()" group-starting-with="dictScrap[@rend='BBlock']">
                <div class="citations-subblock">
                  <xsl:apply-templates select="current-group()" mode="html_fulltext" />
                </div>
              </xsl:for-each-group>
            </section>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="current-group()">
              <xsl:apply-templates select="." mode="html_fulltext" />
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </div>
  </xsl:template>

  <xsl:template match="def" mode="html_fulltext">
    <xsl:variable name="defNumber" select="count(preceding::def) + 1" />
    <xsl:variable name="defAnchor" select="concat('def', $defNumber)" />
    <xsl:variable name="senseRendNumber" select="parent::sense/@rend" />
    <div class="definition">
      <xsl:comment>start <xsl:value-of select="$defAnchor" /></xsl:comment>
      <xsl:variable name="isFirst" select="not(preceding-sibling::def)" />
      <xsl:if test="$senseRendNumber and $isFirst">
        <div class="sense-number">
          <xsl:call-template name="printRendNumbers">
            <xsl:with-param name="rendNumberString" select="$senseRendNumber" />
          </xsl:call-template>
        </div>
      </xsl:if>
      <xsl:apply-templates select="text()|*" mode="html_fulltext" />
      <xsl:apply-templates select="following-sibling::*[1][@rend='wbv']"
        mode="html_fulltext_once" />
      <xsl:comment>end <xsl:value-of select="$defAnchor" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template name="printRendNumbers">
    <xsl:param name="rendNumberString" />
    <xsl:choose>
      <xsl:when test="number($rendNumberString)">
        <xsl:value-of select="$rendNumberString" />
        <xsl:text>. </xsl:text>
      </xsl:when>
      <xsl:when test="contains($rendNumberString, '-')">
        <xsl:variable name="from" select="substring-before($rendNumberString, '-')" />
        <xsl:variable name="to" select="substring-after($rendNumberString, '-')" />
        <xsl:variable name="numbersList" as="xs:integer*" >
          <xsl:sequence select="xs:integer($from) to xs:integer($to)" />
        </xsl:variable>
        <xsl:value-of select="$numbersList" separator=".; "/>
        <xsl:text>., </xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbv']" mode="html_fulltext_once">
    <xsl:variable name="wbvNumber" select="count(preceding::dictScrap[@rend='wbv']) + 1" />
    <xsl:variable name="wbvId" select="concat('wbv', $wbvNumber)" />
    <div class="wbv">
      <xsl:comment>start <xsl:value-of select="$wbvId" /></xsl:comment>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$wbvId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbv']" mode="html_fulltext">
    <xsl:if test="not(preceding-sibling::*[1][local-name() = 'def'])">
      <xsl:apply-templates select="." mode="html_fulltext_once" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='stw']" mode="html_fulltext">
    <xsl:variable name="stwNumber" select="count(preceding::dictScrap[@rend='stw']) + 1" />
    <xsl:variable name="stwId" select="concat('stw', $stwNumber)" />
    <div class="stw">
      <xsl:comment>start <xsl:value-of select="$stwId" /></xsl:comment>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$stwId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bdv']" mode="html_fulltext">
    <div class="bdv">
      <div class="bdv-begin">
        <xsl:text>Bedeutungsverwandte: </xsl:text>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='synt']" mode="html_fulltext">
    <xsl:variable name="syntNumber" select="count(preceding::dictScrap[@rend='synt']) + 1" />
    <xsl:variable name="syntId" select="concat('synt', $syntNumber)" />
    <div class="synt">
      <xsl:comment>start <xsl:value-of select="$syntId" /></xsl:comment>
      <div class="synt-begin">
        <xsl:text>Syntagmen: </xsl:text>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$syntId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='cit']" mode="html_fulltext">
    <xsl:call-template name="printCitationsHeader" />
    <div class="citations">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bls' and cit]" mode="html_fulltext">
    <xsl:call-template name="printCitationsHeader" />
    <div class="bls">
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bls' and not(cit)]" mode="html_fulltext">
    <xsl:call-template name="printCitationsHeader" />
    <p>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </p>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='BBlock']" mode="html_fulltext">
    <xsl:call-template name="printCitationsHeader" />
    <h2>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
    </h2>
  </xsl:template>

  <xsl:template name="printCitationsHeader">
    <xsl:variable name="isFirst" select="not(preceding-sibling::dictScrap[@rend='BBlock' or @rend='cit' or @rend='bls'])" />
    <xsl:if test="$isFirst">
      <h1>
        <xsl:text>Belegblock: </xsl:text>
      </h1>
    </xsl:if>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ref']" mode="html_fulltext">
    <xsl:variable name="refNumber" select="count(preceding::dictScrap[@rend='ref']) + 1" />
    <xsl:variable name="refId" select="concat('zursache', $refNumber)" />
    <div class="dict-ref">
      <xsl:comment>start <xsl:value-of select="$refId" /></xsl:comment>
      <div class="dict-ref-begin">
        <xsl:choose>
          <xsl:when test="ends-with(normalize-space(text()[1]), ':')">
            <xsl:text>Zur Sache </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Zur Sache: </xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$refId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='sv']" mode="html_fulltext">
    <xsl:variable name="svNumber" select="count(preceding::dictScrap[@rend='sv']) + 1" />
    <xsl:variable name="svId" select="concat('subvoce', $svNumber)" />
    <div class="subvoce">
      <xsl:comment>start <xsl:value-of select="$svId" /></xsl:comment>
      <div class="subvoce-begin">
        <xsl:text>‒ </xsl:text>
      </div>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$svId" /></xsl:comment>
    </div>
  </xsl:template>

  <xsl:template match="cit" mode="html_fulltext">
    <div class="citation">
      <xsl:apply-templates select="*" mode="html_fulltext" />
    </div>
  </xsl:template>

  <xsl:template match="bibl" mode="html_fulltext">
    <xsl:if test="preceding-sibling::*[1]/local-name() = 'bibl' and not(parent::etym) and not(parent::def)">
      <xsl:text> </xsl:text>
    </xsl:if>
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
    <xsl:if test="not(../..[@rend='artkopf']) and not(ancestor::quote) and not(ancestor::etym) and not(ancestor::def)">
      <xsl:text> </xsl:text>
    </xsl:if>
    <a class="name citation-source_link" href="/source/{$currentCitationId}">
      <xsl:value-of select="." />
    </a>
  </xsl:template>

  <xsl:template match="name" mode="html_fulltext">
    <div class="name">
      <xsl:value-of select="." />
    </div>
  </xsl:template>

  <xsl:template match="citedRange[.//text()]" mode="html_fulltext">
    <div class="cited-range">
      <xsl:value-of select="." />
    </div>
  </xsl:template>

  <xsl:template match="region[.//text()]" mode="html_fulltext">
    <div class="region">
      <xsl:value-of select="." />
    </div>
  </xsl:template>

  <xsl:template match="date[.//text()]" mode="html_fulltext">
    <div class="date">
      <xsl:value-of select="." />
    </div>
  </xsl:template>

  <xsl:template match="quote[.//text()]" mode="html_fulltext">
    <xsl:variable name="quoteNr" select="count(preceding::quote) + 1" />
    <xsl:variable name="quoteId" select="concat('quote',$quoteNr)" />
    <div class="quote" id="{$quoteId}">
      <xsl:comment>start <xsl:value-of select="$quoteId" /></xsl:comment>
      <xsl:apply-templates select="*|text()" mode="html_fulltext" />
      <xsl:comment>end <xsl:value-of select="$quoteId" /></xsl:comment>
    </div>
  </xsl:template>


  <xsl:template match="sense">
      <xsl:apply-templates select="def[.//text()]" />
      <xsl:call-template name="printDefTextAndNumber" />
      <xsl:apply-templates select="dictScrap[@rend='bdv']" />
      <xsl:apply-templates select="dictScrap[@rend='sv']" />
      <xsl:apply-templates select="dictScrap[@rend='ggs']" />
      <xsl:apply-templates select=".//cit" />
      <xsl:apply-templates select="dictScrap[@rend='synt']" />
      <xsl:apply-templates select="dictScrap[@rend='phras']" />
      <xsl:apply-templates select="dictScrap[@rend='ra']" />
      <xsl:apply-templates select="dictScrap[@rend='stw']" />
      <xsl:apply-templates select="dictScrap[@rend='ref']" />
      <xsl:apply-templates select="dictScrap[@rend='ipLiPkt']" />
      <xsl:apply-templates select="dictScrap[@rend='wbg']" />
      <xsl:apply-templates select="dictScrap[@rend='BBlock']" />
      <xsl:apply-templates select="dictScrap[@rend='wbv']" />
      <xsl:apply-templates select=".//re[@type='re.ggs']" />
      <xsl:apply-templates select=".//re[@type='re.bdv']" />
  </xsl:template>

  <xsl:template match="lb">
    <xsl:text> / </xsl:text>
  </xsl:template>

  <xsl:template name="printDefTextAndNumber">
    <xsl:if test="def[.//text()]">
      <field name="bed_text">
        <xsl:value-of select="def" separator=" " />
      </field>
    </xsl:if>
    <xsl:if test="@rend and @rend != 'bedzif'">
      <field name="def_number">
        <xsl:call-template name="printDefinitionNumber">
          <xsl:with-param name="rendNumber" select="@rend" />
        </xsl:call-template>
      </field>
    </xsl:if>
  </xsl:template>

  <xsl:template match="def[.//text()]">
    <field name="bed">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
  </xsl:template>

  <xsl:template name="printDefinitionNumber">
    <xsl:param name="rendNumber" />
    <xsl:choose>
      <xsl:when test="number($rendNumber)">
        <xsl:value-of select="$rendNumber" />
        <xsl:text>.</xsl:text>
      </xsl:when>
      <xsl:when test="contains($rendNumber, '-')">
        <xsl:value-of select="substring-before($rendNumber, '-')" />
        <xsl:text>.-</xsl:text>
        <xsl:value-of select="substring-after($rendNumber, '-')" />
        <xsl:text>.</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='synt']">
    <field name="synt">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="synt_text">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbv']">
    <field name="wbv">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext_once" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="wbv_text">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='stw']">
    <field name="stw">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="stw_text">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='phras' or @rend='ra']">
    <field name="phras">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="phras_text">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ref']">
    <field name="zursache">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="zursache_text">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbg']">
    <xsl:for-each select="re[@type='re.wbg'] | ref[not(matches(@target, '_s\d+$') and number(.))]">
      <field name="wbg">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:apply-templates select="." mode="html_fulltext" />
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </field>
    </xsl:for-each>
    <field name="wbg_text">
      <xsl:apply-templates select="re[@type='re.wbg'] | ref[not(matches(@target, '_s\d+$') and number(.))]" />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ipLiPkt']">
    <xsl:for-each select="re[@type='re.wbg']">
      <field name="wbg">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:apply-templates select="." mode="html_fulltext" />
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </field>
    </xsl:for-each>
    <field name="wbg_text">
      <xsl:apply-templates select="re[@type='re.wbg']" />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='BBlock']">
    <xsl:for-each select="re[@type='re.wbg']">
      <field name="wbg">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:apply-templates select="." mode="html_fulltext" />
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </field>
    </xsl:for-each>
    <field name="wbg_text">
      <xsl:apply-templates select="re[@type='re.wbg']" />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbg']/re[@type='re.wbg']">
    <xsl:if test="preceding-sibling::re[@type='re.wbg']">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ipLiPkt']/re[@type='re.wbg']">
    <xsl:if test="preceding-sibling::re[@type='re.wbg']">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="dictScrap[@rend='BBlock']/re[@type='re.wbg']">
    <xsl:if test="preceding-sibling::re[@type='re.wbg']">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="dictScrap[@rend='wbg']/ref">
    <xsl:if test="preceding-sibling::ref or preceding-sibling::re[@type='re.wbg']">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bdv']">
    <xsl:for-each select="ref[not(matches(@target, '_s\d+$') and number(.))]">
      <field name="bdv">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:apply-templates select="." mode="html_fulltext" />
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </field>
    </xsl:for-each>
    <field name="bdv_text">
      <xsl:apply-templates select="ref[not(matches(@target, '_s\d+$') and number(.))]" />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='bdv']/ref">
    <xsl:if test="preceding-sibling::ref">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ggs']">
    <xsl:for-each select="ref[not(matches(@target, '_s\d+$') and number(.))]">
      <field name="ggs">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:apply-templates select="." mode="html_fulltext" />
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </field>
    </xsl:for-each>
    <field name="ggs_text">
      <xsl:apply-templates select="ref[not(matches(@target, '_s\d+$') and number(.))]" />
    </field>
  </xsl:template>

  <xsl:template match="dictScrap[@rend='ggs']/ref">
    <xsl:if test="preceding-sibling::ref">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="dictScrap[@rend='sv']">
    <field name="subvoce">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="." mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="subvoce_text">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="cit[quote]">
    <field name="definition_source_id">
      <xsl:text>source_</xsl:text>
      <xsl:value-of select="./bibl/name/@n" />
    </field>
    <field name="zitat">
      <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
      <xsl:apply-templates select="quote" mode="html_fulltext" />
      <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
    </field>
    <field name="zitat_text">
      <xsl:apply-templates select="quote//lb | quote//text()" />
    </field>
    <xsl:apply-templates select=".//region | .//date" />
  </xsl:template>

  <xsl:template match="cit//region">
    <field name="region">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template match="cit//date">
    <field name="datum">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template match="cit[not(quote)]">
    <field name="definition_source_instance">
      <xsl:text>source_</xsl:text>
      <xsl:value-of select="./bibl/name/@n" />
    </field>
    <xsl:apply-templates select=".//region | .//date" />
  </xsl:template>

</xsl:stylesheet>