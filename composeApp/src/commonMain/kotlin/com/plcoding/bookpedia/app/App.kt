package com.plcoding.bookpedia.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Route.BookGraph
        ) {
//            navigation<Route.BookGraph>(
//                startDestination = Route.BookList
//            ) {
//                composable<Route.BookList>(
//                    exitTransition = { slideOutHorizontally() },
//                    popEnterTransition = { slideInHorizontally() }
//                ) {
//                    val viewModel = koinViewModel<BookListViewModel>()
//                    val selectedBookViewModel =
//                        it.sharedKoinViewModel<SelectedBookViewModel>(navController)
//
//                    LaunchedEffect(true) {
//                        selectedBookViewModel.onSelectBook(null)
//                    }
//
//                    BookListScreenRoot(
//                        viewModel = viewModel,
//                        onBookClick = { book ->
//                            selectedBookViewModel.onSelectBook(book)
//                            navController.navigate(
//                                Route.BookDetail(book.id)
//                            )
//                        }
//                    )
//                }
//                composable<Route.BookDetail>(
//                    enterTransition = { slideInHorizontally { initialOffset ->
//                        initialOffset
//                    } },
//                    exitTransition = { slideOutHorizontally { initialOffset ->
//                        initialOffset
//                    } }
//                ) {
//                    val selectedBookViewModel =
//                        it.sharedKoinViewModel<SelectedBookViewModel>(navController)
//                    val viewModel = koinViewModel<BookDetailViewModel>()
//                    val selectedBook by selectedBookViewModel.selectedBook.collectAsStateWithLifecycle()
//
//                    LaunchedEffect(selectedBook) {
//                        selectedBook?.let {
//                            viewModel.onAction(BookDetailAction.OnSelectedBookChange(it))
//                        }
//                    }
//
//                    BookDetailScreenRoot(
//                        viewModel = viewModel,
//                        onBackClick = {
//                            navController.navigateUp()
//                        }
//                    )
//                }
//            }
        }

    }
}

@Composable
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}