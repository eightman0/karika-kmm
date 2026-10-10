package karika.distribucija.ba.ui.common

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIScreen
import platform.UIKit.UIToolbar
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
fun createInputAccessoryToolbar(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit
): UIView {
    // An accessory view needs a real frame: a toolbar created without one is 0 wide and
    // never shows. Width follows the screen, the toolbar resizes with rotation.
    val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
    val toolbar = UIToolbar(frame = CGRectMake(0.0, 0.0, screenWidth, 44.0)).apply {
        autoresizingMask = UIViewAutoresizingFlexibleWidth
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

