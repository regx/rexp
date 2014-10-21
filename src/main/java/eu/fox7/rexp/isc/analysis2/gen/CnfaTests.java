package eu.fox7.rexp.isc.analysis2.gen;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis2.gen.EvalTreeUpdateOp.*;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.util.Log;

import java.util.logging.Level;

public class CnfaTests {
	public static void main(String[] args) {
		Director.setup();
		Log.configureRootLogger(Level.FINER);
		CnfaTester2.singleTest();
	}

	private final CnfaTester2 runner;

	public CnfaTests(CnfaTester2 runner) {
		this.runner = runner;
	}

	public void test1(RegExp re, Word word) {
		CharSymbol a = new CharSymbol('a');
		CharSymbol b = new CharSymbol('b');
		runner.updateOp = new Replace(2, b);
		runner.process(re, word);
		runner.updateOp = new Replace(2, b);
		runner.process(re, word);
		runner.updateOp = new Append(b);
		runner.process(re, word);
		runner.updateOp = new Replace(3, a);
		runner.process(re, word);
		runner.updateOp = new Insert(2, a);
		runner.process(re, word);
		runner.updateOp = new Delete(3);
		runner.process(re, word);
		runner.updateOp = new Insert(1, b);
		runner.process(re, word);
		runner.updateOp = new Delete(3);
		runner.process(re, word);
		runner.updateOp = new Append(a);
		runner.process(re, word);
		runner.updateOp = new Replace(0, b);
		runner.process(re, word);
		runner.updateOp = new Insert(0, a);
		runner.process(re, word);
		runner.updateOp = new Replace(4, a);
		runner.process(re, word);
		runner.updateOp = new Delete(4);
		runner.process(re, word);
	}

	public void test2(RegExp re, Word word) {
		char c = 'd';
		runner.updateOp = new Assign("abc");
		runner.process(re, word);
		runner.updateOp = new Append(c++);
		runner.process(re, word);
		runner.updateOp = new Replace(0, c++);
		runner.process(re, word);
		runner.updateOp = new Insert(1, c++);
		runner.process(re, word);
		runner.updateOp = new Append(c++);
		runner.process(re, word);
		runner.updateOp = new Delete(5);
		runner.process(re, word);
		runner.updateOp = new Append(c++);
		runner.process(re, word);
		runner.updateOp = new Delete(3);
		runner.process(re, word);
		runner.updateOp = new Insert(1, c++);
		runner.process(re, word);
		runner.updateOp = new Delete(5);
		runner.process(re, word);
		runner.updateOp = new Insert(4, c++);
		runner.process(re, word);
	}

	public void test3(RegExp re, Word word) {
		runner.updateOp = new Assign("abcdef");
		runner.process(re, word);
		runner.updateOp = new Insert(0, 'e');
		runner.process(re, word);
		runner.updateOp = new Insert(4, 'f');
		runner.process(re, word);
		runner.updateOp = new Delete(6);
		runner.process(re, word);
	}

	public void test4(RegExp re, Word word) {
		runner.updateOp = new Assign("ab");
		runner.process(re, word);
		runner.updateOp = new Delete(1);
		runner.process(re, word);
		runner.updateOp = new Append('c');
		runner.process(re, word);
		runner.updateOp = new Insert(0, 'd');
		runner.process(re, word);
		runner.updateOp = new Insert(1, 'e');
		runner.process(re, word);
		runner.updateOp = new Insert(0, 'f');
		runner.process(re, word);
		runner.updateOp = new Delete(4);
		runner.process(re, word);
		runner.updateOp = new Delete(1);
		runner.process(re, word);
	}

	public void test5(RegExp re, Word word) {
		runner.updateOp = new Reset("(a{3,3}|b{4,4}){3,4}");
		runner.process(re, word);
		runner.updateOp = new Assign("aaaaaaaaa");
		runner.process(re, word);

		runner.updateOp = new Insert(0, 'a');
		runner.process(re, word);
		runner.updateOp = new Delete(1);
		runner.process(re, word);
		runner.updateOp = new Insert(2, 'a');
		runner.process(re, word);
		runner.updateOp = new Insert(4, 'a');
		runner.process(re, word);
	}

	public void test6(RegExp re, Word word) {
		System.out.println("^^^^");
		runner.updateOp = new Reset("(a{2,2}){2,3}");
		runner.process(re, word);
		runner.updateOp = new Assign("aaaa");
		runner.process(re, word);
		System.out.println("$$$$");

		runner.updateOp = new Insert(0, 'a');
		runner.process(re, word);
		runner.updateOp = new Delete(1);
		runner.process(re, word);
		runner.updateOp = new Insert(2, 'a');
		runner.process(re, word);
		runner.updateOp = new Insert(1, 'a');
		runner.process(re, word);
	}

	public void test7(RegExp re, Word word) {
		System.out.println("^^^^");
		runner.updateOp = new Reset("(a{2,2}){2,3}");
		runner.process(re, word);
		runner.updateOp = new Assign("abcd");
		runner.process(re, word);
		System.out.println("$$$$");

		runner.updateOp = new Insert(0, 'e');
		runner.process(re, word);
		runner.updateOp = new Delete(1);
		runner.process(re, word);
		runner.updateOp = new Insert(2, 'f');
		runner.process(re, word);
		runner.updateOp = new Insert(1, 'g');
		runner.process(re, word);
	}

	public void test(RegExp re, Word word) {
		test6(re, word);
	}
}
