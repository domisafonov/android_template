package net.domisafonov.templateproject.ui.mainscreen

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UrlDialog(
    modifier: Modifier = Modifier,
) {
    var urlText by rememberSaveable { mutableStateOf("") }

    TextField(
        value = urlText,
        onValueChange = { urlText = it },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun UrlDialogUi() {
    TODO()
}
