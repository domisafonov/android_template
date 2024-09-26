@file:OptIn(ExperimentalMaterial3Api::class)

package net.domisafonov.templateproject.ui.wordcountscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.ui.COMPACT_VIEW_LINES_LIMIT

@Composable
fun WordCountScreen(
    modifier: Modifier = Modifier,
    doCompactView: Boolean = false,
) {
    if (LocalInspectionMode.current) {
        if (doCompactView) {
            WordCountScreenUiCompactPreview(modifier = modifier)
        } else {
            WordCountScreenUiFullPreview(modifier = modifier)
        }
        return
    }

    val viewModel: WordCountScreenViewModel = hiltViewModel()

    val lines by viewModel.wordCountLines.map { it.orEmpty() }.collectAsState(initial = emptyList())

    val pullRefreshState = rememberPullToRefreshState()
    val isRefreshCompleted by viewModel.isRefreshCompleted.collectAsState()

    if (pullRefreshState.isRefreshing) {
        viewModel.setRefreshing(true)
    }

    if (isRefreshCompleted) {
        pullRefreshState.endRefresh()
        viewModel.setRefreshing(false)
    }

    WordCountScreenUi(
        doCompactView = doCompactView,
        lines = lines,
        modifier = modifier,
        pullRefreshState = pullRefreshState,
    )
}

@Composable
private fun WordCountScreenUi(
    doCompactView: Boolean,
    lines: List<String>,
    modifier: Modifier = Modifier,
    pullRefreshState: PullToRefreshState = rememberPullToRefreshState(),
) {
    if (doCompactView) {
        val size = lines.size.coerceAtMost(COMPACT_VIEW_LINES_LIMIT)
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(count = size) { i ->
                Text(text = lines[i])
            }
            if (lines.size > COMPACT_VIEW_LINES_LIMIT) {
                item { Text(text = "...") }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .nestedScroll(connection = pullRefreshState.nestedScrollConnection)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = modifier) {
                items(count = lines.size) { i ->
                    Text(text = lines[i])
                }
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WordCountScreenUiCompactPreview(modifier: Modifier = Modifier) {
    WordCountScreenUi(
        doCompactView = true,
        lines = (1..100).map { "\"$it\": $it" },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun WordCountScreenUiFullPreview(modifier: Modifier = Modifier) {
    WordCountScreenUi(
        doCompactView = false,
        lines = (1..100).map { "\"$it\": $it" },
        modifier = modifier,
    )
}
