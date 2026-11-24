package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.components.LanguageTogglePill
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.BuyersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LinesScreen
import com.example.ui.screens.ProductionBalanceScreen
import com.example.ui.screens.ProductionEntryScreen
import com.example.ui.screens.PurchaseOrdersScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StylesScreen
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavyVoid
import com.example.ui.theme.PrimaryGradientBrush
import com.example.ui.theme.SidebarGradientBrush
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AhbRoutes {
    const val DASHBOARD = "dashboard"
    const val BUYERS = "buyers"
    const val STYLES = "styles"
    const val PURCHASE_ORDERS = "purchase_orders"
    const val PRODUCTION = "production"
    const val BALANCE = "balance"
    const val HISTORY = "history"
    const val LINES = "lines"
    const val REPORTS = "reports"
    const val BACKUP = "backup"
}

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AhbPmsApp(
    viewModel: PmsViewModel,
    uiState: PmsUiState
) {
    val strings = LocalStrings.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showSplash by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (showSplash) {
            delay(750L)
            showSplash = false
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val allDestinations = remember(strings) {
        listOf(
            NavDestinationItem(AhbRoutes.DASHBOARD, strings.navDashboard, Icons.Default.Dashboard),
            NavDestinationItem(AhbRoutes.BUYERS, strings.navBuyers, Icons.Default.People),
            NavDestinationItem(AhbRoutes.STYLES, strings.navStyles, Icons.Default.Checkroom),
            NavDestinationItem(AhbRoutes.PURCHASE_ORDERS, strings.navPurchaseOrders, Icons.Default.ReceiptLong),
            NavDestinationItem(AhbRoutes.PRODUCTION, strings.navProduction, Icons.Default.PrecisionManufacturing),
            NavDestinationItem(AhbRoutes.BALANCE, strings.navBalance, Icons.Default.Inventory2),
            NavDestinationItem(AhbRoutes.HISTORY, strings.navHistory, Icons.Default.History),
            NavDestinationItem(AhbRoutes.LINES, strings.navLines, Icons.Default.ViewModule),
            NavDestinationItem(AhbRoutes.REPORTS, strings.navReports, Icons.Default.BarChart),
            NavDestinationItem(AhbRoutes.BACKUP, strings.navBackup, Icons.Default.CloudSync)
        )
    }

    val bottomDestinations = remember(strings) {
        listOf(
            NavDestinationItem(AhbRoutes.DASHBOARD, strings.navDashboard, Icons.Default.Dashboard),
            NavDestinationItem(AhbRoutes.PURCHASE_ORDERS, strings.navPurchaseOrders, Icons.Default.ReceiptLong),
            NavDestinationItem(AhbRoutes.PRODUCTION, strings.navProduction, Icons.Default.PrecisionManufacturing),
            NavDestinationItem(AhbRoutes.BALANCE, strings.navBalanceShort, Icons.Default.Inventory2),
            NavDestinationItem(AhbRoutes.REPORTS, strings.navReports, Icons.Default.BarChart)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AhbRoutes.DASHBOARD
    val currentTitle = allDestinations.firstOrNull { it.route == currentRoute }?.label ?: strings.appName

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(NavyBackground)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 840.dp

            if (isTablet) {
                Row(modifier = Modifier.fillMaxSize()) {
                    PersistentSidebar(
                        strings = strings,
                        destinations = allDestinations,
                        currentRoute = currentRoute,
                        onSelectRoute = { navigateTopLevel(it) },
                        modifier = Modifier
                            .width(264.dp)
                            .fillMaxHeight()
                    )
                    AppScaffold(
                        title = currentTitle,
                        isTablet = true,
                        uiState = uiState,
                        bottomDestinations = bottomDestinations,
                        currentRoute = currentRoute,
                        snackbarHostState = snackbarHostState,
                        drawerOpen = false,
                        onCloseDrawer = {},
                        onOpenDrawer = {},
                        onToggleLanguage = viewModel::toggleLanguage,
                        onSelectRoute = { navigateTopLevel(it) },
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = NavyVoid,
                            drawerContentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(288.dp)
                        ) {
                            DrawerMenuContent(
                                strings = strings,
                                destinations = allDestinations,
                                currentRoute = currentRoute,
                                onSelectRoute = { route ->
                                    scope.launch { drawerState.close() }
                                    navigateTopLevel(route)
                                }
                            )
                        }
                    }
                ) {
                    AppScaffold(
                        title = currentTitle,
                        isTablet = false,
                        uiState = uiState,
                        bottomDestinations = bottomDestinations,
                        currentRoute = currentRoute,
                        snackbarHostState = snackbarHostState,
                        drawerOpen = drawerState.isOpen,
                        onCloseDrawer = { scope.launch { drawerState.close() } },
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onToggleLanguage = viewModel::toggleLanguage,
                        onSelectRoute = { navigateTopLevel(it) },
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }

        // Animated Branded Splash Overlay
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(animationSpec = tween(durationMillis = 380))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SidebarGradientBrush),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PrimaryGradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = strings.appName,
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = strings.appSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkyBlue
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    title: String,
    isTablet: Boolean,
    uiState: PmsUiState,
    bottomDestinations: List<NavDestinationItem>,
    currentRoute: String,
    snackbarHostState: SnackbarHostState,
    drawerOpen: Boolean,
    onCloseDrawer: () -> Unit,
    onOpenDrawer: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSelectRoute: (String) -> Unit,
    navController: NavHostController,
    viewModel: PmsViewModel
) {
    val strings = LocalStrings.current

    Scaffold(
        containerColor = NavyBackground,
        contentWindowInsets = if (isTablet) {
            WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
        } else {
            WindowInsets.safeDrawing
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NavySurfaceElevated,
                    contentColor = Color.White,
                    actionColor = SkyBlue,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (!isTablet) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = strings.menu,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    LanguageTogglePill(
                        language = uiState.language,
                        onToggle = onToggleLanguage,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavySurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    containerColor = NavySurface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 4.dp
                ) {
                    bottomDestinations.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onSelectRoute(dest.route) },
                            icon = {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.label
                                )
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SkyBlue,
                                indicatorColor = ElectricBlue,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("bottom_nav_${dest.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AhbRoutes.DASHBOARD,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AhbRoutes.DASHBOARD) {
                    DashboardScreen(
                        uiState = uiState,
                        onSelectMonth = viewModel::selectDashboardMonth,
                        onNavigateBuyers = { onSelectRoute(AhbRoutes.BUYERS) },
                        onNavigateStyles = { onSelectRoute(AhbRoutes.STYLES) },
                        onNavigatePOs = { onSelectRoute(AhbRoutes.PURCHASE_ORDERS) },
                        onNavigateProduction = {
                            viewModel.startEditingProductionEntry(null)
                            onSelectRoute(AhbRoutes.PRODUCTION)
                        },
                        onNavigateBalance = { onSelectRoute(AhbRoutes.BALANCE) },
                        onNavigateLines = { onSelectRoute(AhbRoutes.LINES) },
                        onNavigateHistory = { onSelectRoute(AhbRoutes.HISTORY) }
                    )
                }

                composable(AhbRoutes.BUYERS) {
                    BuyersScreen(
                        uiState = uiState,
                        onSaveBuyer = viewModel::saveBuyer,
                        onDeleteBuyer = viewModel::deleteBuyer
                    )
                }

                composable(AhbRoutes.STYLES) {
                    StylesScreen(
                        uiState = uiState,
                        onSaveStyle = viewModel::saveStyle,
                        onDeleteStyle = viewModel::deleteStyle
                    )
                }

                composable(AhbRoutes.PURCHASE_ORDERS) {
                    PurchaseOrdersScreen(
                        uiState = uiState,
                        onSavePO = viewModel::savePurchaseOrder,
                        onDeletePO = viewModel::deletePurchaseOrder
                    )
                }

                composable(AhbRoutes.PRODUCTION) {
                    ProductionEntryScreen(
                        uiState = uiState,
                        calculatePreviousProduction = viewModel::calculatePreviousProduction,
                        calculateTotalProducedOnPo = viewModel::calculateTotalProducedOnPo,
                        onSaveProduction = viewModel::saveProductionEntry,
                        onCancelEdit = { viewModel.startEditingProductionEntry(null) },
                        onConsumePreselectedPo = viewModel::consumePreselectedPo,
                        onNavigatePOs = { onSelectRoute(AhbRoutes.PURCHASE_ORDERS) },
                        onNavigateHistory = { onSelectRoute(AhbRoutes.HISTORY) },
                        onNavigateBalance = { onSelectRoute(AhbRoutes.BALANCE) }
                    )
                }

                composable(AhbRoutes.BALANCE) {
                    ProductionBalanceScreen(
                        uiState = uiState,
                        onAddProductionForPo = { poId ->
                            viewModel.selectPoForProduction(poId)
                            onSelectRoute(AhbRoutes.PRODUCTION)
                        },
                        onNavigatePOs = { onSelectRoute(AhbRoutes.PURCHASE_ORDERS) },
                        onNotify = viewModel::notifyMessage
                    )
                }

                composable(AhbRoutes.HISTORY) {
                    HistoryScreen(
                        uiState = uiState,
                        onEditEntry = { entryId ->
                            viewModel.startEditingProductionEntry(entryId)
                            navController.navigate(AhbRoutes.PRODUCTION) {
                                launchSingleTop = true
                            }
                        },
                        onDeleteEntry = viewModel::deleteProductionEntry
                    )
                }

                composable(AhbRoutes.LINES) {
                    LinesScreen(
                        uiState = uiState,
                        onSaveLine = viewModel::saveLine,
                        onDeleteLine = viewModel::deleteLine
                    )
                }

                composable(AhbRoutes.REPORTS) {
                    ReportsScreen(
                        uiState = uiState,
                        buildReportRows = viewModel::buildReportRows,
                        onNotify = viewModel::notifyMessage
                    )
                }

                composable(AhbRoutes.BACKUP) {
                    BackupScreen(
                        uiState = uiState,
                        onBuildBackupJson = viewModel::buildBackupJson,
                        onRestoreBackupJson = viewModel::restoreBackupJson,
                        onLoadSampleData = viewModel::loadSampleFactoryData,
                        onClearAllData = viewModel::clearAllData,
                        onNotify = viewModel::notifyMessage
                    )
                }
            }

            BackHandler(enabled = !isTablet && drawerOpen) {
                onCloseDrawer()
            }
        }
    }
}

@Composable
private fun PersistentSidebar(
    strings: AppStrings,
    destinations: List<NavDestinationItem>,
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyVoid,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SidebarGradientBrush)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start))
        ) {
            DrawerMenuContent(
                strings = strings,
                destinations = destinations,
                currentRoute = currentRoute,
                onSelectRoute = onSelectRoute
            )
        }
    }
}

@Composable
private fun DrawerMenuContent(
    strings: AppStrings,
    destinations: List<NavDestinationItem>,
    currentRoute: String,
    onSelectRoute: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = 12.dp, vertical = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryGradientBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Factory,
                    contentDescription = strings.appName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = strings.appSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider(
            color = NavyBorder,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationDrawerItem(
                selected = selected,
                onClick = { onSelectRoute(dest.route) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = ElectricBlue.copy(alpha = 0.24f),
                    selectedIconColor = BrightBlue,
                    selectedTextColor = Color.White,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.testTag("drawer_nav_${dest.route}")
            )
        }
    }
}
