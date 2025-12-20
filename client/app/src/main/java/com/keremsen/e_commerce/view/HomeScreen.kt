package com.keremsen.e_commerce.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.keremsen.e_commerce.models.entityModel.Product
import com.keremsen.e_commerce.models.entityModel.Category
import com.keremsen.e_commerce.navigation.Screen
import com.keremsen.e_commerce.utils.Constants
import com.keremsen.e_commerce.viewmodel.CategoryViewModel
import com.keremsen.e_commerce.viewmodel.ProductViewModel
import com.keremsen.e_commerce.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Ana Sayfa") },
                    label = { Text("Ana Sayfa") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.FavoriteBorder, null) },
                    label = { Text("Favoriler") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ShoppingCart, null) },
                    label = { Text("Sepet") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeContent(navController)
                1 -> FavoritesScreen(navController = navController)
                2 -> CartScreen(navController = navController)
                3 -> ProfileScreen(navController = navController, onGoToFavorites = { selectedTab = 1 })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val products by productViewModel.products.collectAsState()
    val categories by categoryViewModel.categories.observeAsState(emptyList())
    val isLoadingProducts by productViewModel.isLoading.collectAsState()
    val isLoadingCategories by categoryViewModel.isLoading.observeAsState(false)
    val userProfile by userViewModel.userProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf("Varsayılan") }

    var rootCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var childCategoriesMap by remember { mutableStateOf<Map<Int, List<Category>>>(emptyMap()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var expandedRootIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var lastFetchedRootId by remember { mutableStateOf<Int?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        categoryViewModel.fetchRootCategories()
        productViewModel.fetchAllProducts()
        userViewModel.loadMyProfile()
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && expandedRootIds.isEmpty()) {
            rootCategories = categories
            childCategoriesMap = emptyMap()
            lastFetchedRootId = null
        } else if (lastFetchedRootId != null && categories.isNotEmpty()) {
            childCategoriesMap = childCategoriesMap + mapOf(lastFetchedRootId!! to categories)
            lastFetchedRootId = null
        }
    }

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = if (selectedCategoryId != null) {
            product.category_id == selectedCategoryId
        } else true
        matchesSearch && matchesCategory
    }.let { list ->
        when (sortOrder) {
            "Ucuzdan Pahalıya" -> list.sortedBy { it.price }
            "Pahalıdan Ucuza" -> list.sortedByDescending { it.price }
            else -> list
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                CategoryDrawerContent(
                    rootCategories = rootCategories,
                    childCategoriesMap = childCategoriesMap,
                    expandedRootIds = expandedRootIds,
                    selectedCategoryId = selectedCategoryId,
                    isLoading = isLoadingCategories,
                    onRootClick = { category ->
                        if (expandedRootIds.contains(category.id)) {
                            expandedRootIds = expandedRootIds - category.id
                        } else {
                            expandedRootIds = expandedRootIds + category.id
                            lastFetchedRootId = category.id
                            categoryViewModel.fetchChildCategories(category.id)
                        }
                    },
                    onChildClick = { category ->
                        selectedCategoryId = category.id
                        scope.launch { drawerState.close() }
                    },
                    onReset = {
                        selectedCategoryId = null
                        expandedRootIds = emptySet()
                        categoryViewModel.fetchRootCategories()
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
            HomeTopBar(userName = userProfile?.fullname ?: "Kullanıcı")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Menu, null, tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        SearchBarSection(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // ⭐ YENİ SIRALA BUTONU
                        SortBottomSheetButton(
                            currentSortOrder = sortOrder,
                            onSortSelected = { sortOrder = it }
                        )
                    }
                }

                if (selectedCategoryId != null) {
                    item(span = { GridItemSpan(2) }) {
                        val selectedCategory = rootCategories.find { it.id == selectedCategoryId }
                            ?: childCategoriesMap.values.flatten().find { it.id == selectedCategoryId }

                        selectedCategory?.let {
                            FilterChip(
                                selected = true,
                                onClick = { selectedCategoryId = null },
                                label = { Text(it.name) },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                if (isLoadingProducts) {
                    item(span = { GridItemSpan(2) }) {
                        Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                            Text("Ürün bulunamadı.", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredProducts) { product ->
                        ProductItem(product = product) {
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheetButton(
    currentSortOrder: String,
    onSortSelected: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // UI Butonu
    Surface(
        onClick = { showSheet = true },
        modifier = Modifier
            .height(52.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Sort, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (currentSortOrder == "Varsayılan") "Sırala" else currentSortOrder,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)) {
                Text(
                    "Sıralama Seçenekleri",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                val options = listOf("Varsayılan", "Ucuzdan Pahalıya", "Pahalıdan Ucuza")
                options.forEach { option ->
                    ListItem(
                        headlineContent = { Text(option) },
                        leadingContent = {
                            RadioButton(
                                selected = (currentSortOrder == option),
                                onClick = null
                            )
                        },
                        modifier = Modifier.clickable {
                            onSortSelected(option)
                            showSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDrawerContent(
    rootCategories: List<Category>,
    childCategoriesMap: Map<Int, List<Category>>,
    expandedRootIds: Set<Int>,
    selectedCategoryId: Int?,
    isLoading: Boolean,
    onRootClick: (Category) -> Unit,
    onChildClick: (Category) -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(24.dp)
        ) {
            Text("Kategoriler", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (selectedCategoryId != null) {
            TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text("Filtreyi Temizle")
            }
        }

        LazyColumn {
            items(rootCategories) { root ->
                ListItem(
                    headlineContent = { Text(root.name, fontWeight = FontWeight.SemiBold) },
                    trailingContent = {
                        Icon(if (expandedRootIds.contains(root.id)) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight, null)
                    },
                    modifier = Modifier.clickable { onRootClick(root) }
                )
                if (expandedRootIds.contains(root.id)) {
                    val children = childCategoriesMap[root.id] ?: emptyList()
                    children.forEach { child ->
                        ListItem(
                            headlineContent = { Text(child.name, fontSize = 14.sp) },
                            modifier = Modifier.padding(start = 24.dp).clickable { onChildClick(child) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(userName: String) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Hoş Geldin,", color = Color.Gray)
        Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchBarSection(query: String, onQueryChange: (String) -> Unit, modifier: Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Ürün ara...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        modifier = modifier.height(52.dp).shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = Constants.getImageUrl(product.images?.firstOrNull()?.url),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text("${product.price} ₺", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}