package builder.containers

import builder._
import builder.raw.onsen._
import builder.state._
import diode.data.{Empty, Pending, Pot, Ready}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.raw.HTMLInputElement
import scalacss.ScalaCssReact._
import org.scalajs.dom.window

object BuildCodeDialog {
  final case class Props(
    context: Context,
    model: ModelProxy[BuildCodeDialogState]
  )

  def apply(context: Context): VdomElement =
    MainCircuit.connect(_.buildCodeDialog).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    def copyToClipboard(inputId: String): Callback = Callback {
      val elem = window.document.getElementById(inputId).asInstanceOf[HTMLInputElement]
      elem.select()
      window.document.execCommand("Copy")
    }

    model.value.state match {
      case Empty =>
        AlertDialog(isOpen = false, isCancelable = false)(
          <.div(^.className := "alert-dialog-title")(ctx.localization.ui("share_build")),
          <.div(^.className := "alert-dialog-content"),
          <.div(^.className := "alert-dialog-footer")
        )
      case Ready(resp) =>
        val loc = window.location
        AlertDialog(isOpen = true, isCancelable = false)(
          <.div(^.className := "alert-dialog-title")(ctx.localization.ui("share_build")),
          <.div(Theme.buildCodePopupContent, ^.className := "alert-dialog-content")(
            <.span(ctx.localization.ui("unique_code")),
            <.div(Theme.buildCodePopupRow,
              Input(inputId = "code", modifier = "underbar", value = resp.code),
              Button(onClick = () => copyToClipboard("code"))(Icon(icon = "clipboard"))
            ),
            <.span(ctx.localization.ui("url")),
            <.div(Theme.buildCodePopupRow,
              Input(inputId = "code-url", modifier = "underbar", value =
                s"${loc.protocol}//${loc.host}/${loc.pathname}?code=${resp.code}&lang=${ctx.translation.symbol}"),
              Button(onClick = () => copyToClipboard("code-url"))(Icon(icon = "clipboard"))
            )
          ),
          <.div(^.className := "alert-dialog-footer")(
            <.button(
              ^.className := "alert-dialog-button",
              ^.onClick --> model.dispatchCB(SetBuildCodeDialog(Pot.empty)),
              "OK"
            )
          )
        )
      case Pending(_) =>
        AlertDialog(isOpen = true, isCancelable = false)(
          <.div(^.className := "alert-dialog-title")(ctx.localization.ui("share_build")),
          <.div(^.className := "alert-dialog-content", ProgressCircular()),
          <.div(^.className := "alert-dialog-footer")
        )
      case _ =>
        AlertDialog(isOpen = true, isCancelable = false)(
          <.div(^.className := "alert-dialog-title")(ctx.localization.ui("share_build")),
          <.div(^.className := "alert-dialog-content", "Failed to save the build"),
          <.div(^.className := "alert-dialog-footer",
            <.button(
              ^.className := "alert-dialog-button",
              ^.onClick --> model.dispatchCB(SetBuildCodeDialog(Pot.empty)),
              "OK"
            )
          )
        )
    }
  }
}
