import glew.*
import glfw.*
import glut.GL_ARRAY_BUFFER
import glut.GL_FLOAT
import glut.GL_STATIC_DRAW
import glut.GL_TRIANGLES
import glut.GL_COLOR_BUFFER_BIT
import glut.GLboolean
import glut.PFNGLBINDBUFFERPROC
import glut.PFNGLBINDVERTEXARRAYPROC
import glut.PFNGLBUFFERDATAPROC
import glut.PFNGLGENBUFFERSPROC
import glut.PFNGLGENVERTEXARRAYSPROC
import glut.glClear
import glut.glDrawArrays
import kotlinx.cinterop.*
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

    glewExperimental = GL_TRUE

    glfwMakeContextCurrent(window)
    initGlew()
    // Ensure we can capture the escape key being pressed below
    glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE)
    vaoBind()
    val buff = vbufBind(
        floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
        )
    )

    do {
        glClear(GL_COLOR_BUFFER_BIT.toUInt());

        glEnableVertexAttribArray!!(0u);
        glBindBuffer!!(GL_ARRAY_BUFFER.toUInt(), buff);
        println("3")

        glVertexAttribPointer!!(
            0u,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
            3,                  // size
            GL_FLOAT.toUInt(),           // type
            GL_FALSE,           // normalized?
            0,                  // stride
            null           // array buffer offset
        )
        // Draw the triangle !
        glDrawArrays(GL_TRIANGLES, 0, 3); // Starting from vertex 0; 3 vertices total -> 1 triangle

        glDisableVertexAttribArray!!(0u);

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

private inline fun vaoBind() = memScoped {
    println("00")

    val vertexArrayId: UIntVarOf<UInt> = alloc()
    println("01")

    val vaoGenFunc: VertexArraysGenFunc = glGenVertexArrays ?: return
    println("02")
    vaoGenFunc(1, vertexArrayId.ptr)
    println("03")

    val vaoBindFunc: VertexArraysBindFunc = glBindVertexArray!!
    println("04")
    vaoBindFunc(vertexArrayId.value)
    println("05")
}

private inline fun vbufBind(floatArray: FloatArray): UInt = memScoped {
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

    return vertexbuffer.value
}
