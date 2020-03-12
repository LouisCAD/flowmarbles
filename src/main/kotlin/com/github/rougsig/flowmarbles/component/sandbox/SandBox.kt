package com.github.rougsig.flowmarbles.component.sandbox

import com.github.rougsig.flowmarbles.component.timeline.Marble
import com.github.rougsig.flowmarbles.component.timeline.Timeline
import com.github.rougsig.flowmarbles.core.Component
import com.github.rougsig.flowmarbles.core.appendComponent
import com.github.rougsig.flowmarbles.core.createElement
import com.github.rougsig.flowmarbles.extensions.VirtualTimeDispatcher
import com.github.rougsig.flowmarbles.extensions.toTimedFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlin.browser.window

typealias SandBoxTransformer<T> = (inputs: List<Flow<Marble.Model<T>>>) -> Flow<Marble.Model<T>>

@InternalCoroutinesApi
class SandBox<T : Any> : Component {
  data class Model<T : Any>(
    val input: SandBoxInput.Model<T>,
    val label: String,
    val transformer: SandBoxTransformer<T>
  )

  override val rootNode = createElement("div") {
    setAttribute("class", "sandbox")
  }

  private val input = SandBoxInput<T>()
  private val label = SandBoxLabel()
  private val output = SandBoxOutput<T>()

  init {
    rootNode.appendComponent(input)
    rootNode.appendComponent(label)
    rootNode.appendComponent(output)
  }

  fun setModel(model: Model<T>) {
    input.setModel(model.input)
    label.setLabel(model.label)
    invalidateOutput(model.input, model.transformer)
    input.setTimelinesChangeListener { newModel ->
      invalidateOutput(newModel, model.transformer)
    }
  }

  private fun invalidateOutput(input: SandBoxInput.Model<T>, transformer: SandBoxTransformer<T>) {
    val virtualTimeDispatcher = VirtualTimeDispatcher()
    GlobalScope.launch {
      val inputs = input.timelines
        .map { it.marbles }
        .map { it.toTimedFlow(virtualTimeDispatcher) }
      val outputModel = transformer(inputs)
        .flowOn(virtualTimeDispatcher)
        .toList()
      output.setModel(Timeline.Model(outputModel))
    }
    window.setTimeout({ virtualTimeDispatcher.advanceUntilIdle() }, 0)
  }
}
