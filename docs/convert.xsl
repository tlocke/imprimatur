<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
  <xsl:template match="/imprimatur">
    <xsl:text>[</xsl:text>
    <xsl:for-each select="test | comment()">
      <xsl:choose>
        <xsl:when test="name() = 'test'">
          <xsl:variable name="name" select="@name" /> 
          <xsl:variable name="test_pos" select="position()" /> 

          <xsl:for-each select="request | comment()">
            <xsl:choose>
              <xsl:when test="name() = 'request'">
                <xsl:text>&#10;    {</xsl:text>

                <xsl:variable
                  name="is_first" select="position() = 1 and $test_pos = 1" /> 

                <xsl:if test="position() = 1">

                  <xsl:text>&#10;        'name': "</xsl:text>
                  <xsl:value-of select="$name"/>
                  <xsl:text>",</xsl:text>
                </xsl:if>

                <xsl:choose>
                  <xsl:when test="@scheme">
                    <xsl:text>&#10;        'scheme': '</xsl:text>
                    <xsl:value-of select="@scheme"/><xsl:text>',</xsl:text>
                  </xsl:when>
                  <xsl:when test="$is_first and /imprimatur/@scheme">
                    <xsl:text>&#10;        'scheme': '</xsl:text>
                    <xsl:value-of select="/imprimatur/@scheme"/>
                    <xsl:text>',</xsl:text>
                  </xsl:when>
                </xsl:choose>

                <xsl:choose>
                  <xsl:when test="@port">
                    <xsl:text>&#10;        'port': '</xsl:text>
                    <xsl:value-of select="@port"/><xsl:text>',</xsl:text>
                  </xsl:when>
                  <xsl:when test="$is_first and /imprimatur/@port">
                    <xsl:text>&#10;        'port': '</xsl:text>
                    <xsl:value-of select="/imprimatur/@port"/>
                    <xsl:text>',</xsl:text>
                  </xsl:when>
                </xsl:choose>

                <xsl:choose>
                  <xsl:when test="@hostname">
                    <xsl:text>&#10;        'host': '</xsl:text>
                    <xsl:value-of select="@hostname"/><xsl:text>',</xsl:text>
                  </xsl:when>
                  <xsl:when test="$is_first and /imprimatur/@hostname">
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

                <xsl:for-each
                    select="credentials | control | refresh | response-code | comment()">
                  <xsl:choose>
                    <xsl:when test="name() = 'credentials'">
                      <xsl:text>&#10;        'auth': ('</xsl:text>
                      <xsl:value-of select="@username"/>
                      <xsl:text>', '</xsl:text>
                      <xsl:value-of select="@password"/>
                      <xsl:text>'),</xsl:text>
                    </xsl:when>

                    <xsl:when test="name() = 'control' and @type = 'file'">
                      <xsl:text>&#10;        'files': {'</xsl:text>
                      <xsl:value-of select="@name"/>
                      <xsl:text>': '</xsl:text>
                      <xsl:value-of select="@value"/>
                      <xsl:text>'},</xsl:text>
                    </xsl:when>

                    <xsl:when test="name() = 'control' and not(@type)">
                      <xsl:text>&#10;        'data': {</xsl:text>
                      <xsl:for-each select="control[not(@type)]">
                        <xsl:text>&#10;            '</xsl:text>
                        <xsl:value-of select="@name"/><xsl:text>': '</xsl:text>
                        <xsl:value-of select="@value"/><xsl:text>',</xsl:text>
                      </xsl:for-each>
                      <xsl:text>},</xsl:text>
                    </xsl:when>

                    <xsl:when test="name() = 'refresh'">
                      <xsl:text>&#10;        'tries': {'number': </xsl:text>
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

                    <xsl:when test="name() = 'response-code'">
                      <xsl:text>&#10;        'status_code': </xsl:text>
                      <xsl:value-of select="@value"/>
                      <xsl:text>,</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:text>&#10;&#10;        #</xsl:text>
                      <xsl:value-of select="."/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
                <xsl:if test="regex">
                  <xsl:text>&#10;        'regexes': [</xsl:text>
                  <xsl:for-each select="regex">
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
                  </xsl:for-each>
                  <xsl:text>]</xsl:text>
                </xsl:if>
                <xsl:text>},</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>&#10;&#10;    # </xsl:text>
                <xsl:value-of select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>&#10;&#10;    #</xsl:text>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
  </xsl:template>
</xsl:stylesheet>
