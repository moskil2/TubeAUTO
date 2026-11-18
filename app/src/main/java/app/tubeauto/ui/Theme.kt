package app.tubeauto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tubeauto.R

/** Kolory jak w OnlyMusic, z fioletów przestawione na czerwienie TubeAUTO. */
object Pal {
    val Bg = Color(0xFF0E0909)
    val Card = Color(0xFF1E1213)
    val Line = Color(0xFF34211F)
    val Text = Color(0xFFF2EAEA)
    val Text2 = Color(0xFFC4B1AF)
    val Text3 = Color(0xFFA08E8C)
    val Red = Color(0xFFFF0000)
    val Accent = Color(0xFFFF6B6B)
    val Close = Color(0xFFFF8A8A)
}

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

@Composable
fun Txt(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    color: Color = Pal.Text,
    weight: FontWeight = FontWeight.Normal,
    maxLines: Int = 1,
    lineHeight: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Start,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color, fontSize = size, fontWeight = weight, fontFamily = Inter,
            lineHeight = lineHeight, textAlign = align,
        ),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Napis z nazwą: "Tube" biały, "AUTO" czerwony jak w ikonie. */
@Composable
fun Wordmark(sizeSp: Int) {
    androidx.compose.foundation.layout.Row {
        Txt("Tube", size = sizeSp.sp, weight = FontWeight.SemiBold)
        Txt("AUTO", size = sizeSp.sp, weight = FontWeight.SemiBold, color = Pal.Red)
    }
}

/** Ikony liniowe z tych samych ścieżek co w OnlyMusic. */
object Ic {
    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

    private fun icon(stroke: String, width: Float = 1.8f): ImageVector =
        ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(
                pathData = addPathNodes(stroke),
                stroke = SolidColor(Color.White),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ).build()

    val Settings by lazy { icon("M4 7h10M18 7h2M4 17h3M11 17h9" + circle(16f, 7f, 2f) + circle(9f, 17f, 2f)) }
    val Back by lazy { icon("M15 5l-7 7 7 7") }
    val Chevron by lazy { icon("M9 5l7 7-7 7") }
    val Info by lazy { icon(circle(12f, 12f, 9f) + "M12 11v6M12 7.5v.5") }
    val Hamburger by lazy { icon("M4 7h16M4 12h16M4 17h16") }
    val Star by lazy { icon("M12 3.5l2.6 5.3 5.9.9-4.2 4.1 1 5.8-5.3-2.8-5.3 2.8 1-5.8-4.2-4.1 5.9-.9z") }
    val Update by lazy { icon("M12 19V6M6.5 11.5L12 6l5.5 5.5M5 20h14") }
    val Mail by lazy { icon("M4 6h16v12H4zM4.5 7l7.5 6 7.5-6") }
    val Globe by lazy { icon(circle(12f, 12f, 9f) + "M3 12h18M12 3c3.2 3 3.2 15 0 18M12 3c-3.2 3-3.2 15 0 18") }
    val Shield by lazy { icon("M12 3l7 3v5c0 4.5-3 8.2-7 10-4-1.8-7-5.5-7-10V6z") }
    val Power by lazy { icon("M12 3v8M7 6.5a7 7 0 1 0 10 0") }
}

@Composable
fun Icn(icon: ImageVector, color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Image(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(color),
    )
}

/** Tło zaznaczonej pozycji menu (jak w OnlyMusic, w czerwieniach). */
val SelectedBrush = Brush.horizontalGradient(listOf(Color(0x38FF2D2D), Color(0x29A8322E), Color(0x1FF2704B)))
