package com.example.passwordmanager.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.passwordmanager.R
import com.example.passwordmanager.data.model.PasswordData
import com.example.passwordmanager.databinding.ActivityMainBinding
import com.example.passwordmanager.util.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PassViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var passAdapter: PassAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPreferences = getSharedPreferences("com.example.myapp", MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
        viewModel = ViewModelProvider(this@MainActivity)[PassViewModel::class.java]



        CoroutineScope(Dispatchers.Main).launch {
            viewModel.fetchUserList()
            viewModel.passwordDataList.observe(this@MainActivity, Observer { passwordList ->

                if (passwordList.isNullOrEmpty()) {
                    binding.noData.visibility = View.VISIBLE
                    binding.rv.visibility = View.GONE
                    binding.searchView.visibility = View.GONE
                } else {
                    binding.noData.visibility = View.GONE
                    binding.rv.visibility = View.VISIBLE
                    passAdapter =
                        PassAdapter(userList = passwordList, onItemClicked = { passwordData ->
                            val modal = ModalBottomSheetDialogDetails(passwordData,
                                viewModel,
                                onItemDelete = { viewModel.fetchUserList() },
                                onItemEdit = { viewModel.fetchUserList() })
                            supportFragmentManager.let {
                                modal.show(
                                    it, AppConstants.TAG
                                )
                            }
                        })
                    binding.rv.adapter = passAdapter

                    binding.searchView.visibility = View.VISIBLE
                    binding.edMobile.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                            filter(s.toString(), passwordList as ArrayList<PasswordData>)
                        }

                    })

                }
            })
        }

        binding.add.setOnClickListener {
            val modal = ModalBottomSheetDialog(passViewModel = viewModel)
            supportFragmentManager.let { modal.show(it, AppConstants.TAG) }
        }

        if (isFirstLaunch) {
            showPrompts()
        }
    }

    fun filter(text: String, passwordList: ArrayList<PasswordData>) {
        val temp = ArrayList<PasswordData>()
        for (data in passwordList) {
            if (data.accountType.contains(text, ignoreCase = true)) {
                binding.noData.visibility = View.GONE
                binding.rv.visibility = View.VISIBLE
                temp.add(data)
            }else{
                binding.noData.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
            }
        }
        passAdapter.updateList(temp)
    }

    private fun showPrompts() {
        MaterialTapTargetPrompt.Builder(this@MainActivity).setTarget(binding.add)
            .setPrimaryText("Click on add").setSecondaryText("To add your account credentials...")
            .setPromptStateChangeListener { prompt, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSED) {

                }
            }.show()

        // Update the flag to indicate that the prompts have been shown
        val sharedPreferences = getSharedPreferences("com.example.myapp", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstLaunch", false)
        editor.apply()
    }


}