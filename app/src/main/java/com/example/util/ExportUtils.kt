package com.example.util

import android.content.Context
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ExportUtils {
    fun buildCsvContent(headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder("\uFEFF")
        fun escapeCell(value: String): String {
            val sanitized = value.replace("\"", "\"\"")
            return "\"$sanitized\""
        }
        sb.append(headers.joinToString(",") { escapeCell(it) }).append("\n")
        rows.forEach { row ->
            sb.append(row.joinToString(",") { escapeCell(it) }).append("\n")
        }
        return sb.toString()
    }

    suspend fun writeTextToUri(context: Context, uri: Uri, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: return@withContext Result.failure(IllegalStateException("Unable to open output stream"))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun readTextFromUri(context: Context, uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: return@withContext Result.failure(IllegalStateException("Unable to read selected file"))
                Result.success(text)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private var activePrintWebView: WebView? = null

    fun printReportHtml(
        context: Context,
        jobName: String,
        title: String,
        subtitle: String,
        summaryPairs: List<Pair<String, String>>,
        headers: List<String>,
        rows: List<List<String>>
    ) {
        fun esc(s: String): String = s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

        val summaryHtml = summaryPairs.joinToString("") { (k, v) ->
            "<div class='stat'><div class='stat-k'>${esc(k)}</div><div class='stat-v'>${esc(v)}</div></div>"
        }
        val headerHtml = headers.joinToString("") { "<th>${esc(it)}</th>" }
        val rowsHtml = rows.joinToString("") { row ->
            "<tr>" + row.joinToString("") { cell -> "<td>${esc(cell)}</td>" } + "</tr>"
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8" />
              <style>
                body { font-family: sans-serif; color: #0f172a; margin: 24px; }
                .header { border-bottom: 2px solid #1e3a8a; padding-bottom: 12px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: flex-end; }
                .brand { font-size: 20px; font-weight: 800; color: #1e3a8a; }
                .sub { font-size: 12px; color: #475569; margin-top: 4px; }
                .meta { font-size: 11px; color: #64748b; text-align: right; }
                .stats { display: flex; gap: 12px; margin-bottom: 18px; }
                .stat { flex: 1; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 12px; }
                .stat-k { font-size: 11px; color: #64748b; }
                .stat-v { font-size: 18px; font-weight: 700; color: #0f172a; margin-top: 4px; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; }
                th { background: #1e293b; color: #ffffff; text-align: left; padding: 8px 10px; }
                td { border-bottom: 1px solid #e2e8f0; padding: 8px 10px; }
                tr:nth-child(even) td { background: #f8fafc; }
                .footer { margin-top: 20px; font-size: 10px; color: #64748b; text-align: center; }
              </style>
            </head>
            <body>
              <div class="header">
                <div>
                  <div class="brand">AHB PMS — ${esc(title)}</div>
                  <div class="sub">${esc(subtitle)}</div>
                </div>
                <div class="meta">Printed: ${esc(DateUtils.formatDisplayDate())}</div>
              </div>
              <div class="stats">$summaryHtml</div>
              <table>
                <thead><tr>$headerHtml</tr></thead>
                <tbody>$rowsHtml</tbody>
              </table>
              <div class="footer">AHB PMS · Garment Production Management System</div>
            </body>
            </html>
        """.trimIndent()

        try {
            val webView = WebView(context)
            activePrintWebView = webView
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager != null && view != null) {
                        val printAdapter = view.createPrintDocumentAdapter(jobName)
                        printManager.print(
                            jobName,
                            printAdapter,
                            PrintAttributes.Builder().build()
                        )
                    }
                    activePrintWebView = null
                }
            }
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        } catch (_: Exception) {
            activePrintWebView = null
        }
    }
}
