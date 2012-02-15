/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.compiler.output;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.generator.trace.AbstractTraceRegion;
import org.eclipse.xtext.xbase.compiler.ImportManager;
import org.eclipse.xtext.xbase.compiler.StringBuilderBasedAppendable;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@NonNullByDefault
public class FakeTreeAppendable extends StringBuilderBasedAppendable implements ITreeAppendable {

	public FakeTreeAppendable() {
		super();
	}

	public FakeTreeAppendable(ImportManager typeSerializer, String indentation, String lineSeparator) {
		super(typeSerializer, indentation, lineSeparator);
	}

	public FakeTreeAppendable(ImportManager typeSerializer) {
		super(typeSerializer);
	}

	@Override
	public ITreeAppendable append(JvmType type) {
		super.append(type);
		return this;
	}
	
	@Override
	public ITreeAppendable append(String string) {
		super.append(string);
		return this;
	}
	
	@Override
	public ITreeAppendable decreaseIndentation() {
		super.decreaseIndentation();
		return this;
	}
	
	@Override
	public ITreeAppendable increaseIndentation() {
		super.increaseIndentation();
		return this;
	}
	
	@Override
	public ITreeAppendable newLine() {
		super.newLine();
		return this;
	}
	
	public AbstractTraceRegion getTraceRegion() {
		throw new UnsupportedOperationException("FakeTreeAppendable cannot provide trace information");
	}

	public ITreeAppendable trace(EObject object) {
		return this;
	}

	public ITreeAppendable append(ITreeAppendable other) {
		throw new UnsupportedOperationException("FakeTreeAppendable cannot append another ITreeAppendable");
	}
	
}