package com.gfxtool.roblox.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Utility chạy lệnh shell với quyền root an toàn.
 * Bọc mọi lệnh trong try-catch, timeout 10 giây.
 */
object RootCommandRunner {

    data class Result(
        val exitCode : Int,
        val stdout   : String,
        val stderr   : String,
    ) {
        val isSuccess get() = exitCode == 0
    }

    /**
     * Chạy một lệnh shell với su.
     * @param command Lệnh cần thực thi (không cần prefix `su -c`)
     * @param timeoutMs Thời gian timeout tính bằng ms (mặc định 10s)
     */
    suspend fun exec(command: String, timeoutMs: Long = 10_000L): Result =
        withContext(Dispatchers.IO) {
            withTimeoutOrNull(timeoutMs) {
                try {
                    val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                    val stdout = proc.inputStream.bufferedReader().readText()
                    val stderr = proc.errorStream.bufferedReader().readText()
                    val exit   = proc.waitFor()
                    Result(exit, stdout, stderr)
                } catch (e: Exception) {
                    Result(-1, "", e.message ?: "Unknown error")
                }
            } ?: Result(-2, "", "Timeout sau ${timeoutMs}ms")
        }

    /**
     * Kiểm tra root khả dụng bằng cách chạy `id` và xác nhận uid=0.
     */
    suspend fun isAvailable(): Boolean =
        exec("id", timeoutMs = 3_000L).let { it.isSuccess && it.stdout.contains("uid=0") }

    /**
     * Ghi nội dung vào file với quyền root, tạo thư mục nếu chưa có.
     *
     * Lưu ý: trong bash, backslash KHÔNG escape được dấu nháy đơn bên trong
     * chuỗi single-quote. Kỹ thuật đúng là đóng quote, chèn nháy đơn đã escape,
     * rồi mở lại quote: ' → '\''
     */
    suspend fun writeFile(path: String, content: String): Result {
        val safe = content.replace("'", "'\\''")
        val dir = path.substringBeforeLast("/")
        return exec("mkdir -p '$dir' && printf '%s' '$safe' > '$path'")
    }

    /**
     * Đọc nội dung file với quyền root.
     */
    suspend fun readFile(path: String): Result = exec("cat '$path'")

    /**
     * Kiểm tra Roblox có đang chạy không (để cảnh báo user restart game).
     */
    suspend fun isRobloxRunning(): Boolean {
        val result = exec("pidof com.roblox.client || pidof com.roblox.client2")
        return result.isSuccess && result.stdout.isNotBlank()
    }
}