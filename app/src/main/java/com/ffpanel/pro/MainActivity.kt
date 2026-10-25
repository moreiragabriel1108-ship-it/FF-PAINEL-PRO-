package com.ffpanel.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random

private val Black = Color(0xFF070708)
private val SurfaceDark = Color(0xFF121215)
private val CardDark = Color(0xFF17171B)
private val Red = Color(0xFFFF1735)
private val RedDark = Color(0xFFB60019)
private val Muted = Color(0xFF9B9BA5)
private val White = Color(0xFFF6F6F8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FFPanelPro() }
    }
}

@Composable
fun FFPanelPro() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ff_panel_pro", 0) }
    var page by remember { mutableStateOf("Início") }
    var aimEnabled by remember { mutableStateOf(prefs.getBoolean("aim", true)) }
    var fps by remember { mutableStateOf(prefs.getString("fps", "120 FPS") ?: "120 FPS") }
    var sensitivity by remember { mutableFloatStateOf(prefs.getInt("sens", 100).toFloat()) }
    var hud by remember { mutableStateOf(prefs.getString("hud", "Personalizado") ?: "Personalizado") }
    var saved by remember { mutableStateOf(false) }
    var aimColor by remember { mutableStateOf(Red) }
    var targetX by remember { mutableFloatStateOf(0.5f) }
    var targetY by remember { mutableFloatStateOf(0.5f) }
    var hits by remember { mutableIntStateOf(0) }
    var shots by remember { mutableIntStateOf(0) }
    var showSplash by remember { mutableStateOf(true) }

    MaterialTheme(colorScheme = darkColorScheme(primary = Red, background = Black, surface = SurfaceDark, onBackground = White, onSurface = White)) {
        Surface(Modifier.fillMaxSize(), color = Black) {
            if (showSplash) {
                SplashScreen { showSplash = false }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().background(Black).padding(horizontal = 18.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Menu, null, tint = White, modifier = Modifier.size(25.dp).clickable { page = "Início" })
                        Spacer(Modifier.width(15.dp))
                        Text("FF ", color = White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                        Text("PANEL", color = Red, fontSize = 23.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Settings, "Configurações", tint = White, modifier = Modifier.clickable { page = "Configurações" }.size(24.dp))
                    }
                    HorizontalDivider(color = Red.copy(alpha = .65f), thickness = 1.dp)
                    Box(Modifier.weight(1f)) {
                        when (page) {
                            "Início" -> HomeScreen(aimEnabled, fps, sensitivity, hud, onOpenAim = { page = "Mira" }, onOpenSettings = { page = "Configurações" })
                            "Configurações" -> SettingsScreen(
                                aimEnabled, { aimEnabled = it }, fps, { fps = it },
                                sensitivity, { sensitivity = it }, hud, { hud = it },
                                saved, {
                                    prefs.edit().putBoolean("aim", aimEnabled).putString("fps", fps)
                                        .putInt("sens", sensitivity.toInt()).putString("hud", hud).apply()
                                    saved = true
                                }
                            )
                            "Mira" -> AimScreen(aimColor, targetX, targetY, hits, shots,
                                onTarget = { targetX = Random.nextFloat() * .7f + .15f; targetY = Random.nextFloat() * .7f + .15f; hits++; shots++ },
                                onMiss = { shots++ }, onColor = { aimColor = it },
                                onReset = { hits = 0; shots = 0 })
                            "HUD" -> HudScreen(hud, { hud = it })
                            else -> ProfileScreen(aimEnabled, fps, sensitivity.toInt(), hud, onNewProfile = {
                                prefs.edit().putBoolean("aim", aimEnabled).putString("fps", fps)
                                    .putInt("sens", sensitivity.toInt()).putString("hud", hud).apply()
                                saved = true
                            })
                        }
                    }
                    NavigationBar(containerColor = Color(0xFF0C0C0E), contentColor = Muted) {
                        listOf("Início", "Configurações", "HUD", "Perfil").forEach { item ->
                            val icon = when(item) {
                                "Início" -> Icons.Default.Home
                                "Configurações" -> Icons.Default.Settings
                                "HUD" -> Icons.Default.Tune
                                else -> Icons.Default.Person
                            }
                            NavigationBarItem(
                                selected = page == item,
                                onClick = { page = item },
                                icon = { Icon(icon, null) },
                                label = { Text(item, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Red, selectedTextColor = Red, indicatorColor = Red.copy(alpha = .12f), unselectedIconColor = Muted, unselectedTextColor = Muted)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Black).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(112.dp).border(2.dp, Red, RoundedCornerShape(28.dp)).background(SurfaceDark, RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.GpsFixed, null, tint = Red, modifier = Modifier.size(66.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("FF PANEL", fontSize = 36.sp, fontWeight = FontWeight.Black, color = White)
        Text("AIM  •  FPS  •  HUD  •  SENSI", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(18.dp))
        Text("Seu painel de configurações gamer", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(42.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Red), shape = RoundedCornerShape(12.dp)) {
            Text("COMEÇAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Default.ArrowForward, null)
        }
        Spacer(Modifier.height(18.dp))
        Text("PROTÓTIPO INDEPENDENTE", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
    }
}

@Composable
fun HomeScreen(aim: Boolean, fps: String, sens: Float, hud: String, onOpenAim: () -> Unit, onOpenSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).background(Red.copy(alpha = .15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Bolt, null, tint = Red, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("BEM-VINDO AO PAINEL", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Seu setup gamer", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Ajuste suas preferências em um só lugar.", color = Muted, fontSize = 12.sp)
            }
        }
        CardBox {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Pill(if (aim) "ATIVADO" else "DESATIVADO")
                    Spacer(Modifier.height(9.dp))
                    Text("MIRA DE TREINO", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Pratique sua precisão dentro do painel.", color = Muted, fontSize = 12.sp)
                }
                Icon(Icons.Default.GpsFixed, null, tint = Red, modifier = Modifier.size(54.dp))
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenAim, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red), shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Default.GpsFixed, null)
                Spacer(Modifier.width(8.dp))
                Text("ABRIR MIRA DE TREINO", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("FPS PREFERIDO", fps, Icons.Default.Bolt, Modifier.weight(1f))
            StatCard("SENSIBILIDADE", sens.toInt().toString(), Icons.Default.GpsFixed, Modifier.weight(1f))
        }
        CardBox {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, null, tint = Red, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("HUD $hud".uppercase(), fontWeight = FontWeight.Bold)
                    Text("Layout e preferências de controle.", color = Muted, fontSize = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Red, modifier = Modifier.clickable { onOpenSettings() })
            }
        }
        Text("ATALHOS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = White), border = androidx.compose.foundation.BorderStroke(1.dp, Red.copy(alpha = .6f))) {
            Icon(Icons.Default.Settings, null, tint = Red)
            Spacer(Modifier.width(8.dp))
            Text("CONFIGURAÇÕES COMPLETAS")
        }
    }
}

@Composable
fun SettingsScreen(aim: Boolean, onAim: (Boolean)->Unit, fps: String, onFps: (String)->Unit, sens: Float, onSens: (Float)->Unit, hud: String, onHud: (String)->Unit, saved: Boolean, onSave: ()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("CONFIGURAÇÕES", fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("PERSONALIZE SUA EXPERIÊNCIA", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        CardBox {
            Text("GERAL", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GpsFixed, null, tint = Red)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Mira de treino", fontWeight = FontWeight.Bold)
                    Text("Alterne a ferramenta de treino do painel.", color = Muted, fontSize = 11.sp)
                }
                Switch(checked = aim, onCheckedChange = onAim, colors = SwitchDefaults.colors(checkedTrackColor = Red))
            }
            HorizontalDivider(color = Color(0xFF333338))
            Text("FPS PREFERIDO", fontWeight = FontWeight.Bold)
            Text("Preferência visual; não força o FPS do jogo.", color = Muted, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("60 FPS", "90 FPS", "120 FPS").forEach { v ->
                    FilterChip(selected = fps == v, onClick = { onFps(v) }, label = { Text(v) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Red, selectedLabelColor = White))
                }
            }
            HorizontalDivider(color = Color(0xFF333338))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sensibilidade", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(sens.toInt().toString(), color = Red, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Slider(value = sens, onValueChange = onSens, valueRange = 0f..100f, colors = SliderDefaults.colors(thumbColor = Red, activeTrackColor = Red))
            HorizontalDivider(color = Color(0xFF333338))
            Text("HUD", fontWeight = FontWeight.Bold)
            listOf("Padrão", "Personalizado", "Quatro dedos").forEach { v ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = hud == v, onClick = { onHud(v) }, colors = RadioButtonDefaults.colors(selectedColor = Red))
                    Text(v)
                }
            }
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Red), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Save, null)
            Spacer(Modifier.width(8.dp))
            Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.Black)
        }
        if (saved) Text("✓ Configurações salvas neste dispositivo.", color = Color(0xFF7EE49B), fontSize = 12.sp)
    }
}

@Composable
fun AimScreen(color: Color, x: Float, y: Float, hits: Int, shots: Int, onTarget: ()->Unit, onMiss: ()->Unit, onColor: (Color)->Unit, onReset: ()->Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("MIRA DE TREINO", fontSize = 22.sp, fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("ACERTOS", hits.toString(), Modifier.weight(1f))
            MiniStat("TENTATIVAS", shots.toString(), Modifier.weight(1f))
            MiniStat("PRECISÃO", if (shots == 0) "0%" else "${(hits * 100 / shots)}%", Modifier.weight(1f))
        }
        Box(Modifier.weight(1f).fillMaxWidth().border(1.dp, Red.copy(alpha = .7f), RoundedCornerShape(14.dp)).background(Color(0xFF09090B), RoundedCornerShape(14.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                for (i in 1..5) drawCircle(color.copy(alpha = .18f), radius = i * 32f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
                drawLine(color.copy(alpha = .3f), Offset(center.x, 0f), Offset(center.x, size.height), 1.dp.toPx())
                drawLine(color.copy(alpha = .3f), Offset(0f, center.y), Offset(size.width, center.y), 1.dp.toPx())
                val t = Offset(size.width * x, size.height * y)
                drawCircle(color, radius = 20.dp.toPx(), center = t, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                drawCircle(color, radius = 4.dp.toPx(), center = t)
                drawLine(color, Offset(t.x - 30.dp.toPx(), t.y), Offset(t.x - 10.dp.toPx(), t.y), 2.dp.toPx())
                drawLine(color, Offset(t.x + 10.dp.toPx(), t.y), Offset(t.x + 30.dp.toPx(), t.y), 2.dp.toPx())
                drawLine(color, Offset(t.x, t.y - 30.dp.toPx()), Offset(t.x, t.y - 10.dp.toPx()), 2.dp.toPx())
                drawLine(color, Offset(t.x, t.y + 10.dp.toPx()), Offset(t.x, t.y + 30.dp.toPx()), 2.dp.toPx())
            }
            Box(Modifier.fillMaxSize().clickable { onTarget() })
            Text("TOQUE NA TELA PARA ACERTAR O ALVO", color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Cor da mira", color = Muted)
            Spacer(Modifier.width(10.dp))
            listOf(Red, Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFFFFC107), Color(0xFFD500F9)).forEach { c ->
                Box(Modifier.padding(horizontal = 4.dp).size(25.dp).background(c, CircleShape).border(if (c == color) 2.dp else 0.dp, White, CircleShape).clickable { onColor(c) })
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onReset) { Text("REINICIAR", color = Red) }
        }
        Text("Treinamento isolado dentro do aplicativo; não interage com o jogo.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
fun HudScreen(hud: String, onHud: (String)->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("HUD PERSONALIZADO", fontSize = 21.sp, fontWeight = FontWeight.Black)
        Text("Escolha um perfil de layout para organizar suas preferências.", color = Muted, fontSize = 12.sp)
        Box(Modifier.fillMaxWidth().height(205.dp).border(1.dp, Red, RoundedCornerShape(14.dp)).background(Color(0xFF101013), RoundedCornerShape(14.dp)).padding(12.dp)) {
            Box(Modifier.fillMaxSize().border(1.dp, Color(0xFF333338), RoundedCornerShape(8.dp))) {
                listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd, Alignment.BottomStart, Alignment.BottomEnd).forEachIndexed { i, a ->
                    Box(Modifier.align(a).padding(8.dp).size(if (i == 3) 36.dp else 25.dp).border(1.dp, Red, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) {
                        if (i == 3) Icon(Icons.Default.GpsFixed, null, tint = Red, modifier = Modifier.size(22.dp))
                        else Icon(Icons.Default.ControlCamera, null, tint = Muted, modifier = Modifier.size(15.dp))
                    }
                }
            }
            Text("PRÉVIA DO LAYOUT", color = Red, fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomCenter))
        }
        listOf("Padrão", "Personalizado", "Quatro dedos").forEach { v ->
            Row(Modifier.fillMaxWidth().background(CardDark, RoundedCornerShape(12.dp)).clickable { onHud(v) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, null, tint = if (hud == v) Red else Muted)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(v, fontWeight = FontWeight.Bold)
                    Text(when(v) { "Padrão" -> "Layout simples"; "Personalizado" -> "Ajuste ao seu estilo"; else -> "Controles para quatro dedos" }, color = Muted, fontSize = 11.sp)
                }
                RadioButton(selected = hud == v, onClick = { onHud(v) }, colors = RadioButtonDefaults.colors(selectedColor = Red))
            }
        }
        Text("A prévia é ilustrativa: não altera o HUD dentro do Free Fire.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
fun ProfileScreen(aim: Boolean, fps: String, sens: Int, hud: String, onNewProfile: ()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("PERFIL", fontSize = 22.sp, fontWeight = FontWeight.Black)
        CardBox {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).background(Red.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Red, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Meu Perfil", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("ID: FF_PANEL_USER", color = Muted, fontSize = 11.sp)
                    Text("CONFIGURAÇÃO LOCAL", color = Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("CONFIGURAÇÕES ATUAIS", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        CardBox {
            ProfileRow("Mira de treino", if (aim) "Ativada" else "Desativada")
            ProfileRow("FPS preferido", fps)
            ProfileRow("Sensibilidade", sens.toString())
            ProfileRow("HUD", hud)
        }
        Text("PERFIS DE SENSIBILIDADE", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        CardBox {
            Text("Meu Perfil", fontWeight = FontWeight.Bold)
            Text("Sensibilidade $sens • HUD $hud • $fps", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = onNewProfile, modifier = Modifier.fillMaxWidth(), border = androidx.compose.foundation.BorderStroke(1.dp, Red), colors = ButtonDefaults.outlinedButtonColors(contentColor = White)) {
                Icon(Icons.Default.Save, null, tint = Red)
                Spacer(Modifier.width(8.dp))
                Text("SALVAR PERFIL ATUAL")
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = Color(0xFF2C2C31))
}

@Composable
fun CardBox(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().border(1.dp, Color(0xFF29292F), RoundedCornerShape(16.dp)).background(CardDark, RoundedCornerShape(16.dp)).padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, null, tint = Red)
            Text(value, color = Red, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text(label, color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Preferência configurável", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(CardDark, RoundedCornerShape(10.dp)).padding(10.dp)) {
        Text(label, color = Muted, fontSize = 9.sp)
        Text(value, color = Red, fontWeight = FontWeight.Black, fontSize = 17.sp)
    }
}

@Composable
fun Pill(text: String) {
    Box(Modifier.background(Red.copy(alpha = .16f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, color = Red, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}
