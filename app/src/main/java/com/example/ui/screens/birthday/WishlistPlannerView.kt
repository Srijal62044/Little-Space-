package com.example.ui.screens.birthday

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class WishlistItem(
    val id: String,
    val name: String,
    val estPrice: String,
    val category: String,
    var isPurchased: Boolean = false
)

data class PartyCheckitem(
    val id: String,
    val title: String,
    var isDone: Boolean = false
)

data class LovedOneBirthday(
    val id: String,
    val name: String,
    val dateText: String,
    val daysLeft: Int,
    val relation: String
)

@Composable
fun WishlistPlannerView(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Wishlist, 1: Party Prep, 2: Birthday Calendar

    val wishlist = remember {
        mutableStateListOf(
            WishlistItem("1", "Wireless Noise-Canceling Headphones", "₹4,999", "Tech", false),
            WishlistItem("2", "Aesthetic Pastel Ceramic Mug Set", "₹1,200", "Home", true),
            WishlistItem("3", "Hardcover Journal & Fine Gel Pens", "₹850", "Stationery", false),
            WishlistItem("4", "Scented Lavender Soy Candle", "₹650", "Decor", false)
        )
    }

    val partyChecklist = remember {
        mutableStateListOf(
            PartyCheckitem("1", "Order Customized Strawberry Cake 🎂", true),
            PartyCheckitem("2", "Hang Pastel Fairy Lights & Balloons 🎈", false),
            PartyCheckitem("3", "Prepare Coziest Party Music Playlist 🎵", true),
            PartyCheckitem("4", "Send Invitation E-Cards to Friends 💌", true),
            PartyCheckitem("5", "Set Up Polaroid Memory Photo Booth 📸", false)
        )
    }

    val lovedOnes = remember {
        mutableStateListOf(
            LovedOneBirthday("1", "Aarav", "Sept 14", 13, "Best Friend"),
            LovedOneBirthday("2", "Mom", "Oct 02", 31, "Family"),
            LovedOneBirthday("3", "Riya", "Nov 19", 79, "Cousin")
        )
    }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }

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
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📋 Gift Wishlist & Birthday Planner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF881337)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Manage your dream gifts, party checklists, & friend birthdays!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFFBE123C),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFFDA4AF), RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "🎁 Wishlist",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (selectedTab == 0) Color(0xFFBE123C) else Color(0xFF64748B)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "🎉 Party Prep",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (selectedTab == 1) Color(0xFFBE123C) else Color(0xFF64748B)
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        "📅 Calendar",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 2) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (selectedTab == 2) Color(0xFFBE123C) else Color(0xFF64748B)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color(0xFFFECDD3), RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Wishlist Tab
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Gift Wishlist Items",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF0F172A)
                                )
                                IconButton(
                                    onClick = { showAddItemDialog = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFE4E6))
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = Color(0xFFBE123C))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(wishlist) { item ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (item.isPurchased) Color(0xFFF1F5F9) else Color(0xFFFFF1F2),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isPurchased) Color(0xFFCBD5E1) else Color(0xFFFECDD3)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Checkbox(
                                                    checked = item.isPurchased,
                                                    onCheckedChange = {
                                                        val idx = wishlist.indexOf(item)
                                                        if (idx >= 0) wishlist[idx] = item.copy(isPurchased = it)
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE11D48))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(
                                                        text = item.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = if (item.isPurchased) Color(0xFF64748B) else Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "${item.category} • ${item.estPrice}",
                                                        fontSize = 11.sp,
                                                        color = if (item.isPurchased) Color(0xFF94A3B8) else Color(0xFF334155),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Party Prep Checklist Tab
                        Column {
                            Text(
                                "Surprise Party Checklist",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(partyChecklist) { check ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (check.isDone) Color(0xFFF1F5F9) else Color(0xFFFFF1F2),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (check.isDone) Color(0xFFCBD5E1) else Color(0xFFFECDD3)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = check.isDone,
                                                onCheckedChange = {
                                                    val idx = partyChecklist.indexOf(check)
                                                    if (idx >= 0) partyChecklist[idx] = check.copy(isDone = it)
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE11D48))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = check.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (check.isDone) Color(0xFF64748B) else Color(0xFF0F172A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Loved Ones Calendar Tab
                        Column {
                            Text(
                                "Upcoming Birthdays Calendar 🎂",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(lovedOnes) { loved ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFF1F2),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🎂", fontSize = 24.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(text = loved.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                                    Text(text = "${loved.relation} • ${loved.dateText}", fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                                                }
                                            }

                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFFE4E6),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                                            ) {
                                                Text(
                                                    text = "In ${loved.daysLeft} Days",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF9F1239),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Wishlist Item Dialog
    if (showAddItemDialog) {
        Dialog(onDismissRequest = { showAddItemDialog = false }) {
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
                    Text(text = "Add Gift Wishlist Item 🎁", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE11D48))

                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Gift Name") },
                        placeholder = { Text("e.g. Wireless Headphones") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newItemPrice,
                        onValueChange = { newItemPrice = it },
                        label = { Text("Estimated Price") },
                        placeholder = { Text("e.g. ₹1,500") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddItemDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newItemName.isNotBlank()) {
                                    wishlist.add(
                                        WishlistItem(
                                            id = System.currentTimeMillis().toString(),
                                            name = newItemName,
                                            estPrice = newItemPrice.ifBlank { "Custom" },
                                            category = "Personal"
                                        )
                                    )
                                    newItemName = ""
                                    newItemPrice = ""
                                    showAddItemDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                        ) {
                            Text("Add Item")
                        }
                    }
                }
            }
        }
    }
}
