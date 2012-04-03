package aeminium.java.compiler.ltr.template;

import spoon.reflect.code.CtExpression;
import spoon.template.Local;
import spoon.template.Parameter;
import spoon.template.Template;

public class WhileToRecTemplate implements Template {

	
	@Parameter
	CtExpression<Boolean> _cond_;
	
	public void aeminium_rec_method() {
		if (_cond_.S()) {
		}
	}

	@Local
	public WhileToRecTemplate(CtExpression<Boolean> cond) {
		_cond_ = cond;
	}
}
