package com.example.ui.gallery

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.GalleryCreationEntity
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val creations by viewModel.allGalleryCreations.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var filterFavoritesOnly by remember { mutableStateOf(false) }

    // Upload state
    var selectedPhotoPathsForNote by remember { mutableStateOf<List<String>?>(null) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteCaption by remember { mutableStateOf("") }
    var isSavingPhotoNote by remember { mutableStateOf(false) }

    // Selected detail view
    var selectedCreationForDetail by remember { mutableStateOf<GalleryCreationEntity?>(null) }
    var isEditingNoteInline by remember { mutableStateOf(false) }
    var editedNoteCaption by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(9)
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                val copiedPaths = uris.mapNotNull { uri ->
                    GalleryStorageHelper.copyUriToInternalStorage(context, uri)
                }
                if (copiedPaths.isNotEmpty()) {
                    selectedPhotoPathsForNote = copiedPaths
                    newNoteTitle = ""
                    newNoteCaption = ""
                } else {
                    Toast.makeText(context, "Failed to load selected photos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val filteredCreations = remember(creations, searchQuery, filterFavoritesOnly) {
        creations.filter { creation ->
            val matchesFav = if (filterFavoritesOnly) creation.isFavorite else true
            val matchesQuery = if (searchQuery.isBlank()) true else {
                creation.title.contains(searchQuery, ignoreCase = true) ||
                creation.caption.contains(searchQuery, ignoreCase = true)
            }
            matchesFav && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Photo Gallery 📸",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upload photos & keep small notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { filterFavoritesOnly = !filterFavoritesOnly }
                    ) {
                        Icon(
                            imageVector = if (filterFavoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Filter Favorites",
                            tint = if (filterFavoritesOnly) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null) },
                text = { Text("Upload Photo", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_upload_photo")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search photo notes & memories...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filterFavoritesOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Showing Favorites Only (${filteredCreations.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBE123C)
                    )
                }
            }

            // Photo Cards Grid
            if (filteredCreations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🖼️", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (filterFavoritesOnly) "No favorite photos yet" else if (searchQuery.isNotBlank()) "No matching photos found" else "No photos uploaded yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (filterFavoritesOnly) "Tap the heart on any photo to add it to your favorites." else "Upload your photos and add small notes to save your memories!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Upload Photo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCreations, key = { it.id }) { creation ->
                        PhotoNoteCard(
                            creation = creation,
                            onClick = {
                                selectedCreationForDetail = creation
                                editedNoteCaption = creation.caption
                                isEditingNoteInline = false
                            },
                            onToggleFavorite = {
                                viewModel.toggleGalleryFavorite(creation.id, !creation.isFavorite)
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG 1: Add Small Note to Uploaded Photo ---
    selectedPhotoPathsForNote?.let { paths ->
        Dialog(
            onDismissRequest = {
                if (!isSavingPhotoNote) selectedPhotoPathsForNote = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Note to Photo 📝",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedPhotoPathsForNote = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Photo Thumbnail Preview
                    val firstPhoto = paths.firstOrNull()
                    if (firstPhoto != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = File(firstPhoto),
                                contentDescription = "Photo Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (paths.size > 1) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "+${paths.size - 1} more",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Title Input (Optional)
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Title / Location (Optional)") },
                        placeholder = { Text("e.g., Sunset Beach 🏖️") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Small Note / Caption Input
                    OutlinedTextField(
                        value = newNoteCaption,
                        onValueChange = { newNoteCaption = it },
                        label = { Text("Small Note / Memory") },
                        placeholder = { Text("Write a brief memory, feeling or quote...") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (!isSavingPhotoNote) {
                                isSavingPhotoNote = true
                                val titleText = newNoteTitle.ifBlank {
                                    if (newNoteCaption.isNotBlank()) newNoteCaption.take(20) else "Photo Memory"
                                }
                                val newEntry = GalleryCreationEntity(
                                    title = titleText,
                                    caption = newNoteCaption,
                                    templateId = "simple_photo",
                                    templateName = "Photo & Note",
                                    photoUrisJson = paths.joinToString(","),
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.saveGalleryCreation(newEntry)
                                isSavingPhotoNote = false
                                selectedPhotoPathsForNote = null
                                Toast.makeText(context, "Photo saved to gallery! 📸", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSavingPhotoNote) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Photo Note", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 2: Full-Screen Photo Detail & Edit Note Modal ---
    selectedCreationForDetail?.let { creation ->
        val photoPaths = remember(creation.photoUrisJson) {
            creation.photoUrisJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        val firstPhotoPath = photoPaths.firstOrNull()

        Dialog(
            onDismissRequest = { selectedCreationForDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Bar Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedCreationForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    viewModel.toggleGalleryFavorite(creation.id, !creation.isFavorite)
                                    selectedCreationForDetail = creation.copy(isFavorite = !creation.isFavorite)
                                }
                            ) {
                                Icon(
                                    imageVector = if (creation.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (creation.isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (firstPhotoPath != null) {
                                IconButton(
                                    onClick = {
                                        GalleryStorageHelper.shareImageFile(
                                            context = context,
                                            filePath = firstPhotoPath,
                                            captionText = "${creation.title}\n${creation.caption}"
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                            }

                            IconButton(
                                onClick = { showDeleteConfirmDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Photo Viewer (Scrollable content)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (firstPhotoPath != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 250.dp, max = 400.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = File(firstPhotoPath),
                                    contentDescription = creation.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Title & Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = creation.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            val dateStr = remember(creation.createdAt) {
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(creation.createdAt))
                            }
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Note Section
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Small Note 📝",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (!isEditingNoteInline) {
                                        TextButton(
                                            onClick = {
                                                isEditingNoteInline = true
                                                editedNoteCaption = creation.caption
                                            }
                                        ) {
                                            Text("Edit Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                if (isEditingNoteInline) {
                                    OutlinedTextField(
                                        value = editedNoteCaption,
                                        onValueChange = { editedNoteCaption = it },
                                        minLines = 3,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { isEditingNoteInline = false }) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = {
                                                val updated = creation.copy(caption = editedNoteCaption)
                                                viewModel.saveGalleryCreation(updated)
                                                selectedCreationForDetail = updated
                                                isEditingNoteInline = false
                                                Toast.makeText(context, "Note updated!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                } else {
                                    Text(
                                        text = creation.caption.ifBlank { "No note added to this photo." },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (creation.caption.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog && selectedCreationForDetail != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Photo?") },
            text = { Text("Are you sure you want to remove this photo and its note from your gallery?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedCreationForDetail?.let {
                            viewModel.deleteGalleryCreation(it)
                        }
                        showDeleteConfirmDialog = false
                        selectedCreationForDetail = null
                        Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PhotoNoteCard(
    creation: GalleryCreationEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val photoPaths = remember(creation.photoUrisJson) {
        creation.photoUrisJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val firstPhotoPath = photoPaths.firstOrNull()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            // Photo Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.Black.copy(alpha = 0.05f))
            ) {
                if (firstPhotoPath != null) {
                    AsyncImage(
                        model = File(firstPhotoPath),
                        contentDescription = creation.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🖼️", fontSize = 32.sp)
                    }
                }

                // Favorite Overlay Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (creation.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (creation.isFavorite) Color(0xFFE11D48) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Note & Title Body
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = creation.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (creation.caption.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = creation.caption,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }

                Spacer(Modifier.height(6.dp))

                val dateStr = remember(creation.createdAt) {
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(creation.createdAt))
                }
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
