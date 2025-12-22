package com.example.app.feature.listeners.ui
/*
//import androidx.compose.ui.graphics.vector.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ListenerDashboardScreen() {

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            HeaderSection(username = "John!")

            Spacer(Modifier.height(20.dp))

            OverviewCards()

            Spacer(Modifier.height(20.dp))

            RevenueChartCard()

            Spacer(Modifier.height(20.dp))

            TasksCard()

            Spacer(Modifier.height(20.dp))

            PopularProducts()
        }
    }
}

@Composable
fun HeaderSection(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Good Morning, $username",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
    }
}

@Composable
fun OverviewCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OverviewCard("Total Sales", "$4,200", "+1.5%")
        OverviewCard("Total Visitors", "18,729", "+0.3%")
    }
}

@Composable
fun OverviewCard(title: String, value: String, change: String) {
    Card(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.secondary)
            Text(value, color = MaterialTheme.colorScheme.onSurface)
            Text(change, color = Color.Green, fontSize = 12.sp)
        }
    }
}

@Composable
fun RevenueChartCard() {
    val revenueData = listOf(20f, 40f, 30f, 60f, 45f, 70f, 55f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Revenue", color = MaterialTheme.colorScheme.secondary)
            Text("+2%", color = Color.Green)

            Spacer(Modifier.height(12.dp))

            RevenueLineChartCurvy(
                data = revenueData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }
    }
}

/* ===========================
   CURVY CANVAS CHART
   =========================== */

@Composable
fun RevenueLineChartCurvy(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f

    Canvas(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {

        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * stepX,
                y = height - (value / maxValue) * height
            )
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)

            for (i in 0 until points.lastIndex) {
                val current = points[i]
                val next = points[i + 1]
                val midX = (current.x + next.x) / 2

                cubicTo(
                    midX, current.y,
                    midX, next.y,
                    next.x, next.y
                )
            }
        }

        drawPath(
            path = path,
            color = Color.Green,
            style = Stroke(
                width = 6f,
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
fun TasksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tasks Done", color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 0.6f,
                color = Color(0xFFFF8000)
            )
        }
    }
}

@Composable
fun PopularProducts() {
    Column {
        Text("Popular Products", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        ProductCard("Creative Bag", "Available", "14k views")
        ProductCard("Electric Mug", "Available", "8k views")
    }
}

@Composable
fun ProductCard(name: String, status: String, views: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Medium)
                Text(views, color = Color.Gray, fontSize = 12.sp)
            }

            Text(status, color = Color.Green, fontSize = 12.sp)
        }
    }
}

/*
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ListenerDashboardScreen() {
//    val systemUi = rememberSystemUiController()
//    systemUi.setStatusBarColor(Color(0xFF0E0E10))

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            HeaderSection(username = "John!")

            Spacer(Modifier.height(20.dp))

            OverviewCards()

            Spacer(Modifier.height(20.dp))

            RevenueChartCard()

            Spacer(Modifier.height(20.dp))

            TasksCard()

            Spacer(Modifier.height(20.dp))

            PopularProducts()

        }
    }
}

@Composable
fun HeaderSection(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Good Morning, $username",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // Profile Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
    }
}

@Composable
fun OverviewCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        OverviewCard(title = "Total Sales", value = "$4,200", change = "+1.5%")
        OverviewCard(title = "Total Visitors", value = "18,729", change = "+0.3%")
    }
}

@Composable
fun OverviewCard(title: String, value: String, change: String) {
    Card(
        modifier = Modifier
//            .weight(1f)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
//            Text(title, color = Color.Gray, fontSize = 12.sp)
//            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(title, color = MaterialTheme.colorScheme.secondary)
            Text(value, color = MaterialTheme.colorScheme.onSurface)
            Text(change, color = Color.Green, fontSize = 12.sp)
        }
    }
}

@Composable
fun RevenueChartCard() {
    val revenueData = listOf(20f, 40f, 30f, 60f, 45f, 70f, 55f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Revenue", color = MaterialTheme.colorScheme.secondary)
            Text("+2%", color = Color.Green)

            Spacer(Modifier.height(12.dp))

            RevenueLineChart(
                data = revenueData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }
    }
}

@Composable
fun RevenueLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f

    Canvas(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {

        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxValue) * height
            Offset(x, y)
        }

        // Line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color.Green,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        // Points
        points.forEach { point ->
            drawCircle(
                color = Color.Green,
                radius = 6f,
                center = point
            )
        }
    }
}

@Composable
fun TasksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tasks Done", color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 0.6f,
                color = Color(0xFFFF8000)
            )
        }
    }
}

@Composable
fun PopularProducts() {
    Column {
        Text("Popular Products", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        ProductCard("Creative Bag", "Available", "14k views")
        ProductCard("Electric Mug", "Available", "8k views")
    }
}

@Composable
fun ProductCard(name: String, status: String, views: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E))
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Medium)
                Text(views, color = Color.Gray, fontSize = 12.sp)
            }

            Text(status, color = Color.Green, fontSize = 12.sp)
        }
    }
}

 */