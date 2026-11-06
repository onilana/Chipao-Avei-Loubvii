package com.pnba.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pnba.data.model.Hymn
import com.pnba.data.model.HymnCategory
import com.pnba.data.repository.HymnCatalog
import com.pnba.ui.HymnalViewModel
import com.pnba.ui.IndexViewMode
import com.pnba.ui.components.HymnListItem
import com.pnba.ui.components.StickyScrubber
import kotlinx.coroutines.launch

@Composable
fun IndexScreen(
    viewModel: HymnalViewModel,
    modifier: Modifier = Modifier
) {
    val indexMode by viewModel.indexMode.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    val allHymns = viewModel.getAllHymns()
    val filteredHymns = remember(allHymns, selectedCategory) {
        if (selectedCategory == "All" || selectedCategory == "All Categories") allHymns
        else allHymns.filter { it.belongsToCategory(selectedCategory) }
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // Distinct starting letters for the alphabetical scrubber
    val letters = remember(filteredHymns) {
        filteredHymns
            .map { it.title.firstOrNull()?.uppercaseChar() ?: 'A' }
            .distinct()
            .sorted()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("index_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isLandscape) 16.dp else 20.dp,
                    vertical = if (isLandscape) 4.dp else 12.dp
                )
        ) {
            if (isLandscape) {
                // In landscape: integrate Title & TabRow on a single compact line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hymnal Index",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TabRow(
                        selectedTabIndex = when (indexMode) {
                            IndexViewMode.GRID -> 0
                            IndexViewMode.PAGE -> 1
                            IndexViewMode.TOPICAL -> 2
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(360.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = indexMode == IndexViewMode.GRID,
                            onClick = { viewModel.setIndexMode(IndexViewMode.GRID) },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("tab_grid_index"),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Grid (1-520)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                        Tab(
                            selected = indexMode == IndexViewMode.PAGE,
                            onClick = { viewModel.setIndexMode(IndexViewMode.PAGE) },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("tab_page_index"),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Page (A-Z)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                        Tab(
                            selected = indexMode == IndexViewMode.TOPICAL,
                            onClick = { viewModel.setIndexMode(IndexViewMode.TOPICAL) },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("tab_topical_index"),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Topics (1-30)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = "Hymnal Index",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Browse by numerical grid, alphabetical index, or 30 topical themes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // View Mode Tab Switcher: Grid Index vs Page Index vs Topical Index
                TabRow(
                    selectedTabIndex = when (indexMode) {
                        IndexViewMode.GRID -> 0
                        IndexViewMode.PAGE -> 1
                        IndexViewMode.TOPICAL -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = indexMode == IndexViewMode.GRID,
                        onClick = { viewModel.setIndexMode(IndexViewMode.GRID) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Grid (1-520)")
                            }
                        },
                        modifier = Modifier.testTag("tab_grid_index")
                    )
                    Tab(
                        selected = indexMode == IndexViewMode.PAGE,
                        onClick = { viewModel.setIndexMode(IndexViewMode.PAGE) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Page (A-Z)")
                            }
                        },
                        modifier = Modifier.testTag("tab_page_index")
                    )
                    Tab(
                        selected = indexMode == IndexViewMode.TOPICAL,
                        onClick = { viewModel.setIndexMode(IndexViewMode.TOPICAL) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Topics (1-30)")
                            }
                        },
                        modifier = Modifier.testTag("tab_topical_index")
                    )
                }
            }

            // Thematic Filter Chips (shown when in Grid or Page mode)
            if (indexMode != IndexViewMode.TOPICAL) {
                Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf("All") + HymnCategory.topics().map { "${it.number}. ${it.displayName}" }
                    items(categories) { categoryLabel ->
                        val isSelected = if (categoryLabel == "All") {
                            selectedCategory == "All" || selectedCategory == "All Categories"
                        } else {
                            val plainName = categoryLabel.replace(Regex("^\\d+\\.\\s*"), "")
                            selectedCategory.equals(categoryLabel, ignoreCase = true) || selectedCategory.equals(plainName, ignoreCase = true)
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (categoryLabel == "All") {
                                    viewModel.setCategoryFilter("All")
                                } else {
                                    val plainName = categoryLabel.replace(Regex("^\\d+\\.\\s*"), "")
                                    if (isSelected) viewModel.setCategoryFilter("All")
                                    else viewModel.setCategoryFilter(plainName)
                                }
                            },
                            label = {
                                Text(
                                    text = if (categoryLabel == "All") "All Categories" else categoryLabel,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .then(if (isLandscape) Modifier.height(28.dp) else Modifier)
                                .testTag("filter_chip_$categoryLabel")
                        )
                    }
                }
            }
        }

        // Content Area: Grid Index OR Page Index with Sticky Scrubber
        if (indexMode == IndexViewMode.GRID) {
            // --- GRID INDEX ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Quick range jump row (1-50, 51-100, 101-150, 151-200, 201-250)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isLandscape) 4.dp else 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Text(
                            text = "RANGE:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val ranges = listOf(
                        1 to "1–50",
                        51 to "51–100",
                        101 to "101–150",
                        151 to "151–200",
                        201 to "201–250",
                        251 to "251–300",
                        301 to "301–350",
                        351 to "351–400",
                        401 to "401–450",
                        451 to "451–500",
                        501 to "501–520"
                    )
                    items(ranges) { (targetId, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val index = filteredHymns.indexOfFirst { it.id >= targetId }
                                    if (index != -1) {
                                        coroutineScope.launch { gridState.animateScrollToItem(index) }
                                    }
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(
                                    horizontal = if (isLandscape) 8.dp else 10.dp,
                                    vertical = if (isLandscape) 3.dp else 6.dp
                                )
                            )
                        }
                    }
                }

                // Dense, Clean Number Grid for Rapid Navigation
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isLandscape) 9 else 5),
                    state = gridState,
                    contentPadding = PaddingValues(bottom = if (isLandscape) 24.dp else 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 10.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("hymn_number_grid")
                ) {
                    items(filteredHymns) { hymn ->
                        val isFavorite = favoriteIds.contains(hymn.id)
                        Surface(
                            modifier = Modifier
                                .size(if (isLandscape) 42.dp else 58.dp)
                                .clip(RoundedCornerShape(if (isLandscape) 10.dp else 14.dp))
                                .clickable { viewModel.openHymn(hymn.id) }
                                .testTag("grid_hymn_btn_${hymn.id}"),
                            shape = RoundedCornerShape(if (isLandscape) 10.dp else 14.dp),
                            color = if (isFavorite) MaterialTheme.colorScheme.primaryContainer
                                   else MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${hymn.id}",
                                        style = if (isLandscape) {
                                            MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        color = if (isFavorite) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isLandscape) {
                                        Text(
                                            text = hymn.tune.take(4),
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (indexMode == IndexViewMode.PAGE) {
            // --- PAGE INDEX (LIST VIEW WITH STICKY SCRUBBER) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = if (isLandscape) 24.dp else 96.dp, end = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("page_index_list")
                ) {
                    items(filteredHymns, key = { it.id }) { hymn ->
                        HymnListItem(
                            hymn = hymn,
                            isFavorite = favoriteIds.contains(hymn.id),
                            onClick = { viewModel.openHymn(hymn.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(hymn.id) }
                        )
                    }
                }

                // Sticky Alphabetical Scrubber positioned along the right edge
                if (letters.isNotEmpty()) {
                    StickyScrubber(
                        letters = letters,
                        onLetterSelected = { selectedChar ->
                            val targetIndex = filteredHymns.indexOfFirst {
                                it.title.startsWith(selectedChar, ignoreCase = true)
                            }
                            if (targetIndex != -1) {
                                coroutineScope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    )
                }
            }
        } else {
            // --- TOPICAL INDEX (THEMATIC REGISTRY 1-30) ---
            val topics = remember { HymnCategory.topics() }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = if (isLandscape) 4.dp else 8.dp, bottom = if (isLandscape) 24.dp else 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("topical_index_list")
            ) {
                items(topics) { topic ->
                    val hymnCount = remember(allHymns, topic) {
                        allHymns.count { it.belongsToCategory(topic.displayName) }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.setCategoryFilter(topic.displayName)
                                viewModel.setIndexMode(IndexViewMode.PAGE)
                            }
                            .testTag("topic_card_${topic.number}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${topic.number}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = topic.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Hymns: ${topic.hymnRange}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$hymnCount hymns",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
