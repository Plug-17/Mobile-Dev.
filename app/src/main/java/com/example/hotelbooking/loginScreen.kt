package com.example.hotelbooking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email = rememberTextFieldState()
    var password = rememberTextFieldState()
    var passVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail = rememberTextFieldState()
    var localError by remember { mutableStateOf<String?>(null) }


    val authVM = viewModel<AuthViewModel>()
    val authState by authVM.authState.collectAsState()
    LaunchedEffect(authState) {
        when(authState){
            is AuthViewModel.AuthState.Success -> {
                authVM.resetState()
                onLoginSuccess()
            }
            else -> {}
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                resetEmail.clearText()
            },
            title = { Text("ลืมรหัสผ่าน") },
            text = {
                Column {
                    Text("กรอก Email ที่ใช้สมัครสมาชิก\nระบบจะส่งลิงก์รีเซ็ตรหัสผ่านให้")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        state = resetEmail,
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        lineLimits = TextFieldLineLimits.SingleLine,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { authVM.resetPassword(resetEmail.text.toString()); showForgotDialog = false; resetEmail.clearText() },
                    enabled = resetEmail.text.isNotBlank(),
                ) { Text("ส่ง Email", color = Color(0xFF6D9E51)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetEmail.clearText()
                }) { Text("ยกเลิก", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_home),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFE3DBBB)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sign in", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

        //------------------- TextField กรอก Email และ Password -------------------
        OutlinedTextField(
            state = email,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedSecureTextField(
            state = password,
            textObfuscationMode = if (passVisible) {
                TextObfuscationMode.Visible
            } else {
                TextObfuscationMode.Hidden
            },
            label = { Text("Password") },
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passVisible) R.drawable.visibility_24px_2 else R.drawable.visibility_off_24px_2
                        ),
                        contentDescription = if (passVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        //------------------- ปุ่มลืมรหัสผ่าน -------------------
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                resetEmail.setTextAndPlaceCursorAtEnd(email.text.toString())
                showForgotDialog = true
            }) {
                Text("ลืมรหัสผ่าน?")
            }
        }
        val errorMessage = localError ?: (authState as? AuthViewModel.AuthState.Error)?.message
        if (!errorMessage.isNullOrEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))

        //------------------- ปุ่มเข้าสู่ระบบ -------------------
        Button(
            onClick = { authVM.loginWithEmail(email.text.toString(), password.text.toString()); localError = null },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(6.dp),
            enabled = email.text.isNotBlank() && password.text.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6D9E51),
                contentColor = Color.White
            )
        ) { Text("เข้าสู่ระบบ", color = Color.White) }

        Spacer(Modifier.height(12.dp))

        //------------------- ปุ่มไปหน้าลงทะเบียน -------------------
        TextButton(onClick = onNavigateToRegister) {
            Text("ยังไม่เป็นสมาชิก? สมัครสมาชิก")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        //------------------- Icon สำหรับล็อกอินผ่านโซเชียล -------------------
//        Row {
//            IconButton(
//                onClick = {  },
//                modifier = Modifier
//                    .size(56.dp)
//                    .border(1.dp, Color.LightGray, CircleShape)
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.search),
//                    contentDescription = "Sign in with Google",
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//            Spacer(Modifier.width(12.dp))
//
//            IconButton(
//                onClick = { },
//                modifier = Modifier
//                    .size(56.dp)
//                    .border(1.dp, Color.LightGray, CircleShape)
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.facebook),
//                    contentDescription = "Sign in with Google",
//                    modifier = Modifier.size(38.dp)
//                )
//            }
//        }
        OutlinedButton (
            onClick = { authVM.loginWithGoogle(context); localError = null},
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("Sign in with Google", color = Color.Black)
        }
    }
}
