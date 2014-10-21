package eu.fox7.rexp.isc.cnfa.core;

import eu.fox7.rexp.data.CharSymbol;
import eu.fox7.rexp.data.Symbol;
import eu.fox7.rexp.data.UniSymbolWord;
import eu.fox7.rexp.data.Word;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.cnfa.guard.Guard;
import eu.fox7.rexp.isc.fast.cnfa.ArrayGuard;
import eu.fox7.rexp.isc.fast.cnfa.ArrayRe2Cnfa;
import eu.fox7.rexp.isc.fast.cnfa.ArrayValuation;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.base.RegExpUtil;
import eu.fox7.rexp.tree.nfa.lw.NfaState;
import eu.fox7.rexp.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

public class CnfaRunner {
	public static void main(String[] args) {
		Director.setup();
		String reStr = "a{65536,65536}";
		RegExp re = RegExpUtil.parseString(reStr);
		ArrayRe2Cnfa rc = new ArrayRe2Cnfa();
		Cnfa cnfa = rc.apply(re);
		Word word = new UniSymbolWord(new CharSymbol('a'), 65536);
		Log.i("Initiating run");
		System.out.println(CnfaRunner.apply(cnfa, word));
	}

	public static boolean apply(Cnfa cnfa, Word word) {
		CnfaRunner r = new CnfaRunner(cnfa);
		r.consume(word);
		if (nonDeterminismWarning && r.configurations.size() > 1) {
			Log.d("CNFA execution was non-determinstic, not a CDFA.");
		}
		return r.isAccepting();
	}

	protected static class CnfaConfig {
		protected NfaState state;
		protected Valuation valuation;

		public CnfaConfig(NfaState state, Valuation valuation) {
			this.state = state;
			this.valuation = valuation;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final CnfaConfig other = (CnfaConfig) obj;
			if (this.state != other.state && (this.state == null || !this.state.equals(other.state))) {
				return false;
			}
			if (this.valuation != other.valuation && (this.valuation == null || !this.valuation.equals(other.valuation))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 13 * hash + (this.state != null ? this.state.hashCode() : 0);
			hash = 13 * hash + (this.valuation != null ? this.valuation.hashCode() : 0);
			return hash;
		}
	}

	private final Cnfa cnfa;
	private Set<CnfaConfig> configurations;
	public static boolean nonDeterminismWarning = true;

	public CnfaRunner(Cnfa cnfa) {
		this.cnfa = cnfa;
		configurations = new LinkedHashSet<CnfaConfig>();

		int size = 0;
		for (CnfaTransition e : cnfa.delta.entrySet()) {
			ArrayGuard g = (ArrayGuard) e.guard;
			size = g.size();
			break;
		}
		NfaState q = cnfa.getInitialState();
		Valuation v = new ArrayValuation(size);
		configurations.add(new CnfaConfig(q, v));
	}

	public void consume(Word word) {
		for (Symbol s : word) {
			consume(s);
		}
	}

	public void consume(Symbol s) {
		Set<CnfaConfig> nextConfigs = new LinkedHashSet<CnfaConfig>();
		for (CnfaConfig c : configurations) {
			Set<CnfaTransition> qs = cnfa.getTransitionMap().srcMap.get(c.state);
			if (qs != null) {
				for (CnfaTransition tr : qs) {
					if (tr.symbol.equals(s)) {
						if (tr.guard.evaluate(c.valuation)) {
							Valuation v = c.valuation.clone();
							tr.update.applyTo(v);
							nextConfigs.add(new CnfaConfig(tr.target, v));
						}
					}
				}
			}
		}
		configurations = nextConfigs;
	}

	public boolean isAccepting() {
		for (CnfaConfig c : configurations) {
			Guard g = cnfa.acceptance.get(c.state);
			if (g != null && g.evaluate(c.valuation)) {
				return true;
			}
		}
		return false;
	}
}
