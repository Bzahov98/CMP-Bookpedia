package com.plcoding.bookpedia.book.presentation.book_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.book.domain.BookRepository
import com.plcoding.bookpedia.core.domain.onError
import com.plcoding.bookpedia.core.domain.onSuccess
import com.plcoding.bookpedia.core.presentation.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Presentation -> Domain <- Data
class BookListViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookListState())
    private var searchJob: Job? = null

    //that while there are active subscribers of our state we will execute this whole flow
    //chain here and 5 Seconds more than that so um for at least um 5 Seconds more when the last subscriber disappears and
    //the initial value is just our state
    val state = _state
        .onStart {
            if (cachedBooks.isEmpty()) {
                observeSearchQuery()
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value //
        )

//    init {
//        //will work just fine but it is kind of um a side effect of initializing your
//        //view model you won't be able to initialize your view model without also triggering all that initial data load
//        //possibly this can lead to problems in tests
//        observeSearchQuery()
//    }

    fun onAction(action: BookListAction) {
        when (action) {
            is BookListAction.OnSearchQueryChange -> {
                _state.update {
                    it.copy(searchQuery = action.query)
                }
            }

            is BookListAction.OnBookClick -> {
                // Navigate to detail screen
            }

            is BookListAction.OnTabSelected -> {
                _state.update {
                    it.copy(selectedTabIndex = action.index)
                }
            }
        }
    }

    private var cachedBooks = emptyList<Book>()

    @OptIn(FlowPreview::class)
    // We want to observe the search query so we can listen to each emission
    private fun observeSearchQuery() {
        state.map {
            it.searchQuery
        }.distinctUntilChanged() // Only emit when the query changes
            .debounce(500L) // we only want to search when user stop typing for 500ms
            // then we can listen to each emission so to the queries here where we can then check when the query is
            //blank so if it's empty then we update our state so State update it copy we
            //make sure the error message is reset to null and we want to set our search
            //results to our cached books so cached books will be a simple list here that we
            .onEach { query ->
                when {
                    // If the query is blank, show the cached books
                    query.isBlank() -> {
                        // Show empty state
                        _state.update {
                            it.copy(
                                errorMessage = null,
                                searchResults = cachedBooks
                            )
                        }
                    }
                    //cancels any ongoing search job and starts a new search:
                    query.length >= 2 -> {
                        searchJob?.cancel()
                        searchJob = searchBooks(query)
                    }
                }
            }
            .launchIn(viewModelScope) // ensure it runs within the lifecycle of the ViewModel:
    }

    private fun searchBooks(query: String) = viewModelScope.launch {
        _state.update {
            it.copy(isLoading = true)
        }
        bookRepository
            // calls the searchBooks() of the BookRepo to perform the actual search
            .searchBooks(query)
            .onSuccess { searchResults ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        searchResults = searchResults
                    )
                }
            }
            .onError { error ->
                _state.update {
                    it.copy(
                        searchResults = emptyList(),
                        isLoading = false,
                        errorMessage = error.toUiText()
                    )
                }
            }
    }
}