package net.domisafonov.templateproject.ui.mainscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.domisafonov.templateproject.R
import net.domisafonov.templateproject.ui.tenthcharacterscreen.TenthCharacterScreen
import net.domisafonov.templateproject.ui.tenthcharacterscreen.TenthCharacterScreenUiCompactPreview
import net.domisafonov.templateproject.ui.topbar.AppBarController
import net.domisafonov.templateproject.ui.wordcountscreen.WordCountScreen
import net.domisafonov.templateproject.ui.wordcountscreen.WordCountScreenUiCompactPreview

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onTenthClick: () -> Unit,
    onWordCountClick: () -> Unit,
    appBarController: AppBarController<MainScreenAppBarState, Unit>,
) {
    val viewModel: MainScreenViewModel = hiltViewModel()
    val isActivated by viewModel.isActivated.collectAsState()

    if (isActivated) {
        appBarController.setState(MainScreenAppBarState(isActivated = true))
        ActivatedUi(
            modifier = modifier,
            onTenthClick = onTenthClick,
            onWordCountClick = onWordCountClick,
        )
    } else {
        appBarController.setState(MainScreenAppBarState(isActivated = false))
        NonActivatedUi(
            modifier = modifier,
            onButtonClick = { viewModel.onButtonClick() },
        )
    }
}

@Composable
private fun ActivatedUi(
    modifier: Modifier = Modifier,
    onTenthClick: () -> Unit = {},
    onWordCountClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { onTenthClick() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.tenth_screen_label),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall,
                )

                if (LocalInspectionMode.current) {
                    TenthCharacterScreenUiCompactPreview(modifier = Modifier.fillMaxSize())
                } else {
                    TenthCharacterScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        doCompactView = true,
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { onWordCountClick() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.word_count_screen_label),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall,
                )

                if (LocalInspectionMode.current) {
                    WordCountScreenUiCompactPreview(modifier = Modifier.fillMaxSize())
                } else {
                    WordCountScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        doCompactView = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun NonActivatedUi(
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = onButtonClick,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Do it!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivatedUiPreview() {
    ActivatedUi()
}

@Preview(showBackground = true)
@Composable
fun NonActivatedUiPreview() {
    NonActivatedUi()
}
