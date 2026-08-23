package com.example.mfak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mfak.model.Account
import com.example.mfak.repository.AccountRepository
import com.example.mfak.security.BiometricPromptManager
import com.example.mfak.ui.theme.MFAKTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val biometricPromptManager by lazy {
        BiometricPromptManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MFAKTheme {
                var isUnlocked by remember { mutableStateOf(false) }

                if (!isUnlocked) {
                    LockScreen(onUnlocked = { isUnlocked = true })
                } else {
                    MainAppContent()
                }
            }
        }
    }

    @Composable
    fun LockScreen(onUnlocked: () -> Unit) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            biometricPromptManager.showBiometricPrompt(
                title = "Desbloquear MFAK",
                subtitle = "Use seu PIN ou Biometria",
                onSuccess = { onUnlocked() },
                onError = { code, err ->
                    Toast.makeText(context, "Erro: $err", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("App Bloqueado", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    biometricPromptManager.showBiometricPrompt(
                        title = "Desbloquear MFAK",
                        subtitle = "Use seu PIN ou Biometria",
                        onSuccess = { onUnlocked() },
                        onError = { _, _ -> }
                    )
                }) {
                    Text("Tentar Novamente")
                }
            }
        }
    }
}

class MainViewModel(private val repository: AccountRepository) : ViewModel() {
    var accounts by mutableStateOf<List<Account>>(emptyList())
        private set

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        accounts = repository.getAccounts()
    }

    fun addAccount(name: String, email: String, key: String) {
        val newAccount = Account(
            id = UUID.randomUUID().toString(),
            serviceName = name,
            email = email,
            secretKey = key
        )
        repository.addAccount(newAccount)
        loadAccounts()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val repository = remember { AccountRepository(context) }
    val viewModel: MainViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    })

    var showAddDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MFAK - Suas Contas") },
                actions = {
                    IconButton(onClick = { viewModel.loadAccounts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showScanner = true },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Manual")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (viewModel.accounts.isEmpty()) {
                Text("Nenhuma conta adicionada", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.accounts) { account ->
                        AccountCard(account)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddAccountDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, email, key ->
                    viewModel.addAccount(name, email, key)
                    showAddDialog = false
                }
            )
        }

        if (showScanner) {
            // Aqui entraria a implementação da CameraX para o QR Code
            // Por brevidade no protótipo, simulamos o scanner
            Toast.makeText(context, "Scanner de QR em breve...", Toast.LENGTH_SHORT).show()
            showScanner = false
        }
    }
}

@Composable
fun AccountCard(account: Account) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(account.serviceName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(account.email, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            // Simulação de código TOTP (em real usaria uma lib como a zxing ou similar para gerar)
            Text(
                text = "123 456", 
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AddAccountDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Conta") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Serviço (ex: Google)") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") })
                TextField(value = key, onValueChange = { key = it }, label = { Text("Chave Secreta") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, email, key) }) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
