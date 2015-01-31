<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
  <xsl:template match="/imprimatur">
    <xsl:text>[</xsl:text>
    <xsl:for-each select="//request | //comment() | //response-code | //credentials | //refresh | //control | //regex">
      <xsl:choose>
        <xsl:when test="self::request">
          <xsl:variable name="is_first_request"
            select="not(preceding-sibling::request)"/>
          <xsl:variable name="is_first_ever_request"
            select="$is_first_request and not(../preceding-sibling::test)"/>
          <xsl:value-of select="name((parent::node()/preceding-sibling::* | ../preceding-sibling::comment())[position() = 1 and self::test])"/>
          <xsl:if
            test="preceding-sibling::node()[self::request or self::test or self::comment()][position() = 1][self::test or self::request] or ($is_first_request and parent::test/preceding-sibling::node()[self::test or self::comment()][position() = 1][self::test])">
            <xsl:text> },</xsl:text>
          </xsl:if>

          <xsl:text>&#10;    {</xsl:text>

          <xsl:if test="$is_first_request">
            <xsl:text>&#10;        'name': "</xsl:text>
            <xsl:value-of select="parent::node()/attribute::name"/>
            <xsl:text>",</xsl:text>
          </xsl:if>

          <xsl:choose>
            <xsl:when test="@port">
              <xsl:text>&#10;        'port': '</xsl:text>
              <xsl:value-of select="@port"/><xsl:text>',</xsl:text>
            </xsl:when>
            <xsl:when test="$is_first_ever_request and /imprimatur/@port">
              <xsl:text>&#10;        'port': '</xsl:text>
              <xsl:value-of select="/imprimatur/@port"/>
              <xsl:text>',</xsl:text>
            </xsl:when>
          </xsl:choose>

          <xsl:choose>
            <xsl:when test="@scheme">
              <xsl:text>&#10;        'scheme': '</xsl:text>
              <xsl:value-of select="@scheme"/><xsl:text>',</xsl:text>
            </xsl:when>
            <xsl:when test="$is_first_ever_request and /imprimatur/@scheme">
              <xsl:text>&#10;        'scheme': '</xsl:text>
              <xsl:value-of select="/imprimatur/@scheme"/>
              <xsl:text>',</xsl:text>
            </xsl:when>
          </xsl:choose>

          <xsl:choose>
            <xsl:when test="@hostname">
              <xsl:text>&#10;        'host': '</xsl:text>
              <xsl:value-of select="@hostname"/><xsl:text>',</xsl:text>
            </xsl:when>
            <xsl:when test="$is_first_ever_request and /imprimatur/@hostname">
              <xsl:text>&#10;        'host': '</xsl:text>
              <xsl:value-of select="/imprimatur/@hostname"/>
              <xsl:text>',</xsl:text>
            </xsl:when>
          </xsl:choose>

          <xsl:text>&#10;        'path': '</xsl:text>
          <xsl:value-of select="@path"/>
          <xsl:text>',</xsl:text>

          <xsl:if test="@method != 'get'">
            <xsl:text>&#10;        'method': '</xsl:text>
            <xsl:value-of select="@method"/><xsl:text>',</xsl:text>
          </xsl:if>

        </xsl:when>

        <xsl:when test="self::regex">
          <xsl:if test="not(preceding-sibling::regex)">
            <xsl:text>&#10;        'regexes': [</xsl:text>
          </xsl:if>
          <xsl:choose>
            <xsl:when test="contains(@pattern, '&quot;')">
              <xsl:text>&#10;            r'</xsl:text>
              <xsl:value-of select="@pattern"/>
              <xsl:text>'</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>&#10;            r"</xsl:text>
              <xsl:value-of select="@pattern"/>
              <xsl:text>"</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text>,</xsl:text>
          <xsl:if test="not(following-sibling::regex)">
            <xsl:text> ],</xsl:text>
          </xsl:if>
        </xsl:when>

        <xsl:when test="self::response-code">
          <xsl:text>&#10;        'status_code': </xsl:text>
          <xsl:value-of select="attribute::value"/>
          <xsl:text>,</xsl:text>
        </xsl:when>

        <xsl:when test="self::credentials">
          <xsl:text>&#10;        'auth': ('</xsl:text>
          <xsl:value-of select="@username"/>
          <xsl:text>', '</xsl:text>
          <xsl:value-of select="@password"/>
          <xsl:text>'),</xsl:text>
        </xsl:when>

        <xsl:when test="self::refresh">
          <xsl:text>&#10;        'tries': {'max': </xsl:text>
          <xsl:choose>
            <xsl:when test="refresh/@max">
              <xsl:value-of select="refresh/@max"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="10"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text>, 'period': </xsl:text>
          <xsl:choose>
            <xsl:when test="refresh/@wait">
              <xsl:value-of select="refresh/@wait div 1000"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="1"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text>},</xsl:text>
        </xsl:when>

        <xsl:when test="name() = 'control' and @type = 'file'">
          <xsl:text>&#10;        'files': {'</xsl:text>
          <xsl:value-of select="@name"/>
          <xsl:text>': '</xsl:text>
          <xsl:value-of select="@value"/>
          <xsl:text>'},</xsl:text>
        </xsl:when>

        <xsl:when test="self::control and not(attribute::type)">
          <xsl:if test="not(preceding-sibling::control)">
            <xsl:text>&#10;        'data': {</xsl:text>
          </xsl:if>
          <xsl:text>&#10;            '</xsl:text>
          <xsl:value-of select="@name"/><xsl:text>': </xsl:text>
          <xsl:choose>
            <xsl:when test="contains(@value, '&quot;')">
              <xsl:text>'</xsl:text>
              <xsl:value-of select="@value"/>
              <xsl:text>'</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>"</xsl:text>
              <xsl:value-of select="@value"/>
              <xsl:text>"</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="not(following-sibling::control)">
            <xsl:text>},</xsl:text>
          </xsl:if>
        </xsl:when>

        <xsl:when test="self::control and attribute::type">
          <xsl:text>&#10;        'files': {'</xsl:text>
          <xsl:value-of select="@name"/>
          <xsl:text>': '</xsl:text>
          <xsl:value-of select="@value"/>
          <xsl:text>'},</xsl:text>
        </xsl:when>

        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="parent::request">
              <xsl:text>&#10;&#10;        # </xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:if
                  test="preceding-sibling::*[position() = 1][self::test or self::request]">
                <xsl:text> },</xsl:text>
              </xsl:if>
              <xsl:text>&#10;&#10;    # </xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:value-of select="normalize-space(.)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:text> }]</xsl:text>
  </xsl:template>
</xsl:stylesheet>
