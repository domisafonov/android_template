@file:OptIn(ExperimentalMaterial3Api::class)

package net.domisafonov.templateproject.ui.tenthcharacterscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.ui.COMPACT_VIEW_CHAR_LIMIT

@Composable
fun TenthCharacterScreen(
    modifier: Modifier = Modifier,
    doCompactView: Boolean = false,
) {
    if (LocalInspectionMode.current) {
        if (doCompactView) {
            TenthCharacterScreenUiCompactPreview(modifier = modifier)
        } else {
            TenthCharacterScreenUiFullPreview(modifier = modifier)
        }
        return
    }

    val viewModel: TenthCharacterScreenViewModel = hiltViewModel()

    val text by viewModel.text.map { it.orEmpty() }.collectAsState(initial = "")
    val pullRefreshState = rememberPullToRefreshState()
    val isRefreshCompleted by viewModel.isRefreshCompleted.collectAsState()

    if (!doCompactView) {
        if (pullRefreshState.isRefreshing) {
            LaunchedEffect(pullRefreshState.isRefreshing) {
                viewModel.setRefreshing(true)
            }
        }

        if (isRefreshCompleted) {
            pullRefreshState.endRefresh()
            viewModel.setRefreshing(false)
        }
    }

    TenthCharacterScreenUi(
        doCompactView = doCompactView,
        text = text,
        modifier = modifier,
        pullRefreshState = pullRefreshState,
    )
}

@Composable
private fun TenthCharacterScreenUi(
    doCompactView: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    pullRefreshState: PullToRefreshState = rememberPullToRefreshState(),
) {
    if (doCompactView) {
        Text(
            text = text.take(COMPACT_VIEW_CHAR_LIMIT),
            modifier = modifier.fillMaxSize(),
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        Box(
            modifier = Modifier
                .nestedScroll(connection = pullRefreshState.nestedScrollConnection)
                .fillMaxSize()
        ) {
            Text(
                text = text,
                modifier = modifier.verticalScroll(rememberScrollState()),
            )
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TenthCharacterScreenUiCompactPreview(modifier: Modifier = Modifier) {
    TenthCharacterScreenUi(
        doCompactView = true,
        text = "The Tenth Text",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun TenthCharacterScreenUiFullPreview(modifier: Modifier = Modifier) {
    TenthCharacterScreenUi(
        doCompactView = false,
        text = "The Tenth Text",
        modifier = modifier,
    )
}
