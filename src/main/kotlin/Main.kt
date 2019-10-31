import tornadofx.*

class MyApp: App(MyView::class)

class MyView: View() {
    val controller: MyController by inject()
    override val root = vbox {
        val l = label("y u not push button")
        button("U PUSH BUTTON") {
            action {
                runAsync {
                    controller.pushButtonText()
                } ui {
                    l.text = it
                }
            }
        }
    }
}

class MyController: Controller() {
    fun pushButtonText() = "U PUSHED BUTTON!!1!!1!1!!!"
}

fun main() {
    launch<MyApp>()
}