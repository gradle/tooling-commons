package com.gradleware.tooling.toolingutils.binding

import com.google.common.base.Optional
import spock.lang.Specification

class ValidatorsTest extends Specification {

    def "noOp never invalidates a value"() {
        setup:
        def op = Validators.noOp()
        assert op.validate(null) == Optional.absent()
        assert op.validate("something") == Optional.absent()
        assert op.validate(new File("somewhere")) == Optional.absent()
    }

}
