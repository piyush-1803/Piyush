package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TaskEntity
import com.example.data.UserStatsEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.AuraTeal
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraGlowGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeutralSubtle
import com.example.ui.theme.NeutralLight
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityLow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                AuraProductivityApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraProductivityApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val stats by viewModel.userStats.collectAsState()
    val context = LocalContext.current

    val shopMsg by viewModel.shopStatusMessage.collectAsState()
    LaunchedEffect(shopMsg) {
        if (shopMsg.isNotEmpty()) {
            Toast.makeText(context, shopMsg, Toast.LENGTH_SHORT).show()
            viewModel.clearShopStatus()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("root_scaffold")
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            AuraBottomNav(
                currentTab = currentTab,
                onTabSelect = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith
                    fadeOut(animationSpec = tween(220))
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    "DASHBOARD" -> DashboardTab(
                        viewModel = viewModel,
                        stats = stats,
                        tasks = tasks
                    )
                    "TASKS" -> TasksTab(
                        viewModel = viewModel,
                        tasks = tasks
                    )
                    "AURA_AI" -> AuraAiTab(
                        viewModel = viewModel
                    )
                    "UPGRADES" -> FocusShopTab(
                        viewModel = viewModel,
                        stats = stats
                    )
                }
            }
        }
    }
}

@Composable
fun AuraBottomNav(
    currentTab: String,
    onTabSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home, // Core Icon for Dashboard
                label = "Dashboard",
                isSelected = currentTab == "DASHBOARD",
                tag = "tab_dashboard",
                onClick = { onTabSelect("DASHBOARD") }
            )
            NavItem(
                icon = Icons.Default.List, // Core Icon for Grid
                label = "Grid",
                isSelected = currentTab == "TASKS",
                tag = "tab_tasks",
                onClick = { onTabSelect("TASKS") }
            )
            NavItem(
                icon = Icons.Default.Star, // Core Icon for Aura AI
                label = "Aura AI",
                isSelected = currentTab == "AURA_AI",
                tag = "tab_aura_ai",
                onClick = { onTabSelect("AURA_AI") }
            )
            NavItem(
                icon = Icons.Default.ShoppingCart, // Core Icon for Shop
                label = "Shop",
                isSelected = currentTab == "UPGRADES",
                tag = "tab_upgrades",
                onClick = { onTabSelect("UPGRADES") }
            )
        }
    }
}

@Composable
fun RowScope.NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val transition = updateTransition(targetState = isSelected, label = "nav_item_transition")
    val alpha by transition.animateFloat(label = "alpha") { if (it) 1.0f else 0.5f }

    Column(
        modifier = Modifier
            .weight(1f)
            .testTag(tag)
            .clickable(
                onClick = onClick,
                indication = ripple(bounded = true, radius = 28.dp),
                interactionSource = remember { MutableInteractionSource() }
            )
            .semantics {
                contentDescription = "$label Navigation Tab, ${if (isSelected) "Active" else "Inactive"}"
            }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AuraTeal else NeutralSubtle.copy(alpha = alpha),
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AuraTeal else NeutralSubtle.copy(alpha = alpha),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DashboardTab(
    viewModel: MainViewModel,
    stats: UserStatsEntity?,
    tasks: List<TaskEntity>
) {
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_scroll_container")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            stats?.let {
                AuraHeaderCard(stats = it, completedCount = completedCount, totalCount = tasks.size)
            }
        }

        item {
            FocusCrystalWidget(stats = stats ?: UserStatsEntity())
        }

        item {
            AuraQuickActionRow(
                activeCount = activeTasks.size,
                completedCount = completedCount,
                onPrioritizeClick = {
                    viewModel.setTab("AURA_AI")
                    viewModel.requestAuraPrioritization()
                },
                onVibeCheckClick = {
                    viewModel.setTab("AURA_AI")
                    viewModel.requestAuraVibeCheck()
                }
            )
        }

        item {
            val emergencyTask = activeTasks.firstOrNull { it.priority == "HIGH" }
                ?: activeTasks.firstOrNull()
            
            if (emergencyTask != null) {
                Text(
                    text = "AURA PRIORITY CRITICALS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraPurple,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
                EmergencyTaskCard(task = emergencyTask, onComplete = { viewModel.completeTask(emergencyTask) })
            } else {
                EmptyStateFocusedWidget()
            }
        }
    }
}

@Composable
fun AuraHeaderCard(stats: UserStatsEntity, completedCount: Int, totalCount: Int) {
    val levelTitle = when (stats.level) {
        1 -> "Novice Scholar"
        2 -> "Focus Cadet"
        3 -> "Productivity Ranger"
        4 -> "Zen Alchemist"
        5 -> "Flow Sorcerer"
        else -> "Aether Grandmaster"
    }
    
    val contrastPurple = Color(0xFF381E72)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("header_stats_card"),
        colors = CardDefaults.cardColors(containerColor = AuraTeal),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, contrastPurple.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "YOUR STABILIZED AURA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contrastPurple.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Level ${stats.level}: $levelTitle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = contrastPurple
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(contrastPurple.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .semantics { contentDescription = "Active streak of ${stats.streak} days" }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = contrastPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stats.streak}D STREAK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = contrastPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val xpProgress = stats.xp.toFloat() / 500f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AURA RESONANCE",
                    fontSize = 10.sp,
                    color = contrastPurple.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stats.xp} / 500 XP",
                    fontSize = 11.sp,
                    color = contrastPurple,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { xpProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .semantics { contentDescription = "Level progress: ${stats.xp} out of 500 XP" },
                color = contrastPurple,
                trackColor = contrastPurple.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "AURUM ENERGY", fontSize = 10.sp, color = contrastPurple.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = contrastPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${stats.gold} G", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = contrastPurple)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "GRID CLEARANCE", fontSize = 10.sp, color = contrastPurple.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Text(
                        text = "$completedCount/$totalCount Completed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = contrastPurple
                    )
                }
            }
        }
    }
}

@Composable
fun FocusCrystalWidget(stats: UserStatsEntity) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crystal_pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crystal_focus_widget"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOUR SPIRIT FOCUS CRYSTAL",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralSubtle,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .semantics { contentDescription = "An adaptive interactive focus crystal reflecting your achievements" }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val crystalW = 45f * stats.crystalTier * pulseScale
                val crystalH = 75f * stats.crystalTier * pulseScale

                val pedestalColor = when (stats.pedestalType) {
                    1 -> Color(0xFF2C2C3E)
                    2 -> Color(0xFF0F0F14)
                    else -> AuraGlowGold
                }
                
                if (stats.pedestalType >= 1) {
                    drawRect(
                        color = pedestalColor,
                        topLeft = androidx.compose.ui.geometry.Offset(cx - 60f, cy + 65f),
                        size = androidx.compose.ui.geometry.Size(120f, 15f)
                    )
                }
                if (stats.pedestalType >= 2) {
                    drawRect(
                        color = pedestalColor.copy(alpha = 0.8f),
                        topLeft = androidx.compose.ui.geometry.Offset(cx - 30f, cy + 40f),
                        size = androidx.compose.ui.geometry.Size(60f, 25f)
                    )
                }

                val maxGlowR = 25f * stats.crystalGlow
                val centerOffset = androidx.compose.ui.geometry.Offset(cx, cy)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AuraTeal.copy(alpha = 0.15f * stats.crystalGlow),
                            Color.Transparent
                        ),
                        center = centerOffset
                    ),
                    radius = maxGlowR + 40f,
                    center = centerOffset
                )

                val p = Path()
                p.moveTo(cx, cy - crystalH)
                p.lineTo(cx + crystalW, cy)
                p.lineTo(cx, cy + crystalH)
                p.lineTo(cx - crystalW, cy)
                p.close()

                val coreColors = when (stats.crystalTier) {
                    1 -> listOf(Color(0xFFE2E2FF), AuraTeal)
                    2 -> listOf(AuraPurple, Color(0xFF5E11A5))
                    3 -> listOf(Color(0xFF00E676), Color(0xFF1B5E20))
                    else -> listOf(AuraGlowGold, AuraPurple)
                }

                drawPath(
                    path = p,
                    brush = Brush.verticalGradient(
                        colors = coreColors,
                        startY = cy - crystalH,
                        endY = cy + crystalH
                    )
                )

                val highlightPath = Path()
                highlightPath.moveTo(cx, cy - crystalH)
                highlightPath.lineTo(cx, cy + crystalH)
                drawPath(highlightPath, Color.White.copy(alpha = 0.35f), style = Stroke(width = 3f))

                val crossPath = Path()
                crossPath.moveTo(cx - crystalW, cy)
                crossPath.lineTo(cx + crystalW, cy)
                drawPath(crossPath, Color.White.copy(alpha = 0.2f), style = Stroke(width = 1.5f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            val coreStr = when (stats.crystalTier) {
                1 -> "Quartz Core (Tier 1)"
                2 -> "Amethyst Catalyst (Tier 2)"
                3 -> "Emerald Focus Core (Tier 3)"
                else -> "Radiant Supernova (Tier 4)"
            }
            val glowStr = "Glow Intensity " + "★".repeat(stats.crystalGlow) + "☆".repeat(5 - stats.crystalGlow)

            Text(
                text = coreStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTeal
            )
            Text(
                text = glowStr,
                fontSize = 11.sp,
                color = NeutralSubtle
            )
        }
    }
}

@Composable
fun AuraQuickActionRow(
    activeCount: Int,
    completedCount: Int,
    onPrioritizeClick: () -> Unit,
    onVibeCheckClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPrioritizeClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("dashboard_btn_flow"),
            colors = ButtonDefaults.buttonColors(containerColor = AuraTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = DarkSurface, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PRIORITIZE FLOW", color = DarkSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Button(
            onClick = onVibeCheckClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("dashboard_btn_vibe"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B30)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AuraPurple)
        ) {
            Icon(Icons.Default.Face, null, tint = AuraPurple, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("VIBE CHECK AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun EmergencyTaskCard(task: TaskEntity, onComplete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emergency_task_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PriorityHigh)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = NeutralSubtle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = task.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraPurple,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF231433))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    if (task.isRecurring) {
                        Text(
                            text = "✍ RECURRING (${task.recurrenceInterval})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTeal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F262B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onComplete,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AuraTeal)
            ) {
                Icon(Icons.Default.Check, "Mark emergency task complete", tint = DarkSurface, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun EmptyStateFocusedWidget() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Warning, null, tint = AuraTeal.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Perfect Sanctuary Stabilized",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Your priority queue is clear. Generate or complete another goal into grid!",
                fontSize = 11.sp,
                color = NeutralSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun TasksTab(
    viewModel: MainViewModel,
    tasks: List<TaskEntity>
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var difficulty by remember { mutableStateOf("NORMAL") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceInterval by remember { mutableStateOf("DAILY") }

    var filterCategory by remember { mutableStateOf("ALL") }
    var showForm by remember { mutableStateOf(false) }

    val categories = listOf("Work", "Personal", "Health", "Study", "Fitness")
    val filteredTasks = if (filterCategory == "ALL") tasks else tasks.filter { it.category == filterCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR FOCUS MATRIX",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTeal,
                letterSpacing = 1.2.sp
            )
            
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) Color(0xFFE53935) else AuraTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp).testTag("btn_toggle_form")
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (showForm) Color.White else DarkSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showForm) "CLOSE" else "NEW TASK",
                    color = if (showForm) Color.White else DarkSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = showForm,
            enter = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ADD COMPACT TASK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AuraTeal)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title", color = NeutralSubtle) },
                        modifier = Modifier.fillMaxWidth().testTag("add_title_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraTeal,
                            unfocusedBorderColor = Color(0xFF1B1B34),
                            focusedLabelColor = AuraTeal,
                            unfocusedLabelColor = NeutralSubtle
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Task description", color = NeutralSubtle) },
                        modifier = Modifier.fillMaxWidth().testTag("add_desc_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraPurple,
                            unfocusedBorderColor = Color(0xFF1B1B34),
                            focusedLabelColor = AuraPurple,
                            unfocusedLabelColor = NeutralSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CATEGORY", fontSize = 9.sp, color = NeutralSubtle, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1B1B30))
                                        .clickable {
                                            val currentIdx = categories.indexOf(category)
                                            category = categories[(currentIdx + 1) % categories.size]
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category, fontSize = 12.sp, color = Color.White)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = AuraTeal)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("DIFFICULTY", fontSize = 9.sp, color = NeutralSubtle, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1B1B30))
                                    .clickable {
                                        difficulty = when(difficulty) {
                                            "EASY" -> "NORMAL"
                                            "NORMAL" -> "HARD"
                                            else -> "EASY"
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(difficulty, fontSize = 12.sp, color = Color.White)
                                Icon(Icons.Default.ArrowDropDown, null, tint = AuraTeal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PRIORITY", fontSize = 9.sp, color = NeutralSubtle, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1B1B30))
                                    .clickable {
                                        priority = when(priority) {
                                            "LOW" -> "MEDIUM"
                                            "MEDIUM" -> "HIGH"
                                            else -> "LOW"
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(priority, fontSize = 12.sp, color = Color.White)
                                Icon(Icons.Default.ArrowDropDown, null, tint = AuraTeal)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("RECURRING", fontSize = 9.sp, color = NeutralSubtle, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1B1B30))
                                    .clickable { isRecurring = !isRecurring }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isRecurring,
                                    onCheckedChange = { isRecurring = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AuraTeal, checkmarkColor = DarkSurface)
                                )
                                Text("Enabled", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    if (isRecurring) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("RECUR INTERVAL", fontSize = 9.sp, color = NeutralSubtle, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1B1B30))
                                .clickable {
                                    recurrenceInterval = when(recurrenceInterval) {
                                        "DAILY" -> "WEEKLY"
                                        "WEEKLY" -> "MONTHLY"
                                        else -> "DAILY"
                                    }
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(recurrenceInterval, fontSize = 12.sp, color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, null, tint = AuraTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.addTask(
                                    title = title,
                                    description = desc,
                                    priority = priority,
                                    dueDate = System.currentTimeMillis(),
                                    isRecurring = isRecurring,
                                    recurrenceInterval = recurrenceInterval,
                                    difficulty = difficulty,
                                    category = category
                                )
                                title = ""
                                desc = ""
                                showForm = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_submit_task"),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraTeal)
                    ) {
                        Text("MATERIALIZE FOCUS GOAL", color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "Work", "Personal", "Health", "Study", "Fitness")
            filters.forEach { term ->
                val isSelected = filterCategory == term
                Text(
                    text = term.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) DarkSurface else Color.White,
                    modifier = Modifier
                        .testTag("filter_$term")
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AuraTeal else Color(0xFF1A1A2A))
                        .clickable { filterCategory = term }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateFocusedWidget()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("tasks_scroller"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskGridCard(
                        task = task,
                        onComplete = { viewModel.completeTask(task) },
                        onUncomplete = { viewModel.uncompleteTask(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onAuraBreakdown = { viewModel.requestTaskBreakdown(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskGridCard(
    task: TaskEntity,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit,
    onDelete: () -> Unit,
    onAuraBreakdown: () -> Unit
) {
    val difficultyColor = when (task.difficulty.uppercase()) {
        "EASY" -> AuraTeal
        "NORMAL" -> AuraPurple
        else -> AuraGlowGold
    }

    val prColor = when (task.priority.uppercase()) {
        "HIGH" -> PriorityHigh
        "MEDIUM" -> PriorityMedium
        else -> PriorityLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(prColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (task.isCompleted) NeutralSubtle else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252).copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                fontSize = 12.sp,
                color = NeutralSubtle,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = task.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTeal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F262B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Text(
                        text = "${task.difficulty} [EXP]",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = difficultyColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(difficultyColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    if (task.isRecurring) {
                        Icon(
                            imageVector = Icons.Default.Refresh, // Core Refresh icon instead of Autorenew
                            contentDescription = "Recurring",
                            tint = AuraTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!task.isCompleted) {
                        IconButton(
                            onClick = onAuraBreakdown,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B1B34))
                                .border(BorderStroke(1.dp, AuraPurple), CircleShape),
                            content = {
                                Icon(Icons.Default.Face, "Break down task with AI", tint = AuraPurple, modifier = Modifier.size(16.dp)) // Core Face icon
                            }
                        )
                    }

                    Button(
                        onClick = {
                            if (task.isCompleted) onUncomplete() else onComplete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (task.isCompleted) Color(0xFF1B1B30) else AuraTeal
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(34.dp).semantics {
                            contentDescription = if (task.isCompleted) "Reopen Task" else "Mark Task Done"
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (task.isCompleted) "REOPEN" else "DONE",
                            color = if (task.isCompleted) NeutralSubtle else DarkSurface,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuraAiTab(
    viewModel: MainViewModel
) {
    val response by viewModel.aiResponse.collectAsState()
    val loading by viewModel.isAiLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "CYBERNETIC COGNITIVE CONSOLE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTeal,
            letterSpacing = 1.3.sp
        )

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, BentoBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (loading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = AuraPurple)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = response,
                            fontSize = 12.sp,
                            color = AuraPurple,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (response.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // Core Info icon
                            contentDescription = null,
                            tint = NeutralSubtle.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aura Matrix Node Idle",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Select a console action below to stream priority alignment forecasts or break down a task from your Grid.",
                            fontSize = 11.sp,
                            color = NeutralSubtle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 4.dp) // Legally chained padding args
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = response,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = NeutralLight,
                                modifier = Modifier.testTag("ai_console_output")
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AURA GRID BEAM TARGETS",
                    fontSize = 10.sp,
                    color = NeutralSubtle,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.requestAuraPrioritization() },
                        enabled = !loading,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("ai_btn_prioritize"),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraTeal)
                    ) {
                        Icon(Icons.Default.Settings, null, tint = DarkSurface, modifier = Modifier.size(18.dp)) // Core Settings icon
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FLOW PATH", color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.requestAuraVibeCheck() },
                        enabled = !loading,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("ai_btn_horoscope"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B34)),
                        border = BorderStroke(1.dp, AuraPurple)
                    ) {
                        Icon(Icons.Default.Star, null, tint = AuraPurple, modifier = Modifier.size(16.dp)) // Core Star icon
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VIBE CHECKS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FocusShopTab(
    viewModel: MainViewModel,
    stats: UserStatsEntity?
) {
    val gold = stats?.gold ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("upgrades_scroller")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AURA GOLD VAULT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraGlowGold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your Focus Balance",
                            fontSize = 14.sp,
                            color = NeutralSubtle
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AuraGlowGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$gold G",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "UPGRADES SCHEMATA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AuraPurple,
                letterSpacing = 1.2.sp
            )
        }

        item {
            stats?.let {
                UpgradeShopCard(
                    title = "AURA CRYSTAL CORE",
                    description = "Change structural core mineral. Higher tiers expand scale metrics. (Max Tier 4)",
                    cost = 150,
                    currentLevel = it.crystalTier,
                    maxLevel = 4,
                    icon = Icons.Default.Star, // Diamond Core replacement
                    onBuy = { viewModel.upgradeCore() },
                    isEnabled = gold >= 150 && it.crystalTier < 4,
                    tag = "buy_crystal_core"
                )
            }
        }

        item {
            stats?.let {
                UpgradeShopCard(
                    title = "RADIANCES EMITTER FIELD",
                    description = "Amplifies ambient radial glowing range. Accelerates mental clarity indicators. (Max level 5)",
                    cost = 80,
                    currentLevel = it.crystalGlow,
                    maxLevel = 5,
                    icon = Icons.Default.Info, // Lightbulb Core replacement
                    onBuy = { viewModel.upgradeGlow() },
                    isEnabled = gold >= 80 && it.crystalGlow < 5,
                    tag = "buy_glow_emitter"
                )
            }
        }

        item {
            stats?.let {
                UpgradeShopCard(
                    title = "CELESTIAL BASE PEDESTAL",
                    description = "Upgrades primary stone support structure to obsidian columns or golden altars. (Max level 3)",
                    cost = 200,
                    currentLevel = it.pedestalType,
                    maxLevel = 3,
                    icon = Icons.Default.Settings, // Settings base pedestal replacement
                    onBuy = { viewModel.upgradePedestal() },
                    isEnabled = gold >= 200 && it.pedestalType < 3,
                    tag = "buy_pedestal_altar"
                )
            }
        }
    }
}

@Composable
fun UpgradeShopCard(
    title: String,
    description: String,
    cost: Int,
    currentLevel: Int,
    maxLevel: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onBuy: () -> Unit,
    isEnabled: Boolean,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1B1B34)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = AuraTeal, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = description, fontSize = 11.sp, color = NeutralSubtle, lineHeight = 15.sp)
                
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..maxLevel) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= currentLevel) AuraTeal else Color(0xFF141424))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Button(
                onClick = onBuy,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuraGlowGold,
                    disabledContainerColor = Color(0xFF1C1B12)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier
                    .width(72.dp)
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (currentLevel >= maxLevel) "MAX" else "$cost G",
                    color = if (isEnabled) DarkSurface else NeutralSubtle,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}
