package io.horizontalsystems.bankwallet.ui.compose

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Stable
class Colors(
    jacob: Color,
    remus: Color,
    lucian: Color,
    oz: Color,
    tyler: Color,
    bran: Color,
    leah: Color,
    claude: Color,
    lawrence: Color
) {
    var jacob by mutableStateOf(jacob)
        private set
    var remus by mutableStateOf(remus)
        private set
    var lucian by mutableStateOf(lucian)
        private set
    var oz by mutableStateOf(oz)
        private set
    var tyler by mutableStateOf(tyler)
        private set
    var bran by mutableStateOf(bran)
        private set
    var leah by mutableStateOf(leah)
        private set
    var claude by mutableStateOf(claude)
        private set
    var lawrence by mutableStateOf(claude)
        private set

    fun update(other: Colors) {
        jacob = other.jacob
        remus = other.remus
        lucian = other.lucian
        oz = other.oz
        tyler = other.tyler
        bran = other.bran
        leah = other.leah
        claude = other.claude
        lawrence = other.lawrence
    }

    fun copy(): Colors = Colors(
        jacob = jacob,
        remus = remus,
        lucian = lucian,
        oz = oz,
        tyler = tyler,
        bran = bran,
        leah = leah,
        claude = claude,
        lawrence = lawrence
    )
}
