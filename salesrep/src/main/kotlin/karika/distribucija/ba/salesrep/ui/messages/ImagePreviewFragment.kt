package karika.distribucija.ba.salesrep.ui.messages

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentImagePreviewBinding
import karika.distribucija.ba.salesrep.util.loadUrl

/** Mirrors composeApp's ImagePreview.kt - a full-screen white overlay showing the tapped
 * attachment with pinch-zoom (via [karika.distribucija.ba.salesrep.util.ZoomableImageView]) and
 * an explicit circular close button, rather than tap-to-dismiss. */
class ImagePreviewFragment : DialogFragment() {

    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Theme_SalesRep_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    /** Mirrors ImagePreview.kt's `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` on both
     * the image and the close button - padding the shared root keeps both inset from the system
     * bars together, since the root's white background already matches the window background. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = requireArguments().getString(ARG_URL)
        binding.imagePreview.loadUrl(url, viewLifecycleOwner)
        binding.buttonClosePreview.setOnClickListener { dismiss() }

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root,
            OnApplyWindowInsetsListener { root, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                root.updatePadding(top = bars.top, bottom = bars.bottom, left = bars.left, right = bars.right)
                insets
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String) = ImagePreviewFragment().apply {
            arguments = bundleOf(ARG_URL to url)
        }
    }
}
