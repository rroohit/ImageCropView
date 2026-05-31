package com.k.image.cropview.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.k.image.cropview.navigation.SampleExample

/**
 * Landing screen listing the available examples. Tapping a card navigates to that example.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    examples: List<SampleExample>,
    onExampleClick: (SampleExample) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("ImageCropView Samples") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(examples) { example ->
                ExampleCard(
                    example = example,
                    onClick = { onExampleClick(example) },
                )
            }
        }
    }
}

@Composable
private fun ExampleCard(
    example: SampleExample,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = example.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = example.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}