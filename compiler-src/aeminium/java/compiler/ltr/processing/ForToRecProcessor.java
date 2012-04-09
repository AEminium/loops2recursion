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


}
