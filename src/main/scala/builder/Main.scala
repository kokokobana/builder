package builder

import builder.CssSettings._
import builder.containers._
import org.scalajs.dom.document
import scalacss.ScalaCssReact._

object Main {
  def main(args: Array[String]): Unit = {
    Theme.addToDocument()

    raw.ons.ready(() => {
      BuilderContainer(MainCircuit.context).renderIntoDOM(document.getElementById("app"))
      ()
    })
  }
}
