<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0">

  <xsl:output method="xml" indent="yes" />
  <xsl:strip-space elements="*" />

  <xsl:template match="/">
    <add>
      <doc>
        <field name="type">artikel</field>
        <xsl:apply-templates />
      </doc>
    </add>
  </xsl:template>

  <xsl:template match="//bibl[@type='printedSource']">
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

</xsl:stylesheet>