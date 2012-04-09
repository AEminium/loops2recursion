package aeminium.java.compiler.ltr.processing.utils.methodfixer;

import java.util.HashSet;
import java.util.Set;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.QueryVisitor;
import spoon.reflect.visitor.filter.AbstractFilter;

public class ThrownTypesMethodFixer {

	CtMethod<?> met;

	public ThrownTypesMethodFixer(CtMethod<?> m) {
		met = m;
	}

	public void fix() {
		Set<CtTypeReference<? extends Throwable>> throwables = new HashSet<CtTypeReference<? extends Throwable>>();
		extractTypesFromThrow(throwables);
		extractTypesFromInvocations(throwables);
		met.setThrownTypes(throwables);
	}

	private void extractTypesFromInvocations(
			Set<CtTypeReference<? extends Throwable>> throwables) {
		Filter<CtInvocation<?>> finv = new AbstractFilter<CtInvocation<?>>(
				CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation<?> t) {
				return true;
			}
		};
		QueryVisitor<?> qv = new QueryVisitor<CtInvocation<?>>(finv);
		qv.scan(met);
		for (Object t : qv.getResult()) {
			CtInvocation<?> th = (CtInvocation<?>) t;
			CtExecutable<?> decl = th.getExecutable().getDeclaration();
			if (decl != null) {
				for (CtTypeReference<? extends Throwable> ref : decl
						.getThrownTypes()) {
					throwables.add(ref);
				}
			} else {
				// External code.
				for (Class<?> real : th.getExecutable().getActualMethod()
						.getExceptionTypes()) {
					@SuppressWarnings("unchecked")
					CtTypeReference<? extends Throwable> ref = (CtTypeReference<? extends Throwable>) met
							.getFactory().Type().createReference(real);
					throwables.add(ref);
				}

			}
		}
	}

	private void extractTypesFromThrow(
			Set<CtTypeReference<? extends Throwable>> throwables) {
		Filter<CtThrow> fthrow = new AbstractFilter<CtThrow>(CtThrow.class) {
			@Override
			public boolean matches(CtThrow t) {
				return true;
			}
		};
		QueryVisitor<?> qv = new QueryVisitor<CtThrow>(fthrow);
		qv.scan(met);
		for (Object t : qv.getResult()) {
			CtThrow th = (CtThrow) t;
			throwables.add(th.getThrownExpression().getType());
		}
	}
}
