import libglew.GLboolean
import libglfw.GLFW_TRUE
import libglfw.glfwInit
import libglfw.glfwTerminate
import platform.posix.exit

const val GL_TRUE: GLboolean = 1u
const val GL_FALSE: GLboolean = 0u

fun main() {
    val glfwInitResul = glfwInit()
    if (glfwInitResul != GLFW_TRUE) {
        println("Failed to initialize GLFW!")
        exit(glfwInitResul)
    }


    glfwTerminate()
}

