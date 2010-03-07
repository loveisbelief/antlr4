package org.antlr.v4.automata;

import java.util.ArrayList;
import java.util.List;

/** A DFA state represents a set of possible NFA configurations.
 *  As Aho, Sethi, Ullman p. 117 says "The DFA uses its state
 *  to keep track of all possible states the NFA can be in after
 *  reading each input symbol.  That is to say, after reading
 *  input a1a2..an, the DFA is in a state that represents the
 *  subset T of the states of the NFA that are reachable from the
 *  NFA's start state along some path labeled a1a2..an."
 *  In conventional NFA->DFA conversion, therefore, the subset T
 *  would be a bitset representing the set of states the
 *  NFA could be in.  We need to track the alt predicted by each
 *  state as well, however.  More importantly, we need to maintain
 *  a stack of states, tracking the closure operations as they
 *  jump from rule to rule, emulating rule invocations (method calls).
 *  Recall that NFAs do not normally have a stack like a pushdown-machine
 *  so I have to add one to simulate the proper lookahead sequences for
 *  the underlying LL grammar from which the NFA was derived.
 *
 *  I use a list of NFAConfig objects.  An NFAConfiguration
 *  is both a state (ala normal conversion) and an NFAContext describing
 *  the chain of rules (if any) followed to arrive at that state.  There
 *  is also the semantic context, which is the "set" of predicates found
 *  on the path to this configuration.
 *
 *  A DFA state may have multiple references to a particular state,
 *  but with different NFAContexts (with same or different alts)
 *  meaning that state was reached via a different set of rule invocations.
 */
public class DFAState extends State {
	public static final int INITIAL_NUM_TRANSITIONS = 4;

	/** State in which DFA? */
	public DFA dfa;

	/** Track the transitions emanating from this DFA state. */
	protected List<Transition> transitions =
		new ArrayList<Transition>(INITIAL_NUM_TRANSITIONS);

	/** The set of NFA configurations (state,alt,context) for this DFA state */
	public OrderedHashSet<NFAConfig> nfaConfigs =
		new OrderedHashSet<NFAConfig>();

	public DFAState(DFA dfa) { this.dfa = dfa; }

	public void addNFAConfig(NFAState s, NFAConfig c) {
		if ( nfaConfigs.contains(c) ) return;
		nfaConfigs.add(c);
	}

	public NFAConfig addNFAConfig(NFAState state,
								  int alt,
								  NFAState invokingState)
	{
		NFAConfig c = new NFAConfig(state.stateNumber,
									alt,
									invokingState);
		addNFAConfig(state, c);
		return c;
	}

	@Override
	public int getNumberOfTransitions() { return transitions.size(); }

	@Override
	public void addTransition(Transition e) { transitions.add(e); }

	@Override
	public Transition transition(int i) { return transitions.get(i); }

	/** A decent hash for a DFA state is the sum of the NFA state/alt pairs. */
	public int hashCode() {
		int h = 0;
		for (NFAConfig c : nfaConfigs) {
			h += c.state + c.alt;
		}
		return h;
	}

	/** Two DFAStates are equal if their NFA configuration sets are the
	 *  same. This method is used to see if a DFA state already exists.
	 *
	 *  Because the number of alternatives and number of NFA configurations are
	 *  finite, there is a finite number of DFA states that can be processed.
	 *  This is necessary to show that the algorithm terminates.
	 *
	 *  Cannot test the DFA state numbers here because in DFA.addState we need
	 *  to know if any other state exists that has this exact set of NFA
	 *  configurations.  The DFAState state number is irrelevant.
	 */
	public boolean equals(Object o) {
		// compare set of NFA configurations in this set with other
		DFAState other = (DFAState)o;
		return this.nfaConfigs.equals(other.nfaConfigs);
	}
	
}
