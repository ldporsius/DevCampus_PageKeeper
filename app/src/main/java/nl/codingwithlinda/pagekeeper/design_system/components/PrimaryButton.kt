package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    text: String,
    onClick: () -> Unit
) {

    @Composable
    fun icon() = iconRes?.let {
        Icon(painter = painterResource(it),
            contentDescription = null,
            Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors().copy(
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(text,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
private fun PreviewPrimaryButton() {
    PageKeeperTheme() {
        PrimaryButton(
            iconRes = R.drawable.import_book,
            text = "Button"
        ) { }
    }
}