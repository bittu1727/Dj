package com.example.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MixRecorder(private val context: Context) {

    private var currentFile: File? = null
    private var fileOutputStream: FileOutputStream? = null
    private var isRecording = false
    private var startTimeMs = 0L
    private var totalBytesWritten = 0

    private val sampleRate = 44100
    private val numChannels = 2
    private val bitsPerSample = 16

    fun startRecording(): String? {
        if (isRecording) return currentFile?.absolutePath

        val recordingsDir = File(context.filesDir, "dj_recordings").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(recordingsDir, "Mix_${timeStamp}.wav")

        try {
            val fos = FileOutputStream(file)
            // Placeholder 44-byte WAV header
            val header = ByteArray(44)
            fos.write(header)

            currentFile = file
            fileOutputStream = fos
            isRecording = true
            startTimeMs = System.currentTimeMillis()
            totalBytesWritten = 0
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun writeMixedSamples(buffer: ShortArray, length: Int) {
        if (!isRecording || fileOutputStream == null) return
        try {
            val byteCount = length * 2
            val bytes = ByteArray(byteCount)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer, 0, length)
            fileOutputStream?.write(bytes)
            totalBytesWritten += byteCount
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return currentFile
        isRecording = false

        try {
            fileOutputStream?.flush()
            fileOutputStream?.close()
            fileOutputStream = null

            val file = currentFile ?: return null

            // Update WAV header with accurate lengths
            RandomAccessFile(file, "rw").use { raf ->
                val totalDataLen = totalBytesWritten + 36
                val byteRate = sampleRate * numChannels * (bitsPerSample / 8)

                val header = ByteArray(44)
                val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
                bb.put("RIFF".toByteArray())
                bb.putInt(totalDataLen)
                bb.put("WAVE".toByteArray())
                bb.put("fmt ".toByteArray())
                bb.putInt(16) // Subchunk1Size
                bb.putShort(1.toShort()) // PCM format
                bb.putShort(numChannels.toShort())
                bb.putInt(sampleRate)
                bb.putInt(byteRate)
                bb.putShort((numChannels * (bitsPerSample / 8)).toShort())
                bb.putShort(bitsPerSample.toShort())
                bb.put("data".toByteArray())
                bb.putInt(totalBytesWritten)

                raf.seek(0)
                raf.write(header)
            }

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getRecordingDurationMs(): Long {
        if (!isRecording) return 0L
        return System.currentTimeMillis() - startTimeMs
    }

    fun isRecordingActive(): Boolean = isRecording

    fun shareRecording(file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share DJ Mix").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
