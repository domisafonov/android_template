package net.domisafonov.templateproject.ui.mainscreen.urldialog

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.parcelize.Parcelize
import net.domisafonov.templateproject.R

@Composable
fun UrlDialog(
    modifier: Modifier = Modifier,
    coordinator: UrlDialogCoordinator,
) {
    val viewModel: UrlDialogViewModel = hiltViewModel()

    val text by viewModel.text.collectAsState()
    val error by viewModel.error.collectAsState()
    val isDismissedOnSuccess by viewModel.isDismissedOnSuccess.collectAsState()

    LaunchedEffect(isDismissedOnSuccess) {
        if (isDismissedOnSuccess) {
            coordinator.goBack(isSuccessful = true)
        }
    }

    UrlDialogUi(
        text = text,
        error = error,
        modifier = modifier,
        onTextChanged = viewModel::onTextChanged,
        onCancelClick = coordinator::goBack,
        onSaveClick = viewModel::onSaveClick,
    )
}

@Composable
fun UrlDialogUi(
    text: String,
    error: String?,
    modifier: Modifier = Modifier,
    onTextChanged: (value: String) -> Unit = {},
    onCancelClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.url_dialog_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(12.dp)
        )

        TextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            label = { error?.let { Text(text = it) } },
            singleLine = true,
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
            TextButton(onClick = onSaveClick, enabled = error == null) {
                Text(text = stringResource(id = R.string.dialog_save))
            }
        }
    }
}

@Parcelize
class UrlDialogResult : Parcelable

@Preview(showBackground = true)
@Composable
private fun UrlDialogUiNormalPreview() {
    UrlDialogUi(text = "/about/", error = null)
}

@Preview(showBackground = true)
@Composable
private fun UrlDialogUiErrorPreview() {
    UrlDialogUi(text = "/about/", error = stringResource(id = R.string.invalid_url_error))
}
