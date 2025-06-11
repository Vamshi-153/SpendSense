package com.example.spendsense

import android.content.Context
import android.net.Uri
import android.util.Log
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.spendsense.ui.BottomNavItem
import com.example.spendsense.ui.screens.InvestmentsScreen
import com.example.spendsense.ui.screens.ProfileScreen
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import coil.request.Parameters
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.absoluteValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendsense.ui.screens.TransactionsScreen
import com.example.spendsense.ui.screens.TransactionsViewModel

@Composable
fun HomeScreen(navController: NavController, mobileNumber: String) {
    val userData = FileUtils.getUserData(navController.context, mobileNumber)
    val userName = userData?.get("name") ?: "User"
    val profilePictureUri = userData?.get("profilePictureUri")
        ?: "android.resource://com.example.spendsense/drawable/ic_default_profile"

    // Create and share a TransactionsViewModel
    val transactionsViewModel: TransactionsViewModel = viewModel()

    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(bottomNavController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    color = Color(0xFFB39DDB) // Light Purple
                )
        ) {
            NavHostContainer(
                navController = bottomNavController,
                mainNavController = navController,
                mobileNumber = mobileNumber,
                userName = userName,
                profilePictureUri = profilePictureUri,
                transactionsViewModel = transactionsViewModel,
                modifier = Modifier
                    .weight(1f)
                    .background(color = Color(0xFFB39DDB))
            )
        }
    }
}

@Composable
fun WelcomeText(
    userName: String,
    profilePictureUri: String,
    onDataUpdated: (Map<String, Double>) -> Unit,
    transactionsViewModel: TransactionsViewModel // Now accepting the viewModel directly
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Excel File Picker
    val pickExcelLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Toast.makeText(context, "Processing file...", Toast.LENGTH_SHORT).show()
                coroutineScope.launch {
                    // Load transactions into the shared ViewModel
                    transactionsViewModel.loadTransactionsFromExcel(context, uri)

                    // Also process for chart data
                    val newData = processExcelFile(context, uri)
                    onDataUpdated(newData)
                }
            } else {
                Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profilePictureUri.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(profilePictureUri),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "Welcome, $userName!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE),
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = {
                pickExcelLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            },
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF6200EE)) // Purple Button
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Action",
                tint = Color.White
            )
        }
    }
}

// ViewModel for Managing Persistent Data
class MainViewModel : ViewModel() {
    private val _categorizedData = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categorizedData: StateFlow<Map<String, Double>> = _categorizedData

    fun updateData(newData: Map<String, Double>) {
        _categorizedData.value = newData
    }

    suspend fun loadDataFromExcel(context: Context, uri: Uri) {
        val data = processExcelFile(context, uri)
        _categorizedData.value = data
    }
}

// Excel Processing Function
suspend fun processExcelFile(context: Context, uri: Uri): Map<String, Double> {
    return withContext(Dispatchers.IO) {
        try {
            val categorizedData = processTransactionData.process(context, uri)
            categorizedData
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error processing file", Toast.LENGTH_SHORT).show()
            }
            emptyMap()
        }
    }
}

// Composable for Main Home Screen
@Composable
fun MainHomeScreen(
    userName: String,
    navController: NavController,
    profilePictureUri: String,
    viewModel: MainViewModel = viewModel(),
    transactionsViewModel: TransactionsViewModel
) {
    val categorizedData by viewModel.categorizedData.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Pass transactionsViewModel directly
        WelcomeText(
            userName = userName,
            profilePictureUri = profilePictureUri,
            onDataUpdated = { newData ->
                viewModel.updateData(newData)
            },
            transactionsViewModel = transactionsViewModel
        )
        Spacer(modifier = Modifier.height(16.dp))
        InfoText()
        PieChartView(categorizedData)
    }
}

//@Composable
//fun InfoText() {
//    Text(
//        text = "Your Spending Overview",
//        fontSize = 18.sp,
//        fontWeight = FontWeight.Bold,
//        color = Color(0xFF6200EE),
//        modifier = Modifier.padding(vertical = 8.dp)
//    )
//}

// Composable for Pie Chart View
@Composable
fun PieChartView(data: Map<String, Double>) {
    var selectedCategory by remember { mutableStateOf("") }
    var centerText by remember { mutableStateOf("SpendSense") }
    var holeColor by remember { mutableStateOf(Color.Gray) }

    val categoryColors = mapOf(
        "Food" to Color(0xFFE57373),
        "Bills" to Color(0xFF64B5F6),
        "Entertainment" to Color(0xFFBA68C8),
        "Other" to Color(0xFF81C784),
        "Transport" to Color(0xFFFFD54F),
        "Groceries" to Color(0xFFFF8A65),
        "Healthcare" to Color(0xFFA1887F),
        "Shopping" to Color(0xFFB39DDB)
    )

    val colorMap = remember(data.keys) {
        LinkedHashMap<String, Color>().apply {
            data.keys.forEach { category ->
                this[category] = categoryColors[category] ?: Color.Gray
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        setDrawEntryLabels(false)
                        legend.isEnabled = false

                        setDrawHoleEnabled(true)
                        holeRadius = 50f
                        setHoleColor(holeColor.toArgb())
                        setCenterTextColor(Color.White.toArgb())
                        setCenterTextSize(14f)
                        centerText = "SpendSense"

                        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                if (e is PieEntry) {
                                    selectedCategory = e.label
                                    holeColor = colorMap[e.label] ?: Color.Gray
                                    centerText = e.label
                                    invalidate()
                                }
                            }

                            override fun onNothingSelected() {
                                centerText = "SpendSense"
                                holeColor = Color.Gray
                                invalidate()
                            }
                        })
                    }
                },
                update = { pieChart ->
                    val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
                    val dataSet = PieDataSet(entries, "")

                    dataSet.colors = data.keys.map { colorMap[it]?.toArgb() ?: Color.Gray.toArgb() }
                    dataSet.valueTextSize = 12f
                    dataSet.valueTextColor = Color.Black.toArgb()

                    val pieData = PieData(dataSet)
                    pieChart.data = pieData

                    pieChart.setCenterText(centerText)
                    pieChart.setHoleColor(holeColor.toArgb())

                    pieChart.notifyDataSetChanged()
                    pieChart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray, shape = RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(data.keys.toList()) { category ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(colorMap[category] ?: Color.Gray, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = category,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Transactions,
        BottomNavItem.Investments,
        BottomNavItem.Profile
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFB39DDB)) // Light Purple background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 64.dp,
                        topEnd = 64.dp,
                        bottomStart = 64.dp,
                        bottomEnd = 64.dp
                    )
                )
                .background(Color(0xFF6200EE)) // Purple background
        ) {
            NavigationBar(
                containerColor = Color(0xFF6200EE),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 64.dp,
                            topEnd = 64.dp,
                            bottomStart = 64.dp,
                            bottomEnd = 64.dp
                        )
                    )
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(start = 1.dp, top = 20.dp)
            ) {
                Text(
                    text = "SpendSense",
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold,// Force Purple
                    modifier = Modifier.graphicsLayer {
                        alpha = 1f
                    }
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { /* Handle image click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Profile Icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(top = 20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFB39DDB) // Background color remains unchanged
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    mainNavController: NavController,
    mobileNumber: String,
    userName: String,
    profilePictureUri: String,
    transactionsViewModel: TransactionsViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current

    NavHost(navController, startDestination = BottomNavItem.Home.route, modifier = modifier) {
        composable(BottomNavItem.Home.route) {
            MainHomeScreen(
                userName = userName,
                navController = mainNavController,
                profilePictureUri = profilePictureUri,
                transactionsViewModel = transactionsViewModel
            )
        }
        composable(BottomNavItem.Transactions.route) {
            // Use the shared ViewModel
            TransactionsScreen(transactionsViewModel)
        }
        composable(BottomNavItem.Investments.route) {
            // Pass the transactionsViewModel to InvestmentsScreen
            InvestmentsScreen(transactionsViewModel)
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(mainNavController, mobileNumber, profilePictureUri)
        }
    }
}