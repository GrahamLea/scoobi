package com.nicta.scoobi
package impl
package plan
package mscr

import testing.UnitSpecification
import org.specs2.specification.Groups
import comp.factory
import org.specs2.matcher.ThrownExpectations

class InputChannelSpec extends UnitSpecification with Groups with ThrownExpectations { def is =

  "An InputChannel encapsulates CompNodes which are the inputs of a Mscr"^
    "the inputs of an InputChannel are the input nodes of the channel nodes"! g1().e1^
    "the inputs of an IdInputChannel are the input of the parallelDos and combine nodes in that channel"! g1().e2^
  end

  "inputs" - new g1 with factory {
    e1 := MapperInputChannel(pd(load, rt)).inputs === Seq(load, rt)
    e2 := {
      IdInputChannel(pd(load), gbk(load)).inputs === Seq(load)
      IdInputChannel(cb(load), gbk(load)).inputs === Seq(load)
    }
  }
}