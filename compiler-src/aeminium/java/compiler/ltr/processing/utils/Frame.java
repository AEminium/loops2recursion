package aeminium.java.compiler.ltr.processing.utils;

import java.util.HashMap;

import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

public class Frame {
	
	Frame parent;
	public HashMap<String, CtTypeReference<?>> usages;
	public Frame(Frame parent, HashMap<String, CtTypeReference<?>> usages) {
		this.parent = parent;
		this.usages = usages;
	}
	
	private HashMap<String, CtExpression<?>> declarations = new HashMap<String, CtExpression<?>>();
	
	public void addUsage(String u, CtTypeReference<?> t) {
		Frame c = this;
		do {
			if (c.declarations.containsKey(u)) return;
			c = c.parent;
		} while (c != null);
		usages.put(u, t);
	}
	
	public void addDeclaration(String u, CtTypeReference<?> t, CtExpression<?> d) {
		declarations.put(u, d);
	}	
}
