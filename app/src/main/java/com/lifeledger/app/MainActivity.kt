package com.lifeledger.app

import android.content.Context
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Bg = Color(0xFF0B1110)
private val SurfaceCard = Color(0xFF151E1B)
private val SurfaceRaised = Color(0xFF1C2823)
private val Green = Color(0xFFB8F36B)
private val Mint = Color(0xFF77D9B0)
private val TextMain = Color(0xFFF2F6F1)
private val TextMuted = Color(0xFF9AA9A0)
private val Red = Color(0xFFFF8A80)
private val Blue = Color(0xFF8DC9FF)

data class Tx(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val amount: Double,
    val type: String,
    val date: Long = System.currentTimeMillis()
)

data class GoalItem(val id: String, val name: String, val target: Double, val saved: Double)

private fun money(value: Double): String = "₱" + String.format(Locale.US, "%,.2f", value)
private fun dateLabel(time: Long): String = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(time))

private class LedgerStore(context: Context) {
    private val prefs = context.getSharedPreferences("life_ledger_store", Context.MODE_PRIVATE)
    fun loadTransactions(): List<Tx> {
        val array = runCatching { JSONArray(prefs.getString("transactions", "[]")) }.getOrElse { JSONArray() }
        return (0 until array.length()).mapNotNull { i ->
            runCatching {
                val o = array.getJSONObject(i)
                Tx(o.getString("id"), o.getString("title"), o.getString("category"), o.getDouble("amount"), o.getString("type"), o.getLong("date"))
            }.getOrNull()
        }.sortedByDescending { it.date }
    }
    fun saveTransactions(items: List<Tx>) {
        val array = JSONArray()
        items.forEach { t -> array.put(JSONObject().put("id", t.id).put("title", t.title).put("category", t.category).put("amount", t.amount).put("type", t.type).put("date", t.date)) }
        prefs.edit().putString("transactions", array.toString()).apply()
    }
    fun loadGoals(): List<GoalItem> {
        val array = runCatching { JSONArray(prefs.getString("goals", null) ?: "[]") }.getOrElse { JSONArray() }
        val loaded = (0 until array.length()).mapNotNull { i -> runCatching {
            val o = array.getJSONObject(i)
            GoalItem(o.getString("id"), o.getString("name"), o.getDouble("target"), o.getDouble("saved"))
        }.getOrNull() }
        if (loaded.isNotEmpty()) return loaded
        return listOf(
            GoalItem("phone", "New phone", 25000.0, 0.0),
            GoalItem("travel", "Japan trip", 50000.0, 0.0),
            GoalItem("emergency", "Emergency fund", 30000.0, 0.0)
        )
    }
    fun saveGoals(items: List<GoalItem>) {
        val array = JSONArray()
        items.forEach { g -> array.put(JSONObject().put("id", g.id).put("name", g.name).put("target", g.target).put("saved", g.saved)) }
        prefs.edit().putString("goals", array.toString()).apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(11,17,16)
        window.navigationBarColor = android.graphics.Color.rgb(11,17,16)
        setContent { LifeLedgerApp(LedgerStore(applicationContext)) }
    }
}

@Composable
private fun LifeLedgerApp(store: LedgerStore) {
    var tab by remember { mutableStateOf("Home") }
    var transactions by remember { mutableStateOf(store.loadTransactions()) }
    var goals by remember { mutableStateOf(store.loadGoals()) }
    var showAdd by remember { mutableStateOf(false) }
    var initialType by remember { mutableStateOf("Expense") }
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<GoalItem?>(null) }
    var showAddGoal by remember { mutableStateOf(false) }

    val income = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val expenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
    val balance = income - expenses
    val monthStart = remember { java.util.Calendar.getInstance().apply { set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis }
    val monthIncome = transactions.filter { it.type == "Income" && it.date >= monthStart }.sumOf { it.amount }
    val monthExpenses = transactions.filter { it.type == "Expense" && it.date >= monthStart }.sumOf { it.amount }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = SurfaceCard, primary = Green, onPrimary = Bg,
        onBackground = TextMain, onSurface = TextMain, secondary = Mint
    )) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize()) {
                Header()
                when (tab) {
                    "Home" -> HomeScreen(balance, income, expenses, monthIncome, monthExpenses, transactions, goals,
                        onAddExpense = { initialType = "Expense"; showAdd = true },
                        onAddIncome = { initialType = "Income"; showAdd = true },
                        onSeeAll = { tab = "Activity" },
                        onGoals = { tab = "Goals" })
                    "Activity" -> ActivityScreen(transactions, onDelete = { tx ->
                        transactions = transactions.filterNot { it.id == tx.id }; store.saveTransactions(transactions)
                    }, onAdd = { initialType = "Expense"; showAdd = true })
                    "Budget" -> BudgetScreen(monthIncome, monthExpenses, transactions, onAdd = { initialType = "Expense"; showAdd = true })
                    else -> GoalsScreen(goals, onAddGoal = { showAddGoal = true }, onAddSavings = { selectedGoal = it; showGoalDialog = true })
                }
                BottomBar(tab) { tab = it }
            }
        }
    }

    if (showAdd) AddTransactionDialog(initialType, onDismiss = { showAdd = false }, onSave = { title, category, amount, type ->
        val tx = Tx(title = title, category = category, amount = amount, type = type)
        transactions = (listOf(tx) + transactions).sortedByDescending { it.date }
        store.saveTransactions(transactions)
        showAdd = false
    })
    if (showGoalDialog && selectedGoal != null) AddSavingsDialog(selectedGoal!!, onDismiss = { showGoalDialog = false }, onSave = { amount ->
        goals = goals.map { if (it.id == selectedGoal!!.id) it.copy(saved = it.saved + amount) else it }
        store.saveGoals(goals)
        showGoalDialog = false
    })
    if (showAddGoal) AddGoalDialog(onDismiss = { showAddGoal = false }, onSave = { name, target ->
        goals = goals + GoalItem(UUID.randomUUID().toString(), name, target, 0.0)
        store.saveGoals(goals)
        showAddGoal = false
    })
}

@Composable private fun Header() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).background(Green, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
            Text("L", color = Bg, fontSize = 26.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Life Ledger", color = TextMain, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("Your money, in focus", color = TextMuted, fontSize = 12.sp)
        }
        Box(Modifier.size(40.dp).background(SurfaceRaised, CircleShape), contentAlignment = Alignment.Center) {
            Text("₱", color = Green, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable private fun ColumnScope.HomeScreen(
    balance: Double, income: Double, expenses: Double, monthIncome: Double, monthExpenses: Double, transactions: List<Tx>, goals: List<GoalItem>,
    onAddExpense: () -> Unit, onAddIncome: () -> Unit, onSeeAll: () -> Unit, onGoals: () -> Unit
) {
    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("OVERVIEW", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Your finances", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("A clearer view of your everyday money.", color = TextMuted, fontSize = 13.sp)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(26.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NET CASH FLOW", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.background(Color(0xFF293B2A), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) { Text("ALL TIME", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
                Text(money(balance), color = TextMain, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(if (transactions.isEmpty()) "Add your first transaction to get started." else "Based on your saved transactions", color = TextMuted, fontSize = 12.sp)
                HorizontalDivider(color = SurfaceRaised)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(Green, CircleShape)); Spacer(Modifier.width(7.dp)); Text("Income", color = TextMuted, fontSize = 12.sp) }
                        Spacer(Modifier.height(7.dp)); Text(money(income), color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(Red, CircleShape)); Spacer(Modifier.width(7.dp)); Text("Expenses", color = TextMuted, fontSize = 12.sp) }
                        Spacer(Modifier.height(7.dp)); Text(money(expenses), color = Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction("−", "Expense", Red, Modifier.weight(1f), onAddExpense)
                QuickAction("+", "Income", Green, Modifier.weight(1f), onAddIncome)
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("This month", color = TextMain, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("Income vs. spending", color = TextMuted, fontSize = 12.sp) }
                Text("LIVE TOTALS", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        item { MonthlyChart(monthIncome, monthExpenses) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Recent activity", color = TextMain, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("See all  →", color = Green, fontSize = 12.sp, modifier = Modifier.clickable { onSeeAll() })
            }
        }
        if (transactions.isEmpty()) item { EmptyState("No transactions yet", "Add income or an expense and your activity will appear here.") }
        items(transactions.take(4), key = { it.id }) { TransactionRow(it) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Savings goals", color = TextMain, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("View all  →", color = Green, fontSize = 12.sp, modifier = Modifier.clickable { onGoals() })
            }
        }
        if (goals.isEmpty()) item { EmptyState("Start saving", "Create a goal and track your progress.") }
        items(goals.take(2), key = { it.id }) { GoalCard(it, onAdd = onGoals) }
    }
}

@Composable private fun QuickAction(symbol: String, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.background(SurfaceCard, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(Modifier.size(36.dp).background(tint.copy(alpha = .15f), CircleShape), contentAlignment = Alignment.Center) { Text(symbol, color = tint, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(10.dp)); Text(label, color = TextMain, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun ColumnScope.ActivityScreen(transactions: List<Tx>, onDelete: (Tx) -> Unit, onAdd: () -> Unit) {
    Column(Modifier.weight(1f).padding(horizontal = 20.dp)) {
        Text("Activity", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Every transaction, in one place.", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 18.dp))
        if (transactions.isEmpty()) EmptyState("Nothing here yet", "Your saved transactions will show up here.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(transactions, key = { it.id }) { tx ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { TransactionRow(tx) }
                    Text("×", color = Red, fontSize = 23.sp, modifier = Modifier.clickable { onDelete(tx) }.padding(horizontal = 10.dp))
                }
            }
        }
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Bg)) { Text("＋  Add transaction", fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun ColumnScope.BudgetScreen(income: Double, expenses: Double, transactions: List<Tx>, onAdd: () -> Unit) {
    val categories = transactions.filter { it.type == "Expense" }.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }.toList().sortedByDescending { it.second }
    Column(Modifier.weight(1f).padding(horizontal = 20.dp)) {
        Text("Spending", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Your monthly money snapshot", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniMetric("Income", money(income), Green, Modifier.weight(1f))
            MiniMetric("Spent", money(expenses), Red, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(22.dp)).padding(18.dp)) {
            Text("Income left after spending", color = TextMuted, fontSize = 12.sp)
            Text(money(income - expenses), color = TextMain, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { if (income > 0) (expenses / income).toFloat().coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = if (expenses > income) Red else Green, trackColor = SurfaceRaised)
            Text(if (income > 0) "${String.format(Locale.US, "%.0f", expenses / income * 100)}% of recorded income spent" else "Record income to calculate your spending ratio", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Text("Expense categories", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
        if (categories.isEmpty()) EmptyState("No expenses recorded", "Add an expense to see your category breakdown.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(categories) { (category, amount) ->
                Row(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(14.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).background(Blue, CircleShape)); Spacer(Modifier.width(12.dp))
                    Text(category, color = TextMain, modifier = Modifier.weight(1f)); Text(money(amount), color = TextMain, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Bg)) { Text("＋  Add expense", fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun ColumnScope.GoalsScreen(goals: List<GoalItem>, onAddGoal: () -> Unit, onAddSavings: (GoalItem) -> Unit) {
    Column(Modifier.weight(1f).padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Your goals", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Small steps, real progress.", color = TextMuted, fontSize = 13.sp) }
            Button(onClick = onAddGoal, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Bg), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) { Text("+ Goal") }
        }
        Spacer(Modifier.height(18.dp))
        if (goals.isEmpty()) EmptyState("Make your first goal", "Set a target and start adding savings.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(goals, key = { it.id }) { goal ->
                Column(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(20.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).background(Color(0xFF2A3828), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("◎", color = Green, fontSize = 24.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { Text(goal.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("${money(goal.saved)} saved", color = TextMuted, fontSize = 12.sp) }
                        Text("${(goal.saved / goal.target * 100).toInt().coerceIn(0, 100)}%", color = Green, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { (goal.saved / goal.target).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Green, trackColor = SurfaceRaised)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Target  ${money(goal.target)}", color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onAddSavings(goal) }) { Text("+ Add savings", color = Green) }
                    }
                }
            }
        }
    }
}

@Composable private fun MonthlyChart(income: Double, expenses: Double) {
    val max = maxOf(income, expenses, 1.0)
    Column(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(20.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Income", color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(money(income), color = Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { (income / max).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(10.dp), color = Green, trackColor = SurfaceRaised)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Expenses", color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(money(expenses), color = Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { (expenses / max).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(10.dp), color = Red, trackColor = SurfaceRaised)
    }
}

@Composable private fun TransactionRow(tx: Tx) {
    Row(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).background(if (tx.type == "Income") Color(0xFF263B2A) else Color(0xFF3A2828), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Text(if (tx.type == "Income") "↙" else "↗", color = if (tx.type == "Income") Green else Red, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(tx.title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("${tx.category}  ·  ${dateLabel(tx.date)}", color = TextMuted, fontSize = 11.sp)
        }
        Text((if (tx.type == "Income") "+" else "−") + money(tx.amount), color = if (tx.type == "Income") Green else Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable private fun MiniMetric(title: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier.background(SurfaceCard, RoundedCornerShape(18.dp)).padding(16.dp)) {
        Text(title, color = TextMuted, fontSize = 12.sp); Spacer(Modifier.height(7.dp)); Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable private fun GoalCard(goal: GoalItem, onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(18.dp)).clickable { onAdd() }.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text(goal.name, color = TextMain, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text("${(goal.saved / goal.target * 100).toInt().coerceIn(0, 100)}%", color = Green, fontSize = 12.sp) }
        LinearProgressIndicator(progress = { (goal.saved / goal.target).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Green, trackColor = SurfaceRaised)
        Text("${money(goal.saved)} of ${money(goal.target)}", color = TextMuted, fontSize = 12.sp)
    }
}

@Composable private fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().background(SurfaceCard, RoundedCornerShape(18.dp)).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("◌", color = Green, fontSize = 30.sp)
        Text(title, color = TextMain, fontWeight = FontWeight.Bold)
        Text(subtitle, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable private fun BottomBar(tab: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().background(SurfaceCard).padding(horizontal = 8.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        listOf("Home", "Activity", "Budget", "Goals").forEach { item ->
            Column(Modifier.weight(1f).clickable { onSelect(item) }.padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(when (item) { "Home" -> "⌂"; "Activity" -> "↕"; "Budget" -> "▥"; else -> "◎" }, color = if (tab == item) Green else TextMuted, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(item, color = if (tab == item) Green else TextMuted, fontSize = 10.sp, fontWeight = if (tab == item) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable private fun AddTransactionDialog(initialType: String, onDismiss: () -> Unit, onSave: (String, String, Double, String) -> Unit) {
    var type by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (initialType == "Income") "Salary" else "Food") }
    var amountText by remember { mutableStateOf("") }
    val categories = if (type == "Income") listOf("Salary", "Tips", "Business", "Gift", "Other") else listOf("Food", "Transport", "Bills", "Shopping", "Health", "Rent", "Other")
    AlertDialog(onDismissRequest = onDismiss, containerColor = SurfaceCard, title = { Text("New transaction", color = TextMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth().background(Bg, RoundedCornerShape(12.dp)).padding(4.dp)) {
                    listOf("Expense", "Income").forEach { option ->
                        Box(Modifier.weight(1f).background(if (type == option) SurfaceRaised else Bg, RoundedCornerShape(9.dp)).clickable { type = option; category = if (option == "Income") "Salary" else "Food" }.padding(10.dp), contentAlignment = Alignment.Center) {
                            Text(option, color = if (type == option) Green else TextMuted, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12) }, label = { Text("Amount (PHP)") }, prefix = { Text("₱") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Description") }, placeholder = { Text(if (type == "Income") "e.g. Monthly salary" else "e.g. Lunch") }, singleLine = true)
                Text("CATEGORY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { c ->
                                Box(Modifier.weight(1f).background(if (category == c) Color(0xFF2B3D28) else SurfaceRaised, RoundedCornerShape(10.dp)).clickable { category = c }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(c, color = if (category == c) Green else TextMuted, fontSize = 11.sp)
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { val amount = amountText.toDoubleOrNull(); if (amount != null && amount > 0) onSave(title.ifBlank { category }, category, amount, type) }) { Text("Save transaction", color = Green, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } })
}

@Composable private fun AddSavingsDialog(goal: GoalItem, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amountText by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = SurfaceCard, title = { Text("Add to ${goal.name}", color = TextMain) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saved ${money(goal.saved)} of ${money(goal.target)}", color = TextMuted, fontSize = 12.sp)
            OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12) }, label = { Text("Savings amount") }, prefix = { Text("₱") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        } },
        confirmButton = { TextButton(onClick = { val n = amountText.toDoubleOrNull(); if (n != null && n > 0) onSave(n) }) { Text("Add savings", color = Green) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } })
}

@Composable private fun AddGoalDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = SurfaceCard, title = { Text("Create savings goal", color = TextMain) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal name") }, singleLine = true)
            OutlinedTextField(value = targetText, onValueChange = { targetText = it.filter { c -> c.isDigit() || c == '.' }.take(12) }, label = { Text("Target amount") }, prefix = { Text("₱") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        } },
        confirmButton = { TextButton(onClick = { val n = targetText.toDoubleOrNull(); if (name.isNotBlank() && n != null && n > 0) onSave(name.trim(), n) }) { Text("Create goal", color = Green) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } })
}
