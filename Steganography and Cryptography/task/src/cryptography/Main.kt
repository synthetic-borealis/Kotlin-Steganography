package cryptography

import java.io.File
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Color
import java.io.FileNotFoundException
import kotlin.experimental.xor

fun main() {
    var taskName = ""
    while (taskName != "exit") {
        println("Task (hide, show, exit):")
        taskName = readln()

        when (taskName) {
            "hide" -> hide()
            "exit" -> println("Bye!")
            "show" -> show()
            else -> println("Wrong task: $taskName")
        }
    }
}

fun show() {
    println("Input image file:")
    val inputFileName = readln()
    println("Password:")
    val password = readln()

    try {
        val image = ImageIO.read(File(inputFileName))
        val lsBits = mutableListOf<Int>()

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixelColor = Color(image.getRGB(x, y))
                lsBits += pixelColor.blue.toString(2).takeLast(1).toInt()
            }
        }

        val encryptedMessage = decodeMessage(lsBits)
        val message = encryptMessage(encryptedMessage, password)
        println("Message:")
        println(message)
    } catch (e: FileNotFoundException) {
        println("Can't read input file!")
    } catch (e: Exception) {
        println(e.message)
    }
}

fun hide() {
    println("Input image file:")
    val inputFileName = readln()
    println("Output image file:")
    val outputFileName = readln()
    println("Message to hide:")
    val message = readln()
    println("Password:")
    val password = readln()
    val encryptedMessage = encryptMessage(message, password)
    val bits = encodeMessage(encryptedMessage)

    try {
        val image = ImageIO.read(File(inputFileName))
        val availableBits = image.width * image.height
        val outImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        if (availableBits < bits.size) {
            throw Exception("The input image is not large enough to hold this message.")
        }

        println("Input Image: $inputFileName")
        println("Output Image: $outputFileName")

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixelColor = Color(image.getRGB(x, y))
                val r = pixelColor.red
                val g = pixelColor.green
                val bitIndex = y * image.width + x
                val b = if (bitIndex < bits.size) {
                    pixelColor.blue.and(254).or(bits[bitIndex]) % 256
                } else {
                    pixelColor.blue
                }
                val newColor = Color(r, g, b)
                outImage.setRGB(x, y, newColor.rgb)
            }
        }
        ImageIO.write(outImage, "png", File(outputFileName))

        println("Message saved in $outputFileName image.")
    } catch (e: FileNotFoundException) {
        println("Can't read input file!")
    } catch (e: Exception) {
        println(e.message)
    }
}

fun decodeMessage(bits: List<Int>): String {
    val endMark = "11".padStart(24, '0')
    val msgBytes = bits.joinToString("").split(endMark).first().chunked(8).map { it.toByte(2) }.toByteArray()
    return msgBytes.toString(Charsets.UTF_8)
}

fun encodeMessage(message: String): List<Int> {
    val messageToEncode = "$message\u0000\u0000\u0003"
    val encoded = mutableListOf<String>()

    messageToEncode.toByteArray(Charsets.UTF_8).forEach { encoded += it.toString(2).padStart(8, '0') }

    return encoded.joinToString("").map { it.digitToInt() }
}

fun encryptMessage(message: String, password: String): String {
    val messageBytes = message.toByteArray(Charsets.UTF_8)
    val passwordBytes = password.toByteArray(Charsets.UTF_8)
    val encryptedBytes = mutableListOf<Byte>()

    for (i in messageBytes.indices) {
        encryptedBytes += messageBytes[i].xor(passwordBytes[i % passwordBytes.size])
    }

    return encryptedBytes.toByteArray().toString(Charsets.UTF_8)
}
