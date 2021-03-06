package io.horizontalsystems.bankwallet.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.YellowD

@Composable
fun TabRounded(title: String, onSelect: () -> Unit, selected: Boolean = false) {
    TabBox(
        colors = TabDefaults.textButtonColors(),
        content = { Text(title) },
        selected = selected,
        onSelect = onSelect
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabBox(
    selected: Boolean = false,
    shape: Shape = RoundedCornerShape(14.dp),
    colors: ButtonColors = TabDefaults.textButtonColors(),
    contentPadding: PaddingValues = TabDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
    onSelect: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        val contentColor by colors.contentColor(selected)
        Surface(
            onClick = onSelect,
            color = colors.backgroundColor(selected).value,
            shape = shape,
            contentColor = contentColor,
        ) {
            // Text is a predefined composable that does exactly what you'd expect it to -
            // display text on the screen. It allows you to customize its appearance using the
            // style property.
            CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
                ProvideTextStyle(
                    value = ComposeAppTheme.typography.subhead1
                ) {
                    Row(
                        Modifier
                            .defaultMinSize(
                                minWidth = TabDefaults.MinWidth,
                                minHeight = TabDefaults.MinHeight
                            )
                            .padding(contentPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        content = content
                    )
                }
            }
        }
    }
}

object TabDefaults {
    private val ButtonHorizontalPadding = 16.dp
    private val ButtonVerticalPadding = 6.dp

    val ContentPadding = PaddingValues(
        start = ButtonHorizontalPadding,
        top = ButtonVerticalPadding,
        end = ButtonHorizontalPadding,
        bottom = ButtonVerticalPadding
    )


    /**
     * The default min width applied for the [Button].
     * Note that you can override it by applying Modifier.widthIn directly on [Button].
     */
    val MinWidth = 50.dp

    /**
     * The default min width applied for the [Button].
     * Note that you can override it by applying Modifier.heightIn directly on [Button].
     */
    val MinHeight = 28.dp

    @Composable
    fun textButtonColors(
        backgroundColor: Color = Color.Transparent,
        contentColor: Color = ComposeAppTheme.colors.oz,
        activeBackgroundColor: Color = YellowD,
        activeContentColor: Color = ComposeAppTheme.colors.claude,
    ): ButtonColors = DefaultTabColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        activeBackgroundColor = activeBackgroundColor,
        activeContentColor = activeContentColor
    )
}

@Immutable
private class DefaultTabColors(
    private val backgroundColor: Color,
    private val contentColor: Color,
    private val activeBackgroundColor: Color,
    private val activeContentColor: Color
) : ButtonColors {
    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) activeBackgroundColor else backgroundColor)
    }

    @Composable
    override fun contentColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) activeContentColor else contentColor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultTabColors

        if (backgroundColor != other.backgroundColor) return false
        if (contentColor != other.contentColor) return false
        if (activeBackgroundColor != other.activeBackgroundColor) return false
        if (activeContentColor != other.activeContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + activeBackgroundColor.hashCode()
        result = 31 * result + activeContentColor.hashCode()
        return result
    }
}