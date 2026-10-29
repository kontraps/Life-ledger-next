package com.lifeledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF03150F)
private val Panel = Color(0xFF0A241B)
private val Green = Color(0xFF19D28B)
private val Mint = Color(0xFF9AE8C5)
private val Muted = Color(0xFF9BB8AB)
private val White = Color(0xFFF3FFF9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LifeLedgerApp() }
    }
}

@Composable fun LifeLedgerApp() {
    var tab by remember { mutableStateOf("Home") }
    var showAdd by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var transactions by remember { mutableStateOf(listOf("Food  ·  Today                 −₱150.00", "Salary  ·  Oct 28             +₱8,000.00", "Transport  ·  Oct 28          −₱80.00")) }
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssetImage("life_ledger_logo.png", Modifier.size(46.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Life Ledger", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("Your progress matters", color = Muted, fontSize = 12.sp) }
                    Text("◉", color = Mint, fontSize = 22.sp)
                }
                when (tab) {
                    "Home" -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item { Text("Good morning,", color = White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold); Text("Small steps build a better you.", color = Muted) }
                        item { Box(Modifier.fillMaxWidth().height(164.dp)) { AssetImage("mountain_landscape.png", Modifier.fillMaxSize()); Column(Modifier.fillMaxSize().background(Panel.copy(alpha = .72f), RoundedCornerShape(22.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("TOTAL BALANCE", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("₱ 24,850.00", color = White, fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("↗  +12% this month", color = Green) } } }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { ActionCard("−", "Add expense", Color(0xFFFF6868), Modifier.weight(1f)) { showAdd = true }; ActionCard("+", "Add income", Green, Modifier.weight(1f)) { showAdd = true }; ActionCard("⇄", "Transfer", Color(0xFF54B8FF), Modifier.weight(1f)) { showAdd = true } } }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Summary("Income", "₱18,500", Modifier.weight(1f)); Summary("Expenses", "₱12,340", Modifier.weight(1f)) } }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Summary("Savings", "₱6,160", Modifier.weight(1f)); Summary("Bills", "₱2,000", Modifier.weight(1f)) } }
                        item { Text("Recent transactions", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                        items(transactions.size) { Text(transactions[it], Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(16.dp), color = White, fontSize = 13.sp) }
                        item { Text("Savings goals", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Goal("New phone", "₱8,500 / ₱25,000", .34f); Goal("Travel to Japan", "₱15,000 / ₱50,000", .30f); Goal("Emergency fund", "₱10,000 / ₱30,000", .33f) }
                    }
                    "Transactions" -> Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Transactions", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold); transactions.forEach { Text(it, Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(16.dp), color = White) } }
                    "Reports" -> Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Insights & Reports", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold); Summary("Total income", "₱18,500", Modifier.fillMaxWidth()); Summary("Total expenses", "₱12,340", Modifier.fillMaxWidth()); Text("Monthly spending", color = Mint); Row(Modifier.fillMaxWidth().height(150.dp).background(Panel, RoundedCornerShape(18.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) { listOf(.35f, .48f, .70f, .55f, .90f, .62f, .78f).forEach { h -> Box(Modifier.width(22.dp).fillMaxHeight(h).background(Green, RoundedCornerShape(6.dp))) } } }
                    else -> Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("Savings goals", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold); Goal("New phone", "₱8,500 / ₱25,000", .34f); Goal("Travel to Japan", "₱15,000 / ₱50,000", .30f); Goal("Emergency fund", "₱10,000 / ₱30,000", .33f); Goal("PC build", "₱12,000 / ₱40,000", .30f); Text("Bills & reminders", color = White, fontSize = 18.sp); Summary("Internet bill · Due Oct 30", "₱1,500", Modifier.fillMaxWidth()); Summary("Rent · Due Nov 10", "₱3,000", Modifier.fillMaxWidth()) }
                }
                Row(Modifier.fillMaxWidth().background(Color(0xFF061B13)).padding(10.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    listOf("Home", "Transactions", "+", "Reports", "Goals").forEach { item ->
                        if (item == "+") Box(Modifier.size(52.dp).background(Green, CircleShape).clickable { showAdd = true }, contentAlignment = Alignment.Center) { Text("+", color = Bg, fontSize = 28.sp) }
                        else Column(Modifier.clickable { tab = item }.padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(item, color = if (tab == item) Green else Muted, fontSize = 11.sp); if (tab == item) Box(Modifier.padding(top = 3.dp).size(4.dp).background(Green, CircleShape)) }
                    }
                }
            }
            if (showAdd) AlertDialog(onDismissRequest = { showAdd = false }, containerColor = Panel, title = { Text("Add transaction", color = White) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount in pesos") }, singleLine = true); OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Category or note") }, singleLine = true) } }, confirmButton = { TextButton(onClick = { val n = amount.toDoubleOrNull(); if (n != null && n > 0) { transactions = listOf((note.ifBlank { "New transaction" }) + "  ·  Just now                 ₱" + amount) + transactions; amount = ""; note = ""; showAdd = false } }) { Text("Save", color = Green) } }, dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel", color = Muted) } })
        }
    }
}

@Composable private fun AssetImage(name: String, modifier: Modifier = Modifier) {\n    val context = LocalContext.current\n    val image = remember(name) { runCatching { context.assets.open(name).use { BitmapFactory.decodeStream(it).asImageBitmap() } }.getOrNull() }\n    if (image != null) Image(bitmap = image, contentDescription = name.substringBefore("."), modifier = modifier)\n    else Box(modifier.background(Color(0xFF0E3A29), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("LL", color = Green, fontWeight = FontWeight.Bold) }\n}\n\n@Composable private fun ActionCard(symbol: String, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) { Column(modifier.background(Panel, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(38.dp).background(tint, CircleShape), contentAlignment = Alignment.Center) { Text(symbol, color = Bg, fontSize = 23.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(8.dp)); Text(label, color = White, fontSize = 10.sp) } }
@Composable private fun Summary(title: String, value: String, modifier: Modifier) { Column(modifier.background(Panel, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(title, color = Muted, fontSize = 12.sp); Text(value, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun Goal(title: String, value: String, progress: Float) { Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, color = White, fontWeight = FontWeight.SemiBold); Text(value, color = Muted, fontSize = 12.sp); LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Green, trackColor = Color(0xFF173D2D)) } }
