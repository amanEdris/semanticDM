<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
        <rules>
            <xsl:for-each select="//Node[not(Node)]">
                
                <xsl:for-each select="ancestor-or-self::Node/SimplePredicate">
                    <rule>
                        <if name="{@field}" operator="{@operator}" value="{@value}"/>
                        <xsl:if test="position() = last()">
                            <xsl:param name="p" select="../@score"/>
                            <xsl:choose>
                                <xsl:when test="contains($p,'&lt;=')">
                                    <then  class="{//MiningField[@usageType = 'target']/@name}" score="{../@score}" operator="lessOrEqual" />
                                </xsl:when>
                                <xsl:when test="contains($p,'&gt;=')">
                                    <then  class="{//MiningField[@usageType = 'target']/@name}" score="{../@score}" operator="greaterOrEqual" />
                                </xsl:when>
                                <xsl:when test="contains($p,'&lt;') and not(contains($p,'&gt;='))">
                                    <then  class="{//MiningField[@usageType = 'target']/@name}" score="{../@score}" operator="lessThan" />
                                </xsl:when>
                                <xsl:when test="contains($p,'&gt;') and not(contains($p,'&gt;='))">
                                    <then  class="{//MiningField[@usageType = 'target']/@name}" score="{../@score}" operator="greaterThan" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <then  class="{//MiningField[@usageType = 'target']/@name}" score="{../@score}" operator="equal" />                         
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </rule>
                </xsl:for-each>
            
                <xsl:text>&#10;</xsl:text>
            </xsl:for-each>
        </rules>
    </xsl:template>

</xsl:stylesheet>
