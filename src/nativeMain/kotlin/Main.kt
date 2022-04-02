import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UByteVarOf
import kotlinx.cinterop.get
import libglew.*
import libglfw.*
import platform.posix.exit

const val GL_TRUE: GLboolean = 1u
const val GL_FALSE: GLboolean = 0u

fun main() {
    initGlfw()

    glfwWindowHint(GLFW_SAMPLES, 4) // 4x antialiasing
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3) // We want OpenGL 3.3
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE) // To make MacOS happy should not be needed
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE) // We don't want the old OpenGL

    val window = glfwCreateWindow(1024, 768, "Tutorial 01", null, null)

    if (window == null) {
        println("Failed to open GLFW window!");
        glfwTerminate()
        exit(-1)
    }

    glfwMakeContextCurrent(window)

    glewExperimental = GL_TRUE

    initGlew()

    // Ensure we can capture the escape key being pressed below
    glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE)

    do {
        // Draw nothing fn

        // Swap buffers
        glfwSwapBuffers(window);
        glfwPollEvents();
    } while (glfwGetKey(window, GLFW_KEY_ESCAPE) != GLFW_PRESS && glfwWindowShouldClose(window) == 0)
    // Check if the ESC key was pressed or the window was closed

    glfwTerminate()
}

private inline fun initGlfw() {
    val glfwInitResul = glfwInit()
    if (glfwInitResul != GLFW_TRUE) {
        println("Failed to initialize GLFW! [$glfwInitResul]")
        exit(glfwInitResul)
    }

    println("SUCCESSFUL initialize GLFW!")
}

private inline fun initGlew() {
    val glewInitResul: UInt = glewInit()
    if (GLEW_OK.toUInt() != glewInitResul) {
        println("Failed to initialize GLEW! [$glewInitResul] : ${glewGetErrorString(glewInitResul)?.toKString()}")
        exit(glewInitResul.toInt())
    }

    println("SUCCESSFUL initialize GLEW!")
}

private fun CPointer<UByteVarOf<UByte>>?.toKString(): CharSequence? {
    if (this == null)
        return null

    var i = 0
    var d: UByte = this[i]

    val sb = StringBuilder()
    while (d != UByte.MIN_VALUE) {
        sb.append(d.toInt().toChar())
        d = this[++i]
    }

    return sb
}

