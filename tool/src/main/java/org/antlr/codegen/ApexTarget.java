/*
 * [The "BSD license"]
 *  Copyright (c) 2010 Terence Parr
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.antlr.codegen;

import org.antlr.Tool;
import org.antlr.tool.Grammar;
import org.antlr.tool.Rule;
import org.stringtemplate.v4.ST;

import java.util.Set;

public class ApexTarget extends Target {
	public static char QUOTE_MARK = '\'';

	public ApexTarget() {
		targetCharValueEscape['\''] = "\\'";
	}
	/** Convert from an ANTLR string literal found in a grammar file to
	 *  an equivalent string literal in the target language.  For Java, this
	 *  is the translation 'a\n"' -> "a\n\"".  Expect single quotes
	 *  around the incoming literal.  Just flip the quotes and replace
	 *  double quotes with \"
	 *
	 *  Note that we have decided to allow poeple to use '\"' without
	 *  penalty, so we must build the target string in a loop as Utils.replae
	 *  cannot handle both \" and " without a lot of messing around.
	 *
	 */
        @Override
	public String getTargetStringLiteralFromANTLRStringLiteral(
		CodeGenerator generator,
		String literal)
	{
		StringBuilder sb = new StringBuilder();
		StringBuilder is = new StringBuilder(literal);

		// Opening quote
		//
		sb.append(QUOTE_MARK);

		for (int i = 1; i < is.length() -1; i++) {
		    if  (is.charAt(i) == '\\') {
			// Anything escaped is what it is! We assume that
			// people know how to escape characters correctly. However
			// we catch anything that does not need an escape in Java (which
			// is what the default implementation is dealing with and remove
			// the escape. The C target does this for instance.
			//
			switch (is.charAt(i+1)) {
			    // Pass through any escapes that Apex also needs
			    //
			    case    '\'':
			    case    'n':
			    case    'r':
			    case    't':
			    case    'b':
			    case    'f':
			    case    '\\':
			    case    'u':    // Assume unnnn
				sb.append('\\');    // Pass the escape through
				break;
			    default:
				// Remove the escape by virtue of not adding it here
				// Thus \' becomes ' and so on
				//
				break;
			}

			// Go past the \ character
			//
			i++;
		    } else {
			// Chracters that don't need \ in ANTLR 'strings' but do in Java
			//
			if (is.charAt(i) == QUOTE_MARK) {
			    // We need to escape " in Java
			    //
			    sb.append('\\');
			}
		    }
		    // Add in the next character, which may have been escaped
		    //
		    sb.append(is.charAt(i));
		}

		// Append closing " and return
		//
		sb.append(QUOTE_MARK);

		return sb.toString();
	}

	/** Given a random string of Java unicode chars, return a new string with
	 *  optionally appropriate quote characters for target language and possibly
	 *  with some escaped characters.  For example, if the incoming string has
	 *  actual newline characters, the output of this method would convert them
	 *  to the two char sequence \n for Java, C, C++, ...  The new string has
	 *  double-quotes around it as well.  Example String in memory:
	 *
	 *     a"[newlinechar]b'c[carriagereturnchar]d[tab]e\f
	 *
	 *  would be converted to the valid Java s:
	 *
	 *     "a\"\nb'c\rd\te\\f"
	 *
	 *  or
	 *
	 *     a\"\nb'c\rd\te\\f
	 *
	 *  depending on the quoted arg.
	 */
	@Override
	public String getTargetStringLiteralFromString(String s, boolean quoted) {
		if ( s==null ) {
			return null;
		}

		StringBuilder buf = new StringBuilder();
		if ( quoted ) {
			buf.append(QUOTE_MARK);
		}
		for (int i=0; i<s.length(); i++) {
			int c = s.charAt(i);
			if ( c!='\"' && // don't escape double quotes in strings for Apex
				 c<targetCharValueEscape.length &&
				 targetCharValueEscape[c]!=null )
			{
				buf.append(targetCharValueEscape[c]);
			}
			else {
				buf.append((char)c);
			}
		}
		if ( quoted ) {
			buf.append(QUOTE_MARK);
		}
		return buf.toString();
	}

    @Override
    public boolean useBaseTemplatesForSynPredFragments() {
        return false;
    }

	protected ST chooseWhereCyclicDFAsGo(Tool tool,
										 CodeGenerator generator,
										 Grammar grammar,
										 ST recognizerST,
										 ST cyclicDFAST)
	{
		return recognizerST;
	}

	@Override
	protected void performGrammarAnalysis(CodeGenerator generator, Grammar grammar) {
		super.performGrammarAnalysis(generator, grammar);
		for (Rule rule : grammar.getRules()) {
			rule.throwsSpec.add("RecognitionException");
		}
		Set<? extends Rule> delegatedRules = grammar.getDelegatedRules();
		if ( delegatedRules!=null ) {
			for (Rule rule : delegatedRules) {
				rule.throwsSpec.add("RecognitionException");
			}
		}
	}
}
