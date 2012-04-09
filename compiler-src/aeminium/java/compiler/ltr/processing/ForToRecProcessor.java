package aeminium.java.compiler.ltr.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.ExecutableFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.QueryVisitor;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.template.Substitution;
import spoon.template.Template;
import aeminium.java.compiler.ltr.processing.utils.Counter;
import aeminium.java.compiler.ltr.processing.utils.VariablesUsedVisitor;
import aeminium.java.compiler.ltr.processing.utils.methodfixer.ThrownTypesMethodFixer;
import aeminium.java.compiler.ltr.template.FieldAccessTemplate;
import aeminium.java.compiler.ltr.template.ForToRecTemplate;

public class ForToRecProcessor extends AbstractLoopToRecProcessor<CtFor> {

	boolean isStatic;

	@Override
	public void process(CtFor target) {
		int id = Counter.getId();

		CtClass<?> outterClass = target.getParent(CtClass.class);
		System.out.println("For found in class " + outterClass.getSimpleName()
				+ " in " + target.getPosition());

		// Add skeleton method
		Template t = new ForToRecTemplate();
		Substitution.insertAllMethods(outterClass, t);

		// Rename method
		CtMethod<?> met = outterClass.getMethod("aeminium_rec_for_method");
		met.setSimpleName(met.getSimpleName() + "_" + id);

		// Which arguments to pass?
		VariablesUsedVisitor varVis = new VariablesUsedVisitor(outterClass);
		target.accept(varVis);
		HashMap<String, CtTypeReference<?>> variableTypes = varVis.usages;
		HashMap<String, CtExpression<?>> variableDefaults = new HashMap<String, CtExpression<?>>();
		for (CtStatement st : target.getForInit()) {
			if (st instanceof CtLocalVariable<?>) {
				CtLocalVariable<?> var = (CtLocalVariable<?>) st;
				variableTypes.put(var.getSimpleName(), var.getType());
				variableDefaults.put(var.getSimpleName(),
						var.getDefaultExpression());
			}
		}
		Set<String> orderedVariables = variableTypes.keySet();

		// Add arguments to method definition
		List<CtParameter<?>> parList = new ArrayList<CtParameter<?>>();
		for (String u : orderedVariables) {
			ExecutableFactory ef = getFactory().Executable();
			CtParameter<?> p = ef.createParameter(met, variableTypes.get(u), u);
			parList.add(p);
		}
		met.setParameters(parList);

		// Make static work
		isStatic = checkStatic(target.getParent(CtMethod.class));
		if (isStatic)
			isStatic = makeStatic(met);

		// Add cond to if
		CtIf iff = (CtIf) met.getBody().getStatements().get(0);
		iff.setCondition(target.getExpression());

		// Add body to if.
		CtBlock<?> body = (CtBlock<?>) target.getBody();
		for (CtStatement s : target.getForUpdate())
			body.getStatements().add(s);
		body.getStatements().add(
				createMethodInvocation(outterClass, isStatic,
						met.getReference(), orderedVariables, variableTypes));
		iff.setThenStatement(body);

		checkConsistency(target);

		CtBlock<?> repl = getFactory().Core().createBlock();
		List<CtStatement> stms = repl.getStatements();
		// Add assignments
		for (CtStatement st : target.getForInit()) {
			if (st instanceof CtAssignment) {
				stms.add(st);
				fixInternalUsagesOfOutterVariable(st, outterClass, iff);
			}
		}

		// Add Invocation
		stms.add(createMethodInvocation(outterClass, isStatic,
				met.getReference(), orderedVariables, variableTypes,
				variableDefaults));
		repl.setStatements(stms);
		target.replace(repl);

		met.setDocComment("Generated from the For cycle in line "
				+ target.getPosition().getLine() + " of the original file.");

		new ThrownTypesMethodFixer(met).fix();
		checkConsistency(target);
	}

	private void fixInternalUsagesOfOutterVariable(CtStatement st,
			CtClass<?> outterClass, CtStatement stm) {
		CtExpression<?> assigned = ((CtAssignment<?, ?>) st).getAssigned();
		if (assigned instanceof CtVariableAccess) {
			CtVariableAccess<?> localVarAccess = (CtVariableAccess<?>) assigned;
			final CtVariableReference<?> localVar = localVarAccess
					.getVariable();
			final String auxVarName = "aeminium_tmp_var_" + Counter.getId();

			Template accessor = new FieldAccessTemplate<Object>(
					assigned.getType(), auxVarName);
			Substitution.insertAllFields(outterClass, accessor);
			Substitution.insertAllMethods(outterClass, accessor);

			// Rename methods
			CtMethod<?> gm = outterClass.getMethod("get_Field_");
			gm.setSimpleName("get_" + auxVarName);
			CtMethod<?> sm = outterClass.getMethod("set_Field_",
					assigned.getType());
			sm.setSimpleName("set_" + auxVarName);

			// Static work
			if (isStatic) {
				makeStatic(gm);
				makeStatic(sm);
				makeStatic(outterClass.getField(auxVarName));
			}

			// Replace usage of variable in assignments
			Filter<CtAssignment<?, ?>> assignments = new AbstractFilter<CtAssignment<?, ?>>(
					CtAssignment.class) {
				@Override
				public boolean matches(CtAssignment<?, ?> ass) {
					if (ass.getAssigned() instanceof CtVariableAccess) {
						CtVariableAccess<?> vd = (CtVariableAccess<?>) ass
								.getAssigned();
						if (vd.getVariable().equals(localVar)) {
							return true;
						}
					}
					return false;
				}
			};
			QueryVisitor<?> qv = new QueryVisitor<CtAssignment<?, ?>>(
					assignments);
			qv.scan(stm);
			for (Object t : qv.getResult()) {
				CtAssignment<?, ?> ass = (CtAssignment<?, ?>) t;
				CtInvocation<?> inv = getFactory().Code().createInvocation(
						null, sm.getReference(), ass.getAssignment());
				ass.replace(inv);
			}
			
			// Setup things
			CtExpression<?> expr;
			if (isStatic) {
				expr = null;
			} else {
				expr = getFactory().Code().createThisAccess(
						outterClass.getReference());
			}

			// Replace usage of variable in unary expression
			Filter<CtUnaryOperator<?>> unaries = new AbstractFilter<CtUnaryOperator<?>>(
					CtUnaryOperator.class) {
				@Override
				public boolean matches(CtUnaryOperator<?> ass) {
					if (ass.getOperand() instanceof CtVariableAccess) {
						CtVariableAccess<?> vd = (CtVariableAccess<?>) ass
								.getOperand();
						if (vd.getVariable().equals(localVar)) {
							return true;
						}
					}
					return false;
				}
			};
			qv = new QueryVisitor<CtUnaryOperator<?>>(unaries);
			qv.scan(stm);
			for (Object t : qv.getResult()) {
				// TODO: Convert unary to binary operator.
				/*
				CtUnaryOperator<?> u = (CtUnaryOperator<?>) t;
				
				CtExpression<?> newValue = getFactory().Code().createInvocation(
						expr, gm.getReference(),
						new ArrayList<CtExpression<?>>());
						 
				CtInvocation<?> inv = getFactory().Code().createInvocation(
						null, sm.getReference(), nop);
				
				u.replace(inv);
				*/
			}

			// Replace usage of variable in access
			Filter<CtVariableAccess<?>> accesses = new AbstractFilter<CtVariableAccess<?>>(
					CtVariableAccess.class) {
				@Override
				public boolean matches(CtVariableAccess<?> acc) {
					if (acc.getVariable().equals(localVar)) {
						return true;
					}
					return false;
				}
			};
			qv = new QueryVisitor<CtVariableAccess<?>>(accesses);
			qv.scan(stm);
			
			for (Object t : qv.getResult()) {
				CtVariableAccess<?> acc = (CtVariableAccess<?>) t;
				CtInvocation<?> inv = getFactory().Code().createInvocation(
						expr, gm.getReference(),
						new ArrayList<CtExpression<?>>());
				if (acc.getParent() != null) {
					acc.replace(inv);
				} // TODO: Else?
			}
		}

	}

}
