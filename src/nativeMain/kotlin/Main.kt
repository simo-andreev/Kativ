import kotlinx.cinterop.*
import libglew.*
import libglfw.*
import platform.posix.exit

const val GL_TRUE: GLboolean = 1u
const val GL_FALSE: GLboolean = 0u

typealias VertexArraysGenFunc = PFNGLGENVERTEXARRAYSPROC
typealias VertexArraysBindFunc = PFNGLBINDVERTEXARRAYPROC
typealias VertexBufferGenFunc = PFNGLGENBUFFERSPROC
typealias VertexBufferBindFunc = PFNGLBINDBUFFERPROC
typealias VertexBufferDataFunc = PFNGLBUFFERDATAPROC

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


    memScoped {
        glfwMakeContextCurrent(window)
        println("0")

        vaoBind()

        println("1")


        glewExperimental = GL_TRUE

        initGlew()

        // Ensure we can capture the escape key being pressed below
        glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE)

        vbufBind(
            floatArrayOf(
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
            )
        )
        println("2")

        do {
            vaoBind()
            println("3")

            // Swap buffers
            glfwSwapBuffers(window);
            glfwPollEvents();
        } while (glfwGetKey(window, GLFW_KEY_ESCAPE) != GLFW_PRESS && glfwWindowShouldClose(window) == 0)
        // Check if the ESC key was pressed or the window was closed
        println("4")

    }

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

private inline fun MemScope.vaoBind() {
    println("00")

    val vertexArrayId: UIntVarOf<UInt> = alloc()
    println("01")

    val vaoGenFunc: VertexArraysGenFunc = glGenVertexArrays?: return
    println("02")
    vaoGenFunc(1, vertexArrayId.ptr)
    println("03")

    val vaoBindFunc: VertexArraysBindFunc = glBindVertexArray!!
    println("04")
    vaoBindFunc(vertexArrayId.value)
    println("05")
}

private inline fun MemScope.vbufBind(floatArray: FloatArray) {
    // This will identify our vertex buffer
    val vertexbuffer: UIntVarOf<UInt> = alloc()

    // Generate 1 buffer, put the resulting identifier in vertexbuffer
    val bufferGenFunc: VertexBufferGenFunc = glGenBuffers!!
    bufferGenFunc(1, vertexbuffer.ptr)


    // The following commands will talk about our 'vertexbuffer' buffer
    val bufferBindFunc: VertexBufferBindFunc = glBindBuffer!!
    bufferBindFunc(GL_ARRAY_BUFFER.toUInt(), vertexbuffer.value)


    // Give our vertices to OpenGL.
    val bufferDataFunc: VertexBufferDataFunc = glBufferData!!
    bufferDataFunc(
        GL_ARRAY_BUFFER.toUInt(),
        (floatArray.size * 4).signExtend(),
        floatArray.refTo(0).getPointer(this.memScope),
        GL_STATIC_DRAW.toUInt()
    )
}
