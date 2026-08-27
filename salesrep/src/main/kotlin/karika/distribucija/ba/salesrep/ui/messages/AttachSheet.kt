package karika.distribucija.ba.salesrep.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.databinding.BottomSheetAttachBinding
import karika.distribucija.ba.salesrep.util.applyWhiteSheetBackground

/** Mirrors composeApp's "Dodaj prilog" ModalBottomSheet, duplicated 4 times (once per
 * Customer/Admin conversation/new-message screen) in the Compose source - consolidated here
 * into one reusable sheet since all 4 instances are visually and behaviorally identical. */
class AttachSheet(
    private val onPickFile: () -> Unit,
    private val onPickPhoto: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAttachBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAttachBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        applyWhiteSheetBackground()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rowAttachFile.setOnClickListener {
            dismiss()
            onPickFile()
        }
        binding.rowAttachPhoto.setOnClickListener {
            dismiss()
            onPickPhoto()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
