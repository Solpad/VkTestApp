package com.example.vktestapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.vktestapp.db.RecordDatabase
import com.example.vktestapp.repo.RecordRepo
import com.example.vktestapp.screen.login.LoginViewModel
import com.example.vktestapp.screen.main.MainViewModel
import com.example.vktestapp.screen.main.MainViewModelProviderFactory
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.exceptions.VKAuthException

class MainActivity : AppCompatActivity() {

    lateinit var mainViewModel: MainViewModel
    lateinit var loginViewModel: LoginViewModel

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val recordRepo = RecordRepo(RecordDatabase(this))
        val mainViewModelProviderFactory = MainViewModelProviderFactory(recordRepo,application)
        mainViewModel = ViewModelProvider(this, mainViewModelProviderFactory).get(MainViewModel::class.java)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        supportActionBar?.hide()
        if (!checkPerm(Manifest.permission.RECORD_AUDIO) && !checkPerm(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            askPerm(permissions)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object: VKAuthCallback {
            override fun onLogin(token: VKAccessToken) { loginViewModel.auth = true }
            override fun onLoginFailed(authException: VKAuthException) {
                Toast.makeText(applicationContext, "Ошибка авторизации", Toast.LENGTH_LONG).show()
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback))
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkPerm(permission: String): Boolean {
        ActivityCompat.checkSelfPermission(this, permission)
        return ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun askPerm(permissions: Array<String>) { ActivityCompat.requestPermissions(this, permissions, 1) }
    private fun onPerm(granted: Boolean) { if (!granted) askPerm(permissions) }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        if (results.isNotEmpty()) {
            val perm1 = results[0] == PackageManager.PERMISSION_GRANTED
            onPerm(perm1)
            val perm2 = results[1] == PackageManager.PERMISSION_GRANTED
            onPerm(perm2)
        }
        super.onRequestPermissionsResult(requestCode, permissions, results)
    }
}