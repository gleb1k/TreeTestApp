package ru.glebik.treetestapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.glebik.treetestapp.data.Node
import ru.glebik.treetestapp.data.NodeSerializer
import ru.glebik.treetestapp.ui.theme.TreeTestAppTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TreeTestAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TreeView(viewModel)
                }
            }
        }
    }

    override fun onStop() {
        viewModel.save()
        super.onStop()
    }
}

@Composable
fun TreeView(viewModel: MainViewModel) {

    val currentNode by viewModel.node.collectAsStateWithLifecycle()

    //чтобы рекомпозировать экран, тк я не меняю стейт из-за mutableList'a
    var recomposeToggleState by remember { mutableStateOf(true) }
    fun manualRecompose() {
        recomposeToggleState = !recomposeToggleState
    }
    LaunchedEffect(recomposeToggleState) {}

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(text = "Current Node: ${currentNode.getName()}")
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    viewModel.goBack()
                },
            ) {
                Text(text = "Go to parent")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                onClick = {
                    viewModel.addChild()
                    manualRecompose()
                },
            ) {
                Text(text = "Add child")
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "Childs:")
        Divider()
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                currentNode.childs,
                key = {
                    it.id
                }
            ) { child ->
                ChildItem(
                    node = child,
                    onDeleteClick = {
                        viewModel.deleteChild(it)
                        manualRecompose()
                    },
                    onGoClick = {
                        viewModel.goToChild(it)
                    })
            }
        }
    }
}

@Composable
fun ChildItem(
    node: Node,
    onDeleteClick: (Node) -> Unit,
    onGoClick: (Node) -> Unit,
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = node.getName())
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onGoClick(node) },
                ) {
                    Text(text = "Go to")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    onClick = { onDeleteClick(node) },
                ) {
                    Text(text = "Delete")
                }
            }
        }
    }
}

class MainViewModel(
    //ТАК ЛУЧШЕ НЕ ДЕЛАТЬ
    private val context: Context,
    private val nodeSerializer: NodeSerializer,
) : ViewModel() {

    private val _node = MutableStateFlow<Node>(Node())
    val node: StateFlow<Node>
        get() = _node.asStateFlow()

    init {
        getNode()
    }

    fun addChild() {
        viewModelScope.launch {
            _node.value.addChild()
            _node.emit(_node.value)
        }
    }

    fun deleteChild(child: Node) {
        viewModelScope.launch {
            _node.value.deleteChild(child)
            _node.emit(_node.value)
        }
    }

    fun goToChild(child: Node) {
        viewModelScope.launch {
            _node.emit(child)
        }
    }

    fun goBack() {
        viewModelScope.launch {
            _node.value.parent?.let {
                _node.emit(it)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val root = _node.value.getRoot()
            val file = File(context.filesDir, "node_tree.ser")

            nodeSerializer.serialize(root, file)
        }
    }

    private fun getNode() {
        viewModelScope.launch {

            val file = File(context.filesDir, "node_tree.ser")

            val restoredNode = nodeSerializer.deserialize(file)

            if (restoredNode == null)
                _node.emit(Node())
            else
                _node.emit(restoredNode)
        }
    }
}