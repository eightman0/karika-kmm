package karika.distribucija.ba.ui.common

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIToolbar
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
fun createInputAccessoryToolbar(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit
): UIView {
    val toolbar = UIToolbar().apply {
        sizeToFit()
        translucent = true
    }

    //  val previousButton = UIBarButtonItem(
    //      image = UIImage.systemImageNamed("chevron.up"),
    //      style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
    //      target = onPrevious,
    //      action = null
    //  )
//
    //  val nextButton = UIBarButtonItem(
    //      image = UIImage.systemImageNamed("chevron.down"),
    //      style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
    //      target = onNext,
    //      action = null
    //  )
//
    val flexSpace = UIBarButtonItem(
        barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
        target = null,
        action = null
    )

    val doneButton = UIBarButtonItem().apply {
        title = "Zatvori"
        style = UIBarButtonItemStyle.UIBarButtonItemStyleDone
        primaryAction = UIAction.actionWithHandler { _ ->
            onDone()
        }
    }

    toolbar.setItems(
        listOf(
            //previousButton,
            //nextButton,
            flexSpace,
            doneButton
        ),
        animated = false
    )

    return toolbar
}

