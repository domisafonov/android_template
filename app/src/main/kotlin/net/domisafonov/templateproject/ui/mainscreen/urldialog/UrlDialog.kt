package net.domisafonov.templateproject.ui.mainscreen.urldialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.domisafonov.templateproject.R

@Composable
fun UrlDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val viewModel: UrlDialogViewModel = hiltViewModel()

    val text by viewModel.text.collectAsState()
    val error by viewModel.error.collectAsState()
    val isDismissed by viewModel.isDismissed.collectAsState()

    if (isDismissed) {
        onDismiss()
    }

    UrlDialogUi(
        text = text,
        error = error,
        modifier = modifier,
        onTextChanged = viewModel::onTextChanged,
        onCancelClick = onDismiss,
        onSaveClick = viewModel::onSaveClick,
    )
}

@Composable
fun UrlDialogUi(
    text: String,
    error: String?, // TODO
    modifier: Modifier = Modifier,
    onTextChanged: (value: String) -> Unit = {},
    onCancelClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.url_dialog_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(12.dp)
        )

        TextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancelClick) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
            TextButton(onClick = onSaveClick) {
                Text(text = stringResource(id = R.string.dialog_save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UrlDialogUiNormalPreview() {
    UrlDialogUi(text = "/about/", error = null)
}

@Preview(showBackground = true)
@Composable
fun UrlDialogUiErrorPreview() {
    UrlDialogUi(text = "/about/", error = "error")
}
