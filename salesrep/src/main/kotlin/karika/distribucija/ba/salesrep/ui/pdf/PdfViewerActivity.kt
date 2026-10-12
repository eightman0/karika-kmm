package karika.distribucija.ba.salesrep.ui.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ActivityPdfViewerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * In-app document screen for order PDFs and message / comment attachments. The app runs on
 * kiosk-locked tablets, so handing the URL to a browser (the old ACTION_VIEW) either fails or
 * leaves the app. WebView can't display PDFs on Android, so the file is downloaded and its pages
 * drawn with [PdfRenderer]; anything that isn't a PDF is shown in a WebView instead.
 */
class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private var renderer: PdfRenderer? = null
    private var descriptor: ParcelFileDescriptor? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val renderDispatcher = Dispatchers.IO.limitedParallelism(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.buttonBack.setOnClickListener { finish() }
        binding.textTitle.text = intent.getStringExtra(EXTRA_TITLE)?.takeIf { it.isNotBlank() }
            ?: getString(R.string.pdf_viewer_title)

        val url = intent.getStringExtra(EXTRA_URL)
        if (url.isNullOrBlank()) showError() else load(url)
    }

    private fun load(url: String) {
        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) { runCatching { download(url) }.getOrNull() }
            when {
                file == null -> showError()
                file.isPdf() -> showPdf(file)
                else -> showWeb(url)
            }
        }
    }

    private fun download(url: String): File {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        try {
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            val file = File(cacheDir, "viewer.pdf")
            connection.inputStream.use { input -> file.outputStream().use { input.copyTo(it) } }
            return file
        } finally {
            connection.disconnect()
        }
    }

    private fun File.isPdf(): Boolean = runCatching {
        inputStream().use { input ->
            val header = ByteArray(4)
            input.read(header) == 4 && String(header) == "%PDF"
        }
    }.getOrDefault(false)

    private fun showPdf(file: File) {
        val opened = runCatching {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            descriptor = fd
            renderer = PdfRenderer(fd)
        }
        val pdf = renderer
        if (opened.isFailure || pdf == null || pdf.pageCount == 0) {
            showError()
            return
        }
        binding.progress.visibility = View.GONE
        binding.recyclerPages.visibility = View.VISIBLE
        binding.recyclerPages.layoutManager = LinearLayoutManager(this)
        binding.recyclerPages.adapter = PageAdapter(pdf.pageCount)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWeb(url: String) {
        binding.progress.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        // Keep every navigation inside the WebView - never hand a link to an outside browser.
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false
        }
        binding.webView.loadUrl(url)
    }

    private fun showError() {
        binding.progress.visibility = View.GONE
        binding.recyclerPages.visibility = View.GONE
        binding.webView.visibility = View.GONE
        binding.textError.visibility = View.VISIBLE
    }

    /** Renders a page at the list's width, white background, on the single render thread
     * (PdfRenderer allows one open page at a time). */
    private suspend fun renderPage(index: Int, width: Int): Bitmap? = withContext(renderDispatcher) {
        val pdf = renderer ?: return@withContext null
        runCatching {
            pdf.openPage(index).use { page ->
                val height = (width.toFloat() * page.height / page.width).toInt()
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }
        }.getOrNull()
    }

    private inner class PageAdapter(private val count: Int) : RecyclerView.Adapter<PageAdapter.Holder>() {

        inner class Holder(val image: ImageView) : RecyclerView.ViewHolder(image) {
            var job: Job? = null
        }

        override fun getItemCount() = count

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val density = parent.resources.displayMetrics.density
            val image = ImageView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (12 * density).toInt() }
                adjustViewBounds = true
                setBackgroundColor(Color.WHITE)
                elevation = 2 * density
            }
            return Holder(image)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.job?.cancel()
            holder.image.setImageDrawable(null)
            holder.image.minimumHeight = binding.recyclerPages.width
            holder.image.contentDescription = getString(R.string.pdf_viewer_page_cd, position + 1)
            val width = binding.recyclerPages.width - binding.recyclerPages.paddingStart - binding.recyclerPages.paddingEnd
            if (width <= 0) return
            holder.job = lifecycleScope.launch {
                val bitmap = renderPage(position, width)
                holder.image.minimumHeight = 0
                holder.image.setImageBitmap(bitmap)
            }
        }

        override fun onViewRecycled(holder: Holder) {
            holder.job?.cancel()
            holder.image.setImageDrawable(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { renderer?.close() }
        runCatching { descriptor?.close() }
        binding.webView.destroy()
    }

    companion object {
        private const val EXTRA_URL = "url"
        private const val EXTRA_TITLE = "title"

        fun open(context: Context, url: String, title: String? = null) {
            context.startActivity(
                Intent(context, PdfViewerActivity::class.java)
                    .putExtra(EXTRA_URL, url)
                    .putExtra(EXTRA_TITLE, title)
                    .apply { if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }
    }
}
