import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.*

class MyApp: App(FirstView::class)

class FirstView: View() {
    var instructionPane: Pane by singleAssign()
    var controller: MyController by singleAssign()

    override val root = vbox {
        label("Doom Robot Control Program") {
            minWidth = this@vbox.width //Full width of parent
            minHeight = 50.0
        }
        hbox {
            instructionPane = flowpane {
                minWidth = 450.0
                minHeight = 450.0
                hgap = 10.0
                vgap = 10.0

                background = Background(BackgroundFill(
                    Color.AZURE,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
                ))

                instructionbutton("Go Forward") {
                    robotInstruction = RobotInstruction.FORWARD
                }

                instructionbutton("Turn Left") {
                    robotInstruction = RobotInstruction.LEFT
                }

                instructionbutton("Light Up") {
                    robotInstruction = RobotInstruction.LIGHT
                }

                instructionbutton("Go Forward") {
                    robotInstruction = RobotInstruction.FORWARD
                }

                instructionbutton("Light Up") {
                    robotInstruction = RobotInstruction.LIGHT
                }

                instructionbutton("Kill The Humans") {
                    robotInstruction = RobotInstruction.KILL_ALL_HUMANS
                }
            }
        }

        button("Send Instructions") {
            action {
                runAsync {
                    controller.getInstructions()
                }
            }
        }
    }

    init {
        controller = MyController(instructionPane)
    }
}

class InstructionButton(bText: String, val parent: Pane): Button() {
    var robotInstruction = RobotInstruction.KILL_ALL_HUMANS
    init {
        text = bText

        minHeight = 100.0
        maxHeight = 100.0

        minWidth = 100.0
        maxWidth = 100.0

        action {
            parent.children.remove(this)
        }
    }
}

//Extension function for above
fun Pane.instructionbutton(text: String, function: (InstructionButton.() -> Unit)): InstructionButton {
    val button = InstructionButton(text, this)
    button.apply(function)

    children.add(button)

    return button
}

fun Pane.instructionbutton(text: String): InstructionButton {
    return instructionbutton(text) {}
}

enum class RobotInstruction {
    FORWARD, BACKWARD, LEFT, RIGHT, LIGHT, KILL_ALL_HUMANS
}

class MyController(val instructionPane: Pane): Controller() {
    fun getInstructions() {
        val list = instructionPane.children.filtered { it is InstructionButton }
        for (item in list) {
            println(item)
        }
    }
}

fun main() {
    launch<MyApp>()
}