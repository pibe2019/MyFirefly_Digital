package com.example.myfireflydigital.ui.core.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(searchQuery: String, onSearchQueryChanged: (String) -> Unit, modifier: Modifier = Modifier, content:@Composable () -> Unit) {
    SearchBar(
        modifier = modifier.fillMaxWidth(),
        windowInsets = WindowInsets(top=0),////QUITA EL ESPACIO X DEFECTO DE PADDING SUPERIOR
        inputField = {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = { onSearchQueryChanged(it) },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(text = "Buscar ..") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "busqueda") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear,contentDescription = "limpiar busqueda")
                        }
                    }
                }
            )
        },
        expanded = false,
        onExpandedChange = {}
    ) {content()}
}