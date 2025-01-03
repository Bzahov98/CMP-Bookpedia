package com.plcoding.bookpedia.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.book.presentation.SharedSelectedBookViewModel
import com.plcoding.bookpedia.book.presentation.book_list.BookListScreenRoot
import com.plcoding.bookpedia.book.presentation.book_list.BookListViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


@Composable
@Preview
fun App() {
    MaterialTheme {
        // Define the navigation graph Controller
        val navController = rememberNavController()
        // Define properties of the navigation graph
        NavHost(
            navController = navController,
            startDestination = Route.BookGraph
        ) {
            navigation<Route.BookGraph>(
                startDestination = Route.BookList
            ) {
                composable<Route.BookList>() {
                    val bookViewModel = koinViewModel<BookListViewModel>()

                    val selectedBookViewModel =
                        it.sharedKoinViewModel<SharedSelectedBookViewModel>(navController)

                    // with LaunchedEffect true it will run every time the composable enters teh composition
                    // as initial or recomposition (i.e. every time the screen is shown)
                    LaunchedEffect(true) { //
                        // Reset the selected book when the book list screen is shown
                        selectedBookViewModel.onSelectBook(null)
                    }

                    BookListScreenRoot(
                        viewModel = bookViewModel,
                        onBookClick = { book: Book ->
                            // Update the selected book in the shared view model
                            selectedBookViewModel.onSelectBook(book)
                            // Navigate to the book detail screen
                            navController.navigate(
                                Route.BookDetail(book.id)
                            )

                        }
                    )
                }

                composable<Route.BookDetail>() { entry ->
                    val args = entry.toRoute<Route.BookDetail>()
                    val selectedBookViewModel =
                        entry.sharedKoinViewModel<SharedSelectedBookViewModel>(navController)

                    val selectedBook by selectedBookViewModel.selectedBook.collectAsStateWithLifecycle()

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Book Detail Screen:" +
                                " ${args.id} and ${selectedBook?.title}")
                    }
                }
            }
        }
    }
}


// call in navigation<Route.SomeRoute>(){}
@Composable
private inline fun <reified T : ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    // The model VM owner is set Explicitly Scope our VM to parent
    // nav graph rather than nav entry of the single vm
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}