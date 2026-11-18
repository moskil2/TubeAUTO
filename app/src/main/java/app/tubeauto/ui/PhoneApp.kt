package app.tubeauto.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tubeauto.BuildConfig
import app.tubeauto.Browser
import app.tubeauto.Settings
import app.tubeauto.UblockStatus
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import app.tubeauto.R

private const val CONTACT_EMAIL = "tomasz.pieczara@gazeta.pl"
private const val WEBSITE = "spotrobotics.app"

enum class Screen { Main, Settings, Ublock, FilterLists, About, Licenses }

/** Ekran aplikacji na telefonie: nazwa na środku, menu boczne jak w OnlyMusic. */
@Composable
fun PhoneApp() {
    val context = LocalContext.current
    val stack = remember { mutableStateListOf(Screen.Main) }
    var menuOpen by remember { mutableStateOf(false) }
    val go: (Screen) -> Unit = { stack.add(it) }
    val back: () -> Unit = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }

    BackHandler(enabled = stack.size > 1 && !menuOpen, onBack = back)

    Box(Modifier.fillMaxSize().background(Pal.Bg)) {
        val top = stack.last()
        key(stack.size, top) {
            when (top) {
                Screen.Main -> MainPhoneScreen { menuOpen = true }
                Screen.Settings -> SettingsScreen(back)
                Screen.Ublock -> UblockScreen(back, go)
                Screen.FilterLists -> FilterListsScreen(back)
                Screen.About -> AboutScreen(back, go)
                Screen.Licenses -> LicensesScreen(back)
            }
        }
        SideMenu(
            open = menuOpen,
            current = top,
            onNavigate = { target ->
                menuOpen = false
                if (target != top) go(target)
            },
            onDismiss = { menuOpen = false },
            onCloseApp = { (context as? Activity)?.finishAndRemoveTask() },
        )
    }
}

@Composable
private fun MainPhoneScreen(openMenu: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().height(60.dp).padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIcon(Ic.Hamburger, "Menu", openMenu)
        }
        Box(Modifier.align(Alignment.Center)) { Wordmark(34) }
    }
}

private class MenuEntry(val label: String, val icon: ImageVector, val screen: Screen)

/** Menu boczne, układ jak w OnlyMusic: napis, przycisk zamknięcia, potem ekrany. */
@Composable
private fun SideMenu(open: Boolean, current: Screen, onNavigate: (Screen) -> Unit, onDismiss: () -> Unit, onCloseApp: () -> Unit) {
    val progress by animateFloatAsState(if (open) 1f else 0f, tween(220), label = "sideMenu")
    if (progress == 0f) return
    BackHandler(enabled = open, onBack = onDismiss)
    val entries = listOf(
        MenuEntry("Settings", Ic.Settings, Screen.Settings),
        MenuEntry("uBlock", Ic.Shield, Screen.Ublock),
        MenuEntry("About", Ic.Info, Screen.About),
    )
    val shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress }
                .background(Color(0xAD0A0404))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
        )
        Column(
            Modifier
                .fillMaxHeight()
                .width(300.dp)
                .graphicsLayer { translationX = -(1f - progress) * size.width }
                .shadow(24.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(shape)
                .drawBehind {
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF2A0E10), Color(0xFF240C12), Color(0xFF1C0B0D))))
                    drawRect(Brush.radialGradient(listOf(Color(0x55E62020), Color.Transparent), Offset(size.width * 0.2f, size.height * 0.12f), size.width * 0.9f))
                    drawRect(Brush.radialGradient(listOf(Color(0x40961E30), Color.Transparent), Offset(size.width * 0.8f, size.height * 0.9f), size.width))
                }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                    Wordmark(19)
                    Txt("YouTube on your car screen", size = 12.sp, color = Pal.Text3)
                }
                Row(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Pal.Close, RoundedCornerShape(16.dp))
                        .clickable(onClick = onCloseApp)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icn(Ic.Power, Pal.Close, 14.dp)
                    Spacer(Modifier.width(6.dp))
                    Txt("CLOSE", size = 12.sp, weight = FontWeight.SemiBold, color = Pal.Close)
                }
            }
            Spacer(Modifier.height(22.dp))
            entries.forEach { e ->
                val selected = e.screen == current
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .then(if (selected) Modifier.background(SelectedBrush) else Modifier)
                        .clickable { onNavigate(e.screen) }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icn(e.icon, if (selected) Pal.Accent else Pal.Text2, 20.dp)
                    Spacer(Modifier.width(14.dp))
                    Txt(e.label, size = 16.sp, weight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) Pal.Text else Pal.Text2)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(back: () -> Unit) {
    val context = LocalContext.current
    var theme by remember { mutableStateOf(Settings.theme(context)) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Settings", null, back)
        Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Page theme")
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Settings.Theme.entries.forEach { t ->
                            ChoiceButton(t.label, selected = t == theme) {
                                theme = t
                                Browser.setTheme(context, t)
                            }
                        }
                    }
                    Txt(
                        "Dark or light mode for YouTube and other pages. System follows the phone setting. If a theme is chosen in YouTube itself, YouTube uses that one.",
                        size = 13.sp, color = Pal.Text2, maxLines = 5, lineHeight = 19.sp,
                    )
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

/** uBlock Origin: wersja, ostatnie sprawdzenie, sprawdzenie na addons.mozilla.org, prośba o odświeżenie list. */
@Composable
private fun UblockScreen(back: () -> Unit, go: (Screen) -> Unit) {
    val context = LocalContext.current
    var installed by remember { mutableStateOf(UblockStatus.installedVersion(context)) }
    var checkTime by remember { mutableStateOf(UblockStatus.lastCheckTime(context)) }
    var checkResult by remember { mutableStateOf(UblockStatus.lastCheckResult(context)) }
    var message by remember { mutableStateOf<String?>(null) }
    var checking by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("uBlock", "uBlock Origin, ad blocker", back)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card {
                Column {
                    InfoRow("Installed version", installed ?: "not known yet")
                    Divider()
                    InfoRow(
                        "Last check",
                        if (checkTime == 0L) "never"
                        else SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(checkTime)),
                    )
                    Divider()
                    InfoRow("Result", checkResult ?: "-")
                }
            }
            SectionLabel("Add-on update", Modifier.padding(top = 8.dp))
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillButton(if (checking) "Checking..." else "Check now") {
                        if (checking) return@PillButton
                        checking = true
                        Browser.updateUblockNow(context) { result ->
                            message = result
                            installed = UblockStatus.installedVersion(context)
                            checkTime = UblockStatus.lastCheckTime(context)
                            checkResult = UblockStatus.lastCheckResult(context)
                            checking = false
                        }
                    }
                    message?.let { Txt(it, size = 13.sp, color = Pal.Text2, maxLines = 4, lineHeight = 19.sp) }
                }
            }
            SectionLabel("Filter lists", Modifier.padding(top = 8.dp))
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillButton("Refresh filter lists") { go(Screen.FilterLists) }
                    Txt(
                        "Opens the uBlock \"Filter lists\" page. Tap \"Update now\" there (the button is grey when all lists are up to date). Pinch to zoom the page.",
                        size = 13.sp, color = Pal.Text2, maxLines = 6, lineHeight = 19.sp,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

/** Strona uBlock "Listy filtrów" na ekranie telefonu (ten sam silnik co w aucie). */
@Composable
private fun FilterListsScreen(back: () -> Unit) {
    val context = LocalContext.current
    val session = remember { GeckoSession() }
    DisposableEffect(Unit) {
        session.open(Browser.runtimeFromPhone(context))
        Browser.whenUblockReady { ext ->
            if (session.isOpen) session.loadUri(ext.metaData.baseUrl + "dashboard.html#3p-filters.html")
        }
        onDispose { if (session.isOpen) session.close() }
    }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Filter lists", "uBlock Origin", back)
        AndroidView(
            factory = { ctx -> GeckoView(ctx).apply { setSession(session) } },
            onRelease = { it.releaseSession() },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        Spacer(Modifier.navigationBarsPadding())
    }
}

/** Przycisk wyboru: wybrany podświetlony jak pozycja w menu, pozostałe tylko z obramowaniem. */
@Composable
private fun ChoiceButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .then(if (selected) Modifier.background(SelectedBrush) else Modifier)
            .border(1.dp, Pal.Line, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(label, size = 14.sp, weight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) Pal.Text else Pal.Text2)
    }
}

@Composable
private fun PillButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SelectedBrush)
            .border(1.dp, Pal.Line, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(label, size = 14.sp, weight = FontWeight.SemiBold)
    }
}

/** Ekran "O aplikacji" jak w OnlyMusic; pozycje bez linków (TubeAUTO nie ma strony ani Google Play). */
@Composable
private fun AboutScreen(back: () -> Unit, go: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("About", null, back)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Wordmark(22)
                    Txt("Author: Tomasz Pieczara", size = 14.sp)
                }
            }
            Card {
                Column {
                    ActionRow(Ic.Star, "Rate on Google Play")
                    Divider()
                    ActionRow(Ic.Update, "Check for updates")
                }
            }
            Card {
                Column {
                    InfoRow("Version", BuildConfig.VERSION_NAME)
                    Divider()
                    InfoRow("Build", BuildConfig.BUILD_STAMP)
                }
            }
            Card {
                Column {
                    InfoRow("Contact", CONTACT_EMAIL, Ic.Mail)
                    Divider()
                    InfoRow("Website", WEBSITE, Ic.Globe)
                }
            }
            ExpandableTile(
                "Privacy policy",
                "TubeAUTO does not collect, send or store any personal data outside your phone. " +
                    "The app connects to the internet only to show the pages you open (e.g. YouTube) " +
                    "and to check for updates of the built-in uBlock Origin. The pages you visit follow their own policies. " +
                    "No accounts, no ads, no analytics, no tracking.",
            )
            ExpandableTile(
                "Terms of use",
                "TubeAUTO is a private app that shows web pages on your car screen. " +
                    "Watch only when parked - while driving, keep your attention on the road. " +
                    "The app is provided \"as is\", without any guarantee of uninterrupted or error-free operation. " +
                    "The author accepts no liability for any damage resulting from the use of the app - you use it at your own risk.",
            )
            ExpandableTile(
                "Contact / Report a bug / Feature request",
                "Found a bug or have an idea for a new feature? Or just want to say hello? " +
                    "Write to $CONTACT_EMAIL - I read every message myself.",
            )
            ExpandableTile(
                "Copyright",
                "© 2026 Tomasz Pieczara. All rights reserved. The source code, resources and documentation of this app are the property of the author. " +
                    "Copying, modifying, distributing or selling all or part of the code, in source or compiled form, " +
                    "without the written permission of the author is prohibited.",
            )
            Card {
                ActionRow(Ic.Info, "Open source licenses") { go(Screen.Licenses) }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

private class Component(val name: String, val owner: String, val license: String)

private val COMPONENTS = listOf(
    Component("GeckoView", "Mozilla", "Mozilla Public License 2.0"),
    Component("uBlock Origin", "Raymond Hill", "GNU General Public License v3"),
    Component("Android for Cars App Library", "Google", "Apache License 2.0"),
    Component("Jetpack Compose, AndroidX", "Google", "Apache License 2.0"),
    Component("Inter (font)", "Rasmus Andersson", "SIL Open Font License 1.1"),
)

@Composable
private fun LicensesScreen(back: () -> Unit) {
    val context = LocalContext.current
    val ofl = remember { context.resources.openRawResource(R.raw.license_ofl).bufferedReader().use { it.readText() } }
    val apache = remember { context.resources.openRawResource(R.raw.license_apache2).bufferedReader().use { it.readText() } }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Open source licenses", "Built with", back)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card {
                Column {
                    COMPONENTS.forEachIndexed { i, c ->
                        if (i > 0) Divider()
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Txt(c.name, size = 14.sp, weight = FontWeight.Medium)
                            Txt("${c.owner} · ${c.license}", size = 12.sp, color = Pal.Text3, maxLines = 2)
                        }
                    }
                }
            }
            SectionLabel("License texts", Modifier.padding(top = 8.dp))
            ExpandableTile("SIL Open Font License 1.1", ofl, maxLines = 400)
            ExpandableTile("Apache License 2.0", apache, maxLines = 400)
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

// ---------- elementy wspólne (jak w OnlyMusic) ----------

@Composable
private fun ScreenHeader(title: String, subtitle: String?, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().height(60.dp).padding(start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTap(Ic.Back, "Back", onBack, tint = Pal.Text)
        Column(Modifier.weight(1f)) {
            Txt(title, size = 19.sp, weight = FontWeight.SemiBold)
            if (subtitle != null) Txt(subtitle, size = 12.sp, color = Pal.Text3)
        }
        actions()
    }
}

@Composable
private fun IconTap(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color = Pal.Text2, size: Dp = 44.dp, iconSize: Dp = 20.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icn(icon, tint, iconSize)
    }
}

@Composable
private fun GlassIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(start = 6.dp)
            .size(52.dp)
            .shadow(6.dp, CircleShape, ambientColor = Color.Black, spotColor = Color.Black)
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f))), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        IconTap(icon, label, onClick, tint = Color(0xFFEAD8D8), size = 52.dp, iconSize = 26.dp)
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Txt(text.uppercase(), modifier.padding(start = 4.dp, bottom = 2.dp), size = 12.sp, weight = FontWeight.SemiBold, color = Pal.Text3)
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Pal.Card)) { content() }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(Pal.Line))
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icn(icon, Pal.Accent, 20.dp)
        Spacer(Modifier.width(14.dp))
        Txt(label, Modifier.weight(1f), size = 14.sp)
        Icn(Ic.Chevron, Pal.Text3, 18.dp)
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icn(icon, Pal.Accent, 20.dp)
            Spacer(Modifier.width(14.dp))
        }
        Txt(label, Modifier.weight(1f), size = 14.sp, color = Pal.Text2)
        Txt(value, size = 14.sp, color = Pal.Text)
    }
}

/** Stuknij, żeby przeczytać: tekst pokazuje się dopiero po otwarciu kafelka. */
@Composable
private fun ExpandableTile(title: String, body: String, maxLines: Int = 30) {
    var open by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Pal.Card)
            .clickable { open = !open }
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt(title, Modifier.weight(1f), size = 14.sp, weight = FontWeight.Medium)
            Box(Modifier.size(20.dp).rotate(if (open) 90f else 0f), contentAlignment = Alignment.Center) {
                Icn(Ic.Chevron, Pal.Text3, 18.dp)
            }
        }
        if (open) Txt(body, size = 13.sp, color = Pal.Text2, maxLines = maxLines, lineHeight = 20.sp)
    }
}
