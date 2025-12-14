import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun UserCard(
    name: String,
    age: Int,
    rating: String,
    onTalkNowClick: () -> Unit
) {


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar
//            Image(
//                painter = painterResource(R.drawable.ic_launcher_foreground),
//                contentDescription = null,
//                modifier = Modifier
//                    .size(56.dp)
//                    .clip(CircleShape)
//            )

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Column - Flexible area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Age: $age",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Rating: $rating",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Button
            Button(
                onClick = onTalkNowClick,
                modifier = Modifier.widthIn(min = 90.dp)   // prevents shrinking
            ) {
                Text("Talk Now")
            }
        }
    }
}
