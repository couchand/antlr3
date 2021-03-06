/*
 * [The "BSD license"]
 * Copyright (c) 2011 Terence Parr
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.antlr.test;

import org.junit.Test;

import static org.junit.Assert.*;

/** test runtime parse errors */
public class TestSyntaxErrors extends BaseTest {
	@Test public void testLL2() throws Exception {
		String grammar =
			"grammar T;\n" +
			"a : 'a' 'b'" +
			"  | 'a' 'c'" +
			";\n" +
			"q : 'e' ;\n";
		String found = execParser("T.g", grammar, "TParser", "TLexer", "a", "ae", false);
		String expecting = "line 1:1 no viable alternative at input 'e'\n";
		String result = stderrDuringParse;
		assertEquals(expecting, result);
	}

	@Test public void testLL3() throws Exception {
		String grammar =
			"grammar T;\n" +
			"a : 'a' 'b'* 'c'" +
			"  | 'a' 'b' 'd'" +
			"  ;\n" +
			"q : 'e' ;\n";
		System.out.println(grammar);
		String found = execParser("T.g", grammar, "TParser", "TLexer", "a", "abe", false);
		String expecting = "line 1:2 no viable alternative at input 'e'\n";
		String result = stderrDuringParse;
		assertEquals(expecting, result);
	}

	@Test public void testLLStar() throws Exception {
		String grammar =
			"grammar T;\n" +
			"a : 'a'+ 'b'" +
			"  | 'a'+ 'c'" +
			";\n" +
			"q : 'e' ;\n";
		String found = execParser("T.g", grammar, "TParser", "TLexer", "a", "aaae", false);
		String expecting = "line 1:3 no viable alternative at input 'e'\n";
		String result = stderrDuringParse;
		assertEquals(expecting, result);
	}

	@Test public void testSynPred() throws Exception {
		String grammar =
			"grammar T;\n" +
			"a : (e '.')=> e '.'" +
			"  | (e ';')=> e ';'" +
			"  | 'z'" +
			"  ;\n" +
			"e : '(' e ')'" +
			"  | 'i'" +
			"  ;\n";
		System.out.println(grammar);
		String found = execParser("T.g", grammar, "TParser", "TLexer", "a", "((i))z", false);
		String expecting = "line 1:1 no viable alternative at input '('\n";
		String result = stderrDuringParse;
		assertEquals(expecting, result);
	}

	@Test public void testLL1ErrorInfo() throws Exception {
		String grammar =
			"grammar T;\n" +
			"start : animal (AND acClass)? service EOF;\n" +
			"animal : (DOG | CAT );\n" +
			"service : (HARDWARE | SOFTWARE) ;\n" +
			"AND : 'and';\n" +
			"DOG : 'dog';\n" +
			"CAT : 'cat';\n" +
			"HARDWARE: 'hardware';\n" +
			"SOFTWARE: 'software';\n" +
			"WS : ' ' {skip();} ;" +
			"acClass\n" +
			"@init\n" +
			"{ System.out.println(computeContextSensitiveRuleFOLLOW().toString(tokenNames)); }\n" +
			"  : ;\n";
		String result = execParser("T.g", grammar, "TParser", "TLexer", "start", "dog and software", false);
		String expecting = "{HARDWARE,SOFTWARE}\n";
		assertEquals(expecting, result);
	}
}
