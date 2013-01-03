/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.xtext.ui.generator.quickfix;

import static java.util.Collections.*;

import java.util.List;
import java.util.Set;

import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.generator.AbstractStubGeneratorFragment;
import org.eclipse.xtext.generator.BindFactory;
import org.eclipse.xtext.generator.Binding;
import org.eclipse.xtext.generator.IGeneratorFragment;
import org.eclipse.xtext.generator.IInheriting;
import org.eclipse.xtext.generator.Naming;

/**
 * {@link IGeneratorFragment} to generate a quickfix provider for a given grammar.
 *
 * @author Knut Wannheden - Initial contribution and API
 * @author Heiko Behrens
 */
public class QuickfixProviderFragment extends AbstractStubGeneratorFragment implements IInheriting {

	private boolean isInheritImplementation = true;
	
	public static String getQuickfixProviderName(Grammar g, Naming n) {
		return n.basePackageUi(g) + ".quickfix." + GrammarUtil.getName(g) + "QuickfixProvider";
	}

	/**
	 * @since 2.4
	 */
	public String getQuickfixProviderSuperClassName(Grammar g) {
		if(isInheritImplementation && !g.getUsedGrammars().isEmpty()) 
			return getQuickfixProviderName(g.getUsedGrammars().get(0), getNaming());
		return "org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider";
	}
	
	@Override
	public Set<Binding> getGuiceBindingsUi(Grammar grammar) {
		if(isGenerateStub())
			return new BindFactory()
				.addTypeToType("org.eclipse.xtext.ui.editor.quickfix.IssueResolutionProvider", getQuickfixProviderName(grammar, getNaming()))
				.getBindings();
		else
			return emptySet();
	}

	/**
	 * @since 2.4
	 */
	public boolean isInheritImplementation() {
		return isInheritImplementation;
	}

	/**
	 * @since 2.4
	 */
	public void setInheritImplementation(boolean isInheritImplementation) {
		this.isInheritImplementation = isInheritImplementation;
	}

	@Override
	protected List<Object> getParameters(Grammar grammar) {
		List<Object> parameters = super.getParameters(grammar);
		parameters.add(getQuickfixProviderSuperClassName(grammar));
		return parameters;
	}

}
