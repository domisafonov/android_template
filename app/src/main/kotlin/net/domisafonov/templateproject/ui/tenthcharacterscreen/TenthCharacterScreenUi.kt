package net.domisafonov.templateproject.ui.tenthcharacterscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.map
import net.domisafonov.templateproject.ui.COMPACT_VIEW_CHAR_LIMIT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenthCharacterScreenUi(
    modifier: Modifier = Modifier,
    doCompactView: Boolean = false,
) {

    val viewModel: TenthCharacterScreenViewModel = hiltViewModel()

    val text by viewModel.text.map { it.orEmpty() }.collectAsState(initial = "")

    if (doCompactView) {
        Text(
            text = text.take(COMPACT_VIEW_CHAR_LIMIT),
            modifier = modifier,
            overflow = TextOverflow.Ellipsis,
        )
    } else {

        val pullRefreshState = rememberPullToRefreshState()
        val isRefreshCompleted by viewModel.isRefreshCompleted.collectAsState()

        if (pullRefreshState.isRefreshing) {
            viewModel.setRefreshing(isRefreshing = true)
        }

        if (isRefreshCompleted) {
            pullRefreshState.endRefresh()
            viewModel.setRefreshing(isRefreshing = false)
        }

        Box(
            modifier = Modifier
                .nestedScroll(connection = pullRefreshState.nestedScrollConnection)
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
