package builder.containers

import builder.MainCircuit
import builder.raw.onsen.{Modal, ProgressCircular}
import builder.state.ClassDataState
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._

object Loading {
  def apply: VdomElement =
    MainCircuit.connect(_.classData).apply(component(_))

  private val component = ScalaFnComponent[ModelProxy[ClassDataState]] { model =>
    val isOpen =
      model.value match {
        case ClassDataState.Loading => true
        case _ => false
      }
    Modal(isOpen = isOpen)(ProgressCircular(indeterminate = true))
  }
}
