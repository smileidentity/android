package com.smileidentity.util.watermark.utils

object StringUtils {
    init {
        System.loadLibrary("Watermark")
    }

    /**
     * Converting a [String] text into a binary text.
     *
     *
     * This is the native version.
     */
    external fun stringToBinary(inputText: String?): String?

    /**
     * String to integer array.
     *
     *
     * This is the native version.
     */
    external fun stringToIntArray(inputString: String?): IntArray?

    /**
     * Converting a binary string to a ASCII string.
     */
    @JvmStatic
    external fun binaryToString(inputText: String?): String?

    /**
     * get the single digit number and set it to the target one.
     */
    fun replaceSingleDigit(target: Int, singleDigit: Int): Int {
        return (target / 10) * 10 + singleDigit
    }

    /**
     * Converts a string to an array of integers by interpreting each character as a digit.
     * Each character's numeric value is calculated by subtracting the ASCII value of '0'.
     *
     * @param inputString The string to convert
     * @return An integer array with each element being the numeric value of the corresponding character
     */
    fun nonNativeStringToIntArray(inputString: String?): IntArray? {
        // Return null if input is null
        if (inputString == null) return null

        // Create an integer array with the same length as the input string
        return IntArray(inputString.length) { i ->
            // Convert each character to an integer by subtracting the ASCII value of '0'
            inputString[i].code - '0'.code
        }
    }

    /**
     * Converts a string to its binary representation.
     * Each character in the input string is converted to an 8-bit binary string.
     *
     * @param inputText The string to convert
     * @return A string containing the binary representation of the input, or null if the input is null
     */
    fun nonNativeStringToBinary(inputText: String?): String? {
        if (inputText == null) {
            return null
        }

        val result = StringBuilder()

        for (char in inputText) {
            // Convert each character to its 8-bit binary representation
            val binaryChar = Integer.toBinaryString(char.code)

            // Ensure each binary representation is 8 bits by padding with leading zeros if needed
            val paddedBinary = binaryChar.padStart(8, '0')

            result.append(paddedBinary)
        }

        return result.toString()
    }

    /**
     * Converts a binary string back to a regular text string.
     * Each 8 bits in the binary string are interpreted as a character.
     *
     * @param inputText The binary string to convert
     * @return The text string representation of the binary input, or null if the input is null
     */
    fun nonNativeBinaryToString(inputText: String?): String? {
        if (inputText == null) {
            return null
        }

        val result = StringBuilder()
        var i = 0

        // Process the binary string in 8-bit chunks
        while (i + 8 <= inputText.length) {
            // Take the next 8 bits
            val byte = inputText.substring(i, i + 8)

            // Convert the 8-bit binary string to an integer
            val decimal = byte.toInt(2)

            // Convert the integer to a character and append it
            result.append(decimal.toChar())

            // Move to the next 8 bits
            i += 8
        }

        return result.toString()
    }
}
