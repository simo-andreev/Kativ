import glew.*
import glew.PFNGLSHADERSOURCEPROC
import glut.GL_INVALID_ENUM
import kotlinx.cinterop.*
import libgl.GL_COMPILE_STATUS
import libgl.GL_FRAGMENT_SHADER
import libgl.GL_INVALID_FRAMEBUFFER_OPERATION
import libgl.GL_INVALID_OPERATION
import libgl.GL_INVALID_VALUE
import libgl.GL_LINK_STATUS
import libgl.GL_OUT_OF_MEMORY
import libgl.GL_VERTEX_SHADER
import libgl.glGetError
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.stat

typealias KillMe = PFNGLSHADERSOURCEPROC

fun checkError(message: String?) {
    val error = glGetError()
    if (error != 0u) {
        val errorString = when (error) {
            GL_INVALID_ENUM.toUInt() -> "GL_INVALID_ENUM"
            GL_INVALID_VALUE.toUInt() -> "GL_INVALID_VALUE"
            GL_INVALID_OPERATION.toUInt() -> "GL_INVALID_OPERATION"
            GL_INVALID_FRAMEBUFFER_OPERATION.toUInt() -> "GL_INVALID_FRAMEBUFFER_OPERATION"
            GL_OUT_OF_MEMORY.toUInt() -> "GL_OUT_OF_MEMORY"
            else -> "unknown"
        }

        if (message != null) println("- $message")
        throw Exception("\tGL error: 0x${error.toString(16)} ($errorString)")
    }
}

fun readFile(path: String): String? = memScoped {
    val info = alloc<stat>()
    if (stat(path, info.ptr) != 0) return null
    val size = info.st_size.toInt()
    val result = ByteArray(size)
    val file = fopen(path, "rb") ?: return null
    var position = 0
    while (position < size) {
        val toRead = minOf(size - position, 4096)
        val read = fread(result.refTo(position), 1, toRead.toULong(), file).toInt()
        if (read <= 0) break
        position += read
    }
    fclose(file)
    return result.toKString()
}

class ShaderProgram(vertex: String, fragment: String) {
    var id: UInt


    init {
        println("aa")
        val vertexSource = readFile(vertex) ?: throw Error("File $vertex not found")
        val fragmentSource = readFile(fragment) ?: throw Error("File $fragment not found")

        val vertexId = compile(GL_VERTEX_SHADER.toUInt(), vertexSource)
        val fragmentId = compile(GL_FRAGMENT_SHADER.toUInt(), fragmentSource)

        id = glCreateProgram!!()

        glAttachShader!!(id, vertexId)
        glAttachShader!!(id, fragmentId)

        glLinkProgram!!(id)

        checkStatus()

        glDeleteShader!!(vertexId)
        glDeleteShader!!(fragmentId)
        println("bb")
    }
    fun use() {
        glUseProgram!!(id)
    }
    private fun checkStatus() = memScoped {
        val status = alloc<IntVar>()
        glGetProgramiv!!(id, GL_LINK_STATUS.toUInt(), status.ptr)
        if (status.value != GL_TRUE.toInt()) {
            val log = allocArray<ByteVar>(512)
            glGetProgramInfoLog!!(id, 512, null, log)
            throw Error("Program linking errors: ${log.toKString()}")
        }
    }

    private fun compile(type: UInt, source: String) = memScoped {
        println("cc")
        val shader = glCreateShader!!(type)

        if (shader == 0u) throw Error("Failed to create shader")

        println("dd")

        glShaderSource!!(shader, 1, source.cstr.ptr.reinterpret(), null)

        println("ee")

        glCompileShader!!(shader)
        println("ff")

        val status = alloc<IntVar>()
        glGetShaderiv!!(shader, GL_COMPILE_STATUS.toUInt(), status.ptr)
        println("gg")

        if (status.value != GL_TRUE.toInt()) {
            val log = allocArray<ByteVar>(512)
            glGetShaderInfoLog!!(shader, 512, null, log)
            throw Error("Shader compilation failed: ${log.toKString()}")
        }

        println("hh")
        checkError("glShaderSource")
        println("jj")

        shader
    }
}