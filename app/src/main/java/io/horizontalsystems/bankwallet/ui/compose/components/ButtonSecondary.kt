package io.horizontalsystems.bankwallet.ui.compose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.horizontalsystems.bankwallet.ui.compose.*

@Composable
fun ButtonSecondaryDefault(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    ButtonSecondary(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = Steel20,
            contentColor = ComposeAppTheme.colors.oz
        ),
        content = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        enabled = enabled
    )
}

@Composable
fun ButtonSecondaryTransparent(
    modifier: Modifier = Modifier,
    title: String,
    @DrawableRes iconRight: Int? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val textColor = if (isPressed) Grey else ComposeAppTheme.colors.oz

    ButtonSecondary(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = textColor
        ),
        content = {
            if (iconRight != null) {
                Row {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(
                        modifier = Modifier.padding(start = 4.dp),
                        painter = painterResource(id = iconRight),
                        contentDescription = null,
                        tint = Grey
                    )
                }
            } else {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled
    )
}

@Composable
fun ButtonSecondaryToggle(
    toggleIndicators: List<ToggleIndicator>,
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    ButtonSecondary(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = Steel20,
            contentColor = ComposeAppTheme.colors.oz
        ),
        content = {
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Column(
                    modifier = Modifier.height(16.dp).padding(start = 12.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    toggleIndicators.forEach { indicator ->
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(if (indicator.enabled) ComposeAppTheme.colors.jacob else Grey)
                        )
                    }
                }
            }
        },
        enabled = enabled
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ButtonSecondary(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = rememberRipple(),
    shape: Shape = RoundedCornerShape(14.dp),
    border: BorderStroke? = null,
    colors: ButtonColors = SecondaryButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = SecondaryButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor by colors.contentColor(enabled)
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = 0.dp,
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interactionSource,
        indication = indication
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(
                value = ComposeAppTheme.typography.subhead1
            ) {
                Row(
                    Modifier
                        .defaultMinSize(
                            minWidth = SecondaryButtonDefaults.MinWidth,
                            minHeight = SecondaryButtonDefaults.MinHeight
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

object SecondaryButtonDefaults {
    private val ButtonHorizontalPadding = 16.dp

    val ContentPadding = PaddingValues(
        start = ButtonHorizontalPadding,
        end = ButtonHorizontalPadding
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
    val MinHeight = 27.dp

    @Composable
    fun textButtonColors(
        backgroundColor: Color = Color.Transparent,
        contentColor: Color = ComposeAppTheme.colors.claude,
        disabledBackgroundColor: Color = Steel20,
        disabledContentColor: Color = Grey50,
    ): ButtonColors = DefaultButtonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        disabledBackgroundColor = disabledBackgroundColor,
        disabledContentColor = disabledContentColor
    )
}

class ToggleIndicator(val enabled: Boolean)