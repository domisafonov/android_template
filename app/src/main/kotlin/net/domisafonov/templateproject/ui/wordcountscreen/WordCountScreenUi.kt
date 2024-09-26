package net.domisafonov.templateproject.ui.wordcountscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.ui.COMPACT_VIEW_LINES_LIMIT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordCountScreenUi(
    modifier: Modifier = Modifier,
    doCompactView: Boolean = false,
) {

    val viewModel: WordCountScreenViewModel = hiltViewModel()

    val lines by viewModel.wordCountLines.map { it.orEmpty() }.collectAsState(initial = emptyList())

    val pullRefreshState = rememberPullToRefreshState()
    val isRefreshCompleted by viewModel.isRefreshCompleted.collectAsState()

    if (pullRefreshState.isRefreshing) {
        viewModel.setRefreshing(isRefreshing = true)
    }

    if (isRefreshCompleted) {
        pullRefreshState.endRefresh()
        viewModel.setRefreshing(isRefreshing = false)
    }

    if (doCompactView) {
        val size = lines.size.coerceAtMost(COMPACT_VIEW_LINES_LIMIT)
        LazyColumn(modifier = modifier) {
            items(count = size) { i ->
                Text(text = lines[i])
            }
            if (lines.size > COMPACT_VIEW_LINES_LIMIT) {
                item() { Text(text = "...") }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .nestedScroll(connection = pullRefreshState.nestedScrollConnection)
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
