/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.serializer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.junit4.AbstractXtextTests;
import org.eclipse.xtext.serializer.analysis.Context2NameFunction;
import org.eclipse.xtext.serializer.analysis.IContextPDAProvider;
import org.eclipse.xtext.serializer.analysis.IContextTypePDAProvider;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class ContextProviderTest extends AbstractXtextTests {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		with(XtextStandaloneSetup.class);
	}

	final static String HEADER = "grammar org.eclipse.xtext.serializer.ContextProviderTestLanguage"
			+ " with org.eclipse.xtext.common.Terminals "
			+ "generate contextProviderTest \"http://www.eclipse.org/2010/tmf/xtext/ContextProvider\"  ";

	protected String getContexts(String body) throws Exception {
		Grammar grammar = (Grammar) getModel(HEADER + body);
		IContextPDAProvider contextProvider = get(IContextPDAProvider.class);
		IContextTypePDAProvider typeProvider = get(IContextTypePDAProvider.class);
		Context2NameFunction names = get(Context2NameFunction.class);
		List<Pair<String, List<String>>> result = Lists.newArrayList();
		for (EObject context : contextProvider.getAllContexts(grammar)) {
			List<String> types = Lists.newArrayList();
			for (EClass type : typeProvider.getTypesForContext(grammar, context))
				types.add(type == null ? "null" : type.getName());
			Collections.sort(types);
			result.add(Tuples.create(names.getContextName(grammar, context), types));
		}
		Collections.sort(result, new Comparator<Pair<String, List<String>>>() {
			@Override
			public int compare(Pair<String, List<String>> o1, Pair<String, List<String>> o2) {
				return o1.getFirst().compareTo(o2.getFirst());
			}
		});
		return Joiner.on("\n").join(Iterables.transform(result, new Function<Pair<String, List<String>>, String>() {
			@Override
			public String apply(Pair<String, List<String>> from) {
				return from.getFirst() + " returns " + Joiner.on(", ").join(from.getSecond());
			}
		}));
	}

	@Test
	public void testSimple() throws Exception {
		String actual = getContexts("Rule: foo=ID;");
		String expected = "Rule returns Rule";
		assertEquals(expected, actual);
	}

	@Test
	@Ignore("TODO implement in context provider")
	public void testFragmentIsNotAContext() throws Exception {
		String actual = getContexts(
				"Rule: foo=ID; fragment Fragment: name=ID WCFragment; fragment WCFragment*: desc=STRING;");
		String expected = "Rule returns Rule";
		assertEquals(expected, actual);
	}

	@Test
	@Ignore("TODO implement in context provider")
	public void testActionInFragment() throws Exception {
		String actual = getContexts(
				"Rule: foo=ID Fragment; fragment Fragment returns AbstractRule: {AnotherRule.prev=current} name=ID;");
		String expected = "Fragment_AnotherRule_0 returns Rule\n" + "Rule returns AnotherRule";
		assertEquals(expected, actual);
	}

	@Test
	@Ignore("TODO implement in context provider")
	public void testTwoActionsInFragment() throws Exception {
		String actual = getContexts(
				"Rule0: f1=ID Fragment; fragment Fragment returns Rule: {Rule1.prev=current} f2=ID {Rule2.prev=current} f3=ID;");
		String expected = "Fragment_Rule1_0 returns Rule0\n" + "Fragment_Rule2_2 returns Rule1\n"
				+ "Rule0 returns Rule2";
		assertEquals(expected, actual);
	}

	@Test
	@Ignore("TODO implement in context provider")
	public void testActionsInFragmentTwoCallers() throws Exception {
		String actual = getContexts(
				"Rule0: f1=ID Fragment; Rule1: f2=ID Fragment; fragment Fragment returns Rule: {Rule2.prev=current} f3=ID;");
		String expected = "Fragment_Rule2_0 returns Rule0, Rule1\n" + "Rule0 returns Rule2\n" + "Rule1 returns Rule2";
		assertEquals(expected, actual);
	}

	@Test
	@Ignore("TODO implement in context provider")
	public void testActionsInFragmentTwoPrecedingActions() throws Exception {
		String actual = getContexts(
				"Rule0: ({Rule1} f1=ID | {Rule2} f1=STRING) Fragment?; fragment Fragment returns Rule: {Rule3.prev=current} f3=ID;");
		String expected = "Fragment_Rule3_0 returns Rule1, Rule2\n" + "Rule0 returns Rule1, Rule2, Rule3";
		assertEquals(expected, actual);
	}

	@Test
	public void testUnassignedAction1() throws Exception {
		String actual = getContexts("Rule: {Foo};");
		String expected = "Rule returns Foo";
		assertEquals(expected, actual);
	}

	@Test
	public void testUnassignedAction2() throws Exception {
		String actual = getContexts("Rule: ('foo' {Foo})? val=ID;");
		String expected = "Rule returns Foo, Rule";
		assertEquals(expected, actual);
	}

	@Test
	public void testUnassignedAction3() throws Exception {
		String actual = getContexts("Rule: ('foo' {Foo})?;");
		String expected = "Rule returns Foo, null";
		assertEquals(expected, actual);
	}

	@Test
	public void testUnassignedAction4() throws Exception {
		String actual = getContexts("Rule: {Foo} | {Bar};");
		String expected = "Rule returns Bar, Foo";
		assertEquals(expected, actual);
	}

	@Test
	public void testUnassignedAction5() throws Exception {
		String actual = getContexts("Rule: {Foo} | {Bar} | val=ID;");
		String expected = "Rule returns Bar, Foo, Rule";
		assertEquals(expected, actual);
	}

	@Test
	public void testUnassignedAction6() throws Exception {
		String actual = getContexts("Rule: 'foo' ('foo' {Foo})?;");
		String expected = "Rule returns Foo, null";
		assertEquals(expected, actual);
	}

	@Test
	public void testUnassignedRuleCall1() throws Exception {
		String actual = getContexts("Rule: Foo; Foo: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Foo");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testUnassignedRuleCall2() throws Exception {
		String actual = getContexts("Rule: Foo? val2=ID; Foo: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Foo, Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testUnassignedRuleCall3() throws Exception {
		String actual = getContexts("Rule: Foo?; Foo: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Foo, null");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testUnassignedRuleCall4() throws Exception {
		String actual = getContexts("Rule: (Foo 'foo')?; Foo: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Foo, null");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testUnassignedRuleCall5() throws Exception {
		String actual = getContexts("Rule: Foo | Bar; Foo: val=ID; Bar: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Bar returns Bar\n");
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Bar, Foo");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testUnassignedRuleCall6() throws Exception {
		String actual = getContexts("Rule: Foo | Bar | val=ID; Foo: val=ID; Bar: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Bar returns Bar\n");
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Bar, Foo, Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testUnassignedRuleCall7() throws Exception {
		String actual = getContexts("Rule: Foo | Bar | 'baz'; Foo: val=ID; Bar: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Bar returns Bar\n");
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Bar, Foo, null");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedAction1() throws Exception {
		String actual = getContexts("Rule: val=ID {Foo.bar=current};");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Foo\n");
		expected.append("Rule_Foo_1 returns Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedAction2() throws Exception {
		String actual = getContexts("Rule: val=ID ({Foo.bar=current} 'x')?;");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Foo, Rule\n");
		expected.append("Rule_Foo_1_0 returns Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedAction3() throws Exception {
		String actual = getContexts("Rule: val=ID ({Foo.bar=current} 'x')*;");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Foo, Rule\n");
		expected.append("Rule_Foo_1_0 returns Foo, Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedAction4() throws Exception {
		String actual = getContexts("Rule: val=ID ({Foo.bar=current} 'x')+;");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Foo\n");
		expected.append("Rule_Foo_1_0 returns Foo, Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedAction5() throws Exception {
		String actual = getContexts("Rule: val=ID {Foo.bar=current} 'x' {Baz.bar=current} 'x';");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Baz\n");
		expected.append("Rule_Baz_3 returns Foo\n");
		expected.append("Rule_Foo_1 returns Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedAction6() throws Exception {
		String actual = getContexts("Model: {Foo.left=current} (val=ID? | {Foo.left=current});");
		StringBuilder expected = new StringBuilder();
		expected.append("Model returns Foo\n");
		expected.append("Model_Foo_0 returns null\n");
		expected.append("Model_Foo_1_1 returns Foo");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedRuleCall1() throws Exception {
		String actual = getContexts("Rule: foo=Foo; Foo: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Rule");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAssignedRuleCall2() throws Exception {
		String actual = getContexts("Rule: 'foo' foo=Foo; Foo: val=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Foo returns Foo\n");
		expected.append("Rule returns Rule");
		assertEquals(expected.toString(), actual);
	}
}
