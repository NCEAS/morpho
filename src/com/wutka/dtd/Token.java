package com.wutka.dtd;

/** Token returned by the lexical scanner
 *
 * @author Mark Wutka
 * @version $Revision: 1.2 $ $Date: 2001-01-15 02:21:25 $ by $Author: higgins $
 */
class Token
{
	public TokenType type;
	public String value;

	public Token(TokenType aType)
	{
		type = aType;
		value = null;
	}

	public Token(TokenType aType, String aValue)
	{
		type = aType;
		value = aValue;
	}
}
