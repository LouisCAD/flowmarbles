package com.github.rougsig.flowmarbles.operators

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
val operators = listOf(
  contextOperators(),
  distinctOperators(),
  emittersOperators(),
  limitOperators(),
  mergeOperators(),
  transformOperators(),
  zipOperators()
).flatten()
