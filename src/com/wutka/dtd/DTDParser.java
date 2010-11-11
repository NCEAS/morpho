package com.wutka.dtd;

import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

/** Parses a DTD file and returns a DTD object
 *
 * @author Mark Wutka
 * @version $Revision: 1.3 $ $Date: 2001-06-06 22:44:57 $ by $Author: higgins $
 */
public class DTDParser implements EntityExpansion
{
    protected Scanner scanner;
    protected DTD dtd;
    protected Object defaultLocation;

/** Creates a parser that will read from the specified Reader object */
    public DTDParser(Reader in)
    {
        scanner = new Scanner(in, false, this);
        dtd = new DTD();
    }

/** Creates a parser that will read from the specified Reader object
 * @param in The input stream to read
 * @param trace True if the parser should print out tokens as it reads them
 *  (used for debugging the parser)
 */
    public DTDParser(Reader in, boolean trace)
    {
        scanner = new Scanner(in, trace, this);
        dtd = new DTD();
    }

/** Creates a parser that will read from the specified File object */
    public DTDParser(File in)
        throws IOException
    {
        defaultLocation = new File(in.getParent());

        scanner = new Scanner(
        		new BufferedReader(
        				new InputStreamReader(new FileInputStream(in), Charset.forName("UTF-8"))),
            false, this);
        dtd = new DTD();
    }

/** Creates a parser that will read from the specified File object
 * @param in The file to read
 * @param trace True if the parser should print out tokens as it reads them
 *  (used for debugging the parser)
 */
    public DTDParser(File in, boolean trace)
        throws IOException
    {
        defaultLocation = in.getParentFile();

        scanner = 
        	new Scanner(
        		new BufferedReader(
        				new InputStreamReader(new FileInputStream(in), Charset.forName("UTF-8"))),
        				trace, 
        				this);
        dtd = new DTD();
    }

/** Creates a parser that will read from the specified URL object */
    public DTDParser(URL in)
        throws IOException
    {
        defaultLocation = in;

        scanner = new Scanner(new BufferedReader(
            new InputStreamReader(in.openStream())), false, this);
        dtd = new DTD();
    }

/** Creates a parser that will read from the specified URL object
 * @param in The URL to read
 * @param trace True if the parser should print out tokens as it reads them
 *  (used for debugging the parser)
 */
    public DTDParser(URL in, boolean trace)
        throws IOException
    {
        defaultLocation = in;

        scanner = new Scanner(new BufferedReader(
            new InputStreamReader(in.openStream())), trace, this);
        dtd = new DTD();
    }

/** Parses the DTD file and returns a DTD object describing the DTD.
    This invocation of parse does not try to guess the root element
    (for efficiency reasons) */
    public DTD parse()
        throws IOException
    {
        return parse(false);
    }

/** Parses the DTD file and returns a DTD object describing the DTD.
 * @param guessRootElement If true, tells the parser to try to guess the
          root element of the document by process of elimination
 */
    public DTD parse(boolean guessRootElement)
        throws IOException
    {
        Token token;

        for (;;)
        {
            token = scanner.peek();

            if (token.type == Scanner.EOF) break;

            parseTopLevelElement();
        }

        if (guessRootElement)
        {
            Hashtable roots = new Hashtable();

            Enumeration e = dtd.elements.elements();

            while (e.hasMoreElements())
            {
                DTDElement element = (DTDElement) e.nextElement();
                roots.put(element.name, element);
            }

            e = dtd.elements.elements();
            while (e.hasMoreElements())
            {
                DTDElement element = (DTDElement) e.nextElement();
                if (!(element.content instanceof DTDContainer)) continue;

                Enumeration items = ((DTDContainer) element.content).
                    getItemsVec().  elements();

                while (items.hasMoreElements())
                {
                    removeElements(roots, dtd, (DTDItem) items.nextElement());
                }
            }

            if (roots.size() == 1)
            {
                e = roots.elements();
                dtd.rootElement = (DTDElement) e.nextElement();
            }
            else
            {
                dtd.rootElement = null;
            }
        }
        else
        {
            dtd.rootElement = null;
        }

        return dtd;
    }

    protected void removeElements(Hashtable h, DTD dtd, DTDItem item)
    {
        if (item instanceof DTDName)
        {
            h.remove(((DTDName) item).value);
        }
        else if (item instanceof DTDContainer)
        {
            Enumeration e = ((DTDContainer) item).getItemsVec().elements();

            while (e.hasMoreElements())
            {
                removeElements(h, dtd, (DTDItem) e.nextElement());
            }
        }
    }

    protected void parseTopLevelElement()
        throws IOException
    {
        Token token = scanner.get();

// Is <? xxx ?> even valid in a DTD?  I'll ignore it just in case it's there
        if (token.type == Scanner.LTQUES)
        {
            StringBuffer textBuffer = new StringBuffer();

            for (;;)
            {
                String text = scanner.getUntil('?');
                textBuffer.append(text);

                token = scanner.peek();
                if (token.type == Scanner.GT)
                {
                    scanner.get();
                    break;
                }
                textBuffer.append('?');
            }
            DTDProcessingInstruction instruct =
                new DTDProcessingInstruction(textBuffer.toString());

            dtd.items.addElement(instruct);

            return;
        }
        else if (token.type == Scanner.CONDITIONAL)
        {
            token = expect(Scanner.IDENTIFIER);

            if (token.value.equals("IGNORE"))
            {
                scanner.skipConditional();
            }
            else
            {
                if (token.value.equals("INCLUDE"))
                {
                    scanner.skipUntil('[');
                }
                else
                {
                    throw new DTDParseException(scanner.getUriId(),
                        "Invalid token in conditional: "+token.value,
                        scanner.getLineNumber(), scanner.getColumn());
                }
            }
        }
        else if (token.type == Scanner.ENDCONDITIONAL)
        {
            // Don't need to do anything for this token
        }
        else if (token.type == Scanner.COMMENT)
        {
            dtd.items.addElement(
                new DTDComment(token.value));
        }
        else if (token.type == Scanner.LTBANG)
        {

            token = expect(Scanner.IDENTIFIER);

            if (token.value.equals("ELEMENT"))
            {
                parseElement();
            }
            else if (token.value.equals("ATTLIST"))
            {
                parseAttlist();
            }
            else if (token.value.equals("ENTITY"))
            {
                parseEntity();
            }
            else if (token.value.equals("NOTATION"))
            {
                parseNotation();
            }
            else
            {
                skipUntil(Scanner.GT);
            }
        }
    }

    protected void skipUntil(TokenType stopToken)
        throws IOException
    {
        Token token = scanner.get();

        while (token.type != stopToken)
        {
            token = scanner.get();
        }
    }

    protected Token expect(TokenType expected)
        throws IOException
    {
        Token token = scanner.get();

        if (token.type != expected)
        {
            if (token.value == null)
            {
                throw new DTDParseException(scanner.getUriId(),
                            "Expected "+expected.name+" instead of "+token.type.name,
                            scanner.getLineNumber(), scanner.getColumn());
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(),
                            "Expected "+expected.name+
                                " instead of "+ token.type.name+"("+token.value+")",
                            scanner.getLineNumber(), scanner.getColumn());
            }
        }

        return token;
    }

    protected void parseElement()
        throws IOException
    {
        Token name = expect(Scanner.IDENTIFIER);

        DTDElement element = (DTDElement) dtd.elements.get(name.value);

        if (element == null)
        {
            element = new DTDElement(name.value);
            dtd.elements.put(element.name, element);
        }

        dtd.items.addElement(element);
        parseContentSpec(scanner, element);

        expect(Scanner.GT);
    }

    protected void parseContentSpec(Scanner scanner, DTDElement element)
        throws IOException
    {
        Token token = scanner.get();

        if (token.type == Scanner.IDENTIFIER)
        {
            if (token.value.equals("EMPTY"))
            {
                element.content = new DTDEmpty();
            }
            else if (token.value.equals("ANY"))
            {
                element.content = new DTDAny();
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(),
                    "Invalid token in entity content spec "+
                        token.value,
                        scanner.getLineNumber(), scanner.getColumn());
            }
        }
        else if (token.type == Scanner.LPAREN)
        {
            token = scanner.peek();

            if (token.type == Scanner.IDENTIFIER)
            {
                if (token.value.equals("#PCDATA"))
                {
                    parseMixed(element);
                }
                else
                {
                    parseChildren(element);
                }
            }
            else if (token.type == Scanner.LPAREN)
            {
                parseChildren(element);
            }
        }
    }

    protected void parseMixed(DTDElement element)
        throws IOException
    {
        DTDMixed mixed = new DTDMixed();

        mixed.add(new DTDPCData());

        scanner.get();

        element.content = mixed;

        for (;;)
        {
            Token token = scanner.get();

            if (token.type == Scanner.RPAREN)
            {
                token = scanner.peek();

                if (token.type == Scanner.ASTERISK)
                {
                    scanner.get();
                    mixed.cardinal = DTDCardinal.ZEROMANY;
                }
                else
                {
                    mixed.cardinal = DTDCardinal.NONE;
                }

                return;
            }
            else if (token.type == Scanner.PIPE)
            {
                token = scanner.get();

                mixed.add(new DTDName(token.value));
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(),
                                "Invalid token in Mixed content type: "+
                                token.type.name, scanner.getLineNumber(), scanner.getColumn());
            }
        }
    }

    protected void parseChildren(DTDElement element)
        throws IOException
    {
        DTDContainer choiceSeq = parseChoiceSequence();

        Token token = scanner.peek();

        choiceSeq.cardinal = parseCardinality();

        if (token.type == Scanner.QUES)
        {
            choiceSeq.cardinal = DTDCardinal.OPTIONAL;
        }
        else if (token.type == Scanner.ASTERISK)
        {
            choiceSeq.cardinal = DTDCardinal.ZEROMANY;
        }
        else if (token.type == Scanner.PLUS)
        {
            choiceSeq.cardinal = DTDCardinal.ONEMANY;
        }
        else
        {
            choiceSeq.cardinal = DTDCardinal.NONE;
        }

        element.content = choiceSeq;
    }

    protected DTDContainer parseChoiceSequence()
        throws IOException
    {
        TokenType separator = null;

        DTDContainer cs = null;

        for (;;)
        {
            DTDItem item = parseCP();

            Token token = scanner.get();

            if ((token.type == Scanner.PIPE) ||
                (token.type == Scanner.COMMA))
            {
                if ((separator != null) && (separator != token.type))
                {
                    throw new DTDParseException(scanner.getUriId(),
                        "Can't mix separators in a choice/sequence",
                        scanner.getLineNumber(), scanner.getColumn());
                }
                separator = token.type;

                if (cs == null)
                {
                    if (token.type == Scanner.PIPE)
                    {
                        cs = new DTDChoice();
                    }
                    else
                    {
                        cs = new DTDSequence();
                    }
                }
                cs.add(item);
            }
            else if (token.type == Scanner.RPAREN)
            {
                if (cs == null)
                {
                    cs = new DTDChoice();
                }
                cs.add(item);
                return cs;
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(),
                                "Found invalid token in sequence: "+
                    token.type.name, scanner.getLineNumber(), scanner.getColumn());
            }
        }
    }

    protected DTDItem parseCP()
        throws IOException
    {
        Token token = scanner.get();

        DTDItem item = null;

        if (token.type == Scanner.IDENTIFIER)
        {
            item = new DTDName(token.value);
        }
        else if (token.type == Scanner.LPAREN)
        {
            item = parseChoiceSequence();
        }
        else
        {
            throw new DTDParseException(scanner.getUriId(),
                            "Found invalid token in sequence: "+
                            token.type.name, scanner.getLineNumber(),
                            scanner.getColumn());
        }

        item.cardinal = parseCardinality();

        return item;
    }

    protected DTDCardinal parseCardinality()
        throws IOException
    {
        Token token = scanner.peek();

        if (token.type == Scanner.QUES)
        {
            scanner.get();
            return DTDCardinal.OPTIONAL;
        }
        else if (token.type == Scanner.ASTERISK)
        {
            scanner.get();
            return DTDCardinal.ZEROMANY;
        }
        else if (token.type == Scanner.PLUS)
        {
            scanner.get();
            return DTDCardinal.ONEMANY;
        }
        else
        {
            return DTDCardinal.NONE;
        }
    }

    protected void parseAttlist()
        throws IOException
    {
        Token token = expect(Scanner.IDENTIFIER);

        DTDElement element = (DTDElement) dtd.elements.get(token.value);

        DTDAttlist attlist = new DTDAttlist(token.value);

        dtd.items.addElement(attlist);

        if (element == null)
        {
            element = new DTDElement(token.value);
            dtd.elements.put(token.value, element);
        }

        token = scanner.peek();

        while (token.type != Scanner.GT)
        {
            parseAttdef(scanner, element, attlist);
            token = scanner.peek();
        }
    }

    protected void parseAttdef(Scanner scanner, DTDElement element,
        DTDAttlist attlist)
        throws IOException
    {
        Token token = expect(Scanner.IDENTIFIER);

        DTDAttribute attr = new DTDAttribute(token.value);

        attlist.attributes.addElement(attr);

        element.attributes.put(token.value, attr);

        token = scanner.get();

        if (token.type == Scanner.IDENTIFIER)
        {
            if (token.value.equals("NOTATION"))
            {
                attr.type = parseNotationList();
            }
            else
            {
                attr.type = token.value;
            }
        }
        else if (token.type == Scanner.LPAREN)
        {
            attr.type = parseEnumeration();
        }

        token = scanner.peek();

        if (token.type == Scanner.IDENTIFIER)
        {
            scanner.get();
            if (token.value.equals("#FIXED"))
            {
                attr.decl = DTDDecl.FIXED;

                token = scanner.get();
                attr.defaultValue = token.value;
            }
            else if (token.value.equals("#REQUIRED"))
            {
                attr.decl = DTDDecl.REQUIRED;
            }
            else if (token.value.equals("#IMPLIED"))
            {
                attr.decl = DTDDecl.IMPLIED;
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(),
                    "Invalid token in attribute declaration: "+
                    token.value, scanner.getLineNumber(), scanner.getColumn());
            }
        }
        else if (token.type == Scanner.STRING)
        {
            scanner.get();
            attr.decl = DTDDecl.VALUE;
            attr.defaultValue = token.value;
        }
    }

    protected DTDNotationList parseNotationList()
        throws IOException
    {
        DTDNotationList notation = new DTDNotationList();

        for (;;)
        {
            Token token = scanner.get();

            if (token.type != Scanner.IDENTIFIER)
            {
                throw new DTDParseException(scanner.getUriId(),
                                "Invalid token in notation: "+
                                token.type.name, scanner.getLineNumber(),
                                scanner.getColumn());
            }

            notation.add(token.value);

            token = scanner.peek();

            if (token.type == Scanner.RPAREN)
            {
                scanner.get();
                return notation;
            }
            else if (token.type != Scanner.PIPE)
            {
                throw new DTDParseException(scanner.getUriId(),
                                "Invalid token in notation: "+
                                token.type.name, scanner.getLineNumber(),
                                scanner.getColumn());
            }
            scanner.get(); // eat the pipe
        }
    }

    protected DTDEnumeration parseEnumeration()
        throws IOException
    {
        DTDEnumeration enumeration = new DTDEnumeration();

        for (;;)
        {
            Token token = scanner.get();

            if ((token.type != Scanner.IDENTIFIER) &&
                (token.type != Scanner.NMTOKEN))
            {
                throw new DTDParseException(scanner.getUriId(),
                                "Invalid token in enumeration: "+
                                token.type.name, scanner.getLineNumber(),
                                scanner.getColumn());
            }

            enumeration.add(token.value);

            token = scanner.peek();

            if (token.type == Scanner.RPAREN)
            {
                scanner.get();
                return enumeration;
            }
            else if (token.type != Scanner.PIPE)
            {
                throw new DTDParseException(scanner.getUriId(),
                                "Invalid token in enumeration: "+
                                token.type.name, scanner.getLineNumber(),
                                scanner.getColumn());
            }
            scanner.get(); // eat the pipe
        }
    }

    protected void parseEntity()
        throws IOException
    {
        boolean isParsed = false;

        Token name = scanner.get();

        if (name.type == Scanner.PERCENT)
        {
            isParsed = true;
            name = expect(Scanner.IDENTIFIER);
        }
        else if (name.type != Scanner.IDENTIFIER)
        {
            throw new DTDParseException(scanner.getUriId(),
                            "Invalid entity declaration",
                            scanner.getLineNumber(), scanner.getColumn());
        }

        DTDEntity entity = (DTDEntity) dtd.entities.get(name.value);

        if (entity == null)
        {
            entity = new DTDEntity(name.value, defaultLocation);
            dtd.entities.put(entity.name, entity);
        }

        dtd.items.addElement(entity);

        entity.isParsed = isParsed;

        parseEntityDef(entity);

        if (entity.isParsed && (entity.value != null))
        {
            scanner.addEntity(entity.name, entity.value);
        }
    }

    protected void parseEntityDef(DTDEntity entity)
        throws IOException
    {
        Token token = scanner.get();

        if (token.type == Scanner.STRING)
        {
            entity.value = token.value;
        }
        else if (token.type == Scanner.IDENTIFIER)
        {
            if (token.value.equals("SYSTEM"))
            {
                DTDSystem sys = new DTDSystem();
                token = expect(Scanner.STRING);

                sys.system = token.value;
                entity.externalID = sys;
            }
            else if (token.value.equals("PUBLIC"))
            {
                DTDPublic pub = new DTDPublic();

                token = expect(Scanner.STRING);
                pub.pub = token.value;
                token = expect(Scanner.STRING);
                pub.system = token.value;
                entity.externalID = pub;
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(),
                                "Invalid External ID specification",
                                scanner.getLineNumber(), scanner.getColumn());
            }

            if (entity.isParsed)
            {
                token = scanner.peek();
                if (token.type == Scanner.IDENTIFIER)
                {
                    if (!token.value.equals("NDATA"))
                    {
                        throw new DTDParseException(scanner.getUriId(),
                            "Invalid NData declaration",
                            scanner.getLineNumber(), scanner.getColumn());
                    }
                    token = expect(Scanner.IDENTIFIER);
                    entity.ndata = token.value;
                }
            }
        }
        else
        {
            throw new DTDParseException(scanner.getUriId(),
                            "Invalid entity definition",
                            scanner.getLineNumber(), scanner.getColumn());
        }

        expect(Scanner.GT);
    }

    protected void parseNotation()
        throws java.io.IOException
    {
        DTDNotation notation = new DTDNotation();

        Token token = expect(Scanner.IDENTIFIER);

        notation.name = token.value;

        dtd.notations.put(notation.name, notation);
        dtd.items.addElement(notation);

        token = expect(Scanner.IDENTIFIER);

        if (token.value.equals("SYSTEM"))
        {
            DTDSystem sys = new DTDSystem();
            token = expect(Scanner.STRING);

            sys.system = token.value;
            notation.externalID = sys;
        }
        else if (token.value.equals("PUBLIC"))
        {
            DTDPublic pub = new DTDPublic();
            token = expect(Scanner.STRING);

            pub.pub = token.value;
            pub.system = null;

// For <!NOTATION>, you can have PUBLIC PubidLiteral without
// a SystemLiteral
            token = scanner.peek();
            if (token.type == Scanner.STRING)
            {
                token = scanner.get();
                pub.system = token.value;
            }

            notation.externalID = pub;
        }
        expect(Scanner.GT);
    }

    public DTDEntity expandEntity(String name)
    {
        return (DTDEntity) dtd.entities.get(name);
    }
}
