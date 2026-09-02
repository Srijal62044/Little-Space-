package com.example.ui.screens.birthday

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class ScrapbookPage(
    val photoUri: String? = null,
    val id: String,
    val title: String,
    val message: String,
    val dateTag: String,
    val emoji: String,
    val stickerBadge: String,
    val author: String
)

@Composable
fun ScrapbookBookletView(
    modifier: Modifier = Modifier
) {
    val pages = remember {
        mutableStateListOf(
            ScrapbookPage(
                photoUri = null,
                id = "1",
                title = "Panda Lamp & Fairy Lights 🐼✨",
                message = "Sitting under a canopy of fairy lights & golden balloons, holding a cute glowing panda lamp.",
                dateTag = "Photo 1 • Midnight Glow",
                emoji = "🐼",
                stickerBadge = "✨ Cozy Night",
                author = "Priyanka"
            ),
            ScrapbookPage(
                photoUri = null,
                id = "2",
                title = "Traditional Emerald Grace 🌺",
                message = "Dressed in a gorgeous emerald green embroidered kurti with yellow dupatta & traditional jhumkas.",
                dateTag = "Photo 2 • Festive Vibe",
                emoji = "💚",
                stickerBadge = "👑 Desi Queen",
                author = "Celebration Team"
            ),
            ScrapbookPage(
                photoUri = null,
                id = "3",
                title = "Golden Hour Glow 🌅",
                message = "Sunlit portrait in a pink striped kurti with glasses resting on hair.",
                dateTag = "Photo 3 • Sunset Hour",
                emoji = "✨",
                stickerBadge = "🌸 Golden Hour",
                author = "Priyanka"
            ),
            ScrapbookPage(
                photoUri = null,
                id = "4",
                title = "Green Hearts Selfie 💚",
                message = "Sweet smile adorned with green heart filter & sparkly statement earrings.",
                dateTag = "Photo 4 • Cute Selfie",
                emoji = "💖",
                stickerBadge = "✨ Pure Joy",
                author = "Friends"
            ),
            ScrapbookPage(
                photoUri = null,
                id = "5",
                title = "Golden Balloon Night 🎈",
                message = "Sitting peacefully amongst glowing balloons in a floral dress, celebrating the special night.",
                dateTag = "Photo 5 • Birthday Lights",
                emoji = "🎈",
                stickerBadge = "🎂 Celebration",
                author = "With Love"
            ),
            ScrapbookPage(
                photoUri = null,
                id = "6",
                title = "Red Room Ambient Mood ❤️",
                message = "A warm, dreamy aesthetic selfie bathed in soft red room lighting.",
                dateTag = "Photo 6 • Ambient Vibe",
                emoji = "❤️",
                stickerBadge = "✨ Dreamy",
                author = "Priyanka"
            ),
            ScrapbookPage(
                photoUri = null,
                id = "7",
                title = "Panda Magic Celebration 🐼",
                message = "Playing with the panda lamp surrounded by party snacks & birthday decorations.",
                dateTag = "Photo 7 • Party Room",
                emoji = "🎁",
                stickerBadge = "🎉 Party Time",
                author = "Forever Memories"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    var showAddPageDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newMessage by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("🎂") }
    var newSticker by remember { mutableStateOf("✨ Wish") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📖 Birthday Scrapbook & Wish Booklet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF881337)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Swipe through your birthday flip-book pages & stickers!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showAddPageDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Page", tint = Color(0xFFBE123C))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrapbook Horizontal Pager Card
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            val page = pages[pageIndex]

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .border(3.dp, Color(0xFFFECDD3), RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFF1F2), Color.White)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header Date Tag + Sticker Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFE4E6),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                            ) {
                                Text(
                                    text = page.dateTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9F1239),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFDE68A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.rotate(3f)
                            ) {
                                Text(
                                    text = page.stickerBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF78350F),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Main Polaroid Photo / Content Block
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFFF472B6).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF1F2))
                                ) {
                                    Text(text = page.emoji, fontSize = 42.sp)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = page.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF881337),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = page.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "— ${page.author}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFFBE123C)
                                )
                            }
                        }

                        // Page Footer Indicator
                        Text(
                            text = "Page ${pageIndex + 1} of ${pages.size} • Swipe to turn page 📖",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Page Dot Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFFE11D48) else Color(0xFFFDA4AF))
                )
            }
        }
    }

    // Add Scrapbook Page Dialog
    if (showAddPageDialog) {
        Dialog(onDismissRequest = { showAddPageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Add New Scrapbook Page 📖", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFBE123C))

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Page Title") },
                        placeholder = { Text("e.g. Memory of Joy") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        label = { Text("Wish / Memory Note") },
                        placeholder = { Text("Write your wholesome memory or wish here...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddPageDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    pages.add(
                                        ScrapbookPage(
                                            id = System.currentTimeMillis().toString(),
                                            title = newTitle,
                                            message = newMessage.ifBlank { "A happy birthday moment!" },
                                            dateTag = "Chapter ${pages.size + 1} • Special Wish",
                                            emoji = "🌟",
                                            stickerBadge = "✨ Sparkle",
                                            author = "Priyanka"
                                        )
                                    )
                                    newTitle = ""
                                    newMessage = ""
                                    showAddPageDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C))
                        ) {
                            Text("Add to Scrapbook")
                        }
                    }
                }
            }
        }
    }
}
