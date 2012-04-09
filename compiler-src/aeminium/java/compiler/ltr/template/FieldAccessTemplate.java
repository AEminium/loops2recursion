package aeminium.java.compiler.ltr.template;

import spoon.reflect.reference.CtTypeReference;
import spoon.template.Local;
import spoon.template.Parameter;
import spoon.template.Template;

/**
 * This template defines the needed template code for matching and/or generating
 * field accesses through getters and setters:
 */
public class FieldAccessTemplate<_FieldType_> implements Template {

	@Parameter
	CtTypeReference _FieldType_;

	@Parameter("_field_")
	String __field_;
	
	@SuppressWarnings("unchecked")
	@Local
	public FieldAccessTemplate(CtTypeReference type, String field) {
		_FieldType_ = type;
		__field_ = field;
	}

	_FieldType_ _field_;

	public _FieldType_ set_Field_(_FieldType_ par__field_) {
		_field_ = par__field_;
		return _field_;
	}

	public _FieldType_ get_Field_() {
		return _field_;
	}
}
